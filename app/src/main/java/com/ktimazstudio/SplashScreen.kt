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
import com.ktimazstudio.ui.theme.Purple80
import com.ktimazstudio.ui.theme.PurpleGrey80
import com.ktimazstudio.ui.theme.Purple40
import com.ktimazstudio.ui.theme.Pink40
import com.ktimazstudio.ui.theme.Pink80

class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            SplashScreenContent()
        }
    }
}

@Composable
fun SplashScreenContent() {
    val context = LocalContext.current
    val splashState = remember { MutableTransitionState(false) }

    LaunchedEffect(Unit) {
        splashState.targetState = true
        delay(4000)
        context.startActivity(Intent(context, MainActivity::class.java))
        if (context is ComponentActivity) context.finish()
    }

    val transition = updateTransition(splashState, label = "SplashTransition")

    val logoScale by transition.animateFloat(
        transitionSpec = { tween(1000, easing = EaseOutBounce) },
        label = "logoScale"
    ) { if (it) 1.2f else 0.5f }

    val logoRotation by transition.animateFloat(
        transitionSpec = { tween(2000, easing = FastOutSlowInEasing) },
        label = "logoRotation"
    ) { if (it) 720f else 0f }

    val logoAlpha by transition.animateFloat(
        transitionSpec = { tween(800, delayMillis = 200) },
        label = "logoAlpha"
    ) { if (it) 1f else 0f }

    val poweredByAlpha by transition.animateFloat(
        transitionSpec = { tween(800, delayMillis = 2500) },
        label = "poweredByAlpha"
    ) { if (it) 1f else 0f }

    // --- Layout ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Purple80, PurpleGrey80)
                )
            )
    ) {
        RippleWaveAnimation()

        // Logo + Blur Halo
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
                    .background(Purple40),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(120.dp)
                )
            }
        }

        // Loading Dots
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 170.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AdvancedLoadingDots()
        }

        // "Powered by" Text
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
                Text(
                    text = "Powered by",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp,
                    color = Color.Black, // fixed black color
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                AnimatedGradientText()
            }
        }
    }
}

@Composable
fun RippleWaveAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "RippleWave")
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
    val singleDotDuration = 400
    val delayBetweenDots = 200
    val totalDuration = singleDotDuration * 2 + delayBetweenDots * (dotCount - 1)
    val infiniteTransition = rememberInfiniteTransition(label = "LoadingDots")

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(dotCount) { index ->
            val startDelay = index * delayBetweenDots

            val dotScale by infiniteTransition.animateFloat(
                initialValue = 0.7f,
                targetValue = 0.7f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = totalDuration
                        0.7f at startDelay
                        1.2f at startDelay + singleDotDuration / 2
                        0.7f at startDelay + singleDotDuration
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
                        durationMillis = totalDuration
                        0.5f at startDelay
                        1f at startDelay + singleDotDuration / 2
                        0.5f at startDelay + singleDotDuration
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
                    .background(Pink40)
            )
        }
    }
}

@Composable
fun AnimatedGradientText() {
    val infiniteTransition = rememberInfiniteTransition(label = "TextPulse")

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
        modifier = Modifier.graphicsLayer {
            scaleX = textPulse
            scaleY = textPulse
        }
    ) {
        Text(
            text = "KTiMAZ Studio",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            color = Color.Black.copy(alpha = 0.25f),
            modifier = Modifier
                .offset(x = 2.dp, y = 2.dp)
                .blur(8.dp)
        )

        Text(
            text = "KTiMAZ Studio",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            color = Color.Black,
            shadow = androidx.compose.ui.graphics.Shadow(
                color = Pink80.copy(alpha = 0.6f),
                offset = androidx.compose.ui.geometry.Offset(0f, 0f),
                blurRadius = 20f
            )
        )
    }
}
