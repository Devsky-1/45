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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.ChatMessageEntity
import com.example.device.ActiveTimer
import com.example.ui.JarvisViewModel
import com.example.ui.components.AssistantShapeContainer
import com.example.ui.components.HoloCard
import com.example.ui.components.JarvisState
import com.example.ui.components.VoiceWaveformVisualizer
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisCardBg
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCardBorderCyan
import com.example.ui.theme.JarvisCardGlass
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisObsidian
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisSpaceDark
import com.example.ui.theme.JarvisSpaceMid
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
    val activeProtocol by viewModel.activeProtocol.collectAsStateWithLifecycle()
    val config by viewModel.appearanceConfig.collectAsStateWithLifecycle()
    val isAmbientListening by viewModel.isAmbientWakeWordListening.collectAsStateWithLifecycle()
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Auto-scroll on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickActions = remember {
        listOf(
            QuickActionItem("Briefing", "Good morning", Icons.Default.WbSunny, JarvisAmber),
            QuickActionItem("Flashlight", "Flashlight on", Icons.Default.FlashlightOn, JarvisCyan),
            QuickActionItem("5m Timer", "Set 5 minute timer", Icons.Default.Timer, JarvisGreen),
            QuickActionItem("Diagnostics", "System diagnostic", Icons.Default.ElectricBolt, JarvisCyanLight),
            QuickActionItem("Stealth", "Protocol Stealth Mode", Icons.Default.Security, JarvisBlue),
            QuickActionItem("House Party", "House Party Protocol", Icons.Default.PowerSettingsNew, JarvisRed),
            QuickActionItem("Clean Slate", "Protocol Clean Slate", Icons.Default.Refresh, JarvisGold)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        JarvisObsidian,
                        JarvisSpaceDark,
                        JarvisSpaceMid,
                        JarvisObsidian
                    )
                )
            )
    ) {
        // TOP HUD BAR
        Surface(
            color = JarvisCardGlass,
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            border = BorderStroke(1.dp, JarvisCardBorder.copy(alpha = 0.7f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // Left Brand & Wake Word Indicator
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (jarvisState) {
                                        JarvisState.ALERT -> JarvisRed
                                        JarvisState.LISTENING -> JarvisGreen
                                        JarvisState.SPEAKING -> JarvisAmber
                                        JarvisState.PROCESSING -> JarvisCyanLight
                                        JarvisState.STANDBY -> if (config.wakeWordEnabled && isAmbientListening) JarvisGreen else JarvisCyan
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "J.A.R.V.I.S. MARK 85",
                            color = config.colorTheme.accentColor,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clickable { viewModel.setSelectedTab(1) } // Navigate to Studio
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = "Wake Word",
                            tint = if (config.wakeWordEnabled) JarvisCyanLight else JarvisTextMuted,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (config.wakeWordEnabled) "SAY \"${config.effectiveWakeWord.uppercase()}\"" else "WAKE WORD DISABLED",
                            color = if (config.wakeWordEnabled) JarvisCyanLight else JarvisTextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Right Utility Action Icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Studio shortcut
                    IconButton(
                        onClick = { viewModel.setSelectedTab(1) },
                        modifier = Modifier.size(36.dp).testTag("btn_nav_studio")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Studio Customization",
                            tint = config.colorTheme.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Quick Translucent Siri Overlay Launch
                    IconButton(
                        onClick = {
                            val intent = Intent(context, JarvisAssistActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.size(36.dp).testTag("btn_assistant_overlay_launch")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assistant,
                            contentDescription = "Assistant Overlay",
                            tint = JarvisCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Lock Screen Mode Simulator
                    IconButton(
                        onClick = { viewModel.toggleLockScreenSimulator(true) },
                        modifier = Modifier.size(36.dp).testTag("btn_lockscreen_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Screen Mode",
                            tint = JarvisCyanLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Audio Mute toggle
                    IconButton(
                        onClick = { viewModel.toggleMute() },
                        modifier = Modifier.size(36.dp).testTag("btn_mute_toggle")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = "Mute Voice",
                            tint = if (isMuted) JarvisRed else JarvisCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // SCROLLABLE CONTENT: VISUALIZER + TIMERS + QUICK COMMANDS + CONVERSATION
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Hero Interactive Assistant Orb / Shape
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    AssistantShapeContainer(
                        state = jarvisState,
                        config = config,
                        audioLevel = audioLevel,
                        onClick = { viewModel.toggleVoiceRecognition() },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // State Status Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (jarvisState) {
                            JarvisState.LISTENING -> JarvisGreen.copy(alpha = 0.15f)
                            JarvisState.PROCESSING -> JarvisCyan.copy(alpha = 0.15f)
                            JarvisState.SPEAKING -> JarvisAmber.copy(alpha = 0.15f)
                            JarvisState.ALERT -> JarvisRed.copy(alpha = 0.15f)
                            JarvisState.STANDBY -> JarvisObsidian
                        },
                        border = BorderStroke(
                            1.dp,
                            when (jarvisState) {
                                JarvisState.LISTENING -> JarvisGreen.copy(alpha = 0.6f)
                                JarvisState.PROCESSING -> JarvisCyan.copy(alpha = 0.6f)
                                JarvisState.SPEAKING -> JarvisAmber.copy(alpha = 0.6f)
                                JarvisState.ALERT -> JarvisRed.copy(alpha = 0.6f)
                                JarvisState.STANDBY -> JarvisCardBorder
                            }
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = when (jarvisState) {
                                JarvisState.LISTENING -> "● ACTIVE LISTENING..."
                                JarvisState.PROCESSING -> "● PROCESSING DIRECTIVE..."
                                JarvisState.SPEAKING -> "● TRANSMITTING AUDIO..."
                                JarvisState.ALERT -> "● ALERT PROTOCOL ACTIVE"
                                JarvisState.STANDBY -> if (config.wakeWordEnabled) "SAY \"${config.effectiveWakeWord.uppercase()}\" OR TAP ORB" else "TAP ORB OR MIC TO SPEAK"
                            },
                            color = when (jarvisState) {
                                JarvisState.LISTENING -> JarvisGreen
                                JarvisState.PROCESSING -> JarvisCyanLight
                                JarvisState.SPEAKING -> JarvisAmber
                                JarvisState.ALERT -> JarvisRed
                                JarvisState.STANDBY -> JarvisTextSecondary
                            },
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Active Timers Section
            if (activeTimers.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "ACTIVE COUNTDOWN PROTOCOLS",
                            color = JarvisAmber,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        activeTimers.forEach { timer ->
                            ActiveTimerCard(
                                timer = timer,
                                onCancel = { viewModel.cancelTimer(timer.id) }
                            )
                        }
                    }
                }
            }

            // Quick Tactical Command Action Bar
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickActions) { item ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = JarvisCardBg,
                            border = BorderStroke(1.dp, item.color.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    viewModel.processUserQuery(item.command)
                                }
                                .testTag("chip_${item.title.replace(" ", "_")}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = item.color,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = item.title,
                                    color = JarvisTextPrimary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Chat Messages Feed
            items(messages, key = { it.id }) { msg ->
                EnhancedChatMessageBubble(
                    message = msg,
                    onReplay = { viewModel.ttsHelper.speak(msg.text) }
                )
            }
        }

        // BOTTOM CONTROLS & VOICE INPUT DOCK
        Surface(
            color = JarvisCardBg.copy(alpha = 0.95f),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            border = BorderStroke(1.dp, JarvisCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // Waveform indicator during active voice
                if (jarvisState == JarvisState.LISTENING || jarvisState == JarvisState.SPEAKING) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        VoiceWaveformVisualizer(
                            isListening = jarvisState == JarvisState.LISTENING,
                            audioLevel = audioLevel
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = currentInput,
                        onValueChange = { viewModel.setQueryInput(it) },
                        placeholder = {
                            Text(
                                text = if (jarvisState == JarvisState.LISTENING) "Listening to voice..." else "Say \"${config.effectiveWakeWord}\" or type...",
                                color = JarvisTextMuted,
                                fontSize = 12.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = config.colorTheme.accentColor,
                            unfocusedBorderColor = JarvisCardBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary,
                            cursorColor = config.colorTheme.accentColor,
                            focusedContainerColor = Color(0xFF070D1A),
                            unfocusedContainerColor = Color(0xFF070D1A)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_command_query")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    if (currentInput.isNotBlank()) {
                        IconButton(
                            onClick = { viewModel.submitCurrentQuery() },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(config.colorTheme.accentColor)
                                .testTag("btn_submit_query")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = Color(0xFF001F24)
                            )
                        }
                    } else {
                        // Mic Button with soft pulse
                        val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
                        val micScale by infiniteTransition.animateFloat(
                            initialValue = 1.0f,
                            targetValue = if (jarvisState == JarvisState.LISTENING) 1.15f else 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "mic_scale"
                        )

                        FloatingActionButton(
                            onClick = { viewModel.toggleVoiceRecognition() },
                            containerColor = if (jarvisState == JarvisState.LISTENING) JarvisAmber else config.colorTheme.accentColor,
                            contentColor = Color(0xFF001F24),
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                            modifier = Modifier
                                .size(44.dp)
                                .scale(micScale)
                                .testTag("btn_voice_input")
                        ) {
                            Icon(
                                imageVector = if (jarvisState == JarvisState.LISTENING) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Voice Command",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedChatMessageBubble(
    message: ChatMessageEntity,
    onReplay: () -> Unit
) {
    val isJarvis = message.sender.equals("JARVIS", ignoreCase = true)
    val timeString = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))
    val context = LocalContext.current

    Column(
        horizontalAlignment = if (isJarvis) Alignment.Start else Alignment.End,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isJarvis) Arrangement.Start else Arrangement.End,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Text(
                text = if (isJarvis) "JARVIS" else "YOU",
                color = if (isJarvis) JarvisCyanLight else JarvisGold,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = timeString,
                color = JarvisTextMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        HoloCard(
            borderColor = if (isJarvis) JarvisCardBorderCyan.copy(alpha = 0.4f) else JarvisBlue.copy(alpha = 0.5f),
            glowEffect = isJarvis,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                if (!message.actionType.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = JarvisCyanGlow,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "[${message.actionType}]",
                            color = JarvisCyanLight,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = message.text,
                    color = JarvisTextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                // Message Action footer for Jarvis responses
                if (isJarvis) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        IconButton(
                            onClick = onReplay,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play voice",
                                tint = JarvisCyan.copy(alpha = 0.8f),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Jarvis message", message.text)
                                clipboard.setPrimaryClip(clip)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy text",
                                tint = JarvisTextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
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

    HoloCard(
        borderColor = JarvisAmber.copy(alpha = 0.6f),
        glowEffect = true,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Timer",
                    tint = JarvisAmber,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = timer.label.ifBlank { "Countdown Protocol" },
                        color = JarvisTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "REMAINING: $formattedTime",
                        color = JarvisAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            IconButton(
                onClick = onCancel,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Cancel Timer",
                    tint = JarvisRed,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

