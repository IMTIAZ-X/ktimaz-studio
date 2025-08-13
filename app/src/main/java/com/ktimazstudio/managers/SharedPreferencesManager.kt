package com.ktimazstudio.managers

import android.content.Context
import android.content.SharedPreferences
import com.ktimazstudio.enums.ThemeSetting
import com.ktimazstudio.enums.NavigationStyle
import com.ktimazstudio.enums.LayoutDensity
import com.ktimazstudio.enums.AnimationSpeed
import com.ktimazstudio.enums.DashboardViewType
import com.ktimazstudio.enums.CardSize
import com.ktimazstudio.utils.CryptoUtils

/**
 * Enhanced SharedPreferencesManager with new settings and encryption
 */
class SharedPreferencesManager(context: Context) {
    val prefs: SharedPreferences = context.getSharedPreferences("AppPrefsKtimazStudio", Context.MODE_PRIVATE)
    private val securePrefs: SharedPreferences = context.getSharedPreferences("SecurePrefsKtimaz", Context.MODE_PRIVATE)
    private val cryptoUtils = CryptoUtils()

    companion object {
        // Existing keys
        private const val KEY_IS_LOGGED_IN = "is_logged_in_key"
        private const val KEY_USERNAME = "username_key"
        const val KEY_THEME_SETTING = "theme_setting_key"
        const val KEY_SOUND_ENABLED = "sound_enabled_key"
        
        // New UI/UX Settings
        const val KEY_OLD_UI_ENABLED = "old_ui_enabled_key"
        const val KEY_NAVIGATION_STYLE = "navigation_style_key"
        const val KEY_LAYOUT_DENSITY = "layout_density_key"
        const val KEY_ANIMATION_SPEED = "animation_speed_key"
        const val KEY_DASHBOARD_VIEW_TYPE = "dashboard_view_type_key"
        const val KEY_CARD_SIZE = "card_size_key"
        const val KEY_HAPTIC_FEEDBACK = "haptic_feedback_key"
        const val KEY_AUTO_THEME_SWITCH = "auto_theme_switch_key"
        const val KEY_SECURE_MODE = "secure_mode_key"
        const val KEY_DEBUG_MODE = "debug_mode_key"
        
        // Security keys (encrypted storage)
        private const val SECURE_KEY_USER_TOKEN = "user_token_secure"
        private const val SECURE_KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    }

    // === Existing Methods ===
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun setLoggedIn(loggedIn: Boolean, username: String? = null) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, loggedIn)
            if (loggedIn && username != null) {
                putString(KEY_USERNAME, username)
            } else if (!loggedIn) {
                remove(KEY_USERNAME)
            }
            apply()
        }
    }

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    fun getThemeSetting(): ThemeSetting {
        val themeString = prefs.getString(KEY_THEME_SETTING, ThemeSetting.SYSTEM.name)
        return try {
            ThemeSetting.valueOf(themeString ?: ThemeSetting.SYSTEM.name)
        } catch (e: IllegalArgumentException) {
            ThemeSetting.SYSTEM
        }
    }

    fun setThemeSetting(themeSetting: ThemeSetting) {
        prefs.edit().putString(KEY_THEME_SETTING, themeSetting.name).apply()
    }

    fun isSoundEnabled(): Boolean = prefs.getBoolean(KEY_SOUND_ENABLED, true)

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    // === New UI/UX Settings ===
    fun isOldUiEnabled(): Boolean = prefs.getBoolean(KEY_OLD_UI_ENABLED, false)

    fun setOldUiEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_OLD_UI_ENABLED, enabled).apply()
    }

    fun getNavigationStyle(): NavigationStyle {
        val styleString = prefs.getString(KEY_NAVIGATION_STYLE, NavigationStyle.RAIL.name)
        return try {
            NavigationStyle.valueOf(styleString ?: NavigationStyle.RAIL.name)
        } catch (e: IllegalArgumentException) {
            NavigationStyle.RAIL
        }
    }

    fun setNavigationStyle(style: NavigationStyle) {
        prefs.edit().putString(KEY_NAVIGATION_STYLE, style.name).apply()
    }

    fun getLayoutDensity(): LayoutDensity {
        val densityString = prefs.getString(KEY_LAYOUT_DENSITY, LayoutDensity.MEDIUM.name)
        return try {
            LayoutDensity.valueOf(densityString ?: LayoutDensity.MEDIUM.name)
        } catch (e: IllegalArgumentException) {
            LayoutDensity.MEDIUM
        }
    }

    fun setLayoutDensity(density: LayoutDensity) {
        prefs.edit().putString(KEY_LAYOUT_DENSITY, density.name).apply()
    }

    fun getAnimationSpeed(): AnimationSpeed {
        val speedString = prefs.getString(KEY_ANIMATION_SPEED, AnimationSpeed.NORMAL.name)
        return try {
            AnimationSpeed.valueOf(speedString ?: AnimationSpeed.NORMAL.name)
        } catch (e: IllegalArgumentException) {
            AnimationSpeed.NORMAL
        }
    }

    fun setAnimationSpeed(speed: AnimationSpeed) {
        prefs.edit().putString(KEY_ANIMATION_SPEED, speed.name).apply()
    }

    fun getDashboardViewType(): DashboardViewType {
        val typeString = prefs.getString(KEY_DASHBOARD_VIEW_TYPE, DashboardViewType.GRID.name)
        return try {
            DashboardViewType.valueOf(typeString ?: DashboardViewType.GRID.name)
        } catch (e: IllegalArgumentException) {
            DashboardViewType.GRID
        }
    }

    fun setDashboardViewType(viewType: DashboardViewType) {
        prefs.edit().putString(KEY_DASHBOARD_VIEW_TYPE, viewType.name).apply()
    }

    fun getCardSize(): CardSize {
        val sizeString = prefs.getString(KEY_CARD_SIZE, CardSize.MEDIUM.name)
        return try {
            CardSize.valueOf(sizeString ?: CardSize.MEDIUM.name)
        } catch (e: IllegalArgumentException) {
            CardSize.MEDIUM
        }
    }

    fun setCardSize(size: CardSize) {
        prefs.edit().putString(KEY_CARD_SIZE, size.name).apply()
    }

    fun isHapticFeedbackEnabled(): Boolean = prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true)

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC_FEEDBACK, enabled).apply()
    }

    fun isAutoThemeSwitchEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_THEME_SWITCH, false)

    fun setAutoThemeSwitchEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_THEME_SWITCH, enabled).apply()
    }

    fun isSecureModeEnabled(): Boolean = prefs.getBoolean(KEY_SECURE_MODE, true)

    fun setSecureModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SECURE_MODE, enabled).apply()
    }

    fun isDebugModeEnabled(): Boolean = prefs.getBoolean(KEY_DEBUG_MODE, false)

    fun setDebugModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DEBUG_MODE, enabled).apply()
    }

    // === Secure Storage Methods ===
    fun setUserToken(token: String) {
        val encryptedToken = cryptoUtils.encrypt(token)
        securePrefs.edit().putString(SECURE_KEY_USER_TOKEN, encryptedToken).apply()
    }

    fun getUserToken(): String? {
        val encryptedToken = securePrefs.getString(SECURE_KEY_USER_TOKEN, null)
        return encryptedToken?.let { cryptoUtils.decrypt(it) }
    }

    fun isBiometricEnabled(): Boolean = securePrefs.getBoolean(SECURE_KEY_BIOMETRIC_ENABLED, false)

    fun setBiometricEnabled(enabled: Boolean) {
        securePrefs.edit().putBoolean(SECURE_KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    // === Utility Methods ===
    fun clearAllData() {
        prefs.edit().clear().apply()
        securePrefs.edit().clear().apply()
    }

    fun clearUserData() {
        prefs.edit().apply {
            remove(KEY_IS_LOGGED_IN)
            remove(KEY_USERNAME)
            apply()
        }
        securePrefs.edit().apply {
            remove(SECURE_KEY_USER_TOKEN)
            apply()
        }
    }

    fun exportSettings(): Map<String, Any?> {
        return prefs.all.filterKeys { key ->
            !key.contains("login") && !key.contains("username")
        }
    }

    fun importSettings(settings: Map<String, Any?>) {
        prefs.edit().apply {
            settings.forEach { (key, value) ->
                when (value) {
                    is Boolean -> putBoolean(key, value)
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Float -> putFloat(key, value)
                    is Long -> putLong(key, value)
                }
            }
            apply()
        }
    }
}