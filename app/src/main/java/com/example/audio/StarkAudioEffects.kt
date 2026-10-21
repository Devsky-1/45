package com.example.audio

import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Tactical audio synthesizer that procedural generates iconic Iron Man HUD sound effects
 * using Android's built-in ToneGenerator. Requires zero external asset downloads.
 */
class StarkAudioEffects(private val scope: CoroutineScope) {

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (_: Exception) {
        }
    }

    /**
     * Iconic Iron Man targeting reticle acquire & lock tone sequence (stepped rising chirps).
     */
    fun playTargetLockTone() {
        scope.launch(Dispatchers.IO) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 70)
                delay(90)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 100)
                delay(120)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 160)
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Repulsor capacitor charging hum followed by high-energy blast discharge.
     */
    fun playRepulsorCharge(onBlastDischarged: (() -> Unit)? = null) {
        scope.launch(Dispatchers.IO) {
            try {
                // Ramping charge tones
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 120)
                delay(150)
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_D, 180)
                delay(200)
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_0, 220)
                delay(240)
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_A, 280)
                delay(300)

                // High-energy blast burst
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 400)
                onBlastDischarged?.invoke()
            } catch (_: Exception) {
                onBlastDischarged?.invoke()
            }
        }
    }

    /**
     * Immediate Repulsor discharge burst.
     */
    fun playRepulsorBlast() {
        scope.launch(Dispatchers.IO) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 350)
                delay(100)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 150)
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Stark Armor Protocol confirmation sound (e.g. Veronica, House Party, Stealth).
     */
    fun playProtocolEngagedTone() {
        scope.launch(Dispatchers.IO) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 80)
                delay(100)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 120)
                delay(140)
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 180)
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Sentry Perimeter alert klaxon warning tone.
     */
    fun playSentryAlarm() {
        scope.launch(Dispatchers.IO) {
            try {
                repeat(3) {
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 150)
                    delay(200)
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150)
                    delay(200)
                }
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Arc Reactor Overclock high-frequency whine.
     */
    fun playArcReactorOverclock() {
        scope.launch(Dispatchers.IO) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_C, 250)
                delay(270)
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_PBX_L, 350)
            } catch (_: Exception) {
            }
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {
        }
    }
}
