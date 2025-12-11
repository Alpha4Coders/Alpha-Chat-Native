package com.example.alpha_chat_native.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChatListScreen(
    onChatClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Chats", modifier = Modifier.padding(bottom = 16.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onChatClick() }
        ) {
            Text(
                text = "Global Chat",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
