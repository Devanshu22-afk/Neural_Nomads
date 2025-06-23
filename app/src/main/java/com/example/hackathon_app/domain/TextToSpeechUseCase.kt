package com.example.hackathon_app.domain
 
interface TextToSpeechUseCase {
    fun speak(text: String, languageTag: String)
    fun shutdown()
} 