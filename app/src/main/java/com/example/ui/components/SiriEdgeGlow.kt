package com.example.ui.components

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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.data.repository.AssistantColorTheme

/**
 * Renders an Apple Intelligence Siri iOS 18 & Google Assistant style
 * dynamic luminous perimeter edge glow around the entire device screen.
 */
@Composable
fun SiriScreenEdgeGlow(
    state: JarvisState,
    colorTheme: AssistantColorTheme,
    isActive: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SiriEdgeAnim")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    JarvisState.LISTENING -> 2200
                    JarvisState.PROCESSING -> 1500
                    JarvisState.SPEAKING -> 2800
                    else -> 4500
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "edge_phase"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    JarvisState.LISTENING -> 600
                    JarvisState.PROCESSING -> 400
                    JarvisState.SPEAKING -> 750
                    else -> 1800
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "edge_pulse"
    )

    AnimatedVisibility(
        visible = isActive,
        enter = fadeIn(tween(400)),
        exit = fadeOut(tween(400))
    ) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val strokeThickness = (14.dp.toPx() * pulse).coerceAtLeast(10f)

            val baseColors = colorTheme.gradientColors
            val primary = colorTheme.primaryColor
            val accent = colorTheme.accentColor
            val secondary = colorTheme.secondaryColor

            // Calculate rotating angle for gradient offset
            val angle = phase * 2f * Math.PI.toFloat()
            val startOffset = Offset(
                x = w / 2f + (w / 2f) * kotlin.math.cos(angle),
                y = h / 2f + (h / 2f) * kotlin.math.sin(angle)
            )
            val endOffset = Offset(
                x = w / 2f - (w / 2f) * kotlin.math.cos(angle),
                y = h / 2f - (h / 2f) * kotlin.math.sin(angle)
            )

            val rainbowBrush = Brush.linearGradient(
                colors = listOf(
                    primary.copy(alpha = 0.85f * pulse),
                    accent.copy(alpha = 0.95f * pulse),
                    secondary.copy(alpha = 0.85f * pulse),
                    primary.copy(alpha = 0.85f * pulse)
                ),
                start = startOffset,
                end = endOffset
            )

            // Outer diffused ambient bloom
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.18f * pulse),
                        primary.copy(alpha = 0.08f * pulse),
                        Color.Transparent
                    ),
                    center = Offset(w / 2f, h),
                    radius = w * 0.9f
                ),
                topLeft = Offset.Zero,
                size = size
            )

            // Draw glowing border inset along screen frame
            drawRect(
                brush = rainbowBrush,
                topLeft = Offset(strokeThickness / 2f, strokeThickness / 2f),
                size = Size(w - strokeThickness, h - strokeThickness),
                style = Stroke(width = strokeThickness)
            )

            // Top screen highlight beam
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        primary.copy(alpha = 0.9f * pulse),
                        accent.copy(alpha = 0.9f * pulse),
                        Color.Transparent
                    )
                ),
                topLeft = Offset.Zero,
                size = Size(w, strokeThickness * 1.5f)
            )

            // Bottom screen luminous wave
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        accent.copy(alpha = 0.95f * pulse),
                        secondary.copy(alpha = 0.95f * pulse),
                        primary.copy(alpha = 0.95f * pulse),
                        Color.Transparent
                    )
                ),
                topLeft = Offset(0f, h - strokeThickness * 2f),
                size = Size(w, strokeThickness * 2f)
            )
        }
    }
}
