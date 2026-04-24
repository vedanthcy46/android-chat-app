package com.app.kotlinmode.network

import com.app.kotlinmode.model.*
import retrofit2.Response
import retrofit2.http.*
import okhttp3.MultipartBody

/**
 * ApiService defines every backend endpoint as a Kotlin suspend function.
 *
 * ⚠️ BASE_URL is already "https://.../api/" so all paths below
 *    must NOT start with "api/" — just the resource path directly.
 *
 * Rules:
 *  - All functions are `suspend` so they run on a coroutine.
 *  - Return type is `Response<T>` so we can inspect HTTP status codes.
 *  - Path segments use @Path, query params use @Query, request bodies use @Body.
 */
interface ApiService {

    // ───────────────────────────────────────────
    //  AUTH  →  /api/auth
    // ───────────────────────────────────────────

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    // ───────────────────────────────────────────
    //  USERS  →  /api/users
    // ───────────────────────────────────────────

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: String): Response<User>

    @GET("users/search")
    suspend fun searchUsers(@Query("q") query: String): Response<List<User>>

    @GET("users/list")
    suspend fun getUsersByIds(@Query("ids") ids: String): Response<List<User>>

    @PUT("users/update")
    suspend fun updateUser(@Body body: UpdateUserRequest): Response<User>

    @PUT("users/fcm-token")
    suspend fun updateFcmToken(@Body body: FcmTokenRequest): Response<Map<String, String>>

    @POST("users/follow/{id}")
    suspend fun followUser(@Path("id") targetId: String): Response<Map<String, String>>

    // ───────────────────────────────────────────
    //  POSTS  →  /api/posts
    // ───────────────────────────────────────────

    @POST("posts/create")
    suspend fun createPost(@Body body: CreatePostRequest): Response<Post>

    @GET("posts/feed")
    suspend fun getFeed(): Response<List<Post>>

    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") id: String): Response<Post>

    @PUT("posts/like/{id}")
    suspend fun likePost(@Path("id") id: String): Response<Post>

    @PUT("posts/save/{id}")
    suspend fun savePost(@Path("id") postId: String): Response<Post>

    @POST("posts/comment/{id}")
    suspend fun addComment(@Path("id") id: String, @Body body: CommentRequest): Response<Post>

    @POST("posts/reply/{id}")
    suspend fun addReply(@Path("id") id: String, @Body body: ReplyRequest): Response<Post>

    @Multipart
    @POST("posts/upload")
    suspend fun uploadImage(@Part image: MultipartBody.Part): Response<UploadResponse>

    @GET("posts/user/{userId}")
    suspend fun getUserPosts(@Path("userId") userId: String): Response<List<Post>>

    // ───────────────────────────────────────────
    //  CHAT  →  /api/chat
    // ───────────────────────────────────────────

    @POST("chat/conversation")
    suspend fun createConversation(@Body body: CreateConversationRequest): Response<Conversation>

    @GET("chat/conversations")
    suspend fun getConversations(): Response<List<Conversation>>

    @GET("posts/reels")
    suspend fun getReels(): Response<List<Post>>

    @POST("chat/message")
    suspend fun sendMessage(@Body body: SendMessageRequest): Response<Message>

    @GET("chat/messages/{conversationId}")
    suspend fun getMessages(@Path("conversationId") convId: String): Response<List<Message>>

    @PUT("chat/read/{conversationId}")
    suspend fun markAsRead(@Path("conversationId") convId: String): Response<Map<String, String>>

    @GET("chat/unread-count")
    suspend fun getUnreadCount(): Response<UnreadCountResponse>

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: String): Response<Map<String, String>>
}
