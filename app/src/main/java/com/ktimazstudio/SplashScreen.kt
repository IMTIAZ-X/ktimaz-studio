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
import androidx.compose.foundation.shape.RoundedCornerShape
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
    ) { if (it) 1.2f else 0.5f }

    val logoRotation by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 2000, easing = FastOutSlowInEasing) },
        label = "logoRotation"
    ) { if (it) 720f else 0f }

    val logoAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 800, delayMillis = 200) },
        label = "logoAlpha"
    ) { if (it) 1f else 0f }

    val textAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 1000, delayMillis = 2500) },
        label = "textAlpha"
    ) { if (it) 1f else 0f }

    val textScale by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 800, delayMillis = 2500, easing = EaseOutBack) },
        label = "textScale"
    ) { if (it) 1f else 0.5f }

    val textOffsetY by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 900, delayMillis = 2500, easing = EaseOutCubic) },
        label = "textOffsetY"
    ) { if (it) 0f else 50f }

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

        // Logo
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

        // Futuristic loading indicator (from old version)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 170.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            FuturisticLoadingIndicator()
        }

        // Animated "Powered by KTiMAZ Studio" text (from old version)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 40.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    alpha = textAlpha
                    scaleX = textScale
                    scaleY = textScale
                    translationY = textOffsetY
                }
            ) {
                Text(
                    text = "Powered by",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                AnimatedGradientText()
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Ripple background effect
// -----------------------------------------------------------------------------
@Composable
fun RippleWaveAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "RippleWaveAnimation")
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
                .background(Color.White.copy(alpha = 0.08f))
        )
    }
}

// -----------------------------------------------------------------------------
// Old-style loading dots & bar (kept from old FuturisticLoadingIndicator)
// -----------------------------------------------------------------------------
@Composable
fun FuturisticLoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "LoadingIndicator")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loadingProgress"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Animated dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                val dotScale by infiniteTransition.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 1200
                            0.6f at 0
                            1.2f at (index * 150 + 300) with FastOutSlowInEasing
                            0.6f at (index * 150 + 600) with FastOutSlowInEasing
                            0.6f at 1200
                        },
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "dot_$index"
                )

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .graphicsLayer {
                            scaleX = dotScale
                            scaleY = dotScale
                        }
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                        .shadow(4.dp, CircleShape, ambientColor = Color.White)
                )
            }
        }

        // Progress bar
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.6f),
                                Color.White,
                                Color.White.copy(alpha = 0.6f)
                            )
                        )
                    )
                    .shadow(4.dp, ambientColor = Color.White)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// Animated gradient & pulse text (from old AnimatedGradientText)
// -----------------------------------------------------------------------------
@Composable
fun AnimatedGradientText() {
    val infiniteTransition = rememberInfiniteTransition(label = "TextGradient")

    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "textGradientShift"
    )

    val textPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "textPulse"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = textPulse
                scaleY = textPulse
            }
    ) {
        // Shadow/glow layer
        Text(
            text = "KTiMAZ Studio",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            color = Color.White.copy(alpha = 0.3f),
            modifier = Modifier
                .offset(x = 2.dp, y = 2.dp)
                .blur(8.dp)
        )

        // Main glowing text
        Text(
            text = "KTiMAZ Studio",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            color = Color.White,
            style = MaterialTheme.typography.titleLarge.copy(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color(0xFFFFD93D).copy(alpha = 0.5f),
                    blurRadius = 20f
                )
            )
        )
    }
}
