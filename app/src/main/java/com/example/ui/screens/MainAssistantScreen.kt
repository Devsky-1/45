package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.ChatMessageEntity
import com.example.ui.JarvisViewModel
import com.example.ui.components.ActiveTimerCard
import com.example.ui.components.ArcReactorVisualizer
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

    val listState = rememberLazyListState()

    // Auto-scroll on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickActionSuggestions = listOf(
        "Good morning",
        "Flashlight on",
        "Set 5 min timer",
        "Atmospheric report",
        "System diagnostic",
        "Protocol Clean Slate",
        "House Party Protocol"
    )

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
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder.copy(alpha = 0.7f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (jarvisState == JarvisState.ALERT) JarvisRed else JarvisCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "J.A.R.V.I.S. MARK 85",
                            color = JarvisCyanLight,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = activeProtocol,
                            color = if (jarvisState == JarvisState.ALERT) JarvisRed else JarvisGold,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                val context = LocalContext.current
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Siri/Assistant Voice Overlay Pop-up
                    IconButton(
                        onClick = {
                            val intent = Intent(context, JarvisAssistActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.testTag("btn_assistant_overlay_launch")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assistant,
                            contentDescription = "Test Assistant Overlay",
                            tint = JarvisCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Lock Screen Simulator button
                    IconButton(
                        onClick = { viewModel.toggleLockScreenSimulator(true) },
                        modifier = Modifier.testTag("btn_lockscreen_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Screen Mode",
                            tint = JarvisCyanLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Audio Mute toggle
                    IconButton(
                        onClick = { viewModel.toggleMute() },
                        modifier = Modifier.testTag("btn_mute_toggle")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = "Mute Voice",
                            tint = if (isMuted) JarvisRed else JarvisCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // SCROLLABLE CONTENT: ARC REACTOR + TIMERS + CHAT HISTORY
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Customizable Visualizer at the top of the HUD
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    com.example.ui.components.AssistantShapeContainer(
                        state = jarvisState,
                        config = config,
                        audioLevel = audioLevel,
                        onClick = { viewModel.toggleVoiceRecognition() }
                    )
                }
            }

            // Active Timers Section
            if (activeTimers.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "ACTIVE COUNTDOWN PROTOCOLS",
                            color = JarvisAmber,
                            fontSize = 11.sp,
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

            // Quick suggestion chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickActionSuggestions) { chipText ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = JarvisCardBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    viewModel.processUserQuery(chipText)
                                }
                                .testTag("chip_${chipText.replace(" ", "_")}")
                        ) {
                            Text(
                                text = chipText,
                                color = JarvisCyanLight,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Chat Messages Feed
            items(messages, key = { it.id }) { msg ->
                ChatMessageBubble(message = msg)
            }
        }

        // BOTTOM CONTROLS & INPUT BAR
        Surface(
            color = JarvisCardBg.copy(alpha = 0.95f),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
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
                                text = if (jarvisState == JarvisState.LISTENING) "Listening..." else "Ask Jarvis anything or give directive...",
                                color = JarvisTextMuted,
                                fontSize = 13.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisCardBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary,
                            cursorColor = JarvisCyan,
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
                                .background(JarvisCyan)
                                .testTag("btn_submit_query")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = Color(0xFF001F24)
                            )
                        }
                    } else {
                        FloatingActionButton(
                            onClick = { viewModel.toggleVoiceRecognition() },
                            containerColor = if (jarvisState == JarvisState.LISTENING) JarvisAmber else JarvisCyan,
                            contentColor = Color(0xFF001F24),
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                            modifier = Modifier
                                .size(44.dp)
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
fun ChatMessageBubble(message: ChatMessageEntity) {
    val isJarvis = message.sender.equals("JARVIS", ignoreCase = true)
    val timeString = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))

    Column(
        horizontalAlignment = if (isJarvis) Alignment.Start else Alignment.End,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isJarvis) Arrangement.Start else Arrangement.End,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Text(
                text = if (isJarvis) "JARVIS" else "USER",
                color = if (isJarvis) JarvisCyan else JarvisGold,
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
            Column(modifier = Modifier.padding(12.dp)) {
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
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
