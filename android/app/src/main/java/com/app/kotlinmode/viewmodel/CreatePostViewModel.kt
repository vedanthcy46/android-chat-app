package com.app.kotlinmode.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.kotlinmode.model.Post
import com.app.kotlinmode.repository.PostRepository
import com.app.kotlinmode.utils.Resource
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody

class CreatePostViewModel(private val repo: PostRepository) : ViewModel() {

    private val _uploadState = MutableStateFlow<Resource<String>?>(null)
    val uploadState: StateFlow<Resource<String>?> = _uploadState.asStateFlow()

    private val _createState = MutableStateFlow<Resource<Post>?>(null)
    val createState: StateFlow<Resource<Post>?> = _createState.asStateFlow()

    fun uploadAndCreatePost(imagePart: MultipartBody.Part, caption: String) {
        repo.uploadImage(imagePart).onEach { result ->
            _uploadState.value = result
            if (result is Resource.Success) {
                val imageUrl = result.data
                if (!imageUrl.isNullOrBlank()) {
                    createPost(caption, imageUrl)
                } else {
                    _createState.value = Resource.Error("Upload succeeded but returned no URL")
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun createPost(caption: String, imageUrl: String) {
        repo.createPost(caption, imageUrl).onEach {
            _createState.value = it
        }.launchIn(viewModelScope)
    }

    fun resetState() {
        _uploadState.value = null
        _createState.value = null
    }
}
