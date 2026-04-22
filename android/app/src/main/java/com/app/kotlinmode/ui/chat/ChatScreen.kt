package com.app.kotlinmode.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.kotlinmode.ui.theme.*
import com.app.kotlinmode.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    conversationId: String,
    currentUserId: String,
    receiverId: String,
    onBack: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val messages by viewModel.messages.collectAsState()

    LaunchedEffect(conversationId) {
        viewModel.loadMessages(conversationId)
        viewModel.startListening(conversationId, currentUserId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) scope.launch { listState.animateScrollToItem(messages.size - 1) }
    }

    DisposableEffect(Unit) { onDispose { viewModel.stopListening() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = TextPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground,
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(DarkSurface).padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText, onValueChange = { messageText = it },
                    placeholder = { Text("Type a message...", color = TextMuted) },
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPrimary, unfocusedBorderColor = TextMuted,
                        cursorColor = BrandPrimary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendMessage(conversationId, currentUserId, receiverId, messageText.trim())
                            messageText = ""
                        }
                    }),
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendMessage(conversationId, currentUserId, receiverId, messageText.trim())
                            messageText = ""
                        }
                    },
                    modifier = Modifier.size(48.dp).background(
                        Brush.linearGradient(listOf(BrandPrimary, BrandSecondary)),
                        shape = RoundedCornerShape(50)
                    )
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).background(DarkBackground).padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isOwn = msg.sender == currentUserId
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start) {
                    Box(
                        modifier = Modifier.widthIn(max = 280.dp).background(
                            if (isOwn) Brush.linearGradient(listOf(BrandPrimary, BrandSecondary))
                            else Brush.linearGradient(listOf(DarkCard, DarkSurface)),
                            shape = RoundedCornerShape(
                                topStart = 18.dp, topEnd = 18.dp,
                                bottomStart = if (isOwn) 18.dp else 4.dp,
                                bottomEnd = if (isOwn) 4.dp else 18.dp
                            )
                        ).padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column {
                            Text(msg.text, color = Color.White, fontSize = 15.sp)
                            Text(msg.createdAt.take(16), color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, modifier = Modifier.align(Alignment.End))
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}
