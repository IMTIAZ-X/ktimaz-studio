package com.ktimazstudio.agent.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ktimazstudio.agent.data.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = AppTheme.PrimaryStart,
    background = AppTheme.SurfaceDark,
    surface = AppTheme.CardDark,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = AppTheme.PrimaryStart,
    background = Color(0xFFF8F9FE),
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun ModernAgentTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}
