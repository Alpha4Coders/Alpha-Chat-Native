package com.example.alpha_chat_native.data.models

import com.google.firebase.Timestamp

data class Conversation(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Timestamp? = null,
    // These fields are populated client-side
    var otherUser: User? = null 
)
