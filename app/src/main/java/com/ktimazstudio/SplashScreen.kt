package com.ktimazstudio

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ktimazstudio.ui.theme.ktimaz
import kotlinx.coroutines.delay
import androidx.constraintlayout.compose.MotionLayout
import androidx.constraintlayout.compose.MotionScene
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight

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
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(80.dp)
            )
        }

        AnimatedVisibility(
            visible = startAnimation,
            enter = fadeIn(animationSpec = tween(1200)),
            modifier = Modifier.layoutId("text")
        ) {
            Text(
                text = "Powered by KTiMAZ Studio",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
        }
    }
}
