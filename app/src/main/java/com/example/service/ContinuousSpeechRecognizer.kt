package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class ContinuousSpeechRecognizer(
    private val context: Context,
    private val onListeningStateChanged: (isListening: Boolean) -> Unit,
    private val onSpeechRecognized: (text: String) -> Unit,
    private val onRmsLevel: (rms: Float) -> Unit = {}
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isAutoListeningEnabled = false
    private var isCurrentlyListening = false
    private var isMutedOrSpeaking = false
    private var currentLanguageCode = "ur"

    init {
        initRecognizer()
    }

    private fun initRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.w("ContinuousSpeech", "Speech recognition not available on this device")
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createListener())
            }
        } catch (e: Exception) {
            Log.e("ContinuousSpeech", "Error initializing SpeechRecognizer: ${e.message}")
        }
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isCurrentlyListening = true
                onListeningStateChanged(true)
            }

            override fun onBeginningOfSpeech() {
                isCurrentlyListening = true
                onListeningStateChanged(true)
            }

            override fun onRmsChanged(rmsdB: Float) {
                onRmsLevel(rmsdB)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isCurrentlyListening = false
                onListeningStateChanged(false)
            }

            override fun onError(error: Int) {
                isCurrentlyListening = false
                onListeningStateChanged(false)
                Log.d("ContinuousSpeech", "SpeechRecognizer error: $error")

                // If auto-listening is enabled and Nina is not speaking, restart listening after a brief delay
                if (isAutoListeningEnabled && !isMutedOrSpeaking) {
                    mainHandler.removeCallbacksAndMessages(null)
                    mainHandler.postDelayed({
                        restartIfAllowed()
                    }, 600)
                }
            }

            override fun onResults(results: Bundle?) {
                isCurrentlyListening = false
                onListeningStateChanged(false)

                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognizedText = matches?.firstOrNull()?.trim()

                if (!recognizedText.isNullOrBlank()) {
                    onSpeechRecognized(recognizedText)
                } else if (isAutoListeningEnabled && !isMutedOrSpeaking) {
                    mainHandler.postDelayed({
                        restartIfAllowed()
                    }, 400)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    fun startContinuousListening(languageCode: String = "ur") {
        isAutoListeningEnabled = true
        currentLanguageCode = languageCode
        restartIfAllowed()
    }

    fun stopContinuousListening() {
        isAutoListeningEnabled = false
        mainHandler.removeCallbacksAndMessages(null)
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
        } catch (e: Exception) {
            Log.e("ContinuousSpeech", "Error stopping: ${e.message}")
        }
        isCurrentlyListening = false
        onListeningStateChanged(false)
    }

    fun toggleContinuousListening(languageCode: String = "ur"): Boolean {
        return if (isAutoListeningEnabled) {
            stopContinuousListening()
            false
        } else {
            startContinuousListening(languageCode)
            true
        }
    }

    fun onAssistantStartedSpeaking() {
        isMutedOrSpeaking = true
        mainHandler.removeCallbacksAndMessages(null)
        try {
            speechRecognizer?.cancel()
        } catch (e: Exception) {
            Log.e("ContinuousSpeech", "Error cancelling on speak: ${e.message}")
        }
        isCurrentlyListening = false
        onListeningStateChanged(false)
    }

    fun onAssistantFinishedSpeaking() {
        isMutedOrSpeaking = false
        if (isAutoListeningEnabled) {
            // Automatically resume listening immediately after Nina finishes speaking!
            mainHandler.removeCallbacksAndMessages(null)
            mainHandler.postDelayed({
                restartIfAllowed()
            }, 350)
        }
    }

    private fun restartIfAllowed() {
        if (!isAutoListeningEnabled || isMutedOrSpeaking) return

        try {
            if (speechRecognizer == null) {
                initRecognizer()
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguageCode)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e("ContinuousSpeech", "Restart listening failed: ${e.message}")
            // Re-init and retry once
            initRecognizer()
        }
    }

    fun isAutoListening(): Boolean = isAutoListeningEnabled

    fun destroy() {
        stopContinuousListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
