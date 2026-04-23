package com.app.kotlinmode.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.kotlinmode.model.Post
import com.app.kotlinmode.ui.components.CommentBottomSheet
import com.app.kotlinmode.ui.components.PostCard
import com.app.kotlinmode.ui.theme.*
import com.app.kotlinmode.utils.Resource
import com.app.kotlinmode.viewmodel.FeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    currentUserId: String,
    onNavigateToCreatePost: () -> Unit,
    onUserClick: (String) -> Unit
) {
    val feedState by viewModel.state.collectAsState()
    var selectedPostForComments by remember { mutableStateOf<Post?>(null) }
    var showComments by remember { mutableStateOf(false) }

    // Synchronize selectedPost with feedState when feed updates
    LaunchedEffect(feedState) {
        if (showComments && selectedPostForComments != null) {
            val updatedList = (feedState as? Resource.Success)?.data ?: return@LaunchedEffect
            selectedPostForComments = updatedList.find { it.id == selectedPostForComments!!.id }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreatePost,
                containerColor = BrandPrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Post")
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("✦ KotlinMode", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = TextPrimary) },
                actions = {
                    IconButton(onClick = { viewModel.loadFeed() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = BrandPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(DarkBackground)) {
            when (val state = feedState) {
                is Resource.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = BrandPrimary)
                is Resource.Error -> Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message ?: "Error", color = ErrorRed, fontSize = 16.sp)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.loadFeed() }) { Text("Retry") }
                }
                is Resource.Success -> {
                    val posts = state.data ?: emptyList()
                    if (posts.isEmpty()) {
                        Text("No posts yet. Follow people to see their posts!", modifier = Modifier.align(Alignment.Center), color = TextSecondary, fontSize = 15.sp)
                    } else {
                        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                            items(posts, key = { it.id }) { post ->
                                PostCard(
                                    post = post,
                                    currentUserId = currentUserId,
                                    onLike = { viewModel.likePost(post.id, currentUserId) },
                                    onSave = { viewModel.savePost(post.id) },
                                    onComment = { 
                                        selectedPostForComments = post
                                        showComments = true
                                    },
                                    onProfileClick = { onUserClick(post.user.id) },
                                    onDelete = { viewModel.deletePost(post.id) }
                                )
                            }
                        }
                    }
                }
            }

            if (showComments && selectedPostForComments != null) {
                CommentBottomSheet(
                    post = selectedPostForComments!!,
                    onAddComment = { viewModel.addComment(selectedPostForComments!!.id, it) },
                    onAddReply = { commentId, text -> viewModel.addReply(selectedPostForComments!!.id, commentId, text) },
                    onDismiss = { showComments = false }
                )
            }
        }
    }
}
