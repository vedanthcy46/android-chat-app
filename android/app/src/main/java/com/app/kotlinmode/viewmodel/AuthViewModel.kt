package com.app.kotlinmode.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.kotlinmode.repository.AuthRepository
import com.app.kotlinmode.utils.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {

    // Exposed as StateFlow so Compose can collect it
    private val _state = MutableStateFlow<Resource<Unit>?>(null)
    val state: StateFlow<Resource<Unit>?> = _state.asStateFlow()

    fun login(email: String, password: String) {
        repo.login(email, password)
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

    fun register(username: String, email: String, password: String) {
        repo.register(username, email, password)
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            repo.logout()
            onDone()
        }
    }

    fun resetState() { _state.value = null }
}
