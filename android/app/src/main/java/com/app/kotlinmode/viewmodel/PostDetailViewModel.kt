package com.app.kotlinmode.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.kotlinmode.model.Post
import com.app.kotlinmode.repository.PostRepository
import com.app.kotlinmode.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostDetailViewModel(private val repo: PostRepository) : ViewModel() {

    private val _post = MutableStateFlow<Resource<Post>>(Resource.Loading())
    val post: StateFlow<Resource<Post>> = _post

    fun loadPost(postId: String) {
        viewModelScope.launch {
            repo.getPostById(postId).collect { _post.value = it }
        }
    }

    fun likePost(postId: String) {
        viewModelScope.launch {
            repo.likePost(postId).collect { result ->
                if (result is Resource.Success) _post.value = result
            }
        }
    }

    fun savePost(postId: String) {
        viewModelScope.launch {
            repo.savePost(postId).collect { result ->
                if (result is Resource.Success) _post.value = result
            }
        }
    }

    fun addComment(postId: String, text: String) {
        viewModelScope.launch {
            repo.addComment(postId, text).collect { result ->
                if (result is Resource.Success) _post.value = result
            }
        }
    }

    fun addReply(postId: String, commentId: String, text: String) {
        viewModelScope.launch {
            repo.addReply(postId, commentId, text).collect { result ->
                if (result is Resource.Success) _post.value = result
            }
        }
    }
}
