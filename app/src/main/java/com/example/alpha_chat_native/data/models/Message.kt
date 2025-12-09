package com.example.alpha_chat_native.data.models

import com.google.firebase.Timestamp

data class Message(
    val id: String = "",
    val text: String = "",
    val fromId: String = "",
    val toId: String? = null,
    val timestamp: Timestamp? = null
)