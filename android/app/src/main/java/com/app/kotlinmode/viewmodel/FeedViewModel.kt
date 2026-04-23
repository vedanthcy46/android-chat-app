package com.app.kotlinmode.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.kotlinmode.model.Post
import com.app.kotlinmode.repository.PostRepository
import com.app.kotlinmode.utils.Resource
import kotlinx.coroutines.flow.*

class FeedViewModel(private val repo: PostRepository) : ViewModel() {

    private val _state = MutableStateFlow<Resource<List<Post>>>(Resource.Loading())
    val state: StateFlow<Resource<List<Post>>> = _state.asStateFlow()

    private val _reels = MutableStateFlow<Resource<List<Post>>>(Resource.Loading())
    val reels: StateFlow<Resource<List<Post>>> = _reels.asStateFlow()

    init { 
        loadFeed() 
        loadReels()
    }

    fun loadFeed() {
        repo.getFeed()
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

    fun loadReels() {
        repo.getReels()
            .onEach { _reels.value = it }
            .launchIn(viewModelScope)
    }

    fun likePost(postId: String) = updatePostInList(postId) { repo.likePost(it) }
    fun savePost(postId: String) = updatePostInList(postId) { repo.savePost(it) }
    
    fun deletePost(postId: String) {
        repo.deletePost(postId).onEach { result ->
            if (result is Resource.Success) {
                // Remove from feed
                if (_state.value is Resource.Success) {
                    val currentList = (_state.value as Resource.Success).data ?: emptyList()
                    _state.value = Resource.Success(currentList.filter { it.id != postId })
                }
                // Remove from reels
                if (_reels.value is Resource.Success) {
                    val currentList = (_reels.value as Resource.Success).data ?: emptyList()
                    _reels.value = Resource.Success(currentList.filter { it.id != postId })
                }
            }
        }.launchIn(viewModelScope)
    }

    fun addComment(postId: String, text: String) = updatePostInList(postId) { repo.addComment(it, text) }
    fun addReply(postId: String, commentId: String, text: String) = updatePostInList(postId) { repo.addReply(it, commentId, text) }

    private fun updatePostInList(postId: String, action: (String) -> Flow<Resource<Post>>) {
        action(postId).onEach { result ->
            if (result is Resource.Success) {
                val updated = result.data!!
                val currentList = (_state.value as? Resource.Success)?.data ?: return@onEach
                _state.value = Resource.Success(
                    currentList.map { if (it.id == updated.id) updated else it }
                )
            }
        }.launchIn(viewModelScope)
    }
}
