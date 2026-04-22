package com.app.kotlinmode.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.kotlinmode.model.User
import com.app.kotlinmode.repository.UserRepository
import com.app.kotlinmode.utils.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel(private val repo: UserRepository) : ViewModel() {

    private val _state = MutableStateFlow<Resource<List<User>>?>(null)
    val state: StateFlow<Resource<List<User>>?> = _state.asStateFlow()

    private var searchJob: Job? = null

    /** Debounced search — waits 400ms after user stops typing before calling API */
    fun search(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) { _state.value = null; return }
        searchJob = viewModelScope.launch {
            delay(400)
            repo.searchUsers(query).onEach { _state.value = it }.collect()
        }
    }

    fun followUser(id: String) {
        repo.followUser(id).launchIn(viewModelScope)
    }
}
