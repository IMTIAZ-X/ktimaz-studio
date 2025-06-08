package com.ktimazstudio

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

    // Track animation completion
    val logoAnimationFinished = remember { mutableStateOf(false) }
    val poweredByAnimationFinished = remember { mutableStateOf(false) }

    // Minimum display time for the splash screen to avoid flickering
    val minimumDisplayTime = 2000L // 2 seconds

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        splashScreenState.targetState = true

        // Wait for all main animations to complete
        // The longest animation is logoRotation at 2000ms.
        // PoweredBy text starts at 2500ms and lasts 800ms, so 3300ms total.
        // We'll wait for the "Powered By" animation to signal completion.
        // This is handled implicitly by the `poweredByAnimationFinished` state.

        // Wait until both main animations are finished or minimum display time is met.
        // The actual navigation will happen once all animations are truly done.
        while (!(logoAnimationFinished.value && poweredByAnimationFinished.value) &&
               (System.currentTimeMillis() - startTime < minimumDisplayTime)) {
            delay(50) // Small delay to prevent busy-waiting
        }

        context.startActivity(Intent(context, MainActivity::class.java))
        if (context is ComponentActivity) context.finish()
    }

    val transition = updateTransition(splashScreenState, label = "SplashScreenTransition")

    // Logo scale animation with a spring effect for a more dynamic pop
    val logoScale by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 800, easing = EaseOutBounce) // Slightly faster
        },
        label = "logoScale"
    ) { state ->
        if (state) 1.1f else 0.5f // Smaller pop, faster feel
    }

    // Logo rotation animation
    val logoRotation by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 1800, easing = FastOutSlowInEasing) // Slightly faster
        },
        label = "logoRotation"
    ) { state ->
        if (state) 720f else 0f
    }

    // Logo alpha animation for initial fade-in
    val logoAlpha by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 600, delayMillis = 100) // Slightly faster
        },
        label = "logoAlpha"
    ) { state ->
        if (state) 1f else 0f
    }

    // Monitor logo animation completion
    LaunchedEffect(logoScale, logoRotation, logoAlpha) {
        // If all logo-related animations reach their target values
        if (logoScale == 1.1f && logoRotation == 720f && logoAlpha == 1f) {
            logoAnimationFinished.value = true
        }
    }


    // Powered By text alpha animation
    val poweredByAlpha by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 700, delayMillis = 1800) // Starts slightly earlier, faster fade
        },
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
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f), // Softer primary container
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        // Ripple Wave Effect - Subtler
        RippleWaveAnimation()

        // Center Logo with blur behind
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background blur circle - slightly smaller, less prominent
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)) // Use primary color, very subtle
                    .blur(25.dp) // Slightly less blur
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
                    .background(MaterialTheme.colorScheme.primary), // Use primary color for the logo background
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
            AdvancedLoadingDots()
        }

        // Powered By Text
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
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), // Slightly less opaque
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
        targetValue = 350f, // Slightly smaller max radius
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing), // Slightly faster wave
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleRadius"
    )
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2500
                0.1f at 0 with LinearEasing
                0.0f at 2000 // Fade out sooner
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleAlpha"
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
                .background(MaterialTheme.colorScheme.primary.copy(alpha = rippleAlpha)) // Use primary color, controlled by alpha
        )
    }
}

@Composable
fun AdvancedLoadingDots() {
    val dotCount = 3
    val dotSize = 10.dp
    val animationDuration = 700 // Duration for one dot's pulse (slightly faster)
    val delayBetweenDots = 150 // Delay before the next dot starts its pulse (slightly less)

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
                        1.1f at (index * delayBetweenDots + animationDuration / 2) with LinearEasing // Smaller peak
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
                        0.9f at (index * delayBetweenDots + animationDuration / 2) with LinearEasing // Less opaque peak
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
                    .background(MaterialTheme.colorScheme.primary) // Keep primary color
            )
        }
    }
}
