package com.ktimazstudio.utils

import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.ktimazstudio.enums.ThemeSetting

import com.ktimazstudio.managers.new.EnhancedSharedPreferencesManager

/**
 * Determines if the app should be in dark theme based on the ThemeSetting.
 * Marked as @Composable because it calls isSystemInDarkTheme().
 */
// Replace the entire isAppInDarkTheme function with:
@Composable
fun isAppInDarkTheme(themeSetting: ThemeSetting, context: Context, sharedPrefsManager: EnhancedSharedPreferencesManager): Boolean {
    val systemInDarkTheme = isSystemInDarkTheme()
    val isHighContrastEnabled = sharedPrefsManager.isHighContrastEnabled()
    
    return when (themeSetting) {
        ThemeSetting.LIGHT -> false
        ThemeSetting.DARK -> true
        ThemeSetting.SYSTEM -> systemInDarkTheme
        ThemeSetting.BATTERY_SAVER -> {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                powerManager.isPowerSaveMode
            } else {
                systemInDarkTheme
            }
        }
    }
}