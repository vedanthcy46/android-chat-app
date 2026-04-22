package com.app.kotlinmode.data.model

import com.google.gson.annotations.SerializedName

data class MessageDto(
    @SerializedName("_id") val id: String,
    val conversationId: String,
    val sender: String,
    val text: String,
    val createdAt: String
)

data class ConversationDto(
    @SerializedName("_id") val id: String,
    val members: List<String>,
    val updatedAt: String
)

data class CreateConversationRequest(
    val receiverId: String
)

data class SendMessageRequest(
    val conversationId: String,
    val sender: String,
    val text: String
)

// Socket.IO event payload
data class SocketMessage(
    val senderId: String,
    val receiverId: String,
    val text: String
)
