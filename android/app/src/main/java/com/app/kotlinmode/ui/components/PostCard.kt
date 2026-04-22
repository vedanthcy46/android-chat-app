package com.app.kotlinmode.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import com.app.kotlinmode.ui.theme.*

@Composable
fun PostCard(
    post: Post,
    currentUserId: String,
    onLike: () -> Unit,
    onSave: () -> Unit,
    onComment: () -> Unit,
    onProfileClick: () -> Unit
) {
    val isLiked = post.likes.contains(currentUserId)
    val isSaved = post.saves.contains(currentUserId)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .animateContentSize()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onProfileClick
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(InstagramGradient)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .clip(CircleShape)
                        .background(DarkBackground),
                    contentAlignment = Alignment.Center
                ) {
                    if (!post.user.profilePicture.isNullOrBlank()) {
                        AsyncImage(
                            model = post.user.profilePicture,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = post.user.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = post.user.username,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }

        // Image with subtle fade-in
        if (!post.image.isNullOrBlank()) {
            AsyncImage(
                model = post.image,
                contentDescription = "Post image",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Instagram Square Ratio
                    .background(DarkSurface),
                contentScale = ContentScale.Crop
            )
        }

        // Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onLike) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) BrandSecondary else TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            IconButton(onClick = onComment) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Comment",
                    tint = TextPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onSave) {
                Icon(
                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Save",
                    tint = if (isSaved) Color.White else TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Likes & Caption
        Column(modifier = Modifier.padding(horizontal = 14.dp)) {
            if (post.likes.isNotEmpty()) {
                Text(
                    text = "${post.likes.size} likes",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            
            if (!post.description.isNullOrBlank()) {
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = post.user.username,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = post.description,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                }
            }
            
            if (post.comments.isNotEmpty()) {
                Text(
                    text = "View all ${post.comments.size} comments",
                    color = TextMuted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 6.dp).clickable { onComment() }
                )
            }
            
            Text(
                text = "JUST NOW", // Simplified for demo
                color = TextMuted,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
            )
        }
    }
}
