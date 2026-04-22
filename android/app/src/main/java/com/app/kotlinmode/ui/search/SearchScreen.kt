package com.app.kotlinmode.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.kotlinmode.model.User
import com.app.kotlinmode.ui.theme.*
import com.app.kotlinmode.utils.Resource
import com.app.kotlinmode.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onUserClick: (userId: String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it; viewModel.search(it) },
            placeholder = { Text("Search users...", color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = BrandPrimary) },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandPrimary, unfocusedBorderColor = TextMuted,
                cursorColor = BrandPrimary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
            ),
            singleLine = true
        )

        when (val s = state) {
            null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Search for people to follow", color = TextSecondary)
            }
            is Resource.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandPrimary)
            }
            is Resource.Error -> Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(s.message ?: "Error", color = ErrorRed)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { viewModel.search(query) }) { Text("Retry") }
            }
            is Resource.Success -> {
                LazyColumn {
                    items(s.data ?: emptyList(), key = { it.id }) { user ->
                        UserSearchItem(
                            user = user,
                            onUserClick = { onUserClick(user.id) },
                            onFollow = { viewModel.followUser(user.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserSearchItem(user: User, onUserClick: () -> Unit, onFollow: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUserClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(46.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(BrandPrimary, BrandSecondary))),
            contentAlignment = Alignment.Center
        ) {
            Text(user.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(user.username, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(user.email ?: "", color = TextMuted, fontSize = 12.sp)
        }
        IconButton(onClick = onFollow) {
            Icon(Icons.Default.PersonAdd, contentDescription = "Follow", tint = BrandPrimary)
        }
    }
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkCard))
}
