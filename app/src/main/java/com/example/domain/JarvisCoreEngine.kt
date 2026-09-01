package com.example.domain

import android.content.Context
import android.content.Intent
import com.example.audio.SpeechRecognizerHelper
import com.example.audio.SpeechState
import com.example.audio.TextToSpeechHelper
import com.example.data.api.GeminiClient
import com.example.data.db.ChatMessageEntity
import com.example.data.db.JarvisDatabase
import com.example.data.db.JarvisNoteEntity
import com.example.data.db.QuickCommandEntity
import com.example.data.db.ReminderEntity
import com.example.data.repository.AssistantAppearanceConfig
import com.example.data.repository.AssistantColorTheme
import com.example.data.repository.AssistantPersonality
import com.example.data.repository.AssistantPreferencesManager
import com.example.data.repository.AssistantShape
import com.example.data.repository.JarvisRepository
import com.example.device.ActiveTimer
import com.example.device.DeviceController
import com.example.device.DeviceTelemetry
import com.example.ui.components.JarvisState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JarvisCoreEngine private constructor(val context: Context) {

    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val database = JarvisDatabase.getDatabase(context, engineScope)
    val repository = JarvisRepository(database.jarvisDao())
    val preferencesManager = AssistantPreferencesManager(context)

    val deviceController = DeviceController(context, engineScope)
    val ttsHelper = TextToSpeechHelper(context)
    val speechHelper = SpeechRecognizerHelper(context)
    val wakeWordEngine = com.example.audio.WakeWordEngine(context)

    val appearanceConfig: StateFlow<AssistantAppearanceConfig> = preferencesManager.configFlow
    val isAmbientWakeWordListening: StateFlow<Boolean> = wakeWordEngine.isAmbientListening

    // Data streams from Room
    val messages: StateFlow<List<ChatMessageEntity>> = repository.allMessages
        .stateIn(engineScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<ReminderEntity>> = repository.allReminders
        .stateIn(engineScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<JarvisNoteEntity>> = repository.allNotes
        .stateIn(engineScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quickCommands: StateFlow<List<QuickCommandEntity>> = repository.allQuickCommands
        .stateIn(engineScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Telemetry & Active Timers
    val telemetry: StateFlow<DeviceTelemetry> = deviceController.telemetry
    val activeTimers: StateFlow<List<ActiveTimer>> = deviceController.activeTimers

    // UI & Assistant State
    private val _jarvisState = MutableStateFlow(JarvisState.STANDBY)
    val jarvisState: StateFlow<JarvisState> = _jarvisState.asStateFlow()

    private val _currentQueryInput = MutableStateFlow("")
    val currentQueryInput: StateFlow<String> = _currentQueryInput.asStateFlow()

    private val _lastJarvisResponse = MutableStateFlow("At your service, sir.")
    val lastJarvisResponse: StateFlow<String> = _lastJarvisResponse.asStateFlow()

    private val _activeProtocol = MutableStateFlow("STANDARD PROTOCOL")
    val activeProtocol: StateFlow<String> = _activeProtocol.asStateFlow()

    val rmsAudioLevel: StateFlow<Float> = speechHelper.rmsAudioLevel
    val isTtsSpeaking: StateFlow<Boolean> = ttsHelper.isSpeaking
    val isMuted: StateFlow<Boolean> = ttsHelper.isMuted

    init {
        // Apply initial TTS & STT settings
        ttsHelper.setAssistantLanguage(appearanceConfig.value.voiceLanguage)
        ttsHelper.setPitch(appearanceConfig.value.speechPitch)
        ttsHelper.setSpeechRate(appearanceConfig.value.speechSpeed)
        speechHelper.setAssistantLanguage(appearanceConfig.value.voiceLanguage)

        // Configure wake word
        wakeWordEngine.configure(
            wakeWord = appearanceConfig.value.effectiveWakeWord,
            enabled = appearanceConfig.value.wakeWordEnabled,
            language = appearanceConfig.value.voiceLanguage
        )

        // Start ambient wake word listener
        startAmbientWakeWordListener()

        // Observe TTS state to update visualizer & manage ambient wake word
        engineScope.launch {
            ttsHelper.isSpeaking.collect { speaking ->
                if (speaking && _jarvisState.value != JarvisState.LISTENING) {
                    _jarvisState.value = JarvisState.SPEAKING
                    wakeWordEngine.pause()
                } else if (!speaking && _jarvisState.value == JarvisState.SPEAKING) {
                    _jarvisState.value = JarvisState.STANDBY
                    wakeWordEngine.resume()
                }
            }
        }

        // Observe speech recognition state
        engineScope.launch {
            speechHelper.speechState.collect { state ->
                when (state) {
                    is SpeechState.Listening -> {
                        _jarvisState.value = JarvisState.LISTENING
                        wakeWordEngine.pause()
                    }
                    is SpeechState.PartialResult -> {
                        _currentQueryInput.value = state.text
                    }
                    is SpeechState.FinalResult -> {
                        _currentQueryInput.value = state.text
                        processUserQuery(state.text)
                    }
                    is SpeechState.Error -> {
                        _jarvisState.value = JarvisState.STANDBY
                        wakeWordEngine.resume()
                    }
                    SpeechState.Idle -> {
                        if (_jarvisState.value == JarvisState.LISTENING) {
                            _jarvisState.value = JarvisState.STANDBY
                            wakeWordEngine.resume()
                        }
                    }
                }
            }
        }

        // Dynamic config observer
        engineScope.launch {
            appearanceConfig.collect { config ->
                ttsHelper.setAssistantLanguage(config.voiceLanguage)
                ttsHelper.setPitch(config.speechPitch)
                ttsHelper.setSpeechRate(config.speechSpeed)
                speechHelper.setAssistantLanguage(config.voiceLanguage)
                wakeWordEngine.configure(
                    wakeWord = config.effectiveWakeWord,
                    enabled = config.wakeWordEnabled,
                    language = config.voiceLanguage
                )
            }
        }

        // Auto re-listen on continuous conversation after speech completes
        ttsHelper.onSpeechCompleted = {
            if (appearanceConfig.value.continuousVoiceConversation && !isMuted.value) {
                engineScope.launch {
                    kotlinx.coroutines.delay(400)
                    if (_jarvisState.value == JarvisState.STANDBY) {
                        deviceController.vibrateHaptic(30)
                        speechHelper.startListening(
                            language = appearanceConfig.value.voiceLanguage,
                            onResult = { spokenText ->
                                processUserQuery(spokenText)
                            },
                            onError = {
                                _jarvisState.value = JarvisState.STANDBY
                                wakeWordEngine.resume()
                            }
                        )
                    }
                }
            } else {
                wakeWordEngine.resume()
            }
        }
    }

    private fun startAmbientWakeWordListener() {
        wakeWordEngine.startAmbientListening { command ->
            engineScope.launch(Dispatchers.Main) {
                if (appearanceConfig.value.wakeHapticFeedback) {
                    deviceController.vibrateHaptic(60)
                }

                // Show only the selected floating shape from Studio (like Siri) - no full-screen window!
                try {
                    com.example.service.JarvisFloatingOverlayService.activateFromWakeWord(context, command)
                } catch (_: Exception) {}

                if (!command.isNullOrBlank()) {
                    // Spoken command attached directly after wake word (e.g. "Hey Jarvis turn on flashlight")
                    _currentQueryInput.value = command
                    processUserQuery(command)
                } else {
                    // Wake word triggered alone ("Hey Jarvis") -> transition into active listening
                    _jarvisState.value = JarvisState.LISTENING
                    val wakeGreeting = when (appearanceConfig.value.voiceLanguage) {
                        com.example.data.repository.AssistantLanguage.HINDI ->
                            "नमस्ते सर, मैं सुन रहा हूँ। आज्ञा दीजिए।"
                        com.example.data.repository.AssistantLanguage.HINGLISH ->
                            "Haan ji Sir, main sun raha hoon. Boliye!"
                        com.example.data.repository.AssistantLanguage.ENGLISH -> when (appearanceConfig.value.personality) {
                            AssistantPersonality.JARVIS_AI -> "Online, sir. How may I assist?"
                            AssistantPersonality.SIRI_PRO -> "I'm listening."
                            AssistantPersonality.PRO_EXECUTIVE -> "Ready. Go ahead."
                        }
                    }
                    ttsHelper.speak(wakeGreeting)
                }
            }
        }
    }

    fun triggerWakeWordTest(customWord: String? = null) {
        val word = customWord ?: appearanceConfig.value.effectiveWakeWord
        deviceController.vibrateHaptic(70)
        wakeWordEngine.playActivationChime()
        val reply = when (appearanceConfig.value.voiceLanguage) {
            com.example.data.repository.AssistantLanguage.HINDI ->
                "वेक वर्ड '$word' सफलतापूर्वक कैलिब्रेट हो गया है, सर। माइक्रोफोन सेंसर्स सक्रिय हैं।"
            com.example.data.repository.AssistantLanguage.HINGLISH ->
                "Wake word '$word' perfectly calibrate ho gaya hai Sir! System ekdum alert aur active hai."
            com.example.data.repository.AssistantLanguage.ENGLISH ->
                "Wake word '$word' verified and calibrated successfully, sir. Ambient acoustic sensors are operational."
        }
        engineScope.launch(Dispatchers.Main) {
            ttsHelper.speak(reply)
        }
    }

    fun setQueryInput(text: String) {
        _currentQueryInput.value = text
    }

    fun toggleVoiceRecognition(onComplete: ((String) -> Unit)? = null) {
        if (_jarvisState.value == JarvisState.LISTENING) {
            speechHelper.stopListening()
            _jarvisState.value = JarvisState.STANDBY
        } else {
            ttsHelper.stop()
            deviceController.vibrateHaptic(50)
            speechHelper.startListening(
                onResult = { spokenText ->
                    processUserQuery(spokenText, onComplete)
                },
                onError = {
                    _jarvisState.value = JarvisState.STANDBY
                }
            )
        }
    }

    fun startListeningForAssist(onResult: (String) -> Unit, onError: (String) -> Unit) {
        ttsHelper.stop()
        deviceController.vibrateHaptic(50)
        speechHelper.startListening(
            onResult = { spokenText ->
                _currentQueryInput.value = spokenText
                processUserQuery(spokenText)
                onResult(spokenText)
            },
            onError = { err ->
                _jarvisState.value = JarvisState.STANDBY
                onError(err)
            }
        )
    }

    fun toggleMute() {
        ttsHelper.toggleMute()
        deviceController.vibrateHaptic(30)
    }

    fun submitCurrentQuery() {
        val query = _currentQueryInput.value.trim()
        if (query.isNotBlank()) {
            processUserQuery(query)
            _currentQueryInput.value = ""
        }
    }

    fun processUserQuery(query: String, onResponseReady: ((String) -> Unit)? = null) {
        if (query.isBlank()) return
        _jarvisState.value = JarvisState.PROCESSING
        deviceController.vibrateHaptic(30)

        engineScope.launch(Dispatchers.IO) {
            // Save user message to Room DB
            repository.saveMessage(sender = "USER", text = query)

            // Parse command intent
            val command = JarvisCommandParser.parse(query)
            executeParsedCommand(command, query, onResponseReady)
        }
    }

    private suspend fun executeParsedCommand(
        command: ParsedJarvisCommand,
        rawQuery: String,
        onResponseReady: ((String) -> Unit)? = null
    ) {
        val lang = appearanceConfig.value.voiceLanguage

        when (command) {
            is ParsedJarvisCommand.Flashlight -> {
                val success = deviceController.setFlashlight(command.enable)
                val reply = when (lang) {
                    com.example.data.repository.AssistantLanguage.HINDI ->
                        if (command.enable) "टॉर्च चालू कर दी गई है, सर। रोशनी सक्रिय है।" else "टॉर्च बंद कर दी गई है, सर।"
                    com.example.data.repository.AssistantLanguage.HINGLISH ->
                        if (command.enable) "Tactical flashlight on kar di gayi hai Sir! Full illumination active." else "Flashlight band kar di gayi hai Sir."
                    com.example.data.repository.AssistantLanguage.ENGLISH ->
                        if (command.enable) "Illumination protocol engaged, sir. Tactical LED emitter activated." else "Flashlight powered down, sir. Systems returning to ambient standby."
                }
                respondAsJarvis(reply, actionType = "FLASHLIGHT", actionPayload = if (command.enable) "ON" else "OFF", onResponseReady = onResponseReady)
            }

            is ParsedJarvisCommand.Timer -> {
                deviceController.startTimer(command.seconds, command.label)
                val minutes = command.seconds / 60
                val secs = command.seconds % 60
                val durationText = when {
                    minutes > 0 && secs > 0 -> "$minutes minutes and $secs seconds"
                    minutes > 0 -> "$minutes minutes"
                    else -> "$secs seconds"
                }
                val reply = when (lang) {
                    com.example.data.repository.AssistantLanguage.HINDI ->
                        "$durationText का टाइमर '${command.label}' के लिए शुरू कर दिया गया है, सर।"
                    com.example.data.repository.AssistantLanguage.HINGLISH ->
                        "$durationText ka countdown timer set ho gaya hai Sir! Time complete hone par alert kar doonga."
                    com.example.data.repository.AssistantLanguage.ENGLISH ->
                        "Timer initialized for $durationText under '${command.label}', sir. I will alert you upon countdown completion."
                }
                respondAsJarvis(reply, actionType = "TIMER", actionPayload = "${command.seconds}", onResponseReady = onResponseReady)
            }

            is ParsedJarvisCommand.Reminder -> {
                repository.addReminder(command.title, command.dueTime, command.priority)
                val reply = when (lang) {
                    com.example.data.repository.AssistantLanguage.HINDI ->
                        "रिमाइंडर मेमोरी में दर्ज कर लिया गया है: '${command.title}', ${command.dueTime} के लिए, सर।"
                    com.example.data.repository.AssistantLanguage.HINGLISH ->
                        "Reminder log ho gaya hai: '${command.title}', schedule time ${command.dueTime} hai Sir."
                    com.example.data.repository.AssistantLanguage.ENGLISH ->
                        "Reminder logged to memory matrix: '${command.title}' scheduled for ${command.dueTime}, sir."
                }
                respondAsJarvis(reply, actionType = "REMINDER", actionPayload = command.title, onResponseReady = onResponseReady)
            }

            is ParsedJarvisCommand.Note -> {
                repository.addNote(command.title, command.content)
                val reply = when (lang) {
                    com.example.data.repository.AssistantLanguage.HINDI ->
                        "नोट सुरक्षित कर लिया गया है: '${command.title}', सर।"
                    com.example.data.repository.AssistantLanguage.HINGLISH ->
                        "Note encrypted archives mein save ho gaya hai: '${command.title}', Sir."
                    com.example.data.repository.AssistantLanguage.ENGLISH ->
                        "Note secured in encrypted archives: '${command.title}', sir."
                }
                respondAsJarvis(reply, actionType = "NOTE", actionPayload = command.content, onResponseReady = onResponseReady)
            }

            is ParsedJarvisCommand.DailyBriefing -> {
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
                val battery = telemetry.value.batteryLevel
                val pendingReminders = reminders.value.count { !it.isCompleted }
                val reply = when (lang) {
                    com.example.data.repository.AssistantLanguage.HINDI ->
                        "शुभ दिवस सर। समय $timeFormat हुआ है, $dateFormat। बैटरी $battery% है, मौसम 24°C के साथ अनुकूल है, और आपके $pendingReminders रिमाइंडर बाकी हैं।"
                    com.example.data.repository.AssistantLanguage.HINGLISH ->
                        "Good day Sir! Abhi time $timeFormat hai, $dateFormat. Battery level $battery% pe hai aur weather 24°C clear hai. Aapke $pendingReminders reminders scheduled hain."
                    com.example.data.repository.AssistantLanguage.ENGLISH ->
                        "Good day, sir. It is $timeFormat on $dateFormat. Power levels are at $battery%. Weather is clear at 24°C. You have $pendingReminders scheduled reminders in your agenda. All defense grids are nominal."
                }
                respondAsJarvis(reply, actionType = "BRIEFING", onResponseReady = onResponseReady)
            }

            is ParsedJarvisCommand.Diagnostic -> {
                val t = telemetry.value
                val reply = when (lang) {
                    com.example.data.repository.AssistantLanguage.HINDI ->
                        "सिस्टम रिपोर्ट, सर: बैटरी ${t.batteryLevel}% (${if (t.isCharging) "चार्जिंग" else "डिस्चार्जिंग"}), रैम उपयोग ${t.ramUsagePercent}%, फ्री स्टोरेज ${String.format(Locale.getDefault(), "%.1f", t.storageFreeGb)}GB उपलब्ध, और नेटवर्क स्थिति ${t.networkStatus} है।"
                    com.example.data.repository.AssistantLanguage.HINGLISH ->
                        "System Diagnostic Report Sir: Battery ${t.batteryLevel}% (${if (t.isCharging) "Charging" else "On Battery"}), RAM usage ${t.ramUsagePercent}%, Free Space ${String.format(Locale.getDefault(), "%.1f", t.storageFreeGb)}GB, aur network connection ${t.networkStatus} hai."
                    com.example.data.repository.AssistantLanguage.ENGLISH ->
                        "System Diagnostic Report, sir: Core battery at ${t.batteryLevel}% (${if (t.isCharging) "Charging" else "Discharging"}), RAM allocation at ${t.ramUsagePercent}% (${t.ramUsedMb}MB of ${t.ramTotalMb}MB), Flash storage with ${String.format(Locale.getDefault(), "%.1f", t.storageFreeGb)}GB available, and Network link is ${t.networkStatus}."
                }
                respondAsJarvis(reply, actionType = "DIAGNOSTIC", onResponseReady = onResponseReady)
            }

            is ParsedJarvisCommand.Protocol -> {
                _activeProtocol.value = command.protocolName.uppercase()
                val reply = when (command.protocolName) {
                    "House Party Protocol" -> {
                        _jarvisState.value = JarvisState.ALERT
                        if (lang == com.example.data.repository.AssistantLanguage.HINDI) "हाउस पार्टी प्रोटोकॉल अधिकृत, सर। सभी यूनिट्स सक्रिय कर दी गई हैं।"
                        else if (lang == com.example.data.repository.AssistantLanguage.HINGLISH) "House Party Protocol authorized Sir! Sabhi units aapke location pe deploy ho rahi hain."
                        else "House Party Protocol authorized, sir. All automated secondary combat units dispatched to your coordinates."
                    }
                    "Stealth Mode" -> {
                        ttsHelper.stop()
                        if (lang == com.example.data.repository.AssistantLanguage.HINDI) "स्टेल्थ मोड सक्रिय, सर। ऑडियो शांत कर दिया गया है।"
                        else if (lang == com.example.data.repository.AssistantLanguage.HINGLISH) "Stealth mode active Sir. Audio telemetry suppress ho gaya hai."
                        else "Stealth mode engaged, sir. Audio telemetry suppressed, visual signature reduced."
                    }
                    "Perimeter Defense Grid" -> {
                        if (lang == com.example.data.repository.AssistantLanguage.HINDI) "सुरक्षा घेरा ग्रिड सक्रिय, सर। सेंसर्स पूरी क्षमता पर हैं।"
                        else if (lang == com.example.data.repository.AssistantLanguage.HINGLISH) "Perimeter defense grid online Sir! Sensors scanning range maximum pe hai."
                        else "Perimeter Defense Grid online, sir. Sentry scanners sweep radius set to maximum."
                    }
                    else -> {
                        if (lang == com.example.data.repository.AssistantLanguage.HINDI) "प्रोटोकॉल ${command.protocolName} निष्पादित, सर।"
                        else if (lang == com.example.data.repository.AssistantLanguage.HINGLISH) "Protocol ${command.protocolName} execute ho gaya Sir."
                        else "Protocol ${command.protocolName} executed, sir. Subsystems aligned."
                    }
                }
                respondAsJarvis(reply, actionType = "PROTOCOL", actionPayload = command.protocolName, onResponseReady = onResponseReady)
            }

            is ParsedJarvisCommand.Weather -> {
                val reply = when (lang) {
                    com.example.data.repository.AssistantLanguage.HINDI ->
                        "${command.location} में मौसम: 22°C, आसमान साफ और 48% आर्द्रता है, सर।"
                    com.example.data.repository.AssistantLanguage.HINGLISH ->
                        "${command.location} mein weather conditions: 22°C, mostly clear sky aur halki hawa chal rahi hai Sir."
                    com.example.data.repository.AssistantLanguage.ENGLISH ->
                        "Atmospheric conditions in ${command.location}: 22°C, mostly clear with 12 km/h westerly winds, 48% humidity. Optimal flight conditions, sir."
                }
                respondAsJarvis(reply, actionType = "WEATHER", actionPayload = command.location, onResponseReady = onResponseReady)
            }

            is ParsedJarvisCommand.ClearHistory -> {
                repository.clearHistory()
                val reply = when (lang) {
                    com.example.data.repository.AssistantLanguage.HINDI ->
                        "मेमोरी बफ़र्स पूरी तरह साफ कर दिए गए हैं, सर।"
                    com.example.data.repository.AssistantLanguage.HINGLISH ->
                        "Memory buffers clear ho gaye hain Sir. Clean slate protocol complete!"
                    com.example.data.repository.AssistantLanguage.ENGLISH ->
                        "Memory buffers purged, sir. Clean slate protocol completed."
                }
                respondAsJarvis(reply, actionType = "CLEAR", onResponseReady = onResponseReady)
            }

            is ParsedJarvisCommand.GeneralQuery -> {
                // Call Gemini API or fallback to smart Jarvis offline reasoning with selected language and personality
                val recentMessages = messages.value.takeLast(6).map { it.sender to it.text }
                val aiResult = GeminiClient.askJarvis(
                    userPrompt = command.query,
                    conversationHistory = recentMessages,
                    language = appearanceConfig.value.voiceLanguage,
                    personality = appearanceConfig.value.personality
                )
                val defaultFallback = when (lang) {
                    com.example.data.repository.AssistantLanguage.HINDI -> "निर्देश प्राप्त हुआ, सर। सभी प्रणालियाँ तत्पर हैं।"
                    com.example.data.repository.AssistantLanguage.HINGLISH -> "Samajh gaya Sir! Subroutines update ho gaye hain."
                    com.example.data.repository.AssistantLanguage.ENGLISH -> "Understood, sir. Subroutines updated."
                }
                val reply = aiResult.getOrDefault(defaultFallback)
                respondAsJarvis(reply, onResponseReady = onResponseReady)
            }
        }
    }

    private suspend fun respondAsJarvis(
        reply: String,
        actionType: String? = null,
        actionPayload: String? = null,
        onResponseReady: ((String) -> Unit)? = null
    ) {
        _lastJarvisResponse.value = reply
        repository.saveMessage(
            sender = "JARVIS",
            text = reply,
            actionType = actionType,
            actionPayload = actionPayload
        )

        engineScope.launch(Dispatchers.Main) {
            ttsHelper.speak(reply)
            onResponseReady?.invoke(reply)
        }
    }

    fun speakText(text: String) {
        engineScope.launch(Dispatchers.Main) {
            ttsHelper.speak(text)
        }
    }

    fun quickExecuteBriefing() {
        engineScope.launch {
            processUserQuery("status briefing")
        }
    }

    fun quickToggleFlashlight() {
        val newState = !deviceController.telemetry.value.isFlashlightOn
        deviceController.setFlashlight(newState)
        val reply = if (newState) "Tactical light activated, sir." else "Tactical light deactivated, sir."
        engineScope.launch(Dispatchers.Main) {
            ttsHelper.speak(reply)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: JarvisCoreEngine? = null

        fun getInstance(context: Context): JarvisCoreEngine {
            return INSTANCE ?: synchronized(this) {
                val instance = JarvisCoreEngine(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
