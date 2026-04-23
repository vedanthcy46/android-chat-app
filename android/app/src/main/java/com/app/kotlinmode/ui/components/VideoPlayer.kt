package com.app.kotlinmode.ui.components

import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout

/**
 * A reusable Video Player component for Jetpack Compose.
 * Uses Media3 ExoPlayer.
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    playWhenReady: Boolean = true
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Initialize ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            if (videoUrl.isNotBlank()) {
                val mediaItem = MediaItem.fromUri(videoUrl)
                setMediaItem(mediaItem)
                repeatMode = Player.REPEAT_MODE_ONE
                prepare()
                Log.d("VideoPlayer", "ExoPlayer initialized for URL: $videoUrl")
            }
            
            addListener(object : Player.Listener {
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    Log.e("VideoPlayer", "Playback Error: ${error.message}, URL: $videoUrl")
                }
            })
        }
    }

    // Handle lifecycle events
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.pause()
                    Log.d("VideoPlayer", "Lifecycle ON_PAUSE: Video Paused")
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (playWhenReady) {
                        exoPlayer.play()
                        Log.d("VideoPlayer", "Lifecycle ON_RESUME: Video Playing")
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
            Log.d("VideoPlayer", "ExoPlayer released")
        }
    }

    // Sync playWhenReady state
    LaunchedEffect(playWhenReady) {
        exoPlayer.playWhenReady = playWhenReady
    }

    var isPlaying by remember { mutableStateOf(playWhenReady) }

    // Sync playWhenReady state from parent
    LaunchedEffect(playWhenReady) {
        isPlaying = playWhenReady
        exoPlayer.playWhenReady = playWhenReady
    }

    // The Player View
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable {
                isPlaying = !isPlaying
                exoPlayer.playWhenReady = isPlaying
                Log.d("VideoPlayer", "Toggled Playback: $isPlaying")
            }
    ) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Optional: Show play/pause icon overlay briefly on click
        if (!isPlaying) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Paused",
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.Center)
            )
        }
    }
}
