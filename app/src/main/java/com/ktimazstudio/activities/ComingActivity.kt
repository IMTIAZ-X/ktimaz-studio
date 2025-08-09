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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
    val buttonGlow = remember { Animatable(0f) }
    var isClicked by remember { mutableStateOf(false) }

    LaunchedEffect(isClicked) {
        if (isClicked) {
            buttonGlow.animateTo(
                targetValue = 0.5f,
                animationSpec = tween(durationMillis = 200)
            )
            buttonGlow.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 200)
            )
            isClicked = false
            onBackClick()
        }
    }

    LaunchedEffect(Unit) {
        delay(300) // Subtle staggered entrance
        cardAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = ProfessionalEasing)
        )
        shimmerAnimation.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
        delay(600) // Stagger button glow
        buttonGlow.animateTo(
            targetValue = 0.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = ProfessionalEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
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
                    .fillMaxWidth(0.9f)
                    .alpha(cardAnimation.value)
                    .offset(y = (-10f + cardAnimation.value * 10f).dp),
                primaryColor = primaryColor,
                secondaryColor = secondaryColor
            ) {
                TitleText(
                    text = "$title Coming Soon",
                    modifier = Modifier.padding(top = 32.dp),
                    shimmerProgress = shimmerAnimation.value,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor
                )
                Spacer(modifier = Modifier.height(24.dp))
                DescriptionText(
                    text = "This feature is being crafted with precision. Expect a world-class experience soon.",
                    modifier = Modifier.padding(horizontal = 32.dp),
                    shimmerProgress = shimmerAnimation.value,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor
                )
                Spacer(modifier = Modifier.height(40.dp))
                OutlineButton(
                    text = "Back to Home",
                    onClick = { isClicked = true },
                    modifier = Modifier
                        .padding(bottom = 32.dp)
                        .size(width = 260.dp, height = 56.dp)
                        .alpha(1f - buttonGlow.value * 0.5f),
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
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.2f),
                        Color.White.copy(alpha = 0.1f),
                        secondaryColor.copy(alpha = 0.15f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.8f),
                        secondaryColor.copy(alpha = 0.6f),
                        primaryColor.copy(alpha = 0.6f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            secondaryColor.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )
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
                brush = Brush.linearGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.4f),
                        primaryColor.copy(alpha = 0.4f),
                        secondaryColor.copy(alpha = 0.7f),
                        primaryColor.copy(alpha = 0.4f),
                        primaryColor.copy(alpha = 0.4f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(shimmerProgress * 1200f, 0f),
                    end = androidx.compose.ui.geometry.Offset(shimmerProgress * 1200f + 400f, 0f)
                )
            )
            .padding(10.dp)
    ) {
        androidx.compose.material3.Text(
            text = text,
            style = TextStyle(
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            ),
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
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.4f),
                        Color.White.copy(alpha = 0.4f),
                        secondaryColor.copy(alpha = 0.7f),
                        Color.White.copy(alpha = 0.4f),
                        Color.White.copy(alpha = 0.4f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(shimmerProgress * 1000f, 0f),
                    end = androidx.compose.ui.geometry.Offset(shimmerProgress * 1000f + 350f, 0f)
                )
            )
            .padding(10.dp)
    ) {
        androidx.compose.material3.Text(
            text = text,
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.95f),
                textAlign = TextAlign.Center,
                lineHeight = 32.sp,
                letterSpacing = 0.8.sp
            ),
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
                brush = Brush.linearGradient(
                    colors = if (isClicked) listOf(
                        secondaryColor.copy(alpha = 0.3f),
                        primaryColor.copy(alpha = 0.3f)
                    ) else listOf(Color.Transparent, Color.Transparent)
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .border(
                width = 2.5.dp,
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
        androidx.compose.material3.Text(
            text = text,
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = 0.8.sp
            )
        )
    }
}

object ProfessionalEasing : Easing {
    override fun transform(fraction: Float): Float {
        return fraction * fraction * (3f - 2f * fraction) // Smooth, subtle ease-in-out
    }
}
