package com.example.hackathon_app.data

import android.content.Context
import android.speech.tts.TextToSpeech
import com.example.hackathon_app.domain.TextToSpeechUseCase
import java.util.Locale

class TextToSpeechUseCaseImpl(private val context: Context) : TextToSpeechUseCase {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context) { status ->
            isInitialized = status == TextToSpeech.SUCCESS
        }
    }

    override fun speak(text: String, languageTag: String) {
        if (!isInitialized) return
        val locale = Locale.forLanguageTag(languageTag)
        tts?.language = locale
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun shutdown() {
        tts?.shutdown()
    }
} 