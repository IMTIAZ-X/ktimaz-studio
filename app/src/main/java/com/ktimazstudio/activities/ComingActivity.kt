package com.ktimazstudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
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
import androidx.compose.ui.platform.LocalConfiguration
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
        val cardTitle = intent.getStringExtra("CARD_TITLE") ?: "New Feature"
        setContent {
            ktimaz {
                NebulaComingSoonScreen(cardTitle) {
                    finish()
                }
            }
        }
    }
}

@Composable
fun NebulaComingSoonScreen(title: String, onBackClick: () -> Unit) {
    val isDarkTheme = LocalConfiguration.current.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES
    val backgroundColorStart = if (isDarkTheme) Color(0xFF0A0A1A) else Color(0xFFDDEEFF)
    val backgroundColorEnd = if (isDarkTheme) Color(0xFF1A1A3A) else Color(0xFFAABBFF)
    val cardColor = if (isDarkTheme) Color(0xFF1C1C3C) else Color(0xFFF0F4FF)
    val accentColor = if (isDarkTheme) Color(0xFF7B61FF) else Color(0xFF4A3DFF)
    val accentColorSecondary = if (isDarkTheme) Color(0xFFAB8FFF) else Color(0xFF7B61FF)

    val cardAnimation = remember { Animatable(0f) }
    val buttonGlow = remember { Animatable(0f) }
    var isClicked by remember { mutableStateOf(false) }
    val buttonScale = remember { Animatable(1f) }

    LaunchedEffect(isClicked) {
        if (isClicked) {
            buttonScale.animateTo(
                targetValue = 0.92f,
                animationSpec = tween(durationMillis = 100)
            )
            buttonScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 100)
            )
            buttonGlow.animateTo(
                targetValue = 0.8f,
                animationSpec = tween(durationMillis = 100)
            )
            buttonGlow.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 100)
            )
            isClicked = false
            onBackClick()
        }
    }

    LaunchedEffect(Unit) {
        delay(200)
        cardAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = NebulaEasing)
        )
        delay(400)
        buttonGlow.animateTo(
            targetValue = 0.3f,
            animationSpec = tween(durationMillis = 1200, easing = NebulaEasing)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(backgroundColorStart, backgroundColorEnd),
                    radius = 1000f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HolographicCard(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .alpha(cardAnimation.value)
                    .offset(y = ((-30f + cardAnimation.value * 30f).dp)),
                cardColor = cardColor,
                accentColor = accentColor,
                accentColorSecondary = accentColorSecondary
            ) {
                TitleText(
                    text = "$title Coming Soon",
                    modifier = Modifier.padding(top = 20.dp),
                    accentColor = accentColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                DescriptionText(
                    text = "Prepare for an innovative experience. Launching soon with cutting-edge features.",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                NebulaButton(
                    text = "Return",
                    onClick = { isClicked = true },
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .size(width = 200.dp, height = 44.dp)
                        .scale(buttonScale.value)
                        .alpha(1f + buttonGlow.value * 0.5f),
                    accentColor = accentColor,
                    accentColorSecondary = accentColorSecondary
                )
            }
        }
    }
}

@Composable
fun HolographicCard(
    modifier: Modifier = Modifier,
    cardColor: Color,
    accentColor: Color,
    accentColorSecondary: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        cardColor.copy(alpha = 0.6f),
                        cardColor.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(accentColor, accentColorSecondary)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(8.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.05f),
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
    accentColor: Color
) {
    androidx.compose.material3.Text(
        text = text,
        style = TextStyle(
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = accentColor,
            textAlign = TextAlign.Center,
            letterSpacing = 1.2.sp
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun DescriptionText(
    text: String,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Text(
        text = text,
        style = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            letterSpacing = 0.8.sp
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun NebulaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color,
    accentColorSecondary: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(accentColor.copy(alpha = 0.5f), accentColorSecondary.copy(alpha = 0.5f))
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(accentColor, accentColorSecondary, Color.White.copy(alpha = 0.7f))
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = text,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = 0.8.sp
            )
        )
    }
}

object NebulaEasing : Easing {
    override fun transform(fraction: Float): Float {
        return 1f - (1f - fraction) * (1f - fraction) // Ease-out for smooth transitions
    }
}
