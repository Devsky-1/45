package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.AssistantAppearanceConfig
import com.example.data.repository.AssistantColorTheme
import com.example.data.repository.AssistantLanguage
import com.example.data.repository.AssistantPersonality
import com.example.data.repository.AssistantPreferencesManager
import com.example.data.repository.AssistantShape
import com.example.data.repository.WAKE_WORD_PRESETS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("JARVIS", appName)
    }

    @Test
    fun `test assistant appearance preferences persistence`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = AssistantPreferencesManager(context)

        // Default configuration check
        val initialConfig = prefs.configFlow.value
        assertEquals("Hey Jarvis", initialConfig.wakeWordPreset)
        assertEquals(true, initialConfig.wakeWordEnabled)
        assertEquals(AssistantShape.SIRI_ORB, initialConfig.shape)
        assertEquals(AssistantColorTheme.SIRI_IRIDESCENT, initialConfig.colorTheme)
        assertEquals(AssistantLanguage.ENGLISH, initialConfig.voiceLanguage)

        // Update shape, theme and language
        prefs.setShape(AssistantShape.CURVED_PILL)
        prefs.setColorTheme(AssistantColorTheme.CYBER_CYAN)
        prefs.setWakeWordPreset("Hey Siri")
        prefs.setVoiceLanguage(AssistantLanguage.HINDI)

        val updated = prefs.configFlow.value
        assertEquals(AssistantShape.CURVED_PILL, updated.shape)
        assertEquals(AssistantColorTheme.CYBER_CYAN, updated.colorTheme)
        assertEquals("Hey Siri", updated.wakeWordPreset)
        assertEquals("Hey Siri", updated.effectiveWakeWord)
        assertEquals(AssistantLanguage.HINDI, updated.voiceLanguage)

        // Update to Hinglish
        prefs.setVoiceLanguage(AssistantLanguage.HINGLISH)
        val hinglishConfig = prefs.configFlow.value
        assertEquals(AssistantLanguage.HINGLISH, hinglishConfig.voiceLanguage)
    }

    @Test
    fun `test custom wake word configuration`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = AssistantPreferencesManager(context)

        prefs.setWakeWordPreset("Custom")
        prefs.setCustomWakeWord("Computer Alpha")

        val config = prefs.configFlow.value
        assertEquals("Custom", config.wakeWordPreset)
        assertEquals("Computer Alpha", config.customWakeWord)
        assertEquals("Computer Alpha", config.effectiveWakeWord)
    }

    @Test
    fun `test all shapes and themes and languages are available`() {
        assertTrue(AssistantShape.entries.isNotEmpty())
        assertTrue(AssistantColorTheme.entries.isNotEmpty())
        assertTrue(AssistantPersonality.entries.isNotEmpty())
        assertTrue(AssistantLanguage.entries.contains(AssistantLanguage.ENGLISH))
        assertTrue(AssistantLanguage.entries.contains(AssistantLanguage.HINDI))
        assertTrue(AssistantLanguage.entries.contains(AssistantLanguage.HINGLISH))
        assertTrue(WAKE_WORD_PRESETS.contains("Hey Jarvis"))
        assertTrue(WAKE_WORD_PRESETS.contains("Hey Siri"))
    }
}
