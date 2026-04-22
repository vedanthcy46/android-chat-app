package com.app.kotlinmode.network

import com.app.kotlinmode.repository.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthTokenInterceptor(private val session: SessionManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { session.getToken().first() }

        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrBlank()) {
                addHeader("Authorization", "Bearer $token")
            }
        }.build()

        val response = chain.proceed(request)

        // Token Expiry Handling: If server returns 401, clear session
        if (response.code == 401) {
            runBlocking { session.clearSession() }
        }

        return response
    }
}
