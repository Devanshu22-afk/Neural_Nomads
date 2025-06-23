package com.example.hackathon_app.domain
 
interface VoiceToTextUseCase {
    fun startListening(languageTag: String, onResult: (String) -> Unit, onError: (String) -> Unit)
    fun stopListening()
} 