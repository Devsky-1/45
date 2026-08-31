package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.JarvisCoreEngine
import com.example.ui.components.ArcReactorVisualizer
import com.example.ui.components.JarvisState
import com.example.ui.components.VoiceWaveformVisualizer
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisAmberGlow
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisCardBg
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCardBorderCyan
import com.example.ui.theme.JarvisCardGlass
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisObsidian
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisSpaceDark
import com.example.ui.theme.JarvisSpaceMid
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AssistantOverlayContent(
    engine: JarvisCoreEngine,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val jarvisState by engine.jarvisState.collectAsState()
    val rmsAudio by engine.rmsAudioLevel.collectAsState()
    val lastResponse by engine.lastJarvisResponse.collectAsState()
    val queryInput by engine.currentQueryInput.collectAsState()
    val isSpeaking by engine.isTtsSpeaking.collectAsState()
    val isMuted by engine.isMuted.collectAsState()

    var showTextInput by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "overlay_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Auto-listen on entry if in standby
    LaunchedEffect(Unit) {
        if (jarvisState != JarvisState.LISTENING) {
            engine.toggleVoiceRecognition()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisObsidian.copy(alpha = 0.85f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Main Holographic Bottom Sheet
        Card(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            colors = CardDefaults.cardColors(
                containerColor = JarvisCardBg.copy(alpha = 0.96f)
            ),
            border = BorderStroke(
                1.5.dp,
                Brush.verticalGradient(
                    listOf(JarvisCyan, JarvisCardBorder.copy(alpha = 0.6f))
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* Consume clicks to prevent dismissal */ }
                )
                .testTag("assistant_overlay_card")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header Pill
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    when (jarvisState) {
                                        JarvisState.LISTENING -> JarvisCyan
                                        JarvisState.PROCESSING -> JarvisAmber
                                        JarvisState.SPEAKING -> JarvisGreen
                                        JarvisState.ALERT -> JarvisRed
                                        JarvisState.STANDBY -> JarvisCyan.copy(alpha = 0.5f)
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "J.A.R.V.I.S. ASSISTANT OVERLAY",
                            color = JarvisCyanLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { engine.toggleMute() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = "Toggle Mute",
                                tint = if (isMuted) JarvisRed else JarvisTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("overlay_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Overlay",
                                tint = JarvisTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Arc Reactor Holographic Center Visualizer
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(140.dp)
                        .padding(vertical = 4.dp)
                ) {
                    ArcReactorVisualizer(
                        state = jarvisState,
                        audioLevel = rmsAudio,
                        modifier = Modifier.size(130.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Real-time audio waveform when listening or speaking
                if (jarvisState == JarvisState.LISTENING || isSpeaking) {
                    VoiceWaveformVisualizer(
                        isListening = true,
                        audioLevel = rmsAudio,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Dynamic Status Text
                Text(
                    text = when (jarvisState) {
                        JarvisState.LISTENING -> "Listening to your voice..."
                        JarvisState.PROCESSING -> "Analyzing command & telemetry..."
                        JarvisState.SPEAKING -> "J.A.R.V.I.S. Responding..."
                        JarvisState.ALERT -> "Tactical Protocol Engaged"
                        JarvisState.STANDBY -> "Tap microphone or speak"
                    },
                    color = when (jarvisState) {
                        JarvisState.LISTENING -> JarvisCyan
                        JarvisState.PROCESSING -> JarvisAmber
                        JarvisState.SPEAKING -> JarvisGreen
                        JarvisState.ALERT -> JarvisRed
                        JarvisState.STANDBY -> JarvisTextSecondary
                    },
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(10.dp))

                // User Input / Recognized Query Display
                if (queryInput.isNotBlank()) {
                    Surface(
                        color = JarvisSpaceDark.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, JarvisCyanGlow),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "YOU: ",
                                color = JarvisCyanLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = queryInput,
                                color = JarvisTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Jarvis Response Card
                Surface(
                    color = JarvisSpaceMid.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, JarvisCardBorderCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "J.A.R.V.I.S. INTELLIGENCE",
                                color = JarvisCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            if (isSpeaking) {
                                Text(
                                    text = "SPEAKING",
                                    color = JarvisGreen,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = lastResponse,
                            color = JarvisTextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tactical Quick Action Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AssistQuickChip("🔦 Torch", onClick = { engine.processUserQuery("turn on flashlight") })
                    AssistQuickChip("📊 Daily Brief", onClick = { engine.processUserQuery("status briefing") })
                    AssistQuickChip("⏱️ 5m Timer", onClick = { engine.processUserQuery("set a timer for 5 minutes") })
                    AssistQuickChip("⚡ Diagnostics", onClick = { engine.processUserQuery("run diagnostic check") })
                    AssistQuickChip("🛡️ Sentry Mode", onClick = { engine.processUserQuery("Perimeter Defense Grid") })
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Microphone & Input Action Row
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Tap to Speak Glowing Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .scale(if (jarvisState == JarvisState.LISTENING) pulseScale else 1.0f)
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        if (jarvisState == JarvisState.LISTENING) JarvisCyan else JarvisBlue,
                                        JarvisObsidian
                                    )
                                )
                            )
                            .clickable {
                                engine.toggleVoiceRecognition()
                            }
                            .testTag("overlay_mic_button")
                    ) {
                        Icon(
                            imageVector = if (jarvisState == JarvisState.LISTENING) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = "Voice Input",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (jarvisState == JarvisState.LISTENING) "Listening... Tap to stop" else "Tap to activate voice",
                    color = JarvisTextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun AssistQuickChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = JarvisCardGlass,
        border = BorderStroke(1.dp, JarvisCardBorder.copy(alpha = 0.8f))
    ) {
        Text(
            text = label,
            color = JarvisCyanLight,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}
