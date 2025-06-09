package com.ktimazstudio

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.draw.* // Keep for .clip()
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer // Keep for scale, rotation, alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.ExperimentalTextApi // Still needed if you use Brush in TextStyle later, but not for this version
import androidx.compose.ui.graphics.Shadow // Still needed if you want a *static* shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.TextUnit
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

    val logoAnimationFinished = remember { mutableStateOf(false) }
    val poweredByTextAnimationFinished = remember { mutableStateOf(false) }

    // Crucial: Keep minimum display time short for perceived speed
    val minimumDisplayTime = 1000L // 1 second

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        splashScreenState.targetState = true // Start all animations

        // Determine when to navigate:
        // Wait for logo animation to complete, OR until minimumDisplayTime is met, whichever is later.
        // We'll make logo animation very fast.
        val logoFinishTime = startTime + 600L // Approx time for quick logo anim
        val textFinishTime = logoFinishTime + 300L // Approx time for quick text anim

        // Wait until all main animations have had a chance to start and finish
        // (This assumes the durations below are quick)
        delay(maxOf(minimumDisplayTime, textFinishTime - startTime)) // Wait for essential animations + min time

        context.startActivity(Intent(context, MainActivity::class.java))
        if (context is ComponentActivity) context.finish()
    }

    val transition = updateTransition(splashScreenState, label = "SplashScreenTransition")

    // Logo scale: Quick pop
    val logoScale by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 600, easing = EaseOutBounce) }, // Faster
        label = "logoScale"
    ) { state ->
        if (state) 1.2f else 0.5f
    }

    // Logo rotation: Quick 360 spin
    val logoRotation by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 800, easing = FastOutSlowInEasing) }, // Much Faster
        label = "logoRotation"
    ) { state ->
        if (state) 360f else 0f // Only one full spin
    }

    // Logo alpha: Quick fade in
    val logoAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 400, delayMillis = 50) }, // Faster
        label = "logoAlpha"
    ) { state ->
        if (state) 1f else 0f
    }

    // Mark logo animation as finished quickly for subsequent actions (like text)
    LaunchedEffect(logoScale, logoRotation, logoAlpha) {
        if (logoScale == 1.2f && logoRotation == 360f && logoAlpha == 1f) {
            logoAnimationFinished.value = true
        }
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
        // Ripple Wave: Make it much faster and simpler
        RippleWaveAnimation()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            contentAlignment = Alignment.Center
        ) {
            // REMOVED THE BLURRED CIRCLE - THIS IS A HUGE PERFORMANCE GAIN
            // Instead, just a solid translucent circle if you want something behind the logo
            Box(
                modifier = Modifier
                    .size(200.dp) // Slightly smaller
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f)) // Simple translucent background
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

        // Loading Dots: Keep, they are relatively cheap
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 170.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AdvancedLoadingDots()
        }

        // "Powered By" Text: Simple fade in
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Trigger text fade in after logo is mostly visible
            AnimatedPoweredByText(
                text = "Powered by KTiMAZ Studio",
                initialDelayMillis = 600L, // Start shortly after logo
                onAnimationFinish = { poweredByTextAnimationFinished.value = true }
            )
        }
    }
}

@Composable
fun RippleWaveAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "RippleWaveAnimation")
    val rippleRadius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing), // Much faster ripple
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleRadius"
    )
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0f, // Fade out as it expands
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing), // Match ripple speed
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
                .background(Color.White.copy(alpha = rippleAlpha)) // Use animated alpha
        )
    }
}

@Composable
fun AdvancedLoadingDots() {
    val dotCount = 3
    val dotSize = 10.dp
    val animationDuration = 700
    val delayBetweenDots = 150

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
fun AnimatedPoweredByText(
    text: String,
    modifier: Modifier = Modifier,
    initialDelayMillis: Long = 0L,
    textColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    staticShadowColor: Color = Color.Black.copy(alpha = 0.5f), // Static shadow, if desired
    onAnimationFinish: () -> Unit
) {
    val textAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 300, delayMillis = initialDelayMillis), // Quick fade-in
        label = "textAlpha"
    )

    // Notify parent when animation is complete.
    // We'll consider it "finished" shortly after its fade-in animation, not after a long typewriter.
    LaunchedEffect(textAlpha) {
        if (textAlpha == 1f) {
            onAnimationFinish()
        }
    }

    Text(
        text = text, // Full text instantly
        modifier = modifier.graphicsLayer {
            alpha = textAlpha
        },
        style = TextStyle(
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = textColor,
            // Keep a static shadow if it's crucial for the look, but it's an extra draw call
            shadow = Shadow(
                color = staticShadowColor,
                offset = Offset(2f, 2f), // Smaller offset
                blurRadius = 4f // Smaller blur
            )
        )
    )
}
