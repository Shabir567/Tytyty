package com.example

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.model.ActionType
import com.example.model.AssistantStatus
import com.example.ui.NinaUiState
import com.example.ui.NinaViewModel
import com.example.ui.components.NinaAvatarView
import com.example.ui.components.NinaChatDrawer
import com.example.ui.components.NinaHeader
import com.example.ui.components.NinaInputBar
import com.example.ui.components.NinaQuickActionsGrid
import com.example.ui.dialogs.AgentHubDialog
import com.example.ui.dialogs.AvatarPickerDialog
import com.example.ui.dialogs.DialerDialog
import com.example.ui.dialogs.MapsDialog
import com.example.ui.dialogs.ScreenVisionDialog
import com.example.ui.dialogs.YouTubeSearchDialog
import com.example.ui.theme.DarkBg
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: NinaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsState()
                NinaMainScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }
        }
    }
}

@Composable
fun NinaMainScreen(
    viewModel: NinaViewModel,
    uiState: NinaUiState
) {
    val context = LocalContext.current

    // Fallback Dialog Speech Recognizer if embedded recognizer has transient issues
    val fallbackSpeechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.setListening(false)
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenResults = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = spokenResults?.firstOrNull()
            if (!recognizedText.isNullOrBlank()) {
                viewModel.submitUserMessage(context, recognizedText)
            }
        }
    }

    // Audio Permission Launcher
    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startAutoListening()
            Toast.makeText(context, "مائیک آن ہو گیا ہے! اب مسلسل سنتی رہے گی", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "آواز کی شناخت کے لیے مائیکروفون کی اجازت درکار ہے", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleMicToggle() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            val isNowActive = viewModel.toggleAutoListening()
            if (isNowActive) {
                Toast.makeText(context, "🎙️ آٹو لسننگ آن ہے! اب بار بار دبانے کی ضرورت نہیں", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "مائیک بند کر دیا گیا ہے", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Fallback for emulators without speech recognizer service
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, uiState.language.code)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "نینا سن رہی ہے، بولیے...")
                }
                viewModel.setListening(true)
                fallbackSpeechLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "اس ڈیوائس پر اسپیچ ریکگنیشن سروس موجود نہیں", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Auto-prompt permission on initial launch if needed
    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            viewModel.startAutoListening()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBg,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBg)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Top Status & System Header
                NinaHeader(
                    currentLanguage = uiState.language,
                    isMuted = uiState.isMuted,
                    isFlashlightOn = uiState.isFlashlightOn,
                    onLanguageSelected = { viewModel.setLanguage(it) },
                    onToggleMute = { viewModel.toggleMute() },
                    onToggleFlashlight = { viewModel.executeQuickAction(ActionType.FLASHLIGHT, context) },
                    onOpenAgentHub = { viewModel.setDialog(agentHub = true) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )

                // 2. Central Animated Avatar (Nina with glowing halo & audio visualizer)
                NinaAvatarView(
                    avatar = uiState.avatar,
                    status = uiState.status,
                    currentSpokenText = uiState.currentSpokenText,
                    onAvatarClick = { viewModel.setDialog(avatar = true) },
                    modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                )

                // 3. Quick Actions Tray (YouTube, Maps, Dialer, WhatsApp, Camera, etc.)
                NinaQuickActionsGrid(
                    onActionClick = { actionType ->
                        viewModel.executeQuickAction(actionType, context)
                    },
                    onOpenYouTubeDialog = { viewModel.setDialog(youtube = true) },
                    onOpenMapsDialog = { viewModel.setDialog(maps = true) },
                    onOpenDialerDialog = { viewModel.setDialog(dialer = true) },
                    onOpenAvatarPicker = { viewModel.setDialog(avatar = true) },
                    onOpenVisionDialog = { viewModel.setDialog(vision = true) }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 4. Expandable Live Chat & Interaction History
                NinaChatDrawer(
                    messages = uiState.messages,
                    onSpeakMessage = { text ->
                        viewModel.speakAssistant(text, uiState.language.code)
                    },
                    onExecuteAction = { actionType, payload ->
                        viewModel.executeQuickAction(actionType, context, payload)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                )

                // 5. Bottom Voice & Text Input Bar with Auto-Listening
                NinaInputBar(
                    inputText = uiState.inputText,
                    onInputTextChanged = { viewModel.onInputTextChanged(it) },
                    onSendMessage = { viewModel.submitUserMessage(context) },
                    onToggleAutoListening = { handleMicToggle() },
                    isListening = uiState.status == AssistantStatus.LISTENING,
                    isAutoListening = uiState.isAutoListening,
                    onSelectSuggestion = { suggestion ->
                        viewModel.submitUserMessage(context, suggestion)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Dialogs
            if (uiState.showAgentHubDialog) {
                AgentHubDialog(
                    modules = uiState.agentModules,
                    onToggleModule = { viewModel.toggleAgentModule(it) },
                    onDismiss = { viewModel.setDialog(agentHub = false) }
                )
            }

            if (uiState.showDialerDialog) {
                DialerDialog(
                    currentNumber = uiState.activeDialerNumber,
                    onNumberChanged = { viewModel.updateDialerNumber(it) },
                    onCallClick = { number ->
                        viewModel.executeQuickAction(ActionType.DIALER, context, number)
                    },
                    onDismiss = { viewModel.setDialog(dialer = false) }
                )
            }

            if (uiState.showYouTubeDialog) {
                YouTubeSearchDialog(
                    query = uiState.activeYouTubeQuery,
                    onQueryChanged = { viewModel.updateYouTubeQuery(it) },
                    onPlay = { query ->
                        viewModel.executeQuickAction(ActionType.YOUTUBE, context, query)
                    },
                    onDismiss = { viewModel.setDialog(youtube = false) }
                )
            }

            if (uiState.showMapsDialog) {
                MapsDialog(
                    destination = uiState.activeMapsQuery,
                    onDestinationChanged = { viewModel.updateMapsQuery(it) },
                    onNavigate = { destination ->
                        viewModel.executeQuickAction(ActionType.MAPS, context, destination)
                    },
                    onDismiss = { viewModel.setDialog(maps = false) }
                )
            }

            if (uiState.showAvatarPicker) {
                AvatarPickerDialog(
                    currentAvatar = uiState.avatar,
                    onAvatarSelected = { avatar ->
                        viewModel.setAvatar(avatar)
                    },
                    onDismiss = { viewModel.setDialog(avatar = false) }
                )
            }

            if (uiState.showVisionDialog) {
                ScreenVisionDialog(
                    onExplainScreen = {
                        viewModel.submitUserMessage(context, "اسکرین پر کیا ہے اور کون سے ایجنٹس فعال ہیں؟")
                    },
                    onLaunchCameraScan = {
                        viewModel.executeQuickAction(ActionType.CAMERA, context)
                    },
                    onDismiss = { viewModel.setDialog(vision = false) }
                )
            }
        }
    }
}
