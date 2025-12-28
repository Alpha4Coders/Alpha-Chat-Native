package com.example.alpha_chat_native.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.alpha_chat_native.data.models.User
import com.example.alpha_chat_native.vm.ChatViewModel

// --- Theme Colors ---
private val SplashBackground = Color(0xFF012106)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectUserScreen(
    onUserSelected: (String) -> Unit,
    vm: ChatViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val users by vm.users.collectAsState()
    val currentUserId = vm.currentUserId

    // Background Gradient
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            SplashBackground,
            Color(0xFF020E2A),
            SplashBackground
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Select User", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(users.filter { it.uid != currentUserId }) { user ->
                    val chatId = if (currentUserId != null) {
                        if (currentUserId < user.uid) "${currentUserId}_${user.uid}" else "${user.uid}_${currentUserId}"
                    } else {
                        user.uid 
                    }
                    UserItem(user = user) {
                        onUserSelected(chatId)
                    }
                }
            }
        }
    }
}

@Composable
fun UserItem(
    user: User,
    onClick: () -> Unit
) {
    val cardBgColor = Color(0xFF020E2A).copy(alpha = 0.5f)
    val textColor = Color.White
    val secondaryTextColor = Color.White.copy(alpha = 0.7f)
    val splashPrimary = Color(0xFF07AD52)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageModel = if (user.imageUrl.isNotEmpty()) user.imageUrl else "https://ui-avatars.com/api/?name=${user.displayName}"

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(BorderStroke(1.dp, splashPrimary.copy(alpha = 0.5f)), CircleShape)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = imageModel),
                    contentDescription = "User Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.displayName.ifBlank { "Unknown User" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
