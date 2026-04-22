package com.app.kotlinmode.data.model

import com.google.gson.annotations.SerializedName

data class CommentDto(
    @SerializedName("_id") val id: String,
    val userId: UserDto,
    val text: String,
    val createdAt: String
)

data class PostDto(
    @SerializedName("_id") val id: String,
    val userId: UserDto,
    val description: String? = null,
    val image: String? = null,
    val likes: List<String> = emptyList(),
    val saves: List<String> = emptyList(),
    val comments: List<CommentDto> = emptyList(),
    val createdAt: String
)

data class CreatePostRequest(
    val description: String,
    val image: String? = null
)

data class CommentRequest(
    val text: String
)
