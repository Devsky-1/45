package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisRed
import kotlin.math.cos
import kotlin.math.sin

enum class JarvisState {
    STANDBY,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ALERT
}

@Composable
fun ArcReactorVisualizer(
    state: JarvisState,
    audioLevel: Float = 0f,
    size: Dp = 220.dp,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorTransitions")

    // Continuous smooth rotations
    val rotationClockwise by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state == JarvisState.PROCESSING) 3000 else 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ClockwiseRotation"
    )

    val rotationCounterClockwise by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state == JarvisState.PROCESSING) 4000 else 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CounterClockwiseRotation"
    )

    // Pulse animation
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    JarvisState.LISTENING -> 600
                    JarvisState.PROCESSING -> 400
                    JarvisState.SPEAKING -> 800
                    JarvisState.ALERT -> 300
                    else -> 2000
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseGlow"
    )

    val baseColor = when (state) {
        JarvisState.LISTENING -> JarvisAmber
        JarvisState.PROCESSING -> JarvisPurpleCore
        JarvisState.SPEAKING -> JarvisCyan
        JarvisState.ALERT -> JarvisRed
        JarvisState.STANDBY -> JarvisCyan
    }

    val stateText = when (state) {
        JarvisState.LISTENING -> "VOICE INPUT ACTIVE"
        JarvisState.PROCESSING -> "COMPUTING MATRIX..."
        JarvisState.SPEAKING -> "TRANSMITTING VOCAL"
        JarvisState.ALERT -> "HIGH ALERT PROTOCOL"
        JarvisState.STANDBY -> "ONLINE • STANDBY"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .testTag("arc_reactor_core")
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val baseRadius = this.size.minDimension / 2f * 0.9f
                val dynamicLevel = if (state == JarvisState.LISTENING || state == JarvisState.SPEAKING) {
                    (audioLevel * 0.4f).coerceIn(0f, 0.4f)
                } else 0f

                val pulseFactor = (pulseGlow + dynamicLevel).coerceIn(0.7f, 1.6f)

                // 1. Ambient Background Halo Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            baseColor.copy(alpha = 0.35f * pulseFactor),
                            baseColor.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = baseRadius * 1.15f * pulseFactor
                    ),
                    radius = baseRadius * 1.15f * pulseFactor,
                    center = center
                )

                // 2. Outer Segmented Bracket Ring (Rotates Clockwise)
                rotate(rotationClockwise, pivot = center) {
                    drawCircle(
                        color = baseColor.copy(alpha = 0.5f),
                        radius = baseRadius * 0.98f,
                        center = center,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(35f, 18f, 10f, 18f), 0f)
                        )
                    )

                    // 4 corner ticks
                    for (i in 0 until 4) {
                        val angleRad = Math.toRadians((i * 90.0))
                        val start = Offset(
                            center.x + (baseRadius * 0.90f) * cos(angleRad).toFloat(),
                            center.y + (baseRadius * 0.90f) * sin(angleRad).toFloat()
                        )
                        val end = Offset(
                            center.x + (baseRadius * 1.02f) * cos(angleRad).toFloat(),
                            center.y + (baseRadius * 1.02f) * sin(angleRad).toFloat()
                        )
                        drawLine(
                            color = baseColor,
                            start = start,
                            end = end,
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // 3. Middle High-Energy Segments (Rotates Counter-Clockwise)
                rotate(rotationCounterClockwise, pivot = center) {
                    drawCircle(
                        color = JarvisBlue.copy(alpha = 0.7f),
                        radius = baseRadius * 0.78f,
                        center = center,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(50f, 25f), 0f)
                        )
                    )

                    // Energy node notches (8 points)
                    for (i in 0 until 8) {
                        val angleRad = Math.toRadians((i * 45.0))
                        val nodePos = Offset(
                            center.x + (baseRadius * 0.78f) * cos(angleRad).toFloat(),
                            center.y + (baseRadius * 0.78f) * sin(angleRad).toFloat()
                        )
                        drawCircle(
                            color = JarvisCyanLight,
                            radius = 3.dp.toPx(),
                            center = nodePos
                        )
                    }
                }

                // 4. Concentric Glowing Target Ring
                drawCircle(
                    color = baseColor.copy(alpha = 0.85f),
                    radius = baseRadius * 0.58f * (0.95f + pulseFactor * 0.05f),
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                // 5. Inner Hexagonal/Triangular Fusion Core (Rotates with Audio)
                rotate(rotationClockwise * 1.5f, pivot = center) {
                    val coreRadius = baseRadius * 0.42f
                    val vertices = 6
                    for (i in 0 until vertices) {
                        val a1 = Math.toRadians((i * (360.0 / vertices)))
                        val a2 = Math.toRadians(((i + 1) * (360.0 / vertices)))
                        val p1 = Offset(center.x + coreRadius * cos(a1).toFloat(), center.y + coreRadius * sin(a1).toFloat())
                        val p2 = Offset(center.x + coreRadius * cos(a2).toFloat(), center.y + coreRadius * sin(a2).toFloat())
                        drawLine(
                            color = baseColor,
                            start = p1,
                            end = p2,
                            strokeWidth = 2.5.dp.toPx()
                        )
                    }
                }

                // 6. Central Fusion Plasma (Intense White & Cyan core)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            JarvisCyanLight,
                            baseColor,
                            Color.Transparent
                        ),
                        center = center,
                        radius = baseRadius * 0.28f * pulseFactor
                    ),
                    radius = baseRadius * 0.28f * pulseFactor,
                    center = center
                )

                drawCircle(
                    color = Color.White,
                    radius = baseRadius * 0.12f,
                    center = center
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Status Label with HUD styling
        Text(
            text = stateText,
            color = baseColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp
        )
    }
}

private val JarvisPurpleCore = Color(0xFF00E5FF)
