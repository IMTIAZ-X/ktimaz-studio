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
                targetValue = 0.82f,
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
        delay(500) // Staggered entrance
        cardAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1800, easing = LuxeEasing)
        )
        shimmerAnimation.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2600, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
        delay(1000) // Stagger button animation
        buttonPulse.animateTo(
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = LuxeEasing),
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
                    .fillMaxWidth(0.95f)
                    .alpha(cardAnimation.value)
                    .scale(0.7f + cardAnimation.value * 0.3f)
                    .offset(y = (-20f + cardAnimation.value * 20f).dp)
                    .rotate(cardAnimation.value * 4f - 2f),
                primaryColor = primaryColor,
                secondaryColor = secondaryColor
            ) {
                TitleText(
                    text = "$title Coming Soon",
                    modifier = Modifier.padding(top = 44.dp),
                    shimmerProgress = shimmerAnimation.value,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor
                )
                Spacer(modifier = Modifier.height(36.dp))
                DescriptionText(
                    text = "Designed with unparalleled elegance, this feature is arriving soon. Get ready for a premium experience!",
                    modifier = Modifier.padding(horizontal = 40.dp),
                    shimmerProgress = shimmerAnimation.value,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor
                )
                Spacer(modifier = Modifier.height(64.dp))
                OutlineButton(
                    text = "Back to Home",
                    onClick = { isClicked = true },
                    modifier = Modifier
                        .padding(bottom = 44.dp)
                        .size(width = 320.dp, height = 68.dp)
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
            .clip(RoundedCornerShape(36.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.28f),
                        Color.White.copy(alpha = 0.18f),
                        secondaryColor.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(36.dp)
                )
            )
            .border(
                width = 2.8.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        secondaryColor.copy(alpha = 0.75f),
                        primaryColor.copy(alpha = 0.75f)
                    )
                ),
                shape = RoundedCornerShape(36.dp)
            )
            .padding(36.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(32.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            secondaryColor.copy(alpha = 0.12f),
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
                        primaryColor.copy(alpha = 0.55f),
                        primaryColor.copy(alpha = 0.55f),
                        secondaryColor.copy(alpha = 0.85f),
                        primaryColor.copy(alpha = 0.55f),
                        primaryColor.copy(alpha = 0.55f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(shimmerProgress * 1800f, 0f),
                    end = androidx.compose.ui.geometry.Offset(shimmerProgress * 1800f + 500f, 0f)
                )
            )
            .padding(14.dp)
    ) {
        androidx.compose.material3.Text(
            text = text,
            style = TextStyle(
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = 1.5.sp
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
                        Color.White.copy(alpha = 0.55f),
                        Color.White.copy(alpha = 0.55f),
                        secondaryColor.copy(alpha = 0.85f),
                        Color.White.copy(alpha = 0.55f),
                        Color.White.copy(alpha = 0.55f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(shimmerProgress * 1600f, 0f),
                    end = androidx.compose.ui.geometry.Offset(shimmerProgress * 1600f + 450f, 0f)
                )
            )
            .padding(14.dp)
    ) {
        androidx.compose.material3.Text(
            text = text,
            style = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.95f),
                textAlign = TextAlign.Center,
                lineHeight = 36.sp,
                letterSpacing = 1.sp
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
            .clip(RoundedCornerShape(40.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = if (isClicked) listOf(
                        secondaryColor.copy(alpha = 0.45f),
                        primaryColor.copy(alpha = 0.45f)
                    ) else listOf(Color.Transparent, Color.Transparent),
                    shape = RoundedCornerShape(40.dp)
                )
            )
            .border(
                width = 3.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        primaryColor,
                        secondaryColor,
                        Color.White.copy(alpha = 0.95f)
                    )
                ),
                shape = RoundedCornerShape(40.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 32.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = text,
            style = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )
        )
    }
}

object LuxeEasing : Easing {
    override fun transform(fraction: Float): Float {
        return (Math.sin(fraction * Math.PI * 2.5).toFloat() * 0.25f + 0.75f) * fraction
    }
}
