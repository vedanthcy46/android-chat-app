package com.app.kotlinmode.ui.reels

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.kotlinmode.model.Post
import com.app.kotlinmode.ui.components.VideoPlayer
import com.app.kotlinmode.ui.theme.BrandPrimary
import com.app.kotlinmode.ui.theme.DarkBackground
import com.app.kotlinmode.utils.Resource
import com.app.kotlinmode.viewmodel.FeedViewModel

@Composable
fun ReelsScreen(
    viewModel: FeedViewModel,
    currentUserId: String
) {
    val state by viewModel.reels.collectAsState()
    var selectedPostForComments by remember { mutableStateOf<Post?>(null) }
    var showComments by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadReels()
    }

    // Synchronize selectedPost with reels state
    LaunchedEffect(state) {
        if (showComments && selectedPostForComments != null) {
            val updatedList = (state as? Resource.Success)?.data ?: return@LaunchedEffect
            selectedPostForComments = updatedList.find { it.id == selectedPostForComments!!.id }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        when (val s = state) {
            is Resource.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = BrandPrimary)
            is Resource.Error -> Text(s.message ?: "Error", color = Color.White, modifier = Modifier.align(Alignment.Center))
            is Resource.Success -> {
                val reels = s.data ?: emptyList()
                if (reels.isEmpty()) {
                    Text("No reels available", color = Color.White, modifier = Modifier.align(Alignment.Center))
                } else {
                    ReelsPager(
                        reels = reels,
                        currentUserId = currentUserId,
                        viewModel = viewModel,
                        onCommentClick = { post ->
                            selectedPostForComments = post
                            showComments = true
                        }
                    )
                }
            }
        }

        if (showComments && selectedPostForComments != null) {
            com.app.kotlinmode.ui.components.CommentBottomSheet(
                post = selectedPostForComments!!,
                onAddComment = { viewModel.addComment(selectedPostForComments!!.id, it) },
                onAddReply = { commentId, text -> viewModel.addReply(selectedPostForComments!!.id, commentId, text) },
                onDismiss = { showComments = false }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReelsPager(
    reels: List<Post>,
    currentUserId: String,
    viewModel: FeedViewModel,
    onCommentClick: (Post) -> Unit
) {
    // pageCount is reels.size + 1 to show "No more videos" at the end
    val pagerState = rememberPagerState(pageCount = { reels.size + 1 })

    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        beyondBoundsPageCount = 1
    ) { page ->
        if (page == reels.size) {
            // "No more videos" screen
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Share, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No more videos", color = Color.Gray, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("You've caught up with all reels!", color = Color.DarkGray, fontSize = 14.sp)
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.loadReels() },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        Text("Refresh Feed")
                    }
                }
            }
        } else {
            val post = reels[page]
            val isVisible = pagerState.currentPage == page
            
            Box(modifier = Modifier.fillMaxSize()) {
                // Debugging log for Reels playback
                LaunchedEffect(post.id) {
                    Log.d("ReelsPager", "Displaying Post: ${post.id}, URL: ${post.videoUrl}, isVisible: $isVisible")
                }

                // Video Player
                VideoPlayer(
                    videoUrl = post.videoUrl ?: "",
                    modifier = Modifier.fillMaxSize(),
                    playWhenReady = isVisible
                )

                // Gradient Overlay for text visibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                startY = 0.7f
                            )
                        )
                )

                // Interaction Sidebar
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 80.dp, end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val isLiked = post.likes.contains(currentUserId)
                    IconButton(onClick = { viewModel.likePost(post.id) }) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Color.Red else Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text("${post.likes.size}", color = Color.White, fontSize = 12.sp)

                    Spacer(Modifier.height(20.dp))

                    IconButton(onClick = { onCommentClick(post) }) {
                        Icon(Icons.Default.ChatBubbleOutline, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Text("${post.comments.size}", color = Color.White, fontSize = 12.sp)

                    Spacer(Modifier.height(20.dp))

                    IconButton(onClick = { /* Share logic */ }) {
                        Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }

                // User Info
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 32.dp, start = 16.dp, end = 80.dp)
                ) {
                    Text(
                        text = "@${post.user.username}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = post.description ?: "",
                        color = Color.White,
                        fontSize = 14.sp,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
