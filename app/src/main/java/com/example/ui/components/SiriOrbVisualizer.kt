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
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.repository.AssistantColorTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SiriOrbVisualizer(
    state: JarvisState,
    colorTheme: AssistantColorTheme = AssistantColorTheme.SIRI_IRIDESCENT,
    audioLevel: Float = 0f,
    size: Dp = 200.dp,
    glowIntensity: Float = 1.0f,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SiriOrbTransitions")

    // Slow organic fluid rotation
    val rotationFast by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    JarvisState.LISTENING -> 2200
                    JarvisState.PROCESSING -> 1400
                    JarvisState.SPEAKING -> 2800
                    else -> 6500
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbRotationFast"
    )

    val rotationSlow by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    JarvisState.LISTENING -> 3500
                    JarvisState.PROCESSING -> 2000
                    JarvisState.SPEAKING -> 4200
                    else -> 9500
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbRotationSlow"
    )

    // Breathing pulse
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    JarvisState.LISTENING -> 500
                    JarvisState.PROCESSING -> 350
                    JarvisState.SPEAKING -> 700
                    else -> 1800
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbPulse"
    )

    val morphFactor by infiniteTransition.animateFloat(
        initialValue = -0.15f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbMorph"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .testTag("siri_orb_visualizer")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 2f * 0.78f

            val dynamicAudio = if (state == JarvisState.LISTENING || state == JarvisState.SPEAKING) {
                (audioLevel * 0.35f).coerceIn(0f, 0.4f)
            } else 0f

            val currentScale = (breathingPulse + dynamicAudio) * glowIntensity.coerceIn(0.7f, 1.4f)
            val currentRadius = baseRadius * currentScale

            val colors = colorTheme.gradientColors
            val primary = colorTheme.primaryColor
            val secondary = colorTheme.secondaryColor
            val accent = colorTheme.accentColor

            // 1. Multi-Layer Outer Ambient Aura Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primary.copy(alpha = 0.45f * glowIntensity),
                        secondary.copy(alpha = 0.25f * glowIntensity),
                        Color.Transparent
                    ),
                    center = center,
                    radius = currentRadius * 1.55f
                ),
                radius = currentRadius * 1.55f,
                center = center
            )

            // 2. Secondary Diffuse Halo Ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.30f * glowIntensity),
                        Color.Transparent
                    ),
                    center = center,
                    radius = currentRadius * 1.3f
                ),
                radius = currentRadius * 1.3f,
                center = center
            )

            // 3. Rotating Fluid Iridescent Blobs (Plasma layer 1)
            rotate(degrees = rotationFast, pivot = center) {
                for (i in colors.indices) {
                    val angle = (i * 360f / colors.size) * (Math.PI / 180f)
                    val offsetDistance = currentRadius * (0.35f + morphFactor * 0.5f)
                    val nodeCenter = Offset(
                        center.x + (cos(angle) * offsetDistance).toFloat(),
                        center.y + (sin(angle) * offsetDistance).toFloat()
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                colors[i].copy(alpha = 0.85f),
                                colors[i].copy(alpha = 0.4f),
                                Color.Transparent
                            ),
                            center = nodeCenter,
                            radius = currentRadius * 0.85f
                        ),
                        radius = currentRadius * 0.85f,
                        center = nodeCenter,
                        blendMode = BlendMode.Screen
                    )
                }
            }

            // 4. Counter-Rotating Inner Plasma Nodes (Plasma layer 2)
            rotate(degrees = rotationSlow, pivot = center) {
                val reversedColors = colors.reversed()
                for (i in reversedColors.indices) {
                    val angle = ((i * 360f / reversedColors.size) + 45f) * (Math.PI / 180f)
                    val offsetDist = currentRadius * (0.28f - morphFactor * 0.4f)
                    val nodeCenter = Offset(
                        center.x + (cos(angle) * offsetDist).toFloat(),
                        center.y + (sin(angle) * offsetDist).toFloat()
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                reversedColors[i].copy(alpha = 0.90f),
                                reversedColors[i].copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            center = nodeCenter,
                            radius = currentRadius * 0.75f
                        ),
                        radius = currentRadius * 0.75f,
                        center = nodeCenter,
                        blendMode = BlendMode.Plus
                    )
                }
            }

            // 5. Core Radiant Energy Sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        accent.copy(alpha = 0.75f),
                        primary.copy(alpha = 0.5f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = currentRadius * 0.55f
                ),
                radius = currentRadius * 0.55f,
                center = center
            )

            // 6. Liquid Glass Surface Rim Highlight
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.8f),
                        accent.copy(alpha = 0.6f),
                        secondary.copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0.9f)
                    ),
                    center = center
                ),
                radius = currentRadius * 0.92f,
                center = center,
                style = Stroke(width = (2.2f + dynamicAudio * 4f).dp.toPx())
            )

            // 7. Active Voice Ripple Wave on Speech
            if (state == JarvisState.LISTENING || state == JarvisState.SPEAKING) {
                drawCircle(
                    color = accent.copy(alpha = (0.5f + dynamicAudio).coerceIn(0.2f, 0.85f)),
                    radius = currentRadius * (1.15f + dynamicAudio * 0.6f),
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }
    }
}
