package com.app.kotlinmode.ui.profile

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
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.app.kotlinmode.model.Post
import com.app.kotlinmode.model.User
import com.app.kotlinmode.ui.theme.*
import com.app.kotlinmode.utils.Resource
import com.app.kotlinmode.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    profileId: String,
    currentUserId: String,
    onLogout: () -> Unit,
    onMessageClick: (receiverId: String) -> Unit,
    onFollowersClick: (List<String>) -> Unit,
    onFollowingClick: (List<String>) -> Unit
) {
    LaunchedEffect(profileId) { viewModel.loadProfile(profileId) }

    val state by viewModel.state.collectAsState()
    val postsState by viewModel.posts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold, color = TextPrimary) },
                actions = {
                    if (profileId == currentUserId) {
                        IconButton(onClick = { viewModel.logout(onLogout) }) {
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
                is Resource.Error   -> Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
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
                                onMessageClick = { onMessageClick(profileId) },
                                onFollowersClick = { onFollowersClick(user.followers) },
                                onFollowingClick = { onFollowingClick(user.following) }
                            )
                            
                            UserPostsGrid(postsState)
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
    onFollowingClick: () -> Unit
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
                modifier = Modifier.size(86.dp).clip(CircleShape)
                    .background(Brush.linearGradient(InstagramGradient)),
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
        
        if (!isOwnProfile) {
            Spacer(Modifier.height(20.dp))
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
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Message", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        } else {
             Spacer(Modifier.height(16.dp))
             Button(
                onClick = {}, // Edit Profile Placeholder
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
private fun UserPostsGrid(state: Resource<List<Post>>) {
    when (state) {
        is Resource.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BrandPrimary) }
        is Resource.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.message ?: "Error", color = ErrorRed) }
        is Resource.Success -> {
            val posts = state.data ?: emptyList()
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
                        AsyncImage(
                            model = post.image,
                            contentDescription = "Post",
                            modifier = Modifier.aspectRatio(1f).background(DarkSurface),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, count: Int, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(count.toString(), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, color = TextPrimary, fontSize = 13.sp)
    }
}
