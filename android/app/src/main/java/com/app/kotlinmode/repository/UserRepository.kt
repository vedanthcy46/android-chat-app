package com.app.kotlinmode.repository

import com.app.kotlinmode.model.*
import com.app.kotlinmode.network.ApiService
import com.app.kotlinmode.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UserRepository(private val api: ApiService) {

    fun getUserById(id: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val res = api.getUserById(id)
            if (res.isSuccessful) emit(Resource.Success(res.body()!!))
            else emit(Resource.Error("User not found"))
        } catch (e: Exception) { emit(Resource.Error(e.localizedMessage ?: "Network error")) }
    }

    fun searchUsers(query: String): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading())
        try {
            val res = api.searchUsers(query)
            if (res.isSuccessful) emit(Resource.Success(res.body()!!))
            else emit(Resource.Error("Search failed"))
        } catch (e: Exception) { emit(Resource.Error(e.localizedMessage ?: "Network error")) }
    }

    fun getUsersByIds(ids: List<String>): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading())
        try {
            val res = api.getUsersByIds(ids.joinToString(","))
            if (res.isSuccessful) emit(Resource.Success(res.body()!!))
            else emit(Resource.Error("Failed to fetch users"))
        } catch (e: Exception) { emit(Resource.Error(e.localizedMessage ?: "Network error")) }
    }

    fun followUser(id: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val res = api.followUser(id)
            if (res.isSuccessful) emit(Resource.Success(Unit))
            else emit(Resource.Error("Follow failed"))
        } catch (e: Exception) { emit(Resource.Error(e.localizedMessage ?: "Network error")) }
    }

    fun updateUser(req: UpdateUserRequest): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val res = api.updateUser(req)
            if (res.isSuccessful) emit(Resource.Success(res.body()!!))
            else emit(Resource.Error("Update failed"))
        } catch (e: Exception) { emit(Resource.Error(e.localizedMessage ?: "Network error")) }
    }

    fun updateFcmToken(token: String): Flow<Resource<Unit>> = flow {
        try {
            val res = api.updateFcmToken(FcmTokenRequest(token))
            if (res.isSuccessful) emit(Resource.Success(Unit))
            else emit(Resource.Error("Token sync failed"))
        } catch (e: Exception) { emit(Resource.Error(e.localizedMessage ?: "Network error")) }
    }
}
