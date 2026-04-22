package com.app.kotlinmode.network

import android.content.Context
import com.app.kotlinmode.repository.SessionManager
import com.app.kotlinmode.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * RetrofitInstance is a singleton that provides a single, shared [ApiService].
 *
 * Why singleton?
 *   Creating a Retrofit client is expensive (thread pools, connection pools).
 *   We create it once and reuse it throughout the app lifetime.
 *
 * How JWT is injected?
 *   The [AuthTokenInterceptor] reads the token from [SessionManager] and
 *   adds "Authorization: Bearer <token>" header to every request automatically.
 */
object RetrofitInstance {

    private var _api: ApiService? = null

    fun getApi(context: Context): ApiService {
        if (_api == null) {
            // SessionManager is the single owner of Context.dataStore
            val session = SessionManager(context)
            _api = buildRetrofit(session).create(ApiService::class.java)
        }
        return _api!!
    }

    private fun buildRetrofit(session: SessionManager): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(buildOkHttpClient(session))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun buildOkHttpClient(session: SessionManager): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(AuthTokenInterceptor(session)) // ← injects JWT
            .addInterceptor(logging)                        // ← logs traffic
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
