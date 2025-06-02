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
// Removed: import androidx.core.view.WindowCompat // Not needed for non-edge-to-edge
// Removed: import com.google.accompanist.systemuicontroller.rememberSystemUiController // Not needed for non-edge-to-edge
import kotlinx.coroutines.delay

class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Removed: WindowCompat.setDecorFitsSystemWindows(window, false)
        // This line is removed to disable drawing behind system bars.

        setContent {
            // Removed: System UI Controller setup for transparent system bars.
            // The system bars will now use the default theme colors.

            // Your main splash screen composable
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
            // Removed: .padding(WindowInsets.systemBars.asPaddingValues())
            // This padding is no longer needed as content will not draw behind system bars.
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        // Enhanced Ripple Wave Effect
        EnhancedRippleWaveAnimation(transition)

        // Center Logo with blur behind
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
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(120.dp)
                )
            }
        }

        // Advanced Loading Dots under logo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 170.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AdvancedLoadingDots(transition)
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
fun EnhancedRippleWaveAnimation(transition: Transition<Boolean>) {
    val rippleCount = 3
    val maxRippleRadius = 400.dp
    val rippleDuration = 3000

    val rippleProgress by transition.animateFloat(
        transitionSpec = { infiniteRepeatable(
            animation = tween(rippleDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )},
        label = "rippleProgress"
    ) { state ->
        if (state) 1f else 0f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 100.dp),
        contentAlignment = Alignment.Center
    ) {
        repeat(rippleCount) { index ->
            val delayFraction = index.toFloat() / rippleCount
            val currentProgress = (rippleProgress + (1f - delayFraction)) % 1f

            val radius = maxRippleRadius * currentProgress
            val alpha = (1f - currentProgress).coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .size(radius)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f * alpha))
            )
        }
    }
}

/**
 * Creates an advanced loading dots animation where dots pulse in sequence.
 */
@Composable
fun AdvancedLoadingDots(transition: Transition<Boolean>) {
    val dotCount = 3
    val dotSize = 10.dp
    val animationDuration = 800
    val delayBetweenDots = 200

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(dotCount) { index ->
            val dotTransition = updateTransition(transition.currentState, label = "DotTransition_$index")

            val dotScale by dotTransition.animateFloat(
                transitionSpec = {
                    infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = animationDuration * dotCount + delayBetweenDots * (dotCount - 1)
                            0.7f at (index * delayBetweenDots).toLong()
                            1.2f at (index * delayBetweenDots + animationDuration / 2).toLong() easing LinearEasing
                            0.7f at (index * delayBetweenDots + animationDuration).toLong() easing LinearEasing
                            0.7f at durationMillis.toLong()
                        },
                        repeatMode = RepeatMode.Restart
                    )
                },
                label = "dotScale_$index"
            ) { state ->
                if (state) 1f else 0.7f
            }

            val dotAlpha by dotTransition.animateFloat(
                transitionSpec = {
                    infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = animationDuration * dotCount + delayBetweenDots * (dotCount - 1)
                            0.5f at (index * delayBetweenDots).toLong()
                            1f at (index * delayBetweenDots + animationDuration / 2).toLong() easing LinearEasing
                            0.5f at (index * delayBetweenDots + animationDuration).toLong() easing LinearEasing
                            0.5f at durationMillis.toLong()
                        },
                        repeatMode = RepeatMode.Restart
                    )
                },
                label = "dotAlpha_$index"
            ) { state ->
                if (state) 1f else 0.5f
            }

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
// This function is still useful if you want to manually set status bar icon colors
// even without full edge-to-edge, but it's not directly used for the primary
// edge-to-edge setup anymore. Keeping it for potential future use or manual control.
private fun Color.luminance(): Float {
    return (0.2126f * red + 0.7152f * green + 0.0722f * blue)
}
