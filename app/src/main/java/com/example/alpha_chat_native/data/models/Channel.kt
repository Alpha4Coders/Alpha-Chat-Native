package com.example.alpha_chat_native.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Channel model matching AlphaChat-V2 backend schema.
 * Channels are group chat rooms for different topics.
 * Note: members/admins are string IDs in list responses
 */
@JsonClass(generateAdapter = true)
data class Channel(
    @Json(name = "_id") val id: String = "",
    val name: String = "",
    val slug: String = "",
    val description: String = "",
    val icon: String = "",
    val type: String = "public",  // public, private
    val members: List<String> = emptyList(),  // User IDs as strings
    val admins: List<String> = emptyList(),  // User IDs as strings
    val messageCount: Int = 0,
    val lastActivity: String? = null,
    val order: Int = 0,
    val isDefault: Boolean = false,
    // Computed fields from backend
    val memberCount: Int = 0,
    val isMember: Boolean = false,
    val isAdmin: Boolean = false
)

/**
 * Channel model with populated members (used in getChannel response)
 * When fetching a single channel, backend populates members as User objects
 */
@JsonClass(generateAdapter = true)
data class ChannelWithMembers(
    @Json(name = "_id") val id: String = "",
    val name: String = "",
    val slug: String = "",
    val description: String = "",
    val icon: String = "",
    val type: String = "public",
    val members: List<User> = emptyList(),  // Populated User objects
    val admins: List<User> = emptyList(),  // Populated User objects
    val messageCount: Int = 0,
    val lastActivity: String? = null,
    val order: Int = 0,
    val isDefault: Boolean = false,
    val memberCount: Int = 0,
    val isMember: Boolean = false,
    val isAdmin: Boolean = false
)

/**
 * Channel detail response with messages
 */
@JsonClass(generateAdapter = true)
data class ChannelDetail(
    val channel: ChannelWithMembers,
    val messages: List<ChannelMessage> = emptyList(),
    val pinnedMessages: List<ChannelMessage> = emptyList(),
    val pagination: Pagination? = null
)

