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

// --- NEW FUTURISTIC COLORS ---
private val NeonCyan = Color(0xFF00FFFF)
private val ElectricPurple = Color(0xFFB500FF)
private val DarkBackground = Color(0xFF0A0A0A)
private val DarkAccent = Color(0xFF202020)

class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            // In a real app, you would wrap this with your MaterialTheme
            SplashScreenContent()
        }
    }
}

/**
 * Main Composable for the Advanced Futuristic Splash Screen.
 */
@Composable
fun SplashScreenContent() {
    val context = LocalContext.current
    val splashScreenState = remember { MutableTransitionState(false) }

    // Navigation logic
    LaunchedEffect(Unit) {
        splashScreenState.targetState = true // Start initial animations
        delay(4000)
        context.startActivity(Intent(context, MainActivity::class.java))
        if (context is ComponentActivity) context.finish()
    }

    val transition = updateTransition(splashScreenState, label = "SplashScreenTransition")

    // Logo Animations (Kept, but fine-tuned for the futuristic feel)
    val logoScale by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 1000, easing = EaseOutBounce) },
        label = "logoScale"
    ) { state -> if (state) 1.2f else 0.5f }

    val logoRotation by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 2000, easing = FastOutSlowInEasing) },
        label = "logoRotation"
    ) { state -> if (state) 720f else 0f }

    val logoAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 800, delayMillis = 200) },
        label = "logoAlpha"
    ) { state -> if (state) 1f else 0f }

    val poweredByAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 800, delayMillis = 2500) },
        label = "poweredByAlpha"
    ) { state -> if (state) 1f else 0f }

    // --- "Ganatire" Inspired Background Gradient ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground, // Deep Black at the top
                        DarkAccent,     // Dark Grey/Accent below
                        DarkBackground, // Deep Black at the bottom
                    )
                )
            )
    ) {
        // 1. Ripple Wave Effect (Now Neon)
        RippleWaveAnimation(color = NeonCyan.copy(alpha = 0.5f))

        // 2. Center Logo and Blur Halo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            contentAlignment = Alignment.Center
        ) {
            // Futuristic Halo Effect (Neon Purple Blur)
            Box(
                modifier = Modifier
                    .size(250.dp) // Made slightly larger
                    .clip(CircleShape)
                    .background(ElectricPurple.copy(alpha = 0.15f))
                    .blur(40.dp) // Stronger blur
            )

            // Logo Container with Animations
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
                    // Inner background is DarkAccent for a black/dark look
                    .background(DarkAccent),
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

        // 3. Loading dots below logo (Now Neon)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 170.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AdvancedLoadingDots(dotColor = NeonCyan)
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
                modifier = Modifier.graphicsLayer { alpha = poweredByAlpha }
            ) {
                // Text color is a subtle white/grey against the dark background
                val poweredTextColor = Color.White.copy(alpha = 0.7f)

                Text(
                    text = "Powered by",
                    fontSize = 12.sp, // Slightly larger
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp,
                    color = poweredTextColor,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                AnimatedGradientText(
                    glowColor1 = ElectricPurple, // Primary neon glow
                    glowColor2 = NeonCyan        // Secondary neon glow
                )
            }
        }
    }
}

// ----------------------------------------------------------------------
// SUB-COMPOSABLES FOR ANIMATION EFFECTS (Updated for Dark/Neon)
// ----------------------------------------------------------------------

/**
 * Creates a subtle, continuously expanding wave effect centered below the logo.
 */
@Composable
fun RippleWaveAnimation(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "RippleWaveAnimation")
    val rippleRadius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 500f, // Made larger for a wider, more dramatic effect
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing), // Slower, wider pulse
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
                .background(color.copy(alpha = 0.1f)) // Use the passed neon color
        )
    }
}

/**
 * Creates a modern, staggered three-dot loading animation.
 */
@Composable
fun AdvancedLoadingDots(dotColor: Color) {
    val dotCount = 3
    val dotSize = 12.dp // Slightly larger dots
    val singleDotDuration = 400
    val delayBetweenDots = 200
    val totalDuration = singleDotDuration * 2 + delayBetweenDots * (dotCount - 1)

    val infiniteTransition = rememberInfiniteTransition(label = "LoadingDotsInfiniteTransition")

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp), // More spacing
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(dotCount) { index ->
            val startDelay = index * delayBetweenDots

            // Dot Scale Animation
            val dotScale by infiniteTransition.animateFloat(
                initialValue = 0.6f, // Starts smaller
                targetValue = 0.6f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = totalDuration
                        0.6f at startDelay with LinearEasing
                        1.4f at startDelay + singleDotDuration / 2 with LinearEasing // Larger peak
                        0.6f at startDelay + singleDotDuration with LinearEasing
                        0.6f at durationMillis
                    },
                    repeatMode = RepeatMode.Restart
                ),
                label = "dotScale_$index"
            )

            // Dot Alpha Animation
            val dotAlpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 0.4f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = totalDuration
                        0.4f at startDelay with LinearEasing
                        1f at startDelay + singleDotDuration / 2 with LinearEasing
                        0.4f at startDelay + singleDotDuration with LinearEasing
                        0.4f at durationMillis
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
                    .background(dotColor) // Use the neon dot color
            )
        }
    }
}

/**
 * Creates a pulsing, glowing text effect with dual neon glow.
 */
@Composable
fun AnimatedGradientText(glowColor1: Color, glowColor2: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "TextGlowPulse")

    // Text Pulse Animation
    val textPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing), // Faster pulse
            repeatMode = RepeatMode.Reverse
        ),
        label = "textPulse"
    )

    // Animated glow color shift for a more dynamic neon look
    val animatedGlowColor by infiniteTransition.animateColor(
        initialValue = glowColor1,
        targetValue = glowColor2,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "animatedGlowColor"
    )

    // Text color is White/Light Grey against the dark background
    val textColor = Color.White

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = textPulse
                scaleY = textPulse
            }
    ) {
        // 1. Shadow for deep diffusion/secondary glow (Purple)
        Text(
            text = "KTiMAZ Studio",
            fontSize = 26.sp, // Larger text
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp, // Wider spacing
            color = DarkBackground, // Shadow color blends with background (hidden text effect)
            modifier = Modifier
                .offset(x = 0.dp, y = 0.dp) // Centered shadow
                .blur(12.dp) // Intense blur
                .drawBehind {
                    drawRect(
                        color = animatedGlowColor.copy(alpha = 0.8f),
                        blendMode = androidx.compose.ui.graphics.BlendMode.Screen // Lightens the effect
                    )
                }
        )

        // 2. Main glowing text (with text style shadow for crisp edges)
        Text(
            text = "KTiMAZ Studio",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            color = textColor,
            style = MaterialTheme.typography.titleLarge.copy(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = animatedGlowColor.copy(alpha = 1f), // Use the animated color for the glow
                    offset = androidx.compose.ui.geometry.Offset(0f, 0f),
                    blurRadius = 15f
                )
            )
        )
    }
}
