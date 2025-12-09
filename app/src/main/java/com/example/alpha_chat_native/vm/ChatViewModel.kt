package com.example.alpha_chat_native.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alpha_chat_native.data.models.Message
import com.example.alpha_chat_native.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: ChatRepository
) : ViewModel() {

    val messages = repo.observeMessages()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<Message>())

    fun send(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repo.sendMessage(text)
        }
    }

    fun ensureSignedIn() {
        viewModelScope.launch {
            try {
                repo.signInAnonymously()
            } catch (e: Exception) {
            }
        }
    }
}