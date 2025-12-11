package com.example.alpha_chat_native.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.alpha_chat_native.data.models.Message
import com.example.alpha_chat_native.vm.ChatViewModel
import kotlin.collections.emptyList

@Composable
fun ChatScreen(
    vm: ChatViewModel = hiltViewModel()
) {
    val messages by vm.messages.collectAsState(initial = emptyList())
    var input by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages) { msg ->
                MessageRow(msg)
            }
        }
        Row(modifier = Modifier.padding(8.dp)) {
            TextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                vm.send(input)
                input = ""
            }) {
                Text("Send")
            }
        }
    }
}

@Composable
fun MessageRow(msg: Message) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(6.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = msg.text)
            msg.timestamp?.let { ts ->
                Text(text = ts.toDate().toString(), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}