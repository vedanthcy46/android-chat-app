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

    fun setReceiverName(name: String) { _receiverName.value = name }

    fun loadConversations(userId: String) {
        repo.getConversations(userId).onEach { _conversations.value = it }.launchIn(viewModelScope)
    }

    fun loadMessages(conversationId: String) {
        repo.getMessages(conversationId).onEach { result ->
            if (result is Resource.Success) _messages.value = result.data ?: emptyList()
        }.launchIn(viewModelScope)
    }

    fun sendMessage(conversationId: String, senderId: String, receiverId: String, text: String) {
        val temp = Message("temp_${System.currentTimeMillis()}", conversationId, senderId, text, "just now")
        _messages.value = _messages.value + temp

        viewModelScope.launch {
            SocketManager.sendMessage(conversationId, senderId, receiverId, text)
        }
    }

    fun startListening(currentConversationId: String, currentUserId: String) {
        SocketManager.onNewMessage { conversationId, senderId, text ->
            if (conversationId == currentConversationId && senderId != currentUserId) {
                val msg = Message("socket_${System.currentTimeMillis()}", conversationId, senderId, text, "just now")
                _messages.value = _messages.value + msg
            }
        }
    }

    fun stopListening() = SocketManager.offNewMessage()

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
