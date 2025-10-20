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
        // Ripple animation (from new code)
        RippleWaveAnimation()

        // Logo section (unchanged)
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

        // Loading animation (new)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 170.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AdvancedLoadingDots()
        }

        // ✅ Powered by section (merged from old code)
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
                    color = if (isSystemInDarkTheme())
                        Color.White.copy(alpha = 0.5f)
                    else
                        Color.Black.copy(alpha = 0.4f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                AnimatedStudioName() // from old version
            }
        }
    }
}

// ---------------------------------------------------------------
// New effects kept from your latest version
// ---------------------------------------------------------------

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

@Composable
fun AdvancedLoadingDots() {
    val dotCount = 3
    val dotSize = 10.dp
    val singleDotDuration = 400
    val delayBetweenDots = 200
    val totalDuration = singleDotDuration * 2 + delayBetweenDots * (dotCount - 1)

    val infiniteTransition = rememberInfiniteTransition(label = "LoadingDotsInfiniteTransition")

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(dotCount) { index ->
            val startDelay = index * delayBetweenDots

            val dotScale by infiniteTransition.animateFloat(
                initialValue = 0.7f,
                targetValue = 0.7f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = totalDuration
                        0.7f at startDelay
                        1.2f at startDelay + singleDotDuration / 2
                        0.7f at startDelay + singleDotDuration
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
                        durationMillis = totalDuration
                        0.5f at startDelay
                        1f at startDelay + singleDotDuration / 2
                        0.5f at startDelay + singleDotDuration
                        0.5f at durationMillis
                    },
                    repeatMode = RepeatMode.Restart
                ),
                label = "dotAlpha_$index"
            )

            Box(
                modifier = Modifier
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

// ---------------------------------------------------------------
// From old version — reused for powered by section
// ---------------------------------------------------------------

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
