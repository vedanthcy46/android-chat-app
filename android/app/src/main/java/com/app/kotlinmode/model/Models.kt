package com.app.kotlinmode.model

import com.google.gson.annotations.SerializedName

// ─────────────────────────────────────────
//  AUTH
// ─────────────────────────────────────────

data class LoginRequest(val email: String, val password: String)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val user: AuthUser  // uses AuthUser because login returns 'id' not '_id'
)

// Separate model for the login/register response user object
data class AuthUser(
    val id: String,         // backend sends 'id' (not '_id') in auth response
    val username: String,
    val email: String
)

// ─────────────────────────────────────────
//  USER
// ─────────────────────────────────────────

data class User(
    @SerializedName("_id") val id: String,
    val username: String,
    val email: String? = null,                                         // nullable — not all endpoints return email
    @SerializedName("profilePic") val profilePicture: String? = null,
    val bio: String? = null,
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList()
)

data class UpdateUserRequest(
    val username: String? = null,
    val bio: String? = null,
    @SerializedName("profilePic") val profilePicture: String? = null
)

data class FcmTokenRequest(val token: String)

// ─────────────────────────────────────────
//  POST
// ─────────────────────────────────────────

data class Post(
    @SerializedName("_id") val id: String,
    @SerializedName("user") val user: User,             // backend uses 'user', not 'userId'
    @SerializedName("caption") val description: String? = null, // backend uses 'caption', not 'description'
    val image: String? = null,
    val videoUrl: String? = null,
    val postType: String? = "image",
    val likes: List<String> = emptyList(),
    val saves: List<String> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val createdAt: String
)

data class Comment(
    @SerializedName("_id") val id: String,
    @SerializedName("user") val user: User,
    val text: String,
    val replies: List<Reply> = emptyList(),
    val createdAt: String
)

data class Reply(
    @SerializedName("_id") val id: String,
    @SerializedName("user") val user: User,
    val text: String,
    val createdAt: String
)

data class CreatePostRequest(
    @SerializedName("caption") val description: String,
    val image: String? = null,
    val videoUrl: String? = null,
    val postType: String? = "image"
)

data class CommentRequest(val text: String)
data class ReplyRequest(val text: String, val commentId: String)

data class UploadResponse(
    val url: String,
    val postType: String
)

// ─────────────────────────────────────────
//  CHAT
// ─────────────────────────────────────────

// Member inside a populated conversation response
data class ConversationMember(
    @SerializedName("_id") val id: String,
    val username: String,
    @SerializedName("profilePic") val profilePicture: String? = null
)

data class Conversation(
    @SerializedName("_id") val id: String,
    val members: List<ConversationMember>,
    val lastMessage: Message? = null,
    val updatedAt: String? = null
)

data class Message(
    @SerializedName("_id") val id: String,
    val conversationId: String,
    @SerializedName("senderId") val sender: String,  // backend field is 'senderId'
    val text: String,
    val createdAt: String
)

data class CreateConversationRequest(val receiverId: String)

data class SendMessageRequest(
    val conversationId: String,
    @SerializedName("senderId") val sender: String,  // backend expects 'senderId' in body
    val text: String
)
