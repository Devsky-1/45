package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
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
import com.example.data.repository.AssistantColorTheme
import com.example.ui.theme.JarvisObsidian
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary

@Composable
fun CurvedPillVisualizer(
    state: JarvisState,
    colorTheme: AssistantColorTheme = AssistantColorTheme.SIRI_IRIDESCENT,
    audioLevel: Float = 0f,
    width: Dp = 260.dp,
    height: Dp = 68.dp,
    glowIntensity: Float = 1.0f,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PillGlowTransition")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    JarvisState.LISTENING -> 450
                    JarvisState.PROCESSING -> 300
                    JarvisState.SPEAKING -> 600
                    else -> 1500
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PillPulse"
    )

    // Animated soundwave bars
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(320, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(260, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.15f, targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b3"
    )
    val bar4 by infiniteTransition.animateFloat(
        initialValue = 0.35f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(290, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b4"
    )

    val dynamicAudio = if (state == JarvisState.LISTENING || state == JarvisState.SPEAKING) {
        (audioLevel * 1.6f + 0.3f).coerceIn(0.2f, 1.3f)
    } else 0.15f

    val primary = colorTheme.primaryColor
    val secondary = colorTheme.secondaryColor
    val accent = colorTheme.accentColor

    val stateText = when (state) {
        JarvisState.LISTENING -> "Listening..."
        JarvisState.PROCESSING -> "Thinking..."
        JarvisState.SPEAKING -> "Speaking..."
        JarvisState.ALERT -> "Alert Protocol"
        JarvisState.STANDBY -> "Ready to Assist"
    }

    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = JarvisObsidian.copy(alpha = 0.94f)
        ),
        border = BorderStroke(
            width = (1.8f * glowIntensity).dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    primary,
                    accent,
                    secondary,
                    primary
                )
            )
        ),
        modifier = modifier
            .scale(pulseScale)
            .size(width = width, height = height)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("curved_pill_visualizer")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            // Left Status Orb / Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(accent, primary.copy(alpha = 0.6f))
                        )
                    )
            ) {
                Icon(
                    imageVector = when (state) {
                        JarvisState.SPEAKING -> Icons.Default.VolumeUp
                        JarvisState.LISTENING -> Icons.Default.Mic
                        else -> Icons.Default.GraphicEq
                    },
                    contentDescription = "Voice State",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Middle Label
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stateText,
                    color = JarvisTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = if (state == JarvisState.LISTENING) "Speak now" else "Tap or speak hands-free",
                    color = JarvisTextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }

            // Right Equalizer Wave Bars
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                val heights = listOf(
                    (bar1 * dynamicAudio * 26).dp.coerceIn(4.dp, 28.dp),
                    (bar2 * dynamicAudio * 28).dp.coerceIn(5.dp, 30.dp),
                    (bar3 * dynamicAudio * 24).dp.coerceIn(4.dp, 26.dp),
                    (bar4 * dynamicAudio * 26).dp.coerceIn(4.dp, 28.dp)
                )

                heights.forEachIndexed { index, barHeight ->
                    Box(
                        modifier = Modifier
                            .width(3.5.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(accent, primary)
                                )
                            )
                    )
                }
            }
        }
    }
}
