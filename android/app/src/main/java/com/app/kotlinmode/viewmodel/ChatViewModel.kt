package com.app.kotlinmode.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.kotlinmode.model.Message
import com.app.kotlinmode.model.Conversation
import com.app.kotlinmode.network.SocketManager
import com.app.kotlinmode.repository.ChatRepository
import com.app.kotlinmode.utils.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(private val repo: ChatRepository) : ViewModel() {

    private val _conversations = MutableStateFlow<Resource<List<Conversation>>>(Resource.Loading())
    val conversations: StateFlow<Resource<List<Conversation>>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _receiverName = MutableStateFlow<String?>(null)
    val receiverName: StateFlow<String?> = _receiverName.asStateFlow()

    private val _totalUnreadCount = MutableStateFlow(0)
    val totalUnreadCount: StateFlow<Int> = _totalUnreadCount.asStateFlow()

    // Map to track the most recent online status of users (userId -> isOnline)
    // This helps sync status even if the conversation list hasn't loaded yet
    private val presenceMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    private val lastSeenMap = MutableStateFlow<Map<String, String?>>(emptyMap())

    fun setReceiverName(name: String) { _receiverName.value = name }

    fun loadConversations() {
        repo.getConversations().onEach { result ->
            if (result is Resource.Success) {
                // Apply current known presence to the freshly loaded list
                val list = result.data ?: emptyList()
                val syncedList = list.map { conv ->
                    conv.copy(members = conv.members.map { member ->
                        val currentOnline = presenceMap.value[member.id] ?: member.isOnline
                        val currentLastSeen = lastSeenMap.value[member.id] ?: member.lastSeen
                        member.copy(isOnline = currentOnline, lastSeen = currentLastSeen)
                    })
                }
                _conversations.value = Resource.Success(syncedList)
            } else {
                _conversations.value = result
            }
        }.launchIn(viewModelScope)
        updateTotalUnreadCount()
    }

    fun updateTotalUnreadCount() {
        repo.getUnreadCount().onEach { if (it is Resource.Success) _totalUnreadCount.value = it.data ?: 0 }.launchIn(viewModelScope)
    }

    fun loadMessages(conversationId: String) {
        repo.getMessages(conversationId).onEach { result ->
            if (result is Resource.Success) {
                _messages.value = result.data ?: emptyList()
                markAsRead(conversationId)
            }
        }.launchIn(viewModelScope)
    }

    fun markAsRead(conversationId: String) {
        repo.markAsRead(conversationId).onEach { result ->
            if (result is Resource.Success) {
                // Locally update unread count in conversations list
                if (_conversations.value is Resource.Success) {
                    val currentList = (_conversations.value as Resource.Success).data ?: emptyList()
                    _conversations.value = Resource.Success(
                        currentList.map { if (it.id == conversationId) it.copy(unreadCount = 0) else it }
                    )
                }
                updateTotalUnreadCount()
            }
        }.launchIn(viewModelScope)
    }

    fun sendMessage(conversationId: String, senderId: String, receiverId: String, text: String) {
        // Optimistic UI update handled by socket callback usually, or add here
        viewModelScope.launch {
            SocketManager.sendMessage(conversationId, senderId, receiverId, text)
        }
    }

    fun startGlobalListeners() {
        SocketManager.onNewMessage { data ->
            val convId = data.optString("conversationId")
            val senderId = data.optString("senderId")
            val text = data.optString("text")
            val id = data.optString("_id")
            val createdAt = data.optString("createdAt")
            val isRead = data.optBoolean("isRead")

            val msg = Message(id, convId, senderId, text, isRead, createdAt)
            
            // If we are in this chat, append it
            // (The specific chat screen should also be listening, but global listener handles it too)
            
            // Update conversation list item
            if (_conversations.value is Resource.Success) {
                val currentList = (_conversations.value as Resource.Success).data ?: emptyList()
                _conversations.value = Resource.Success(
                    currentList.map { 
                        if (it.id == convId) {
                            it.copy(
                                lastMessage = msg,
                                unreadCount = if (isRead) it.unreadCount else it.unreadCount + 1
                            )
                        } else it
                    }
                )
            }
            updateTotalUnreadCount()
        }

        SocketManager.onUserStatusChanged { userId, isOnline, lastSeen ->
            // 1. Update the permanent presence maps
            presenceMap.value = presenceMap.value + (userId to isOnline)
            lastSeenMap.value = lastSeenMap.value + (userId to lastSeen)

            // 2. Apply to the current conversation list if it's already loaded
            if (_conversations.value is Resource.Success) {
                val currentList = (_conversations.value as Resource.Success).data ?: emptyList()
                _conversations.value = Resource.Success(
                    currentList.map { conv ->
                        conv.copy(members = conv.members.map { member ->
                            if (member.id == userId) member.copy(isOnline = isOnline, lastSeen = lastSeen)
                            else member
                        })
                    }
                )
            }
        }
    }

    fun startChatListeners(currentConversationId: String, currentUserId: String) {
        SocketManager.joinChat(currentConversationId)
        
        SocketManager.onNewMessage { data ->
            val convId = data.optString("conversationId")
            if (convId == currentConversationId) {
                val senderId = data.optString("senderId")
                val text = data.optString("text")
                val id = data.optString("_id")
                val isRead = data.optBoolean("isRead")
                val msg = Message(id, convId, senderId, text, isRead, "just now")
                
                if (senderId != currentUserId) {
                    _messages.value = _messages.value + msg
                    // Since we are in the chat, mark read
                    markAsRead(currentConversationId)
                }
            }
        }

        SocketManager.onMessageSent { data ->
            val convId = data.optString("conversationId")
            if (convId == currentConversationId) {
                val senderId = data.optString("senderId")
                val text = data.optString("text")
                val id = data.optString("_id")
                val isRead = data.optBoolean("isRead")
                val msg = Message(id, convId, senderId, text, isRead, "just now")
                _messages.value = _messages.value + msg
            }
        }
    }

    fun stopChatListeners() {
        SocketManager.leaveChat()
        SocketManager.offChatEvents()
        // Resume global listener
        startGlobalListeners()
    }

    override fun onCleared() {
        super.onCleared()
        SocketManager.offChatEvents()
    }
}
