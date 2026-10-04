package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.PillPositionMode
import com.example.ui.JarvisViewModel
import com.example.ui.components.JarvisState
import com.example.ui.components.SiriPillVisualizer
import kotlin.math.roundToInt

/**
 * Pure Voice-First Main Assistant Screen.
 * Contains the customizable, draggable Siri-style Pill with quick HUD access.
 * Zero chat clutter, instant voice-in voice-out.
 */
@Composable
fun MainAssistantScreen(
    viewModel: JarvisViewModel,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenDiagnostics: () -> Unit = {},
    onOpenMemory: () -> Unit = {}
) {
    val jarvisState by viewModel.jarvisState.collectAsStateWithLifecycle()
    val audioLevel by viewModel.rmsAudioLevel.collectAsStateWithLifecycle()
    val config by viewModel.appearanceConfig.collectAsStateWithLifecycle()
    val currentQueryInput by viewModel.currentQueryInput.collectAsStateWithLifecycle()

    val density = LocalDensity.current
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF070B14),
                        Color(0xFF020408)
                    )
                )
            )
            .testTag("main_voice_assistant_canvas")
    ) {
        // Sleek Top HUD Bar with Status and Quick Module Launchers
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = topInset + 10.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.TopCenter)
        ) {
            // Left HUD status indicator & Quick Module Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                // JARVIS Badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x22FFFFFF))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (jarvisState) {
                                        JarvisState.STANDBY -> config.colorTheme.primaryColor
                                        JarvisState.LISTENING -> config.colorTheme.accentColor
                                        JarvisState.PROCESSING -> Color(0xFFF59E0B)
                                        JarvisState.SPEAKING -> Color(0xFF10B981)
                                        JarvisState.ALERT -> Color(0xFFEF4444)
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "J.A.R.V.I.S.",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Telemetry / Diagnostics Button
                IconButton(
                    onClick = onOpenDiagnostics,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x18FFFFFF))
                        .testTag("diagnostics_nav_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "System Diagnostics",
                        tint = config.colorTheme.primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Memory Matrix Button
                IconButton(
                    onClick = onOpenMemory,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x18FFFFFF))
                        .testTag("memory_nav_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "Memory Matrix",
                        tint = config.colorTheme.accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Right Settings Button
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0x18FFFFFF))
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Assistant Settings",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Draggable / Configurable Pill Area
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val containerWidthPx = with(density) { maxWidth.toPx() }
            val containerHeightPx = with(density) { maxHeight.toPx() }

            val pillScale = config.effectiveScale
            val pillWidthDp = config.pillSizeOption.defaultWidthDp.dp * pillScale
            val pillHeightDp = config.pillSizeOption.defaultHeightDp.dp * pillScale

            val pillWidthPx = with(density) { pillWidthDp.toPx() }
            val pillHeightPx = with(density) { pillHeightDp.toPx() }

            val minXPx = pillWidthPx / 2f + 16f
            val maxXPx = containerWidthPx - pillWidthPx / 2f - 16f
            val minYPx = pillHeightPx / 2f + 60f
            val maxYPx = containerHeightPx - pillHeightPx / 2f - 40f

            // Target coordinates based on position mode
            val defaultCenterX = when (config.pillPositionMode) {
                PillPositionMode.LEFT -> (pillWidthPx / 2f + 32f).coerceIn(minXPx, maxXPx)
                PillPositionMode.RIGHT -> (containerWidthPx - pillWidthPx / 2f - 32f).coerceIn(minXPx, maxXPx)
                PillPositionMode.BOTTOM,
                PillPositionMode.TOP,
                PillPositionMode.CENTER -> containerWidthPx / 2f
                PillPositionMode.CUSTOM -> (config.customOffsetXPercent * containerWidthPx).coerceIn(minXPx, maxXPx)
            }

            val defaultCenterY = when (config.pillPositionMode) {
                PillPositionMode.TOP -> (containerHeightPx * 0.14f).coerceIn(minYPx, maxYPx)
                PillPositionMode.BOTTOM -> (containerHeightPx * 0.86f).coerceIn(minYPx, maxYPx)
                PillPositionMode.CENTER,
                PillPositionMode.LEFT,
                PillPositionMode.RIGHT -> containerHeightPx / 2f
                PillPositionMode.CUSTOM -> (config.customOffsetYPercent * containerHeightPx).coerceIn(minYPx, maxYPx)
            }

            var liveCenterX by remember(config.pillPositionMode, config.customOffsetXPercent, containerWidthPx) {
                mutableFloatStateOf(defaultCenterX)
            }
            var liveCenterY by remember(config.pillPositionMode, config.customOffsetYPercent, containerHeightPx) {
                mutableFloatStateOf(defaultCenterY)
            }

            val pillTopLeftXPx = (liveCenterX - pillWidthPx / 2f).coerceIn(16f, containerWidthPx - pillWidthPx - 16f)
            val pillTopOffsetYPx = (liveCenterY - pillHeightPx / 2f).coerceIn(30f, containerHeightPx - pillHeightPx - 30f)

            // Optional subtle transcription caption (if explicitly enabled in settings)
            AnimatedVisibility(
                visible = config.showSubtleTranscription && currentQueryInput.isNotBlank() &&
                        (jarvisState == JarvisState.LISTENING || jarvisState == JarvisState.PROCESSING),
                enter = fadeIn(tween(180)),
                exit = fadeOut(tween(250)),
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = 0,
                            y = (pillTopOffsetYPx - with(density) { 56.dp.toPx() }).roundToInt()
                        )
                    }
                    .align(Alignment.TopCenter)
            ) {
                Text(
                    text = currentQueryInput,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 28.dp)
                        .background(Color(0x66000000), shape = CircleShape)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // The Siri-Style Voice Pill
            SiriPillVisualizer(
                state = jarvisState,
                config = config,
                audioLevel = audioLevel,
                onClick = { viewModel.onPillClicked() },
                onLongClick = { viewModel.onPillLongClicked() },
                onDragDelta = { dx, dy ->
                    liveCenterX = (liveCenterX + dx).coerceIn(minXPx, maxXPx)
                    liveCenterY = (liveCenterY + dy).coerceIn(minYPx, maxYPx)
                },
                onDragEnd = {
                    val xPct = (liveCenterX / containerWidthPx).coerceIn(0.05f, 0.95f)
                    val yPct = (liveCenterY / containerHeightPx).coerceIn(0.05f, 0.95f)
                    viewModel.updateCustomPillOffset(xPct, yPct)
                },
                modifier = Modifier.offset {
                    IntOffset(
                        x = pillTopLeftXPx.roundToInt(),
                        y = pillTopOffsetYPx.roundToInt()
                    )
                }
            )
        }
    }
}
