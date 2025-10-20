package com.ktimazstudio

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            SplashScreenContent()
        }
    }
}

@Composable
fun SplashScreenContent() {
    val context = LocalContext.current
    val splashScreenState = remember { MutableTransitionState(false) }

    LaunchedEffect(Unit) {
        splashScreenState.targetState = true
        delay(4000)
        context.startActivity(Intent(context, MainActivity::class.java))
        if (context is ComponentActivity) context.finish()
    }

    val transition = updateTransition(splashScreenState, label = "SplashScreenTransition")

    val logoScale by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 1000, easing = EaseOutBounce) },
        label = "logoScale"
    ) { state ->
        if (state) 1.2f else 0.5f
    }

    val logoRotation by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 2000, easing = FastOutSlowInEasing) },
        label = "logoRotation"
    ) { state ->
        if (state) 720f else 0f
    }

    val logoAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 800, delayMillis = 200) },
        label = "logoAlpha"
    ) { state ->
        if (state) 1f else 0f
    }

    val poweredByAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 800, delayMillis = 2500) },
        label = "poweredByAlpha"
    ) { state ->
        if (state) 1f else 0f
    }

    val isDark = isSystemInDarkTheme()
    
    // Modern gradient colors
    val gradientColors = if (isDark) {
        listOf(
            Color(0xFF0F0F1E),
            Color(0xFF1A1A2E),
            Color(0xFF16213E)
        )
    } else {
        listOf(
            Color(0xFFE8EAF6),
            Color(0xFFC5CAE9),
            Color(0xFF9FA8DA)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(colors = gradientColors)
            )
    ) {
        // Animated particles background
        FloatingParticles()
        
        // Geometric shapes decoration
        GeometricShapes()

        // Center Logo with modern glass morphism effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer glow rings
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size((240 + index * 40).dp)
                        .clip(CircleShape)
                        .background(
                            if (isDark) Color(0xFF6C63FF).copy(alpha = 0.05f - index * 0.01f)
                            else Color(0xFF5E35B1).copy(alpha = 0.08f - index * 0.02f)
                        )
                        .blur((20 + index * 10).dp)
                )
            }

            // Glass morphism container
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer {
                        scaleX = logoScale
                        scaleY = logoScale
                        rotationZ = logoRotation
                        alpha = logoAlpha
                    }
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        if (isDark) 
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.1f),
                                    Color.White.copy(alpha = 0.05f)
                                )
                            )
                        else 
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.6f),
                                    Color.White.copy(alpha = 0.3f)
                                )
                            )
                    )
                    .blur(0.5.dp),
                contentAlignment = Alignment.Center
            ) {
                // Inner gradient border effect
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            if (isDark) Color(0xFF1A1A2E)
                            else Color.White.copy(alpha = 0.9f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(130.dp)
                    )
                }
            }
        }

        // Modern loading indicator
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 170.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            ModernLoadingBar()
        }

        // Powered by text with modern styling
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    alpha = poweredByAlpha
                }
            ) {
                Text(
                    text = "POWERED BY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 3.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.4f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                AnimatedStudioName()
            }
        }
    }
}

@Composable
fun FloatingParticles() {
    val infiniteTransition = rememberInfiniteTransition(label = "Particles")
    repeat(12) { index ->
        val offsetY by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween((3000 + index * 500), easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "particle_$index"
        )
        
        val offsetX = (index * 60).dp
        val size = (3 + index % 4).dp
        
        Box(
            modifier = Modifier
                .offset(x = offsetX, y = (-100 + offsetY).dp)
                .size(size)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.3f))
        )
    }
}

@Composable
fun GeometricShapes() {
    val infiniteTransition = rememberInfiniteTransition(label = "Shapes")
    
    val rotation1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation1"
    )
    
    val rotation2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation2"
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Top right shape
        Box(
            modifier = Modifier
                .offset(x = 280.dp, y = (-50).dp)
                .size(150.dp)
                .graphicsLayer { rotationZ = rotation1 }
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF6C63FF).copy(alpha = 0.1f),
                            Color(0xFF5E35B1).copy(alpha = 0.05f)
                        )
                    )
                )
                .blur(15.dp)
        )
        
        // Bottom left shape
        Box(
            modifier = Modifier
                .offset(x = (-50).dp, y = 600.dp)
                .size(200.dp)
                .graphicsLayer { rotationZ = rotation2 }
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF6B9D).copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
                .blur(20.dp)
        )
    }
}

@Composable
fun ModernLoadingBar() {
    val infiniteTransition = rememberInfiniteTransition(label = "LoadingBar")
    
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )
    
    val isDark = isSystemInDarkTheme()
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    if (isDark) Color.White.copy(alpha = 0.1f)
                    else Color.Black.copy(alpha = 0.1f)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF6C63FF),
                                Color(0xFFFF6B9D)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun AnimatedStudioName() {
    val infiniteTransition = rememberInfiniteTransition(label = "StudioName")

    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val textPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "textPulse"
    )

    val isDark = isSystemInDarkTheme()

    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = textPulse
            scaleY = textPulse
        }
    ) {
        Text(
            text = "KTiMAZ Studio",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp,
            style = MaterialTheme.typography.titleLarge.copy(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF6C63FF),
                        Color(0xFFFF6B9D),
                        Color(0xFF6C63FF)
                    ),
                    start = androidx.compose.ui.geometry.Offset(shimmer - 200f, 0f),
                    end = androidx.compose.ui.geometry.Offset(shimmer, 100f)
                ),
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = if (isDark) Color(0xFF6C63FF).copy(alpha = 0.5f)
                            else Color(0xFF5E35B1).copy(alpha = 0.3f),
                    offset = androidx.compose.ui.geometry.Offset(0f, 0f),
                    blurRadius = 25f
                )
            )
        )
    }
}