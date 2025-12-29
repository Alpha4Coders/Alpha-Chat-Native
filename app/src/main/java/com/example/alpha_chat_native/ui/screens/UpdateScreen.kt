package com.example.alpha_chat_native.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import java.io.File
import timber.log.Timber

data class StatusUpdate(val userName: String, val imageUri: Uri, val timestamp: String)

@Composable
fun UpdateScreen() {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color.Black
    val context = LocalContext.current

    var myStatus by remember { mutableStateOf<Uri?>(null) }
    var statusList by remember { mutableStateOf(listOf<StatusUpdate>()) }
    var tempImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    
    var viewingStatus by remember { mutableStateOf<StatusUpdate?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            myStatus = tempImageUri
            val newStatus = StatusUpdate("You", tempImageUri!!, "Just now")
            statusList = listOf(newStatus) + statusList // Add to the top of the list
            viewingStatus = newStatus // Immediately view the new status
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val uri = createImageFileUri(context)
                if (uri != null) {
                    tempImageUri = uri
                    cameraLauncher.launch(uri)
                } else {
                     Toast.makeText(context, "Failed to create image URI", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to create image file URI")
                Toast.makeText(context, "Failed to create image file: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        } else {
            Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    containerColor = if(isDark) Color(0xFF39FF14) else Color(0xFFFFB7B2)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Add Status")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Text(
                    text = "Updates",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(16.dp)
                )

                // My Status Section
                myStatus?.let {
                    StatusItem(status = StatusUpdate("My Status", it, "Just now")) {
                        viewingStatus = StatusUpdate("My Status", it, "Just now")
                    }
                } ?: AddStatusItem { permissionLauncher.launch(Manifest.permission.CAMERA) }

                HorizontalDivider(color = Color.Gray.copy(alpha=0.2f))

                Text(
                    "Recent Updates", 
                    fontWeight = FontWeight.Bold, 
                    color = textColor.copy(alpha=0.7f),
                    modifier = Modifier.padding(16.dp)
                )

                LazyColumn {
                    items(statusList) {
                        status ->
                        if (status.userName != "You") { // Don't show your own status again here
                            StatusItem(status = status) { 
                                viewingStatus = status 
                            }
                        }
                    }
                }
            }
        }
        
        // Full-screen status viewer
        AnimatedVisibility(
            visible = viewingStatus != null,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            viewingStatus?.let {
                StatusViewer(status = it) { 
                    viewingStatus = null 
                }
            }
        }
    }
}

@Composable
fun AddStatusItem(onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color.Black
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.White
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text("My Status", fontWeight = FontWeight.Bold, color = textColor)
            Text("Tap to add status update", color = textColor.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun StatusItem(status: StatusUpdate, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color.Black
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(2.dp, if (isDark) Color(0xFF39FF14) else Color(0xFFFFB7B2), CircleShape)
                .padding(4.dp) // Padding inside the border
                .clip(CircleShape) // Clip again for the image
        ) {
            Image(
                painter = rememberAsyncImagePainter(status.imageUri),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(status.userName, fontWeight = FontWeight.Bold, color = textColor)
            Text(status.timestamp, color = textColor.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun StatusViewer(status: StatusUpdate, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .clickable { onDismiss() }
    ) {
        // Close button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }

        // Image and user info
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = status.imageUri),
                contentDescription = "Status Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(status.userName, color = Color.White, fontWeight = FontWeight.Bold)
            Text(status.timestamp, color = Color.White.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

fun createImageFileUri(context: Context): Uri? {
    try {
        val file = File.createTempFile("status_image", ".jpg", context.cacheDir)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    } catch (e: Exception) {
        Timber.e(e, "Error creating image file URI")
        return null
    }
}
