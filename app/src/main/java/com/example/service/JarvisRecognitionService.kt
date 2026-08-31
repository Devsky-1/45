package com.example.service

import android.content.Intent
import android.speech.RecognitionService

class JarvisRecognitionService : RecognitionService() {

    override fun onStartListening(recognizerIntent: Intent?, listener: Callback?) {
        // Recognition is handled via system SpeechRecognizer in core engine
    }

    override fun onCancel(listener: Callback?) {
    }

    override fun onStopListening(listener: Callback?) {
    }
}
