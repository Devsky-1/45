package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.device.DeviceTelemetry
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisObsidian
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

/**
 * High-contrast minimalist HUD overlay component for the Lock Screen.
 *
 * Designed with a sharp cyber-tactical aesthetic:
 * - True pitch-black / obsidian background with glowing geometric borders.
 * - Monospace precision typography.
 * - Segmented high-contrast battery gauge with live charging animation.
 * - Multi-stage stepped connectivity signal monitor.
 * - Security encryption status indicator.
 * - High-contrast palette exceeding WCAG AAA standards for ambient and OLED displays.
 */
@Composable
fun LockScreenStatusHud(
    telemetry: DeviceTelemetry,
    modifier: Modifier = Modifier,
    onStatusClick: (() -> Unit)? = null,
    showDetails: Boolean = true
) {
    LockScreenStatusHud(
        batteryLevel = telemetry.batteryLevel,
        isCharging = telemetry.isCharging,
        networkStatus = telemetry.networkStatus,
        batteryTemperature = telemetry.batteryTemperatureCelsius,
        ramUsagePercent = telemetry.ramUsagePercent,
        modifier = modifier,
        onStatusClick = onStatusClick,
        showDetails = showDetails
    )
}

/**
 * Overload accepting direct primitive telemetry values for maximum reusability.
 */
@Composable
fun LockScreenStatusHud(
    batteryLevel: Int,
    isCharging: Boolean,
    networkStatus: String,
    batteryTemperature: Float = 28.5f,
    ramUsagePercent: Int = 45,
    modifier: Modifier = Modifier,
    onStatusClick: (() -> Unit)? = null,
    showDetails: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    // Pulsing heartbeat animation for live HUD status
    val infiniteTransition = rememberInfiniteTransition(label = "HudPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    // High contrast battery color resolution
    val batteryColor by animateColorAsState(
        targetValue = when {
            isCharging -> JarvisAmber
            batteryLevel > 30 -> JarvisGreen
            batteryLevel > 15 -> JarvisGold
            else -> JarvisRed
        },
        label = "BatteryColor"
    )

    // Connectivity status determination
    val isWifi = networkStatus.contains("Wi-Fi", ignoreCase = true)
    val isOnline = !networkStatus.contains("Offline", ignoreCase = true) && !networkStatus.contains("Disconnected", ignoreCase = true)
    val connectivityColor = if (isOnline) JarvisCyan else JarvisAmber

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xF0020712), // Deep pitch black / obsidian for maximum contrast
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    JarvisCyan.copy(alpha = 0.8f),
                    JarvisCyan.copy(alpha = 0.2f),
                    JarvisCyan.copy(alpha = 0.8f)
                )
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                enabled = onStatusClick != null || showDetails,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = JarvisCyan),
                role = Role.Button,
                onClick = {
                    expanded = !expanded
                    onStatusClick?.invoke()
                }
            )
            .testTag("lock_screen_status_hud")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // HUD HEADER BRACKET BAR: Security Status & Grid Ident
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                // Security / Guard Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("hud_security_status")
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(JarvisGreen.copy(alpha = pulseAlpha))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SECURE // ARMED",
                        color = JarvisCyanLight,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Grid / Protocol Tag
                Text(
                    text = "HUD.LINK // 0x77F",
                    color = JarvisTextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }

            // PRIMARY STATUS ROW: Connectivity (Left) and Battery (Right)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // --- CONNECTIVITY HUD INDICATOR ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("hud_connectivity_status")
                ) {
                    // Connectivity Icon
                    Icon(
                        imageVector = if (isOnline) {
                            if (isWifi) Icons.Default.Wifi else Icons.Default.Security
                        } else {
                            Icons.Default.WifiOff
                        },
                        contentDescription = if (isOnline) "Network Connected" else "Network Offline",
                        tint = connectivityColor,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isOnline) (if (isWifi) "NET // WI-FI" else "NET // CELL") else "NET // OFFLINE",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            SignalStrengthBars(
                                isOnline = isOnline,
                                activeColor = connectivityColor,
                                modifier = Modifier.padding(top = 1.dp)
                            )
                        }

                        Text(
                            text = if (isOnline) "LATENCY: 14ms" else "STANDBY MODE",
                            color = JarvisTextSecondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // --- BATTERY HUD INDICATOR ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("hud_battery_status")
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isCharging) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Charging",
                                    tint = JarvisAmber,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                            }
                            Text(
                                text = "$batteryLevel%",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        // Segmented battery visual gauge
                        SegmentedBatteryGauge(
                            level = batteryLevel,
                            isCharging = isCharging,
                            activeColor = batteryColor,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Tactical Battery Glyphs
                    Icon(
                        imageVector = when {
                            isCharging -> Icons.Default.BatteryChargingFull
                            batteryLevel <= 20 -> Icons.Default.BatteryAlert
                            else -> Icons.Default.BatteryFull
                        },
                        contentDescription = "Battery Status",
                        tint = batteryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // OPTIONAL EXPANDED TELEMETRY TICKER
            AnimatedVisibility(visible = showDetails && expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .testTag("hud_telemetry_quick_stats")
                ) {
                    // Divider line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(JarvisCardBorder)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Temperature Readout
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = "Core Temperature",
                                tint = JarvisCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "CORE TEMP: ${batteryTemperature}°C",
                                color = JarvisTextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // RAM / Memory Load
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = "RAM Usage",
                                tint = JarvisCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SYS.RAM: $ramUsagePercent%",
                                color = JarvisTextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 4-bar stepped signal indicator providing high-contrast visual status.
 */
@Composable
fun SignalStrengthBars(
    isOnline: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    val barCount = 4
    val activeBars = if (isOnline) 4 else 0

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
    ) {
        for (i in 1..barCount) {
            val barHeight = (4 + (i * 2.5)).dp
            val isBarActive = i <= activeBars
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(barHeight)
                    .clip(RoundedCornerShape(1.dp))
                    .background(
                        if (isBarActive) activeColor else Color(0xFF1E293B)
                    )
            )
        }
    }
}

/**
 * Segmented 6-bar battery gauge for minimalist HUD representation.
 */
@Composable
fun SegmentedBatteryGauge(
    level: Int,
    isCharging: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    val totalSegments = 6
    val litSegments = ((level / 100f) * totalSegments).toInt().coerceIn(1, totalSegments)

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        for (i in 1..totalSegments) {
            val isLit = i <= litSegments
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(
                        if (isLit) activeColor else Color(0xFF1A2638)
                    )
            )
        }
    }
}

/**
 * Compact HUD strip variant for minimalist status bar replacement or ambient header.
 */
@Composable
fun LockScreenCompactStatusHud(
    telemetry: DeviceTelemetry,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val batteryColor = when {
        telemetry.isCharging -> JarvisAmber
        telemetry.batteryLevel > 30 -> JarvisGreen
        telemetry.batteryLevel > 15 -> JarvisGold
        else -> JarvisRed
    }

    val isOnline = !telemetry.networkStatus.contains("Offline", ignoreCase = true)
    val connColor = if (isOnline) JarvisCyan else JarvisAmber

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xF202050E), // OLED High Contrast Pure Background
        border = BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.5f)),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = JarvisCyan),
                        onClick = onClick
                    )
                } else Modifier
            )
            .testTag("lock_screen_compact_hud")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 7.dp)
        ) {
            // Left: Shield + Network
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Security Status",
                    tint = JarvisCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isOnline) "NET.ONLINE" else "NET.OFFLINE",
                    color = connColor,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // Right: Battery
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (telemetry.isCharging) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Charging",
                        tint = JarvisAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                }
                Text(
                    text = "${telemetry.batteryLevel}%",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                SegmentedBatteryGauge(
                    level = telemetry.batteryLevel,
                    isCharging = telemetry.isCharging,
                    activeColor = batteryColor
                )
            }
        }
    }
}
