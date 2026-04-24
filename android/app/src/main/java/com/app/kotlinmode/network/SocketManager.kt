package com.app.kotlinmode.network

import com.app.kotlinmode.utils.Constants
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URI

/**
 * SocketManager is a singleton that manages the Socket.IO connection.
 *
 * Usage:
 *   Connect when user logs in:   SocketManager.connect(userId)
 *   Disconnect on logout:        SocketManager.disconnect()
 *   Send a message:              SocketManager.sendMessage(convId, senderId, receiverId, text)
 *   Listen for messages:         SocketManager.onNewMessage { convId, senderId, text -> ... }
 *   Stop listening:              SocketManager.offNewMessage()
 *
 * Socket server events (match your Node.js backend):
 *   Emit  → "sendMessage"
 *   Listen ← "newMessage"
 */
object SocketManager {

    private var socket: Socket? = null

    /** Connects to the Socket.IO server with the user's ID as a query param. */
    fun connect(userId: String) {
        if (socket?.connected() == true) return  // already connected, skip

        val opts = IO.Options.builder()
            .setQuery("userId=$userId")
            .build()

        socket = IO.socket(URI.create(Constants.SOCKET_URL), opts)
        socket?.connect()
    }

    /** Disconnects and clears the socket instance. Call on logout. */
    fun disconnect() {
        socket?.disconnect()
        socket = null
    }

    /**
     * Emits a "joinChat" event so the server knows which conversation
     * the user is currently viewing (to mark messages as read immediately).
     */
    fun joinChat(conversationId: String) {
        val payload = JSONObject().apply { put("conversationId", conversationId) }
        socket?.emit("joinChat", payload)
    }

    /** Clears the active chat status on the server. */
    fun leaveChat() {
        socket?.emit("leaveChat")
    }

    /**
     * Emits a "sendMessage" event to the Socket.IO server.
     */
    fun sendMessage(conversationId: String, senderId: String, receiverId: String, text: String) {
        val payload = JSONObject().apply {
            put("conversationId", conversationId)
            put("senderId", senderId)
            put("receiverId", receiverId)
            put("text", text)
        }
        socket?.emit("sendMessage", payload)
    }

    /** Registers a listener for incoming "newMessage" events. */
    fun onNewMessage(callback: (JSONObject) -> Unit) {
        socket?.on("newMessage") { args ->
            val data = args.getOrNull(0) as? JSONObject ?: return@on
            callback(data)
        }
    }

    /** Registers a listener for user online/offline status changes. */
    fun onUserStatusChanged(callback: (userId: String, isOnline: Boolean, lastSeen: String?) -> Unit) {
        socket?.on("userStatusChanged") { args ->
            val data = args.getOrNull(0) as? JSONObject ?: return@on
            val userId = data.optString("userId")
            val isOnline = data.optBoolean("isOnline")
            val lastSeen = data.optString("lastSeen")
            callback(userId, isOnline, lastSeen)
        }
    }

    /** Registers a listener for message delivery confirmation. */
    fun onMessageSent(callback: (JSONObject) -> Unit) {
        socket?.on("messageSent") { args ->
            val data = args.getOrNull(0) as? JSONObject ?: return@on
            callback(data)
        }
    }

    fun offChatEvents() {
        socket?.off("newMessage")
        socket?.off("userStatusChanged")
        socket?.off("messageSent")
    }

    fun isConnected(): Boolean = socket?.connected() == true
}
