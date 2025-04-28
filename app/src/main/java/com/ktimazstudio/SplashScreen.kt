package com.ktimazstudio

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay
import kotlin.random.Random

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
        targetValue = if (startAnimation) 1.05f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "LogoScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFd8e6ff),
                        Color(0xFFf0f4ff)
                    )
                )
            )
    ) {
        // Light blur + parallax soft background
        ParallaxBlurredBackground()

        // Floating Particles
        FloatingParticles()

        // Center Logo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(16.dp)) // Capsule polished shape
                    .background(Color.White.copy(alpha = 0.1f))
                    .blur(8.dp)
            )

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(100.dp)
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

        // New Powered by Text FX
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            LegendaryTextFX()
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
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

@Composable
fun FloatingParticles() {
    val infiniteTransition = rememberInfiniteTransition(label = "Particles")
    val particleList = remember { generateParticles(30) }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particleList.forEachIndexed { index, particle ->
            val animatedOffsetY by infiniteTransition.animateFloat(
                initialValue = particle.initialOffsetY,
                targetValue = particle.initialOffsetY + particle.deltaY,
                animationSpec = infiniteRepeatable(
                    animation = tween(particle.speed.toInt(), easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ), label = "Particle$index"
            )

            drawCircle(
                color = particle.color,
                radius = particle.radius,
                center = Offset(particle.initialOffsetX, animatedOffsetY)
            )
        }
    }
}

data class Particle(
    val initialOffsetX: Float,
    val initialOffsetY: Float,
    val deltaY: Float,
    val radius: Float,
    val color: Color,
    val speed: Float
)

fun generateParticles(count: Int): List<Particle> {
    return List(count) {
        Particle(
            initialOffsetX = Random.nextFloat() * 1080f,
            initialOffsetY = Random.nextFloat() * 1920f,
            deltaY = Random.nextFloat() * 300f + 300f,
            radius = Random.nextFloat() * 5f + 2f,
            color = Color.White.copy(alpha = Random.nextFloat() * 0.4f + 0.1f),
            speed = Random.nextFloat() * 5000f + 3000f
        )
    }
}

@Composable
fun ParallaxBlurredBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "Parallax")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "ParallaxOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(y = offsetY.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.1f),
                        Color.Transparent
                    )
                )
            )
            .blur(8.dp)
    )
}

@Composable
fun LegendaryTextFX() {
    val infiniteTransition = rememberInfiniteTransition(label = "TextFX")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "TextAlpha"
    )

    Text(
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                append("Powered by ")
            }
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)) {
                append("KTiMAZ Studio")
            }
        },
        fontSize = 14.sp,
        modifier = Modifier.alpha(alpha)
    )
}
