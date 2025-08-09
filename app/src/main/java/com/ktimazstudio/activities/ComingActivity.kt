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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
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
    val buttonPulse = remember { Animatable(1f) }
    val clickAnimation = remember { Animatable(1f) }
    var isClicked by remember { mutableStateOf(false) }

    LaunchedEffect(isClicked) {
        if (isClicked) {
            clickAnimation.animateTo(
                targetValue = 0.80f,
                animationSpec = tween(durationMillis = 300)
            )
            clickAnimation.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 300)
            )
            isClicked = false
            onBackClick()
        }
    }

    LaunchedEffect(Unit) {
        delay(600) // Staggered entrance
        cardAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000, easing = LuxeEasing)
        )
        shimmerAnimation.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
        delay(1200) // Stagger button animation
        buttonPulse.animateTo(
            targetValue = 1.18f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = LuxeEasing),
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
                    .fillMaxWidth(0.96f)
                    .alpha(cardAnimation.value)
                    .scale(0.65f + cardAnimation.value * 0.35f)
                    .offset(y = (-25f + cardAnimation.value * 25f).dp)
                    .rotate(cardAnimation.value * 5f - 2.5f),
                primaryColor = primaryColor,
                secondaryColor = secondaryColor
            ) {
                TitleText(
                    text = "$title Coming Soon",
                    modifier = Modifier.padding(top = 48.dp),
                    shimmerProgress = shimmerAnimation.value,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor
                )
                Spacer(modifier = Modifier.height(40.dp))
                DescriptionText(
                    text = "Designed with unmatched elegance, this feature is arriving soon. Get ready for a premium experience!",
                    modifier = Modifier.padding(horizontal = 44.dp),
                    shimmerProgress = shimmerAnimation.value,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor
                )
                Spacer(modifier = Modifier.height(72.dp))
                OutlineButton(
                    text = "Back to Home",
                    onClick = { isClicked = true },
                    modifier = Modifier
                        .padding(bottom = 48.dp)
                        .size(width = 340.dp, height = 72.dp)
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
            .clip(RoundedCornerShape(40.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0.2f),
                        secondaryColor.copy(alpha = 0.22f)
                    )
                ),
                shape = RoundedCornerShape(40.dp)
            )
            .border(
                width = 3.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        secondaryColor.copy(alpha = 0.8f),
                        primaryColor.copy(alpha = 0.8f)
                    )
                ),
                shape = RoundedCornerShape(40.dp)
            )
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(36.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            secondaryColor.copy(alpha = 0.15f),
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
                        primaryColor.copy(alpha = 0.6f),
                        primaryColor.copy(alpha = 0.6f),
                        secondaryColor.copy(alpha = 0.9f),
                        primaryColor.copy(alpha = 0.6f),
                        primaryColor.copy(alpha = 0.6f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(shimmerProgress * 2000f, 0f),
                    end = androidx.compose.ui.geometry.Offset(shimmerProgress * 2000f + 550f, 0f)
                )
            )
            .padding(16.dp)
    ) {
        androidx.compose.material3.Text(
            text = text,
            style = TextStyle(
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = 1.8.sp
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
                        Color.White.copy(alpha = 0.6f),
                        Color.White.copy(alpha = 0.6f),
                        secondaryColor.copy(alpha = 0.9f),
                        Color.White.copy(alpha = 0.6f),
                        Color.White.copy(alpha = 0.6f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(shimmerProgress * 1800f, 0f),
                    end = androidx.compose.ui.geometry.Offset(shimmerProgress * 1800f + 500f, 0f)
                )
            )
            .padding(16.dp)
    ) {
        androidx.compose.material3.Text(
            text = text,
            style = TextStyle(
                fontSize = 23.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.95f),
                textAlign = TextAlign.Center,
                lineHeight = 38.sp,
                letterSpacing = 1.2.sp
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
            .clip(RoundedCornerShape(44.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = if (isClicked) listOf(
                        secondaryColor.copy(alpha = 0.5f),
                        primaryColor.copy(alpha = 0.5f)
                    ) else listOf(Color.Transparent, Color.Transparent)
                ),
                shape = RoundedCornerShape(44.dp)
            )
            .border(
                width = 3.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        primaryColor,
                        secondaryColor,
                        Color.White.copy(alpha = 0.95f)
                    )
                ),
                shape = RoundedCornerShape(44.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 36.dp, vertical = 22.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = text,
            style = TextStyle(
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = 1.2.sp
            )
        )
    }
}

object LuxeEasing : Easing {
    override fun transform(fraction: Float): Float {
        return (Math.sin(fraction * Math.PI * 2.6).toFloat() * 0.2f + 0.8f) * fraction
    }
}
