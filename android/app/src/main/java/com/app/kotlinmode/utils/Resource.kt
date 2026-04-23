package com.app.kotlinmode.utils

/**
 * Generic sealed class for wrapping all network/data results.
 *
 * Usage in a ViewModel:
 *   _state.value = Resource.Loading()
 *   _state.value = Resource.Success(data)
 *   _state.value = Resource.Error("message")
 *
 * Usage in a Composable:
 *   when (val state = uiState.collectAsState().value) {
 *       is Resource.Loading -> LoadingSpinner()
 *       is Resource.Success -> ShowData(state.data)
 *       is Resource.Error   -> ShowError(state.message)
 *   }
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Success<T>(data: T)         : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)

    fun <R> map(transform: (T) -> R): Resource<R> {
        return when (this) {
            is Success -> Success(transform(data!!))
            is Error -> Error(message ?: "Unknown error", data?.let { transform(it) })
            is Loading -> Loading(data?.let { transform(it) })
        }
    }
}
