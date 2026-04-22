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

    init { loadFeed() }

    fun loadFeed() {
        repo.getFeed()
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

    fun likePost(postId: String) = updatePostInList(postId) { repo.likePost(it) }
    fun savePost(postId: String) = updatePostInList(postId) { repo.savePost(it) }

    // Replaces the changed post in the existing list (avoids full refresh)
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
