package com.ktimazstudio

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// Animation constants
private const val CARD_ANIM_DURATION = 800
private const val GLOW_PULSE_DURATION = 1500
private const val BUTTON_ANIM_DURATION = 100

class ComingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val cardTitle = intent.getStringExtra("CARD_TITLE") ?: "New Feature"

        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val theme = sharedPreferences.getString("theme_setting_key", "system") ?: "system"

        setContent {
            ktimaz(theme = theme) {
                PremiumNebulaComingSoonScreen(
                    title = cardTitle,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@Composable
fun ktimaz(
    theme: String,
    content: @Composable () -> Unit
) {
    val isDarkTheme = remember(theme) {
        when (theme) {
            "dark" -> true
            "light" -> false
            else -> androidx.compose.foundation.isSystemInDarkTheme()
        }
    }

    val colorScheme = if (isDarkTheme) {
        androidx.compose.material3.darkColorScheme()
    } else {
        androidx.compose.material3.lightColorScheme()
    }

    androidx.compose.material3.MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

@Composable
fun PremiumNebulaComingSoonScreen(
    title: String,
    onBackClick: () -> Unit
) {
    val isDarkTheme = androidx.compose.material3.MaterialTheme.colorScheme == androidx.compose.material3.darkColorScheme()
    val backgroundColorStart = if (isDarkTheme) Color(0xFF0A0A1A) else Color(0xFFDDEEFF)
    val backgroundColorEnd = if (isDarkTheme) Color(0xFF1A1A3A) else Color(0xFFAABBFF)
    val cardColor = if (isDarkTheme) Color(0xFF1C1C3C) else Color(0xFFF0F4FF)
    val accentColor = if (isDarkTheme) Color(0xFF7B61FF) else Color(0xFF4A3DFF)
    val accentColorSecondary = if (isDarkTheme) Color(0xFFAB8FFF) else Color(0xFF7B61FF)

    // Animation states
    val cardAnimation = remember { Animatable(0f) }
    val buttonGlow = remember { Animatable(0f) }
    var isClicked by remember { mutableStateOf(false) }
    val buttonScale = remember { Animatable(1f) }
    
    // Infinite glow animation
    val infiniteTransition = rememberInfiniteTransition()
    val glowPulse = infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(GLOW_PULSE_DURATION, easing = NebulaEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Handle back press
    BackHandler { onBackClick() }

    // Animations for card entrance
    LaunchedEffect(Unit) {
        delay(200)
        cardAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(CARD_ANIM_DURATION, easing = NebulaEasing)
        )
        delay(400)
        buttonGlow.animateTo(
            targetValue = 0.3f,
            animationSpec = tween(1200, easing = NebulaEasing)
        )
    }

    // Button click animation
    LaunchedEffect(isClicked) {
        if (isClicked) {
            buttonScale.animateTo(0.92f, tween(BUTTON_ANIM_DURATION))
            buttonScale.animateTo(1f, tween(BUTTON_ANIM_DURATION))
            buttonGlow.animateTo(0.8f, tween(BUTTON_ANIM_DURATION))
            buttonGlow.animateTo(0f, tween(BUTTON_ANIM_DURATION))
            isClicked = false
            onBackClick()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(backgroundColorStart, backgroundColorEnd),
                    radius = 1200f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PremiumHolographicCard(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .alpha(cardAnimation.value)
                    .graphicsLayer(translationY = (-30f + cardAnimation.value * 30f)),
                cardColor = cardColor,
                accentColor = accentColor,
                accentColorSecondary = accentColorSecondary,
                glowAlpha = glowPulse.value
            ) {
                TitleText(
                    text = "$title Coming Soon",
                    modifier = Modifier.padding(top = 24.dp, horizontal = 16.dp),
                    accentColor = accentColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                DescriptionText(
                    text = "Experience innovation like never before. Unveiling soon with revolutionary features.",
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                PremiumNebulaButton(
                    text = "Return",
                    onClick = { isClicked = true },
                    accentColor = accentColor,
                    accentColorSecondary = accentColorSecondary,
                    glowAlpha = glowPulse.value,
                    buttonScale = buttonScale.value,
                    buttonGlow = buttonGlow.value
                )
            }
        }
    }
}

@Composable
fun PremiumHolographicCard(
    modifier: Modifier = Modifier,
    cardColor: Color,
    accentColor: Color,
    accentColorSecondary: Color,
    glowAlpha: Float,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        cardColor.copy(alpha = 0.7f),
                        cardColor.copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(accentColor, accentColorSecondary, accentColor.copy(alpha = 0.7f))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(24.dp)
            .alpha(1f + glowAlpha * 0.3f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.1f),
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
    Text(
        text = text,
        style = TextStyle(
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            textAlign = TextAlign.Center,
            letterSpacing = 1.5.sp
        ),
        modifier = modifier.fillMaxWidth(),
        maxLines = 2
    )
}

@Composable
fun DescriptionText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = if (androidx.compose.material3.MaterialTheme.colorScheme == androidx.compose.material3.darkColorScheme()) 
                Color.White.copy(alpha = 0.9f) 
            else 
                Color.Black.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            lineHeight = 26.sp,
            letterSpacing = 1.sp
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun PremiumNebulaButton(
    text: String,
    onClick: () -> Unit,
    accentColor: Color,
    accentColorSecondary: Color,
    glowAlpha: Float,
    buttonScale: Float,
    buttonGlow: Float
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val hapticFeedback = LocalHapticFeedback.current
    
    // Animated elevation for pressed state
    val elevation by animateDpAsState(if (isPressed) 4.dp else 8.dp)

    Box(
        modifier = Modifier
            .padding(bottom = 24.dp)
            .size(width = 220.dp, height = 48.dp)
            .scale(buttonScale)
            .alpha(1f + buttonGlow * 0.5f)
            .shadow(elevation, shape = RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(accentColor, accentColorSecondary)
                ),
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(accentColor.copy(alpha = 0.8f), Color.White.copy(alpha = 0.5f))
                ),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null // Using custom visual feedback instead of ripple
            ) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 24.dp, vertical = 14.dp)
            .alpha(1f + glowAlpha * 0.4f),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )
        )
    }
}

object NebulaEasing : Easing {
    override fun transform(fraction: Float): Float {
        return 1f - (1f - fraction) * (1f - fraction) // Ease-out
    }
}
