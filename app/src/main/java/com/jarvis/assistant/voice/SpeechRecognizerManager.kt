package com.jarvis.assistant.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

/**
 * Thin wrapper around Android's on-device/system SpeechRecognizer.
 * Exposes simple callbacks instead of the verbose RecognitionListener
 * interface. Uses the device's built-in speech service — no audio
 * is sent to any server controlled by this app.
 */
class SpeechRecognizerManager(
    private val context: Context,
    private val onPartial: (String) -> Unit = {},
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit = {},
    private val onListeningChanged: (Boolean) -> Unit = {}
) {
    private var recognizer: SpeechRecognizer? = null

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Spracherkennung ist auf diesem Gerät nicht verfügbar.")
            return
        }
        stopListening()
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { onListeningChanged(true) }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { onListeningChanged(false) }

                override fun onError(error: Int) {
                    onListeningChanged(false)
                    onError(errorText(error))
                }

                override fun onResults(results: Bundle?) {
                    onListeningChanged(false)
                    val text = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull().orEmpty()
                    if (text.isNotBlank()) onResult(text)
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val text = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull().orEmpty()
                    if (text.isNotBlank()) onPartial(text)
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.GERMANY.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }
        recognizer?.startListening(intent)
    }

    fun stopListening() {
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
    }

    private fun errorText(code: Int): String = when (code) {
        SpeechRecognizer.ERROR_NO_MATCH -> "Ich habe dich nicht verstanden."
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Ich habe nichts gehört."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Mikrofon-Berechtigung fehlt."
        SpeechRecognizer.ERROR_NETWORK -> "Netzwerkfehler bei der Spracherkennung."
        else -> "Es gab ein Problem mit der Spracherkennung."
    }
}
