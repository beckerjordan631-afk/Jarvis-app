package com.jarvis.assistant.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.UUID

/**
 * Wrapper around Android's on-device TextToSpeech engine. Speech is
 * synthesized locally by the OS TTS engine — nothing is uploaded.
 */
class TextToSpeechManager(
    context: Context,
    private val onSpeakingChanged: (Boolean) -> Unit = {}
) {
    private var tts: TextToSpeech? = null
    private var ready = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.GERMANY
                ready = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) { onSpeakingChanged(true) }
                    override fun onDone(utteranceId: String?) { onSpeakingChanged(false) }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) { onSpeakingChanged(false) }
                })
            }
        }
    }

    fun speak(text: String) {
        if (!ready) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
