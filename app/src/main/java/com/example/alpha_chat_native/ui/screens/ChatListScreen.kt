package com.example.alpha_chat_native.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
private val SplashPrimary = Color(0xFF07AD52)
private val NeonGreen = Color(0xFF39FF14)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onChatClick: (String) -> Unit, // Pass chatId
    vm: ChatViewModel = hiltViewModel()
) {
    val users by vm.users.collectAsState()
    val currentUserId = vm.currentUserId

    // Filter out current user from the list
    val displayUsers = users.filter { it.uid != currentUserId }

    // Theme Colors
    val textColor = Color.White
    val secondaryTextColor = Color.White.copy(alpha=0.7f)
    val cardBgColor = Color(0xFF020E2A).copy(alpha = 0.5f) // Glass effect
    val accentColor = SplashPrimary

    Scaffold(
        containerColor = Color.Transparent, // Transparent to show Home gradient
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Alpha Chat", 
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    ) 
                },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.Search, contentDescription = "Search", tint = textColor) }
                    IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, contentDescription = "More", tint = textColor) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = textColor,
                    actionIconContentColor = textColor,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* New Chat Logic */ },
                containerColor = accentColor,
                contentColor = Color(0xFF012106)
            ) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "New Chat")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Always show Global Chat first
            item {
                 ChatItem(
                    user = User(uid = "global", displayName = "Global Chat", email = "Everyone"),
                    lastMessage = "Join the conversation!",
                    time = "",
                    unreadCount = 0,
                    onClick = { onChatClick("global") },
                    textColor = textColor,
                    secondaryTextColor = secondaryTextColor,
                    unreadBadgeColor = NeonGreen,
                    cardBgColor = cardBgColor
                )
            }

            items(displayUsers) { user ->
                // Construct chat ID for 1-on-1
                val chatId = if (currentUserId != null) {
                    if (currentUserId < user.uid) "${currentUserId}_${user.uid}" else "${user.uid}_${currentUserId}"
                } else {
                    "global" // Fallback
                }
                
                ChatItem(
                    user = user, 
                    lastMessage = "Tap to chat", // In a real app, fetch last message
                    time = "",
                    unreadCount = 0,
                    onClick = { onChatClick(chatId) },
                    textColor = textColor,
                    secondaryTextColor = secondaryTextColor,
                    unreadBadgeColor = NeonGreen,
                    cardBgColor = cardBgColor
                )
            }
        }
    }
}

@Composable
fun ChatItem(
    user: User,
    lastMessage: String,
    time: String,
    unreadCount: Int,
    onClick: () -> Unit,
    textColor: Color,
    secondaryTextColor: Color,
    unreadBadgeColor: Color,
    cardBgColor: Color
) {
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
            // Profile Image
            val imageModel = user.imageUrl.ifEmpty { "https://ui-avatars.com/api/?name=${user.displayName}" }

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(BorderStroke(1.dp, SplashPrimary.copy(alpha = 0.5f)), CircleShape)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = imageModel),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Name and Last Message
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
                    text = lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Time and Unread Badge
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (unreadCount > 0) unreadBadgeColor else secondaryTextColor
                )
                
                if (unreadCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(unreadBadgeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unreadCount.toString(),
                            color = Color.Black,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
