package com.example.alpha_chat_native.ui.viewmodels

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alpha_chat_native.ui.screens.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// Discord Colors
private val DiscordAccent = Color(0xFF5865F2)
private val DiscordRed = Color(0xFFED4245)

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    // Channel Name -> List of Messages
    private val _channelMessages = mutableStateMapOf<String, SnapshotStateList<ChatMessage>>()
    val channelMessages: Map<String, List<ChatMessage>> = _channelMessages

    // Channel Name -> Permissions (true = allowed, false = restricted)
    private val _channelPermissions = mutableStateMapOf<String, Boolean>()
    val channelPermissions: Map<String, Boolean> = _channelPermissions

    init {
        // Initialize default local permissions (can be moved to Firestore later)
        _channelPermissions["announcements"] = false
        _channelPermissions["rules"] = false
        _channelPermissions["general"] = true
        _channelPermissions["web-dev"] = true
        _channelPermissions["android"] = true
    }

    fun getMessages(channelId: String): SnapshotStateList<ChatMessage> {
        // If we don't have a list for this channel yet, create it and start listening
        if (!_channelMessages.containsKey(channelId)) {
            val list = SnapshotStateList<ChatMessage>()
            _channelMessages[channelId] = list
            listenToMessages(channelId, list)
        }
        return _channelMessages[channelId]!!
    }

    private fun listenToMessages(channelId: String, list: SnapshotStateList<ChatMessage>) {
        firestore.collection("channels")
            .document(channelId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Timber.e(e, "Listen failed.")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    list.clear()
                    for (doc in snapshot) {
                        try {
                            val id = doc.id
                            val author = doc.getString("author") ?: "Unknown"
                            val content = doc.getString("content") ?: ""
                            val timestampLong = doc.getLong("timestamp") ?: 0L
                            val isCode = doc.getBoolean("isCode") ?: false
                            val isAdmin = doc.getBoolean("isAdmin") ?: false
                            val language = doc.getString("language") ?: "text"

                            // Convert timestamp to readable string
                            val date = Date(timestampLong)
                            val format = SimpleDateFormat("MMM dd 'at' h:mm a", Locale.getDefault())
                            val timestampStr = format.format(date)
                            
                            // Determine color locally for now based on admin status
                            val avatarColor = if (isAdmin) DiscordRed else DiscordAccent

                            list.add(
                                ChatMessage(
                                    id = id,
                                    author = author,
                                    avatarColor = avatarColor,
                                    timestamp = timestampStr,
                                    content = content,
                                    isCode = isCode,
                                    language = language,
                                    isAdmin = isAdmin
                                )
                            )
                        } catch (e: Exception) {
                            Timber.e(e, "Error parsing message")
                        }
                    }
                }
            }
    }
    
    fun getPermission(channel: String): Boolean {
        return _channelPermissions.getOrPut(channel) { true }
    }

    fun togglePermission(channel: String) {
        val current = getPermission(channel)
        _channelPermissions[channel] = !current
        // In a real app, you'd update this in Firestore:
        // firestore.collection("channels").document(channel).update("isChatAllowed", !current)
    }

    fun sendMessage(channelId: String, text: String, isCode: Boolean, isAdmin: Boolean) {
        val user = auth.currentUser
        val authorName = if (isAdmin) "Admin" else (user?.displayName ?: "User")
        
        val messageData = hashMapOf(
            "author" to authorName,
            "content" to text,
            "timestamp" to System.currentTimeMillis(),
            "isCode" to isCode,
            "isAdmin" to isAdmin,
            "language" to if (isCode) "text" else "text", // Can be improved with auto-detection
            "userId" to (user?.uid ?: "")
        )

        viewModelScope.launch {
            try {
                firestore.collection("channels")
                    .document(channelId)
                    .collection("messages")
                    .add(messageData)
            } catch (e: Exception) {
                Timber.e(e, "Error sending message")
            }
        }
    }
}
