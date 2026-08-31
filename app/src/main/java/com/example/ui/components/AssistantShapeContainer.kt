package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AssistantAppearanceConfig
import com.example.data.repository.AssistantColorTheme
import com.example.data.repository.AssistantShape
import com.example.ui.theme.JarvisObsidian
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary

@Composable
fun AssistantShapeContainer(
    state: JarvisState,
    config: AssistantAppearanceConfig,
    audioLevel: Float = 0f,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scaleFactor = config.orbScale

    AnimatedContent(
        targetState = config.shape,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
        label = "AssistantShapeTransition",
        modifier = modifier
    ) { shape ->
        when (shape) {
            AssistantShape.SIRI_ORB -> {
                SiriOrbVisualizer(
                    state = state,
                    colorTheme = config.colorTheme,
                    audioLevel = audioLevel,
                    size = (210 * scaleFactor).dp,
                    glowIntensity = config.glowIntensity,
                    onClick = onClick
                )
            }
            AssistantShape.CURVED_PILL -> {
                CurvedPillVisualizer(
                    state = state,
                    colorTheme = config.colorTheme,
                    audioLevel = audioLevel,
                    width = (270 * scaleFactor).dp,
                    height = (72 * scaleFactor).dp,
                    glowIntensity = config.glowIntensity,
                    onClick = onClick
                )
            }
            AssistantShape.ARC_REACTOR -> {
                ArcReactorVisualizer(
                    state = state,
                    audioLevel = audioLevel,
                    size = (210 * scaleFactor).dp,
                    onClick = onClick
                )
            }
            AssistantShape.WAVEFORM_RIBBON -> {
                WaveformRibbonVisualizer(
                    state = state,
                    colorTheme = config.colorTheme,
                    audioLevel = audioLevel,
                    glowIntensity = config.glowIntensity,
                    onClick = onClick
                )
            }
            AssistantShape.MINIMAL_BUBBLE -> {
                MinimalBubbleVisualizer(
                    state = state,
                    colorTheme = config.colorTheme,
                    audioLevel = audioLevel,
                    size = (110 * scaleFactor).dp,
                    glowIntensity = config.glowIntensity,
                    onClick = onClick
                )
            }
        }
    }
}

@Composable
fun WaveformRibbonVisualizer(
    state: JarvisState,
    colorTheme: AssistantColorTheme,
    audioLevel: Float,
    glowIntensity: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RibbonAnim")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.96f, targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = JarvisObsidian.copy(alpha = 0.92f)),
        border = BorderStroke(
            (1.5f * glowIntensity).dp,
            Brush.horizontalGradient(colorTheme.gradientColors)
        ),
        modifier = modifier
            .scale(pulse)
            .padding(8.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("waveform_ribbon")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            VoiceWaveformVisualizer(
                isListening = state == JarvisState.LISTENING || state == JarvisState.SPEAKING,
                audioLevel = audioLevel
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (state == JarvisState.LISTENING) "LISTENING • SPEAK FREELY" else "TAP OR SPEAK HANDS-FREE",
                color = colorTheme.accentColor,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MinimalBubbleVisualizer(
    state: JarvisState,
    colorTheme: AssistantColorTheme,
    audioLevel: Float,
    size: Dp = 100.dp,
    glowIntensity: Float = 1.0f,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "MinimalBubble")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val dynamicAudio = if (state == JarvisState.LISTENING || state == JarvisState.SPEAKING) {
        (audioLevel * 0.4f).coerceIn(0f, 0.4f)
    } else 0f

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .scale(scale + dynamicAudio)
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("minimal_bubble_visualizer")
    ) {
        // Ambient Outer Glow
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            colorTheme.primaryColor.copy(alpha = 0.5f * glowIntensity),
                            Color.Transparent
                        )
                    )
                )
        )

        // Core Circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size * 0.72f)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(colorTheme.gradientColors)
                )
                .padding(2.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(size * 0.65f)
                    .clip(CircleShape)
                    .background(JarvisObsidian.copy(alpha = 0.85f))
            ) {
                Icon(
                    imageVector = if (state == JarvisState.SPEAKING) Icons.Default.VolumeUp else Icons.Default.Mic,
                    contentDescription = "Voice Assistant",
                    tint = colorTheme.accentColor,
                    modifier = Modifier.size(size * 0.35f)
                )
            }
        }
    }
}
