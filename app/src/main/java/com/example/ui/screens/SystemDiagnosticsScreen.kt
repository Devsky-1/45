package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.JarvisViewModel
import com.example.ui.components.HoloCard
import com.example.ui.components.TelemetryBadge
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisCardBg
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCardBorderCyan
import com.example.ui.theme.JarvisCardGlass
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisObsidian
import com.example.ui.theme.JarvisPurple
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisSpaceDark
import com.example.ui.theme.JarvisSpaceMid
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun SystemDiagnosticsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val activeProtocol by viewModel.activeProtocol.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        JarvisObsidian,
                        JarvisSpaceDark,
                        JarvisSpaceMid,
                        JarvisObsidian
                    )
                )
            )
            .testTag("diagnostics_screen")
    ) {
        // HEADER
        Surface(
            color = JarvisCardGlass,
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder.copy(alpha = 0.7f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Diagnostics",
                        tint = JarvisCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SYSTEM TELEMETRY & HARDWARE",
                            color = JarvisCyanLight,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "REAL-TIME HARDWARE SENSOR MATRIX",
                            color = JarvisTextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.deviceController.refreshTelemetry() },
                    modifier = Modifier.testTag("btn_refresh_telemetry")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = JarvisCyanLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        val context = LocalContext.current
        val isBackgroundActive by viewModel.isBackgroundServiceActive.collectAsStateWithLifecycle()
        val isFloatingActive by viewModel.isFloatingOverlayActive.collectAsStateWithLifecycle()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // EXTERNAL ASSISTANT & SIRI/GOOGLE ASSISTANT REPLACEMENT CARD
            item {
                HoloCard(
                    borderColor = JarvisCyan,
                    glowEffect = true,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Assistant,
                                    contentDescription = "Assistant Integration",
                                    tint = JarvisCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "EXTERNAL ASSISTANT ENGINE",
                                    color = JarvisCyan,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = JarvisCyanGlow
                            ) {
                                Text(
                                    text = "SIRI / GOOGLE MODE",
                                    color = JarvisCyanLight,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Access J.A.R.V.I.S. from any app or lock screen without opening the main app.",
                            color = JarvisTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 1. Set as Default Digital Assistant Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF091428),
                            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorderCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "DEFAULT PHONE ASSISTANT",
                                            color = JarvisTextPrimary,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Trigger via Long-press Home, Power button hold, or corner swipe",
                                            color = JarvisTextMuted,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            try {
                                                val intent = Intent(Settings.ACTION_VOICE_INPUT_SETTINGS)
                                                context.startActivity(intent)
                                            } catch (_: Exception) {
                                                try {
                                                    val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                                                    context.startActivity(intent)
                                                } catch (_: Exception) {
                                                    val intent = Intent(Settings.ACTION_SETTINGS)
                                                    context.startActivity(intent)
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = JarvisCyan,
                                            contentColor = Color(0xFF002229)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("btn_set_default_assistant")
                                    ) {
                                        Text("CONFIGURE", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 2. Always-on Background Service Switch
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = "Background Service",
                                    tint = JarvisCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "BACKGROUND SERVICE",
                                        color = JarvisTextPrimary,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Persistent notification with Speak, Torch & Briefing quick actions",
                                        color = JarvisTextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Switch(
                                checked = isBackgroundActive,
                                onCheckedChange = { viewModel.toggleBackgroundService(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = JarvisCyan,
                                    checkedTrackColor = JarvisCyanGlow,
                                    uncheckedThumbColor = JarvisTextMuted,
                                    uncheckedTrackColor = JarvisCardBorder
                                ),
                                modifier = Modifier.testTag("switch_background_service")
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 3. Floating Arc Reactor Overlay Switch
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Widgets,
                                    contentDescription = "Floating Overlay",
                                    tint = JarvisAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "FLOATING ARC REACTOR",
                                        color = JarvisTextPrimary,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Draggable on-screen bubble to talk over any app",
                                        color = JarvisTextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Switch(
                                checked = isFloatingActive,
                                onCheckedChange = { enable ->
                                    if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        context.startActivity(intent)
                                    } else {
                                        viewModel.toggleFloatingOverlay(enable)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = JarvisAmber,
                                    checkedTrackColor = JarvisAmber.copy(alpha = 0.4f),
                                    uncheckedThumbColor = JarvisTextMuted,
                                    uncheckedTrackColor = JarvisCardBorder
                                ),
                                modifier = Modifier.testTag("switch_floating_overlay")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 4. Test Assistant Overlay Button
                        Button(
                            onClick = {
                                val intent = Intent(context, JarvisAssistActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                }
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = JarvisCyanGlow,
                                contentColor = JarvisCyanLight
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_test_assistant_overlay")
                        ) {
                            Icon(imageVector = Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TEST VOICE OVERLAY (SIRI/ASSISTANT POPUP)", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ARC REACTOR POWER & BATTERY CARD
            item {
                HoloCard(
                    borderColor = JarvisCyan.copy(alpha = 0.5f),
                    glowEffect = true,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Power",
                                    tint = JarvisCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ARC REACTOR POWER MATRIX",
                                    color = JarvisCyan,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (telemetry.isCharging) JarvisGreen.copy(alpha = 0.2f) else JarvisCyanGlow
                            ) {
                                Text(
                                    text = if (telemetry.isCharging) "EXTERNAL POWER CONNECTED" else "BATTERY DISCHARGING",
                                    color = if (telemetry.isCharging) JarvisGreen else JarvisCyanLight,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${telemetry.batteryLevel}%",
                                color = JarvisTextPrimary,
                                fontSize = 42.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "THERMAL: ${telemetry.batteryTemperatureCelsius}°C",
                                    color = if (telemetry.batteryTemperatureCelsius > 40) JarvisRed else JarvisCyanLight,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "CORE EFFICIENCY: ${telemetry.corePowerEfficiency}%",
                                    color = JarvisTextMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { (telemetry.batteryLevel / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (telemetry.batteryLevel > 20) JarvisCyan else JarvisAmber,
                            trackColor = JarvisCardBorder
                        )
                    }
                }
            }

            // RAM & STORAGE MEMORY BARS
            item {
                HoloCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "NEURAL PROCESSING & MEMORY",
                            color = JarvisGold,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // RAM
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "RAM ALLOCATION",
                                color = JarvisTextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${telemetry.ramUsedMb} MB / ${telemetry.ramTotalMb} MB (${telemetry.ramUsagePercent}%)",
                                color = JarvisTextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        LinearProgressIndicator(
                            progress = { (telemetry.ramUsagePercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = JarvisGold,
                            trackColor = JarvisCardBorder
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Storage
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "INTERNAL FLASH MATRIX",
                                color = JarvisTextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${String.format("%.1f", telemetry.storageFreeGb)} GB Free (${telemetry.storageUsagePercent}% Used)",
                                color = JarvisTextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        LinearProgressIndicator(
                            progress = { (telemetry.storageUsagePercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = JarvisBlue,
                            trackColor = JarvisCardBorder
                        )
                    }
                }
            }

            // HARDWARE TACTICAL CONTROLS (Flashlight, Haptics, Audio)
            item {
                HoloCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "TACTICAL HARDWARE OVERRIDES",
                            color = JarvisCyanLight,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Flashlight
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FlashlightOn,
                                    contentDescription = "Flashlight",
                                    tint = if (telemetry.isFlashlightOn) JarvisAmber else JarvisTextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "TACTICAL ILLUMINATION",
                                        color = JarvisTextPrimary,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "High-output LED emitter beam",
                                        color = JarvisTextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Switch(
                                checked = telemetry.isFlashlightOn,
                                onCheckedChange = { viewModel.deviceController.setFlashlight(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = JarvisAmber,
                                    checkedTrackColor = JarvisAmber.copy(alpha = 0.4f),
                                    uncheckedThumbColor = JarvisTextMuted,
                                    uncheckedTrackColor = JarvisCardBorder
                                ),
                                modifier = Modifier.testTag("switch_flashlight")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Haptic Test
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = "Haptics",
                                    tint = JarvisCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "HAPTIC ACTUATOR",
                                        color = JarvisTextPrimary,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Pulse vibration feedback test",
                                        color = JarvisTextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { viewModel.deviceController.vibrateHaptic(120) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JarvisCyanGlow,
                                    contentColor = JarvisCyanLight
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_test_haptics")
                            ) {
                                Text("PULSE", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }

            // JARVIS SECURITY & STARK PROTOCOLS
            item {
                HoloCard(
                    borderColor = JarvisGold.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Protocols",
                                    tint = JarvisGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "STARK PROTOCOL SUITE",
                                    color = JarvisGold,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Text(
                                text = activeProtocol,
                                color = JarvisCyanLight,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val protocols = listOf(
                            Pair("House Party Protocol", "Engage automated multi-unit response"),
                            Pair("Protocol Clean Slate", "Purge transcripts & reset cache to standby"),
                            Pair("Stealth Mode", "Suppress audio telemetry & reduce signature"),
                            Pair("Perimeter Defense Grid", "Activate sentry radars & scanning")
                        )

                        protocols.forEach { (name, desc) ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (activeProtocol.equals(name, ignoreCase = true)) JarvisCyanGlow else Color(0xFF091222),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (activeProtocol.equals(name, ignoreCase = true)) JarvisCyan else JarvisCardBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        viewModel.processUserQuery(name)
                                    }
                                    .testTag("protocol_${name.replace(" ", "_")}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = name.uppercase(),
                                            color = JarvisTextPrimary,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = desc,
                                            color = JarvisTextMuted,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.PowerSettingsNew,
                                        contentDescription = "Execute",
                                        tint = JarvisCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
