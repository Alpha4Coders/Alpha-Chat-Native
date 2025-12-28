package com.example.alpha_chat_native.data.repository

import android.net.Uri
import com.example.alpha_chat_native.data.models.Conversation
import com.example.alpha_chat_native.data.models.Message
import com.example.alpha_chat_native.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private val messagesRef = firestore.collection("messages")
    private val usersRef = firestore.collection("users")
    private val conversationsRef = firestore.collection("conversations")
    private val storageRef = storage.reference

    private suspend fun getCurrentUserIdEnsuringAuth(): String {
        var currentUser = auth.currentUser
        if (currentUser == null) {
            auth.signInAnonymously().await()
            currentUser = auth.currentUser!!
        }
        return currentUser.uid
    }

    suspend fun sendMessage(text: String, toId: String) {
        val fromId = getCurrentUserIdEnsuringAuth()
        val chatId = if (fromId < toId) "${fromId}_$toId" else "${toId}_$fromId"

        val message = Message(
            text = text,
            fromId = fromId,
            toId = toId,
            chatId = chatId,
            timestamp = FieldValue.serverTimestamp()
        )

        coroutineScope {
            async { messagesRef.add(message).await() }
            async { 
                val conversationUpdate = mapOf(
                    "lastMessage" to text,
                    "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                    "participantIds" to listOf(fromId, toId)
                )
                conversationsRef.document(chatId).set(conversationUpdate, SetOptions.merge()).await()
            }
        }
    }

    fun observeMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val subscription = messagesRef
            .whereEqualTo("chatId", chatId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, ex ->
                if (ex != null) {
                    return@addSnapshotListener
                }
                val msgs = snap?.documents?.mapNotNull {
                    it.toObject(Message::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(msgs)
            }
        awaitClose { subscription.remove() }
    }

    fun observeUsers(): Flow<List<User>> = callbackFlow {
        val subscription = usersRef.addSnapshotListener { snap, ex ->
            if (ex != null) {
                return@addSnapshotListener
            }
            val users = snap?.documents?.mapNotNull {
                it.toObject(User::class.java)
            } ?: emptyList()
            trySend(users)
        }
        awaitClose { subscription.remove() }
    }

    fun observeConversations(): Flow<List<Conversation>> = callbackFlow {
        val fromId = auth.currentUser?.uid ?: return@callbackFlow
        val subscription = conversationsRef
            .whereArrayContains("participantIds", fromId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, ex ->
                if (ex != null) {
                    return@addSnapshotListener
                }
                val convos = snap?.documents?.mapNotNull {
                    val conversation = it.toObject(Conversation::class.java)?.copy(id = it.id)
                    conversation?.let { convo ->
                        val otherId = convo.participantIds.firstOrNull { id -> id != fromId }
                        // You would fetch user details here and attach to the conversation object
                        // For now, we will do this in the ViewModel
                    }
                    conversation
                } ?: emptyList()
                trySend(convos)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
        val user = auth.currentUser
        if (user != null) {
            val userMap = User(
                uid = user.uid,
                email = user.email ?: "",
                displayName = user.displayName ?: ""
            )
            usersRef.document(user.uid).set(userMap, SetOptions.merge()).await()
        }
    }
    
    suspend fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    fun currentUserId(): String? = auth.currentUser?.uid

    fun signOut() {
        auth.signOut()
    }

    suspend fun uploadProfileImage(uri: Uri): String {
        val user = auth.currentUser ?: throw IllegalStateException("User not logged in")
        val imageRef = storageRef.child("profile_images/${user.uid}/${UUID.randomUUID()}.jpg")
        imageRef.putFile(uri).await()
        return imageRef.downloadUrl.await().toString()
    }

    suspend fun updateUserProfile(name: String, imageUrl: String? = null) {
        val user = auth.currentUser ?: return
        
        val profileUpdatesBuilder = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
        
        if (imageUrl != null) {
            profileUpdatesBuilder.setPhotoUri(Uri.parse(imageUrl))
        }
        
        user.updateProfile(profileUpdatesBuilder.build()).await()

        val userMap = mutableMapOf<String, Any>("displayName" to name)
        if (imageUrl != null) {
            userMap["imageUrl"] = imageUrl
        }
        usersRef.document(user.uid).set(userMap, SetOptions.merge()).await()
    }
    
    suspend fun getUser(uid: String): User? {
        return try {
            usersRef.document(uid).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
