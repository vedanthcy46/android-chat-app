package com.app.kotlinmode.ui.post

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.kotlinmode.ui.components.CommentBottomSheet
import com.app.kotlinmode.ui.components.PostCard
import com.app.kotlinmode.ui.theme.*
import com.app.kotlinmode.utils.Resource
import com.app.kotlinmode.viewmodel.PostDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    viewModel: PostDetailViewModel,
    postId: String,
    currentUserId: String,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit
) {
    val postState by viewModel.post.collectAsState()
    var showComments by remember { mutableStateOf(false) }

    LaunchedEffect(postId) {
        viewModel.loadPost(postId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val s = postState) {
                is Resource.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = BrandPrimary)
                is Resource.Error -> Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.message ?: "Error", color = ErrorRed)
                    Button(onClick = { viewModel.loadPost(postId) }, modifier = Modifier.padding(top = 8.dp)) { Text("Retry") }
                }
                is Resource.Success -> {
                    val post = s.data!!
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        PostCard(
                            post = post,
                            currentUserId = currentUserId,
                            onLike = { viewModel.likePost(post.id) },
                            onSave = { viewModel.savePost(post.id) },
                            onComment = { showComments = true },
                            onProfileClick = { onUserClick(post.user.id) }
                        )
                    }

                    if (showComments) {
                        CommentBottomSheet(
                            post = post,
                            onAddComment = { viewModel.addComment(post.id, it) },
                            onAddReply = { commentId, text -> viewModel.addReply(post.id, commentId, text) },
                            onDismiss = { showComments = false }
                        )
                    }
                }
            }
        }
    }
}
