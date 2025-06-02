package com.ktimazstudio

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Your main splash screen composable
            SplashScreenContent()
        }
    }
}

@Composable
fun SplashScreenContent() {
    val context = LocalContext.current
    // MutableTransitionState helps orchestrate multiple animations based on a single state
    val splashScreenState = remember { MutableTransitionState(false) }

    LaunchedEffect(Unit) {
        // Trigger the animations
        splashScreenState.targetState = true
        // Delay for animations to play out (adjust as needed)
        delay(4000) // Total animation duration + buffer
        // Navigate to MainActivity and finish this activity
        context.startActivity(Intent(context, MainActivity::class.java))
        if (context is ComponentActivity) context.finish()
    }

    // Define the transition for the entire splash screen animation
    val transition = updateTransition(splashScreenState, label = "SplashScreenTransition")

    // Logo scale animation with a spring effect for a more dynamic pop
    val logoScale by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 1000, easing = EaseOutBounce) },
        label = "logoScale"
    ) { state ->
        if (state) 1.2f else 0.5f // Start slightly smaller, then pop larger
    }

    // Logo rotation animation
    val logoRotation by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 2000, easing = FastOutSlowInEasing) },
        label = "logoRotation"
    ) { state ->
        if (state) 720f else 0f
    }

    // Logo alpha animation for initial fade-in
    val logoAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 800, delayMillis = 200) },
        label = "logoAlpha"
    ) { state ->
        if (state) 1f else 0f
    }

    // Powered By text alpha animation
    val poweredByAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 800, delayMillis = 2500) }, // Fade in after logo
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
        // Enhanced Ripple Wave Effect now uses its own InfiniteTransition
        EnhancedRippleWaveAnimation()

        // Center Logo with blur behind
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp), // Adjust padding to center visually
            contentAlignment = Alignment.Center
        ) {
            // Blurred background circle for the logo
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .blur(30.dp)
            )

            // Logo container with scale, rotation, and alpha animations
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
                    .background(MaterialTheme.colorScheme.primary), // Changed to primary for better contrast
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher), // Ensure you have your app icon here
                    contentDescription = "App Logo",
                    modifier = Modifier.size(120.dp)
                )
            }
        }

        // Advanced Loading Dots under logo now uses its own InfiniteTransition
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 170.dp), // Adjust padding
            contentAlignment = Alignment.BottomCenter
        ) {
            AdvancedLoadingDots()
        }

        // Powered By Text with fade-in animation
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
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                modifier = Modifier.graphicsLayer {
                    alpha = poweredByAlpha
                }
            )
        }
    }
}

/**
 * Creates an enhanced ripple wave animation with multiple expanding circles.
 */
@Composable
fun EnhancedRippleWaveAnimation() {
    val rippleCount = 3 // Number of ripples
    val maxRippleRadius = 400.dp // Maximum radius for a ripple
    val rippleDuration = 3000 // Duration for one ripple cycle in ms

    val infiniteTransition = rememberInfiniteTransition(label = "RippleInfiniteTransition")

    // Animate the progress of the overall ripple effect
    val rippleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(rippleDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 100.dp), // Align with logo
        contentAlignment = Alignment.Center
    ) {
        repeat(rippleCount) { index ->
            // Calculate delay for each ripple
            val delayFraction = index.toFloat() / rippleCount
            val currentProgress = (rippleProgress + (1f - delayFraction)) % 1f

            val radius = maxRippleRadius * currentProgress
            val alpha = (1f - currentProgress).coerceIn(0f, 1f) // Fade out as it expands

            Box(
                modifier = Modifier
                    .size(radius)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f * alpha)) // Adjust alpha for visibility
            )
        }
    }
}

/**
 * Creates an advanced loading dots animation where dots pulse in sequence.
 */
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
                        // All timestamps passed to 'at' must be Int
                        durationMillis = (animationDuration * dotCount + delayBetweenDots * (dotCount - 1)).toInt()
                        0.7f at (index * delayBetweenDots) with LinearEasing
                        1.2f at (index * delayBetweenDots + animationDuration / 2) with LinearEasing
                        0.7f at (index * delayBetweenDots + animationDuration) with LinearEasing
                        0.7f at durationMillis // Use durationMillis directly, which is already Int
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
                        // All timestamps passed to 'at' must be Int
                        durationMillis = (animationDuration * dotCount + delayBetweenDots * (dotCount - 1)).toInt()
                        0.5f at (index * delayBetweenDots) with LinearEasing
                        1f at (index * delayBetweenDots + animationDuration / 2) with LinearEasing
                        0.5f at (index * delayBetweenDots + animationDuration) with LinearEasing
                        0.5f at durationMillis // Use durationMillis directly, which is already Int
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

// Helper function to determine luminance for icon color adjustment
private fun Color.luminance(): Float {
    return (0.2126f * red + 0.7152f * green + 0.0722f * blue)
}
