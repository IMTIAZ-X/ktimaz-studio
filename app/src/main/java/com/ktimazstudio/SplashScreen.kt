package com.ktimazstudio

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColor
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

// --- FUTURISTIC COLORS ---
private val NeonCyan = Color(0xFF00FFFF)
private val ElectricPurple = Color(0xFFB500FF)
private val DarkBackground = Color(0xFF0A0A0A)
private val PoweredByBlack = Color(0xFF000000)

class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            SplashScreenContent()
        }
    }
}

/**
 * Main Composable for the Splash Screen with Unique Style.
 */
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground) // Solid Deep Black
    ) {
        // 1. Ripple Wave Effect (Now Neon Cyan)
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
                    .size(250.dp)
                    .clip(CircleShape)
                    .background(ElectricPurple.copy(alpha = 0.15f))
                    .blur(40.dp)
            )

            // Logo Container with Animations (Inner background is Black)
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
                    .background(Color.Black), // Logo Container is Black
                contentAlignment = Alignment.Center
            ) {
                // Actual Logo Image
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(120.dp)
                )
            }
        }

        // 3. Loading dots below logo (Now Neon Cyan)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 170.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AdvancedLoadingDots(dotColor = NeonCyan)
        }

        // 4. Animated "Powered by KTiMAZ Studio" text (with gradient and black text)
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
                // Unique Gradient Area for Text Background (Gradient under KTiMAZ)
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(ElectricPurple.copy(alpha = 0.1f), NeonCyan.copy(alpha = 0.1f))
                            )
                        )
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Requested: "Powered by" text in Black color
                        Text(
                            text = "Powered by",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 2.sp,
                            color = PoweredByBlack, // Forced Black Text
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        // KTiMAZ Studio Text (Neon)
                        AnimatedGradientText(
                            glowColor1 = ElectricPurple,
                            glowColor2 = NeonCyan
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------
// SUB-COMPOSABLES FOR ANIMATION EFFECTS
// ----------------------------------------------------------------------

@Composable
fun RippleWaveAnimation(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "RippleWaveAnimation")
    val rippleRadius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 500f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
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
                .background(color.copy(alpha = 0.1f))
        )
    }
}

@Composable
fun AdvancedLoadingDots(dotColor: Color) {
    val dotCount = 3
    val dotSize = 12.dp
    val singleDotDuration = 400
    val delayBetweenDots = 200
    val totalDuration = singleDotDuration * 2 + delayBetweenDots * (dotCount - 1)

    val infiniteTransition = rememberInfiniteTransition(label = "LoadingDotsInfiniteTransition")

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(dotCount) { index ->
            val startDelay = index * delayBetweenDots

            val dotScale by infiniteTransition.animateFloat(
                initialValue = 0.6f,
                targetValue = 0.6f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = totalDuration
                        0.6f at startDelay with LinearEasing
                        1.4f at startDelay + singleDotDuration / 2 with LinearEasing
                        0.6f at startDelay + singleDotDuration with LinearEasing
                        0.6f at durationMillis
                    },
                    repeatMode = RepeatMode.Restart
                ),
                label = "dotScale_$index"
            )

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
                    .background(dotColor)
            )
        }
    }
}

/**
 * Creates a pulsing, dual-color glowing text effect.
 * FIX: Removed the 'by' delegate to directly store the State<Color> object, resolving the delegate type mismatch error.
 */
@Composable
fun AnimatedGradientText(glowColor1: Color, glowColor2: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "TextGlowPulse")

    // Text Pulse Animation
    val textPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "textPulse"
    )

    // 🛑 CRITICAL FIX: Store the State<Color> object directly.
    // We remove 'by' and the explicit type annotation (State<Color>) to satisfy the delegate logic.
    // The type is correctly inferred as State<Color> here, and we access the value via .value.
    val animatedGlowColor = infiniteTransition.animateColor(
        initialValue = glowColor1,
        targetValue = glowColor2,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "animatedGlowColor"
    )

    val textColor = Color.White.copy(alpha = 0.9f)

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
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            color = DarkBackground,
            modifier = Modifier
                .offset(x = 0.dp, y = 0.dp)
                .blur(12.dp)
                .drawBehind {
                    // Access the value using .value (Since animatedGlowColor is a State<Color>)
                    drawRect(
                        color = animatedGlowColor.value.copy(alpha = 0.8f),
                        blendMode = androidx.compose.ui.graphics.BlendMode.Screen
                    )
                }
        )

        // 2. Main glowing text
        Text(
            text = "KTiMAZ Studio",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            color = textColor,
            style = MaterialTheme.typography.titleLarge.copy(
                shadow = androidx.compose.ui.graphics.Shadow(
                    // Access the value using .value
                    color = animatedGlowColor.value.copy(alpha = 1f),
                    offset = androidx.compose.ui.geometry.Offset(0f, 0f),
                    blurRadius = 15f
                )
            )
        )
    }
}