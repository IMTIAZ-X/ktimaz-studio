package com.ktimazstudio

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ViewModel for managing UI state
class ComingSoonViewModel : ViewModel() {
    private val _isButtonClicked = MutableStateFlow(false)
    val isButtonClicked: StateFlow<Boolean> = _isButtonClicked

    fun onButtonClick() {
        viewModelScope.launch {
            _isButtonClicked.value = true
            delay(200)
            _isButtonClicked.value = false
        }
    }
}

// Custom Easing for animations
object NebulaAnimations {
    object NebulaEasing : Easing {
        override fun transform(fraction: Float): Float {
            return 1f - (1f - fraction) * (1f - fraction)
        }
    }
}

// Material 3 Color Schemes
private val DarkNebulaColorScheme = darkColorScheme(
    primary = Color(0xFF7B61FF),
    onPrimary = Color.White,
    secondary = Color(0xFFAB8FFF),
    background = Color(0xFF0A0A1A),
    surface = Color(0xFF1A1A3A),
    surfaceVariant = Color(0xFF1C1C3C),
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightNebulaColorScheme = lightColorScheme(
    primary = Color(0xFF4A3DFF),
    onPrimary = Color.White,
    secondary = Color(0xFF7B61FF),
    background = Color(0xFFDDEEFF),
    surface = Color(0xFFAABBFF),
    surfaceVariant = Color(0xFFF0F4FF),
    onBackground = Color(0xFF0A0A1A),
    onSurface = Color(0xFF0A0A1A)
)

// Main Theme Composable
@Composable
fun NebulaTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkNebulaColorScheme else LightNebulaColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

// UI Components
@Composable
fun HolographicCard(
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
fun NebulaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color,
    accentColorSecondary: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hapticFeedback = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(10.dp))
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
                indication = null
            ) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 24.dp, vertical = 14.dp),
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
        modifier = modifier.fillMaxWidth()
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
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            lineHeight = 26.sp,
            letterSpacing = 1.sp
        ),
        modifier = modifier.fillMaxWidth()
    )
}

// Main Composable Screen
@Composable
fun ComingSoonScreen(
    title: String,
    onBackClick: () -> Unit,
    viewModel: ComingSoonViewModel = viewModel()
) {
    val cardAnimation = remember { Animatable(0f) }
    val buttonScale = remember { Animatable(1f) }

    val glowPulse by rememberInfiniteTransition().animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = NebulaAnimations.NebulaEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val isButtonClicked by viewModel.isButtonClicked.collectAsState()

    LaunchedEffect(Unit) {
        cardAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = NebulaAnimations.NebulaEasing)
        )
    }

    LaunchedEffect(isButtonClicked) {
        if (isButtonClicked) {
            buttonScale.animateTo(
                targetValue = 0.92f,
                animationSpec = tween(durationMillis = 100)
            )
            buttonScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 100)
            )
            onBackClick()
        }
    }

    val backgroundColorStart = MaterialTheme.colorScheme.background
    val backgroundColorEnd = MaterialTheme.colorScheme.surface
    val cardColor = MaterialTheme.colorScheme.surfaceVariant
    val accentColor = MaterialTheme.colorScheme.primary
    val accentColorSecondary = MaterialTheme.colorScheme.secondary

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
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HolographicCard(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .alpha(cardAnimation.value)
                    .graphicsLayer(translationY = (-30f + cardAnimation.value * 30f)),
                cardColor = cardColor,
                accentColor = accentColor,
                accentColorSecondary = accentColorSecondary,
                glowAlpha = glowPulse
            ) {
                TitleText(
                    text = "$title Coming Soon",
                    modifier = Modifier.padding(top = 24.dp),
                    accentColor = accentColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                DescriptionText(
                    text = "Experience innovation like never before. Unveiling soon with revolutionary features.",
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                NebulaButton(
                    text = "Return",
                    onClick = { viewModel.onButtonClick() },
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .size(width = 220.dp, height = 48.dp)
                        .scale(buttonScale.value)
                        .alpha(1f + glowPulse * 0.4f),
                    accentColor = accentColor,
                    accentColorSecondary = accentColorSecondary
                )
            }
        }
    }
}

// Main Activity
class ComingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val cardTitle = intent.getStringExtra("CARD_TITLE") ?: "New Feature"
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val themePreference = sharedPreferences.getString("theme_setting_key", "system") ?: "system"

        setContent {
            val isDarkTheme = when (themePreference) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }
            NebulaTheme(darkTheme = isDarkTheme) {
                ComingSoonScreen(
                    title = cardTitle,
                    onBackClick = { finish() }
                )
            }
        }
    }
}
