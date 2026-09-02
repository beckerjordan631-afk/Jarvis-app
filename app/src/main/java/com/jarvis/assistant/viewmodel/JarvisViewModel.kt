package com.jarvis.assistant.viewmodel

import android.app.Application
import android.content.Intent
import android.os.BatteryManager
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.assistant.data.AppDatabase
import com.jarvis.assistant.data.ChatEntity
import com.jarvis.assistant.skills.JarvisIntent
import com.jarvis.assistant.skills.SkillRegistry
import com.jarvis.assistant.voice.SpeechRecognizerManager
import com.jarvis.assistant.voice.TextToSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class JarvisState { IDLE, LISTENING, THINKING, SPEAKING }

data class ChatMessage(val isUser: Boolean, val text: String)

data class JarvisUiState(
    val state: JarvisState = JarvisState.IDLE,
    val messages: List<ChatMessage> = emptyList(),
    val partialTranscript: String = "",
    val batteryLevel: Int = 0,
    val memoryCount: Int = 0,
    val lastCommand: String = "—",
    val errorMessage: String? = null
)

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val skillRegistry = SkillRegistry(application)

    private val _uiState = MutableStateFlow(JarvisUiState())
    val uiState: StateFlow<JarvisUiState> = _uiState.asStateFlow()

    private var speechManager: SpeechRecognizerManager? = null
    private val ttsManager = TextToSpeechManager(application) { speaking ->
        _uiState.value = _uiState.value.copy(
            state = if (speaking) JarvisState.SPEAKING else JarvisState.IDLE
        )
    }

    init {
        refreshBattery()
        viewModelScope.launch {
            db.memoryDao().observeAll().collect { list ->
                _uiState.value = _uiState.value.copy(memoryCount = list.size)
            }
        }
    }

    fun refreshBattery() {
        val app = getApplication<Application>()
        val bm = app.getSystemService(Application.BATTERY_SERVICE) as BatteryManager
        val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        _uiState.value = _uiState.value.copy(batteryLevel = level)
    }

    fun startListening() {
        val app = getApplication<Application>()
        _uiState.value = _uiState.value.copy(state = JarvisState.LISTENING, partialTranscript = "", errorMessage = null)
        speechManager = SpeechRecognizerManager(
            context = app,
            onPartial = { partial ->
                _uiState.value = _uiState.value.copy(partialTranscript = partial)
            },
            onResult = { text -> handleUserUtterance(text) },
            onError = { message ->
                _uiState.value = _uiState.value.copy(state = JarvisState.IDLE, errorMessage = message)
            },
            onListeningChanged = { listening ->
                if (!listening && _uiState.value.state == JarvisState.LISTENING) {
                    _uiState.value = _uiState.value.copy(state = JarvisState.THINKING)
                }
            }
        )
        speechManager?.startListening()
    }

    fun stopListening() {
        speechManager?.stopListening()
        _uiState.value = _uiState.value.copy(state = JarvisState.IDLE)
    }

    /** Called both from voice results and from the text chat input. */
    fun handleUserUtterance(text: String) {
        appendMessage(ChatMessage(isUser = true, text = text))
        _uiState.value = _uiState.value.copy(
            state = JarvisState.THINKING,
            lastCommand = text,
            partialTranscript = ""
        )
        viewModelScope.launch {
            db.chatDao().insert(ChatEntity(role = "user", text = text))
            val result = skillRegistry.route(text, db)
            db.chatDao().insert(ChatEntity(role = "jarvis", text = result.spokenText))
            appendMessage(ChatMessage(isUser = false, text = result.spokenText))
            result.intentAction?.let { fireIntent(it) }
            ttsManager.speak(result.spokenText)
            // state flips to SPEAKING via the TTS progress listener, then back to IDLE onDone.
        }
    }

    private fun appendMessage(message: ChatMessage) {
        _uiState.value = _uiState.value.copy(messages = _uiState.value.messages + message)
    }

    private fun fireIntent(action: JarvisIntent) {
        val app = getApplication<Application>()
        try {
            when (action) {
                is JarvisIntent.WebSearch -> {
                    val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                        putExtra(android.app.SearchManager.QUERY, action.query)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    app.startActivity(intent)
                }
                is JarvisIntent.OpenApp -> {
                    val pm = app.packageManager
                    val match = pm.getInstalledApplications(0).firstOrNull { appInfo ->
                        pm.getApplicationLabel(appInfo).toString().contains(action.appName, ignoreCase = true)
                    }
                    val launchIntent = match?.let { pm.getLaunchIntentForPackage(it.packageName) }
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        app.startActivity(launchIntent)
                    } else {
                        appendMessage(ChatMessage(isUser = false, text = "App \"${action.appName}\" wurde nicht gefunden."))
                    }
                }
                is JarvisIntent.OpenSettings -> {
                    val intent = Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    app.startActivity(intent)
                }
            }
        } catch (e: Exception) {
            appendMessage(ChatMessage(isUser = false, text = "Aktion konnte nicht ausgeführt werden."))
        }
    }

    override fun onCleared() {
        speechManager?.stopListening()
        ttsManager.shutdown()
        super.onCleared()
    }
}
