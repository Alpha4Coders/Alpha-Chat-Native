package com.example.alpha_chat_native.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alpha_chat_native.data.models.Message
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

    val messages = repo.observeMessages()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<Message>())

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun send(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repo.sendMessage(text)
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
                // Optionally save the name to Firestore user profile here
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
}
