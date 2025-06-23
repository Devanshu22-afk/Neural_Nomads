package com.example.hackathon_app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hackathon_app.domain.TextToSpeechUseCase
import com.example.hackathon_app.domain.VoiceToTextUseCase
import com.example.hackathon_app.ui.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.hackathon_app.data.TranslationUseCaseImpl
import com.google.mlkit.nl.translate.TranslateLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.hackathon_app.data.TwilioVoiceManager
import com.example.hackathon_app.data.TwilioTokenService

class ChatViewModel(
    private val voiceToText: VoiceToTextUseCase,
    private val textToSpeech: TextToSpeechUseCase,
    private val translationUseCase: TranslationUseCaseImpl
) : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private val _selectedLanguageTag = MutableStateFlow("hi-IN")
    val selectedLanguageTag: StateFlow<String> = _selectedLanguageTag

    private val _targetLanguageTag = MutableStateFlow("en-US")
    val targetLanguageTag: StateFlow<String> = _targetLanguageTag

    // Twilio call state
    private val _isInCall = MutableStateFlow(false)
    val isInCall: StateFlow<Boolean> = _isInCall
    private val _callStatus = MutableStateFlow("")
    val callStatus: StateFlow<String> = _callStatus
    private var twilioVoiceManager: TwilioVoiceManager? = null

    fun onLanguageSelected(tag: String) {
        _selectedLanguageTag.value = tag
    }

    fun onTargetLanguageSelected(tag: String) {
        _targetLanguageTag.value = tag
    }

    fun onMicClicked() {
        val sourceLang = _selectedLanguageTag.value
        val targetLang = _targetLanguageTag.value
        if (_isListening.value) {
            voiceToText.stopListening()
            _isListening.value = false
        } else {
            _isListening.value = true
            voiceToText.startListening(
                languageTag = sourceLang,
                onResult = { text ->
                    CoroutineScope(Dispatchers.Main).launch {
                        val translated = try {
                            translationUseCase.translate(
                                text,
                                fromLangTag = sourceLang,
                                toLangTag = targetLang
                            )
                        } catch (e: Exception) {
                            text // fallback to original
                        }
                        _inputText.value = translated
                        _isListening.value = false
                    }
                },
                onError = { error ->
                    _isListening.value = false
                }
            )
        }
    }

    fun onSpeakClicked() {
        val text = _inputText.value
        val targetLang = _targetLanguageTag.value
        if (text.isNotBlank()) {
            textToSpeech.speak(text, targetLang)
            addMessage(text, isUser = true)
        }
    }

    fun onTextChanged(newText: String) {
        _inputText.value = newText
    }

    fun addMessage(text: String, isUser: Boolean) {
        _messages.value = listOf(ChatMessage(text, isUser)) + _messages.value
    }

    // Map language tag (e.g., hi-IN) to ML Kit language code (e.g., TranslateLanguage.HINDI)
    private fun mapToMlKitLang(tag: String): String = when(tag) {
        "hi-IN" -> TranslateLanguage.HINDI
        "en-US" -> TranslateLanguage.ENGLISH
        "ta-IN" -> TranslateLanguage.TAMIL
        "bn-IN" -> TranslateLanguage.BENGALI
        "gu-IN" -> TranslateLanguage.GUJARATI
        "mr-IN" -> TranslateLanguage.MARATHI
        else -> TranslateLanguage.ENGLISH
    }

    fun startCall(context: android.content.Context, to: String, tokenUrl: String) {
        _callStatus.value = "Fetching token..."
        viewModelScope.launch(Dispatchers.IO) {
            val tokenJson = TwilioTokenService.fetchToken(tokenUrl)
            android.util.Log.d("TwilioTokenDebug", "Token server response: $tokenJson")
            val accessToken = extractTokenFromJson(tokenJson)
            if (accessToken != null) {
                launch(Dispatchers.Main) {
                    twilioVoiceManager = TwilioVoiceManager(context)
                    twilioVoiceManager?.makeCall(
                        accessToken,
                        to,
                        onCallConnected = {
                            _isInCall.value = true
                            _callStatus.value = "Call connected"
                        },
                        onCallDisconnected = {
                            _isInCall.value = false
                            _callStatus.value = "Call ended"
                        }
                    )
                }
            } else {
                _callStatus.value = "Failed to fetch token"
            }
        }
    }

    fun endCall() {
        twilioVoiceManager?.disconnectCall()
        _isInCall.value = false
        _callStatus.value = "Call ended"
    }

    fun registerForIncomingCalls(context: android.content.Context, tokenUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val tokenJson = com.example.hackathon_app.data.TwilioTokenService.fetchToken(tokenUrl)
            android.util.Log.d("TwilioTokenDebug", "Token server response: $tokenJson")
            val accessToken = extractTokenFromJson(tokenJson)
            if (accessToken != null) {
                launch(Dispatchers.Main) {
                    twilioVoiceManager = com.example.hackathon_app.data.TwilioVoiceManager(context)
                    twilioVoiceManager?.registerForIncomingCalls(accessToken)
                }
            } else {
                _callStatus.value = "Failed to fetch token for incoming call registration"
            }
        }
    }

    private fun extractTokenFromJson(json: String?): String? {
        if (json == null) return null
        // If the response is a JSON object { "token": "..." }
        val regex = """"token"\s*:\s*"([^"]+)""".toRegex()
        val match = regex.find(json)
        return when {
            match != null -> match.groupValues.getOrNull(1)
            json.trim().isNotEmpty() -> json.trim() // fallback: treat as raw token
            else -> null
        }
    }

    override fun onCleared() {
        super.onCleared()
        textToSpeech.shutdown()
        if (voiceToText is com.example.hackathon_app.data.VoiceToTextUseCaseImpl) {
            voiceToText.destroy()
        }
    }
} 