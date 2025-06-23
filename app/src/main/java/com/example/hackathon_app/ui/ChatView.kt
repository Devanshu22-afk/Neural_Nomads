package com.example.hackathon_app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hackathon_app.presentation.ChatViewModel

// Data class for a chat message
data class ChatMessage(val text: String, val isUser: Boolean)

@Composable
fun ChatView(
    messages: List<ChatMessage>,
    modifier: Modifier = Modifier,
    viewModel: com.example.hackathon_app.presentation.ChatViewModel,
    context: android.content.Context,
    to: String = "userB", // Replace with actual callee identifier
    tokenUrl: String = "http://192.168.10.11:3000/accessToken" // User's local token server
) {
    val isInCall by viewModel.isInCall.collectAsState()
    val callStatus by viewModel.callStatus.collectAsState()
    Column(modifier = modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { viewModel.startCall(context, to, tokenUrl) }, enabled = !isInCall) {
                Text("Start Call")
            }
            Button(onClick = { viewModel.endCall() }, enabled = isInCall) {
                Text("End Call")
            }
        }
        if (callStatus.isNotBlank()) {
            Text(text = callStatus, modifier = Modifier.padding(8.dp))
        }
        LazyColumn(
            modifier = Modifier.weight(1f).padding(8.dp),
            reverseLayout = true
        ) {
            items(messages) { message ->
                ChatMessageItem(message)
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
} 