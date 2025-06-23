package com.example.hackathon_app.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TextInputWithSpeakButton(
    value: String,
    onValueChange: (String) -> Unit,
    onSpeak: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            label = { Text("Type your message") },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = onSpeak) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Speak text"
                    )
                }
            }
        )
    }
} 