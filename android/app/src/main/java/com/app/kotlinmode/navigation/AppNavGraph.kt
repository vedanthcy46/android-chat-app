package com.app.kotlinmode.navigation

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.app.kotlinmode.network.RetrofitInstance
import com.app.kotlinmode.network.SocketManager
import com.app.kotlinmode.repository.*
import com.app.kotlinmode.ui.auth.*
import com.app.kotlinmode.ui.chat.*
import com.app.kotlinmode.ui.components.BottomNavBar
import com.app.kotlinmode.ui.feed.*
import com.app.kotlinmode.ui.profile.*
import com.app.kotlinmode.ui.post.*
import com.app.kotlinmode.ui.search.*
import com.app.kotlinmode.ui.theme.DarkBackground
import com.app.kotlinmode.viewmodel.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

// Screen is defined in Screen.kt — no duplicate here

@Composable
fun AppNavGraph(context: Context) {
    val navController = rememberNavController()

    // Build shared dependencies once
    val session    = remember { SessionManager(context) }
    val apiService = remember { RetrofitInstance.getApi(context) }

    val authRepo   = remember { AuthRepository(apiService, session) }
    val userRepo   = remember { UserRepository(apiService) }
    val postRepo   = remember { PostRepository(apiService) }
    val chatRepo   = remember { ChatRepository(apiService) }

    // Shared ViewModels
    val authViewModel       = remember { AuthViewModel(authRepo) }
    val feedViewModel       = remember { FeedViewModel(postRepo) }
    val searchViewModel     = remember { SearchViewModel(userRepo) }
    val profileViewModel    = remember { ProfileViewModel(userRepo, authRepo, postRepo, chatRepo) }
    val chatViewModel       = remember { ChatViewModel(chatRepo) }
    val createPostViewModel = remember { CreatePostViewModel(postRepo) }

    // Determine start destination based on stored JWT
    val startDest = remember {
        val token = runBlocking { session.getToken().first() }
        if (token.isNullOrBlank()) Screen.Login.route else Screen.Feed.route
    }

    val currentUserId by session.getUserId().collectAsState(initial = "")

    // Connect Socket.IO when user ID is available
    LaunchedEffect(currentUserId) {
        if (!currentUserId.isNullOrBlank()) {
            SocketManager.connect(currentUserId!!)
        }
    }

    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    val bottomNavRoutes = setOf(
        Screen.Feed.route, Screen.Search.route,
        Screen.CreatePost.route, Screen.ChatList.route, Screen.Profile.route
    )
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(currentRoute = currentRoute) { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Feed.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDest,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Feed.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    viewModel = authViewModel,
                    onRegisterSuccess = {
                        navController.navigate(Screen.Feed.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            composable(Screen.Feed.route) {
                FeedScreen(
                    viewModel = feedViewModel,
                    currentUserId = currentUserId ?: "",
                    onNavigateToCreatePost = { navController.navigate(Screen.CreatePost.route) },
                    onUserClick = { userId ->
                        navController.navigate(Screen.UserProfile.buildRoute(userId))
                    }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    viewModel = searchViewModel,
                    onUserClick = { userId ->
                        navController.navigate(Screen.UserProfile.buildRoute(userId))
                    }
                )
            }

            composable(Screen.ChatList.route) {
                ConversationListScreen(
                    viewModel = chatViewModel,
                    currentUserId = currentUserId ?: "",
                    onConversationClick = { convId, otherId ->
                        navController.navigate(Screen.Chat.buildRoute(convId, otherId))
                    }
                )
            }

            composable(
                route = Screen.Chat.route,
                arguments = listOf(
                    navArgument("conversationId") { type = NavType.StringType },
                    navArgument("receiverId")     { type = NavType.StringType }
                )
            ) { backStack ->
                val conversationId = backStack.arguments?.getString("conversationId") ?: ""
                val receiverId     = backStack.arguments?.getString("receiverId") ?: ""
                ChatScreen(
                    viewModel = chatViewModel,
                    conversationId = conversationId,
                    currentUserId = currentUserId ?: "",
                    receiverId = receiverId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = profileViewModel,
                    profileId = currentUserId ?: "",
                    currentUserId = currentUserId ?: "",
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onMessageClick = { },
                    onFollowersClick = { ids ->
                        navController.navigate(Screen.UserList.buildRoute("Followers", ids.joinToString(",")))
                    },
                    onFollowingClick = { ids ->
                        navController.navigate(Screen.UserList.buildRoute("Following", ids.joinToString(",")))
                    }
                )
            }

            composable(
                route = Screen.UserProfile.route,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                ProfileScreen(
                    viewModel = profileViewModel,
                    profileId = userId,
                    currentUserId = currentUserId ?: "",
                    onLogout = { },
                    onMessageClick = { receiverId ->
                        profileViewModel.startChat(receiverId) { conversationId ->
                            navController.navigate(Screen.Chat.buildRoute(conversationId, receiverId))
                        }
                    },
                    onFollowersClick = { ids ->
                        navController.navigate(Screen.UserList.buildRoute("Followers", ids.joinToString(",")))
                    },
                    onFollowingClick = { ids ->
                        navController.navigate(Screen.UserList.buildRoute("Following", ids.joinToString(",")))
                    }
                )
            }

            composable(
                route = Screen.UserList.route,
                arguments = listOf(
                    navArgument("title") { type = NavType.StringType },
                    navArgument("userIds") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val title = backStackEntry.arguments?.getString("title") ?: ""
                val userIdsStr = backStackEntry.arguments?.getString("userIds") ?: ""
                val userIds = if (userIdsStr.isBlank()) emptyList() else userIdsStr.split(",")
                
                UserListScreen(
                    userRepo = userRepo,
                    title = title,
                    userIds = userIds,
                    onUserClick = { userId ->
                        navController.navigate(Screen.UserProfile.buildRoute(userId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.CreatePost.route) {
                CreatePostScreen(
                    viewModel = createPostViewModel,
                    onPostCreated = {
                        navController.popBackStack()
                        feedViewModel.loadFeed()
                    }
                )
            }
        }
    }
}
