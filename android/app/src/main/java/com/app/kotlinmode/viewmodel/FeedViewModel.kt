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

    fun likePost(postId: String, currentUserId: String) {
        // Optimistic UI Update: Toggle locally first
        if (_state.value is Resource.Success) {
            val currentList = (_state.value as Resource.Success).data ?: emptyList()
            val newList = currentList.map { post ->
                if (post.id == postId) {
                    val newLikes = if (post.likes.contains(currentUserId)) {
                        post.likes.filter { it != currentUserId }
                    } else {
                        post.likes + currentUserId
                    }
                    post.copy(likes = newLikes)
                } else post
            }
            _state.value = Resource.Success(newList)
            // Also update reels if visible
            if (_reels.value is Resource.Success) {
                val reelsList = (_reels.value as Resource.Success).data ?: emptyList()
                _reels.value = Resource.Success(reelsList.map { if (it.id == postId) newList.find { p -> p.id == postId }!! else it })
            }
        }

        // Call repository in background
        repo.likePost(postId).onEach { result ->
            if (result is Resource.Success) {
                updatePostInState(result.data!!)
            } else if (result is Resource.Error) {
                // If it fails, we just don't do anything or we could revert
                // But definitely DON'T loadFeed() as it causes a full UI refresh jump
            }
        }.launchIn(viewModelScope)
    }

    fun savePost(postId: String) {
        performOptimisticUpdate(postId) { repo.savePost(it) }
    }
    
    fun deletePost(postId: String) {
        repo.deletePost(postId).onEach { result ->
            if (result is Resource.Success) {
                removePostFromState(postId)
            }
        }.launchIn(viewModelScope)
    }

    fun addComment(postId: String, text: String) {
        performOptimisticUpdate(postId) { repo.addComment(it, text) }
    }

    fun addReply(postId: String, commentId: String, text: String) {
        performOptimisticUpdate(postId) { repo.addReply(it, commentId, text) }
    }

    private fun performOptimisticUpdate(postId: String, action: (String) -> Flow<Resource<Post>>) {
        action(postId).onEach { result ->
            if (result is Resource.Success) {
                val updatedPost = result.data!!
                updatePostInState(updatedPost)
            }
        }.launchIn(viewModelScope)
    }

    private fun updatePostInState(updatedPost: Post) {
        // Update Feed
        if (_state.value is Resource.Success) {
            val currentList = (_state.value as Resource.Success).data ?: emptyList()
            _state.value = Resource.Success(currentList.map { if (it.id == updatedPost.id) updatedPost else it })
        }
        // Update Reels
        if (_reels.value is Resource.Success) {
            val currentList = (_reels.value as Resource.Success).data ?: emptyList()
            _reels.value = Resource.Success(currentList.map { if (it.id == updatedPost.id) updatedPost else it })
        }
    }

    private fun removePostFromState(postId: String) {
        if (_state.value is Resource.Success) {
            val currentList = (_state.value as Resource.Success).data ?: emptyList()
            _state.value = Resource.Success(currentList.filter { it.id != postId })
        }
        if (_reels.value is Resource.Success) {
            val currentList = (_reels.value as Resource.Success).data ?: emptyList()
            _reels.value = Resource.Success(currentList.filter { it.id != postId })
        }
    }
}
