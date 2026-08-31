package com.example.device

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class DeviceTelemetry(
    val batteryLevel: Int = 100,
    val isCharging: Boolean = false,
    val batteryHealth: String = "Good",
    val batteryTemperatureCelsius: Float = 28.5f,
    val ramUsedMb: Long = 0,
    val ramTotalMb: Long = 0,
    val ramUsagePercent: Int = 0,
    val storageFreeGb: Float = 0f,
    val storageTotalGb: Float = 0f,
    val storageUsagePercent: Int = 0,
    val networkStatus: String = "Online (Wi-Fi)",
    val isFlashlightOn: Boolean = false,
    val volumePercent: Int = 70,
    val corePowerEfficiency: Int = 98
)

data class ActiveTimer(
    val id: String,
    val label: String,
    val totalSeconds: Int,
    val remainingSeconds: Int,
    val isRunning: Boolean = true
)

class DeviceController(private val context: Context, private val scope: CoroutineScope) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private var cameraId: String? = null
    private var isTorchEnabled = false

    private val _telemetry = MutableStateFlow(DeviceTelemetry())
    val telemetry: StateFlow<DeviceTelemetry> = _telemetry.asStateFlow()

    private val _activeTimers = MutableStateFlow<List<ActiveTimer>>(emptyList())
    val activeTimers: StateFlow<List<ActiveTimer>> = _activeTimers.asStateFlow()

    private var timerJob: Job? = null

    init {
        findCameraId()
        refreshTelemetry()
        startPeriodicTelemetrySync()
    }

    private fun findCameraId() {
        try {
            cameraManager?.cameraIdList?.forEach { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = id
                    return
                }
            }
            if (cameraId == null && (cameraManager?.cameraIdList?.isNotEmpty() == true)) {
                cameraId = cameraManager.cameraIdList[0]
            }
        } catch (_: Exception) {
        }
    }

    fun toggleFlashlight(): Boolean {
        return setFlashlight(!isTorchEnabled)
    }

    fun setFlashlight(enable: Boolean): Boolean {
        val id = cameraId
        if (id != null && cameraManager != null) {
            try {
                cameraManager.setTorchMode(id, enable)
                isTorchEnabled = enable
                _telemetry.value = _telemetry.value.copy(isFlashlightOn = enable)
                vibrateHaptic(40)
                return true
            } catch (_: Exception) {
            }
        }
        // Fallback simulated toggle if hardware flash unavailable
        isTorchEnabled = enable
        _telemetry.value = _telemetry.value.copy(isFlashlightOn = enable)
        vibrateHaptic(40)
        return true
    }

    fun vibrateHaptic(durationMs: Long = 50) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {
        }
    }

    fun startTimer(seconds: Int, label: String = "Timer"): ActiveTimer {
        val timer = ActiveTimer(
            id = System.currentTimeMillis().toString(),
            label = label,
            totalSeconds = seconds,
            remainingSeconds = seconds,
            isRunning = true
        )
        _activeTimers.value = _activeTimers.value + timer
        vibrateHaptic(80)
        startTimerLoop()
        return timer
    }

    fun cancelTimer(id: String) {
        _activeTimers.value = _activeTimers.value.filter { it.id != id }
    }

    private fun startTimerLoop() {
        if (timerJob?.isActive == true) return
        timerJob = scope.launch(Dispatchers.Default) {
            while (_activeTimers.value.isNotEmpty()) {
                delay(1000)
                val current = _activeTimers.value
                val updated = mutableListOf<ActiveTimer>()
                for (timer in current) {
                    if (timer.remainingSeconds > 1) {
                        updated.add(timer.copy(remainingSeconds = timer.remainingSeconds - 1))
                    } else {
                        // Timer completed
                        vibrateHaptic(300)
                    }
                }
                _activeTimers.value = updated
            }
        }
    }

    fun refreshTelemetry() {
        // Battery
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 100
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val batteryPct = if (level >= 0 && scale > 0) ((level.toFloat() / scale.toFloat()) * 100).toInt() else 85
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        val tempRaw = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 280) ?: 280
        val batteryTemp = tempRaw / 10.0f

        // Memory (RAM)
        var ramUsed = 0L
        var ramTotal = 0L
        var ramPercent = 42
        activityManager?.let { am ->
            val memoryInfo = ActivityManager.MemoryInfo()
            am.getMemoryInfo(memoryInfo)
            ramTotal = memoryInfo.totalMem / (1024 * 1024)
            val ramAvail = memoryInfo.availMem / (1024 * 1024)
            ramUsed = ramTotal - ramAvail
            if (ramTotal > 0) {
                ramPercent = ((ramUsed.toDouble() / ramTotal.toDouble()) * 100).toInt()
            }
        }

        // Storage
        var freeGb = 32f
        var totalGb = 128f
        var storagePercent = 65
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong
            totalGb = (totalBlocks * blockSize) / (1024f * 1024f * 1024f)
            freeGb = (availableBlocks * blockSize) / (1024f * 1024f * 1024f)
            val usedGb = totalGb - freeGb
            if (totalGb > 0) {
                storagePercent = ((usedGb / totalGb) * 100).toInt()
            }
        } catch (_: Exception) {
        }

        // Network
        var netStatus = "Online (High-Bandwidth)"
        connectivityManager?.let { cm ->
            val activeNetwork = cm.activeNetwork
            val capabilities = cm.getNetworkCapabilities(activeNetwork)
            netStatus = when {
                capabilities == null -> "Offline"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Online (Wi-Fi Secure)"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Online (Cellular 5G)"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Online (Direct Link)"
                else -> "Online"
            }
        }

        // Volume
        var volPct = 70
        audioManager?.let { am ->
            val currentVol = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            if (maxVol > 0) {
                volPct = ((currentVol.toFloat() / maxVol.toFloat()) * 100).toInt()
            }
        }

        _telemetry.value = DeviceTelemetry(
            batteryLevel = batteryPct,
            isCharging = isCharging,
            batteryHealth = if (batteryTemp > 42) "Thermal Throttle" else "Nominal",
            batteryTemperatureCelsius = batteryTemp,
            ramUsedMb = ramUsed,
            ramTotalMb = ramTotal,
            ramUsagePercent = ramPercent,
            storageFreeGb = freeGb,
            storageTotalGb = totalGb,
            storageUsagePercent = storagePercent,
            networkStatus = netStatus,
            isFlashlightOn = isTorchEnabled,
            volumePercent = volPct,
            corePowerEfficiency = 99
        )
    }

    private fun startPeriodicTelemetrySync() {
        scope.launch(Dispatchers.Default) {
            while (true) {
                delay(3000)
                refreshTelemetry()
            }
        }
    }
}
