package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisPurple
import com.example.ui.theme.JarvisRed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PillPositionMode(val displayName: String, val description: String) {
    BOTTOM("Bottom", "Docked elegantly at the bottom edge"),
    TOP("Top", "Floating gracefully near the top status area"),
    CENTER("Center", "Anchored dead-center on screen"),
    LEFT("Left", "Vertical alignment along the left edge"),
    RIGHT("Right", "Vertical alignment along the right edge"),
    CUSTOM("Custom (Drag Anywhere)", "Touch and drag the pill anywhere freely")
}

enum class PillSizeOption(val displayName: String, val scaleFactor: Float, val defaultWidthDp: Int, val defaultHeightDp: Int) {
    SMALL("Small", 0.82f, 175, 46),
    MEDIUM("Medium", 1.0f, 220, 54),
    LARGE("Large", 1.22f, 265, 62),
    CUSTOM("Custom Slider", 1.0f, 220, 54)
}

enum class PillGlowLevel(val displayName: String, val multiplier: Float) {
    OFF("Off", 0.0f),
    LOW("Low", 0.4f),
    MEDIUM("Medium", 0.85f),
    HIGH("High", 1.4f)
}

enum class PillAnimationStyle(val displayName: String, val speedMultiplier: Float) {
    OFF("Off", 0.0f),
    MINIMAL("Minimal", 0.5f),
    NORMAL("Normal", 1.0f),
    DYNAMIC("Dynamic", 1.4f)
}

enum class AssistantShape(val displayName: String, val description: String) {
    SIRI_ORB("Luminous Siri Orb", "Iridescent spherical fluid energy core with ambient glow"),
    CURVED_PILL("Curved Capsule Pill", "Sleek rounded rectangle with dynamic waveform equalizer"),
    ARC_REACTOR("Quantum Arc Reactor", "Iron Man holographic core with rotating techno-rings"),
    WAVEFORM_RIBBON("Fluid Wave Ribbon", "Horizontal audio waveform bar with soft rounded edges"),
    MINIMAL_BUBBLE("Minimalist Orb", "Clean, compact floating pearl with subtle breathing pulse")
}

enum class AssistantColorTheme(
    val displayName: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val gradientColors: List<Color>
) {
    SIRI_IRIDESCENT(
        displayName = "Siri Multi-Gaze",
        primaryColor = Color(0xFF6C5CE7),
        secondaryColor = Color(0xFF00CEC9),
        accentColor = Color(0xFFFF7675),
        gradientColors = listOf(
            Color(0xFF6C5CE7),
            Color(0xFF00CEC9),
            Color(0xFFFD79A8),
            Color(0xFF0984E3)
        )
    ),
    CYBER_CYAN(
        displayName = "Holographic Cyan",
        primaryColor = JarvisCyan,
        secondaryColor = Color(0xFF0083B0),
        accentColor = Color(0xFF7CF4FF),
        gradientColors = listOf(
            JarvisCyan,
            Color(0xFF00B4DB),
            Color(0xFF0083B0)
        )
    ),
    TITANIUM_SILVER(
        displayName = "Titanium Silver (Pro)",
        primaryColor = Color(0xFFE2E8F0),
        secondaryColor = Color(0xFF94A3B8),
        accentColor = Color(0xFFFFFFFF),
        gradientColors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFCBD5E1),
            Color(0xFF64748B)
        )
    ),
    STARK_GOLD(
        displayName = "Solar Gold & Amber",
        primaryColor = Color(0xFFFFD54F),
        secondaryColor = JarvisAmber,
        accentColor = Color(0xFFFFE082),
        gradientColors = listOf(
            Color(0xFFFFD54F),
            JarvisAmber,
            Color(0xFFFF6D00)
        )
    ),
    NEON_EMERALD(
        displayName = "Cyber Emerald",
        primaryColor = JarvisGreen,
        secondaryColor = Color(0xFF00B894),
        accentColor = Color(0xFF55EFC4),
        gradientColors = listOf(
            JarvisGreen,
            Color(0xFF00B894),
            Color(0xFF55EFC4)
        )
    ),
    ROYAL_AMETHYST(
        displayName = "Royal Amethyst",
        primaryColor = JarvisPurple,
        secondaryColor = Color(0xFF8E44AD),
        accentColor = Color(0xFFE056FD),
        gradientColors = listOf(
            JarvisPurple,
            Color(0xFF8E44AD),
            Color(0xFFE056FD)
        )
    ),
    CRIMSON_RUBY(
        displayName = "Crimson Protocol",
        primaryColor = JarvisRed,
        secondaryColor = Color(0xFFD63031),
        accentColor = Color(0xFFFF7675),
        gradientColors = listOf(
            JarvisRed,
            Color(0xFFD63031),
            Color(0xFFFF7675)
        )
    ),
    AMOLED_DARK(
        displayName = "AMOLED Obsidian",
        primaryColor = Color(0xFF1E293B),
        secondaryColor = Color(0xFF0F172A),
        accentColor = Color(0xFF38BDF8),
        gradientColors = listOf(
            Color(0xFF38BDF8),
            Color(0xFF1E293B),
            Color(0xFF0F172A)
        )
    )
}

enum class AssistantPersonality(val displayName: String, val promptPrefix: String) {
    PRO_EXECUTIVE(
        "Professional Executive",
        "You are an ultra-capable, polite, highly polished executive voice assistant. Be concise, precise, helpful, and professional."
    ),
    JARVIS_AI(
        "J.A.R.V.I.S. Protocol",
        "You are J.A.R.V.I.S., the hyper-intelligent British AI assistant. Address the user respectfully as 'Sir' or 'Ma'am', witty yet deeply sophisticated and tactical."
    ),
    SIRI_PRO(
        "Siri Intelligent Assistant",
        "You are an intuitive, natural, friendly, and rapid AI assistant. Give helpful, warm, and natural conversational answers."
    )
}

enum class AssistantLanguage(
    val id: String,
    val displayName: String,
    val nativeLabel: String,
    val subtitle: String,
    val localeTag: String,
    val samplePhrase: String
) {
    ENGLISH(
        id = "en",
        displayName = "English",
        nativeLabel = "English (Global)",
        subtitle = "Refined aristocratic British & Global English",
        localeTag = "en-US",
        samplePhrase = "Greetings, sir. All core diagnostics report optimal operational efficiency. What is our objective today?"
    ),
    HINDI(
        id = "hi",
        displayName = "हिन्दी",
        nativeLabel = "हिन्दी (Hindi)",
        subtitle = "शुद्ध, विनम्र और प्राकृतिक हिन्दी संवाद",
        localeTag = "hi-IN",
        samplePhrase = "नमस्ते सर! जार्विस प्रणाली सक्रिय है। मैं आपकी क्या सहायता कर सकता हूँ?"
    ),
    HINGLISH(
        id = "hinglish",
        displayName = "Hinglish",
        nativeLabel = "Hinglish (हिंग्लिश)",
        subtitle = "Natural conversational Indian Hindi-English fusion",
        localeTag = "en-IN",
        samplePhrase = "Haan ji Sir! J.A.R.V.I.S. bilkul ready hai. Sabhi systems mast chal rahe hain, boliye kya help karoon?"
    )
}

val WAKE_WORD_PRESETS = listOf(
    "Hey Jarvis",
    "Jarvis",
    "सुनो जार्विस",
    "Suno Jarvis",
    "नमस्ते जार्विस",
    "Namaste Jarvis",
    "Jarvis Bhai",
    "Hey Siri",
    "Computer",
    "Friday",
    "Edith",
    "Hey Assistant",
    "Custom"
)

data class AssistantAppearanceConfig(
    val shape: AssistantShape = AssistantShape.CURVED_PILL,
    val colorTheme: AssistantColorTheme = AssistantColorTheme.SIRI_IRIDESCENT,
    val personality: AssistantPersonality = AssistantPersonality.JARVIS_AI,
    val voiceLanguage: AssistantLanguage = AssistantLanguage.ENGLISH,
    val pillPositionMode: PillPositionMode = PillPositionMode.BOTTOM,
    val customOffsetXPercent: Float = 0.5f, // 0.0 to 1.0 (relative to available width)
    val customOffsetYPercent: Float = 0.85f, // 0.0 to 1.0 (relative to available height)
    val pillSizeOption: PillSizeOption = PillSizeOption.MEDIUM,
    val pillScale: Float = 1.0f,
    val pillOpacity: Float = 0.95f,
    val pillGlowLevel: PillGlowLevel = PillGlowLevel.HIGH,
    val pillAnimationStyle: PillAnimationStyle = PillAnimationStyle.DYNAMIC,
    val showSubtleTranscription: Boolean = false,
    val selectedTtsVoiceName: String = "",
    val autoListenOnOpen: Boolean = true,
    val continuousVoiceConversation: Boolean = true,
    val wakeWordEnabled: Boolean = true,
    val wakeWordPreset: String = "Hey Jarvis",
    val customWakeWord: String = "Jarvis",
    val floatingBubbleEnabled: Boolean = false,
    val wakeHapticFeedback: Boolean = true,
    val wakeChimeSound: Boolean = true,
    val glowIntensity: Float = 1.0f, // 0.5f to 1.5f
    val orbScale: Float = 1.0f, // 0.8f to 1.3f
    val speechSpeed: Float = 1.0f, // 0.7f to 1.5f
    val speechPitch: Float = 1.0f // 0.7f to 1.5f
) {
    val selectedWakeWord: String
        get() = wakeWordPreset

    val effectiveWakeWord: String
        get() = if (wakeWordPreset.equals("Custom", ignoreCase = true)) {
            customWakeWord.trim().ifBlank { "Jarvis" }
        } else {
            wakeWordPreset
        }

    val effectiveScale: Float
        get() = if (pillSizeOption == PillSizeOption.CUSTOM) pillScale else pillSizeOption.scaleFactor
}

class AssistantPreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("jarvis_appearance_prefs", Context.MODE_PRIVATE)

    private val _configFlow = MutableStateFlow(loadConfig())
    val configFlow: StateFlow<AssistantAppearanceConfig> = _configFlow.asStateFlow()

    private fun loadConfig(): AssistantAppearanceConfig {
        val shapeName = prefs.getString("shape", AssistantShape.CURVED_PILL.name) ?: AssistantShape.CURVED_PILL.name
        val colorName = prefs.getString("color_theme", AssistantColorTheme.SIRI_IRIDESCENT.name)
            ?: AssistantColorTheme.SIRI_IRIDESCENT.name
        val personalityName = prefs.getString("personality", AssistantPersonality.JARVIS_AI.name)
            ?: AssistantPersonality.JARVIS_AI.name
        val languageName = prefs.getString("voice_language", AssistantLanguage.ENGLISH.name)
            ?: AssistantLanguage.ENGLISH.name
        val positionModeName = prefs.getString("pill_position_mode", PillPositionMode.BOTTOM.name)
            ?: PillPositionMode.BOTTOM.name
        val customX = prefs.getFloat("custom_offset_x_pct", 0.5f)
        val customY = prefs.getFloat("custom_offset_y_pct", 0.85f)
        val sizeOptionName = prefs.getString("pill_size_option", PillSizeOption.MEDIUM.name)
            ?: PillSizeOption.MEDIUM.name
        val pillScale = prefs.getFloat("pill_scale", 1.0f)
        val pillOpacity = prefs.getFloat("pill_opacity", 0.95f)
        val glowLevelName = prefs.getString("pill_glow_level", PillGlowLevel.HIGH.name)
            ?: PillGlowLevel.HIGH.name
        val animStyleName = prefs.getString("pill_anim_style", PillAnimationStyle.DYNAMIC.name)
            ?: PillAnimationStyle.DYNAMIC.name
        val showTrans = prefs.getBoolean("show_subtle_transcription", false)
        val voiceName = prefs.getString("selected_tts_voice", "") ?: ""

        val autoListen = prefs.getBoolean("auto_listen", true)
        val continuousVoice = prefs.getBoolean("continuous_voice", true)
        val wakeEnabled = prefs.getBoolean("wake_word_enabled", true)
        val wakePreset = prefs.getString("wake_word_preset", "Hey Jarvis") ?: "Hey Jarvis"
        val customWake = prefs.getString("custom_wake_word", "Jarvis") ?: "Jarvis"
        val floatingBubble = prefs.getBoolean("floating_bubble", false)
        val wakeHaptics = prefs.getBoolean("wake_haptic_feedback", true)
        val wakeChime = prefs.getBoolean("wake_chime_sound", true)
        val glow = prefs.getFloat("glow_intensity", 1.0f)
        val scale = prefs.getFloat("orb_scale", 1.0f)
        val speed = prefs.getFloat("speech_speed", 1.0f)
        val pitch = prefs.getFloat("speech_pitch", 1.0f)

        val shape = runCatching { AssistantShape.valueOf(shapeName) }.getOrDefault(AssistantShape.CURVED_PILL)
        val color = runCatching { AssistantColorTheme.valueOf(colorName) }.getOrDefault(AssistantColorTheme.SIRI_IRIDESCENT)
        val personality = runCatching { AssistantPersonality.valueOf(personalityName) }.getOrDefault(AssistantPersonality.JARVIS_AI)
        val language = runCatching { AssistantLanguage.valueOf(languageName) }.getOrDefault(AssistantLanguage.ENGLISH)
        val positionMode = runCatching { PillPositionMode.valueOf(positionModeName) }.getOrDefault(PillPositionMode.BOTTOM)
        val sizeOption = runCatching { PillSizeOption.valueOf(sizeOptionName) }.getOrDefault(PillSizeOption.MEDIUM)
        val glowLevel = runCatching { PillGlowLevel.valueOf(glowLevelName) }.getOrDefault(PillGlowLevel.HIGH)
        val animStyle = runCatching { PillAnimationStyle.valueOf(animStyleName) }.getOrDefault(PillAnimationStyle.DYNAMIC)

        return AssistantAppearanceConfig(
            shape = shape,
            colorTheme = color,
            personality = personality,
            voiceLanguage = language,
            pillPositionMode = positionMode,
            customOffsetXPercent = customX,
            customOffsetYPercent = customY,
            pillSizeOption = sizeOption,
            pillScale = pillScale,
            pillOpacity = pillOpacity,
            pillGlowLevel = glowLevel,
            pillAnimationStyle = animStyle,
            showSubtleTranscription = showTrans,
            selectedTtsVoiceName = voiceName,
            autoListenOnOpen = autoListen,
            continuousVoiceConversation = continuousVoice,
            wakeWordEnabled = wakeEnabled,
            wakeWordPreset = wakePreset,
            customWakeWord = customWake,
            floatingBubbleEnabled = floatingBubble,
            wakeHapticFeedback = wakeHaptics,
            wakeChimeSound = wakeChime,
            glowIntensity = glow,
            orbScale = scale,
            speechSpeed = speed,
            speechPitch = pitch
        )
    }

    fun updateConfig(update: (AssistantAppearanceConfig) -> AssistantAppearanceConfig) {
        val newConfig = update(_configFlow.value)
        _configFlow.value = newConfig
        prefs.edit()
            .putString("shape", newConfig.shape.name)
            .putString("color_theme", newConfig.colorTheme.name)
            .putString("personality", newConfig.personality.name)
            .putString("voice_language", newConfig.voiceLanguage.name)
            .putString("pill_position_mode", newConfig.pillPositionMode.name)
            .putFloat("custom_offset_x_pct", newConfig.customOffsetXPercent)
            .putFloat("custom_offset_y_pct", newConfig.customOffsetYPercent)
            .putString("pill_size_option", newConfig.pillSizeOption.name)
            .putFloat("pill_scale", newConfig.pillScale)
            .putFloat("pill_opacity", newConfig.pillOpacity)
            .putString("pill_glow_level", newConfig.pillGlowLevel.name)
            .putString("pill_anim_style", newConfig.pillAnimationStyle.name)
            .putBoolean("show_subtle_transcription", newConfig.showSubtleTranscription)
            .putString("selected_tts_voice", newConfig.selectedTtsVoiceName)
            .putBoolean("auto_listen", newConfig.autoListenOnOpen)
            .putBoolean("continuous_voice", newConfig.continuousVoiceConversation)
            .putBoolean("wake_word_enabled", newConfig.wakeWordEnabled)
            .putString("wake_word_preset", newConfig.wakeWordPreset)
            .putString("custom_wake_word", newConfig.customWakeWord)
            .putBoolean("floating_bubble", newConfig.floatingBubbleEnabled)
            .putBoolean("wake_haptic_feedback", newConfig.wakeHapticFeedback)
            .putBoolean("wake_chime_sound", newConfig.wakeChimeSound)
            .putFloat("glow_intensity", newConfig.glowIntensity)
            .putFloat("orb_scale", newConfig.orbScale)
            .putFloat("speech_speed", newConfig.speechSpeed)
            .putFloat("speech_pitch", newConfig.speechPitch)
            .apply()
    }

    fun resetToDefaults() {
        _configFlow.value = AssistantAppearanceConfig()
        prefs.edit().clear().apply()
    }

    fun setPillPositionMode(mode: PillPositionMode) {
        updateConfig { it.copy(pillPositionMode = mode) }
    }

    fun setCustomOffset(xPercent: Float, yPercent: Float) {
        updateConfig {
            it.copy(
                pillPositionMode = PillPositionMode.CUSTOM,
                customOffsetXPercent = xPercent.coerceIn(0.05f, 0.95f),
                customOffsetYPercent = yPercent.coerceIn(0.05f, 0.95f)
            )
        }
    }

    fun setPillSizeOption(option: PillSizeOption) {
        updateConfig { it.copy(pillSizeOption = option) }
    }

    fun setPillScale(scale: Float) {
        updateConfig { it.copy(pillScale = scale.coerceIn(0.65f, 1.45f)) }
    }

    fun setPillOpacity(opacity: Float) {
        updateConfig { it.copy(pillOpacity = opacity.coerceIn(0.3f, 1.0f)) }
    }

    fun setPillGlowLevel(glow: PillGlowLevel) {
        updateConfig { it.copy(pillGlowLevel = glow) }
    }

    fun setPillAnimationStyle(anim: PillAnimationStyle) {
        updateConfig { it.copy(pillAnimationStyle = anim) }
    }

    fun setShowSubtleTranscription(show: Boolean) {
        updateConfig { it.copy(showSubtleTranscription = show) }
    }

    fun setSelectedTtsVoice(voiceName: String) {
        updateConfig { it.copy(selectedTtsVoiceName = voiceName) }
    }

    fun setShape(shape: AssistantShape) {
        updateConfig { it.copy(shape = shape) }
    }

    fun setColorTheme(theme: AssistantColorTheme) {
        updateConfig { it.copy(colorTheme = theme) }
    }

    fun setPersonality(personality: AssistantPersonality) {
        updateConfig { it.copy(personality = personality) }
    }

    fun setVoiceLanguage(language: AssistantLanguage) {
        updateConfig { it.copy(voiceLanguage = language) }
    }

    fun setContinuousVoice(enabled: Boolean) {
        updateConfig { it.copy(continuousVoiceConversation = enabled) }
    }

    fun setAutoListen(enabled: Boolean) {
        updateConfig { it.copy(autoListenOnOpen = enabled) }
    }

    fun setWakeWordEnabled(enabled: Boolean) {
        updateConfig { it.copy(wakeWordEnabled = enabled) }
    }

    fun setFloatingBubble(enabled: Boolean) {
        updateConfig { it.copy(floatingBubbleEnabled = enabled) }
    }

    fun setWakeWordPreset(preset: String) {
        updateConfig { it.copy(wakeWordPreset = preset) }
    }

    fun setCustomWakeWord(word: String) {
        updateConfig { it.copy(customWakeWord = word.trim()) }
    }

    fun setWakeHapticFeedback(enabled: Boolean) {
        updateConfig { it.copy(wakeHapticFeedback = enabled) }
    }

    fun setWakeChimeSound(enabled: Boolean) {
        updateConfig { it.copy(wakeChimeSound = enabled) }
    }

    fun setGlowIntensity(intensity: Float) {
        updateConfig { it.copy(glowIntensity = intensity.coerceIn(0.5f, 1.5f)) }
    }

    fun setOrbScale(scale: Float) {
        updateConfig { it.copy(orbScale = scale.coerceIn(0.8f, 1.3f)) }
    }

    fun setSpeechSpeed(speed: Float) {
        updateConfig { it.copy(speechSpeed = speed.coerceIn(0.7f, 1.5f)) }
    }

    fun setSpeechPitch(pitch: Float) {
        updateConfig { it.copy(speechPitch = pitch.coerceIn(0.7f, 1.5f)) }
    }
}

