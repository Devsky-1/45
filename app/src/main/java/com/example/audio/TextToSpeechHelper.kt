package com.example.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.repository.AssistantLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TextToSpeechHelper(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    var onSpeechCompleted: (() -> Unit)? = null

    private var currentPitch: Float = 0.95f
    private var currentRate: Float = 1.02f
    private var currentLanguage: AssistantLanguage = AssistantLanguage.ENGLISH

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { engine ->
                applyLanguageToEngine(engine, currentLanguage)
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

    fun setAssistantLanguage(language: AssistantLanguage) {
        currentLanguage = language
        tts?.let { engine ->
            applyLanguageToEngine(engine, language)
        }
    }

    private fun applyLanguageToEngine(engine: TextToSpeech, language: AssistantLanguage) {
        when (language) {
            AssistantLanguage.HINDI -> {
                val hiLocale = Locale("hi", "IN")
                val res = engine.setLanguage(hiLocale)
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    val hiGeneric = Locale("hi")
                    val resGeneric = engine.setLanguage(hiGeneric)
                    if (resGeneric == TextToSpeech.LANG_MISSING_DATA || resGeneric == TextToSpeech.LANG_NOT_SUPPORTED) {
                        engine.setLanguage(Locale("en", "IN"))
                    }
                }
            }
            AssistantLanguage.HINGLISH -> {
                // Indian English accent produces crisp, natural Hinglish cadence for Latin phonetics
                val inLocale = Locale("en", "IN")
                val res = engine.setLanguage(inLocale)
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    val hiLocale = Locale("hi", "IN")
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

