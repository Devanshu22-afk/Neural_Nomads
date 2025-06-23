package com.example.hackathon_app.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.hackathon_app.domain.VoiceToTextUseCase
import java.util.Locale
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast

class VoiceToTextUseCaseImpl(private val context: Context) : VoiceToTextUseCase {
    private var speechRecognizer: SpeechRecognizer? = null
    private var resultCallback: ((String) -> Unit)? = null
    private var errorCallback: ((String) -> Unit)? = null

    override fun startListening(languageTag: String, onResult: (String) -> Unit, onError: (String) -> Unit) {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        }
        resultCallback = onResult
        errorCallback = onError

        // Check for internet connection for non-English/Hindi
        if (languageTag != "en-US" && languageTag != "hi-IN") {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork
            val capabilities = cm.getNetworkCapabilities(network)
            val hasInternet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            if (!hasInternet) {
                Toast.makeText(context, "Internet connection required for this language.", Toast.LENGTH_LONG).show()
                onError("Internet connection required for this language.")
                return
            }
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageTag)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
        }
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("VoiceToText", "onReadyForSpeech")
            }
            override fun onBeginningOfSpeech() {
                Log.d("VoiceToText", "onBeginningOfSpeech")
            }
            override fun onRmsChanged(rmsdB: Float) {
                Log.d("VoiceToText", "onRmsChanged: $rmsdB")
            }
            override fun onBufferReceived(buffer: ByteArray?) {
                Log.d("VoiceToText", "onBufferReceived")
            }
            override fun onEndOfSpeech() {
                Log.d("VoiceToText", "onEndOfSpeech")
            }
            override fun onError(error: Int) {
                Log.e("VoiceToText", "onError: $error")
                val message = when (error) {
                    1 -> "Audio error. Try again."
                    2 -> "Client error. Try again."
                    3 -> "Insufficient permissions."
                    5 -> "No match. Please speak clearly."
                    7 -> "Recognizer busy. Try again."
                    8 -> "Speech recognizer not available."
                    9 -> "Language not supported on this device."
                    else -> "Speech recognition error: $error"
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                errorCallback?.invoke(message)
            }
            override fun onResults(results: Bundle?) {
                Log.d("VoiceToText", "onResults: $results")
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                resultCallback?.invoke(matches?.firstOrNull() ?: "")
            }
            override fun onPartialResults(partialResults: Bundle?) {
                Log.d("VoiceToText", "onPartialResults: $partialResults")
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    resultCallback?.invoke(matches[0])
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {
                Log.d("VoiceToText", "onEvent: $eventType")
            }
        })
        speechRecognizer?.startListening(intent)
    }

    override fun stopListening() {
        speechRecognizer?.stopListening()
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
} 