package com.example.alpha_chat_native.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.alpha_chat_native.vm.ChatViewModel
import kotlin.random.Random

// --- Colors ---
private val SplashBackground = Color(0xFF012106)
private val SplashPrimary = Color(0xFF07AD52)
private val SplashSecondary = Color(0xFF04450F)
private val DarkBg = Color(0xFF23234A)
private val NeonGreen = Color(0xFF39FF14)
private val NeonYellow = Color(0xFFFFE156)

@Composable
fun ProfileScreen(
    onNavigateHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    vm: ChatViewModel = hiltViewModel()
) {
    // Fetch Current User Data
    val currentUserId = vm.currentUserId
    val users by vm.users.collectAsState()
    val currentUser = users.find { it.uid == currentUserId }
    val context = LocalContext.current
    val isLoading by vm.isLoading.collectAsState()
    
    var name by remember(currentUser) { mutableStateOf(currentUser?.displayName ?: "") }
    val userHandle = currentUser?.email?.substringBefore("@") ?: "username"
    val github = currentUser?.email ?: "email@example.com"
    
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri = it }
    }

    // Dynamic Background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SplashBackground, Color(0xFF020E2A), SplashBackground)
                )
            )
    ) {
        ProfileParticleBackground()

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ProfileTerminalWindow(
                name = name,
                onNameChange = { name = it },
                userHandle = userHandle,
                github = github,
                currentImageUrl = currentUser?.imageUrl,
                imageUri = imageUri,
                onPickImage = { launcher.launch("image/*") },
                saving = isLoading,
                onSave = {
                    vm.updateProfile(name, imageUri) {
                        Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        onNavigateHome()
                    }
                },
                onLogout = {
                    vm.signOut {
                        onNavigateToLogin()
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileTerminalWindow(
    name: String,
    onNameChange: (String) -> Unit,
    userHandle: String,
    github: String,
    currentImageUrl: String?,
    imageUri: Uri?,
    onPickImage: () -> Unit,
    saving: Boolean,
    onSave: () -> Unit,
    onLogout: () -> Unit
) {
    val terminalBg = Brush.linearGradient(
        listOf(
            DarkBg.copy(alpha = 0.9f),
            Color(0xFF181C2F).copy(alpha = 0.9f)
        )
    )
    
    val borderColor = NeonGreen
    val accentColor = NeonGreen
    val textColor = Color.White
    val mutedText = Color(0xFFB3B3FF)
    
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.background(terminalBg)) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = borderColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFFFF5F56), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFFFFBD2E), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF27C93F), CircleShape))
                    
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "alpha-chat@terminal ~ profile --edit",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = mutedText
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                HorizontalDivider(color = borderColor.copy(alpha = 0.2f), thickness = 1.dp)

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ProfileConfig",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color(0xFF7F53AC),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Text(
                        text = "$ nano ~/.config/user/profile.json",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = accentColor,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clickable { onPickImage() }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(4.dp, accentColor, CircleShape)
                                .background(DarkBg)
                        ) {
                            val painter = rememberAsyncImagePainter(
                                model = imageUri ?: currentImageUrl ?: "https://ui-avatars.com/api/?name=$name"
                            )
                            Image(
                                painter = painter,
                                contentDescription = "Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = (-10).dp, y = (-10).dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Edit",
                                tint = Color(0xFF181C2F)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = onNameChange,
                        placeholder = { Text("Enter your name", fontFamily = FontFamily.Monospace) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkBg,
                            unfocusedContainerColor = DarkBg,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = accentColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        ),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = userHandle,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF181C2F),
                            unfocusedContainerColor = Color(0xFF181C2F),
                            focusedBorderColor = accentColor.copy(alpha = 0.5f),
                            unfocusedBorderColor = accentColor.copy(alpha = 0.5f),
                            focusedTextColor = mutedText,
                            unfocusedTextColor = mutedText
                        ),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = github,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF181C2F),
                            unfocusedContainerColor = Color(0xFF181C2F),
                            focusedBorderColor = accentColor.copy(alpha = 0.5f),
                            unfocusedBorderColor = accentColor.copy(alpha = 0.5f),
                            focusedTextColor = mutedText,
                            unfocusedTextColor = mutedText
                        ),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    val buttonBrush = Brush.horizontalGradient(listOf(NeonGreen, NeonYellow))
                    
                    Button(
                        onClick = onSave,
                        enabled = !saving,
                        modifier = Modifier
                            .width(200.dp)
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(buttonBrush),
                            contentAlignment = Alignment.Center
                        ) {
                            if (saving) {
                                CircularProgressIndicator(color = Color(0xFF181C2F), modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    text = "Save & Exit",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF181C2F),
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .width(200.dp)
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Logout",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileParticleBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val particleColor = NeonGreen

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )
    
    // Pulsing Blobs
    Canvas(
        modifier = Modifier
            .offset(x = 50.dp, y = 50.dp)
            .size(200.dp)
            .scale(pulseScale)
            .alpha(pulseAlpha)
    ) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(particleColor, Color.Transparent)
            ),
            radius = size.minDimension / 2
        )
    }
    
    Canvas(
        modifier = Modifier
            .offset(x = 300.dp, y = 600.dp)
            .size(200.dp)
            .scale(pulseScale)
            .alpha(pulseAlpha)
    ) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NeonYellow, Color.Transparent)
            ),
            radius = size.minDimension / 2
        )
    }
}
