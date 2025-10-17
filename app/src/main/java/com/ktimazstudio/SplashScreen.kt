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
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        // Ripple Wave Effect
        RippleWaveAnimation()

        // Center Logo
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

        // Loading dots below logo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 170.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AdvancedLoadingDots()
        }

        // Animated "Powered by KTiMAZ Studio" text (merged from old code)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    alpha = poweredByAlpha
                }
            ) {
                val poweredTextColor = if (isSystemInDarkTheme()) 
                    Color.White.copy(alpha = 0.8f) 
                else 
                    Color.Black.copy(alpha = 0.7f)

                    Text(
                        text = "Powered by",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 2.sp,
                        color = poweredTextColor,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                AnimatedGradientText()
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

@Composable
fun AdvancedLoadingDots() {
    val dotCount = 3
    val dotSize = 10.dp
    val animationDuration = 800
    val delayBetweenDots = 200
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

    val isDark = isSystemInDarkTheme()
    val glowColor = if (isDark) Color(0xFFFFD93D) else MaterialTheme.colorScheme.primary
    val textColor = if (isDark) Color.White else Color.Black

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = textPulse
                scaleY = textPulse
            }
    ) {
        // Glow shadow
        Text(
            text = "ImtBytes",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            color = textColor.copy(alpha = 0.25f),
            modifier = Modifier
                .offset(x = 2.dp, y = 2.dp)
                .blur(8.dp)
        )

        // Main glowing text
        Text(
            text = "ImtBytes",
            fontSize = 22.sp,
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
