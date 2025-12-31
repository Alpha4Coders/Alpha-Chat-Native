package com.example.alpha_chat_native.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.alpha_chat_native.ui.viewmodels.CommunityViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Discord Colors
val DiscordBackground = Color(0xFF36393f)
val DiscordSidebar = Color(0xFF2f3136)
val DiscordServerRail = Color(0xFF202225)
val DiscordText = Color(0xFFdcddde)
val DiscordAccent = Color(0xFF5865F2)
val DiscordGreen = Color(0xFF3BA55C)
val DiscordCodeBackground = Color(0xFF2b2d31)
val DiscordInput = Color(0xFF40444b)
val DiscordRed = Color(0xFFED4245)
val DiscordGold = Color(0xFFFAA61A)

data class Server(val id: Int, val name: String, val initials: String, val isAdmin: Boolean = false)
data class ChannelCategory(val name: String, val channels: List<String>)

data class ChatMessage(
    val id: String,
    val author: String,
    val avatarColor: Color,
    val timestamp: String,
    val content: String,
    val isCode: Boolean = false,
    val language: String = "text",
    val isAdmin: Boolean = false
)

@Composable
fun CommunityScreen(
    viewModel: CommunityViewModel = hiltViewModel()
) {
    // Mock Data
    val servers = remember {
        listOf(
            Server(1, "Alpha Chat", "AC", isAdmin = true),
            Server(2, "Kotlin Devs", "KD"),
            Server(3, "Android Worldwide", "AW"),
            Server(4, "Gaming Lounge", "GL")
        )
    }

    val channels = remember {
        listOf(
            ChannelCategory("INFORMATION", listOf("announcements", "rules", "resources")),
            ChannelCategory("GENERAL", listOf("general", "off-topic", "introductions")),
            ChannelCategory("DEVELOPMENT", listOf("android", "kotlin", "jetpack-compose", "web-dev"))
        )
    }

    var selectedServerId by remember { mutableIntStateOf(1) }
    var selectedChannel by remember { mutableStateOf<String?>(null) }
    
    val currentServer = servers.find { it.id == selectedServerId }
    val isAdmin = currentServer?.isAdmin == true 

    Box(modifier = Modifier.fillMaxSize().background(DiscordBackground)) {
        // Navigation View (Server Rail + Channel List)
        if (selectedChannel == null) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Server Rail
                ServerRail(
                    servers = servers,
                    selectedServerId = selectedServerId,
                    onServerSelect = { selectedServerId = it }
                )

                // Channel List
                ChannelList(
                    serverName = servers.find { it.id == selectedServerId }?.name ?: "Server",
                    categories = channels,
                    onChannelSelect = { selectedChannel = it }
                )
            }
        } else {
            // Chat View
            // Retrieve state from ViewModel
            val messages = viewModel.getMessages(selectedChannel!!)
            
            val isChatAllowed = if (isAdmin) {
                viewModel.getPermission(selectedChannel!!)
            } else {
                true 
            }

            ChatInterface(
                channelName = selectedChannel!!,
                isAdmin = isAdmin,
                areUsersAllowedToChat = isChatAllowed,
                messages = messages,
                onToggleChatPermission = { 
                    viewModel.togglePermission(selectedChannel!!)
                },
                onBack = { selectedChannel = null },
                onSendMessage = { text, isCode ->
                    viewModel.sendMessage(selectedChannel!!, text, isCode, isAdmin)
                }
            )
        }
    }
}

@Composable
fun ServerRail(
    servers: List<Server>,
    selectedServerId: Int,
    onServerSelect: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .width(72.dp)
            .background(DiscordServerRail)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(servers) { server ->
            ServerIcon(
                server = server,
                isSelected = server.id == selectedServerId,
                onClick = { onServerSelect(server.id) }
            )
        }
        
        item {
             Spacer(modifier = Modifier.height(8.dp))
             IconButton(
                 onClick = { /* Add Server */ },
                 modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(DiscordSidebar)
             ) {
                 Icon(Icons.Default.Add, contentDescription = "Add Server", tint = Color.Green)
             }
        }
    }
}

@Composable
fun ServerIcon(server: Server, isSelected: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Selection Indicator
        Box(
            modifier = Modifier
                .height(if (isSelected) 40.dp else 8.dp)
                .width(4.dp)
                .clip(RoundedCornerShape(0.dp, 4.dp, 4.dp, 0.dp))
                .background(if (isSelected) Color.White else Color.Transparent)
        )
        
        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(if (isSelected) RoundedCornerShape(16.dp) else CircleShape)
                .background(if (isSelected) DiscordAccent else DiscordSidebar)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = server.initials,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ChannelList(
    serverName: String, 
    categories: List<ChannelCategory>,
    onChannelSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DiscordSidebar)
    ) {
        // Server Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp)
                .background(DiscordSidebar),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = serverName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        
        HorizontalDivider(color = DiscordBackground)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            categories.forEach { category ->
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = category.name,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                    )
                }
                items(category.channels) { channel ->
                    ChannelItem(
                        channelName = channel,
                        onClick = { onChannelSelect(channel) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChannelItem(channelName: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#",
            color = Color.Gray,
            fontSize = 20.sp,
            fontWeight = FontWeight.Light,
            modifier = Modifier.padding(end = 6.dp)
        )
        Text(
            text = channelName,
            color = DiscordText,
            fontSize = 16.sp
        )
    }
}

@Composable
fun ChatInterface(
    channelName: String,
    isAdmin: Boolean,
    areUsersAllowedToChat: Boolean,
    messages: List<ChatMessage>,
    onToggleChatPermission: () -> Unit,
    onBack: () -> Unit,
    onSendMessage: (String, Boolean) -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        ChannelSettingsDialog(
            channelName = channelName,
            areUsersAllowedToChat = areUsersAllowedToChat,
            onToggleChatPermission = onToggleChatPermission,
            onDismiss = { showSettings = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(DiscordBackground)) {
        // Top Bar
        ChatTopBar(
            channelName = channelName, 
            onBack = onBack, 
            isAdmin = isAdmin,
            onSettingsClick = { showSettings = true }
        )

        // Messages
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            items(messages) { message ->
                MessageItem(message)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Auto-scroll to bottom when messages change
        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }

        // Input
        if (isAdmin || areUsersAllowedToChat) {
            ChatInputArea(
                onSendMessage = { text, isCode ->
                    onSendMessage(text, isCode)
                }
            )
        } else {
             // Read Only View for Users
             Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(DiscordCodeBackground, RoundedCornerShape(8.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
             ) {
                 Row(verticalAlignment = Alignment.CenterVertically) {
                     Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.Gray)
                     Spacer(modifier = Modifier.width(8.dp))
                     Text(
                         text = "Only admins can send messages in this channel.",
                         color = Color.Gray,
                         fontWeight = FontWeight.Bold
                     )
                 }
             }
        }
    }
}

@Composable
fun ChatTopBar(
    channelName: String, 
    onBack: () -> Unit,
    isAdmin: Boolean,
    onSettingsClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(DiscordBackground)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "#",
                color = Color.Gray,
                fontSize = 24.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(start = 8.dp, end = 4.dp)
            )
            Text(
                text = channelName,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            
            if (isAdmin) {
                 IconButton(onClick = onSettingsClick) {
                    Icon(
                        Icons.Default.Settings, 
                        contentDescription = "Channel Settings", 
                        tint = Color.Gray
                    )
                }
            }
            
            IconButton(onClick = { }) {
                Icon(Icons.Default.People, contentDescription = "Members", tint = Color.Gray)
            }
        }
        HorizontalDivider(color = Color(0xFF26272D), thickness = 1.dp)
    }
}

@Composable
fun ChannelSettingsDialog(
    channelName: String,
    areUsersAllowedToChat: Boolean,
    onToggleChatPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DiscordBackground
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Settings Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Channel Settings",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .border(1.dp, Color.Gray, CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.padding(4.dp))
                    }
                }

                HorizontalDivider(color = DiscordServerRail)

                Row(modifier = Modifier.fillMaxSize()) {
                    // Sidebar
                    Column(
                        modifier = Modifier
                            .width(100.dp)
                            .fillMaxHeight()
                            .background(DiscordSidebar)
                            .padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "# $channelName",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF42464D))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Overview",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                         Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Permissions",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Overview",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Permission Toggle Section
                        Text(
                            text = "CHANNEL PERMISSIONS",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Send Messages",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Allow members to send messages in this channel.",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                            
                            Switch(
                                checked = areUsersAllowedToChat,
                                onCheckedChange = { onToggleChatPermission() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = DiscordGreen,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.Gray
                                )
                            )
                        }
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 24.dp), 
                            color = Color.Gray.copy(alpha = 0.3f)
                        )

                         Text(
                            text = "ADVANCED",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Slow Mode",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Members will be restricted to sending one message every 5 seconds.",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                             Switch(
                                checked = false,
                                onCheckedChange = { },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = DiscordGreen,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.Gray
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: ChatMessage) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(message.avatarColor),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder for avatar image
             Text(
                text = message.author.take(1),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = message.author,
                    color = if(message.author == "You") DiscordAccent else if (message.isAdmin) DiscordRed else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                
                if (message.isAdmin) {
                     Icon(
                        imageVector = Icons.Default.Stars, // Using Stars as a crown proxy
                        contentDescription = "Admin",
                        tint = DiscordGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = message.timestamp,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp).align(Alignment.Bottom)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))

            // Content
            if (message.isCode) {
                CodeBlock(content = message.content, language = message.language)
            } else {
                Text(
                    text = message.content,
                    color = DiscordText,
                    fontSize = 16.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun CodeBlock(content: String, language: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DiscordCodeBackground)
            .border(1.dp, Color(0xFF202225), RoundedCornerShape(8.dp))
    ) {
        // Code Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF202225))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mac-style dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFF5F57)))
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFEBC2E)))
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF28C840)))
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = language.lowercase(),
                color = DiscordGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { /* Copy logic */ }
            ) {
                 Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Copy", color = Color.Gray, fontSize = 12.sp)
            }
        }
        
        // Code Content
        Text(
            text = content,
            color = DiscordText,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
fun ChatInputArea(onSendMessage: (String, Boolean) -> Unit) {
    var text by remember { mutableStateOf("") }
    var isCodeMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DiscordBackground)
            .padding(8.dp)
    ) {
        // Formatting Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(DiscordCodeBackground)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.FormatBold, contentDescription = "Bold", tint = Color.Gray, modifier = Modifier.size(20.dp))
            Icon(Icons.Default.FormatItalic, contentDescription = "Italic", tint = Color.Gray, modifier = Modifier.size(20.dp))
            Icon(Icons.Default.StrikethroughS, contentDescription = "Strikethrough", tint = Color.Gray, modifier = Modifier.size(20.dp))
            Icon(Icons.Default.Link, contentDescription = "Link", tint = Color.Gray, modifier = Modifier.size(20.dp))
            Icon(Icons.Default.List, contentDescription = "List", tint = Color.Gray, modifier = Modifier.size(20.dp))
            
            // Code Toggle
            Icon(
                imageVector = Icons.Default.Code, 
                contentDescription = "Code Block", 
                tint = if (isCodeMode) DiscordGreen else Color.Gray, 
                modifier = Modifier
                    .size(20.dp)
                    .clickable { isCodeMode = !isCodeMode }
            )
        }
        
        // Input Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                .background(DiscordInput)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                 Icon(Icons.Default.Add, contentDescription = "Attach", tint = DiscordInput, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            BasicTextField(
                value = text,
                onValueChange = { text = it },
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text("Message #channel", color = Color.Gray)
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                Icons.Default.EmojiEmotions, 
                contentDescription = "Emoji", 
                tint = Color.Gray,
                modifier = Modifier.clickable {}
            )
            
            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                Icons.AutoMirrored.Filled.Send, 
                contentDescription = "Send", 
                tint = if (text.isNotEmpty()) DiscordAccent else Color.Gray,
                modifier = Modifier.clickable {
                    if (text.isNotEmpty()) {
                        onSendMessage(text, isCodeMode)
                        text = ""
                        isCodeMode = false
                    }
                }
            )
        }
        
        if (isCodeMode) {
            Text(
                text = "Code mode enabled - your message will be formatted as code",
                color = DiscordGreen,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp, start = 8.dp)
            )
        }
    }
}
