package com.app.kotlinmode.util

/**
 * A sealed class that represents the state of a network/data operation.
 * Used across all ViewModels for consistent state handling.
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}
