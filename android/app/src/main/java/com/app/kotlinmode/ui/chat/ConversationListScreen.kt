package com.app.kotlinmode.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
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
import com.app.kotlinmode.model.ConversationMember
import com.app.kotlinmode.ui.theme.*
import com.app.kotlinmode.utils.Resource
import com.app.kotlinmode.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    viewModel: ChatViewModel,
    currentUserId: String,
    onConversationClick: (conversationId: String, otherUserId: String, receiverName: String) -> Unit
) {
    LaunchedEffect(Unit) { 
        viewModel.loadConversations() 
        viewModel.startGlobalListeners()
    }

    val state by viewModel.conversations.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).background(DarkBackground)) {
            when (val s = state) {
                is Resource.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = BrandPrimary)
                is Resource.Error   -> Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.message ?: "Error", color = ErrorRed)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.loadConversations() }) { Text("Retry") }
                }
                is Resource.Success -> {
                    val convos = s.data ?: emptyList()
                    if (convos.isEmpty()) {
                        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Chat, null, tint = TextMuted, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("No conversations yet", color = TextSecondary, fontSize = 16.sp)
                        }
                    } else {
                        LazyColumn {
                            items(convos, key = { it.id }) { convo ->
                                val other: ConversationMember? = convo.members.firstOrNull { it.id != currentUserId }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (other != null) onConversationClick(convo.id, other.id, other.username)
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box {
                                        Box(
                                            modifier = Modifier.size(50.dp).clip(CircleShape)
                                                .background(Brush.linearGradient(listOf(BrandPrimary, BrandSecondary))),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = other?.username?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp
                                            )
                                        }
                                        
                                        // Online status dot
                                        if (other?.isOnline == true) {
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .align(Alignment.BottomEnd)
                                                    .background(Color.Black, CircleShape)
                                                    .padding(2.dp)
                                                    .background(Color.Green, CircleShape)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.width(14.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = other?.username ?: "Unknown",
                                            color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp
                                        )
                                        Text(
                                            text = convo.lastMessage?.text ?: "Tap to chat",
                                            color = if (convo.unreadCount > 0) TextPrimary else TextMuted,
                                            fontWeight = if (convo.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }

                                    // Unread badge
                                    if (convo.unreadCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(BrandPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${convo.unreadCount}",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkCard))
                            }
                        }
                    }
                }
            }
        }
    }
}
