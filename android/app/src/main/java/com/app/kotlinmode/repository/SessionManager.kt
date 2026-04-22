package com.app.kotlinmode.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.app.kotlinmode.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = Constants.PREFS_NAME)

/**
 * SessionManager handles reading and writing the user's JWT + ID to DataStore.
 * DataStore is Android's modern, safe replacement for SharedPreferences.
 *
 * Why DataStore instead of SharedPreferences?
 *  ✅ Coroutine-safe (no ANR risk)
 *  ✅ Type-safe keys
 *  ✅ Survives process death
 */
class SessionManager(private val context: Context) {

    private val tokenKey    = stringPreferencesKey(Constants.TOKEN_KEY)
    private val userIdKey   = stringPreferencesKey(Constants.USER_ID_KEY)
    private val usernameKey = stringPreferencesKey("username")

    fun getToken(): Flow<String?>    = context.dataStore.data.map { it[tokenKey] }
    fun getUserId(): Flow<String?>   = context.dataStore.data.map { it[userIdKey] }
    fun getUsername(): Flow<String?> = context.dataStore.data.map { it[usernameKey] }

    suspend fun saveSession(token: String, userId: String, username: String) {
        context.dataStore.edit { prefs ->
            prefs[tokenKey]    = token
            prefs[userIdKey]   = userId
            prefs[usernameKey] = username
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
