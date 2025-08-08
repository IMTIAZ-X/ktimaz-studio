package com.ktimazstudio

import android.content.Context
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ktimazstudio.ui.theme.ktimaz
import kotlinx.coroutines.delay

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
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val theme = sharedPrefs.getString("theme_setting_key", "default") ?: "default"
    val primaryColor = if (theme == "dark") Color(0xFF0277BD) else Color(0xFF01579B)
    val secondaryColor = if (theme == "dark") Color(0xFF4FC3F7) else Color(0xFF4DD0E1)

    val cardAnimation = remember { Animatable(0f) }
    val shimmerAnimation = remember { Animatable(0f) }
    val buttonPulse = remember { Animatable(1f) }
    val clickAnimation = remember { Animatable(1f) }
    var isClicked by remember { mutableStateOf(false) }

    LaunchedEffect(isClicked) {
        if (isClicked) {
            clickAnimation.animateTo(
                targetValue = 0.88f,
                animationSpec = tween(durationMillis = 250)
            )
            clickAnimation.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 250)
            )
            isClicked = false
            onBackClick()
        }
    }

    LaunchedEffect(Unit) {
        delay(300) // Stagger card animation
        cardAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1400, easing = LuxeEasing)
        )
        shimmerAnimation.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
        delay(600) // Stagger button animation
        buttonPulse.animateTo(
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = LuxeEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(primaryColor, secondaryColor)
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
                    .fillMaxWidth(0.92f)
                    .alpha(cardAnimation.value)
                    .scale(0.8f + cardAnimation.value * 0.2f)
                    .offset(y = (-10f + cardAnimation.value * 10f).dp), // Floating effect
                primaryColor = primaryColor,
                secondaryColor = secondaryColor
            ) {
                TitleText(
                    text = "$title Coming Soon",
                    modifier = Modifier.padding(top = 36.dp),
                    shimmerProgress = shimmerAnimation.value,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor
                )
                Spacer(modifier = Modifier.height(28.dp))
                DescriptionText(
                    text = "Crafted with precision, this feature is on its way. Expect a world-class experience soon!",
                    modifier = Modifier.padding(horizontal = 32.dp),
                    shimmerProgress = shimmerAnimation.value,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor
                )
                Spacer(modifier = Modifier.height(48.dp))
                OutlineButton(
                    text = "Back to Home",
                    onClick = { isClicked = true },
                    modifier = Modifier
                        .padding(bottom = 36.dp)
                        .size(width = 280.dp, height = 60.dp)
                        .scale(buttonPulse.value * clickAnimation.value),
                    isClicked = isClicked,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor
                )
            }
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    primaryColor: Color,
    secondaryColor: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.12f),
                        secondaryColor.copy(alpha = 0.15f)
                    )
                )
            )
            .border(
                width = 2.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.85f),
                        secondaryColor.copy(alpha = 0.65f),
                        primaryColor.copy(alpha = 0.65f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(28.dp),
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
    shimmerProgress: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.45f),
                        primaryColor.copy(alpha = 0.45f),
                        secondaryColor.copy(alpha = 0.75f),
                        primaryColor.copy(alpha = 0.45f),
                        primaryColor.copy(alpha = 0.45f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(shimmerProgress * 1400f, 0f),
                    end = androidx.compose.ui.geometry.Offset(shimmerProgress * 1400f + 400f, 0f)
                ),
                alpha = 0.45f
            )
            .padding(10.dp)
    ) {
        Text(
            text = text,
            fontSize = 38.sp,
            fontWeight = FontWeight.Black,
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
    shimmerProgress: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.45f),
                        Color.White.copy(alpha = 0.45f),
                        secondaryColor.copy(alpha = 0.75f),
                        Color.White.copy(alpha = 0.45f),
                        Color.White.copy(alpha = 0.45f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(shimmerProgress * 1200f, 0f),
                    end = androidx.compose.ui.geometry.Offset(shimmerProgress * 1200f + 350f, 0f)
                ),
                alpha = 0.45f
            )
            .padding(10.dp)
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.95f),
            textAlign = TextAlign.Center,
            lineHeight = 32.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isClicked: Boolean,
    primaryColor: Color,
    secondaryColor: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(
                if (isClicked) Brush.linearGradient(
                    colors = listOf(
                        secondaryColor.copy(alpha = 0.35f),
                        primaryColor.copy(alpha = 0.35f)
                    )
                ) else Color.Transparent
            )
            .border(
                width = 2.8.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        primaryColor,
                        secondaryColor,
                        Color.White.copy(alpha = 0.9f)
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

object LuxeEasing : Easing {
    override fun transform(fraction: Float): Float {
        return (Math.sin(fraction * Math.PI * 2.2).toFloat() * 0.35f + 0.65f) * fraction
    }
}
