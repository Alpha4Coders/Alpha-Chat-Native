package com.example.alpha_chat_native.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * User model matching AlphaChat-V2 backend schema.
 * Users are created via GitHub OAuth authentication.
 * 
 * Note: Backend returns 'id' in checkAuth but '_id' in other endpoints.
 * We handle both by setting default values.
 */
@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "_id") val _id: String? = null,
    @Json(name = "id") val idField: String? = null,  // Some endpoints return "id" instead of "_id"
    val githubId: String? = null,
    val username: String = "",
    val displayName: String = "",
    val email: String? = null,
    val avatar: String = "",
    val profileUrl: String? = null,
    val bio: String? = null,
    val company: String? = null,
    val location: String? = null,
    val role: String = "member",  // cofounder, core, member
    val isOnline: Boolean = false,
    val status: String = "offline",  // online, offline, away, busy
    val lastSeen: String? = null
) {
    // Unified ID accessor - use whichever is available
    val id: String get() = _id ?: idField ?: ""
    
    // Backwards compatibility for existing code
    val uid: String get() = id
    val imageUrl: String get() = avatar
}

/**
 * Online user info received from Socket.IO
 */
@JsonClass(generateAdapter = true)
data class OnlineUser(
    val userId: String,
    val status: String,
    val joinedAt: String
)
