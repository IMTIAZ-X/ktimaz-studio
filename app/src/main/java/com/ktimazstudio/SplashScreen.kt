package com.ktimazstudio

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.constraintlayout.compose.MotionLayout
import androidx.constraintlayout.compose.MotionScene
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
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(3500)
        context.startActivity(Intent(context, MainActivity::class.java))
        if (context is ComponentActivity) context.finish()
    }

    val motionScene = remember {
        """
        {
          ConstraintSets: {
            start: {
              logo: {
                width: 150,
                height: 150,
                start: ['parent', 'start', 0],
                end: ['parent', 'end', 0],
                top: ['parent', 'top', 200]
              },
              text: {
                width: 'wrap',
                height: 'wrap',
                start: ['parent', 'start', 0],
                end: ['parent', 'end', 0],
                top: ['logo', 'bottom', 16]
              }
            },
            end: {
              logo: {
                width: 100,
                height: 100,
                start: ['parent', 'start', 0],
                end: ['parent', 'end', 0],
                top: ['parent', 'top', 100]
              },
              text: {
                width: 'wrap',
                height: 'wrap',
                start: ['parent', 'start', 0],
                end: ['parent', 'end', 0],
                top: ['logo', 'bottom', 8]
              }
            }
          },
          Transitions: {
            default: {
              from: 'start',
              to: 'end',
              pathMotionArc: 'startVertical',
              KeyFrames: {
                KeyAttributes: [
                  {
                    target: ['logo'],
                    frames: [50],
                    scaleX: [1.2],
                    scaleY: [1.2]
                  }
                ]
              }
            }
          }
        }
        """.trimIndent()
    }

    MotionLayout(
        motionScene = MotionScene(content = motionScene),
        progress = if (startAnimation) 1f else 0f,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .layoutId("logo")
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            // 3D Effect: Add slight scale and rotate for 3D look
            val scale by animateFloatAsState(
                targetValue = if (startAnimation) 1.1f else 0f,
                animationSpec = tween(1000)
            )

            Image(
                painter = painterResource(id = R.mipmap.ic_launcher),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(150.dp)
                    .scale(scale)
                    .rotate(15f)  // slight rotation for 3D effect
            )
        }

        AnimatedVisibility(
            visible = startAnimation,
            enter = fadeIn(animationSpec = tween(1200)),
            modifier = Modifier.layoutId("text")
        ) {
            Text(
                text = "Powered by KTiMAZ Studio",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
        }
    }
}
