package com.ktimazstudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.draw.shadow
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

    LaunchedEffect(Unit) {
        cardAnimation.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        shimmerAnimation.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
        buttonPulse.animateTo(
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = PremiumEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1A237E), // Deep Indigo
                        Color(0xFF26A69A) // Vibrant Teal
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
            PremiumGlassCard(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .alpha(cardAnimation.value)
                    .scale(0.92f + cardAnimation.value * 0.08f) // Subtle bounce effect
            ) {
                CustomTitleText(
                    text = "$title Coming Soon",
                    modifier = Modifier.padding(top = 32.dp),
                    shimmerProgress = shimmerAnimation.value
                )
                Spacer(modifier = Modifier.height(20.dp))
                CustomDescriptionText(
                    text = "Crafted with precision, this feature is on its way. Stay tuned for a premium experience!",
                    modifier = Modifier.padding(horizontal = 28.dp),
                    shimmerProgress = shimmerAnimation.value
                )
                Spacer(modifier = Modifier.height(40.dp))
                GradientOutlineButton(
                    text = "Return to Home",
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(bottom = 32.dp)
                        .size(width = 220.dp, height = 52.dp)
                        .scale(buttonPulse.value) // Pulsating effect
                )
            }
        }
    }
}

@Composable
fun PremiumGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f)
                    )
                )
            )
            .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = Color(0xFF26A69A))
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF).copy(alpha = 0.6f),
                        Color(0xFF26A69A).copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(24.dp),
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
                        Color(0xFF1A237E),
                        Color(0xFF1A237E),
                        Color(0xFF26A69A),
                        Color(0xFF1A237E),
                        Color(0xFF1A237E)
                    ),
                    start = androidx.compose.ui.geometry.Offset(shimmerProgress * 1200f, 0f),
                    end = androidx.compose.ui.geometry.Offset(shimmerProgress * 1200f + 250f, 0f)
                ),
                alpha = 0.25f
            )
            .padding(8.dp)
    ) {
        Text(
            text = text,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, ambientColor = Color(0xFF26A69A))
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
                        Color(0xFFFFFFFF).copy(alpha = 0.4f),
                        Color(0xFFFFFFFF).copy(alpha = 0.4f),
                        Color(0xFF26A69A).copy(alpha = 0.6f),
                        Color(0xFFFFFFFF).copy(alpha = 0.4f),
                        Color(0xFFFFFFFF).copy(alpha = 0.4f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(shimmerProgress * 1000f, 0f),
                    end = androidx.compose.ui.geometry.Offset(shimmerProgress * 1000f + 200f, 0f)
                ),
                alpha = 0.25f
            )
            .padding(8.dp)
    ) {
        Text(
            text = text,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.95f),
            textAlign = TextAlign.Center,
            lineHeight = 26.sp,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, ambientColor = Color(0xFF26A69A))
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
            .clip(RoundedCornerShape(28.dp))
            .background(Color.Transparent)
            .border(
                width = 2.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1A237E),
                        Color(0xFF26A69A),
                        Color(0xFFFFFFFF).copy(alpha = 0.7f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color(0xFF26A69A), bounded = true)
            ) { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

object PremiumEasing : Easing {
    override fun transform(fraction: Float): Float {
        return (Math.sin(fraction * Math.PI * 1.5).toFloat() * 0.4f + 0.6f) * fraction
    }
}
