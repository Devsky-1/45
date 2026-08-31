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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Replay
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AssistantPersonality
import com.example.domain.JarvisCoreEngine
import com.example.ui.components.AssistantShapeContainer
import com.example.ui.components.JarvisState
import com.example.ui.components.SiriScreenEdgeGlow
import com.example.ui.components.VoiceWaveformVisualizer
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisObsidian
import com.example.ui.theme.JarvisRed
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
    val config by engine.appearanceConfig.collectAsState()

    var showTextInput by remember { mutableStateOf(false) }
    var manualTypedQuery by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    val infiniteTransition = rememberInfiniteTransition(label = "overlay_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Auto-listen on entry if configured
    LaunchedEffect(Unit) {
        if (config.autoListenOnOpen && jarvisState != JarvisState.LISTENING) {
            engine.toggleVoiceRecognition()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xE6050811))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 1. Siri iOS 18 Edge Glow running around screen perimeter
        SiriScreenEdgeGlow(
            state = jarvisState,
            colorTheme = config.colorTheme,
            isActive = true
        )

        // 2. Google Assistant / Apple Siri Frosted Glass Bottom Modal
        Card(
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xF20F1626)
            ),
            border = BorderStroke(
                1.dp,
                Brush.verticalGradient(
                    listOf(
                        Color(0x66FFFFFF),
                        config.colorTheme.primaryColor.copy(alpha = 0.4f),
                        Color(0x1AFFFFFF)
                    )
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
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Grabber / Pill
                Box(
                    modifier = Modifier
                        .size(width = 42.dp, height = 5.dp)
                        .clip(CircleShape)
                        .background(Color(0x4DFFFFFF))
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Top Header Row
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x22FFFFFF))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (jarvisState) {
                                        JarvisState.LISTENING -> config.colorTheme.accentColor
                                        JarvisState.PROCESSING -> JarvisAmber
                                        JarvisState.SPEAKING -> JarvisGreen
                                        JarvisState.ALERT -> JarvisRed
                                        JarvisState.STANDBY -> config.colorTheme.primaryColor
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = config.effectiveWakeWord.uppercase(),
                            color = JarvisTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
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
                                modifier = Modifier.size(20.dp)
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

                Spacer(modifier = Modifier.height(10.dp))

                // CENTERPIECE: EXACT SHAPE SELECTED IN STUDIO
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    AssistantShapeContainer(
                        state = jarvisState,
                        config = config,
                        audioLevel = rmsAudio,
                        onClick = { engine.toggleVoiceRecognition() }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Real-time audio waveform
                if (jarvisState == JarvisState.LISTENING || isSpeaking) {
                    VoiceWaveformVisualizer(
                        isListening = true,
                        audioLevel = rmsAudio,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Dynamic Status Text
                Text(
                    text = when (jarvisState) {
                        JarvisState.LISTENING -> "Listening to your voice..."
                        JarvisState.PROCESSING -> "Thinking..."
                        JarvisState.SPEAKING -> "Responding..."
                        JarvisState.ALERT -> "Tactical Protocol Engaged"
                        JarvisState.STANDBY -> if (config.continuousVoiceConversation) "Hands-free ready • Speak anytime" else "Tap orb or speak to ask"
                    },
                    color = when (jarvisState) {
                        JarvisState.LISTENING -> config.colorTheme.accentColor
                        JarvisState.PROCESSING -> JarvisAmber
                        JarvisState.SPEAKING -> JarvisGreen
                        JarvisState.ALERT -> JarvisRed
                        JarvisState.STANDBY -> JarvisTextSecondary
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(10.dp))

                // User Input Bubble (if recognized)
                if (queryInput.isNotBlank()) {
                    Surface(
                        color = Color(0x2E1E3A8A),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, config.colorTheme.primaryColor.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "You: ",
                                color = config.colorTheme.accentColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
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

                // Assistant Response Card
                Surface(
                    color = Color(0x3D111827),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color(0x2EFFFFFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(config.colorTheme.accentColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = config.personality.displayName,
                                    color = config.colorTheme.accentColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(lastResponse))
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Response",
                                        tint = JarvisTextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { engine.speakText(lastResponse) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Replay,
                                        contentDescription = "Replay Speech",
                                        tint = JarvisTextSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = lastResponse,
                            color = JarvisTextPrimary,
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Suggestion Capsules (Google Assistant / Siri Style)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AssistQuickChip("🔦 Flashlight", onClick = { engine.processUserQuery("turn on flashlight") })
                    AssistQuickChip("⏱️ 5m Timer", onClick = { engine.processUserQuery("set a timer for 5 minutes") })
                    AssistQuickChip("📊 Briefing", onClick = { engine.processUserQuery("status briefing") })
                    AssistQuickChip("🔋 Battery", onClick = { engine.processUserQuery("what is my battery level") })
                    AssistQuickChip("⚡ Diagnostics", onClick = { engine.processUserQuery("run diagnostic check") })
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Optional Keyboard input area
                AnimatedVisibility(
                    visible = showTextInput,
                    enter = fadeIn() + slideInVertically()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        OutlinedTextField(
                            value = manualTypedQuery,
                            onValueChange = { manualTypedQuery = it },
                            placeholder = { Text("Ask anything...", color = JarvisTextMuted, fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = config.colorTheme.accentColor,
                                unfocusedBorderColor = Color(0x33FFFFFF),
                                focusedTextColor = JarvisTextPrimary,
                                unfocusedTextColor = JarvisTextPrimary,
                                focusedContainerColor = Color(0x33000000),
                                unfocusedContainerColor = Color(0x22000000)
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (manualTypedQuery.isNotBlank()) {
                                    engine.processUserQuery(manualTypedQuery)
                                    manualTypedQuery = ""
                                    showTextInput = false
                                }
                            }),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (manualTypedQuery.isNotBlank()) {
                                    engine.processUserQuery(manualTypedQuery)
                                    manualTypedQuery = ""
                                    showTextInput = false
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(config.colorTheme.accentColor)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = Color(0xFF001F24),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // BOTTOM ACTION DOCK
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Keyboard toggle button
                    IconButton(
                        onClick = { showTextInput = !showTextInput },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FFFFFF))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "Type Query",
                            tint = JarvisTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Main Glowing Assistant Orb/Mic Trigger
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .scale(if (jarvisState == JarvisState.LISTENING) pulseScale else 1.0f)
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = if (jarvisState == JarvisState.LISTENING) {
                                        config.colorTheme.gradientColors
                                    } else {
                                        listOf(config.colorTheme.primaryColor, config.colorTheme.accentColor)
                                    }
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

                    // Close action button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FFFFFF))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = JarvisTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
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
        shape = RoundedCornerShape(14.dp),
        color = Color(0x26FFFFFF),
        border = BorderStroke(1.dp, Color(0x33FFFFFF))
    ) {
        Text(
            text = label,
            color = JarvisTextPrimary,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}
