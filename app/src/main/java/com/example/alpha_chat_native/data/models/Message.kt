package com.example.alpha_chat_native.data.models

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Message(
    val id: String = "",
    val text: String = "",
    val fromId: String = "",
    val toId: String? = "", // Made nullable to handle missing field gracefully, though typically should be present
    val chatId: String = "",
    @ServerTimestamp
    val timestamp: Date? = null
) {
    // Add a constructor for creating new messages with a server timestamp
    constructor(
        text: String, 
        fromId: String, 
        toId: String, 
        chatId: String, 
        timestamp: FieldValue
    ) : this(text = text, fromId = fromId, toId = toId, chatId = chatId)
}
