package com.ktimazstudio

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay

// ---------- THEME WRAPPER ---------- //
@Composable
fun KTiMAZTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography(), content = content)
}

// ---------- MAIN SPLASH ---------- //
class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            KTiMAZTheme {
                SplashScreenPro()
            }
        }
    }
}

@Composable
fun SplashScreenPro() {
    val context = LocalContext.current
    var startAnimation by remember { mutableStateOf(false) }
    val dark = isSystemInDarkTheme()

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(4000)
        context.startActivity(Intent(context, MainActivity::class.java))
        (context as? Activity)?.finish()
    }

    // Animations
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1.1f else 0.6f,
        animationSpec = tween(1000, easing = EaseOutBack)
    )
    val logoRotation by animateFloatAsState(
        targetValue = if (startAnimation) 360f else 0f,
        animationSpec = tween(2500, easing = LinearEasing)
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(800)
    )
    val poweredAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1000, delayMillis = 2500)
    )

    // Background gradient shift
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            tween(6000, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "bgShift"
    )

    val backgroundBrush = Brush.linearGradient(
        colors = if (dark) {
            listOf(Color(0xFF0D0D0D), Color(0xFF1E1E1E), MaterialTheme.colorScheme.primary)
        } else {
            listOf(MaterialTheme.colorScheme.primaryContainer, Color.White, MaterialTheme.colorScheme.primary)
        },
        start = Offset(gradientShift, 0f),
        end = Offset(0f, gradientShift)
    )

    // --- UI Layer --- //
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        // Floating logo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            contentAlignment = Alignment.Center
        ) {
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
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(100.dp)
                )
            }
        }

        // Animated dots loader
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 170.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AdvancedLoadingDots()
        }

        // Brand text
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 28.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer { alpha = poweredAlpha }
            ) {
                val poweredColor = if (dark)
                    Color.White.copy(alpha = 0.8f)
                else
                    Color.Black.copy(alpha = 0.7f)

                Text(
                    text = "Powered by",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp,
                    color = poweredColor,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                AnimatedGradientText()
            }
        }
    }
}

// ---------- ANIMATED LOADING DOTS ---------- //
@Composable
fun AdvancedLoadingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dotCount = 3
    val dotSize = 10.dp
    val animationDuration = 900
    val delayBetween = 150

    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        repeat(dotCount) { i ->
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.6f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    keyframes {
                        durationMillis = animationDuration + delayBetween * dotCount
                        0.6f at i * delayBetween
                        1.2f at (i * delayBetween + animationDuration / 2)
                        0.6f at (i * delayBetween + animationDuration)
                    },
                    RepeatMode.Restart
                ),
                label = "dotScale$i"
            )
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    keyframes {
                        durationMillis = animationDuration + delayBetween * dotCount
                        0.4f at i * delayBetween
                        1f at (i * delayBetween + animationDuration / 2)
                        0.4f at (i * delayBetween + animationDuration)
                    },
                    RepeatMode.Restart
                ),
                label = "dotAlpha$i"
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(dotSize)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = alpha
                    }
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

// ---------- BRAND GLOWING TEXT ---------- //
@Composable
fun AnimatedGradientText() {
    val infiniteTransition = rememberInfiniteTransition(label = "text")
    val shift by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 500f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Reverse),
        label = "shift"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val dark = isSystemInDarkTheme()
    val glowColor = if (dark) Color(0xFFFFD93D) else MaterialTheme.colorScheme.primary
    val textColor = if (dark) Color.White else Color.Black

    Box(Modifier.graphicsLayer { scaleX = pulse; scaleY = pulse }) {
        Text(
            text = "KTiMAZ Studio",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            letterSpacing = 1.sp,
            style = MaterialTheme.typography.titleLarge.copy(
                shadow = Shadow(
                    color = glowColor.copy(alpha = 0.6f),
                    offset = Offset(0f, 0f),
                    blurRadius = 20f
                )
            )
        )
    }
}
