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
    ) { state -> if (state) 1.2f else 0.5f }

    val logoRotation by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 2000, easing = FastOutSlowInEasing) },
        label = "logoRotation"
    ) { state -> if (state) 720f else 0f }

    val logoAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 800, delayMillis = 200) },
        label = "logoAlpha"
    ) { state -> if (state) 1f else 0f }

    val poweredByAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 800, delayMillis = 2500) },
        label = "poweredByAlpha"
    ) { state -> if (state) 1f else 0f }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        RippleWaveAnimation()

        // LOGO Section
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .blur(30.dp)
            )

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .graphicsLayer {
                        scaleX = logoScale
                        scaleY = logoScale
                        rotationZ = logoRotation
                        alpha = logoAlpha
                    }
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(120.dp)
                )
            }
        }

        // ✅ Advanced Loading Dots (from new code)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 230.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AdvancedLoadingDots()
        }

        // ✅ Modern Progress Bar (from old code)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 170.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            ModernLoadingBar()
        }

        // ✅ Powered by text
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer { alpha = poweredByAlpha }
            ) {
                Text(
                    text = "POWERED BY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 3.sp,
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                AnimatedStudioName()
            }
        }
    }
}

// -------------------------------------------------------------
// Ripple background animation
// -------------------------------------------------------------
@Composable
fun RippleWaveAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "RippleWaveAnimation")
    val rippleRadius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleRadius"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 100.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(rippleRadius.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        )
    }
}

// -------------------------------------------------------------
// ✅ Advanced Loading Dots
// -------------------------------------------------------------
@Composable
fun AdvancedLoadingDots() {
    val dotCount = 3
    val dotSize = 10.dp
    val animationDuration = 800 // Duration for one dot's pulse
    val delayBetweenDots = 200 // Delay before the next dot starts its pulse

    val infiniteTransition = rememberInfiniteTransition(label = "LoadingDotsInfiniteTransition")

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(dotCount) { index ->
            val dotScale by infiniteTransition.animateFloat(
                initialValue = 0.7f,
                targetValue = 0.7f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = (animationDuration * dotCount + delayBetweenDots * (dotCount - 1))
                        0.7f at (index * delayBetweenDots) with LinearEasing
                        1.2f at (index * delayBetweenDots + animationDuration / 2) with LinearEasing
                        0.7f at (index * delayBetweenDots + animationDuration) with LinearEasing
                        0.7f at durationMillis
                    },
                    repeatMode = RepeatMode.Restart
                ),
                label = "dotScale_$index"
            )

            val dotAlpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 0.5f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = (animationDuration * dotCount + delayBetweenDots * (dotCount - 1))
                        0.5f at (index * delayBetweenDots) with LinearEasing
                        1f at (index * delayBetweenDots + animationDuration / 2) with LinearEasing
                        0.5f at (index * delayBetweenDots + animationDuration) with LinearEasing
                        0.5f at durationMillis
                    },
                    repeatMode = RepeatMode.Restart
                ),
                label = "dotAlpha_$index"
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(dotSize)
                    .graphicsLayer {
                        scaleX = dotScale
                        scaleY = dotScale
                        alpha = dotAlpha
                    }
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}


@Composable
fun Dot(scale: Float) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .graphicsLayer {
                scaleX = 0.7f + 0.3f * scale
                scaleY = 0.7f + 0.3f * scale
            }
            .clip(CircleShape)
            .background(Color(0xFF6C63FF).copy(alpha = 0.8f))
    )
}

// -------------------------------------------------------------
// ✅ Modern Progress Bar
// -------------------------------------------------------------
@Composable
fun ModernLoadingBar() {
    val infiniteTransition = rememberInfiniteTransition(label = "LoadingBar")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.Black.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF6C63FF), Color(0xFFFF6B9D))
                        )
                    )
            )
        }
    }
}

// -------------------------------------------------------------
// Animated studio name
// -------------------------------------------------------------
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
                    color = Color(0xFF6C63FF).copy(alpha = 0.5f),
                    offset = androidx.compose.ui.geometry.Offset(0f, 0f),
                    blurRadius = 25f
                )
            )
        )
    }
}
