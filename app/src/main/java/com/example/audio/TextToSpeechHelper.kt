package com.example.audio

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.example.data.repository.AssistantLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class TtsVoiceInfo(
    val name: String,
    val displayName: String,
    val localeTag: String,
    val isNetworkConnectionRequired: Boolean
)

class TextToSpeechHelper(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _availableVoices = MutableStateFlow<List<TtsVoiceInfo>>(emptyList())
    val availableVoices: StateFlow<List<TtsVoiceInfo>> = _availableVoices.asStateFlow()

    var onSpeechCompleted: (() -> Unit)? = null

    private var currentPitch: Float = 0.95f
    private var currentRate: Float = 1.02f
    private var currentLanguage: AssistantLanguage = AssistantLanguage.ENGLISH
    private var preferredVoiceName: String? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { engine ->
                refreshAvailableVoices(engine)
                applyLanguageToEngine(engine, currentLanguage, preferredVoiceName)
                engine.setPitch(currentPitch)
                engine.setSpeechRate(currentRate)

                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        onSpeechCompleted?.invoke()
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeaking.value = false
                    }
                })
                isInitialized = true
            }
        }
    }

    private fun refreshAvailableVoices(engine: TextToSpeech) {
        try {
            val voicesSet = engine.voices
            if (voicesSet != null && voicesSet.isNotEmpty()) {
                val list = voicesSet.map { voice ->
                    val cleanDisplay = formatVoiceDisplayName(voice)
                    TtsVoiceInfo(
                        name = voice.name,
                        displayName = cleanDisplay,
                        localeTag = voice.locale.toLanguageTag(),
                        isNetworkConnectionRequired = voice.isNetworkConnectionRequired
                    )
                }.sortedBy { it.displayName }
                _availableVoices.value = list
            }
        } catch (_: Exception) {}
    }

    private fun formatVoiceDisplayName(voice: Voice): String {
        val lang = voice.locale.displayLanguage.ifBlank { voice.locale.language }
        val country = voice.locale.displayCountry.ifBlank { "" }
        val subName = voice.name.substringAfterLast("#").substringAfterLast("-")
        return if (country.isNotBlank()) "$lang ($country) - $subName" else "$lang - $subName"
    }

    fun setAssistantLanguage(language: AssistantLanguage, voiceName: String? = null) {
        currentLanguage = language
        if (!voiceName.isNullOrBlank()) {
            preferredVoiceName = voiceName
        }
        tts?.let { engine ->
            applyLanguageToEngine(engine, language, preferredVoiceName)
        }
    }

    fun setVoiceByName(voiceName: String): Boolean {
        preferredVoiceName = voiceName
        val engine = tts ?: return false
        try {
            val matchingVoice = engine.voices?.firstOrNull { it.name == voiceName }
            if (matchingVoice != null) {
                engine.voice = matchingVoice
                return true
            }
        } catch (_: Exception) {}
        return false
    }

    private fun applyLanguageToEngine(
        engine: TextToSpeech,
        language: AssistantLanguage,
        voiceName: String? = null
    ) {
        when (language) {
            AssistantLanguage.HINDI -> {
                val hiLocale = Locale.forLanguageTag("hi-IN")
                val res = engine.setLanguage(hiLocale)
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    val hiGeneric = Locale.forLanguageTag("hi")
                    val resGeneric = engine.setLanguage(hiGeneric)
                    if (resGeneric == TextToSpeech.LANG_MISSING_DATA || resGeneric == TextToSpeech.LANG_NOT_SUPPORTED) {
                        engine.setLanguage(Locale.forLanguageTag("en-IN"))
                    }
                }
            }
            AssistantLanguage.HINGLISH -> {
                // Indian English accent produces crisp, natural Hinglish cadence for Latin phonetics
                val inLocale = Locale.forLanguageTag("en-IN")
                val res = engine.setLanguage(inLocale)
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    val hiLocale = Locale.forLanguageTag("hi-IN")
                    val resHi = engine.setLanguage(hiLocale)
                    if (resHi == TextToSpeech.LANG_MISSING_DATA || resHi == TextToSpeech.LANG_NOT_SUPPORTED) {
                        engine.setLanguage(Locale.US)
                    }
                }
            }
            AssistantLanguage.ENGLISH -> {
                val res = engine.setLanguage(Locale.UK)
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    engine.setLanguage(Locale.US)
                }
            }
        }

        // If specific voice requested, apply it if available, else gracefully fallback
        if (!voiceName.isNullOrBlank()) {
            try {
                val foundVoice = engine.voices?.firstOrNull { it.name == voiceName }
                if (foundVoice != null) {
                    engine.voice = foundVoice
                }
            } catch (_: Exception) {}
        }
    }

    fun setPitch(pitch: Float) {
        currentPitch = pitch
        tts?.setPitch(pitch)
    }

    fun setSpeechRate(rate: Float) {
        currentRate = rate
        tts?.setSpeechRate(rate)
    }

    fun speak(text: String) {
        if (_isMuted.value || !isInitialized) return
        stop()

        // Clean out asterisks, markdown symbols, and bracket tags for clear vocalization
        val cleanText = text
            .replace("*", "")
            .replace("#", "")
            .replace("`", "")
            .replace(Regex("(?i)\\[.*?\\]"), "")
            .trim()

        if (cleanText.isBlank()) return

        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "JARVIS_RESPONSE_${System.currentTimeMillis()}")
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, "JARVIS_UTTERANCE")
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun toggleMute(): Boolean {
        val newMute = !_isMuted.value
        _isMuted.value = newMute
        if (newMute) {
            stop()
        }
        return newMute
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }
}

