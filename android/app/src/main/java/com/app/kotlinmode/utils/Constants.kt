package com.app.kotlinmode.utils

/**
 * App-wide constants.
 * ⚠️  Emulator  → use 10.0.2.2  (maps to host PC localhost)
 * ⚠️  Real device on same WiFi → replace with your PC's local IP, e.g. 192.168.1.5
 */
object Constants {
    // ✅ Local network (real device on same WiFi as PC)
    // ⚠️  BASE_URL trailing slash is REQUIRED by Retrofit
    // ⚠️  SOCKET_URL must be root — no /api/ suffix
    const val BASE_URL    = "https://android-chat-app-g9yu.onrender.com/api/"
    const val SOCKET_URL  = "https://android-chat-app-g9yu.onrender.com/"
    const val PREFS_NAME  = "user_prefs"
    const val TOKEN_KEY   = "jwt_token"
    const val USER_ID_KEY = "user_id"
}
