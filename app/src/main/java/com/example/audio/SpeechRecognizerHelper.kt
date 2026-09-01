package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed interface SpeechState {
    object Idle : SpeechState
    object Listening : SpeechState
    data class PartialResult(val text: String) : SpeechState
    data class FinalResult(val text: String) : SpeechState
    data class Error(val message: String) : SpeechState
}

class SpeechRecognizerHelper(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var currentLanguage: com.example.data.repository.AssistantLanguage =
        com.example.data.repository.AssistantLanguage.ENGLISH

    private val _speechState = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    private val _rmsAudioLevel = MutableStateFlow(0f)
    val rmsAudioLevel: StateFlow<Float> = _rmsAudioLevel.asStateFlow()

    fun setAssistantLanguage(language: com.example.data.repository.AssistantLanguage) {
        currentLanguage = language
    }

    fun startListening(
        language: com.example.data.repository.AssistantLanguage = currentLanguage,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition not available on this device")
            return
        }

        currentLanguage = language
        stopListening()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _speechState.value = SpeechState.Listening
                }

                override fun onBeginningOfSpeech() {
                    _speechState.value = SpeechState.Listening
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Normalize RMS dB typically (-2 to 10) to 0.0 .. 1.0 for visuals
                    val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                    _rmsAudioLevel.value = normalized
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _rmsAudioLevel.value = 0f
                }

                override fun onError(error: Int) {
                    _rmsAudioLevel.value = 0f
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Speech recognition client error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                        SpeechRecognizer.ERROR_NETWORK -> "Network connection error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No voice match detected"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                        else -> "Speech error: $error"
                    }
                    _speechState.value = SpeechState.Error(errorMessage)
                    onError(errorMessage)
                }

                override fun onResults(results: Bundle?) {
                    _rmsAudioLevel.value = 0f
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spokenText = matches?.firstOrNull()
                    if (!spokenText.isNullOrBlank()) {
                        _speechState.value = SpeechState.FinalResult(spokenText)
                        onResult(spokenText)
                    } else {
                        _speechState.value = SpeechState.Idle
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull()
                    if (!text.isNullOrBlank()) {
                        _speechState.value = SpeechState.PartialResult(text)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val primaryLocale = when (language) {
            com.example.data.repository.AssistantLanguage.HINDI -> "hi-IN"
            com.example.data.repository.AssistantLanguage.HINGLISH -> "en-IN"
            com.example.data.repository.AssistantLanguage.ENGLISH -> Locale.getDefault().toLanguageTag()
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, primaryLocale)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, primaryLocale)
            if (language == com.example.data.repository.AssistantLanguage.HINGLISH) {
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("hi-IN", "en-IN", "en-US"))
            } else if (language == com.example.data.repository.AssistantLanguage.HINDI) {
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("en-IN", "hi-IN"))
            }
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        try {
            speechRecognizer?.startListening(intent)
            _speechState.value = SpeechState.Listening
        } catch (e: Exception) {
            _speechState.value = SpeechState.Error("Failed to start voice recognition: ${e.message}")
            onError("Failed to start voice recognition")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (_: Exception) {
        } finally {
            speechRecognizer = null
            _speechState.value = SpeechState.Idle
            _rmsAudioLevel.value = 0f
        }
    }
}
