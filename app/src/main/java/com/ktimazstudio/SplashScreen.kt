package com.ktimazstudio

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor // For pulsing color
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.graphics.Shadow
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
    val poweredByTextAnimationStarted = remember { mutableStateOf(false) }
    val poweredByTextAnimationFinished = remember { mutableStateOf(false) } // Track text animation completion

    val minimumDisplayTime = 1500L // Reduced minimum display time for faster app open

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        splashScreenState.targetState = true

        // Wait for logo animations to finish
        // We'll trust that the logo animation is fast enough (1.8s for rotation)
        // This will be the main bottleneck for when the text animation can start
        delay(1800L) // Wait for logo rotation to finish

        poweredByTextAnimationStarted.value = true // Trigger text animation

        // Wait for text animation to finish OR minimum display time to pass
        val textAnimationEstimatedDuration = (70L * "Powered by KTiMAZ Studio".length) + 800L // Typewriter + a bit for gradient
        val totalAnimationTime = System.currentTimeMillis() - startTime

        if (!poweredByTextAnimationFinished.value && totalAnimationTime < minimumDisplayTime) {
            delay(minimumDisplayTime - totalAnimationTime) // Wait for remaining min time if needed
        }
        if (!poweredByTextAnimationFinished.value) { // Ensure text animation also has time to complete
             delay(textAnimationEstimatedDuration) // Wait for the text animation to fully complete
        }

        context.startActivity(Intent(context, MainActivity::class.java))
        if (context is ComponentActivity) context.finish()
    }

    val transition = updateTransition(splashScreenState, label = "SplashScreenTransition")

    val logoScale by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 800, easing = EaseOutBounce) },
        label = "logoScale"
    ) { state ->
        if (state) 1.2f else 0.5f
    }

    val logoRotation by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 1800, easing = FastOutSlowInEasing) },
        label = "logoRotation"
    ) { state ->
        if (state) 720f else 0f
    }

    val logoAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 600, delayMillis = 100) },
        label = "logoAlpha"
    ) { state ->
        if (state) 1f else 0f
    }

    LaunchedEffect(logoScale, logoRotation, logoAlpha) {
        if (logoScale == 1.2f && logoRotation == 720f && logoAlpha == 1f) {
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
        // Ripple Wave is moderately expensive due to size change and alpha blend. Keep its duration balanced.
        RippleWaveAnimation()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            contentAlignment = Alignment.Center
        ) {
            // Blur is expensive. Keep its radius moderate.
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

        // Loading Dots are relatively cheap.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 170.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AdvancedLoadingDots()
        }

        // Animated "Powered By" Text
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(visible = poweredByTextAnimationStarted.value) {
                AnimatedPoweredByText(
                    text = "Powered by KTiMAZ Studio",
                    onAnimationFinish = { poweredByTextAnimationFinished.value = true }
                )
            }
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
            animation = tween(2500, easing = LinearEasing), // Consider reducing if still laggy
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

@OptIn(ExperimentalTextApi::class)
@Composable
fun AnimatedPoweredByText(
    text: String,
    modifier: Modifier = Modifier,
    // Removed initialDelayMillis as it's handled by AnimatedVisibility parent
    textColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    staticShadowColor: Color = Color.Black.copy(alpha = 0.5f), // Static shadow for performance
    onAnimationFinish: () -> Unit // Callback to notify parent
) {
    var typedText by remember { mutableStateOf("") }
    val isTypingFinished = remember { mutableStateOf(false) }

    // Pulsing Glow colors (using theme colors for consistency)
    val glowColorStart = MaterialTheme.colorScheme.primary
    val glowColorEnd = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)

    val animatedGlowColor by rememberInfiniteTransition(label = "textGlow").animateColor(
        initialValue = glowColorStart,
        targetValue = glowColorEnd,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "textGlowColor"
    )

    // Gradient Shift progress (for the shimmer effect)
    val gradientShiftProgress by rememberInfiniteTransition(label = "gradientShift").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing), // Longer duration for smooth shimmer
            repeatMode = RepeatMode.Restart
        ), label = "gradientShiftProgress"
    )

    LaunchedEffect(text) {
        val typingSpeedMillis = 70L // milliseconds per character

        text.forEachIndexed { index, char ->
            typedText = text.substring(0, index + 1)
            delay(typingSpeedMillis)
        }
        isTypingFinished.value = true
        delay(800L) // Allow gradient/glow to play for a bit after typing
        onAnimationFinish() // Notify parent that text animation is complete
    }

    Text(
        text = typedText,
        modifier = modifier,
        style = TextStyle(
            fontSize = fontSize,
            fontWeight = fontWeight,
            // Apply a combined brush for pulsating glow and gradient shift after typing
            brush = if (isTypingFinished.value) {
                Brush.linearGradient(
                    colors = listOf(
                        animatedGlowColor.copy(alpha = 0.2f), // Start with a lighter, more transparent glow part
                        animatedGlowColor, // Main glow color
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f) // Original text color part
                    ),
                    // Adjust start/end to make the gradient sweep across the text, creating a shimmer/glow effect
                    start = Offset(typedText.length * fontSize.value * -0.5f + typedText.length * fontSize.value * gradientShiftProgress, 0f),
                    end = Offset(typedText.length * fontSize.value * 0.5f + typedText.length * fontSize.value * gradientShiftProgress, 0f)
                )
            } else {
                // During typing, just use a solid color
                Brush.linearGradient(listOf(textColor, textColor))
            },
            // Static shadow for performance - if animated shadow is critical, it will add lag.
            shadow = Shadow(
                color = staticShadowColor,
                offset = Offset(4f, 4f), // Fixed offset
                blurRadius = 8f // Fixed blur
            )
        ),
        color = Color.Unspecified // Must be Unspecified if using Brush for TextStyle
    )
}
