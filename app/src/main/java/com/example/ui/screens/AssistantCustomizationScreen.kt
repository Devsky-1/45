package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.AssistantAppearanceConfig
import com.example.data.repository.AssistantColorTheme
import com.example.data.repository.AssistantPersonality
import com.example.data.repository.AssistantShape
import com.example.data.repository.WAKE_WORD_PRESETS
import com.example.ui.JarvisViewModel
import com.example.ui.components.AssistantShapeContainer
import com.example.ui.components.HoloCard
import com.example.ui.components.JarvisState
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCardBg
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCardBorderCyan
import com.example.ui.theme.JarvisCardGlass
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisObsidian
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisSpaceDark
import com.example.ui.theme.JarvisSpaceMid
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

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
            .testTag("customization_screen")
    ) {
        // TOP HEADER
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
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Studio",
                        tint = config.colorTheme.accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ASSISTANT STUDIO",
                            color = JarvisTextPrimary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "WAKE WORD, SHAPE & PERSONALITY",
                            color = JarvisTextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Active Wake Word Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (config.wakeWordEnabled) JarvisCyan.copy(alpha = 0.15f) else JarvisObsidian,
                    border = BorderStroke(1.dp, if (config.wakeWordEnabled) JarvisCyanLight.copy(alpha = 0.6f) else JarvisCardBorder)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (config.wakeWordEnabled && isAmbientListening) JarvisGreen else if (config.wakeWordEnabled) JarvisAmber else JarvisRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "\"${config.effectiveWakeWord}\"",
                            color = if (config.wakeWordEnabled) JarvisCyanLight else JarvisTextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

            // 1. LIVE INTERACTIVE PREVIEW
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = JarvisCardGlass),
                    border = BorderStroke(1.5.dp, Brush.horizontalGradient(config.colorTheme.gradientColors)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp, horizontal = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "LIVE ASSISTANT PREVIEW",
                                color = config.colorTheme.accentColor,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = config.colorTheme.primaryColor.copy(alpha = 0.2f),
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Text(
                                    text = "TAP OR SPEAK TO TEST",
                                    color = config.colorTheme.accentColor,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Interactive Assistant Shape Container
                        AssistantShapeContainer(
                            state = jarvisState,
                            config = config,
                            audioLevel = audioLevel,
                            onClick = { viewModel.toggleVoiceRecognition() },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${config.shape.displayName} • ${config.colorTheme.displayName}",
                            color = JarvisTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )

                        Text(
                            text = if (config.wakeWordEnabled) "Wake word: \"${config.effectiveWakeWord}\" is active" else "Wake word detection disabled",
                            color = if (config.wakeWordEnabled) JarvisCyanLight else JarvisTextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // 2. WAKE WORD CONFIGURATION COMMAND CENTER
            item {
                HoloCard(
                    borderColor = if (config.wakeWordEnabled) JarvisCyan.copy(alpha = 0.6f) else JarvisCardBorder,
                    glowEffect = config.wakeWordEnabled
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Sensors,
                                    contentDescription = "Wake Word",
                                    tint = if (config.wakeWordEnabled) JarvisCyan else JarvisTextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "WAKE WORD DETECTION",
                                        color = JarvisTextPrimary,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (config.wakeWordEnabled) "Listening for \"${config.effectiveWakeWord}\"" else "Hands-free voice trigger paused",
                                        color = if (config.wakeWordEnabled) JarvisGreen else JarvisTextMuted,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Switch(
                                checked = config.wakeWordEnabled,
                                onCheckedChange = { viewModel.setWakeWordEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = JarvisCyan
                                ),
                                modifier = Modifier.testTag("switch_wake_word_master")
                            )
                        }

                        AnimatedVisibility(
                            visible = config.wakeWordEnabled,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Column(modifier = Modifier.padding(top = 14.dp)) {
                                Text(
                                    text = "SELECT WAKE WORD PRESET",
                                    color = JarvisCyanLight,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Wake word preset chips
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(WAKE_WORD_PRESETS) { preset ->
                                        val isSelected = config.wakeWordPreset.equals(preset, ignoreCase = true)
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (isSelected) JarvisCyan.copy(alpha = 0.25f) else JarvisObsidian,
                                            border = BorderStroke(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) JarvisCyan else JarvisCardBorder
                                            ),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .clickable {
                                                    viewModel.setWakeWordPreset(preset)
                                                }
                                                .testTag("chip_wakeword_${preset.replace(" ", "_")}")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = JarvisCyan,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                Text(
                                                    text = preset,
                                                    color = if (isSelected) JarvisCyanLight else JarvisTextSecondary,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Custom Wake Word Input when "Custom" is selected
                                if (config.wakeWordPreset.equals("Custom", ignoreCase = true)) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "ENTER CUSTOM WAKE WORD",
                                            color = JarvisAmber,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            OutlinedTextField(
                                                value = customInputText,
                                                onValueChange = { customInputText = it },
                                                placeholder = { Text("e.g. Jarvis, Friday, Ultron...", color = JarvisTextMuted, fontSize = 12.sp) },
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = JarvisCyan,
                                                    unfocusedBorderColor = JarvisCardBorder,
                                                    focusedTextColor = JarvisTextPrimary,
                                                    unfocusedTextColor = JarvisTextPrimary,
                                                    focusedContainerColor = Color(0xFF070D1A),
                                                    unfocusedContainerColor = Color(0xFF070D1A)
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("input_custom_wakeword")
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(
                                                onClick = {
                                                    viewModel.setCustomWakeWord(customInputText)
                                                    viewModel.testWakeWord(customInputText)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.testTag("btn_save_custom_wakeword")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Save,
                                                    contentDescription = "Save",
                                                    tint = Color(0xFF001F24),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Apply", color = Color(0xFF001F24), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Test Wake Word Button
                                Button(
                                    onClick = { viewModel.testWakeWord() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = JarvisCardBg
                                    ),
                                    border = BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_test_wake_word")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Test",
                                        tint = JarvisCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Test Wake Word: \"${config.effectiveWakeWord}\"",
                                        color = JarvisCyanLight,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Wake Cue Options: Haptics & Chime
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Vibration,
                                            contentDescription = "Haptics",
                                            tint = JarvisTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Haptic Pulse on Wake", color = JarvisTextSecondary, fontSize = 11.sp)
                                    }
                                    Switch(
                                        checked = config.wakeHapticFeedback,
                                        onCheckedChange = { viewModel.setWakeHapticFeedback(it) },
                                        colors = SwitchDefaults.colors(checkedTrackColor = JarvisCyan)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Chime",
                                            tint = JarvisTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Audio Beep/Chime on Wake", color = JarvisTextSecondary, fontSize = 11.sp)
                                    }
                                    Switch(
                                        checked = config.wakeChimeSound,
                                        onCheckedChange = { viewModel.setWakeChimeSound(it) },
                                        colors = SwitchDefaults.colors(checkedTrackColor = JarvisCyan)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. SHAPE & CONTAINER SELECTION
            item {
                HoloCard(borderColor = config.colorTheme.primaryColor.copy(alpha = 0.5f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RoundedCorner,
                                contentDescription = "Shape",
                                tint = config.colorTheme.accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ASSISTANT SHAPE & FORM",
                                color = JarvisTextPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        AssistantShape.entries.forEach { shapeOption ->
                            val isSelected = config.shape == shapeOption
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) JarvisCardBg else JarvisObsidian.copy(alpha = 0.6f)
                                ),
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) config.colorTheme.accentColor else JarvisCardBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable {
                                        viewModel.setAssistantShape(shapeOption)
                                    }
                                    .testTag("shape_${shapeOption.name}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = shapeOption.displayName,
                                            color = if (isSelected) config.colorTheme.accentColor else JarvisTextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = shapeOption.description,
                                            color = JarvisTextMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = config.colorTheme.accentColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. COLOR PALETTES & IRIDESCENCE
            item {
                HoloCard(borderColor = config.colorTheme.primaryColor.copy(alpha = 0.5f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = "Colors",
                                tint = config.colorTheme.accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GLOW COLOR THEME",
                                color = JarvisTextPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            AssistantColorTheme.entries.forEach { themeOption ->
                                val isSelected = config.colorTheme == themeOption
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable {
                                            viewModel.setAssistantColorTheme(themeOption)
                                        }
                                        .padding(4.dp)
                                        .testTag("color_theme_${themeOption.name}")
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.sweepGradient(themeOption.gradientColors)
                                            )
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) Color.White else Color.Transparent,
                                                shape = CircleShape
                                            )
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = themeOption.displayName.split(" ").first(),
                                        color = if (isSelected) themeOption.accentColor else JarvisTextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. PERSONALITY INTELLIGENCE PROFILES
            item {
                HoloCard(borderColor = config.colorTheme.primaryColor.copy(alpha = 0.5f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Personality",
                                tint = config.colorTheme.accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ASSISTANT INTELLIGENCE PROFILE",
                                color = JarvisTextPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        AssistantPersonality.entries.forEach { persona ->
                            val isSelected = config.personality == persona
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) JarvisCardBg else JarvisObsidian.copy(alpha = 0.6f)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) config.colorTheme.accentColor else JarvisCardBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable {
                                        viewModel.setAssistantPersonality(persona)
                                    }
                                    .testTag("personality_${persona.name}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = persona.displayName,
                                            color = if (isSelected) config.colorTheme.accentColor else JarvisTextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = persona.promptPrefix,
                                            color = JarvisTextMuted,
                                            fontSize = 10.sp,
                                            maxLines = 2
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = config.colorTheme.accentColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. GLOW, SCALE, AND SPEECH SLIDERS
            item {
                HoloCard(borderColor = config.colorTheme.primaryColor.copy(alpha = 0.5f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Tuning",
                                tint = config.colorTheme.accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "FINE TUNING & SIZING",
                                color = JarvisTextPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Glow intensity
                        Text(
                            text = "Glow Intensity: ${(config.glowIntensity * 100).toInt()}%",
                            color = JarvisTextSecondary,
                            fontSize = 11.sp
                        )
                        Slider(
                            value = config.glowIntensity,
                            onValueChange = { viewModel.setGlowIntensity(it) },
                            valueRange = 0.5f..1.5f,
                            colors = SliderDefaults.colors(
                                thumbColor = config.colorTheme.accentColor,
                                activeTrackColor = config.colorTheme.primaryColor,
                                inactiveTrackColor = JarvisCardBorder
                            )
                        )

                        // Orb scale
                        Text(
                            text = "Visual Scale Size: ${(config.orbScale * 100).toInt()}%",
                            color = JarvisTextSecondary,
                            fontSize = 11.sp
                        )
                        Slider(
                            value = config.orbScale,
                            onValueChange = { viewModel.setOrbScale(it) },
                            valueRange = 0.8f..1.3f,
                            colors = SliderDefaults.colors(
                                thumbColor = config.colorTheme.accentColor,
                                activeTrackColor = config.colorTheme.primaryColor,
                                inactiveTrackColor = JarvisCardBorder
                            )
                        )

                        // Speech Speed
                        Text(
                            text = "Voice Speed: ${(config.speechSpeed * 100).toInt()}%",
                            color = JarvisTextSecondary,
                            fontSize = 11.sp
                        )
                        Slider(
                            value = config.speechSpeed,
                            onValueChange = { viewModel.setSpeechSpeed(it) },
                            valueRange = 0.7f..1.4f,
                            colors = SliderDefaults.colors(
                                thumbColor = config.colorTheme.accentColor,
                                activeTrackColor = config.colorTheme.primaryColor,
                                inactiveTrackColor = JarvisCardBorder
                            )
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
