package com.example.alpha_chat_native.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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

data class StatusUpdate(val userName: String, val imageUri: Uri?, val timestamp: String)

@Composable
fun UpdateScreen() {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color.Black
    val context = LocalContext.current
    
    // Status Logic
    var myStatus by remember { mutableStateOf<Uri?>(null) }
    var statusList by remember { mutableStateOf(listOf<StatusUpdate>()) }
    
    // Use rememberSaveable to ensure the URI survives activity recreation (e.g. camera rotation/memory pressure)
    var tempImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            myStatus = tempImageUri
            statusList = statusList + StatusUpdate("You", tempImageUri!!, "Just now")
        } else if (!success) {
            // Optional: Handle cancellation
        }
    }
    
    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val uri = createImageFileUri(context)
                tempImageUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to create image file: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        } else {
            Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                ) {
                    if (myStatus != null) {
                        Image(
                            painter = rememberAsyncImagePainter(myStatus),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                         Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("My Status", fontWeight = FontWeight.Bold, color = textColor)
                    Text("Tap to add status update", color = textColor.copy(alpha = 0.6f))
                }
            }
            
            HorizontalDivider(color = Color.Gray.copy(alpha=0.2f))
            
            Text(
                "Recent Updates", 
                fontWeight = FontWeight.Bold, 
                color = textColor.copy(alpha=0.7f),
                modifier = Modifier.padding(16.dp)
            )
            
            // List of updates
            LazyColumn {
                items(statusList) { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .border(2.dp, if(isDark) Color(0xFF39FF14) else Color(0xFFFFB7B2), CircleShape)
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
            }
        }
    }
}

fun createImageFileUri(context: Context): Uri {
    val imageFileName = "JPEG_" + System.currentTimeMillis() + "_"
    val storageDir = context.cacheDir
    val image = File.createTempFile(
        imageFileName,
        ".jpg",
        storageDir
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        image
    )
}
