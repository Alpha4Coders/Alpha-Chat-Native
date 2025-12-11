package com.example.alpha_chat_native.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.alpha_chat_native.vm.ChatViewModel

@Composable
fun LoginScreen(
    onLogin: () -> Unit,
    vm: ChatViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        vm.ensureSignedIn()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to AlphaChat")
        Spacer(Modifier.height(12.dp))
        TextField(value = name, onValueChange = { name = it }, placeholder = { Text("Display name") })
        Spacer(Modifier.height(12.dp))
        Button(onClick = onLogin) {
            Text("Enter")


        }
    }
}