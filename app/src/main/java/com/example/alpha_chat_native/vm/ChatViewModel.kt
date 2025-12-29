package com.example.alpha_chat_native.vm

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alpha_chat_native.data.models.Conversation
import com.example.alpha_chat_native.data.models.Message
import com.example.alpha_chat_native.data.models.User
import com.example.alpha_chat_native.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: ChatRepository
) : ViewModel() {

    private val _currentChatId = MutableStateFlow("global")
    val currentChatId = _currentChatId.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()
    
    val users = repo.observeUsers()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Combine conversations with users to populate the 'otherUser' field
    val conversations = combine(repo.observeConversations(), users) { convos, allUsers ->
        val currentUid = repo.currentUserId()
        convos.map { convo ->
            val otherId = convo.participantIds.firstOrNull { it != currentUid }
            val otherUser = allUsers.find { it.uid == otherId }
            convo.copy(otherUser = otherUser)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    val currentUserId: String?
        get() = repo.currentUserId()
    
    init {
        // Safe initialization
        try {
            loadMessages("global")
        } catch (e: Exception) {
            Timber.e(e, "Error loading messages in init")
            _error.value = "Failed to load messages"
        }
    }

    fun loadMessages(chatId: String) {
        _currentChatId.value = chatId
        viewModelScope.launch {
             try {
                 repo.observeMessages(chatId).collect { msgs ->
                     _messages.value = msgs
                 }
             } catch (e: Exception) {
                 Timber.e(e, "Error observing messages")
             }
        }
    }

    fun send(text: String, toId: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            try {
                repo.sendMessage(text, toId)
            } catch (e: Exception) {
                _error.value = "Failed to send message"
                Timber.e(e, "Error sending message")
            }
        }
    }

    fun isLoggedIn(): Boolean {
        return try {
            repo.currentUserId() != null
        } catch (e: Exception) {
            Timber.e(e, "Error checking login status")
            false
        }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            _error.value = "Email and password cannot be empty"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repo.signInWithEmail(email, pass)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Login failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(name: String, email: String, pass: String, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            _error.value = "Email and password cannot be empty"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repo.createAccount(email, pass)
                repo.updateUserProfile(name)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Registration failed"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }

    fun signOut(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repo.signOut()
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Sign out failed"
            }
        }
    }

    fun updateProfile(name: String, imageUri: Uri?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val imageUrl = if (imageUri != null) {
                    repo.uploadProfileImage(imageUri)
                } else {
                    null
                }
                repo.updateUserProfile(name, imageUrl)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Update failed"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
