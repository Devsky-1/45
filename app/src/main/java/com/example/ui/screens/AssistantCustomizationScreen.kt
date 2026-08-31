package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.AssistantAppearanceConfig
import com.example.data.repository.AssistantColorTheme
import com.example.data.repository.AssistantPersonality
import com.example.data.repository.AssistantShape
import com.example.data.repository.WAKE_WORD_PRESETS
import com.example.service.JarvisFloatingOverlayService
import com.example.ui.JarvisViewModel
import com.example.ui.components.AssistantShapeContainer
import com.example.ui.components.JarvisState
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AssistantCustomizationScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val config by viewModel.appearanceConfig.collectAsStateWithLifecycle()
    val jarvisState by viewModel.jarvisState.collectAsStateWithLifecycle()
    val audioLevel by viewModel.rmsAudioLevel.collectAsStateWithLifecycle()
    val isAmbientListening by viewModel.isAmbientWakeWordListening.collectAsStateWithLifecycle()

    var customInputText by remember(config.customWakeWord) { mutableStateOf(config.customWakeWord) }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070B14),
                        Color(0xFF0D1424),
                        Color(0xFF080C18)
                    )
                )
            )
            .testTag("customization_screen")
    ) {
        // TOP APP BAR
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
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(config.colorTheme.primaryColor.copy(alpha = 0.25f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Studio",
                            tint = config.colorTheme.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ASSISTANT STUDIO",
                            color = JarvisTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "WAKE WORD, FORM & INTELLIGENCE",
                            color = JarvisTextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Surface(
                    onClick = { viewModel.resetToDefaults() },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0x1AFFFFFF),
                    border = BorderStroke(1.dp, Color(0x33FFFFFF))
                ) {
                    Text(
                        text = "Reset",
                        color = JarvisTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // MAIN SETTINGS LIST
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. LIVE INTERACTIVE STAGE
            item {
                StudioSectionCard(
                    title = "LIVE ASSISTANT PREVIEW",
                    icon = Icons.Default.Sensors,
                    accentColor = config.colorTheme.accentColor
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Live Shape Viewport
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0x33000000))
                                .border(1.dp, config.colorTheme.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                .padding(12.dp)
                        ) {
                            AssistantShapeContainer(
                                state = jarvisState,
                                config = config,
                                audioLevel = audioLevel,
                                onClick = { viewModel.toggleVoiceRecognition() }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Trigger actions
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Test voice recognition
                            Button(
                                onClick = { viewModel.toggleVoiceRecognition() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (jarvisState == JarvisState.LISTENING) config.colorTheme.accentColor else Color(0x26FFFFFF)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, config.colorTheme.accentColor.copy(alpha = 0.5f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (jarvisState == JarvisState.LISTENING) Icons.Default.Mic else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (jarvisState == JarvisState.LISTENING) Color.Black else JarvisTextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (jarvisState == JarvisState.LISTENING) "Listening..." else "Test Voice",
                                    color = if (jarvisState == JarvisState.LISTENING) Color.Black else JarvisTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Test System Overlay
                            Button(
                                onClick = {
                                    val intent = Intent(context, JarvisAssistActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    }
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = config.colorTheme.primaryColor.copy(alpha = 0.35f)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, config.colorTheme.accentColor),
                                modifier = Modifier
                                    .weight(1.1f)
                                    .testTag("btn_test_overlay_studio")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = config.colorTheme.accentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Test Home Overlay",
                                    color = JarvisTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // 2. WAKE WORD TRIGGER SELECTOR
            item {
                StudioSectionCard(
                    title = "WAKE WORD TRIGGER",
                    icon = Icons.Default.Hearing,
                    accentColor = config.colorTheme.accentColor
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Choose the phrase you say on your home screen or anywhere to summon the assistant:",
                            color = JarvisTextSecondary,
                            fontSize = 12.sp
                        )

                        // Preset cards
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            WAKE_WORD_PRESETS.forEach { preset ->
                                val isSelected = config.selectedWakeWord == preset && config.customWakeWord.isBlank()
                                Surface(
                                    onClick = { viewModel.updateSelectedWakeWord(preset) },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) config.colorTheme.primaryColor.copy(alpha = 0.35f) else Color(0x1F1E293B),
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) config.colorTheme.accentColor else Color(0x26FFFFFF)
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = config.colorTheme.accentColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = preset,
                                            color = if (isSelected) JarvisTextPrimary else JarvisTextSecondary,
                                            fontSize = 12.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Custom wake word input
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = customInputText,
                                onValueChange = { customInputText = it },
                                placeholder = { Text("Or enter custom wake word...", color = JarvisTextMuted, fontSize = 12.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = config.colorTheme.accentColor,
                                    unfocusedBorderColor = Color(0x33FFFFFF),
                                    focusedTextColor = JarvisTextPrimary,
                                    unfocusedTextColor = JarvisTextPrimary,
                                    focusedContainerColor = Color(0x22000000),
                                    unfocusedContainerColor = Color(0x1A000000)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("custom_wakeword_input")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.updateCustomWakeWord(customInputText)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = config.colorTheme.accentColor),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("btn_save_wakeword")
                            ) {
                                Text("Save", color = Color(0xFF001E2B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        // Test current wake word
                        Button(
                            onClick = { viewModel.testCurrentWakeWord() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x24FFFFFF)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hearing,
                                contentDescription = null,
                                tint = config.colorTheme.accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Calibrate & Verify: \"${config.effectiveWakeWord}\"",
                                color = JarvisTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 3. ASSISTANT FORM & SHAPE GALLERY
            item {
                StudioSectionCard(
                    title = "ASSISTANT SHAPE & FORM",
                    icon = Icons.Default.RoundedCorner,
                    accentColor = config.colorTheme.accentColor
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AssistantShape.values().forEach { shape ->
                            val isSelected = config.shape == shape
                            Surface(
                                onClick = { viewModel.updateShape(shape) },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) config.colorTheme.primaryColor.copy(alpha = 0.3f) else Color(0x1F1E293B),
                                border = BorderStroke(
                                    width = if (isSelected) 1.8.dp else 1.dp,
                                    color = if (isSelected) config.colorTheme.accentColor else Color(0x26FFFFFF)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("shape_card_${shape.name.lowercase()}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = shape.displayName,
                                                color = JarvisTextPrimary,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (isSelected) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "ACTIVE",
                                                    color = config.colorTheme.accentColor,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = shape.description,
                                            color = JarvisTextSecondary,
                                            fontSize = 11.5.sp
                                        )
                                    }

                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) config.colorTheme.accentColor else Color(0x26FFFFFF))
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. COLOR THEME & IRIDESCENCE
            item {
                StudioSectionCard(
                    title = "COLOR AURA & PALETTE",
                    icon = Icons.Default.ColorLens,
                    accentColor = config.colorTheme.accentColor
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AssistantColorTheme.values().forEach { theme ->
                            val isSelected = config.colorTheme == theme
                            Surface(
                                onClick = { viewModel.updateColorTheme(theme) },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) Color(0x381E293B) else Color(0x1F1E293B),
                                border = BorderStroke(
                                    width = if (isSelected) 1.8.dp else 1.dp,
                                    color = if (isSelected) theme.accentColor else Color(0x26FFFFFF)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("theme_${theme.name.lowercase()}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Theme preview swatch
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Brush.linearGradient(theme.gradientColors))
                                                .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = theme.displayName,
                                                color = JarvisTextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = if (isSelected) "Selected Palette" else "Tap to apply",
                                                color = if (isSelected) theme.accentColor else JarvisTextMuted,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = theme.accentColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. SYSTEM-WIDE & HOME SCREEN CONTROLS
            item {
                StudioSectionCard(
                    title = "HOME SCREEN & SYSTEM OVERLAYS",
                    icon = Icons.Default.Layers,
                    accentColor = config.colorTheme.accentColor
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Always Listen for Wake Word in Background
                        StudioSwitchRow(
                            title = "Always-On Background Listener",
                            subtitle = "Listen for '${config.effectiveWakeWord}' while on home screen or outside app",
                            checked = config.wakeWordEnabled,
                            onCheckedChange = { viewModel.toggleAmbientWakeWord(it) }
                        )

                        // Floating Home Screen Orb
                        StudioSwitchRow(
                            title = "Floating Home Screen Orb",
                            subtitle = "Float your chosen assistant shape over other apps",
                            checked = config.floatingBubbleEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.updateFloatingBubble(enabled)
                                if (enabled) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                                        context.startActivity(intent)
                                    } else {
                                        JarvisFloatingOverlayService.start(context)
                                    }
                                } else {
                                    JarvisFloatingOverlayService.stop(context)
                                }
                            }
                        )

                        // Auto listen on open
                        StudioSwitchRow(
                            title = "Auto-Listen on Overlay Launch",
                            subtitle = "Immediately open speech recognizer when assistant pops up",
                            checked = config.autoListenOnOpen,
                            onCheckedChange = { viewModel.updateAutoListen(it) }
                        )

                        // Continuous Conversation
                        StudioSwitchRow(
                            title = "Continuous Hands-Free Conversation",
                            subtitle = "Keep listening after answering for follow-up prompts",
                            checked = config.continuousVoiceConversation,
                            onCheckedChange = { viewModel.updateContinuousConversation(it) }
                        )

                        // Wake Haptic Feedback
                        StudioSwitchRow(
                            title = "Haptic Pulse on Wake",
                            subtitle = "Tactile confirmation when wake word is detected",
                            checked = config.wakeHapticFeedback,
                            onCheckedChange = { viewModel.updateWakeHaptic(it) }
                        )
                    }
                }
            }

            // 6. VOICE SPEED, PITCH & PERSONALITY
            item {
                StudioSectionCard(
                    title = "VOICE ENGINE & PERSONALITY",
                    icon = Icons.Default.Psychology,
                    accentColor = config.colorTheme.accentColor
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Personality Cards
                        Text(
                            text = "Assistant Persona:",
                            color = JarvisTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AssistantPersonality.values().forEach { personality ->
                                val isSelected = config.personality == personality
                                Surface(
                                    onClick = { viewModel.updatePersonality(personality) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) config.colorTheme.primaryColor.copy(alpha = 0.35f) else Color(0x1F1E293B),
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) config.colorTheme.accentColor else Color(0x26FFFFFF)
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
                                    ) {
                                        Text(
                                            text = personality.displayName,
                                            color = if (isSelected) JarvisTextPrimary else JarvisTextSecondary,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Speech Pitch Slider
                        Text(
                            text = "Voice Pitch: ${String.format("%.2f", config.speechPitch)}x",
                            color = JarvisTextSecondary,
                            fontSize = 12.sp
                        )
                        Slider(
                            value = config.speechPitch,
                            onValueChange = { viewModel.updateSpeechPitch(it) },
                            valueRange = 0.6f..1.5f,
                            colors = SliderDefaults.colors(
                                thumbColor = config.colorTheme.accentColor,
                                activeTrackColor = config.colorTheme.accentColor,
                                inactiveTrackColor = Color(0x33FFFFFF)
                            )
                        )

                        // Speech Speed Slider
                        Text(
                            text = "Voice Speed: ${String.format("%.2f", config.speechSpeed)}x",
                            color = JarvisTextSecondary,
                            fontSize = 12.sp
                        )
                        Slider(
                            value = config.speechSpeed,
                            onValueChange = { viewModel.updateSpeechSpeed(it) },
                            valueRange = 0.7f..1.6f,
                            colors = SliderDefaults.colors(
                                thumbColor = config.colorTheme.accentColor,
                                activeTrackColor = config.colorTheme.accentColor,
                                inactiveTrackColor = Color(0x33FFFFFF)
                            )
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun StudioSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color(0xF00F172A),
        border = BorderStroke(1.dp, Color(0x26FFFFFF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = JarvisTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
fun StudioSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = JarvisTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = JarvisTextSecondary,
                fontSize = 11.5.sp,
                lineHeight = 15.sp
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = JarvisGreen,
                uncheckedThumbColor = JarvisTextMuted,
                uncheckedTrackColor = Color(0x33FFFFFF)
            )
        )
    }
}
