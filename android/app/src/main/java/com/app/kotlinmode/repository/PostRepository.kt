package com.app.kotlinmode.repository

import com.app.kotlinmode.model.*
import com.app.kotlinmode.network.ApiService
import com.app.kotlinmode.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody


class PostRepository(private val api: ApiService) {

    fun getFeed(): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading())
        try {
            val res = api.getFeed()
            if (res.isSuccessful) emit(Resource.Success(res.body()!!))
            else emit(Resource.Error("Failed to load feed"))
        } catch (e: Exception) { emit(Resource.Error(e.localizedMessage ?: "Network error")) }
    }

    fun getPostById(id: String): Flow<Resource<Post>> = flow {
        emit(Resource.Loading())
        try {
            val res = api.getPostById(id)
            if (res.isSuccessful) emit(Resource.Success(res.body()!!))
            else emit(Resource.Error("Post not found"))
        } catch (e: Exception) { emit(Resource.Error(e.localizedMessage ?: "Network error")) }
    }

    fun likePost(postId: String): Flow<Resource<Post>> = flow {
        emit(Resource.Loading())
        try {
            val res = api.likePost(postId)
            if (res.isSuccessful) emit(Resource.Success(res.body()!!))
            else emit(Resource.Error(res.errorBody()?.string() ?: "Like failed"))
        } catch (e: Exception) { emit(Resource.Error(e.localizedMessage ?: "Network error")) }
    }

    fun savePost(postId: String): Flow<Resource<Post>> = flow {
        emit(Resource.Loading())
        try {
            val res = api.savePost(postId)
            if (res.isSuccessful) emit(Resource.Success(res.body()!!))
            else emit(Resource.Error(res.errorBody()?.string() ?: "Save failed"))
        } catch (e: Exception) { emit(Resource.Error(e.localizedMessage ?: "Network error")) }
    }

    fun createPost(description: String, image: String? = null): Flow<Resource<Post>> = flow {
        emit(Resource.Loading())
        try {
            val res = api.createPost(CreatePostRequest(description, image))
            if (res.isSuccessful) emit(Resource.Success(res.body()!!))
            else emit(Resource.Error(res.errorBody()?.string() ?: "Create failed"))
        } catch (e: Exception) { emit(Resource.Error(e.localizedMessage ?: "Network error")) }
    }

    fun addComment(id: String, text: String): Flow<Resource<Post>> = flow {
        emit(Resource.Loading())
        try {
            val res = api.addComment(id, CommentRequest(text))
            if (res.isSuccessful) emit(Resource.Success(res.body()!!))
            else emit(Resource.Error("Failed to comment"))
        } catch (e: Exception) { emit(Resource.Error(e.localizedMessage ?: "Network error")) }
    }

    fun addReply(postId: String, commentId: String, text: String): Flow<Resource<Post>> = flow {
        emit(Resource.Loading())
        try {
            val res = api.addReply(postId, ReplyRequest(text, commentId))
            if (res.isSuccessful) emit(Resource.Success(res.body()!!))
            else emit(Resource.Error("Failed to reply"))
        } catch (e: Exception) { emit(Resource.Error(e.localizedMessage ?: "Network error")) }
    }

    fun uploadImage(image: MultipartBody.Part): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val res = api.uploadImage(image)
            if (res.isSuccessful && res.body() != null) {
                val url = res.body()!!["url"]
                if (!url.isNullOrBlank()) {
                    emit(Resource.Success(url))
                } else {
                    emit(Resource.Error("Upload successful but server returned empty URL. Response: ${res.body()}"))
                }
            } else {
                emit(Resource.Error(res.errorBody()?.string() ?: "Upload failed"))
            }
        } catch (e: Exception) { emit(Resource.Error(e.localizedMessage ?: "Network error")) }
    }

    fun getUserPosts(userId: String): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading())
        try {
            val res = api.getUserPosts(userId)
            if (res.isSuccessful) emit(Resource.Success(res.body()!!))
            else emit(Resource.Error("Failed to load user posts"))
        } catch (e: Exception) { emit(Resource.Error(e.localizedMessage ?: "Network error")) }
    }
}
