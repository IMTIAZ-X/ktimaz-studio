package com.ktimazstudio

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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

    LaunchedEffect(Unit) {
        cardAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = LiquidEasing)
        )
        shimmerAnimation.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    Scaffold(
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .alpha(cardAnimation.value)
                    .scale(0.9f + cardAnimation.value * 0.1f) // Subtle liquid scale effect
            ) {
                CustomTitleText(
                    text = "$title Coming Soon",
                    modifier = Modifier.padding(top = 24.dp),
                    shimmerProgress = shimmerAnimation.value
                )
                Spacer(modifier = Modifier.height(16.dp))
                CustomDescriptionText(
                    text = "This feature is in active development. Get ready for a seamless experience—stay tuned!",
                    modifier = Modifier.padding(horizontal = 24.dp),
                    shimmerProgress = shimmerAnimation.value
                )
                Spacer(modifier = Modifier.height(32.dp))
                GradientOutlineButton(
                    text = "Back to Home",
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .size(width = 200.dp, height = 48.dp)
                        .scale(0.95f + cardAnimation.value * 0.05f) // Subtle button scale
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
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.2f),
                        Color.White.copy(alpha = 0.1f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF).copy(alpha = 0.5f),
                        Color(0xFF80DEEA).copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp),
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
fun CustomTitleText(
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
                        Color(0xFF0288D1),
                        Color(0xFF0288D1),
                        Color(0xFF80DEEA),
                        Color(0xFF0288D1),
                        Color(0xFF0288D1)
                    ),
                    start = androidx.compose.ui.geometry.Offset(shimmerProgress * 1000f, 0f),
                    end = androidx.compose.ui.geometry.Offset(shimmerProgress * 1000f + 200f, 0f)
                ),
                alpha = 0.2f
            )
            .padding(4.dp)
    ) {
        Text(
            text = text,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun CustomDescriptionText(
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
                        Color(0xFFFFFFFF).copy(alpha = 0.3f),
                        Color(0xFFFFFFFF).copy(alpha = 0.3f),
                        Color(0xFF80DEEA).copy(alpha = 0.5f),
                        Color(0xFFFFFFFF).copy(alpha = 0.3f),
                        Color(0xFFFFFFFF).copy(alpha = 0.3f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(shimmerProgress * 800f, 0f),
                    end = androidx.compose.ui.geometry.Offset(shimmerProgress * 800f + 150f, 0f)
                ),
                alpha = 0.2f
            )
            .padding(4.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun GradientOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Transparent)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0288D1),
                        Color(0xFF80DEEA)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color(0xFF80DEEA))
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

object LiquidEasing : Easing {
    override fun transform(fraction: Float): Float {
        return (Math.sin(fraction * Math.PI * 2).toFloat() * 0.5f + 0.5f) * fraction
    }
}
