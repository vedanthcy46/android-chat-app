package com.app.kotlinmode.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.app.kotlinmode.model.Comment
import com.app.kotlinmode.model.Post
import com.app.kotlinmode.model.Reply
import com.app.kotlinmode.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBottomSheet(
    post: Post,
    onAddComment: (String) -> Unit,
    onAddReply: (String, String) -> Unit, // commentId, text
    onDismiss: () -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<Comment?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextMuted) },
        windowInsets = WindowInsets.ime // This is critical for keyboard visibility
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.85f)
                .navigationBarsPadding()
        ) {
            Text(
                "Comments",
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp),
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Divider(color = DarkCard)

            LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                items(post.comments) { comment ->
                    CommentItem(
                        comment = comment,
                        onReplyClick = { 
                            replyingTo = comment
                            commentText = "@${comment.user.username} "
                        }
                    )
                }
            }

            // Input Area
            Column(modifier = Modifier.background(DarkSurface).padding(12.dp)) {
                if (replyingTo != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Replying to ${replyingTo!!.user.username}", color = TextMuted, fontSize = 12.sp)
                        Text(
                            "Cancel",
                            color = BrandPrimary,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable { replyingTo = null; commentText = "" }
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Add a comment...", color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DarkCard,
                            unfocusedContainerColor = DarkCard,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                if (replyingTo != null) {
                                    onAddReply(replyingTo!!.id, commentText)
                                    replyingTo = null
                                } else {
                                    onAddComment(commentText)
                                }
                                commentText = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = BrandPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment, onReplyClick: () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            AsyncImage(
                model = comment.user.profilePicture,
                contentDescription = null,
                modifier = Modifier.size(32.dp).clip(CircleShape).background(DarkCard),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(comment.user.username, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(comment.createdAt.take(10), color = TextMuted, fontSize = 11.sp)
                }
                Text(comment.text, color = TextPrimary, fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
                
                Text(
                    "Reply",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp).clickable { onReplyClick() }
                )
            }
        }
        
        // Replies
        if (comment.replies.isNotEmpty()) {
            comment.replies.forEach { reply ->
                ReplyItem(reply)
            }
        }
    }
}

@Composable
fun ReplyItem(reply: Reply) {
    Row(modifier = Modifier.padding(start = 44.dp, top = 12.dp), verticalAlignment = Alignment.Top) {
        AsyncImage(
            model = reply.user.profilePicture,
            contentDescription = null,
            modifier = Modifier.size(24.dp).clip(CircleShape).background(DarkCard),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(reply.user.username, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp)
                Spacer(Modifier.width(8.dp))
                Text(reply.createdAt.take(10), color = TextMuted, fontSize = 10.sp)
            }
            Text(reply.text, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp))
        }
    }
}
