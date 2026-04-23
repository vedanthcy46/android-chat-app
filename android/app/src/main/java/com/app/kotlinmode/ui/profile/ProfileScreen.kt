package com.app.kotlinmode.ui.profile

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.app.kotlinmode.model.Post
import com.app.kotlinmode.model.User
import com.app.kotlinmode.ui.components.EditProfileBottomSheet
import com.app.kotlinmode.ui.theme.*
import com.app.kotlinmode.utils.Resource
import com.app.kotlinmode.viewmodel.ProfileEvent
import com.app.kotlinmode.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    profileId: String,
    currentUserId: String,
    onLogout: () -> Unit,
    onNavigateToChat: (conversationId: String, receiverId: String, receiverName: String) -> Unit,
    onFollowersClick: (List<String>) -> Unit,
    onFollowingClick: (List<String>) -> Unit,
    onPostClick: (String) -> Unit
) {
    val context = LocalContext.current
    val currentOnNavigateToChat by rememberUpdatedState(onNavigateToChat)
    
    LaunchedEffect(profileId) { viewModel.loadProfile(profileId) }

    // Listen for navigation and toast events
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.NavigateToChat -> {
                    Log.d("ProfileScreen", "Navigating to chat: ${event.conversationId}")
                    currentOnNavigateToChat(event.conversationId, event.receiverId, event.receiverName)
                }
                is ProfileEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val state by viewModel.state.collectAsState()
    val postsState by viewModel.posts.collectAsState()
    var showEditProfile by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold, color = TextPrimary) },
                actions = {
                    if (profileId == currentUserId) {
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Default.Logout, contentDescription = "Logout", tint = ErrorRed)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(DarkBackground)) {
            when (val s = state) {
                is Resource.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = BrandPrimary)
                is Resource.Error -> Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.message ?: "Error", color = ErrorRed)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.loadProfile(profileId) }) { Text("Retry") }
                }
                is Resource.Success -> {
                    s.data?.let { user ->
                        Column(Modifier.fillMaxSize()) {
                            ProfileHeader(
                                user = user,
                                isOwnProfile = profileId == currentUserId,
                                isFollowing = user.followers.contains(currentUserId),
                                onFollowToggle = { viewModel.followUser(profileId) },
                                onMessageClick = { 
                                    Log.d("ProfileScreen", "Message clicked")
                                    viewModel.startChat(profileId) 
                                },
                                onFollowersClick = { onFollowersClick(user.followers) },
                                onFollowingClick = { onFollowingClick(user.following) },
                                onEditProfileClick = { showEditProfile = true },
                                isStartingChat = viewModel.isStartingChat.collectAsState().value
                            )
                            
                            UserPostsGrid(
                                postsState = postsState, 
                                isOwnProfile = profileId == currentUserId,
                                onPostClick = onPostClick,
                                onDeletePost = { viewModel.deletePost(it) }
                             )
                        }

                        if (showEditProfile) {
                            EditProfileBottomSheet(
                                user = user,
                                onSave = { username, bio, pic -> viewModel.updateProfile(username, bio, pic) },
                                onDismiss = { showEditProfile = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    user: User,
    isOwnProfile: Boolean,
    isFollowing: Boolean,
    onFollowToggle: () -> Unit,
    onMessageClick: () -> Unit,
    onFollowersClick: () -> Unit,
    onFollowingClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    isStartingChat: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth().animateContentSize().padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.size(85.dp).clip(CircleShape).background(Brush.linearGradient(InstagramGradient)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(DarkBackground),
                    contentAlignment = Alignment.Center
                ) {
                    if (!user.profilePicture.isNullOrBlank()) {
                        AsyncImage(model = user.profilePicture, contentDescription = "Avatar", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                    } else {
                        Text(
                            text = user.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                StatItem("Posts", 0, onClick = {}) // Simplified
                Spacer(Modifier.width(20.dp))
                StatItem("Followers", user.followers.size, onClick = onFollowersClick)
                Spacer(Modifier.width(20.dp))
                StatItem("Following", user.following.size, onClick = onFollowingClick)
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Text(user.username, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (!user.bio.isNullOrBlank()) {
                Text(user.bio, color = TextPrimary, fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
            }
        }
        
        Spacer(Modifier.height(20.dp))

        if (!isOwnProfile) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onFollowToggle,
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFollowing) DarkSurface else BrandPrimary,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(if (isFollowing) "Following" else "Follow", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Button(
                    onClick = onMessageClick,
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface, contentColor = TextPrimary),
                    contentPadding = PaddingValues(0.dp),
                    enabled = !isStartingChat
                ) {
                    if (isStartingChat) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BrandPrimary, strokeWidth = 2.dp)
                    } else {
                        Text("Message", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        } else {
             Button(
                onClick = onEditProfileClick,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(36.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurface, contentColor = TextPrimary),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Edit Profile", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun StatItem(label: String, count: Int, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(count.toString(), color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        Text(label, color = TextMuted, fontSize = 12.sp)
    }
}

@Composable
private fun UserPostsGrid(
    postsState: Resource<List<Post>>, 
    isOwnProfile: Boolean,
    onPostClick: (String) -> Unit,
    onDeletePost: (String) -> Unit
) {
    when (postsState) {
        is Resource.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BrandPrimary) }
        is Resource.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(postsState.message ?: "Error", color = ErrorRed) }
        is Resource.Success -> {
            val posts = postsState.data ?: emptyList()
            if (posts.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    Text("No posts yet", color = TextMuted, fontSize = 15.sp)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(1.dp),
                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    items(posts) { post ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .background(DarkSurface)
                                .clickable { onPostClick(post.id) }
                        ) {
                            // Thumbnail (Post image or video placeholder)
                            AsyncImage(
                                model = if (post.postType == "video") post.videoUrl else post.image,
                                contentDescription = "Post",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Video Indicator (Play Icon)
                            if (post.postType == "video") {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.PlayArrow,
                                    contentDescription = "Video",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                )
                            }

                            // Delete Button (Owner Only)
                            if (isOwnProfile) {
                                IconButton(
                                    onClick = { onDeletePost(post.id) },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(32.dp)
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = ErrorRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
