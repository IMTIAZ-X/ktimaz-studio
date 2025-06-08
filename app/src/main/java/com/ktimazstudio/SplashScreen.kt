package com.ktimazstudio

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Animatable // Needed for animateColor if used as a standalone Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor // IMPORTANT: Added this import
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
import androidx.compose.ui.text.ExperimentalTextApi // Still needed for Brush.linearGradient for Text.style
import androidx.compose.ui.graphics.Shadow // For text shadow
import androidx.compose.ui.geometry.Offset // For shadow offset
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

    val minimumDisplayTime = 2000L

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        splashScreenState.targetState = true

        // Wait for logo animations to finish OR minimum display time to pass
        while (!logoAnimationFinished.value && (System.currentTimeMillis() - startTime < minimumDisplayTime)) {
            delay(50)
        }
        poweredByTextAnimationStarted.value = true // Trigger text animation after logo/min time

        // Calculate total delay including text animations:
        // 1800ms (previous delay until text starts)
        // + (70ms * "Powered by KTiMAZ Studio".length) (Typewriter effect duration)
        // + 1000ms (buffer for glow/gradient/shadow animations to run for a bit)
        delay(1800L + (70L * "Powered by KTiMAZ Studio".length) + 1000L)

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
        RippleWaveAnimation()

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
                    initialDelayMillis = 0L // Delay is handled by the parent AnimatedVisibility
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
            animation = tween(2500, easing = LinearEasing),
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
    initialDelayMillis: Long = 0L,
    textColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    shadowBaseColor: Color = Color.Black // Base color for the shadow
) {
    var typedText by remember { mutableStateOf("") }
    val isTypingFinished = remember { mutableStateOf(false) }

    // State for shadow animation
    val shadowOffsetX by animateFloatAsState(
        targetValue = if (isTypingFinished.value) 4f else 0f,
        animationSpec = tween(durationMillis = 500), label = "shadowOffsetX"
    )
    val shadowOffsetY by animateFloatAsState(
        targetValue = if (isTypingFinished.value) 4f else 0f,
        animationSpec = tween(durationMillis = 500), label = "shadowOffsetY"
    )
    val shadowBlurRadius by animateFloatAsState(
        targetValue = if (isTypingFinished.value) 8f else 0f,
        animationSpec = tween(durationMillis = 500), label = "shadowBlurRadius"
    )
    val shadowColorAlpha by animateFloatAsState(
        targetValue = if (isTypingFinished.value) 0.6f else 0.0f,
        animationSpec = tween(durationMillis = 500), label = "shadowColorAlpha"
    )


    // State for Pulsing Glow (main text color)
    val glowPulseColor1 = MaterialTheme.colorScheme.primary
    val glowPulseColor2 = MaterialTheme.colorScheme.primaryContainer
    val animatedTextColor by rememberInfiniteTransition(label = "textGlow").animateColor(
        initialValue = glowPulseColor1,
        targetValue = glowPulseColor2,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "textGlowColor"
    )

    // State for Gradient Shift (overlay brush)
    // The gradient will shift across the text
    val gradientShiftProgress by rememberInfiniteTransition(label = "gradientShift").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "gradientShiftProgress"
    )

    LaunchedEffect(text) {
        delay(initialDelayMillis)
        val typingSpeedMillis = 70L

        text.forEachIndexed { index, char ->
            typedText = text.substring(0, index + 1)
            delay(typingSpeedMillis)
        }
        isTypingFinished.value = true
    }

    val currentShadowColor = shadowBaseColor.copy(alpha = shadowColorAlpha)

    Text(
        text = typedText,
        modifier = modifier,
        style = TextStyle(
            fontSize = fontSize,
            fontWeight = fontWeight,
            // Apply pulsating glow and gradient shift as a brush after typing is finished
            brush = if (isTypingFinished.value) {
                Brush.linearGradient(
                    colors = listOf(animatedTextColor, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)),
                    // Adjust start/end to make the gradient shift
                    // We'll use a larger width for the gradient to sweep across the text
                    start = Offset(typedText.length * fontSize.value * -0.5f + typedText.length * fontSize.value * gradientShiftProgress, 0f),
                    end = Offset(typedText.length * fontSize.value * 0.5f + typedText.length * fontSize.value * gradientShiftProgress, 0f)
                )
            } else {
                // During typing, just use a solid color
                Brush.linearGradient(listOf(textColor, textColor))
            },
            // Apply animated shadow
            shadow = Shadow(
                color = currentShadowColor,
                offset = Offset(shadowOffsetX, shadowOffsetY),
                blurRadius = shadowBlurRadius
            )
        ),
        color = Color.Unspecified // Must be Unspecified if using Brush for TextStyle
    )
}
