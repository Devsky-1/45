package com.example.domain

import android.content.Context
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

    val appearanceConfig: StateFlow<AssistantAppearanceConfig> = preferencesManager.configFlow

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
        // Apply initial TTS settings
        ttsHelper.setPitch(appearanceConfig.value.speechPitch)
        ttsHelper.setSpeechRate(appearanceConfig.value.speechSpeed)

        // Observe TTS state to update visualizer
        engineScope.launch {
            ttsHelper.isSpeaking.collect { speaking ->
                if (speaking && _jarvisState.value != JarvisState.LISTENING) {
                    _jarvisState.value = JarvisState.SPEAKING
                } else if (!speaking && _jarvisState.value == JarvisState.SPEAKING) {
                    _jarvisState.value = JarvisState.STANDBY
                }
            }
        }

        // Observe speech recognition state
        engineScope.launch {
            speechHelper.speechState.collect { state ->
                when (state) {
                    is SpeechState.Listening -> {
                        _jarvisState.value = JarvisState.LISTENING
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
                    }
                    SpeechState.Idle -> {
                        if (_jarvisState.value == JarvisState.LISTENING) {
                            _jarvisState.value = JarvisState.STANDBY
                        }
                    }
                }
            }
        }

        // Dynamic config observer
        engineScope.launch {
            appearanceConfig.collect { config ->
                ttsHelper.setPitch(config.speechPitch)
                ttsHelper.setSpeechRate(config.speechSpeed)
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
                            onResult = { spokenText ->
                                processUserQuery(spokenText)
                            },
                            onError = {
                                _jarvisState.value = JarvisState.STANDBY
                            }
                        )
                    }
                }
            }
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
        when (command) {
            is ParsedJarvisCommand.Flashlight -> {
                val success = deviceController.setFlashlight(command.enable)
                val reply = if (command.enable) {
                    "Illumination protocol engaged, sir. Tactical LED emitter activated."
                } else {
                    "Flashlight powered down, sir. Systems returning to ambient standby."
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
                val reply = "Timer initialized for $durationText under '${command.label}', sir. I will alert you upon countdown completion."
                respondAsJarvis(reply, actionType = "TIMER", actionPayload = "${command.seconds}", onResponseReady = onResponseReady)
            }

            is ParsedJarvisCommand.Reminder -> {
                repository.addReminder(command.title, command.dueTime, command.priority)
                val reply = "Reminder logged to memory matrix: '${command.title}' scheduled for ${command.dueTime}, sir."
                respondAsJarvis(reply, actionType = "REMINDER", actionPayload = command.title, onResponseReady = onResponseReady)
            }

            is ParsedJarvisCommand.Note -> {
                repository.addNote(command.title, command.content)
                val reply = "Note secured in encrypted archives: '${command.title}', sir."
                respondAsJarvis(reply, actionType = "NOTE", actionPayload = command.content, onResponseReady = onResponseReady)
            }

            is ParsedJarvisCommand.DailyBriefing -> {
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
                val battery = telemetry.value.batteryLevel
                val pendingReminders = reminders.value.count { !it.isCompleted }
                val reply = "Good day, sir. It is $timeFormat on $dateFormat. Power levels are at $battery%. Weather is clear at 24°C. You have $pendingReminders scheduled reminders in your agenda. All defense grids are nominal."
                respondAsJarvis(reply, actionType = "BRIEFING", onResponseReady = onResponseReady)
            }

            is ParsedJarvisCommand.Diagnostic -> {
                val t = telemetry.value
                val reply = "System Diagnostic Report, sir: Core battery at ${t.batteryLevel}% (${if (t.isCharging) "Charging" else "Discharging"}), RAM allocation at ${t.ramUsagePercent}% (${t.ramUsedMb}MB of ${t.ramTotalMb}MB), Flash storage with ${String.format(Locale.getDefault(), "%.1f", t.storageFreeGb)}GB available, and Network link is ${t.networkStatus}."
                respondAsJarvis(reply, actionType = "DIAGNOSTIC", onResponseReady = onResponseReady)
            }

            is ParsedJarvisCommand.Protocol -> {
                _activeProtocol.value = command.protocolName.uppercase()
                val reply = when (command.protocolName) {
                    "House Party Protocol" -> {
                        _jarvisState.value = JarvisState.ALERT
                        "House Party Protocol authorized, sir. All automated secondary combat units dispatched to your coordinates."
                    }
                    "Stealth Mode" -> {
                        ttsHelper.stop()
                        "Stealth mode engaged, sir. Audio telemetry suppressed, visual signature reduced."
                    }
                    "Perimeter Defense Grid" -> {
                        "Perimeter Defense Grid online, sir. Sentry scanners sweep radius set to maximum."
                    }
                    else -> {
                        "Protocol ${command.protocolName} executed, sir. Subsystems aligned."
                    }
                }
                respondAsJarvis(reply, actionType = "PROTOCOL", actionPayload = command.protocolName, onResponseReady = onResponseReady)
            }

            is ParsedJarvisCommand.Weather -> {
                val reply = "Atmospheric conditions in ${command.location}: 22°C, mostly clear with 12 km/h westerly winds, 48% humidity. Optimal flight conditions, sir."
                respondAsJarvis(reply, actionType = "WEATHER", actionPayload = command.location, onResponseReady = onResponseReady)
            }

            is ParsedJarvisCommand.ClearHistory -> {
                repository.clearHistory()
                val reply = "Memory buffers purged, sir. Clean slate protocol completed."
                respondAsJarvis(reply, actionType = "CLEAR", onResponseReady = onResponseReady)
            }

            is ParsedJarvisCommand.GeneralQuery -> {
                // Call Gemini API or fallback to smart Jarvis offline reasoning with selected personality
                val recentMessages = messages.value.takeLast(6).map { it.sender to it.text }
                val aiResult = GeminiClient.askJarvis(
                    userPrompt = command.query,
                    conversationHistory = recentMessages,
                    systemInstruction = appearanceConfig.value.personality.promptPrefix
                )
                val reply = aiResult.getOrDefault("Understood, sir. Subroutines updated.")
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
