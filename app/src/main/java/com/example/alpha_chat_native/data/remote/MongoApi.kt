package com.example.alpha_chat_native.data.remote

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Query

// Placeholder data models matching your MongoDB schema
data class MongoMessage(
    val id: String,
    val channelId: String,
    val author: String,
    val content: String,
    val timestamp: Long
)

interface MongoApi {
    @GET("messages")
    suspend fun getMessages(@Query("channelId") channelId: String): List<MongoMessage>

    @POST("messages")
    suspend fun sendMessage(@Body message: MongoMessage)
}
