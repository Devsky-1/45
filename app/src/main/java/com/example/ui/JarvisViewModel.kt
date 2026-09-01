package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SpeechRecognizerHelper
import com.example.audio.SpeechState
import com.example.audio.TextToSpeechHelper
import com.example.data.api.GeminiClient
import com.example.data.db.ChatMessageEntity
import com.example.data.db.JarvisDatabase
import com.example.data.db.JarvisNoteEntity
import com.example.data.db.QuickCommandEntity
import com.example.data.db.ReminderEntity
import com.example.data.repository.JarvisRepository
import com.example.device.ActiveTimer
import com.example.device.DeviceController
import com.example.device.DeviceTelemetry
import com.example.domain.JarvisCommandParser
import com.example.domain.ParsedJarvisCommand
import com.example.ui.components.JarvisState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val database = JarvisDatabase.getDatabase(application, viewModelScope)
    private val repository = JarvisRepository(database.jarvisDao())
    val preferencesManager = com.example.data.repository.AssistantPreferencesManager(application)

    val deviceController = DeviceController(application, viewModelScope)
    val ttsHelper = TextToSpeechHelper(application)
    val speechHelper = SpeechRecognizerHelper(application)
    val wakeWordEngine = com.example.audio.WakeWordEngine(application)

    val appearanceConfig: StateFlow<com.example.data.repository.AssistantAppearanceConfig> = preferencesManager.configFlow
    val isAmbientWakeWordListening: StateFlow<Boolean> = wakeWordEngine.isAmbientListening

    // Data streams from Room
    val messages: StateFlow<List<ChatMessageEntity>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<ReminderEntity>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<JarvisNoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quickCommands: StateFlow<List<QuickCommandEntity>> = repository.allQuickCommands
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Telemetry & Active Timers
    val telemetry: StateFlow<DeviceTelemetry> = deviceController.telemetry
    val activeTimers: StateFlow<List<ActiveTimer>> = deviceController.activeTimers

    // UI & Assistant State
    private val _jarvisState = MutableStateFlow(JarvisState.STANDBY)
    val jarvisState: StateFlow<JarvisState> = _jarvisState.asStateFlow()

    private val _currentQueryInput = MutableStateFlow("")
    val currentQueryInput: StateFlow<String> = _currentQueryInput.asStateFlow()

    private val _activeProtocol = MutableStateFlow("STANDARD PROTOCOL")
    val activeProtocol: StateFlow<String> = _activeProtocol.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Core HUD, 1: Studio Customize, 2: Lock Screen, 3: Diagnostics, 4: Memory
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _isLockScreenActive = MutableStateFlow(false)
    val isLockScreenActive: StateFlow<Boolean> = _isLockScreenActive.asStateFlow()

    private val _isBackgroundServiceActive = MutableStateFlow(false)
    val isBackgroundServiceActive: StateFlow<Boolean> = _isBackgroundServiceActive.asStateFlow()

    private val _isFloatingOverlayActive = MutableStateFlow(false)
    val isFloatingOverlayActive: StateFlow<Boolean> = _isFloatingOverlayActive.asStateFlow()

    val rmsAudioLevel: StateFlow<Float> = speechHelper.rmsAudioLevel
    val isTtsSpeaking: StateFlow<Boolean> = ttsHelper.isSpeaking
    val isMuted: StateFlow<Boolean> = ttsHelper.isMuted

    init {
        // Initial speech settings
        ttsHelper.setPitch(appearanceConfig.value.speechPitch)
        ttsHelper.setSpeechRate(appearanceConfig.value.speechSpeed)

        // Configure Wake Word Engine
        wakeWordEngine.configure(
            wakeWord = appearanceConfig.value.effectiveWakeWord,
            enabled = appearanceConfig.value.wakeWordEnabled
        )

        startAmbientWakeWordListener()

        // Observe TTS state to update visualizer & pause/resume wake word
        viewModelScope.launch {
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
        viewModelScope.launch {
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

        // Dynamic config observer for TTS & Wake Word
        viewModelScope.launch {
            appearanceConfig.collect { config ->
                ttsHelper.setPitch(config.speechPitch)
                ttsHelper.setSpeechRate(config.speechSpeed)
                wakeWordEngine.configure(
                    wakeWord = config.effectiveWakeWord,
                    enabled = config.wakeWordEnabled
                )
            }
        }

        // Auto re-listen on continuous conversation after speech completes
        ttsHelper.onSpeechCompleted = {
            if (appearanceConfig.value.continuousVoiceConversation && !isMuted.value) {
                viewModelScope.launch {
                    kotlinx.coroutines.delay(400)
                    if (_jarvisState.value == JarvisState.STANDBY) {
                        deviceController.vibrateHaptic(30)
                        speechHelper.startListening(
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
            viewModelScope.launch(Dispatchers.Main) {
                if (appearanceConfig.value.wakeHapticFeedback) {
                    deviceController.vibrateHaptic(60)
                }

                if (!command.isNullOrBlank()) {
                    _currentQueryInput.value = command
                    processUserQuery(command)
                } else {
                    val wakeGreeting = when (appearanceConfig.value.personality) {
                        com.example.data.repository.AssistantPersonality.JARVIS_AI -> "Online, sir. How may I assist?"
                        com.example.data.repository.AssistantPersonality.SIRI_PRO -> "I'm listening."
                        com.example.data.repository.AssistantPersonality.PRO_EXECUTIVE -> "Ready. Go ahead."
                    }
                    _jarvisState.value = JarvisState.LISTENING
                    ttsHelper.speak(wakeGreeting)
                }
            }
        }
    }

    fun setShape(shape: com.example.data.repository.AssistantShape) = setAssistantShape(shape)
    fun updateShape(shape: com.example.data.repository.AssistantShape) = setAssistantShape(shape)

    fun setColorTheme(theme: com.example.data.repository.AssistantColorTheme) = setAssistantColorTheme(theme)
    fun updateColorTheme(theme: com.example.data.repository.AssistantColorTheme) = setAssistantColorTheme(theme)

    fun setPersonality(personality: com.example.data.repository.AssistantPersonality) = setAssistantPersonality(personality)
    fun updatePersonality(personality: com.example.data.repository.AssistantPersonality) = setAssistantPersonality(personality)

    fun toggleAmbientWakeWord(enabled: Boolean) {
        setWakeWordEnabled(enabled)
        val app = getApplication<Application>()
        if (enabled) {
            com.example.service.JarvisBackgroundService.start(app)
        } else {
            com.example.service.JarvisBackgroundService.stop(app)
        }
    }

    fun updateSelectedWakeWord(preset: String) = setWakeWordPreset(preset)
    fun updateCustomWakeWord(word: String) = setCustomWakeWord(word)
    fun testCurrentWakeWord() = testWakeWord()

    fun updateFloatingBubble(enabled: Boolean) {
        preferencesManager.setFloatingBubble(enabled)
        toggleFloatingOverlay(enabled)
    }

    fun updateAutoListen(enabled: Boolean) = setAutoListenOnOpen(enabled)
    fun updateContinuousConversation(enabled: Boolean) = setContinuousVoice(enabled)
    fun updateWakeHaptic(enabled: Boolean) = setWakeHapticFeedback(enabled)
    fun updateSpeechPitch(pitch: Float) = setSpeechPitch(pitch)
    fun updateSpeechSpeed(speed: Float) = setSpeechSpeed(speed)
    fun resetToDefaults() {
        preferencesManager.resetToDefaults()
        deviceController.vibrateHaptic(40)
    }

    fun speakText(text: String) {
        viewModelScope.launch(Dispatchers.Main) {
            ttsHelper.speak(text)
        }
    }

    fun clearConversation() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearHistory()
        }
        deviceController.vibrateHaptic(30)
    }

    fun setAssistantShape(shape: com.example.data.repository.AssistantShape) {
        preferencesManager.setShape(shape)
        deviceController.vibrateHaptic(30)
    }

    fun setAssistantColorTheme(theme: com.example.data.repository.AssistantColorTheme) {
        preferencesManager.setColorTheme(theme)
        deviceController.vibrateHaptic(30)
    }

    fun setAssistantPersonality(personality: com.example.data.repository.AssistantPersonality) {
        preferencesManager.setPersonality(personality)
        deviceController.vibrateHaptic(30)
    }

    fun setAssistantLanguage(language: com.example.data.repository.AssistantLanguage) {
        preferencesManager.setVoiceLanguage(language)
        deviceController.vibrateHaptic(30)
    }

    fun updateVoiceLanguage(language: com.example.data.repository.AssistantLanguage) {
        setAssistantLanguage(language)
    }

    fun testVoiceLanguage(language: com.example.data.repository.AssistantLanguage) {
        setAssistantLanguage(language)
        viewModelScope.launch(Dispatchers.Main) {
            ttsHelper.setAssistantLanguage(language)
            ttsHelper.speak(language.samplePhrase)
        }
    }

    fun setContinuousVoice(enabled: Boolean) {
        preferencesManager.setContinuousVoice(enabled)
        deviceController.vibrateHaptic(20)
    }

    fun setAutoListenOnOpen(enabled: Boolean) {
        preferencesManager.setAutoListen(enabled)
        deviceController.vibrateHaptic(20)
    }

    fun setWakeWordEnabled(enabled: Boolean) {
        preferencesManager.setWakeWordEnabled(enabled)
        deviceController.vibrateHaptic(20)
    }

    fun setWakeWordPreset(preset: String) {
        preferencesManager.setWakeWordPreset(preset)
        deviceController.vibrateHaptic(20)
    }

    fun setCustomWakeWord(word: String) {
        preferencesManager.setCustomWakeWord(word)
        deviceController.vibrateHaptic(20)
    }

    fun setWakeHapticFeedback(enabled: Boolean) {
        preferencesManager.setWakeHapticFeedback(enabled)
    }

    fun setWakeChimeSound(enabled: Boolean) {
        preferencesManager.setWakeChimeSound(enabled)
    }

    fun testWakeWord(customWord: String? = null) {
        val word = customWord ?: appearanceConfig.value.effectiveWakeWord
        deviceController.vibrateHaptic(70)
        wakeWordEngine.playActivationChime()
        val reply = "Wake word '$word' verified and active, sir. You can say '$word' anytime or tap the visualizer to speak."
        viewModelScope.launch(Dispatchers.Main) {
            ttsHelper.speak(reply)
        }
    }

    fun setGlowIntensity(intensity: Float) {
        preferencesManager.setGlowIntensity(intensity)
    }

    fun setOrbScale(scale: Float) {
        preferencesManager.setOrbScale(scale)
    }

    fun setSpeechSpeed(speed: Float) {
        preferencesManager.setSpeechSpeed(speed)
    }

    fun setSpeechPitch(pitch: Float) {
        preferencesManager.setSpeechPitch(pitch)
    }

    fun setQueryInput(text: String) {
        _currentQueryInput.value = text
    }

    fun setSelectedTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
        deviceController.vibrateHaptic(25)
    }

    fun toggleLockScreenSimulator(active: Boolean? = null) {
        val newState = active ?: !_isLockScreenActive.value
        _isLockScreenActive.value = newState
        deviceController.vibrateHaptic(40)
    }

    fun toggleVoiceRecognition() {
        if (_jarvisState.value == JarvisState.LISTENING) {
            speechHelper.stopListening()
            _jarvisState.value = JarvisState.STANDBY
        } else {
            ttsHelper.stop()
            deviceController.vibrateHaptic(50)
            speechHelper.startListening(
                onResult = { spokenText ->
                    processUserQuery(spokenText)
                },
                onError = { errorMsg ->
                    // Fallback to standby
                    _jarvisState.value = JarvisState.STANDBY
                }
            )
        }
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

    fun processUserQuery(query: String) {
        if (query.isBlank()) return
        _jarvisState.value = JarvisState.PROCESSING
        deviceController.vibrateHaptic(30)

        viewModelScope.launch(Dispatchers.IO) {
            // Save user message to Room DB
            repository.saveMessage(sender = "USER", text = query)

            // Parse command intent
            val command = JarvisCommandParser.parse(query)
            executeParsedCommand(command, query)
        }
    }

    private suspend fun executeParsedCommand(command: ParsedJarvisCommand, rawQuery: String) {
        val lang = appearanceConfig.value.voiceLanguage

        when (command) {
            is ParsedJarvisCommand.Flashlight -> {
                val success = deviceController.setFlashlight(command.enable)
                val reply = when (lang) {
                    com.example.data.repository.AssistantLanguage.HINDI ->
                        if (command.enable) "टॉर्च चालू कर दी गई है, सर।" else "टॉर्च बंद कर दी गई है, सर।"
                    com.example.data.repository.AssistantLanguage.HINGLISH ->
                        if (command.enable) "Flashlight on kar di gayi hai Sir!" else "Flashlight band kar di gayi hai Sir."
                    com.example.data.repository.AssistantLanguage.ENGLISH ->
                        if (command.enable) "Illumination protocol engaged, sir. Tactical LED emitter activated." else "Flashlight powered down, sir. Systems returning to ambient standby."
                }
                respondAsJarvis(reply, actionType = "FLASHLIGHT", actionPayload = if (command.enable) "ON" else "OFF")
            }

            is ParsedJarvisCommand.Timer -> {
                val timer = deviceController.startTimer(command.seconds, command.label)
                val minutes = command.seconds / 60
                val secs = command.seconds % 60
                val durationText = when {
                    minutes > 0 && secs > 0 -> "$minutes minutes and $secs seconds"
                    minutes > 0 -> "$minutes minutes"
                    else -> "$secs seconds"
                }
                val reply = when (lang) {
                    com.example.data.repository.AssistantLanguage.HINDI ->
                        "$durationText का टाइमर शुरू कर दिया गया है, सर।"
                    com.example.data.repository.AssistantLanguage.HINGLISH ->
                        "$durationText ka timer start ho gaya hai Sir!"
                    com.example.data.repository.AssistantLanguage.ENGLISH ->
                        "Timer initialized for $durationText under '${command.label}', sir. I will alert you upon countdown completion."
                }
                respondAsJarvis(reply, actionType = "TIMER", actionPayload = "${command.seconds}")
            }

            is ParsedJarvisCommand.Reminder -> {
                repository.addReminder(command.title, command.dueTime, command.priority)
                val reply = when (lang) {
                    com.example.data.repository.AssistantLanguage.HINDI ->
                        "रिमाइंडर दर्ज कर लिया गया है: '${command.title}', ${command.dueTime} के लिए, सर।"
                    com.example.data.repository.AssistantLanguage.HINGLISH ->
                        "Reminder log ho gaya hai: '${command.title}', schedule time ${command.dueTime} hai Sir."
                    com.example.data.repository.AssistantLanguage.ENGLISH ->
                        "Reminder logged to memory matrix: '${command.title}' scheduled for ${command.dueTime}, sir."
                }
                respondAsJarvis(reply, actionType = "REMINDER", actionPayload = command.title)
            }

            is ParsedJarvisCommand.Note -> {
                repository.addNote(command.title, command.content)
                val reply = when (lang) {
                    com.example.data.repository.AssistantLanguage.HINDI ->
                        "नोट सुरक्षित कर लिया गया है: '${command.title}', सर।"
                    com.example.data.repository.AssistantLanguage.HINGLISH ->
                        "Note secure ho gaya hai: '${command.title}', Sir."
                    com.example.data.repository.AssistantLanguage.ENGLISH ->
                        "Note secured in encrypted archives: '${command.title}', sir."
                }
                respondAsJarvis(reply, actionType = "NOTE", actionPayload = command.content)
            }

            is ParsedJarvisCommand.DailyBriefing -> {
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
                val battery = telemetry.value.batteryLevel
                val pendingReminders = reminders.value.count { !it.isCompleted }
                val reply = when (lang) {
                    com.example.data.repository.AssistantLanguage.HINDI ->
                        "शुभ दिवस सर। समय $timeFormat हुआ है, $dateFormat। बैटरी $battery% है, मौसम 24°C के साथ अनुकूल है। आपके $pendingReminders रिमाइंडर बाकी हैं।"
                    com.example.data.repository.AssistantLanguage.HINGLISH ->
                        "Good day Sir! Abhi time $timeFormat hai, $dateFormat. Battery $battery% pe hai aur weather 24°C clear hai. $pendingReminders reminders scheduled hain."
                    com.example.data.repository.AssistantLanguage.ENGLISH ->
                        "Good day, sir. It is $timeFormat on $dateFormat. Power levels are at $battery%. Weather is clear at 24°C. You have $pendingReminders scheduled reminders in your agenda. All defense grids are nominal."
                }
                respondAsJarvis(reply, actionType = "BRIEFING")
            }

            is ParsedJarvisCommand.Diagnostic -> {
                val t = telemetry.value
                val reply = when (lang) {
                    com.example.data.repository.AssistantLanguage.HINDI ->
                        "सिस्टम रिपोर्ट, सर: बैटरी ${t.batteryLevel}%, रैम मेमोरी ${t.ramUsagePercent}% उपयोग में, फ्री स्टोरेज ${String.format(Locale.getDefault(), "%.1f", t.storageFreeGb)}GB उपलब्ध, और नेटवर्क स्थिति ${t.networkStatus} है।"
                    com.example.data.repository.AssistantLanguage.HINGLISH ->
                        "System Diagnostic Report Sir: Battery ${t.batteryLevel}%, RAM ${t.ramUsagePercent}%, Storage free ${String.format(Locale.getDefault(), "%.1f", t.storageFreeGb)}GB, network ${t.networkStatus}."
                    com.example.data.repository.AssistantLanguage.ENGLISH ->
                        "System Diagnostic Report, sir: Core battery at ${t.batteryLevel}% (${if (t.isCharging) "Charging" else "Discharging"}), RAM allocation at ${t.ramUsagePercent}% (${t.ramUsedMb}MB of ${t.ramTotalMb}MB), Flash storage with ${String.format(Locale.getDefault(), "%.1f", t.storageFreeGb)}GB available, and Network link is ${t.networkStatus}."
                }
                respondAsJarvis(reply, actionType = "DIAGNOSTIC")
            }

            is ParsedJarvisCommand.Protocol -> {
                _activeProtocol.value = command.protocolName.uppercase()
                val reply = when (command.protocolName) {
                    "House Party Protocol" -> {
                        _jarvisState.value = JarvisState.ALERT
                        if (lang == com.example.data.repository.AssistantLanguage.HINDI) "हाउस पार्टी प्रोटोकॉल अधिकृत, सर।"
                        else if (lang == com.example.data.repository.AssistantLanguage.HINGLISH) "House Party Protocol authorized Sir! Deploying all units."
                        else "House Party Protocol authorized, sir. All automated secondary combat units dispatched to your coordinates."
                    }
                    "Stealth Mode" -> {
                        ttsHelper.stop()
                        if (lang == com.example.data.repository.AssistantLanguage.HINDI) "स्टेल्थ मोड सक्रिय, सर। ऑडियो शांत कर दिया गया है।"
                        else if (lang == com.example.data.repository.AssistantLanguage.HINGLISH) "Stealth mode active Sir. Audio muted."
                        else "Stealth mode engaged, sir. Audio telemetry suppressed, visual signature reduced."
                    }
                    "Perimeter Defense Grid" -> {
                        if (lang == com.example.data.repository.AssistantLanguage.HINDI) "सुरक्षा घेरा ग्रिड सक्रिय, सर।"
                        else if (lang == com.example.data.repository.AssistantLanguage.HINGLISH) "Perimeter Defense Grid online Sir! Sensors active."
                        else "Perimeter Defense Grid online, sir. Sentry scanners sweep radius set to maximum."
                    }
                    else -> {
                        if (lang == com.example.data.repository.AssistantLanguage.HINDI) "प्रोटोकॉल ${command.protocolName} निष्पादित, सर।"
                        else if (lang == com.example.data.repository.AssistantLanguage.HINGLISH) "Protocol ${command.protocolName} executed Sir."
                        else "Protocol ${command.protocolName} executed, sir. Subsystems aligned."
                    }
                }
                respondAsJarvis(reply, actionType = "PROTOCOL", actionPayload = command.protocolName)
            }

            is ParsedJarvisCommand.ClearHistory -> {
                repository.clearHistory()
                _activeProtocol.value = "STANDARD PROTOCOL"
                val reply = when (lang) {
                    com.example.data.repository.AssistantLanguage.HINDI -> "क्लीन स्लेट प्रोटोकॉल पूरा हुआ, सर। सारा डेटा साफ कर दिया गया है।"
                    com.example.data.repository.AssistantLanguage.HINGLISH -> "Clean Slate Protocol complete Sir! Cache aur history clear ho gayi hai."
                    com.example.data.repository.AssistantLanguage.ENGLISH -> "Clean Slate Protocol completed, sir. Chat transcripts and local cache purged. System re-initialized."
                }
                respondAsJarvis(reply, actionType = "PROTOCOL")
            }

            is ParsedJarvisCommand.Weather -> {
                val reply = when (lang) {
                    com.example.data.repository.AssistantLanguage.HINDI -> "${command.location} में मौसम: 22°C, आसमान साफ और हवा अनुकूल है, सर।"
                    com.example.data.repository.AssistantLanguage.HINGLISH -> "${command.location} mein weather 22°C hai, sky clear aur conditions great hain Sir."
                    com.example.data.repository.AssistantLanguage.ENGLISH -> "Atmospheric scan for ${command.location}: Temperature is 24°C with clear skies, humidity at 45%, barometric pressure 1014 hPa, and wind velocity 12 km/h, sir."
                }
                respondAsJarvis(reply, actionType = "WEATHER", actionPayload = command.location)
            }

            is ParsedJarvisCommand.GeneralQuery -> {
                // Query Gemini AI model with chosen personality and language
                val history = messages.value.takeLast(6).map { Pair(it.sender, it.text) }
                val result = GeminiClient.askJarvis(
                    userPrompt = rawQuery,
                    conversationHistory = history,
                    language = appearanceConfig.value.voiceLanguage,
                    personality = appearanceConfig.value.personality
                )
                val answer = result.getOrElse {
                    GeminiClient.generateOfflineJarvisResponse(rawQuery, appearanceConfig.value.voiceLanguage)
                }
                respondAsJarvis(answer)
            }
        }
    }

    private suspend fun respondAsJarvis(text: String, actionType: String? = null, actionPayload: String? = null) {
        repository.saveMessage(
            sender = "JARVIS",
            text = text,
            actionType = actionType,
            actionPayload = actionPayload
        )
        _jarvisState.value = JarvisState.SPEAKING
        ttsHelper.speak(text)
    }

    fun toggleReminderStatus(reminder: ReminderEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleReminder(reminder)
            deviceController.vibrateHaptic(20)
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteReminder(id)
            deviceController.vibrateHaptic(20)
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(id)
            deviceController.vibrateHaptic(20)
        }
    }

    fun cancelTimer(id: String) {
        deviceController.cancelTimer(id)
    }

    fun toggleBackgroundService(enable: Boolean? = null) {
        val app = getApplication<Application>()
        val newState = enable ?: !_isBackgroundServiceActive.value
        _isBackgroundServiceActive.value = newState
        if (newState) {
            com.example.service.JarvisBackgroundService.start(app)
            deviceController.vibrateHaptic(40)
        } else {
            com.example.service.JarvisBackgroundService.stop(app)
            deviceController.vibrateHaptic(20)
        }
    }

    fun toggleFloatingOverlay(enable: Boolean? = null) {
        val app = getApplication<Application>()
        val newState = enable ?: !_isFloatingOverlayActive.value
        _isFloatingOverlayActive.value = newState
        if (newState) {
            com.example.service.JarvisFloatingOverlayService.start(app)
            deviceController.vibrateHaptic(40)
        } else {
            com.example.service.JarvisFloatingOverlayService.stop(app)
            deviceController.vibrateHaptic(20)
        }
    }

    override fun onCleared() {
        super.onCleared()
        wakeWordEngine.stopAmbientListening()
        ttsHelper.shutdown()
        speechHelper.stopListening()
    }
}
