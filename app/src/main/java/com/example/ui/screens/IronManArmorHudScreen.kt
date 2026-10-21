package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.StarkArmorModel
import com.example.ui.JarvisViewModel
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCardBg
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisObsidian
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisSpaceDark
import com.example.ui.theme.JarvisSpaceMid
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import java.util.Locale

/**
 * Iron Man Mark Armor Tactical Visor HUD Screen.
 *
 * Implements a real-life Iron Man Helmet Flight HUD:
 * - Live Gyroscope/Accelerometer 3D Artificial Horizon & Pitch Ladder that tilts in real-time.
 * - Magnetic Compass 360° Azimuth Bearing with cardinal heading.
 * - Interactive Central Target Acquisition Reticle with rangefinder & stepped lock chirps.
 * - Tactile Repulsor Blast trigger with capacitor charge, haptics, and flashlight strobe.
 * - Armor Suit Selection (Mark III, VII, XLIV Hulkbuster, Mark L, Mark LXXXV).
 * - Veronica Orbital Support, House Party Protocol, Arc Reactor Overclock, and Sentry Mode tripwire.
 */
@Composable
fun IronManArmorHudScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    BackHandler { onNavigateBack() }

    val visorMotion by viewModel.visorMotionData.collectAsStateWithLifecycle()
    val armorStatus by viewModel.armorSuitStatus.collectAsStateWithLifecycle()
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val jarvisState by viewModel.jarvisState.collectAsStateWithLifecycle()

    val currentArmor = armorStatus.selectedArmor
    val themePrimary = currentArmor.primaryColor
    val themeAccent = currentArmor.accentColor

    val infiniteTransition = rememberInfiniteTransition(label = "VisorTransitions")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "VisorPulse"
    )

    val reticleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ReticleRotate"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF030A18),
                        Color(0xFF01040A),
                        Color(0xFF000000)
                    ),
                    radius = 1600f
                )
            )
            .testTag("iron_man_visor_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // 1. VISOR TOP HUD BAR: Compass Azimuth, Armor Model & Arc Reactor Output
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 6.dp)
            ) {
                // Back Button & Suit Designation
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FFFFFF))
                            .testTag("visor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Dashboard",
                            tint = themeAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = currentArmor.codeName,
                            color = themePrimary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = currentArmor.designation.uppercase(),
                            color = JarvisTextSecondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Compass Azimuth & G-Force
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "HEADING // ${visorMotion.compassHeadingText}",
                        color = themeAccent,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "G-FORCE: ${String.format(Locale.getDefault(), "%.2f", visorMotion.gForce)} G",
                        color = JarvisCyanLight,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Arc Reactor Power Output
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (armorStatus.isArcReactorOverclocked) Color(0x33FF1744) else Color(0x2200F0FF),
                    border = BorderStroke(
                        1.dp,
                        if (armorStatus.isArcReactorOverclocked) JarvisRed else JarvisCyan
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Arc Reactor Power",
                            tint = if (armorStatus.isArcReactorOverclocked) JarvisRed else JarvisCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${armorStatus.arcReactorOutputGigawatts} GW",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // SENTRY BREACH ALERT BANNER (If Motion Detected)
            AnimatedVisibility(
                visible = visorMotion.isSentryAlertTriggered,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xD9FF1744),
                    border = BorderStroke(1.dp, Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("sentry_breach_banner")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Sentry Warning",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PERIMETER MOTION BREACH DETECTED!",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { viewModel.acknowledgeSentryAlert() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("DISARM", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. MAIN CENTER HUD VIEWPORT: 3D Flight Horizon & Targeting Reticle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(
                                themeAccent.copy(alpha = 0.6f * pulseGlow),
                                Color(0x1100F0FF),
                                themeAccent.copy(alpha = 0.6f * pulseGlow)
                            )
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .background(Color(0x66000000))
            ) {
                // Real-time Flight Horizon Canvas responding to Pitch & Roll
                ArtificialFlightHorizon(
                    pitch = visorMotion.pitchDegrees,
                    roll = visorMotion.rollDegrees,
                    primaryColor = themeAccent,
                    modifier = Modifier.fillMaxSize()
                )

                // Central Targeting Crosshairs
                TacticalTargetingReticle(
                    isLocked = armorStatus.isTargetLocked,
                    distanceMeters = armorStatus.targetDistanceMeters,
                    primaryColor = if (armorStatus.isTargetLocked) JarvisRed else themeAccent,
                    rotationDegrees = reticleRotation,
                    onClick = { viewModel.toggleTargetLock() },
                    modifier = Modifier.size(190.dp)
                )

                // Corner HUD Brackets
                HudCornerAccents(
                    color = themeAccent,
                    modifier = Modifier.fillMaxSize()
                )

                // Live Coordinates & Weapon Readiness Readout
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "LAT: 34.0259° N // ELEV: 142M",
                        color = JarvisCyanLight,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "SYS: ${currentArmor.weapons}",
                        color = JarvisTextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Thrusters & Repulsor Output telemetry
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "REPULSORS: ${armorStatus.powerAllocationRepulsors}%",
                        color = JarvisAmber,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "THRUSTERS: ${armorStatus.powerAllocationThrusters}%",
                        color = JarvisCyanLight,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. COMBAT ACTIONS & PROTOCOLS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Big Repulsor Blast Action
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (armorStatus.isRepulsorFiring) JarvisRed else Color(0xD90A1A32),
                    border = BorderStroke(
                        1.5.dp,
                        if (armorStatus.isRepulsorFiring) Color.White else themeAccent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = themeAccent),
                            role = Role.Button,
                            onClick = { viewModel.fireRepulsor() }
                        )
                        .testTag("btn_fire_repulsor")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Fire Repulsor",
                            tint = if (armorStatus.isRepulsorFiring) Color.White else themeAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (armorStatus.isRepulsorFiring) "DISCHARGING REPULSOR BEAM!" else "DISCHARGE REPULSOR BLAST",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tactical Protocol Strip (Veronica, House Party, Sentry Mode, Overclock)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Veronica (Hulkbuster)
                    VisorProtocolTile(
                        title = "VERONICA",
                        subtitle = "ORBITAL CAGE",
                        isActive = armorStatus.isVeronicaDeployed,
                        activeColor = JarvisAmber,
                        onClick = { viewModel.deployVeronicaProtocol() },
                        modifier = Modifier.weight(1f)
                    )

                    // House Party Protocol
                    VisorProtocolTile(
                        title = "HOUSE PARTY",
                        subtitle = "${if (armorStatus.isHousePartyActive) "12 SUITS" else "STANDBY"}",
                        isActive = armorStatus.isHousePartyActive,
                        activeColor = JarvisCyan,
                        onClick = { viewModel.deployHousePartyProtocol() },
                        modifier = Modifier.weight(1f)
                    )

                    // Perimeter Sentry Mode
                    VisorProtocolTile(
                        title = "SENTRY MODE",
                        subtitle = if (visorMotion.isSentryArmed) "ARMED" else "OFFLINE",
                        isActive = visorMotion.isSentryArmed,
                        activeColor = JarvisGreen,
                        onClick = { viewModel.toggleSentryMode() },
                        modifier = Modifier.weight(1f)
                    )

                    // Arc Reactor Overclock
                    VisorProtocolTile(
                        title = "OVERCLOCK",
                        subtitle = if (armorStatus.isArcReactorOverclocked) "12.8 GW" else "3.2 GW",
                        isActive = armorStatus.isArcReactorOverclocked,
                        activeColor = JarvisRed,
                        onClick = { viewModel.toggleArcReactorOverclock() },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 4. MARK ARMOR SUIT SELECTOR
                Text(
                    text = "SELECT STARK ARMOR SUIT:",
                    color = JarvisTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    StarkArmorModel.entries.forEach { suit ->
                        val isSelected = suit == currentArmor
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0x3300F0FF) else JarvisCardBg,
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) suit.accentColor else JarvisCardBorder
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.selectArmor(suit) }
                                .testTag("suit_selector_${suit.name}")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = suit.codeName,
                                    color = if (isSelected) Color.White else JarvisTextPrimary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = suit.designation,
                                    color = if (isSelected) suit.accentColor else JarvisTextMuted,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3D Artificial Flight Horizon canvas responding in real time to phone tilt (Pitch & Roll).
 */
@Composable
fun ArtificialFlightHorizon(
    pitch: Float,
    roll: Float,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        // Clamp visual pitch offset
        val pitchOffset = (pitch * 2.2f).coerceIn(-cy * 0.7f, cy * 0.7f)

        // Rotate canvas around center by -roll angle
        rotate(-roll, pivot = Offset(cx, cy)) {
            // Main Horizon Line
            drawLine(
                color = primaryColor.copy(alpha = 0.75f),
                start = Offset(cx - 160f, cy + pitchOffset),
                end = Offset(cx + 160f, cy + pitchOffset),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )

            // Stepped Pitch Ladder rungs (+30, +15, -15, -30)
            val stepDist = 38f
            listOf(-2, -1, 1, 2).forEach { level ->
                val rungY = cy + pitchOffset - (level * stepDist)
                val rungWidth = if (level % 2 == 0) 70f else 45f
                val rungAlpha = if (level % 2 == 0) 0.6f else 0.4f

                drawLine(
                    color = primaryColor.copy(alpha = rungAlpha),
                    start = Offset(cx - rungWidth, rungY),
                    end = Offset(cx + rungWidth, rungY),
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f)
                )
            }
        }

        // Fixed Center Aircraft Pip / Bore Sight (remains stationary relative to screen)
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = 3f,
            center = Offset(cx, cy)
        )
        drawLine(
            color = Color.White.copy(alpha = 0.85f),
            start = Offset(cx - 22f, cy),
            end = Offset(cx - 8f, cy),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.85f),
            start = Offset(cx + 8f, cy),
            end = Offset(cx + 22f, cy),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.85f),
            start = Offset(cx, cy - 22f),
            end = Offset(cx, cy - 8f),
            strokeWidth = 2f
        )
    }
}

/**
 * Interactive Central Target Acquisition Crosshairs.
 */
@Composable
fun TacticalTargetingReticle(
    isLocked: Boolean,
    distanceMeters: Int,
    primaryColor: Color,
    rotationDegrees: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("targeting_reticle")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = cx * 0.78f

            // Rotating Segmented Outer Ring
            rotate(rotationDegrees, pivot = Offset(cx, cy)) {
                drawCircle(
                    color = primaryColor.copy(alpha = 0.7f),
                    radius = radius,
                    style = Stroke(
                        width = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(35f, 25f), 0f)
                    )
                )
            }

            // Inner Ring
            drawCircle(
                color = primaryColor.copy(alpha = if (isLocked) 0.9f else 0.4f),
                radius = radius * 0.55f,
                style = Stroke(width = 1.2f)
            )

            // Crosshair tick marks
            val tickLen = 14f
            drawLine(primaryColor, Offset(cx - radius - tickLen, cy), Offset(cx - radius + tickLen, cy), 1.5f)
            drawLine(primaryColor, Offset(cx + radius - tickLen, cy), Offset(cx + radius + tickLen, cy), 1.5f)
            drawLine(primaryColor, Offset(cx, cy - radius - tickLen), Offset(cx, cy - radius + tickLen), 1.5f)
            drawLine(primaryColor, Offset(cx, cy + radius - tickLen), Offset(cx, cy + radius + tickLen), 1.5f)
        }

        // Status Label Inside Reticle
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isLocked) "TARGET LOCKED" else "SCANNING...",
                color = primaryColor,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            Text(
                text = "RANGE: ${distanceMeters}M",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

/**
 * Tactical Cyber Brackets for the 4 corners of the HUD visor.
 */
@Composable
fun HudCornerAccents(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val len = 24f
        val stroke = 2f

        // Top-Left
        drawLine(color, Offset(8f, 8f), Offset(8f + len, 8f), stroke)
        drawLine(color, Offset(8f, 8f), Offset(8f, 8f + len), stroke)

        // Top-Right
        drawLine(color, Offset(w - 8f, 8f), Offset(w - 8f - len, 8f), stroke)
        drawLine(color, Offset(w - 8f, 8f), Offset(w - 8f, 8f + len), stroke)

        // Bottom-Left
        drawLine(color, Offset(8f, h - 8f), Offset(8f + len, h - 8f), stroke)
        drawLine(color, Offset(8f, h - 8f), Offset(8f, h - 8f - len), stroke)

        // Bottom-Right
        drawLine(color, Offset(w - 8f, h - 8f), Offset(w - 8f - len, h - 8f), stroke)
        drawLine(color, Offset(w - 8f, h - 8f), Offset(w - 8f, h - 8f - len), stroke)
    }
}

/**
 * Protocol button tile on the Visor interface.
 */
@Composable
fun VisorProtocolTile(
    title: String,
    subtitle: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isActive) activeColor.copy(alpha = 0.25f) else JarvisCardBg,
        border = BorderStroke(
            1.dp,
            if (isActive) activeColor else JarvisCardBorder
        ),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            Text(
                text = title,
                color = if (isActive) activeColor else JarvisTextPrimary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = if (isActive) Color.White else JarvisTextMuted,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }
    }
}
