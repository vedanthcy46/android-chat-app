package com.app.kotlinmode.navigation

/**
 * All navigation routes in a single sealed class.
 *
 * Why sealed class?
 *  - Exhaustive when() checks — the compiler tells you if you miss a screen
 *  - Routes are typed constants, not magic strings
 *
 * Usage:
 *   navController.navigate(Screen.Feed.route)
 *   navController.navigate(Screen.Chat.buildRoute("convId123", "userId456"))
 */
sealed class Screen(val route: String) {

    // ── Auth ──────────────────────────────────────────
    object Login    : Screen("login")
    object Register : Screen("register")

    // ── Main tabs ─────────────────────────────────────
    object Feed     : Screen("feed")
    object Reels    : Screen("reels")
    object Search   : Screen("search")
    object ChatList   : Screen("chat_list")
    object Profile    : Screen("profile")
    object CreatePost : Screen("create_post")

    // ── Detail screens with arguments ─────────────────
    object Chat : Screen("chat/{conversationId}/{receiverId}") {
        fun buildRoute(conversationId: String, receiverId: String) =
            "chat/$conversationId/$receiverId"
    }

    object UserProfile : Screen("user_profile/{userId}") {
        fun buildRoute(userId: String) = "user_profile/$userId"
    }

    object UserList : Screen("user_list/{title}/{userIds}") {
        fun buildRoute(title: String, userIds: String) = "user_list/$title/$userIds"
    }

    object PostDetail : Screen("post_detail/{postId}") {
        fun buildRoute(postId: String) = "post_detail/$postId"
    }
}
