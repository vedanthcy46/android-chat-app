package com.app.kotlinmode.navigation

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
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
import com.app.kotlinmode.ui.reels.*
import com.app.kotlinmode.ui.theme.DarkBackground
import com.app.kotlinmode.viewmodel.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import com.google.firebase.messaging.FirebaseMessaging

@Composable
fun AppNavGraph(
    context: Context,
    navController: androidx.navigation.NavHostController = rememberNavController()
) {
    val localContext = LocalContext.current

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
    val postDetailViewModel = remember { PostDetailViewModel(postRepo) }

    // Determine start destination
    var startDest by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        val token = session.getToken().first()
        startDest = if (token.isNullOrBlank()) Screen.Login.route else Screen.Feed.route
    }

    val currentUserId by session.getUserId().collectAsState(initial = "")

    // Connect Socket.IO and sync FCM token when user ID is available
    LaunchedEffect(currentUserId) {
        if (!currentUserId.isNullOrBlank()) {
            SocketManager.connect(currentUserId!!)
            
            // Sync FCM Token to Backend
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    userRepo.updateFcmToken(token).launchIn(scope = this)
                }
            }
        }
    }

    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    val bottomNavRoutes = setOf(
        Screen.Feed.route, Screen.Reels.route, Screen.Search.route,
        Screen.CreatePost.route, Screen.ChatList.route, Screen.Profile.route
    )
    val showBottomBar = currentRoute in bottomNavRoutes

    if (startDest == null) return // Wait for token check

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(currentRoute = currentRoute) { route ->
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == androidx.lifecycle.Lifecycle.State.RESUMED) {
                        navController.navigate(route) {
                            popUpTo(Screen.Feed.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDest!!,
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

            composable(Screen.Reels.route) {
                ReelsScreen(
                    viewModel = feedViewModel,
                    currentUserId = currentUserId ?: ""
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
                    onConversationClick = { convId, otherId, receiverName ->
                        if (navController.currentBackStackEntry?.lifecycle?.currentState == androidx.lifecycle.Lifecycle.State.RESUMED) {
                            chatViewModel.setReceiverName(receiverName)
                            navController.navigate(Screen.Chat.buildRoute(convId, otherId))
                        }
                    }
                )
            }

            composable(
                route = Screen.Chat.route,
                arguments = listOf(
                    navArgument("conversationId") { type = NavType.StringType },
                    navArgument("receiverId")     { type = NavType.StringType }
                ),
                deepLinks = listOf(
                    navDeepLink { uriPattern = "https://kotlinmode.app/chat/{conversationId}/{receiverId}" }
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
                    onNavigateToChat = { conversationId, receiverId, receiverName ->
                        if (conversationId.isBlank() || receiverId.isBlank()) return@ProfileScreen
                        try {
                            if (navController.currentBackStackEntry?.lifecycle?.currentState == androidx.lifecycle.Lifecycle.State.RESUMED) {
                                chatViewModel.setReceiverName(receiverName)
                                navController.navigate(Screen.Chat.buildRoute(conversationId, receiverId))
                            }
                        } catch (e: Exception) {
                            Log.e("NavGraph", "Nav error: ${e.message}")
                            Toast.makeText(localContext, "Navigation failed", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onFollowersClick = { ids ->
                        navController.navigate(Screen.UserList.buildRoute("Followers", ids.joinToString(",")))
                    },
                    onFollowingClick = { ids ->
                        navController.navigate(Screen.UserList.buildRoute("Following", ids.joinToString(",")))
                    },
                    onPostClick = { postId ->
                        navController.navigate(Screen.PostDetail.buildRoute(postId))
                    }
                )
            }

            composable(
                route = Screen.UserProfile.route,
                arguments = listOf(navArgument("userId") { type = NavType.StringType }),
                deepLinks = listOf(
                    navDeepLink { uriPattern = "https://kotlinmode.app/profile/{userId}" }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                ProfileScreen(
                    viewModel = profileViewModel,
                    profileId = userId,
                    currentUserId = currentUserId ?: "",
                    onLogout = { },
                    onNavigateToChat = { conversationId, receiverId, receiverName ->
                        if (conversationId.isBlank() || receiverId.isBlank()) return@ProfileScreen
                        try {
                            if (navController.currentBackStackEntry?.lifecycle?.currentState == androidx.lifecycle.Lifecycle.State.RESUMED) {
                                chatViewModel.setReceiverName(receiverName)
                                navController.navigate(Screen.Chat.buildRoute(conversationId, receiverId))
                            }
                        } catch (e: Exception) {
                            Log.e("NavGraph", "Nav error: ${e.message}")
                            Toast.makeText(localContext, "Navigation failed", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onFollowersClick = { ids ->
                        navController.navigate(Screen.UserList.buildRoute("Followers", ids.joinToString(",")))
                    },
                    onFollowingClick = { ids ->
                        navController.navigate(Screen.UserList.buildRoute("Following", ids.joinToString(",")))
                    },
                    onPostClick = { postId ->
                        navController.navigate(Screen.PostDetail.buildRoute(postId))
                    }
                )
            }

            composable(
                route = Screen.PostDetail.route,
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: ""
                PostDetailScreen(
                    viewModel = postDetailViewModel,
                    postId = postId,
                    currentUserId = currentUserId ?: "",
                    onBack = { navController.popBackStack() },
                    onUserClick = { userId ->
                        navController.navigate(Screen.UserProfile.buildRoute(userId))
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
