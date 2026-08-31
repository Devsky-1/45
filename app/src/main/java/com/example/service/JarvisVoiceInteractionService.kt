package com.example.service

import android.service.voice.VoiceInteractionService

/**
 * Official VoiceInteractionService allowing J.A.R.V.I.S. to be selected as
 * the default Digital Assistant on Android (replacing Google Assistant / Siri-like functionality).
 */
class JarvisVoiceInteractionService : VoiceInteractionService() {

    override fun onReady() {
        super.onReady()
    }

    override fun onShutdown() {
        super.onShutdown()
    }
}
