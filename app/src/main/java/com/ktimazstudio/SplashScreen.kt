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
            SplashScreenV2()
        }
    }
}

@Composable
fun SplashScreenV2() {
    val context = LocalContext.current
    val splashScreenState = remember { MutableTransitionState(false) }

    LaunchedEffect(Unit) {
        splashScreenState.targetState = true
        delay(4000)
        context.startActivity(Intent(context, MainActivity::class.java))
        if (context is ComponentActivity) context.finish()
    }

    val transition = updateTransition(splashScreenState, label = "SplashScreenTransition")

    // Logo animations
    val logoScale by transition.animateFloat(
        transitionSpec = { 
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "logoScale"
    ) { state ->
        if (state) 1f else 0.3f
    }

    val logoRotation by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 1800, easing = FastOutSlowInEasing) },
        label = "logoRotation"
    ) { state ->
        if (state) 360f else 0f
    }

    val logoAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 600, delayMillis = 100) },
        label = "logoAlpha"
    ) { state ->
        if (state) 1f else 0f
    }

    // Text animations
    val textAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 1000, delayMillis = 2200) },
        label = "textAlpha"
    ) { state ->
        if (state) 1f else 0f
    }

    val textScale by transition.animateFloat(
        transitionSpec = { 
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
                delayMillis = 2200
            )
        },
        label = "textScale"
    ) { state ->
        if (state) 1f else 0.5f
    }

    val textOffsetY by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 900, delayMillis = 2200, easing = EaseOutCubic) },
        label = "textOffsetY"
    ) { state ->
        if (state) 0f else 50f
    }

    // Animated gradient background
    val infiniteGradientTransition = rememberInfiniteTransition(label = "GradientAnimation")
    val gradientOffset by infiniteGradientTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFF3B3B), // Vibrant Red
                        Color(0xFFFF8C42), // Orange
                        Color(0xFFFFD93D), // Yellow
                        Color(0xFFFF6B35), // Red-Orange
                        Color(0xFFFF3B3B)  // Vibrant Red
                    ),
                    start = androidx.compose.ui.geometry.Offset(gradientOffset, gradientOffset),
                    end = androidx.compose.ui.geometry.Offset(
                        gradientOffset + 1000f,
                        gradientOffset + 1000f
                    )
                )
            )
    ) {
        // Floating particles/orbs in background
        FloatingOrbs()

        // Geometric accent lines
        GeometricAccents()

        // Center Logo with modern glass effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer glow rings
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size((200 + index * 40).dp)
                        .graphicsLayer {
                            alpha = (0.15f - index * 0.04f) * logoAlpha
                        }
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .blur((20 + index * 10).dp)
                )
            }

            // Main logo container with glass morphism
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .graphicsLayer {
                        scaleX = logoScale
                        scaleY = logoScale
                        rotationZ = logoRotation
                        alpha = logoAlpha
                    }
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.4f),
                                Color.White.copy(alpha = 0.15f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .shadow(
                        elevation = 30.dp,
                        shape = CircleShape,
                        ambientColor = Color.White.copy(alpha = 0.5f),
                        spotColor = Color.White.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Inner glow
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                )

                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(110.dp)
                        .graphicsLayer {
                            rotationZ = -logoRotation * 0.5f
                        }
                )
            }
        }

        // Futuristic loading indicator
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 140.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            FuturisticLoadingIndicator()
        }

        // Animated "Powered by KTiMAZ Studio" text
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 40.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    alpha = textAlpha
                    scaleX = textScale
                    scaleY = textScale
                    translationY = textOffsetY
                }
            ) {
                Text(
                    text = "Powered by",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                AnimatedGradientText()
            }
        }
    }
}

@Composable
fun FloatingOrbs() {
    val infiniteTransition = rememberInfiniteTransition(label = "FloatingOrbs")
    
    repeat(5) { index ->
        val offsetX by infiniteTransition.animateFloat(
            initialValue = (-100 + index * 50).toFloat(),
            targetValue = (100 + index * 50).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween((3000 + index * 500), easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "orbX_$index"
        )

        val offsetY by infiniteTransition.animateFloat(
            initialValue = (-50 + index * 30).toFloat(),
            targetValue = (50 + index * 30).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween((2500 + index * 400), easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "orbY_$index"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = (50 + index * 70).dp,
                    top = (100 + index * 120).dp
                )
        ) {
            Box(
                modifier = Modifier
                    .size((40 + index * 20).dp)
                    .offset(x = offsetX.dp, y = offsetY.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
                    .blur(20.dp)
            )
        }
    }
}

@Composable
fun GeometricAccents() {
    val infiniteTransition = rememberInfiniteTransition(label = "GeometricAccents")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "geometricRotation"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Top right accent
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = (-50).dp)
                .size(150.dp)
                .graphicsLayer {
                    rotationZ = rotation * 0.5f
                }
                .clip(RoundedCornerShape(30.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .blur(15.dp)
        )

        // Bottom left accent
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-50).dp, y = 50.dp)
                .size(120.dp)
                .graphicsLayer {
                    rotationZ = -rotation * 0.3f
                }
                .clip(RoundedCornerShape(25.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .blur(15.dp)
        )
    }
}

@Composable
fun FuturisticLoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "LoadingIndicator")
    
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loadingProgress"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Animated dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                val dotScale by infiniteTransition.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 1200
                            0.6f at 0
                            1.2f at (index * 150 + 300) with FastOutSlowInEasing
                            0.6f at (index * 150 + 600) with FastOutSlowInEasing
                            0.6f at 1200
                        },
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "dot_$index"
                )

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .graphicsLayer {
                            scaleX = dotScale
                            scaleY = dotScale
                        }
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                        .shadow(4.dp, CircleShape, ambientColor = Color.White)
                )
            }
        }

        // Progress bar
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.6f),
                                Color.White,
                                Color.White.copy(alpha = 0.6f)
                            )
                        )
                    )
                    .shadow(4.dp, ambientColor = Color.White)
            )
        }
    }
}

@Composable
fun AnimatedGradientText() {
    val infiniteTransition = rememberInfiniteTransition(label = "TextGradient")
    
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "textGradientShift"
    )

    val textPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "textPulse"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = textPulse
                scaleY = textPulse
            }
    ) {
        // Shadow/glow layer
        Text(
            text = "KTiMAZ Studio",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            color = Color.White.copy(alpha = 0.3f),
            modifier = Modifier
                .offset(x = 2.dp, y = 2.dp)
                .blur(8.dp)
        )

        // Main text with gradient effect (simulated with white + opacity)
        Text(
            text = "KTiMAZ Studio",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            color = Color.White,
            style = MaterialTheme.typography.titleLarge.copy(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color(0xFFFFD93D).copy(alpha = 0.5f),
                    offset = androidx.compose.ui.geometry.Offset(0f, 0f),
                    blurRadius = 20f
                )
            )
        )
    }
}