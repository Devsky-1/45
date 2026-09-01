package com.example.audio

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class WakeWordEngine(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private var currentWakeWord: String = "Hey Jarvis"
    private var isEnabled: Boolean = true
    private var currentLanguage: com.example.data.repository.AssistantLanguage =
        com.example.data.repository.AssistantLanguage.ENGLISH
    private var onWakeWordTriggered: ((command: String?) -> Unit)? = null

    private val _isAmbientListening = MutableStateFlow(false)
    val isAmbientListening: StateFlow<Boolean> = _isAmbientListening.asStateFlow()

    private val _lastDetectedWakeWord = MutableStateFlow<String?>(null)
    val lastDetectedWakeWord: StateFlow<String?> = _lastDetectedWakeWord.asStateFlow()

    private var isPaused = false
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
        } catch (_: Exception) {}
    }

    fun configure(
        wakeWord: String,
        enabled: Boolean,
        language: com.example.data.repository.AssistantLanguage = currentLanguage
    ) {
        currentWakeWord = wakeWord.trim()
        isEnabled = enabled
        currentLanguage = language
        if (!enabled) {
            stopAmbientListening()
        }
    }

    fun startAmbientListening(onTrigger: (command: String?) -> Unit) {
        onWakeWordTriggered = onTrigger
        if (!isEnabled || isPaused) return
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _isAmbientListening.value = false
            return
        }

        mainHandler.post {
            startInternalRecognizer()
        }
    }

    private fun startInternalRecognizer() {
        if (!isEnabled || isPaused) return

        try {
            destroyRecognizer()

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isAmbientListening.value = true
                    }

                    override fun onBeginningOfSpeech() {}

                    override fun onRmsChanged(rmsdB: Float) {}

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {}

                    override fun onError(error: Int) {
                        _isAmbientListening.value = false
                        // In ambient mode, seamlessly restart on timeout, no-match, or client idle
                        if (!isPaused && isEnabled) {
                            scheduleRestart(delayMillis = if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) 1200L else 400L)
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        _isAmbientListening.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()
                        if (!text.isNullOrBlank()) {
                            checkAndProcessWakeWord(text)
                        } else if (!isPaused && isEnabled) {
                            scheduleRestart(300L)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()
                        if (!text.isNullOrBlank()) {
                            val matched = checkAndProcessWakeWord(text, isPartial = true)
                            if (matched) {
                                // Destroy to prevent duplicate callbacks
                                destroyRecognizer()
                            }
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val ambientLocale = when (currentLanguage) {
                com.example.data.repository.AssistantLanguage.HINDI -> "hi-IN"
                com.example.data.repository.AssistantLanguage.HINGLISH -> "en-IN"
                com.example.data.repository.AssistantLanguage.ENGLISH -> Locale.getDefault().toLanguageTag()
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, ambientLocale)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, ambientLocale)
                if (currentLanguage == com.example.data.repository.AssistantLanguage.HINGLISH || currentLanguage == com.example.data.repository.AssistantLanguage.HINDI) {
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("hi-IN", "en-IN", "en-US"))
                }
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1500L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1000L)
            }

            speechRecognizer?.startListening(intent)
            _isAmbientListening.value = true
        } catch (e: Exception) {
            Log.w("WakeWordEngine", "Failed to start ambient recognizer: ${e.message}")
            _isAmbientListening.value = false
            if (!isPaused && isEnabled) {
                scheduleRestart(1500L)
            }
        }
    }

    private fun checkAndProcessWakeWord(rawText: String, isPartial: Boolean = false): Boolean {
        // Retain unicode letters so Hindi Devanagari is preserved
        val normalized = rawText.lowercase().replace(Regex("[^\\p{L}\\p{N}\\s]"), " ").trim()
        val wakeTarget = currentWakeWord.lowercase().replace(Regex("[^\\p{L}\\p{N}\\s]"), " ").trim()

        // Match variations:
        // E.g. target "hey jarvis" -> also accept "jarvis" if the target includes "jarvis"
        val altTarget = if (wakeTarget.startsWith("hey ")) wakeTarget.removePrefix("hey ").trim() else null
        val okTarget = if (wakeTarget.startsWith("hey ")) "ok ${wakeTarget.removePrefix("hey ").trim()}" else null

        val wakeIndex = when {
            normalized.contains(wakeTarget) -> normalized.indexOf(wakeTarget) to wakeTarget.length
            altTarget != null && normalized.contains(altTarget) -> normalized.indexOf(altTarget) to altTarget.length
            okTarget != null && normalized.contains(okTarget) -> normalized.indexOf(okTarget) to okTarget.length
            // Additional smart multilingual phonetic matching for "jarvis" / "siri" / Hindi wake phrases
            normalized.contains("jarvis") -> normalized.indexOf("jarvis") to 6
            normalized.contains("जार्विस") -> normalized.indexOf("जार्विस") to 7
            normalized.contains("सूनो जार्विस") || normalized.contains("सुनो जार्विस") -> normalized.indexOf("जार्विस") to 7
            normalized.contains("suno jarvis") -> normalized.indexOf("suno jarvis") to 11
            normalized.contains("namaste jarvis") -> normalized.indexOf("namaste jarvis") to 14
            normalized.contains("नमस्ते जार्विस") -> normalized.indexOf("नमस्ते जार्विस") to 13
            normalized.contains("जार्विस भाई") || normalized.contains("jarvis bhai") -> normalized.indexOf("jarvis") to 6
            wakeTarget.contains("siri") && normalized.contains("siri") -> normalized.indexOf("siri") to 4
            wakeTarget.contains("computer") && normalized.contains("computer") -> normalized.indexOf("computer") to 8
            wakeTarget.contains("friday") && normalized.contains("friday") -> normalized.indexOf("friday") to 6
            wakeTarget.contains("edith") && normalized.contains("edith") -> normalized.indexOf("edith") to 5
            else -> null
        }

        if (wakeIndex != null) {
            val (startIdx, length) = wakeIndex
            val afterWake = normalized.substring(startIdx + length).trim()
            val command = afterWake.ifBlank { null }

            _lastDetectedWakeWord.value = currentWakeWord
            playActivationChime()
            onWakeWordTriggered?.invoke(command)
            return true
        } else if (!isPartial && !isPaused && isEnabled) {
            scheduleRestart(300L)
        }
        return false
    }

    fun playActivationChime() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
        } catch (_: Exception) {}
    }

    private fun scheduleRestart(delayMillis: Long) {
        mainHandler.removeCallbacksAndMessages(null)
        mainHandler.postDelayed({
            if (!isPaused && isEnabled) {
                startInternalRecognizer()
            }
        }, delayMillis)
    }

    fun pause() {
        isPaused = true
        stopAmbientListening()
    }

    fun resume() {
        isPaused = false
        if (isEnabled && onWakeWordTriggered != null) {
            scheduleRestart(300L)
        }
    }

    fun stopAmbientListening() {
        mainHandler.removeCallbacksAndMessages(null)
        destroyRecognizer()
        _isAmbientListening.value = false
    }

    private fun destroyRecognizer() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (_: Exception) {
        } finally {
            speechRecognizer = null
        }
    }
}
