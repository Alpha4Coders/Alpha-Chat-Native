package com.example.alpha_chat_native.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.alpha_chat_native.ui.viewmodels.LoginState
import com.example.alpha_chat_native.ui.viewmodels.LoginViewModel
import kotlin.random.Random

// --- Colors from SplashScreen ---
private val SplashBackground = Color(0xFF012106)
private val SplashPrimary = Color(0xFF07AD52)
private val SplashSecondary = Color(0xFF04450F)

// Define Particle Data Class (Must be public or internal to be visible inside Composable)
data class Particle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float
)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSignUpClick: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    // State to hold input values
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Observe ViewModel state
    val loginState by viewModel.loginState.collectAsState()
    val context = LocalContext.current

    // React to state changes
    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginState.Success -> {
                onLoginSuccess()
                viewModel.resetState()
            }
            is LoginState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // 1. The Dynamic Background from SplashScreen
        DynamicParticleBackground()

        // 2. The Login Form Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {

            // Glass Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    // Slightly more transparent white to let the dark background show through gently
                    containerColor = Color.White.copy(alpha = 0.90f)
                ),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Alpha Chat 💬",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = SplashPrimary // Use brand color
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Chat. Connect. Chill.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email") },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SplashPrimary,
                            focusedLabelColor = SplashPrimary
                        ),
                        enabled = loginState !is LoginState.Loading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SplashPrimary,
                            focusedLabelColor = SplashPrimary
                        ),
                        enabled = loginState !is LoginState.Loading
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Gradient Button (Updated to match Theme)
                    Button(
                        onClick = { viewModel.loginUser(email, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(),
                        enabled = loginState !is LoginState.Loading
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            SplashPrimary,
                                            Color(0xFF09D668) // Slightly lighter green
                                        )
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (loginState is LoginState.Loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = "Sign In",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sign Up
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Don’t have an account?")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sign up",
                            color = SplashPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                if (loginState !is LoginState.Loading) onSignUpClick()
                            }
                        )
                    }
                }
            }
        }
    }
}

// --- Extracted Background Logic from SplashScreen ---

@Composable
fun DynamicParticleBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    // Pulse Animation
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

    // Particles Setup
    val particles = remember {
        List(20) {
            Particle(
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

    // Background Container
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
            ),
        contentAlignment = Alignment.Center
    ) {
        // Draw Particles
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

        // Draw Pulsing Circle 1
        Canvas(
            modifier = Modifier
                .size(200.dp)
                .scale(pulseScale)
                .alpha(pulseAlpha)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(SplashPrimary, Color.Transparent)
                ),
                radius = size.minDimension / 2
            )
        }

        // Second Pulse (Offset)
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

        // Draw Pulsing Circle 2
        Canvas(
            modifier = Modifier
                .size(200.dp)
                .scale(pulseScale2)
                .alpha(pulseAlpha2)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(SplashSecondary, Color.Transparent)
                ),
                radius = size.minDimension / 2
            )
        }
    }
}
