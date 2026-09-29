package com.example.service

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class SpeechManager(
    private val context: Context,
    private val onSpeechStatusChanged: (isSpeaking: Boolean) -> Unit,
    private val onSpeechFinished: () -> Unit = {}
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var isMuted = false
    private var toneGenerator: ToneGenerator? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (e: Exception) {
            Log.e("SpeechManager", "Could not init ToneGenerator: ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            setupProgressListener()
            setOptimalLanguage(Locale("ur", "PK"))
        } else {
            Log.e("SpeechManager", "Failed to initialize TextToSpeech")
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                onSpeechStatusChanged(true)
            }

            override fun onDone(utteranceId: String?) {
                onSpeechStatusChanged(false)
                onSpeechFinished()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                onSpeechStatusChanged(false)
                onSpeechFinished()
            }
        })
    }

    private fun setOptimalLanguage(preferredLocale: Locale) {
        if (!isInitialized) return
        try {
            val result = tts?.setLanguage(preferredLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                val hiResult = tts?.setLanguage(Locale("hi", "IN"))
                if (hiResult == TextToSpeech.LANG_MISSING_DATA || hiResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.US)
                }
            }
        } catch (e: Exception) {
            tts?.setLanguage(Locale.US)
        }
    }

    fun speak(text: String, languageCode: String = "ur") {
        if (isMuted || text.isBlank()) {
            onSpeechFinished()
            return
        }

        // Play gentle audio feedback beep so emulator outputs audio even if TTS language is downloading
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
        } catch (e: Exception) {
            // Ignore
        }

        if (!isInitialized) {
            onSpeechStatusChanged(true)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                onSpeechStatusChanged(false)
                onSpeechFinished()
            }, 1500)
            return
        }

        // Stop any current utterance
        stop()

        when (languageCode.lowercase()) {
            "ur" -> setOptimalLanguage(Locale("ur", "PK"))
            "hi" -> tts?.setLanguage(Locale("hi", "IN"))
            "en" -> tts?.setLanguage(Locale.US)
            "es" -> tts?.setLanguage(Locale("es", "ES"))
            "fr" -> tts?.setLanguage(Locale.FRANCE)
            "de" -> tts?.setLanguage(Locale.GERMANY)
            "ja" -> tts?.setLanguage(Locale.JAPAN)
            "ko" -> tts?.setLanguage(Locale.KOREA)
            "ru" -> tts?.setLanguage(Locale("ru", "RU"))
            else -> setOptimalLanguage(Locale("ur", "PK"))
        }

        tts?.setPitch(1.05f)
        tts?.setSpeechRate(0.95f)

        val cleanSpeech = text
            .replace(Regex("[*#_`~>|]"), "")
            .replace(Regex("https?://\\S+"), "")
            .trim()

        val utteranceId = "NINA_SPEECH_${System.currentTimeMillis()}"
        val result = tts?.speak(cleanSpeech, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        if (result != TextToSpeech.SUCCESS) {
            // Fallback retry with US locale
            tts?.setLanguage(Locale.US)
            tts?.speak(cleanSpeech, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    fun stop() {
        if (isInitialized) {
            tts?.stop()
            onSpeechStatusChanged(false)
        }
    }

    fun toggleMute(): Boolean {
        isMuted = !isMuted
        if (isMuted) {
            stop()
        }
        return isMuted
    }

    fun isMuted(): Boolean = isMuted

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        toneGenerator?.release()
        toneGenerator = null
        isInitialized = false
    }
}
