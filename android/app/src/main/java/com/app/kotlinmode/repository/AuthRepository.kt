package com.app.kotlinmode.repository

import com.app.kotlinmode.model.LoginRequest
import com.app.kotlinmode.model.RegisterRequest
import com.app.kotlinmode.model.AuthUser
import com.app.kotlinmode.network.ApiService
import com.app.kotlinmode.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * AuthRepository sits between the ViewModel and the network layer.
 *
 * Responsibilities:
 *  - Call ApiService login/register endpoints
 *  - Emit Resource<T> states (Loading → Success/Error)
 *  - Save/clear JWT via SessionManager
 *
 * The ViewModel never touches ApiService directly — it always goes through here.
 */
class AuthRepository(
    private val api: ApiService,
    private val session: SessionManager
) {
    fun login(email: String, password: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val res = api.login(LoginRequest(email, password))
            if (res.isSuccessful && res.body() != null) {
                val body = res.body()!!
                session.saveSession(body.token, body.user.id, body.user.username)
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error(res.errorBody()?.string() ?: "Login failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    fun register(username: String, email: String, password: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val res = api.register(RegisterRequest(username, email, password))
            if (res.isSuccessful && res.body() != null) {
                val body = res.body()!!
                session.saveSession(body.token, body.user.id, body.user.username)
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error(res.errorBody()?.string() ?: "Registration failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun logout() = session.clearSession()

    fun getToken()  = session.getToken()
    fun getUserId() = session.getUserId()
}
