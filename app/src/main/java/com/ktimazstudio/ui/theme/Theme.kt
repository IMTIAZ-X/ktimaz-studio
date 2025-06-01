// File: app/src/main/java/com/ktimazstudio/ui/theme/Theme.kt
package com.ktimazstudio.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color // Import Color for custom color definitions
import androidx.compose.material3.Typography // Assuming you have a Typography object defined

// --- Color Palettes (Placeholders - Define your actual colors in Color.kt) ---
// You would typically define these in a separate file like app/src/main/java/com/ktimazstudio/ui/theme/Color.kt
// For demonstration, I'll define them here, but consider moving them.
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// --- Global Theme Mode State ---
// This variable holds the currently selected theme mode ("System", "Light", "Dark").
// It is a top-level property so it can be observed and updated from any Composable.
// Changes to this variable will trigger recomposition of the ktimaz Composable.
var AppThemeMode: String by mutableStateOf("System") // Default to "System"

// --- Dark Color Scheme Definition ---
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    // Add other dark mode colors as needed
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    surfaceContainerHigh = Color(0xFF2C2B2F), // Example for dialogs/cards in dark mode
    surfaceContainerHighest = Color(0xFF3C3B3F),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410)
)

// --- Light Color Scheme Definition ---
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    // Add other light mode colors as needed
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    surfaceContainerHigh = Color(0xFFF3EDF7), // Example for dialogs/cards in light mode
    surfaceContainerHighest = Color(0xFFE6E1E5),
    error = Color(0xFFB3261E),
    onError = Color.White
)

// --- Typography Definition (Placeholder) ---
// You would typically define your custom typography here.
// For simplicity, using a basic MaterialTheme.typography structure.
val Typography = Typography() // This will use Material3's default typography.
                              // If you have a custom Typography object, ensure it's imported or defined here.


/**
 * Main Composable for applying the application's theme.
 * It determines the ColorScheme based on the globally set AppThemeMode.
 *
 * @param content The UI content to be themed.
 */
@Composable
fun ktimaz(
    content: @Composable () -> Unit
) {
    // Check if the system is currently in dark theme mode
    val systemInDarkTheme = isSystemInDarkTheme()

    // Determine if dark theme should be applied based on the AppThemeMode state
    val useDarkTheme = when (AppThemeMode) {
        "Light" -> false // Force Light mode
        "Dark" -> true  // Force Dark mode
        else -> systemInDarkTheme // "System" mode follows the system setting
    }

    // Check if dynamic color is available on the device (Android 12+ / API 31+)
    val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    // Select the appropriate ColorScheme
    val colorScheme = when {
        supportsDynamicColor -> {
            val context = LocalContext.current
            // Use dynamic colors if supported and enabled
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDarkTheme -> DarkColorScheme // Fallback to custom DarkColorScheme if dynamic not supported
        else -> LightColorScheme // Fallback to custom LightColorScheme if dynamic not supported
    }

    // Apply the chosen Material Theme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Apply your defined Typography
        content = content
    )
}
