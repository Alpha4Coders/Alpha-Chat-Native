package com.example.alpha_chat_native.vm

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alpha_chat_native.data.models.Message
import com.example.alpha_chat_native.data.models.User
import com.example.alpha_chat_native.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    val currentUserId: String?
        get() = repo.currentUserId()
    
    init {
        loadMessages("global")
    }

    fun loadMessages(chatId: String) {
        _currentChatId.value = chatId
        viewModelScope.launch {
             repo.observeMessages(chatId).collect { msgs ->
                 _messages.value = msgs
             }
        }
    }

    fun send(text: String, toId: String? = null) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repo.sendMessage(text, toId)
        }
    }

    fun ensureSignedIn() {
        if (repo.currentUserId() == null) {
            viewModelScope.launch {
                try {
                    repo.signInAnonymously()
                } catch (e: Exception) {
                    _error.value = e.message
                }
            }
        }
    }

    fun isLoggedIn(): Boolean {
        return repo.currentUserId() != null
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
            repo.signOut()
            onSuccess()
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
