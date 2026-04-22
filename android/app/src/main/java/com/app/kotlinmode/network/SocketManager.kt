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
     * Emits a "sendMessage" event to the Socket.IO server.
     * The server will forward it to the receiver in real-time.
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

    /**
     * Registers a listener for incoming "newMessage" events.
     * [callback] is invoked on the Socket.IO background thread — update
     * your StateFlow inside the callback and Compose will recompose safely.
     */
    fun onNewMessage(callback: (conversationId: String, senderId: String, text: String) -> Unit) {
        socket?.on("newMessage") { args ->
            val data = args.getOrNull(0) as? JSONObject ?: return@on
            val conversationId = data.optString("conversationId")
            val senderId       = data.optString("senderId")
            val text           = data.optString("text")
            callback(conversationId, senderId, text)
        }
    }

    /** Removes the "newMessage" listener. Call when leaving the chat screen. */
    fun offNewMessage() {
        socket?.off("newMessage")
    }

    fun isConnected(): Boolean = socket?.connected() == true
}
