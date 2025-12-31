package com.example.alpha_chat_native.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * User model matching AlphaChat-V2 backend schema.
 * Users are created via GitHub OAuth authentication.
 */
@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "_id") val id: String = "",
    val githubId: String = "",
    val username: String = "",
    val displayName: String = "",
    val email: String = "",
    val avatar: String = "",
    val profileUrl: String = "",
    val bio: String = "",
    val company: String = "",
    val location: String = "",
    val role: String = "member",  // cofounder, core, member
    val isOnline: Boolean = false,
    val status: String = "offline",  // online, offline, away, busy
    val lastSeen: String? = null
) {
    // Backwards compatibility for existing code using 'uid' and 'imageUrl'
    val uid: String get() = id
    val imageUrl: String get() = avatar
}

/**
 * Online user info received from Socket.IO
 */
@JsonClass(generateAdapter = true)
data class OnlineUser(
    val oderId: String,
    val status: String,
    val joinedAt: String
)
