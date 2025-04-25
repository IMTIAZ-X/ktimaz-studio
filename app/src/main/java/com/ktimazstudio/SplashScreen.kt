package com.ktimazstudio

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ktimazstudio.ui.theme.ktimaz
import kotlinx.coroutines.delay

class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ktimaz {
                SplashScreenContent()
            }
        }
    }
}

@Composable
fun SplashScreenContent() {
    val context = LocalContext.current
    var startWhiteAnim by remember { mutableStateOf(false) }
    var startLogoAnim by remember { mutableStateOf(false) }

    val whiteScale by animateFloatAsState(
        targetValue = if (startWhiteAnim) 3f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "whiteScale"
    )

    val logoScale by animateFloatAsState(
        targetValue = if (startLogoAnim) 0.35f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "logoScale"
    )

    LaunchedEffect(true) {
        delay(100)
        startWhiteAnim = true
        delay(1000)
        startLogoAnim = true
        delay(2000)
        context.startActivity(Intent(context, MainActivity::class.java))
        if (context is ComponentActivity) context.finish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .scale(whiteScale)
                .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.extraLarge),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher),
                contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
                    .scale(logoScale)
            )
        }
    }
}
