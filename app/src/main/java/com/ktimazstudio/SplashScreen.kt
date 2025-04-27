package com.ktimazstudio

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay

class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplashScreenContent()
        }
    }
}

@Composable
fun SplashScreenContent() {
    val context = LocalContext.current
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(3500)  // After animation delay, go to MainActivity
        context.startActivity(Intent(context, MainActivity::class.java))
        if (context is ComponentActivity) context.finish()
    }

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.2f else 0f,
        animationSpec = tween(durationMillis = 1200)
    )

    val rotation by animateFloatAsState(
        targetValue = if (startAnimation) 360f else 0f,
        animationSpec = tween(durationMillis = 2000)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(scale)
                    .rotate(rotation)
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

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(animationSpec = tween(durationMillis = 1500))
            ) {
                Text(
                    text = "Powered by KTiMAZ Studio",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
