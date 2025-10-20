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

// Assuming MainActivity exists in the same package or is properly imported/declared
// You must have a resource R.mipmap.ic_launcher and a MainActivity::class.java defined

class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            // NOTE: MaterialTheme is omitted here, assuming it's applied outside this
            // or should be wrapped around the SplashScreen call if needed.
            SplashScreenContent()
        }
    }
}

/**
 * Main Composable for the Splash Screen.
 */
@Composable
fun SplashScreenContent() {
    val context = LocalContext.current
    // MutableTransitionState is used to drive the initial logo/text animations
    val splashScreenState = remember { MutableTransitionState(false) }

    // Navigation logic: starts the animations, waits 4 seconds, then navigates to MainActivity
    LaunchedEffect(Unit) {
        splashScreenState.targetState = true // Start initial animations
        delay(4000) // Wait for 4 seconds (time for logo and text animations to play out)
        context.startActivity(Intent(context, MainActivity::class.java))
        if (context is ComponentActivity) context.finish() // Finish the splash activity
    }

    val transition = updateTransition(splashScreenState, label = "SplashScreenTransition")

    // --- Logo Animations ---

    // 1. Logo Scale (starts small, bounces up)
    val logoScale by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 1000, easing = EaseOutBounce) },
        label = "logoScale"
    ) { state ->
        if (state) 1.2f else 0.5f
    }

    // 2. Logo Rotation (720 degrees spin)
    val logoRotation by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 2000, easing = FastOutSlowInEasing) },
        label = "logoRotation"
    ) { state ->
        if (state) 720f else 0f
    }

    // 3. Logo Alpha (fade-in)
    val logoAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 800, delayMillis = 200) },
        label = "logoAlpha"
    ) { state ->
        if (state) 1f else 0f
    }

    // --- "Powered By" Text Animation ---

    // Alpha for the "Powered by KTiMAZ Studio" text (appears later)
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
        // 1. Ripple Wave Effect (Subtle background animation)
        RippleWaveAnimation()

        // 2. Center Logo and Blur Halo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            contentAlignment = Alignment.Center
        ) {
            // Blurred Halo Effect
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .blur(30.dp)
            )

            // Logo Container with Animations (Scale, Rotation, Alpha)
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
                // Actual Logo Image (Ensure R.mipmap.ic_launcher exists)
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(120.dp)
                )
            }
        }

        // 3. Loading dots below logo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 170.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AdvancedLoadingDots()
        }

        // 4. Animated "Powered by KTiMAZ Studio" text
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    alpha = poweredByAlpha // Driven by the main transition
                }
            ) {
                val poweredTextColor = if (isSystemInDarkTheme())
                    Color.White.copy(alpha = 5.8f)
                else
                    Color.Black.copy(alpha = 5.7f)

                Text(
                    text = "Powered by",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp,
                    color = Color.Black,
                   // color = poweredTextColor,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                AnimatedGradientText()
            }
        }
    }
}

// ----------------------------------------------------------------------
// SUB-COMPOSABLES FOR ANIMATION EFFECTS
// ----------------------------------------------------------------------

/**
 * Creates a subtle, continuously expanding wave effect centered below the logo.
 */
@Composable
fun RippleWaveAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "RippleWaveAnimation")
    // The radius expands from 0f to 400f over 3 seconds
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
                // Use low opacity white for a faint, ethereal ripple
                .background(Color.White.copy(alpha = 0.08f))
        )
    }
}

/**
 * Creates a modern, staggered three-dot loading animation.
 * FIX: Adjusted keyframe timing for a perfect, infinite loop.
 */
@Composable
fun AdvancedLoadingDots() {
    val dotCount = 3
    val dotSize = 10.dp
    val singleDotDuration = 400 // Time for one dot to scale up and down
    val delayBetweenDots = 200 // Stagger delay
    // Total duration for one full cycle (400ms * 2 + 200ms * (3-1)) = 1200ms
    val totalDuration = singleDotDuration * 2 + delayBetweenDots * (dotCount - 1)

    val infiniteTransition = rememberInfiniteTransition(label = "LoadingDotsInfiniteTransition")

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp), // Use spacedBy for cleaner padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(dotCount) { index ->
            val startDelay = index * delayBetweenDots // 0ms, 200ms, 400ms

            // Dot Scale Animation
            val dotScale by infiniteTransition.animateFloat(
                initialValue = 0.7f,
                targetValue = 0.7f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = totalDuration
                        // 1. Initial State (Start of stagger)
                        0.7f at startDelay with LinearEasing
                        // 2. Peak (Scale Up)
                        1.2f at startDelay + singleDotDuration / 2 with LinearEasing
                        // 3. Return (Scale Down)
                        0.7f at startDelay + singleDotDuration with LinearEasing
                        // 4. Hold/Wait until the end of the full cycle (ensures a smooth loop)
                        0.7f at durationMillis
                    },
                    repeatMode = RepeatMode.Restart
                ),
                label = "dotScale_$index"
            )

            // Dot Alpha Animation (synchronised with scale)
            val dotAlpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 0.5f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = totalDuration
                        // 1. Initial State
                        0.5f at startDelay with LinearEasing
                        // 2. Peak (Alpha Up)
                        1f at startDelay + singleDotDuration / 2 with LinearEasing
                        // 3. Return (Alpha Down)
                        0.5f at startDelay + singleDotDuration with LinearEasing
                        // 4. Hold/Wait
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

/**
 * Creates a pulsing, glowing text effect with a fixed studio name.
 * FIX: Replaced "example" with "KTiMAZ Studio".
 */
@Composable
fun AnimatedGradientText() {
    val infiniteTransition = rememberInfiniteTransition(label = "TextGradient")

    // Text Pulse Animation (subtle breathing effect)
    val textPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "textPulse"
    )

//    val isDark = isSystemInDarkTheme()
//    val glowColor = if (isDark) Color(0xFFD0BCFF) else MaterialTheme.colorScheme.primary
//    val textColor = if (isDark) Color.White else Color.Black

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = textPulse
                scaleY = textPulse
            }
    ) {
        // 1. Glow shadow (for depth/diffusion)
        Text(
            text = "IMTBYTES", // FIXED: Placeholder replaced
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            color = textColor.copy(alpha = 0.25f),
            modifier = Modifier
                .offset(x = 2.dp, y = 2.dp)
                .blur(8.dp)
        )

        // 2. Main glowing text (with text style shadow for a crisp glow)
        Text(
            text = "IMTBYTES", // FIXED: Placeholder replaced
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            color = textColor,
            style = MaterialTheme.typography.titleLarge.copy(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = glowColor.copy(alpha = 0.6f),
                    offset = androidx.compose.ui.geometry.Offset(0f, 0f),
                    blurRadius = 20f
                )
            )
        )
    }
}