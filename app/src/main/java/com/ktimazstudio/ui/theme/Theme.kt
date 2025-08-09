package com.ktimazstudio.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import com.ktimazstudio.managers.new.EnhancedSharedPreferencesManager
import com.ktimazstudio.utils.new.getAppColorScheme
import com.ktimazstudio.utils.new.getAdjustedTypography
import androidx.compose.ui.platform.LocalContext

// Import all color variables from Color.kt
import com.ktimazstudio.ui.theme.*

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerLow = DarkSurfaceContainerLow
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerLow = LightSurfaceContainerLow
)

@Composable
fun ktimaz(
    darkTheme: Boolean = isSystemInDarkTheme(),
    sharedPrefsManager: EnhancedSharedPreferencesManager? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = if (sharedPrefsManager != null) {
        getAppColorScheme(
            isDarkTheme = darkTheme,
            context = LocalContext.current,
            sharedPrefsManager = sharedPrefsManager
        )
    } else {
        // Fallback to default colors if manager not available
        if (darkTheme) darkColorScheme() else lightColorScheme()
    }
    
    val typography = if (sharedPrefsManager != null) {
        getAdjustedTypography(
            fontSizePercentage = sharedPrefsManager.getFontSize(),
            isLargeTextEnabled = sharedPrefsManager.isLargeTextEnabled()
        )
    } else {
        Typography() // Default typography
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}