package com.app.kotlinmode.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.kotlinmode.model.*
import com.app.kotlinmode.repository.*
import com.app.kotlinmode.utils.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ProfileEvent {
    data class NavigateToChat(val conversationId: String, val receiverId: String, val receiverName: String) : ProfileEvent()
    data class ShowToast(val message: String) : ProfileEvent()
}

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

    private val _isStartingChat = MutableStateFlow(false)
    val isStartingChat: StateFlow<Boolean> = _isStartingChat.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()

    private var loadJob: Job? = null

    fun loadProfile(userId: String) {
        if (userId.isBlank()) return
        
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.value = Resource.Loading()
            userRepo.getUserById(userId).collect { _state.value = it }
        }
            
        loadUserPosts(userId)
    }

    private fun loadUserPosts(userId: String) {
        viewModelScope.launch {
            _posts.value = Resource.Loading()
            postRepo.getUserPosts(userId).collect { _posts.value = it }
        }
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

    fun startChat(receiverId: String) {
        val viewedProfile = (state.value as? Resource.Success)?.data ?: return
        if (receiverId.isBlank()) return
        
        Log.d("ProfileVM", "Starting chat with: $receiverId")
        
        viewModelScope.launch {
            if (_isStartingChat.value) return@launch // Prevent multiple simultaneous calls
            
            _isStartingChat.value = true
            chatRepo.createConversation(receiverId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _isStartingChat.value = false
                        val convo = result.data ?: return@collect
                        if (convo.id.isNotBlank()) {
                            Log.d("ProfileVM", "Conversation ready: ${convo.id}")
                            _events.emit(ProfileEvent.NavigateToChat(convo.id, receiverId, viewedProfile.username))
                        }
                    }
                    is Resource.Error -> {
                        _isStartingChat.value = false
                        Log.e("ProfileVM", "Error: ${result.message}")
                        _events.emit(ProfileEvent.ShowToast(result.message ?: "Failed to start chat"))
                    }
                    is Resource.Loading -> { }
                }
            }
        }
    }

    fun updateProfile(username: String, bio: String, profilePic: String) {
        viewModelScope.launch {
            userRepo.updateUser(UpdateUserRequest(username, bio, profilePic)).collect { result ->
                if (result is Resource.Success) {
                    result.data?.let { 
                        _state.value = Resource.Success(it)
                        _events.emit(ProfileEvent.ShowToast("Profile updated successfully!"))
                    }
                } else if (result is Resource.Error) {
                    _events.emit(ProfileEvent.ShowToast(result.message ?: "Update failed"))
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
