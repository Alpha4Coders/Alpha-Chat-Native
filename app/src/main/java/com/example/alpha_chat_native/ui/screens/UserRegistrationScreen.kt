package com.example.alpha_chat_native.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.alpha_chat_native.vm.ChatViewModel
import kotlin.random.Random

// --- Colors from SplashScreen ---
private val SplashBackground = Color(0xFF012106)
private val SplashPrimary = Color(0xFF07AD52)
private val SplashSecondary = Color(0xFF04450F)

@Composable
fun UserRegistrationScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    vm: ChatViewModel = hiltViewModel()
) {
    var userName by remember { mutableStateOf("") }
    var github by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val isLoading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()

    // 1. Dynamic Background from SplashScreen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SplashBackground,
                        Color(0xFF020E2A),
                        SplashBackground
                    )
                )
            )
    ) {
        // Shared Background Logic
        RegistrationParticleBackground()

        // 2. Main Signup Container
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Terminal Window
            TerminalWindow(
                userName = userName,
                onUserNameChange = { userName = it },
                github = github,
                onGithubChange = { github = it },
                password = password,
                onPasswordChange = { password = it },
                showPassword = showPassword,
                onToggleShowPassword = { showPassword = !showPassword },
                isLoading = isLoading,
                error = error,
                onSignup = {
                    vm.register(userName, github, password) {
                         onRegisterSuccess()
                    }
                },
                onNavigateToLogin = onNavigateToLogin
            )
        }
    }
}

// Reusing the exact background logic structure from Login/Splash
@Composable
fun RegistrationParticleBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

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

    val particles = remember {
        List(20) {
            RegistrationParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 6 + 2,
                speed = Random.nextFloat() * 0.005f + 0.002f,
                alpha = Random.nextFloat() * 0.5f + 0.2f
            )
        }
    }

    val particleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleProgress"
    )

    // Canvas Layers
    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val animatedY = (particle.y + particleProgress * particle.speed * 100) % 1f
            drawCircle(
                color = SplashPrimary.copy(alpha = particle.alpha),
                radius = particle.size.dp.toPx(),
                center = Offset(
                    x = particle.x * size.width,
                    y = animatedY * size.height
                )
            )
        }
    }

    Canvas(
        modifier = Modifier
            .size(200.dp)
            .scale(pulseScale)
            .alpha(pulseAlpha)
            .wrapContentSize(Alignment.Center) 
    ) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(SplashPrimary, Color.Transparent)
            ),
            radius = size.minDimension / 2
        )
    }
    
    // Second Pulse
    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale2"
    )
    val pulseAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha2"
    )

    Canvas(
        modifier = Modifier
            .size(200.dp)
            .scale(pulseScale2)
            .alpha(pulseAlpha2)
            .wrapContentSize(Alignment.Center)
    ) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(SplashSecondary, Color.Transparent)
            ),
            radius = size.minDimension / 2
        )
    }
}

// Renamed data class to avoid conflict with LoginScreen.kt's Particle
private data class RegistrationParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float
)

@Composable
fun TerminalWindow(
    userName: String,
    onUserNameChange: (String) -> Unit,
    github: String,
    onGithubChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    showPassword: Boolean,
    onToggleShowPassword: () -> Unit,
    isLoading: Boolean,
    error: String?,
    onSignup: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    // Colors based on theme but adapted for the dark splash background
    val terminalBg = Brush.linearGradient(
        listOf(
            Color(0xFF232526).copy(alpha = 0.9f),
            Color(0xFF1E130C).copy(alpha = 0.9f),
            Color(0xFF012106).copy(alpha = 0.8f) // Deep Green tint
        )
    )
    
    val borderColor = SplashPrimary
    val textColor = Color.White
    val accentColor = SplashPrimary
    val secondaryAccent = SplashSecondary
    val inputBg = Color(0xFF000000).copy(alpha = 0.5f)
    val inputBorder = SplashPrimary.copy(alpha = 0.5f)
    val inputText = Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier.background(terminalBg)
        ) {
            Column {
                // Terminal Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Window dots
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFFFF5F56), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFFFFBD2E), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF27C93F), CircleShape))

                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "alpha-chat@terminal ~ register",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = SplashPrimary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                HorizontalDivider(color = borderColor, thickness = 1.dp)

                // Content Area
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "JoinAlphaChat",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = secondaryAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "$ sudo useradd --developer --group=alpha-coders",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = SplashPrimary,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Form Inputs
                    TerminalInput(
                        value = userName,
                        onValueChange = onUserNameChange,
                        placeholder = "Username",
                        textColor = inputText,
                        bgColor = inputBg,
                        borderColor = inputBorder
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    TerminalInput(
                        value = github,
                        onValueChange = onGithubChange,
                        placeholder = "GitHub / Email",
                        textColor = inputText,
                        bgColor = inputBg,
                        borderColor = inputBorder
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Password Field
                    Box(modifier = Modifier.fillMaxWidth()) {
                        TerminalInput(
                            value = password,
                            onValueChange = onPasswordChange,
                            placeholder = "Password",
                            textColor = inputText,
                            bgColor = inputBg,
                            borderColor = inputBorder,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
                        )
                        TextButton(
                            onClick = onToggleShowPassword,
                            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)
                        ) {
                            Text(
                                text = if (showPassword) "Hide" else "Show",
                                color = accentColor,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            color = Color.Red,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Signup Button
                    val buttonBrush = Brush.horizontalGradient(listOf(SplashPrimary, SplashSecondary))
                    
                    Button(
                        onClick = onSignup,
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(buttonBrush),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    text = "Sign Up",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Login Link
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Already have an account?",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        TextButton(onClick = onNavigateToLogin) {
                            Text(
                                text = "Login",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = accentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TerminalInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    textColor: Color,
    bgColor: Color,
    borderColor: Color,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, fontFamily = FontFamily.Monospace, color = textColor.copy(alpha = 0.6f)) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = bgColor,
            unfocusedContainerColor = bgColor,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor.copy(alpha = 0.5f),
            cursorColor = textColor
        ),
        shape = RoundedCornerShape(4.dp),
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace),
        visualTransformation = visualTransformation
    )
}
