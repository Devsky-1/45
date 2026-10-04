package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.repository.AssistantAppearanceConfig
import com.example.data.repository.AssistantColorTheme
import com.example.data.repository.PillAnimationStyle
import com.example.data.repository.PillGlowLevel
import kotlin.math.cos
import kotlin.math.sin

/**
 * Minimal, Voice-First Siri-Style Pill Assistant.
 * Pure audio-reactive fluid visualizer with zero text clutter.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SiriPillVisualizer(
    state: JarvisState,
    config: AssistantAppearanceConfig,
    audioLevel: Float = 0f,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onDragDelta: ((Float, Float) -> Unit)? = null,
    onDragEnd: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val theme = config.colorTheme
    val glowLevel = config.pillGlowLevel
    val animStyle = config.pillAnimationStyle
    val scaleFactor = config.effectiveScale
    val opacity = config.pillOpacity

    val baseWidth = config.pillSizeOption.defaultWidthDp.dp * scaleFactor
    val baseHeight = config.pillSizeOption.defaultHeightDp.dp * scaleFactor

    val isAnimated = animStyle != PillAnimationStyle.OFF
    val speedMult = if (isAnimated) animStyle.speedMultiplier else 0f

    val infiniteTransition = rememberInfiniteTransition(label = "PillInfTransition")

    // Smooth breathing scale
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (1800 / (if (speedMult > 0f) speedMult else 1f)).toInt(),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PillBreath"
    )

    // Flowing orbital phase for thinking & speaking
    val orbitalPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (2400 / (if (speedMult > 0f) speedMult else 1f)).toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbitalPhase"
    )

    // Thinking traveling light trace
    val travelingSweep by infiniteTransition.animateFloat(
        initialValue = -0.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (1200 / (if (speedMult > 0f) speedMult else 1f)).toInt(),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "ThinkingSweep"
    )

    // Smooth state transitions for scale & width expansion
    val targetExpansionScale = when (state) {
        JarvisState.LISTENING -> 1.08f + (audioLevel * 0.12f).coerceIn(0f, 0.15f)
        JarvisState.SPEAKING -> 1.05f + (audioLevel * 0.08f).coerceIn(0f, 0.1f)
        JarvisState.PROCESSING -> 1.02f
        JarvisState.ALERT -> 1.04f
        JarvisState.STANDBY -> if (isAnimated) breathScale else 1.0f
    }
    val smoothScale by animateFloatAsState(
        targetValue = targetExpansionScale,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "SmoothScale"
    )

    // Dynamic colors
    val primaryColor = when (state) {
        JarvisState.ALERT -> Color(0xFFFF4757)
        JarvisState.PROCESSING -> theme.accentColor
        JarvisState.SPEAKING -> theme.primaryColor
        JarvisState.LISTENING -> theme.accentColor
        JarvisState.STANDBY -> theme.primaryColor
    }

    val accentColor = when (state) {
        JarvisState.ALERT -> Color(0xFFFF6B81)
        JarvisState.PROCESSING -> theme.primaryColor
        JarvisState.SPEAKING -> theme.accentColor
        JarvisState.LISTENING -> theme.secondaryColor
        JarvisState.STANDBY -> theme.accentColor
    }

    val glowAlpha = (glowLevel.multiplier * 0.65f).coerceIn(0f, 0.9f)
    val glowBlurRadius = (16.dp * glowLevel.multiplier).coerceAtLeast(0.dp)

    val cornerRadius = baseHeight / 2

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .scale(smoothScale)
            .size(width = baseWidth, height = baseHeight)
            .alpha(opacity)
            .then(
                if (onDragDelta != null) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = { onDragEnd?.invoke() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDragDelta(dragAmount.x, dragAmount.y)
                            }
                        )
                    }
                } else Modifier
            )
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("siri_pill_main_container")
    ) {
        // Outer diffuse ambient glow layer
        if (glowLevel != PillGlowLevel.OFF) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .scale(1.06f)
                    .blur(glowBlurRadius)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = glowAlpha * 0.7f),
                                accentColor.copy(alpha = glowAlpha),
                                theme.secondaryColor.copy(alpha = glowAlpha * 0.7f)
                            )
                        ),
                        shape = RoundedCornerShape(cornerRadius)
                    )
            )
        }

        // Main Glass/Dark Pill Container
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(cornerRadius))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xEE0A0F1D),
                            Color(0xF5050811)
                        )
                    )
                )
                .border(
                    width = (1.5f * (if (glowLevel == PillGlowLevel.HIGH) 1.3f else 1.0f)).dp,
                    brush = Brush.horizontalGradient(
                        colors = when (state) {
                            JarvisState.LISTENING -> listOf(accentColor, primaryColor, theme.secondaryColor, accentColor)
                            JarvisState.PROCESSING -> listOf(
                                primaryColor.copy(alpha = 0.4f),
                                accentColor,
                                primaryColor.copy(alpha = 0.4f)
                            )
                            JarvisState.SPEAKING -> listOf(primaryColor, accentColor, theme.secondaryColor, primaryColor)
                            JarvisState.ALERT -> listOf(Color(0xFFFF4757), Color(0xFFFF7675), Color(0xFFFF4757))
                            JarvisState.STANDBY -> listOf(
                                primaryColor.copy(alpha = 0.6f),
                                accentColor.copy(alpha = 0.8f),
                                theme.secondaryColor.copy(alpha = 0.6f)
                            )
                        }
                    ),
                    shape = RoundedCornerShape(cornerRadius)
                )
        ) {
            // High-precision canvas audio waveform & fluid harmonic visualizer inside the pill
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val midY = h / 2f

                when (state) {
                    JarvisState.STANDBY -> {
                        // IDLE: Subtle organic glowing central core & soft breathing filaments
                        drawIdleOrganicCore(
                            width = w,
                            height = h,
                            primaryColor = primaryColor,
                            accentColor = accentColor,
                            secondaryColor = theme.secondaryColor,
                            phase = orbitalPhase
                        )
                    }

                    JarvisState.LISTENING -> {
                        // LISTENING: Responsive audio soundwaves reacting to microphone
                        drawListeningEqualizer(
                            width = w,
                            height = h,
                            audioLevel = audioLevel,
                            primaryColor = primaryColor,
                            accentColor = accentColor,
                            secondaryColor = theme.secondaryColor,
                            phase = orbitalPhase
                        )
                    }

                    JarvisState.PROCESSING -> {
                        // THINKING: Smooth orbiting fluid particle trace along pill contour
                        drawProcessingOrbitalWave(
                            width = w,
                            height = h,
                            sweep = travelingSweep,
                            primaryColor = primaryColor,
                            accentColor = accentColor
                        )
                    }

                    JarvisState.SPEAKING -> {
                        // SPEAKING: Harmonic audio frequency ribbons synced to voice
                        drawSpeakingHarmonicWaves(
                            width = w,
                            height = h,
                            audioLevel = audioLevel,
                            primaryColor = primaryColor,
                            accentColor = accentColor,
                            secondaryColor = theme.secondaryColor,
                            phase = orbitalPhase
                        )
                    }

                    JarvisState.ALERT -> {
                        // ALERT / ERROR: Amber/Ruby pulse wave
                        drawAlertPulse(
                            width = w,
                            height = h,
                            phase = orbitalPhase
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Canvas Drawing Helper Functions
// -------------------------------------------------------------

private fun DrawScope.drawIdleOrganicCore(
    width: Float,
    height: Float,
    primaryColor: Color,
    accentColor: Color,
    secondaryColor: Color,
    phase: Float
) {
    val midY = height / 2f
    val centerX = width / 2f
    val barCount = 12
    val spacing = width * 0.5f / barCount
    val startX = centerX - (barCount * spacing) / 2f

    // Soft central glow pill
    drawRoundRect(
        brush = Brush.radialGradient(
            colors = listOf(accentColor.copy(alpha = 0.5f), Color.Transparent),
            center = Offset(centerX, midY),
            radius = width * 0.35f
        ),
        topLeft = Offset(centerX - width * 0.25f, height * 0.2f),
        size = Size(width * 0.5f, height * 0.6f),
        cornerRadius = CornerRadius(height / 2f, height / 2f)
    )

    // Gentle undulating harmonic bars
    for (i in 0 until barCount) {
        val x = startX + i * spacing + spacing / 2f
        val rad = Math.toRadians((phase + i * 28.0).toDouble())
        val normHeight = (0.25f + 0.15f * sin(rad).toFloat()).coerceIn(0.15f, 0.45f) * height
        val barTop = midY - normHeight / 2f

        val barColor = if (i % 2 == 0) primaryColor.copy(alpha = 0.75f) else accentColor.copy(alpha = 0.85f)

        drawLine(
            color = barColor,
            start = Offset(x, barTop),
            end = Offset(x, barTop + normHeight),
            strokeWidth = 3.5f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawListeningEqualizer(
    width: Float,
    height: Float,
    audioLevel: Float,
    primaryColor: Color,
    accentColor: Color,
    secondaryColor: Color,
    phase: Float
) {
    val midY = height / 2f
    val barCount = 20
    val usableWidth = width * 0.75f
    val spacing = usableWidth / barCount
    val startX = (width - usableWidth) / 2f

    val boost = (audioLevel * 1.8f).coerceIn(0.15f, 1.0f)

    // Fluid wave curves
    val wavePath = Path()
    wavePath.moveTo(startX, midY)

    for (i in 0 until barCount) {
        val x = startX + i * spacing + spacing / 2f
        val waveFactor = sin(Math.toRadians((phase * 2.0 + i * 32.0).toDouble())).toFloat()
        val bellCurve = 1.0f - kotlin.math.abs(i - barCount / 2f) / (barCount / 2f)

        val barHeight = ((height * 0.65f) * (0.2f + waveFactor * 0.15f + boost * 0.75f * bellCurve))
            .coerceIn(6f, height * 0.82f)

        val top = midY - barHeight / 2f
        val bottom = midY + barHeight / 2f

        val barColor = when {
            i % 3 == 0 -> accentColor
            i % 3 == 1 -> primaryColor
            else -> secondaryColor
        }

        drawLine(
            color = barColor.copy(alpha = (0.75f + boost * 0.25f).coerceIn(0.7f, 1.0f)),
            start = Offset(x, top),
            end = Offset(x, bottom),
            strokeWidth = 4.0f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawProcessingOrbitalWave(
    width: Float,
    height: Float,
    sweep: Float,
    primaryColor: Color,
    accentColor: Color
) {
    val midY = height / 2f
    val sweepX = width * sweep
    val particleRadius = height * 0.24f

    // Sweeping beam of light along the pill center
    drawRoundRect(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                primaryColor.copy(alpha = 0.5f),
                accentColor,
                primaryColor.copy(alpha = 0.5f),
                Color.Transparent
            ),
            startX = sweepX - width * 0.25f,
            endX = sweepX + width * 0.25f
        ),
        topLeft = Offset(0f, height * 0.15f),
        size = Size(width, height * 0.7f),
        cornerRadius = CornerRadius(height / 2f, height / 2f)
    )

    // Orbital particles chasing each other
    for (idx in 0..3) {
        val offsetFraction = (sweep - idx * 0.08f + 1f) % 1f
        val px = width * offsetFraction
        val py = midY + sin(offsetFraction * Math.PI * 4).toFloat() * (height * 0.15f)
        val alpha = (1f - idx * 0.25f).coerceIn(0.2f, 1f)

        drawCircle(
            color = accentColor.copy(alpha = alpha),
            radius = particleRadius * (1f - idx * 0.2f),
            center = Offset(px, py)
        )
    }
}

private fun DrawScope.drawSpeakingHarmonicWaves(
    width: Float,
    height: Float,
    audioLevel: Float,
    primaryColor: Color,
    accentColor: Color,
    secondaryColor: Color,
    phase: Float
) {
    val midY = height / 2f
    val barCount = 18
    val usableWidth = width * 0.78f
    val spacing = usableWidth / barCount
    val startX = (width - usableWidth) / 2f

    val voiceAmp = (audioLevel * 1.5f + 0.45f).coerceIn(0.4f, 1.0f)

    for (i in 0 until barCount) {
        val x = startX + i * spacing + spacing / 2f
        val rad1 = Math.toRadians((phase * 1.8 + i * 40.0).toDouble())
        val rad2 = Math.toRadians((phase * 2.5 - i * 30.0).toDouble())
        val harmonic = (sin(rad1) * 0.6 + cos(rad2) * 0.4).toFloat()

        val bell = 1.0f - kotlin.math.abs(i - barCount / 2f) / (barCount / 2f)
        val barHeight = (height * 0.75f * (0.3f + 0.5f * kotlin.math.abs(harmonic) * voiceAmp * (0.5f + 0.5f * bell)))
            .coerceIn(8f, height * 0.85f)

        val top = midY - barHeight / 2f
        val bottom = midY + barHeight / 2f

        val color = when (i % 4) {
            0 -> primaryColor
            1 -> accentColor
            2 -> secondaryColor
            else -> Color.White
        }

        drawLine(
            color = color.copy(alpha = 0.9f),
            start = Offset(x, top),
            end = Offset(x, bottom),
            strokeWidth = 4.2f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawAlertPulse(
    width: Float,
    height: Float,
    phase: Float
) {
    val midY = height / 2f
    val centerX = width / 2f
    val alertRed = Color(0xFFFF4757)
    val alertAmber = Color(0xFFFF7675)

    val pulse = (0.5f + 0.5f * sin(Math.toRadians(phase * 4.0.toDouble())).toFloat())

    drawRoundRect(
        brush = Brush.radialGradient(
            colors = listOf(alertAmber.copy(alpha = 0.6f * pulse), Color.Transparent),
            center = Offset(centerX, midY),
            radius = width * 0.4f
        ),
        topLeft = Offset(0f, 0f),
        size = Size(width, height),
        cornerRadius = CornerRadius(height / 2f, height / 2f)
    )

    // Center exclamation warning bar
    drawLine(
        color = alertRed,
        start = Offset(centerX, midY - height * 0.25f),
        end = Offset(centerX, midY + height * 0.25f),
        strokeWidth = 5.0f,
        cap = StrokeCap.Round
    )
}
