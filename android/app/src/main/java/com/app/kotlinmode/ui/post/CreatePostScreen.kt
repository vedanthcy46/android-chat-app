package com.app.kotlinmode.ui.post

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.app.kotlinmode.ui.theme.*
import com.app.kotlinmode.utils.Resource
import com.app.kotlinmode.utils.UriUtils
import com.app.kotlinmode.viewmodel.CreatePostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    viewModel: CreatePostViewModel,
    onPostCreated: () -> Unit
) {
    val context = LocalContext.current
    var caption by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val uploadState by viewModel.uploadState.collectAsState()
    val createState by viewModel.createState.collectAsState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(createState) {
        if (createState is Resource.Success) {
            viewModel.resetState()
            onPostCreated()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create Post", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DarkBackground),
                actions = {
                    TextButton(
                        onClick = {
                            selectedImageUri?.let { uri ->
                                UriUtils.uriToMultipart(context, uri, "image")?.let { part ->
                                    viewModel.uploadAndCreatePost(part, caption)
                                }
                            }
                        },
                        enabled = selectedImageUri != null && uploadState !is Resource.Loading && createState !is Resource.Loading
                    ) {
                        Text("Post", color = if (selectedImageUri != null) BrandPrimary else TextMuted, fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image Preview Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkCard)
                    .padding(if (selectedImageUri == null) 40.dp else 0.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { selectedImageUri = null },
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.Black.copy(0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddPhotoAlternate, null, tint = TextMuted, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                        ) {
                            Text("Select from Gallery")
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                placeholder = { Text("Write a caption...", color = TextMuted) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPrimary,
                    unfocusedBorderColor = DarkCard,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(Modifier.height(24.dp))

            // Progress/States
            when {
                uploadState is Resource.Loading -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BrandPrimary)
                        Spacer(Modifier.width(12.dp))
                        Text("Uploading image...", color = TextSecondary)
                    }
                }
                createState is Resource.Loading -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BrandPrimary)
                        Spacer(Modifier.width(12.dp))
                        Text("Creating post...", color = TextSecondary)
                    }
                }
                uploadState is Resource.Error -> {
                    Text((uploadState as Resource.Error).message ?: "Upload failed", color = ErrorRed)
                }
                createState is Resource.Error -> {
                    Text((createState as Resource.Error).message ?: "Creation failed", color = ErrorRed)
                }
            }
        }
    }
}
