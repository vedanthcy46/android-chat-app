package com.app.kotlinmode.viewmodel

import android.util.Log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.kotlinmode.model.Post
import com.app.kotlinmode.model.User
import com.app.kotlinmode.repository.*
import com.app.kotlinmode.utils.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepo: UserRepository,
    private val authRepo: AuthRepository,
    private val postRepo: PostRepository,
    private val chatRepo: ChatRepository
) : ViewModel() {

    private val _state = MutableStateFlow<Resource<User>>(Resource.Loading())
    val state: StateFlow<Resource<User>> = _state.asStateFlow()

    private val _posts = MutableStateFlow<Resource<List<Post>>>(Resource.Loading())
    val posts: StateFlow<Resource<List<Post>>> = _posts.asStateFlow()

    fun loadProfile(userId: String) {
        userRepo.getUserById(userId)
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
            
        loadUserPosts(userId)
    }

    private fun loadUserPosts(userId: String) {
        postRepo.getUserPosts(userId)
            .onEach { _posts.value = it }
            .launchIn(viewModelScope)
    }

    fun followUser(targetId: String) {
        viewModelScope.launch {
            userRepo.followUser(targetId).collect { result ->
                if (result is Resource.Success) {
                    loadProfile(targetId)
                }
            }
        }
    }

    fun startChat(receiverId: String, onConversationReady: (String) -> Unit) {
        Log.d("ProfileVM", "startChat called for receiver: $receiverId")
        viewModelScope.launch {
            chatRepo.createConversation(receiverId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        Log.d("ProfileVM", "Conversation ready: ${result.data?.id}")
                        result.data?.id?.let { onConversationReady(it) }
                    }
                    is Resource.Error -> {
                        Log.e("ProfileVM", "Failed to start chat: ${result.message}")
                    }
                    is Resource.Loading -> {
                        Log.d("ProfileVM", "Creating conversation...")
                    }
                }
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepo.logout()
            onDone()
        }
    }
}
