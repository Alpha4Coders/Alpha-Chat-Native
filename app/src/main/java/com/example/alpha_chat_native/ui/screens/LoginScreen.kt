package com.example.alpha_chat_native.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.alpha_chat_native.ui.viewmodels.LoginState
import com.example.alpha_chat_native.ui.viewmodels.LoginViewModel
import kotlin.random.Random

// --- Colors from SplashScreen ---
private val SplashBackground = Color(0xFF012106)
private val SplashPrimary = Color(0xFF07AD52)
private val SplashSecondary = Color(0xFF04450F)

private data class LoginParticle(
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
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val loginState by viewModel.loginState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginState.Success -> {
                onLoginSuccess()
                viewModel.resetState()
            }
            is LoginState.PasswordResetEmailSent -> {
                Toast.makeText(context, "Password reset email sent to $email", Toast.LENGTH_LONG).show()
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
        DynamicParticleBackground()

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            TerminalLoginWindow(
                email = email,
                onEmailChange = { email = it },
                password = password,
                onPasswordChange = { password = it },
                showPassword = showPassword,
                onToggleShowPassword = { showPassword = !showPassword },
                loginState = loginState,
                onLogin = { viewModel.loginUser(email, password) },
                onForgotPassword = { viewModel.resetPassword(email) },
                onSignUpClick = onSignUpClick
            )
        }
    }
}

@Composable
fun TerminalLoginWindow(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    showPassword: Boolean,
    onToggleShowPassword: () -> Unit,
    loginState: LoginState,
    onLogin: () -> Unit,
    onForgotPassword: () -> Unit,
    onSignUpClick: () -> Unit
) {
    val terminalBg = Brush.linearGradient(
        listOf(
            Color(0xFF232526).copy(alpha = 0.9f),
            Color(0xFF1E130C).copy(alpha = 0.9f),
            Color(0xFF012106).copy(alpha = 0.8f)
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
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFFFF5F56), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFFFFBD2E), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF27C93F), CircleShape))

                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "alpha-chat@terminal ~ login",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = SplashPrimary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                HorizontalDivider(color = borderColor, thickness = 1.dp)

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AlphaChat Login",
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
                        text = "$ ssh user@alpha-chat",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = SplashPrimary,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Email Input
                    OutlinedTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        placeholder = { Text("Email", color = inputText.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = inputText,
                            unfocusedTextColor = inputText,
                            focusedContainerColor = inputBg,
                            unfocusedContainerColor = inputBg,
                            focusedBorderColor = inputBorder,
                            unfocusedBorderColor = inputBorder.copy(alpha = 0.5f)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = inputBorder) }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        placeholder = { Text("Password", color = inputText.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = inputText,
                            unfocusedTextColor = inputText,
                            focusedContainerColor = inputBg,
                            unfocusedContainerColor = inputBg,
                            focusedBorderColor = inputBorder,
                            unfocusedBorderColor = inputBorder.copy(alpha = 0.5f)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                        visualTransformation = if (!showPassword) PasswordVisualTransformation() else VisualTransformation.None,
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = inputBorder) },
                        trailingIcon = {
                            IconButton(onClick = onToggleShowPassword) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password",
                                    tint = inputText
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        Text(
                            text = "Forgot Password?",
                            color = secondaryAccent,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.clickable(onClick = onForgotPassword)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onLogin,
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = loginState !is LoginState.Loading
                    ) {
                        if (loginState is LoginState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                        } else {
                            Text("EXECUTE LOGIN", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("New User? ", color = Color.White, fontFamily = FontFamily.Monospace)
                        Text(
                            text = "./register.sh",
                            color = secondaryAccent,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.clickable(onClick = onSignUpClick)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DynamicParticleBackground() {
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
            LoginParticle(
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
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(SplashPrimary, Color.Transparent)
                ),
                radius = size.minDimension / 2
            )
        }

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
