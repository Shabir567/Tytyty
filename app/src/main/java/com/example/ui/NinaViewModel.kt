package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.ActionType
import com.example.model.AgentModule
import com.example.model.AppLanguage
import com.example.model.AssistantStatus
import com.example.model.AvatarType
import com.example.model.ChatMessage
import com.example.service.ContinuousSpeechRecognizer
import com.example.service.DeviceActionExecutor
import com.example.service.GeminiAssistantService
import com.example.service.NinaIntentHandler
import com.example.service.SpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NinaUiState(
    val avatar: AvatarType = AvatarType.DEFAULT,
    val status: AssistantStatus = AssistantStatus.IDLE,
    val language: AppLanguage = AppLanguage.URDU,
    val messages: List<ChatMessage> = emptyList(),
    val isMuted: Boolean = false,
    val isFlashlightOn: Boolean = false,
    val isAutoListening: Boolean = false,
    val audioRms: Float = 0f,
    val currentSpokenText: String? = null,
    val inputText: String = "",
    val activeDialerNumber: String = "9212345678",
    val activeMapsQuery: String = "Mumbai",
    val activeYouTubeQuery: String = "Neon Labs",
    val showAgentHubDialog: Boolean = false,
    val showDialerDialog: Boolean = false,
    val showYouTubeDialog: Boolean = false,
    val showMapsDialog: Boolean = false,
    val showAvatarPicker: Boolean = false,
    val showVisionDialog: Boolean = false,
    val agentModules: List<AgentModule> = defaultAgentModules()
)

fun defaultAgentModules() = listOf(
    AgentModule("nim", "NVIDIA NIM Core", "اینویڈیا این آئی ایم", "Low-latency neural model handoff & inference", true, "Core AI"),
    AgentModule("ram", "RAM Handoff Cache", "ریم ہینڈ آف", "Fast state transfer between brain and device UI", true, "System"),
    AgentModule("safe", "Safe Agent & Sandbox", "سیف ایجنٹ مانیٹر", "Automated multi-step task validation", true, "Security"),
    AgentModule("coding", "Auto Coding Agent", "آٹو کوڈنگ ایجنٹ", "Builds UI blocks, scripts and tools dynamically", true, "Development"),
    AgentModule("vision", "Screen & Camera Vision", "اسکرین اور کیمرہ ویژن", "Understands on-screen context and camera visuals", true, "Vision"),
    AgentModule("health", "Feature Health Monitor", "فیچر ہیلتھ مانیٹر", "Real-time diagnostic checks across phone modules", true, "Diagnostics"),
    AgentModule("repair", "Safe Repair Inbox", "سیف ریپیئر ان باکس", "Self-heals broken requests and interrupted workflows", true, "System"),
    AgentModule("call_assist", "Call & Message Hub", "کال و میسج اسسٹنٹ", "Direct dialer and WhatsApp action coordinator", true, "Communication")
)

class NinaViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(NinaUiState())
    val uiState: StateFlow<NinaUiState> = _uiState.asStateFlow()

    private val geminiService = GeminiAssistantService()

    val continuousSpeechRecognizer = ContinuousSpeechRecognizer(
        context = application,
        onListeningStateChanged = { isListening ->
            _uiState.value = _uiState.value.copy(
                status = if (isListening) AssistantStatus.LISTENING
                         else if (_uiState.value.status == AssistantStatus.LISTENING) AssistantStatus.IDLE
                         else _uiState.value.status
            )
        },
        onSpeechRecognized = { spokenText ->
            submitUserMessage(application, spokenText)
        },
        onRmsLevel = { rms ->
            _uiState.value = _uiState.value.copy(audioRms = rms)
        }
    )

    val speechManager = SpeechManager(
        context = application,
        onSpeechStatusChanged = { isSpeaking ->
            _uiState.value = _uiState.value.copy(
                status = if (isSpeaking) AssistantStatus.SPEAKING else AssistantStatus.IDLE
            )
            if (isSpeaking) {
                continuousSpeechRecognizer.onAssistantStartedSpeaking()
            }
        },
        onSpeechFinished = {
            _uiState.value = _uiState.value.copy(currentSpokenText = null)
            continuousSpeechRecognizer.onAssistantFinishedSpeaking()
        }
    )

    fun speakAssistant(text: String, langCode: String = _uiState.value.language.code) {
        _uiState.value = _uiState.value.copy(currentSpokenText = text)
        speechManager.speak(text, langCode)
    }

    init {
        val welcomeUrdu = "السلام علیکم! میں نینا ہوں، آپ کی ذاتی اور ذہین اسسٹنٹ۔ مائیک کو ایک بار آن کر دیں، میں مستقل سنتی رہوں گی اور دوبارہ شروع کرنے کی ضرورت نہیں پڑے گی!"
        _uiState.value = _uiState.value.copy(
            messages = listOf(
                ChatMessage(
                    text = welcomeUrdu,
                    isUser = false
                )
            )
        )
    }

    fun onInputTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun toggleMute() {
        val muted = speechManager.toggleMute()
        _uiState.value = _uiState.value.copy(isMuted = muted)
    }

    fun startAutoListening() {
        _uiState.value = _uiState.value.copy(isAutoListening = true)
        continuousSpeechRecognizer.startContinuousListening(_uiState.value.language.code)
    }

    fun stopAutoListening() {
        _uiState.value = _uiState.value.copy(isAutoListening = false)
        continuousSpeechRecognizer.stopContinuousListening()
    }

    fun toggleAutoListening(): Boolean {
        val next = continuousSpeechRecognizer.toggleContinuousListening(_uiState.value.language.code)
        _uiState.value = _uiState.value.copy(isAutoListening = next)
        return next
    }

    fun setLanguage(lang: AppLanguage) {
        _uiState.value = _uiState.value.copy(language = lang)
        val greetingMsg = ChatMessage(
            text = lang.greeting,
            isUser = false
        )
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + greetingMsg
        )
        speakAssistant(lang.greeting, lang.code)
        if (_uiState.value.isAutoListening) {
            continuousSpeechRecognizer.startContinuousListening(lang.code)
        }
    }

    fun setAvatar(avatar: AvatarType) {
        _uiState.value = _uiState.value.copy(
            avatar = avatar,
            showAvatarPicker = false
        )
        val text = "لو بابو! میں نے اپنا اوتار بدل کر '${avatar.titleUrdu}' کر لیا ہے۔ کیسی لگ رہی ہوں؟"
        addAssistantMessage(text)
        speakAssistant(text, _uiState.value.language.code)
    }

    fun submitUserMessage(context: Context, explicitText: String? = null) {
        val query = explicitText ?: _uiState.value.inputText
        if (query.isBlank()) return

        _uiState.value = _uiState.value.copy(inputText = "")

        val userMessage = ChatMessage(
            text = query,
            isUser = true
        )
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            status = AssistantStatus.PROCESSING
        )

        // 1. First test if local intent handler matches (like YouTube, Maps, Dialer, etc.)
        val intentResult = NinaIntentHandler.parseCommand(query)
        if (intentResult != null) {
            viewModelScope.launch {
                if (intentResult.avatarToSwitch != null) {
                    _uiState.value = _uiState.value.copy(avatar = intentResult.avatarToSwitch)
                }

                val assistantMessage = ChatMessage(
                    text = intentResult.displayText,
                    isUser = false,
                    actionType = intentResult.actionType,
                    actionPayload = intentResult.actionPayload
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + assistantMessage
                )

                // Speak response with live subtitles
                speakAssistant(intentResult.speechText, _uiState.value.language.code)

                // Execute action if needed
                if (intentResult.shouldExecuteImmediately && intentResult.actionType != null) {
                    executeQuickAction(intentResult.actionType, context, intentResult.actionPayload)
                }
            }
            return
        }

        // 2. Query Gemini AI for general intelligence, answers, coding, jokes, writing
        viewModelScope.launch {
            try {
                val geminiReply = geminiService.queryGemini(query, _uiState.value.language.code)
                addAssistantMessage(geminiReply)
                speakAssistant(geminiReply, _uiState.value.language.code)
            } catch (e: Exception) {
                val errorMsg = "معذرت، رابطہ میں کچھ تاخیر ہے مگر میں آپ کی بات سمجھ رہی ہوں۔"
                addAssistantMessage(errorMsg)
                speakAssistant(errorMsg, _uiState.value.language.code)
            }
        }
    }

    private fun addAssistantMessage(text: String) {
        val assistantMessage = ChatMessage(
            text = text,
            isUser = false
        )
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + assistantMessage,
            status = AssistantStatus.IDLE
        )
    }

    fun executeQuickAction(actionType: ActionType, context: Context, payload: String? = null) {
        when (actionType) {
            ActionType.YOUTUBE -> {
                val query = payload ?: _uiState.value.activeYouTubeQuery
                DeviceActionExecutor.launchYouTube(context, query)
            }
            ActionType.MAPS -> {
                val destination = payload ?: _uiState.value.activeMapsQuery
                DeviceActionExecutor.launchMaps(context, destination)
            }
            ActionType.DIALER -> {
                val number = payload ?: _uiState.value.activeDialerNumber
                DeviceActionExecutor.launchDialer(context, number)
            }
            ActionType.WHATSAPP -> {
                DeviceActionExecutor.launchWhatsApp(context, payload)
            }
            ActionType.CAMERA -> {
                DeviceActionExecutor.launchCamera(context)
            }
            ActionType.FLASHLIGHT -> {
                val state = DeviceActionExecutor.toggleFlashlight(context)
                _uiState.value = _uiState.value.copy(isFlashlightOn = state)
            }
            ActionType.LANGUAGES_DEMO -> {
                submitUserMessage(context, "10 زبانوں میں بات کرو")
            }
            ActionType.AVATAR_SWITCH -> {
                _uiState.value = _uiState.value.copy(showAvatarPicker = true)
            }
        }
    }

    fun toggleAgentModule(moduleId: String) {
        val updated = _uiState.value.agentModules.map {
            if (it.id == moduleId) it.copy(isEnabled = !it.isEnabled) else it
        }
        _uiState.value = _uiState.value.copy(agentModules = updated)
    }

    fun setListening(listening: Boolean) {
        _uiState.value = _uiState.value.copy(
            status = if (listening) AssistantStatus.LISTENING else AssistantStatus.IDLE
        )
    }

    fun setDialog(
        agentHub: Boolean? = null,
        dialer: Boolean? = null,
        youtube: Boolean? = null,
        maps: Boolean? = null,
        avatar: Boolean? = null,
        vision: Boolean? = null
    ) {
        _uiState.value = _uiState.value.copy(
            showAgentHubDialog = agentHub ?: _uiState.value.showAgentHubDialog,
            showDialerDialog = dialer ?: _uiState.value.showDialerDialog,
            showYouTubeDialog = youtube ?: _uiState.value.showYouTubeDialog,
            showMapsDialog = maps ?: _uiState.value.showMapsDialog,
            showAvatarPicker = avatar ?: _uiState.value.showAvatarPicker,
            showVisionDialog = vision ?: _uiState.value.showVisionDialog
        )
    }

    fun updateDialerNumber(num: String) {
        _uiState.value = _uiState.value.copy(activeDialerNumber = num)
    }

    fun updateMapsQuery(dest: String) {
        _uiState.value = _uiState.value.copy(activeMapsQuery = dest)
    }

    fun updateYouTubeQuery(q: String) {
        _uiState.value = _uiState.value.copy(activeYouTubeQuery = q)
    }

    override fun onCleared() {
        super.onCleared()
        continuousSpeechRecognizer.destroy()
        speechManager.destroy()
    }
}
