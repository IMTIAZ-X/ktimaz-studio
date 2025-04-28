package com.ktimazstudio

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.ui.geometry.Offset

class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplashScreenV5()
        }
    }
}

@Composable
fun SplashScreenV5() {
    val context = LocalContext.current
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(4000)
        context.startActivity(Intent(context, MainActivity::class.java))
        if (context is ComponentActivity) context.finish()
    }

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.2f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = EaseOutBounce),
        label = "ScaleAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE0EAFC),
                        Color(0xFFCFDEF3)
                    )
                )
            )
    ) {
        // Floating Particles
        FloatingParticles()

        // Center Capsule Logo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(width = 220.dp, height = 140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .blur(8.dp)
            )

            Box(
                modifier = Modifier
                    .size(width = 180.dp, height = 100.dp)
                    .scale(scale)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        // Loading Dots under logo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 170.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            LoadingDots()
        }

        // Legendary Text
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            LegendaryText()
        }

        // Powered by text
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "Powered by KTiMAZ Studio",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun LoadingDots() {
    var visible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            visible = !visible
            delay(500)
        }
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) {
            AnimatedVisibility(
                visible = visible,
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

@Composable
fun LegendaryText() {
    val infiniteTransition = rememberInfiniteTransition(label = "TextFX")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AlphaAnimation"
    )

    Text(
        text = "Let's Go V5 Legendary++",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary.copy(alpha = alphaAnim)
    )
}

@Composable
fun FloatingParticles() {
    val particleList = remember {
        List(30) {
            Particle(
                initialOffsetX = Random.nextFloat() * 1080f,
                initialOffsetY = Random.nextFloat() * 1920f,
                deltaY = Random.nextFloat() * 400f + 200f,
                radius = Random.nextFloat() * 4f + 2f,
                color = Color.White.copy(alpha = Random.nextFloat() * 0.5f + 0.2f),
                speed = Random.nextFloat() * 3000f + 2000f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ParticlesAnimation")

    val animatedOffsets = particleList.map { particle ->
        infiniteTransition.animateFloat(
            initialValue = particle.initialOffsetY,
            targetValue = particle.initialOffsetY + particle.deltaY,
            animationSpec = infiniteRepeatable(
                animation = tween(particle.speed.toInt(), easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "Particle_${particle.hashCode()}"
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particleList.forEachIndexed { index, particle ->
            val offsetY = animatedOffsets[index].value

            drawCircle(
                color = particle.color,
                radius = particle.radius,
                center = Offset(particle.initialOffsetX, offsetY)
            )
        }
    }
}

// Particle Data Class
data class Particle(
    val initialOffsetX: Float,
    val initialOffsetY: Float,
    val deltaY: Float,
    val radius: Float,
    val color: Color,
    val speed: Float
)
