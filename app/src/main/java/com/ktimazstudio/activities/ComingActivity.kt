package com.ktimazstudio.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ktimazstudio.ui.theme.ktimaz

class ComingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val cardTitle = intent.getStringExtra("CARD_TITLE") ?: "Unknown Module"
        setContent {
            ktimaz {
                ComingSoonScreen(cardTitle) {
                    finish()
                }
            }
        }
    }
}

@Composable
fun ComingSoonScreen(title: String, onBackClick: () -> Unit) {
    val cardAnimation = remember { Animatable(0f) }
    val shimmerAnimation = remember { Animatable(0f) }
    val buttonPulse = remember { Animatable(1f) }
    val clickAnimation = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        cardAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = LuxeEasing)
        )
        shimmerAnimation.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
        buttonPulse.animateTo(
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LuxeEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0288D1), // Deep Blue
                        Color(0xFF80DEEA) // Cyan
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .alpha(cardAnimation.value)
                    .scale(0.9f + cardAnimation.value * 0.1f) // Smooth scale-in
            ) {
                TitleText(
                    text = "$title Coming Soon",
                    modifier = Modifier.padding(top = 28.dp),
                    shimmerProgress = shimmerAnimation.value
                )
                Spacer(modifier = Modifier.height(20.dp))
                DescriptionText(
                    text = "We're crafting something extraordinary. Stay tuned for a world-class experience!",
                    modifier = Modifier.padding(horizontal = 24.dp),
                    shimmerProgress = shimmerAnimation.value
                )
                Spacer(modifier = Modifier.height(36.dp))
                OutlineButton(
                    text = "Back to Home",
                    onClick = {
                        LaunchedEffect(Unit) {
                            clickAnimation.animateTo(
                                targetValue = 0.95f,
                                animationSpec = tween(durationMillis = 150)
                            )
                            clickAnimation.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(durationMillis = 150)
                            )
                            onBackClick()
                        }
                    },
                    modifier = Modifier
                        .padding(bottom = 28.dp)
                        .size(width = 240.dp, height = 54.dp)
                        .scale(buttonPulse.value)
                )
            }
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.18f),
                        Color.White.copy(alpha = 0.08f),
                        Color(0xFF80DEEA).copy(alpha = 0.1f)
                    )
                )
            )
            .border(
                width = 1.8.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF).copy(alpha = 0.7f),
                        Color(0xFF80DEEA).copy(alpha = 0.5f),
                        Color(0xFF0288D1).copy(alpha = 0.5f)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(22.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}

@Composable
fun TitleText(
    text: String,
    modifier: Modifier = Modifier,
    shimmerProgress: Float
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0288D1).copy(alpha = 0.3f),
                        Color(0xFF0288D1).copy(alpha = 0.3f),
                        Color(0xFF80DEEA).copy(alpha = 0.6f),
                        Color(0xFF0288D1).copy(alpha = 0.3f),
                        Color(0xFF0288D1).copy(alpha = 0.3f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(shimmerProgress * 1000f, 0f),
                    end = androidx.compose.ui.geometry.Offset(shimmerProgress * 1000f + 300f, 0f)
                ),
                alpha = 0.3f
            )
            .padding(6.dp)
    ) {
        Text(
            text = text,
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DescriptionText(
    text: String,
    modifier: Modifier = Modifier,
    shimmerProgress: Float
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF).copy(alpha = 0.35f),
                        Color(0xFFFFFFFF).copy(alpha = 0.35f),
                        Color(0xFF80DEEA).copy(alpha = 0.65f),
                        Color(0xFFFFFFFF).copy(alpha = 0.35f),
                        Color(0xFFFFFFFF).copy(alpha = 0.35f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(shimmerProgress * 800f, 0f),
                    end = androidx.compose.ui.geometry.Offset(shimmerProgress * 800f + 250f, 0f)
                ),
                alpha = 0.3f
            )
            .padding(6.dp)
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.92f),
            textAlign = TextAlign.Center,
            lineHeight = 28.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .background(Color.Transparent)
            .border(
                width = 2.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0288D1),
                        Color(0xFF80DEEA),
                        Color(0xFFFFFFFF).copy(alpha = 0.8f)
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

object LuxeEasing : Easing {
    override fun transform(fraction: Float): Float {
        return (Math.sin(fraction * Math.PI * 1.8).toFloat() * 0.45f + 0.55f) * fraction
    }
}
