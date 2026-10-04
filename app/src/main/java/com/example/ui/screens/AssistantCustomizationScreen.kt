package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.RestartAlt
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
import com.example.data.repository.AssistantLanguage
import com.example.data.repository.AssistantPersonality
import com.example.data.repository.PillAnimationStyle
import com.example.data.repository.PillGlowLevel
import com.example.data.repository.PillPositionMode
import com.example.data.repository.PillSizeOption
import com.example.data.repository.WAKE_WORD_PRESETS
import com.example.ui.JarvisViewModel
import com.example.ui.components.JarvisState
import com.example.ui.components.SiriPillVisualizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AssistantCustomizationScreen(
    viewModel: JarvisViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToDiagnostics: () -> Unit = {},
    onNavigateToMemory: () -> Unit = {}
) {
    val config by viewModel.appearanceConfig.collectAsStateWithLifecycle()
    val jarvisState by viewModel.jarvisState.collectAsStateWithLifecycle()
    val audioLevel by viewModel.rmsAudioLevel.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val availableVoices by viewModel.availableTtsVoices.collectAsStateWithLifecycle()

    var customInputText by remember(config.customWakeWord) { mutableStateOf(config.customWakeWord) }

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
            .testTag("settings_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top App Bar with Back Button
        Surface(
            color = Color(0xDD11192C),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            border = BorderStroke(1.dp, Color(0x22FFFFFF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 720.dp)
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("settings_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Assistant",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "ASSISTANT SETTINGS",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Pill UI, Voice, Language & Behavior",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.resetToDefaults() },
                        modifier = Modifier.testTag("reset_defaults_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Settings",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(6.dp)) }

            // 1. LIVE PILL INTERACTIVE PREVIEW
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x550F172A)),
                    border = BorderStroke(1.dp, config.colorTheme.primaryColor.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "LIVE PILL PREVIEW",
                            color = config.colorTheme.accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        SiriPillVisualizer(
                            state = jarvisState,
                            config = config,
                            audioLevel = audioLevel,
                            onClick = { viewModel.onPillClicked() },
                            onLongClick = { viewModel.onPillLongClicked() }
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tap to test listening / speaking states",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // CORE JARVIS SUBSYSTEMS QUICK JUMP
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0x331E293B)),
                        border = BorderStroke(1.dp, Color(0x3338BDF8)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToDiagnostics() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Diagnostics",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Telemetry", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Live Sensors", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            }
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0x331E293B)),
                        border = BorderStroke(1.dp, Color(0x33A855F7)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToMemory() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = "Memory Matrix",
                                tint = Color(0xFFA855F7),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Memory Matrix", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Notes & Agenda", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            // 2. PILL POSITIONING SYSTEM (EXTREMELY IMPORTANT)
            item {
                SectionHeader(title = "PILL POSITION & DRAGGING", icon = Icons.Default.OpenWith)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x331E293B)),
                    border = BorderStroke(1.dp, Color(0x22FFFFFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Screen Position Anchor",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Choose a preset or freely touch & drag the pill anywhere on the main screen.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            PillPositionMode.values().forEach { mode ->
                                val isSelected = config.pillPositionMode == mode
                                PositionChip(
                                    mode = mode,
                                    isSelected = isSelected,
                                    onClick = { viewModel.setPillPositionMode(mode) }
                                )
                            }
                        }

                        if (config.pillPositionMode == PillPositionMode.CUSTOM) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Custom Coordinates: (${(config.customOffsetXPercent * 100).toInt()}%, ${(config.customOffsetYPercent * 100).toInt()}%)",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp
                                )
                                Button(
                                    onClick = { viewModel.updateCustomPillOffset(0.5f, 0.85f) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x3338BDF8)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Reset Center", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // 3. PILL CUSTOMIZATION & GEOMETRY
            item {
                SectionHeader(title = "PILL VISUAL CRAFT & GLOW", icon = Icons.Default.Tune)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x331E293B)),
                    border = BorderStroke(1.dp, Color(0x22FFFFFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Pill Size
                        Column {
                            Text("Pill Size", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                PillSizeOption.values().forEach { sizeOpt ->
                                    val isSelected = config.pillSizeOption == sizeOpt
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) config.colorTheme.primaryColor else Color(0x22FFFFFF))
                                            .clickable { viewModel.setPillSizeOption(sizeOpt) }
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = sizeOpt.displayName,
                                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }

                            if (config.pillSizeOption == PillSizeOption.CUSTOM) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Scale Factor: ${String.format(Locale.getDefault(), "%.2f", config.pillScale)}x",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                                Slider(
                                    value = config.pillScale,
                                    onValueChange = { viewModel.setPillScale(it) },
                                    valueRange = 0.65f..1.45f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = config.colorTheme.accentColor,
                                        activeTrackColor = config.colorTheme.primaryColor
                                    )
                                )
                            }
                        }

                        // Pill Opacity
                        Column {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Pill Opacity", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("${(config.pillOpacity * 100).toInt()}%", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                            Slider(
                                value = config.pillOpacity,
                                onValueChange = { viewModel.setPillOpacity(it) },
                                valueRange = 0.3f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = config.colorTheme.accentColor,
                                    activeTrackColor = config.colorTheme.primaryColor
                                )
                            )
                        }

                        // Glow Level
                        Column {
                            Text("Glow Intensity", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                PillGlowLevel.values().forEach { glow ->
                                    val isSelected = config.pillGlowLevel == glow
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) config.colorTheme.primaryColor else Color(0x22FFFFFF))
                                            .clickable { viewModel.setPillGlowLevel(glow) }
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = glow.displayName,
                                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        // Animation Style
                        Column {
                            Text("Animation Dynamics", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                PillAnimationStyle.values().forEach { anim ->
                                    val isSelected = config.pillAnimationStyle == anim
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) config.colorTheme.primaryColor else Color(0x22FFFFFF))
                                            .clickable { viewModel.setPillAnimationStyle(anim) }
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = anim.displayName,
                                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        // Optional Subtle Transcription Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Temporary Subtitle Transcription", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Shows a brief floating caption while speaking (Off by default for pure voice-out).", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                            Switch(
                                checked = config.showSubtleTranscription,
                                onCheckedChange = { viewModel.setShowSubtleTranscription(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = config.colorTheme.accentColor)
                            )
                        }
                    }
                }
            }

            // 4. COLOR THEME
            item {
                SectionHeader(title = "COLOR THEME & AURA", icon = Icons.Default.Palette)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x331E293B)),
                    border = BorderStroke(1.dp, Color(0x22FFFFFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AssistantColorTheme.values().forEach { theme ->
                            val isSelected = config.colorTheme == theme
                            ThemeCardItem(
                                theme = theme,
                                isSelected = isSelected,
                                onClick = { viewModel.setColorTheme(theme) }
                            )
                        }
                    }
                }
            }

            // 5. VOICE & MULTILINGUAL SYSTEM
            item {
                SectionHeader(title = "VOICE & MULTILINGUAL ENGINE", icon = Icons.Default.RecordVoiceOver)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x331E293B)),
                    border = BorderStroke(1.dp, Color(0x22FFFFFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Assistant Primary Language",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        AssistantLanguage.values().forEach { lang ->
                            val isSelected = config.voiceLanguage == lang
                            LanguageOptionCard(
                                language = lang,
                                isSelected = isSelected,
                                onSelect = { viewModel.updateVoiceLanguage(lang) },
                                onTest = { viewModel.testVoiceLanguage(lang) }
                            )
                        }

                        // Installed Voice Selection
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Select Voice (${availableVoices.size} available)",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (config.selectedTtsVoiceName.isNotBlank()) {
                                    Text(
                                        text = "Custom",
                                        color = config.colorTheme.accentColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            val prefix = when (config.voiceLanguage) {
                                AssistantLanguage.HINDI -> "hi"
                                AssistantLanguage.HINGLISH -> "en-in"
                                AssistantLanguage.ENGLISH -> "en"
                            }
                            val matchingVoices = availableVoices.filter {
                                it.localeTag.lowercase().startsWith(prefix)
                            }
                            val displayVoices = if (matchingVoices.isNotEmpty()) matchingVoices else availableVoices.take(8)

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val isAutoSelected = config.selectedTtsVoiceName.isBlank()
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isAutoSelected) config.colorTheme.primaryColor else Color(0x22FFFFFF))
                                        .clickable { viewModel.selectTtsVoice("") }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "System Default",
                                        color = if (isAutoSelected) Color.White else Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        fontWeight = if (isAutoSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }

                                displayVoices.forEach { voice ->
                                    val isSelected = config.selectedTtsVoiceName == voice.name
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) config.colorTheme.primaryColor else Color(0x22FFFFFF))
                                            .clickable { viewModel.selectTtsVoice(voice.name) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = voice.displayName,
                                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Test voice",
                                            tint = if (isSelected) Color.White else Color(0xFF64748B),
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable { viewModel.testSelectedVoice(voice.name) }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Speech Speed
                        Column {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Speech Speed", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("${String.format(Locale.getDefault(), "%.1f", config.speechSpeed)}x", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                            Slider(
                                value = config.speechSpeed,
                                onValueChange = { viewModel.updateSpeechSpeed(it) },
                                valueRange = 0.7f..1.5f,
                                colors = SliderDefaults.colors(
                                    thumbColor = config.colorTheme.accentColor,
                                    activeTrackColor = config.colorTheme.primaryColor
                                )
                            )
                        }

                        // Speech Pitch
                        Column {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Speech Pitch", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("${String.format(Locale.getDefault(), "%.1f", config.speechPitch)}x", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                            Slider(
                                value = config.speechPitch,
                                onValueChange = { viewModel.updateSpeechPitch(it) },
                                valueRange = 0.7f..1.5f,
                                colors = SliderDefaults.colors(
                                    thumbColor = config.colorTheme.accentColor,
                                    activeTrackColor = config.colorTheme.primaryColor
                                )
                            )
                        }

                        // Continuous Voice Mode Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Continuous Voice Conversation", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Automatically re-opens microphone after speech finishes for fluid back-and-forth dialogue.", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                            Switch(
                                checked = config.continuousVoiceConversation,
                                onCheckedChange = { viewModel.updateContinuousConversation(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = config.colorTheme.accentColor)
                            )
                        }

                        // Wake Word Configuration
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Hands-Free Wake Word", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Activate hands-free by speaking '${config.effectiveWakeWord}'.", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                            Switch(
                                checked = config.wakeWordEnabled,
                                onCheckedChange = { viewModel.toggleAmbientWakeWord(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = config.colorTheme.accentColor)
                            )
                        }

                        if (config.wakeWordEnabled) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                WAKE_WORD_PRESETS.forEach { preset ->
                                    val isSelected = config.wakeWordPreset == preset
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) config.colorTheme.primaryColor else Color(0x22FFFFFF))
                                            .clickable { viewModel.updateSelectedWakeWord(preset) }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = preset,
                                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            if (config.wakeWordPreset == "Custom") {
                                OutlinedTextField(
                                    value = customInputText,
                                    onValueChange = {
                                        customInputText = it
                                        viewModel.updateCustomWakeWord(it)
                                    },
                                    label = { Text("Custom Wake Phrase") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = config.colorTheme.accentColor,
                                        unfocusedBorderColor = Color(0x44FFFFFF),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // 6. VOICE INTERACTION LOGS & HISTORY
            item {
                SectionHeader(title = "VOICE LOGS & DIAGNOSTICS", icon = Icons.Default.DeleteSweep)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x331E293B)),
                    border = BorderStroke(1.dp, Color(0x22FFFFFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Logged Voice Commands (${messages.size})",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Button(
                                onClick = { viewModel.clearConversation() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FF4757)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Clear Data", color = Color(0xFFFF6B81), fontSize = 11.sp)
                            }
                        }

                        if (messages.isEmpty()) {
                            Text("No voice interactions logged yet.", color = Color(0xFF64748B), fontSize = 11.sp)
                        } else {
                            messages.takeLast(5).reversed().forEach { msg ->
                                val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(msg.timestamp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x18FFFFFF))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = if (msg.sender == "USER") "Voice Input" else "JARVIS Spoken",
                                                color = if (msg.sender == "USER") Color(0xFF38BDF8) else config.colorTheme.accentColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(text = timeStr, color = Color(0xFF64748B), fontSize = 9.sp)
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = msg.text, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

// -------------------------------------------------------------
// Component Helpers
// -------------------------------------------------------------

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF38BDF8),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.1.sp
        )
    }
}

@Composable
private fun PositionChip(
    mode: PillPositionMode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFF38BDF8) else Color(0x1AFFFFFF))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = mode.displayName,
            color = if (isSelected) Color(0xFF0F172A) else Color(0xFFE2E8F0),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ThemeCardItem(
    theme: AssistantColorTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) theme.primaryColor.copy(alpha = 0.25f) else Color(0x11FFFFFF))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) theme.accentColor else Color(0x18FFFFFF),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(theme.gradientColors)
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = theme.displayName,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = theme.accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun LanguageOptionCard(
    language: AssistantLanguage,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onTest: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0x2A38BDF8) else Color(0x11FFFFFF))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) Color(0xFF38BDF8) else Color(0x18FFFFFF),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onSelect)
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = language.nativeLabel,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                if (isSelected) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF38BDF8))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Active", color = Color(0xFF0F172A), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(
                text = language.subtitle,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
        }

        IconButton(
            onClick = onTest,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0x2238BDF8))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Test Voice Sample",
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
