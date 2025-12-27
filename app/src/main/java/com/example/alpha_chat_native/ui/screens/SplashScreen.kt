package com.example.alpha_chat_native.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.alpha_chat_native.R
import com.example.alpha_chat_native.vm.ChatViewModel
import kotlinx.coroutines.delay
import kotlin.random.Random

// ChainTorque Brand Colors
private val SplashBackground = Color(0xFF012106)
private val SplashPrimary = Color(0xFF07AD52)
private val SplashSecondary = Color(0xFF04450F)

@Composable
fun SplashScreen(
    onNavigateToNext: (Boolean) -> Unit,
    vm: ChatViewModel = hiltViewModel()
) {
    var startAnimation by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = tween(durationMillis = 800, easing = EaseOutBack),
        label = "logoScale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "logoAlpha"
    )

    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 400),
        label = "textAlpha"
    )

    val subtitleAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = 700),
        label = "subtitleAlpha"
    )

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

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500)
        onNavigateToNext(vm.isLoggedIn())
    }

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

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(140.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                SplashPrimary.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        radius = 70.dp.toPx()
                    )
                }

                Image(

                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()

                    )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Alpha Chats",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Spacer(modifier = Modifier.height(48.dp))

            LoadingDots(
                modifier = Modifier.alpha(subtitleAlpha)
            )
        }
    }
}

@Composable
private fun LoadingDots(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) { index ->
            val dotAlpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 200)
                ),
                label = "dot$index"
            )

            Canvas(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(dotAlpha)
            ) {
                drawCircle(color = SplashPrimary)
            }
        }
    }
}

// Remove the data class declaration here to avoid redeclaration conflict
// It is already defined in LoginScreen.kt or should be moved to a shared file.
// For now, I will rename it to SplashParticle to avoid conflict if I cannot delete the other one easily.
private data class SplashParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float
)

private val EaseOutBack = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)
