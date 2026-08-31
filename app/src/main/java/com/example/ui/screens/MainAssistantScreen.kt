package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.ChatMessageEntity
import com.example.device.ActiveTimer
import com.example.ui.JarvisViewModel
import com.example.ui.components.AssistantShapeContainer
import com.example.ui.components.JarvisState
import com.example.ui.components.VoiceWaveformVisualizer
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisObsidian
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisSpaceDark
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class QuickActionItem(
    val title: String,
    val command: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun MainAssistantScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val jarvisState by viewModel.jarvisState.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val currentInput by viewModel.currentQueryInput.collectAsStateWithLifecycle()
    val activeTimers by viewModel.activeTimers.collectAsStateWithLifecycle()
    val audioLevel by viewModel.rmsAudioLevel.collectAsStateWithLifecycle()
    val isMuted by viewModel.isMuted.collectAsStateWithLifecycle()
    val config by viewModel.appearanceConfig.collectAsStateWithLifecycle()
    val isAmbientListening by viewModel.isAmbientWakeWordListening.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
    val micPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse"
    )

    // Auto-scroll on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickActions = remember {
        listOf(
            QuickActionItem("Flashlight", "Flashlight on", Icons.Default.FlashlightOn, JarvisCyan),
            QuickActionItem("5m Timer", "Set 5 minute timer", Icons.Default.Timer, JarvisGreen),
            QuickActionItem("Briefing", "Good morning", Icons.Default.WbSunny, JarvisAmber),
            QuickActionItem("Diagnostics", "System diagnostic", Icons.Default.ElectricBolt, JarvisCyanLight),
            QuickActionItem("Stealth", "Protocol Stealth Mode", Icons.Default.Security, JarvisBlue),
            QuickActionItem("House Party", "House Party Protocol", Icons.Default.PowerSettingsNew, JarvisRed),
            QuickActionItem("Clean Slate", "Protocol Clean Slate", Icons.Default.DeleteSweep, JarvisGold)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070B14),
                        Color(0xFF0C1322),
                        Color(0xFF090E1A)
                    )
                )
            )
    ) {
        // TOP GLASS APP BAR
        Surface(
            color = Color(0xCC11192C),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            border = BorderStroke(1.dp, Color(0x2EFFFFFF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Left Brand Pill & Wake Word Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x26FFFFFF))
                        .clickable {
                            viewModel.toggleAmbientWakeWord(!config.wakeWordEnabled)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(
                                when (jarvisState) {
                                    JarvisState.ALERT -> JarvisRed
                                    JarvisState.LISTENING -> config.colorTheme.accentColor
                                    JarvisState.SPEAKING -> JarvisAmber
                                    JarvisState.PROCESSING -> JarvisCyanLight
                                    JarvisState.STANDBY -> if (config.wakeWordEnabled && isAmbientListening) JarvisGreen else config.colorTheme.primaryColor
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = if (config.wakeWordEnabled) "🎤 ${config.effectiveWakeWord}" else "Muted",
                        color = JarvisTextPrimary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Right Quick Action Icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Test Overlay Button
                    Surface(
                        onClick = {
                            val intent = Intent(context, JarvisAssistActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = config.colorTheme.primaryColor.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, config.colorTheme.accentColor.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("btn_test_overlay")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Overlay Mode",
                                tint = config.colorTheme.accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "OVERLAY",
                                color = config.colorTheme.accentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { viewModel.toggleMute() },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Toggle Mute",
                            tint = if (isMuted) JarvisRed else JarvisTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.setSelectedTab(1) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Studio Settings",
                            tint = config.colorTheme.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // MAIN CONTENT SCROLL AREA
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // HERO SECTION: Selected Shape Centerpiece
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    AssistantShapeContainer(
                        state = jarvisState,
                        config = config,
                        audioLevel = audioLevel,
                        onClick = { viewModel.toggleVoiceRecognition() },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Audio waveform when listening or speaking
                    if (jarvisState == JarvisState.LISTENING || jarvisState == JarvisState.SPEAKING) {
                        VoiceWaveformVisualizer(
                            isListening = true,
                            audioLevel = audioLevel,
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .padding(vertical = 4.dp)
                        )
                    }

                    // Status Indicator
                    Text(
                        text = when (jarvisState) {
                            JarvisState.LISTENING -> "Listening to your voice..."
                            JarvisState.PROCESSING -> "Thinking..."
                            JarvisState.SPEAKING -> "Responding..."
                            JarvisState.ALERT -> "Tactical Protocol Engaged"
                            JarvisState.STANDBY -> "Ready • Say '${config.effectiveWakeWord}' or tap orb"
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
                }
            }

            // QUICK ACTIONS HORIZONTAL CAROUSEL
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickActions) { action ->
                        Surface(
                            onClick = { viewModel.processUserQuery(action.command) },
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0x2B1F293D),
                            border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                            modifier = Modifier.testTag("action_${action.title.lowercase().replace(" ", "_")}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = action.icon,
                                    contentDescription = action.title,
                                    tint = action.color,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = action.title,
                                    color = JarvisTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // ACTIVE TIMERS (if any)
            if (activeTimers.isNotEmpty()) {
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        activeTimers.forEach { timer ->
                            ActiveTimerCard(
                                timer = timer,
                                onCancel = { viewModel.cancelTimer(timer.id) }
                            )
                        }
                    }
                }
            }

            // CONVERSATION HISTORY HEADER
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = "CONVERSATION",
                        color = JarvisTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    if (messages.isNotEmpty()) {
                        Text(
                            text = "Clear",
                            color = config.colorTheme.accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.clearConversation() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // CHAT MESSAGES
            if (messages.isEmpty()) {
                item {
                    Surface(
                        color = Color(0x1F1E293B),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Color(0x1AFFFFFF)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "How can I help you today?",
                                color = JarvisTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Say '${config.effectiveWakeWord}' anytime from home screen, or tap the glowing orb below to speak.",
                                color = JarvisTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            } else {
                items(messages, key = { it.id }) { msg ->
                    ModernChatMessageItem(
                        message = msg,
                        config = config,
                        onReplay = { viewModel.speakText(msg.text) },
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            clipboard?.setPrimaryClip(ClipData.newPlainText("Jarvis Message", msg.text))
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // BOTTOM VOICE & INPUT DOCK
        Surface(
            color = Color(0xF00D1322),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            border = BorderStroke(1.dp, Color(0x2BFFFFFF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Modern Query Input Field
                    OutlinedTextField(
                        value = currentInput,
                        onValueChange = { viewModel.setQueryInput(it) },
                        placeholder = {
                            Text(
                                text = "Ask anything or type command...",
                                color = JarvisTextMuted,
                                fontSize = 13.sp
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(26.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = config.colorTheme.accentColor,
                            unfocusedBorderColor = Color(0x2EFFFFFF),
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary,
                            focusedContainerColor = Color(0x26000000),
                            unfocusedContainerColor = Color(0x1A000000)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (currentInput.isNotBlank()) {
                                viewModel.processUserQuery(currentInput)
                            }
                        }),
                        trailingIcon = {
                            if (currentInput.isNotBlank()) {
                                IconButton(
                                    onClick = { viewModel.processUserQuery(currentInput) },
                                    modifier = Modifier.testTag("btn_send_query")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send",
                                        tint = config.colorTheme.accentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("main_query_input")
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Siri / Google Assistant Glowing Microphone Trigger
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .scale(if (jarvisState == JarvisState.LISTENING) micPulse else 1.0f)
                            .size(52.dp)
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
                                viewModel.toggleVoiceRecognition()
                            }
                            .testTag("main_mic_button")
                    ) {
                        Icon(
                            imageVector = if (jarvisState == JarvisState.LISTENING) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = "Microphone",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernChatMessageItem(
    message: ChatMessageEntity,
    config: com.example.data.repository.AssistantAppearanceConfig,
    onReplay: () -> Unit,
    onCopy: () -> Unit
) {
    val isUser = message.sender.equals("USER", ignoreCase = true)
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    Row(
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (!isUser) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(config.colorTheme.primaryColor.copy(alpha = 0.25f))
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Assistant,
                    contentDescription = "Assistant",
                    tint = config.colorTheme.accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            color = if (isUser) Color(0x3D1E3A8A) else Color(0x2E1E293B),
            border = BorderStroke(
                1.dp,
                if (isUser) config.colorTheme.primaryColor.copy(alpha = 0.5f) else Color(0x26FFFFFF)
            ),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isUser) "YOU" else config.personality.displayName.uppercase(),
                        color = if (isUser) config.colorTheme.accentColor else config.colorTheme.primaryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formattedTime,
                            color = JarvisTextMuted,
                            fontSize = 9.sp
                        )
                        if (!isUser) {
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = onCopy,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = JarvisTextMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            IconButton(
                                onClick = onReplay,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay,
                                    contentDescription = "Replay",
                                    tint = config.colorTheme.accentColor,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.text,
                    color = JarvisTextPrimary,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun ActiveTimerCard(
    timer: ActiveTimer,
    onCancel: () -> Unit
) {
    val totalSec = timer.remainingSeconds.coerceAtLeast(0)
    val mins = totalSec / 60
    val secs = totalSec % 60
    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0x33B45309),
        border = BorderStroke(1.dp, JarvisAmber.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Timer",
                    tint = JarvisAmber,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = timer.label.ifBlank { "Countdown Timer" },
                        color = JarvisTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "REMAINING: $formattedTime",
                        color = JarvisAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(
                onClick = onCancel,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Cancel Timer",
                    tint = JarvisRed,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
