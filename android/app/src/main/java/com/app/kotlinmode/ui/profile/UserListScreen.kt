package com.app.kotlinmode.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.app.kotlinmode.model.User
import com.app.kotlinmode.repository.UserRepository
import com.app.kotlinmode.ui.theme.*
import com.app.kotlinmode.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    userRepo: UserRepository,
    title: String,
    userIds: List<String>,
    onUserClick: (String) -> Unit,
    onBack: () -> Unit
) {
    var state by remember { mutableStateOf<Resource<List<User>>>(Resource.Loading()) }

    LaunchedEffect(userIds) {
        if (userIds.isEmpty()) {
            state = Resource.Success(emptyList())
        } else {
            userRepo.getUsersByIds(userIds).collect { state = it }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold, color = TextPrimary) },
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
        Box(Modifier.fillMaxSize().padding(padding).background(DarkBackground)) {
            when (val s = state) {
                is Resource.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = BrandPrimary)
                is Resource.Error   -> Text(s.message ?: "Error", color = ErrorRed, modifier = Modifier.align(Alignment.Center))
                is Resource.Success -> {
                    val users = s.data ?: emptyList()
                    if (users.isEmpty()) {
                        Text("No users found", color = TextMuted, modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn {
                            items(users) { user ->
                                UserListItem(user = user, onClick = { onUserClick(user.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserListItem(user: User, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(45.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(BrandPrimary, BrandSecondary))),
            contentAlignment = Alignment.Center
        ) {
            if (!user.profilePicture.isNullOrBlank()) {
                AsyncImage(model = user.profilePicture, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Text(
                    text = user.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    color = Color.White, fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Text(user.username, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkCard).padding(horizontal = 16.dp))
}
