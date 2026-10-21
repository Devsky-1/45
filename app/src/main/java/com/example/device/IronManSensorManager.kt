package com.example.device

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.sqrt

data class VisorMotionData(
    val pitchDegrees: Float = 0f,
    val rollDegrees: Float = 0f,
    val compassDegrees: Float = 0f,
    val compassHeadingText: String = "000° N",
    val gForce: Float = 1.0f,
    val isSentryArmed: Boolean = false,
    val isSentryAlertTriggered: Boolean = false
)

/**
 * Real-time Hardware Sensor Manager for Iron Man Helmet Visor HUD & Sentry Perimeter Mode.
 * Reads device Accelerometer and Magnetometer to calculate authentic 3D Flight Horizon & Bearing.
 */
class IronManSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _motionData = MutableStateFlow(VisorMotionData())
    val motionData: StateFlow<VisorMotionData> = _motionData.asStateFlow()

    private val gravityValues = FloatArray(3)
    private val geomagneticValues = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var lastAccelerationNorm = SensorManager.GRAVITY_EARTH
    var onSentryBreachDetected: (() -> Unit)? = null

    fun startListening() {
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    fun setSentryArmed(armed: Boolean) {
        _motionData.value = _motionData.value.copy(
            isSentryArmed = armed,
            isSentryAlertTriggered = false
        )
    }

    fun acknowledgeSentryAlert() {
        _motionData.value = _motionData.value.copy(isSentryAlertTriggered = false)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                // Low-pass filter for smooth flight horizon
                gravityValues[0] = gravityValues[0] * 0.8f + event.values[0] * 0.2f
                gravityValues[1] = gravityValues[1] * 0.8f + event.values[1] * 0.2f
                gravityValues[2] = gravityValues[2] * 0.8f + event.values[2] * 0.2f
                hasGravity = true

                // Calculate G-force
                val currentNorm = sqrt(
                    event.values[0] * event.values[0] +
                    event.values[1] * event.values[1] +
                    event.values[2] * event.values[2]
                )
                val gForce = currentNorm / SensorManager.GRAVITY_EARTH

                // Check Sentry Mode Tripwire
                val isArmed = _motionData.value.isSentryArmed
                val isTriggered = _motionData.value.isSentryAlertTriggered
                if (isArmed && !isTriggered) {
                    val delta = abs(currentNorm - lastAccelerationNorm)
                    if (delta > 2.8f) { // Significant device movement / displacement detected
                        _motionData.value = _motionData.value.copy(isSentryAlertTriggered = true)
                        onSentryBreachDetected?.invoke()
                    }
                }
                lastAccelerationNorm = currentNorm

                updateOrientation(gForce)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                geomagneticValues[0] = geomagneticValues[0] * 0.8f + event.values[0] * 0.2f
                geomagneticValues[1] = geomagneticValues[1] * 0.8f + event.values[1] * 0.2f
                geomagneticValues[2] = geomagneticValues[2] * 0.8f + event.values[2] * 0.2f
                hasGeomagnetic = true

                updateOrientation(_motionData.value.gForce)
            }
        }
    }

    private fun updateOrientation(gForce: Float) {
        if (hasGravity && hasGeomagnetic) {
            val success = SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                gravityValues,
                geomagneticValues
            )
            if (success) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles)

                // Azimuth / Compass heading in degrees (0 to 360)
                var azimuthDeg = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                if (azimuthDeg < 0) azimuthDeg += 360f

                // Pitch degrees (-90 to +90)
                val pitchDeg = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()

                // Roll degrees (-180 to +180)
                val rollDeg = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

                val headingText = formatCompassHeading(azimuthDeg)

                _motionData.value = _motionData.value.copy(
                    pitchDegrees = pitchDeg,
                    rollDegrees = rollDeg,
                    compassDegrees = azimuthDeg,
                    compassHeadingText = headingText,
                    gForce = gForce
                )
                return
            }
        }

        // Fallback pitch/roll calculation from gravity if magnetometer absent
        if (hasGravity) {
            val ax = gravityValues[0]
            val ay = gravityValues[1]
            val az = gravityValues[2]

            val pitch = Math.toDegrees(kotlin.math.atan2(-ay.toDouble(), kotlin.math.sqrt((ax * ax + az * az).toDouble()))).toFloat()
            val roll = Math.toDegrees(kotlin.math.atan2(ax.toDouble(), az.toDouble())).toFloat()

            _motionData.value = _motionData.value.copy(
                pitchDegrees = pitch,
                rollDegrees = roll,
                gForce = gForce
            )
        }
    }

    private fun formatCompassHeading(degrees: Float): String {
        val degInt = degrees.toInt().coerceIn(0, 360)
        val cardinal = when (degInt) {
            in 338..360, in 0..22 -> "N"
            in 23..67 -> "NE"
            in 68..112 -> "E"
            in 113..157 -> "SE"
            in 158..202 -> "S"
            in 203..247 -> "SW"
            in 248..292 -> "W"
            in 293..337 -> "NW"
            else -> "N"
        }
        return String.format("%03d° %s", degInt, cardinal)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
