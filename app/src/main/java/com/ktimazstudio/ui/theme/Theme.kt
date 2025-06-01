package com.ktimazstudio.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

// Define your DarkColorScheme (uncommented)
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

// This variable will hold the globally selected theme mode
// You would typically store this in SharedPreferences or a data store
// to persist across app launches. For this example, we'll use a simple
// rememberSaveable state for demonstration within the app's lifetime.
var AppThemeMode by mutableStateOf("System") // "System", "Light", "Dark"

@Composable
fun ktimaz(
    content: @Composable () -> Unit
) {
    // Observe the system's dark theme setting
    val systemDarkTheme = isSystemInDarkTheme()

    // Determine if dark theme should be applied based on AppThemeMode
    val darkTheme = when (AppThemeMode) {
        "Light" -> false
        "Dark" -> true
        else -> systemDarkTheme // "System"
    }

    // Determine if dynamic color should be used (Android 12+)
    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
