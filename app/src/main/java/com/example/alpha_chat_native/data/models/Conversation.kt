package com.example.alpha_chat_native.data.models

import com.google.firebase.Timestamp

data class Conversation(
    val chatId: String = "",
    val lastMessage: String = "",
    val timestamp: Timestamp? = null,
    val participantIds: List<String> = emptyList()
)
