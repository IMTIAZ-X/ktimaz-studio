package com.ktimazstudio

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Animatable
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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import kotlinx.coroutines.delay

class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            SplashScreen()
        }
    }
}

@Composable
fun SplashScreen() {
    val context = LocalContext.current
    val splashScreenState = remember { MutableTransitionState(false) }

    val logoAnimationFinished = remember { mutableStateOf(false) }
    val poweredByTextAnimationStarted = remember { mutableStateOf(false) } // New state to trigger text anim

    val minimumDisplayTime = 2000L

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        splashScreenState.targetState = true

        // Wait for logo animations to finish
        while (!logoAnimationFinished.value && (System.currentTimeMillis() - startTime < minimumDisplayTime)) {
            delay(50)
        }
        poweredByTextAnimationStarted.value = true // Trigger text animation after logo

        // Adjust this delay based on how long the text animations are
        // Typewriter (70ms * 23 chars) + any subsequent glows/shifts. Let's estimate
        // a total text animation duration.
        delay(1800 + (70L * "Powered by KTiMAZ Studio".length) + 1000) // Approx 1.8s delay + typewriter + a bit more for glow/shift

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
            AnimatedVisibility(visible = poweredByTextAnimationStarted.value) { // Only show once triggered
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

// --- NEW ANIMATED TEXT COMPOSABLE ---

@OptIn(ExperimentalTextApi::class) // For Brush.linearGradient and graphicsLayer blend mode
@Composable
fun AnimatedPoweredByText(
    text: String,
    modifier: Modifier = Modifier,
    initialDelayMillis: Long = 0L,
    textColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    shadowColor: Color = Color.Black.copy(alpha = 0.6f) // Base shadow color
) {
    var typedText by remember { mutableStateOf("") }
    val isTypingFinished = remember { mutableStateOf(false) }

    // State for shadow animation
    val shadowOffsetX by animateDpAsState(
        targetValue = if (isTypingFinished.value) 4.dp else 0.dp,
        animationSpec = tween(durationMillis = 500), label = "shadowOffsetX"
    )
    val shadowOffsetY by animateDpAsState(
        targetValue = if (isTypingFinished.value) 4.dp else 0.dp,
        animationSpec = tween(durationMillis = 500), label = "shadowOffsetY"
    )
    val shadowAlpha by animateFloatAsState(
        targetValue = if (isTypingFinished.value) 0.6f else 0.0f,
        animationSpec = tween(durationMillis = 500), label = "shadowAlpha"
    )

    // State for Pulsing Glow (main color)
    val glowPulseColor1 = MaterialTheme.colorScheme.primary // Use primary color for glow
    val glowPulseColor2 = MaterialTheme.colorScheme.primaryContainer // Use primaryContainer for glow
    val animatedColor by rememberInfiniteTransition(label = "textGlow").animateColor(
        initialValue = glowPulseColor1,
        targetValue = glowPulseColor2,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "textGlowColor"
    )

    // State for Gradient Shift (overlay brush)
    val gradientShiftX by rememberInfiniteTransition(label = "gradientShift").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "gradientShiftX"
    )

    LaunchedEffect(text) {
        delay(initialDelayMillis) // Initial delay before typing starts
        val typingSpeedMillis = 70L // milliseconds per character

        text.forEachIndexed { index, char ->
            typedText = text.substring(0, index + 1)
            delay(typingSpeedMillis)
        }
        isTypingFinished.value = true // Signal that typing is done
    }

    // This Text Composable will combine all the effects
    Text(
        text = typedText,
        modifier = modifier
            .graphicsLayer {
                // Apply shadow dynamically
                val currentShadowColor = shadowColor.copy(alpha = shadowAlpha)
                renderWithLayer {
                    drawText(
                        typedText,
                        color = currentShadowColor,
                        style = TextStyle(
                            fontSize = fontSize,
                            fontWeight = fontWeight,
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = currentShadowColor,
                                offset = androidx.compose.ui.geometry.Offset(shadowOffsetX.toPx(), shadowOffsetY.toPx()),
                                blurRadius = 8.dp.toPx()
                            )
                        )
                    )
                }
            },
        // Apply the base text style with animated color and gradient
        style = TextStyle(
            fontSize = fontSize,
            fontWeight = fontWeight,
            brush = if (isTypingFinished.value) { // Apply brush only after typing is finished
                Brush.linearGradient(
                    colors = listOf(animatedColor, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(typedText.length * fontSize.value * gradientShiftX, 0f) // Shift based on text length and animation
                )
            } else {
                Brush.linearGradient(listOf(textColor, textColor)) // Solid color during typing
            }
        ),
        color = Color.Unspecified // Must be Unspecified if using Brush
    )

    // --- CONCEPTUAL/ADVANCED PART (Not directly implemented for brevity and complexity) ---

    // Staggered Fade/Slide & Bouncing Letters:
    // These would typically involve breaking the `text` string into individual characters or words
    // and animating each one independently. This would conflict heavily with the Typewriter effect
    // if run concurrently.
    // To combine:
    // 1. Typewriter finishes.
    // 2. Then, the fully formed text could have a *brief* secondary animation where letters bounce slightly
    //    into place or stagger-fade in if the typewriter wasn't a hard reveal.
    //    Example for individual letters: A `LazyRow` of `Text` composables, each with its own `graphicsLayer` and `LaunchedEffect` for offset/alpha/scale.

    // Particle Effects:
    // This is significantly more complex and often requires:
    // a) A custom `Canvas` drawing where you manage particle positions, sizes, colors, and lifecycles.
    // b) Using a third-party particle library for Compose (if available).
    // You'd typically have particles emanating from the text as it appears or when it's fully formed.
    // This would likely be a separate Composable placed on top of or behind the text.
}
