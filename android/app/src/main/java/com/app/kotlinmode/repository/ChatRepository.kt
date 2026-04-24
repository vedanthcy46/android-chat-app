package com.app.kotlinmode.repository

import com.app.kotlinmode.model.*
import com.app.kotlinmode.network.ApiService
import com.app.kotlinmode.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ChatRepository(private val api: ApiService) {

    fun getConversations(): Flow<Resource<List<Conversation>>> = flow {
        emit(Resource.Loading())
        try {
            val res = api.getConversations()
            if (res.isSuccessful) emit(Resource.Success(res.body()!!))
            else emit(Resource.Error("Failed to load conversations"))
        } catch (e: Exception) { emit(Resource.Error(e.localizedMessage ?: "Network error")) }
    }

    fun createConversation(receiverId: String): Flow<Resource<Conversation>> = flow {
        emit(Resource.Loading())
        try {
            val res = api.createConversation(CreateConversationRequest(receiverId))
            if (res.isSuccessful) emit(Resource.Success(res.body()!!))
            else emit(Resource.Error("Failed to create conversation"))
        } catch (e: Exception) { emit(Resource.Error(e.localizedMessage ?: "Network error")) }
    }

    fun getMessages(conversationId: String): Flow<Resource<List<Message>>> = flow {
        emit(Resource.Loading())
        try {
            val res = api.getMessages(conversationId)
            if (res.isSuccessful) emit(Resource.Success(res.body()!!))
            else emit(Resource.Error("Failed to load messages"))
        } catch (e: Exception) { emit(Resource.Error(e.localizedMessage ?: "Network error")) }
    }

    fun sendMessage(conversationId: String, sender: String, text: String): Flow<Resource<Message>> = flow {
        emit(Resource.Loading())
        try {
            val res = api.sendMessage(SendMessageRequest(conversationId, sender, text))
            if (res.isSuccessful) emit(Resource.Success(res.body()!!))
            else emit(Resource.Error("Failed to send message"))
        } catch (e: Exception) { emit(Resource.Error(e.localizedMessage ?: "Network error")) }
    }

    fun markAsRead(conversationId: String): Flow<Resource<Unit>> = flow {
        try {
            val res = api.markAsRead(conversationId)
            if (res.isSuccessful) emit(Resource.Success(Unit))
        } catch (e: Exception) { /* ignore error for read status */ }
    }

    fun getUnreadCount(): Flow<Resource<Int>> = flow {
        try {
            val res = api.getUnreadCount()
            if (res.isSuccessful) emit(Resource.Success(res.body()!!.totalUnread))
        } catch (e: Exception) { /* ignore error */ }
    }
}
