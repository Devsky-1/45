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

val WAKE_WORD_PRESETS = listOf(
    "Hey Jarvis",
    "Jarvis",
    "Hey Siri",
    "Computer",
    "Friday",
    "Edith",
    "Hey Assistant",
    "Custom"
)

data class AssistantAppearanceConfig(
    val shape: AssistantShape = AssistantShape.SIRI_ORB,
    val colorTheme: AssistantColorTheme = AssistantColorTheme.SIRI_IRIDESCENT,
    val personality: AssistantPersonality = AssistantPersonality.PRO_EXECUTIVE,
    val autoListenOnOpen: Boolean = true,
    val continuousVoiceConversation: Boolean = true,
    val wakeWordEnabled: Boolean = true,
    val wakeWordPreset: String = "Hey Jarvis",
    val customWakeWord: String = "Jarvis",
    val wakeHapticFeedback: Boolean = true,
    val wakeChimeSound: Boolean = true,
    val glowIntensity: Float = 1.0f, // 0.5f to 1.5f
    val orbScale: Float = 1.0f, // 0.8f to 1.3f
    val speechSpeed: Float = 1.0f, // 0.8f to 1.4f
    val speechPitch: Float = 1.0f // 0.8f to 1.3f
) {
    val effectiveWakeWord: String
        get() = if (wakeWordPreset.equals("Custom", ignoreCase = true)) {
            customWakeWord.trim().ifBlank { "Jarvis" }
        } else {
            wakeWordPreset
        }
}

class AssistantPreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("jarvis_appearance_prefs", Context.MODE_PRIVATE)

    private val _configFlow = MutableStateFlow(loadConfig())
    val configFlow: StateFlow<AssistantAppearanceConfig> = _configFlow.asStateFlow()

    private fun loadConfig(): AssistantAppearanceConfig {
        val shapeName = prefs.getString("shape", AssistantShape.SIRI_ORB.name) ?: AssistantShape.SIRI_ORB.name
        val colorName = prefs.getString("color_theme", AssistantColorTheme.SIRI_IRIDESCENT.name)
            ?: AssistantColorTheme.SIRI_IRIDESCENT.name
        val personalityName = prefs.getString("personality", AssistantPersonality.PRO_EXECUTIVE.name)
            ?: AssistantPersonality.PRO_EXECUTIVE.name
        val autoListen = prefs.getBoolean("auto_listen", true)
        val continuousVoice = prefs.getBoolean("continuous_voice", true)
        val wakeEnabled = prefs.getBoolean("wake_word_enabled", true)
        val wakePreset = prefs.getString("wake_word_preset", "Hey Jarvis") ?: "Hey Jarvis"
        val customWake = prefs.getString("custom_wake_word", "Jarvis") ?: "Jarvis"
        val wakeHaptics = prefs.getBoolean("wake_haptic_feedback", true)
        val wakeChime = prefs.getBoolean("wake_chime_sound", true)
        val glow = prefs.getFloat("glow_intensity", 1.0f)
        val scale = prefs.getFloat("orb_scale", 1.0f)
        val speed = prefs.getFloat("speech_speed", 1.0f)
        val pitch = prefs.getFloat("speech_pitch", 1.0f)

        val shape = runCatching { AssistantShape.valueOf(shapeName) }.getOrDefault(AssistantShape.SIRI_ORB)
        val color = runCatching { AssistantColorTheme.valueOf(colorName) }.getOrDefault(AssistantColorTheme.SIRI_IRIDESCENT)
        val personality = runCatching { AssistantPersonality.valueOf(personalityName) }.getOrDefault(AssistantPersonality.PRO_EXECUTIVE)

        return AssistantAppearanceConfig(
            shape = shape,
            colorTheme = color,
            personality = personality,
            autoListenOnOpen = autoListen,
            continuousVoiceConversation = continuousVoice,
            wakeWordEnabled = wakeEnabled,
            wakeWordPreset = wakePreset,
            customWakeWord = customWake,
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
            .putBoolean("auto_listen", newConfig.autoListenOnOpen)
            .putBoolean("continuous_voice", newConfig.continuousVoiceConversation)
            .putBoolean("wake_word_enabled", newConfig.wakeWordEnabled)
            .putString("wake_word_preset", newConfig.wakeWordPreset)
            .putString("custom_wake_word", newConfig.customWakeWord)
            .putBoolean("wake_haptic_feedback", newConfig.wakeHapticFeedback)
            .putBoolean("wake_chime_sound", newConfig.wakeChimeSound)
            .putFloat("glow_intensity", newConfig.glowIntensity)
            .putFloat("orb_scale", newConfig.orbScale)
            .putFloat("speech_speed", newConfig.speechSpeed)
            .putFloat("speech_pitch", newConfig.speechPitch)
            .apply()
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

    fun setContinuousVoice(enabled: Boolean) {
        updateConfig { it.copy(continuousVoiceConversation = enabled) }
    }

    fun setAutoListen(enabled: Boolean) {
        updateConfig { it.copy(autoListenOnOpen = enabled) }
    }

    fun setWakeWordEnabled(enabled: Boolean) {
        updateConfig { it.copy(wakeWordEnabled = enabled) }
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
