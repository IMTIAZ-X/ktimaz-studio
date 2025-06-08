package com.ktimazstudio

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge // Required for edge-to-edge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay

class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable edge-to-edge display
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

    // Track animation completion for dynamic navigation
    val logoAnimationFinished = remember { mutableStateOf(false) }
    val poweredByAnimationFinished = remember { mutableStateOf(false) }

    // Minimum display time to prevent flickering on ultra-fast devices
    val minimumDisplayTime = 2000L // 2 seconds

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        splashScreenState.targetState = true

        // Wait until all main animations are finished AND minimum display time is met
        while (!(logoAnimationFinished.value && poweredByAnimationFinished.value) &&
               (System.currentTimeMillis() - startTime < minimumDisplayTime)) {
            delay(50) // Small delay to prevent busy-waiting
        }

        context.startActivity(Intent(context, MainActivity::class.java))
        if (context is ComponentActivity) context.finish()
    }

    val transition = updateTransition(splashScreenState, label = "SplashScreenTransition")

    // Logo scale animation (kept the speed from the previous optimization)
    val logoScale by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 800, easing = EaseOutBounce) },
        label = "logoScale"
    ) { state ->
        if (state) 1.2f else 0.5f // Original larger pop
    }

    // Logo rotation animation (kept the speed from the previous optimization)
    val logoRotation by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 1800, easing = FastOutSlowInEasing) },
        label = "logoRotation"
    ) { state ->
        if (state) 720f else 0f
    }

    // Logo alpha animation (kept the speed from the previous optimization)
    val logoAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 600, delayMillis = 100) },
        label = "logoAlpha"
    ) { state ->
        if (state) 1f else 0f
    }

    // Monitor logo animation completion
    LaunchedEffect(logoScale, logoRotation, logoAlpha) {
        if (logoScale == 1.2f && logoRotation == 720f && logoAlpha == 1f) {
            logoAnimationFinished.value = true
        }
    }

    // Powered By text alpha animation (kept the speed from the previous optimization)
    val poweredByAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 700, delayMillis = 1800) },
        label = "poweredByAlpha"
    ) { state ->
        if (state) 1f else 0f
    }

    // Monitor Powered By animation completion
    LaunchedEffect(poweredByAlpha) {
        if (poweredByAlpha == 1f) {
            poweredByAnimationFinished.value = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer, // Original color
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        // Ripple Wave Effect (Original appearance with optimized speed)
        RippleWaveAnimation()

        // Center Logo with blur behind (Original appearance)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp) // Original size
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f)) // Original color and alpha
                    .blur(30.dp) // Original blur
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
                    .background(MaterialTheme.colorScheme.primaryContainer), // Original color
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(120.dp)
                )
            }
        }

        // Advanced Loading Dots under logo (Original appearance with optimized speed)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 170.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AdvancedLoadingDots()
        }

        // Powered By Text (Original appearance)
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
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f), // Original color and alpha
                modifier = Modifier.graphicsLayer {
                    alpha = poweredByAlpha
                }
            )
        }
    }
}

@Composable
fun RippleWaveAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "RippleWaveAnimation")
    val rippleRadius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 400f, // Original size
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing), // Kept optimized speed
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
                .background(Color.White.copy(alpha = 0.08f)) // Original color and alpha
        )
    }
}

@Composable
fun AdvancedLoadingDots() {
    val dotCount = 3
    val dotSize = 10.dp
    val animationDuration = 700 // Kept optimized speed
    val delayBetweenDots = 150 // Kept optimized speed

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
                        1.2f at (index * delayBetweenDots + animationDuration / 2) with LinearEasing // Original peak
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
                        1f at (index * delayBetweenDots + animationDuration / 2) with LinearEasing // Original peak
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
