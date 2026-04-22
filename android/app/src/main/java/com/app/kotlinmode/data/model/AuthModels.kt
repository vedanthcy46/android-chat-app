package com.app.kotlinmode.data.model

import com.google.gson.annotations.SerializedName

// ─────────────────────── Auth Request/Response ───────────────────────

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val user: UserDto
)

// ─────────────────────── User ───────────────────────

data class UserDto(
    @SerializedName("_id") val id: String,
    val username: String,
    val email: String,
    val profilePicture: String? = null,
    val bio: String? = null,
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList()
)

data class UpdateUserRequest(
    val username: String? = null,
    val bio: String? = null,
    val profilePicture: String? = null
)
