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
            _uploadState.value = result.map { it.url } // Map to Resource<String> for UI if needed
            if (result is Resource.Success) {
                val uploadData = result.data!!
                createPost(caption, uploadData.url, uploadData.postType)
            }
        }.launchIn(viewModelScope)
    }

    private fun createPost(caption: String, url: String, type: String) {
        val image = if (type == "image") url else null
        val videoUrl = if (type == "video") url else null
        
        repo.createPost(caption, image, videoUrl, type).onEach {
            _createState.value = it
        }.launchIn(viewModelScope)
    }

    fun resetState() {
        _uploadState.value = null
        _createState.value = null
    }
}
