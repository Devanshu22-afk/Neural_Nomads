package com.example.hackathon_app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hackathon_app.data.TextToSpeechUseCaseImpl
import com.example.hackathon_app.data.VoiceToTextUseCaseImpl
import com.example.hackathon_app.data.TranslationUseCaseImpl
import com.example.hackathon_app.presentation.ChatViewModel
import com.example.hackathon_app.ui.ChatView
import com.example.hackathon_app.ui.MicButton
import com.example.hackathon_app.ui.TextInputWithSpeakButton
import com.example.hackathon_app.ui.theme.Hackathon_AppTheme
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.hackathon_app.ui.ChatMessage
import com.example.hackathon_app.ui.LanguageSelector
import com.example.hackathon_app.ui.supportedLanguages
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth

class MainActivity : ComponentActivity() {
    private lateinit var chatViewModel: ChatViewModel
    private val languageTag = "hi-IN" // Default to Hindi

    private fun checkAndRequestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            val requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (!isGranted) {
                    Toast.makeText(this, "Microphone permission is required for speech recognition. Please enable it in app settings.", Toast.LENGTH_LONG).show()
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                }
            }
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val voiceToText = VoiceToTextUseCaseImpl(this)
        val textToSpeech = TextToSpeechUseCaseImpl(this)
        val translationUseCase = TranslationUseCaseImpl(this)
        translationUseCase.preDownloadAllModels {
            Toast.makeText(this, "All translation models downloaded!", Toast.LENGTH_SHORT).show()
        }
        chatViewModel = ChatViewModel(voiceToText, textToSpeech, translationUseCase)

        checkAndRequestAudioPermission()

        setContent {
            AccessibilityApp(
                messages = chatViewModel.messages.collectAsState().value,
                inputText = chatViewModel.inputText.collectAsState().value,
                isListening = chatViewModel.isListening.collectAsState().value,
                selectedLanguageTag = chatViewModel.selectedLanguageTag.collectAsState().value,
                onLanguageSelected = { chatViewModel.onLanguageSelected(it) },
                targetLanguageTag = chatViewModel.targetLanguageTag.collectAsState().value,
                onTargetLanguageSelected = { chatViewModel.onTargetLanguageSelected(it) },
                onMicClicked = { chatViewModel.onMicClicked() },
                onSpeakClicked = { chatViewModel.onSpeakClicked() },
                onTextChanged = { chatViewModel.onTextChanged(it) }
            )
        }
    }
}

@Composable
fun AccessibilityApp(
    messages: List<ChatMessage>,
    inputText: String,
    isListening: Boolean,
    selectedLanguageTag: String,
    onLanguageSelected: (String) -> Unit,
    targetLanguageTag: String,
    onTargetLanguageSelected: (String) -> Unit,
    onMicClicked: () -> Unit,
    onSpeakClicked: () -> Unit,
    onTextChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.material3.Text(
            text = "Convert From",
            modifier = Modifier.align(Alignment.Start).padding(bottom = 4.dp)
        )
        LanguageSelector(
            selectedLanguageTag = selectedLanguageTag,
            onLanguageSelected = onLanguageSelected,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )
        androidx.compose.material3.Text(
            text = "Convert To",
            modifier = Modifier.align(Alignment.Start).padding(bottom = 4.dp)
        )
        LanguageSelector(
            selectedLanguageTag = targetLanguageTag,
            onLanguageSelected = onTargetLanguageSelected,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )
        ChatView(messages = messages, modifier = Modifier.weight(1f))
        Row(modifier = Modifier.padding(8.dp)) {
            MicButton(
                onClick = onMicClicked,
                enabled = !isListening
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextInputWithSpeakButton(
                value = inputText,
                onValueChange = onTextChanged,
                onSpeak = onSpeakClicked,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccessibilityAppPreview() {
    AccessibilityApp(
        messages = listOf(
            ChatMessage("नमस्ते!", isUser = false),
            ChatMessage("Hello!", isUser = true)
        ),
        inputText = "",
        isListening = false,
        selectedLanguageTag = "hi-IN",
        onLanguageSelected = {},
        targetLanguageTag = "en-US",
        onTargetLanguageSelected = {},
        onMicClicked = {},
        onSpeakClicked = {},
        onTextChanged = {}
    )
}