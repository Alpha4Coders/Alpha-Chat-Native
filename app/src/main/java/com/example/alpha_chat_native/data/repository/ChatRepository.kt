package com.example.alpha_chat_native.data.repository

import com.example.alpha_chat_native.data.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.jvm.java

@Singleton
class ChatRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val messagesRef = firestore.collection("messages")

    suspend fun sendMessage(text: String, toId: String? = null) {
        val uid = auth.currentUser?.uid ?: return
        val map = mapOf(
            "text" to text,
            "fromId" to uid,
            "toId" to toId,
            "timestamp" to FieldValue.serverTimestamp()
        )
        messagesRef.add(map).await()
    }

    fun observeMessages() = callbackFlow<List<Message>> {
        val subscription = messagesRef
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, ex ->
                if (ex != null) {
                    close(ex)
                    return@addSnapshotListener
                }
                val msgs = snap?.documents?.mapNotNull {
                    val msg = it.toObject(Message::class.java)?.copy(id = it.id)
                    msg
                } ?: emptyList()
                trySend(msgs)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun signInAnonymously() {
        auth.signInAnonymously().await()
    }

    suspend fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    fun currentUserId(): String? = auth.currentUser?.uid
}