package com.ktimazstudio.managers

import android.content.Context
import android.content.SharedPreferences
import com.ktimazstudio.enums.*

/**
 * Enhanced SharedPreferencesManager with comprehensive settings support
 * Manages all user preferences including themes, navigation, accessibility, and more
 */
class EnhancedSharedPreferencesManager(context: Context) {
    val prefs: SharedPreferences = context.getSharedPreferences("AppPrefsKtimazStudio", Context.MODE_PRIVATE)
    private val context: Context = context

    companion object {
        // Authentication
        private const val KEY_IS_LOGGED_IN = "is_logged_in_key"
        private const val KEY_USERNAME = "username_key"
        
        // Appearance & Themes
        const val KEY_THEME_SETTING = "theme_setting_key"
        const val KEY_DYNAMIC_COLORS = "dynamic_colors_key"
        const val KEY_HIGH_CONTRAST = "high_contrast_key"
        const val KEY_FONT_SIZE = "font_size_key"
        const val KEY_LAYOUT_DENSITY = "layout_density_key"
        const val KEY_CUSTOM_ACCENT_COLOR = "custom_accent_color_key"
        
        // Audio & Haptics
        const val KEY_SOUND_ENABLED = "sound_enabled_key"
        const val KEY_HAPTIC_ENABLED = "haptic_enabled_key"
        const val KEY_ANIMATION_SPEED = "animation_speed_key"
        
        // Navigation & Layout
        const val KEY_NAVIGATION_STYLE = "navigation_style_key"
        const val KEY_DASHBOARD_VIEW_TYPE = "dashboard_view_type_key"
        const val KEY_DEFAULT_CARD_SIZE = "default_card_size_key"
        
        // Accessibility
        const val KEY_REDUCED_MOTION = "reduced_motion_key"
        const val KEY_LARGE_TEXT = "large_text_key"
        const val KEY_SCREEN_READER_MODE = "screen_reader_mode_key"
        
        // Dashboard Customization
        const val KEY_HIDDEN_MODULES = "hidden_modules_key"
        const val KEY_MODULE_ORDER = "module_order_key"
        const val KEY_FAVORITE_MODULES = "favorite_modules_key"
        const val KEY_DASHBOARD_COLUMNS = "dashboard_columns_key"
        
        // Privacy & Security
        const val KEY_BIOMETRIC_ENABLED = "biometric_enabled_key"
        const val KEY_AUTO_LOCK_TIMEOUT = "auto_lock_timeout_key"
        const val KEY_SECURITY_NOTIFICATIONS = "security_notifications_key"
        
        // Performance & Developer
        const val KEY_PERFORMANCE_MODE = "performance_mode_key"
        const val KEY_DEBUG_MODE = "debug_mode_key"
        const val KEY_CRASH_REPORTING = "crash_reporting_key"
        
        // User Experience
        const val KEY_FIRST_LAUNCH = "first_launch_key"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed_key"
        const val KEY_LAST_UPDATE_SHOWN = "last_update_shown_key"
    }

    // === AUTHENTICATION ===
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

    // === APPEARANCE & THEMES ===
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
    
    fun isDynamicColorsEnabled(): Boolean = prefs.getBoolean(KEY_DYNAMIC_COLORS, true)
    fun setDynamicColorsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLORS, enabled).apply()
    }
    
    fun isHighContrastEnabled(): Boolean = prefs.getBoolean(KEY_HIGH_CONTRAST, false)
    fun setHighContrastEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HIGH_CONTRAST, enabled).apply()
    }
    
    fun getFontSize(): Int = prefs.getInt(KEY_FONT_SIZE, 100) // 100 = 100% (normal)
    fun setFontSize(size: Int) {
        prefs.edit().putInt(KEY_FONT_SIZE, size.coerceIn(80, 120)).apply()
    }
    
    fun getLayoutDensity(): LayoutDensity {
        val densityString = prefs.getString(KEY_LAYOUT_DENSITY, LayoutDensity.COMFORTABLE.name)
        return try {
            LayoutDensity.valueOf(densityString ?: LayoutDensity.COMFORTABLE.name)
        } catch (e: IllegalArgumentException) {
            LayoutDensity.COMFORTABLE
        }
    }
    
    fun setLayoutDensity(density: LayoutDensity) {
        prefs.edit().putString(KEY_LAYOUT_DENSITY, density.name).apply()
    }
    
    fun getCustomAccentColor(): Int = prefs.getInt(KEY_CUSTOM_ACCENT_COLOR, 0xFF6200EE.toInt())
    fun setCustomAccentColor(color: Int) {
        prefs.edit().putInt(KEY_CUSTOM_ACCENT_COLOR, color).apply()
    }

    // === AUDIO & HAPTICS ===
    fun isSoundEnabled(): Boolean = prefs.getBoolean(KEY_SOUND_ENABLED, true)
    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }
    
    fun isHapticEnabled(): Boolean = prefs.getBoolean(KEY_HAPTIC_ENABLED, true)
    fun setHapticEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC_ENABLED, enabled).apply()
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

    // === NAVIGATION & LAYOUT ===
    fun getNavigationStyle(): NavigationStyle {
        val styleString = prefs.getString(KEY_NAVIGATION_STYLE, NavigationStyle.AUTO.name)
        return try {
            NavigationStyle.valueOf(styleString ?: NavigationStyle.AUTO.name)
        } catch (e: IllegalArgumentException) {
            NavigationStyle.AUTO
        }
    }
    
    fun setNavigationStyle(style: NavigationStyle) {
        prefs.edit().putString(KEY_NAVIGATION_STYLE, style.name).apply()
    }
    
    fun getDashboardViewType(): DashboardViewType {
        val viewString = prefs.getString(KEY_DASHBOARD_VIEW_TYPE, DashboardViewType.GRID.name)
        return try {
            DashboardViewType.valueOf(viewString ?: DashboardViewType.GRID.name)
        } catch (e: IllegalArgumentException) {
            DashboardViewType.GRID
        }
    }
    
    fun setDashboardViewType(viewType: DashboardViewType) {
        prefs.edit().putString(KEY_DASHBOARD_VIEW_TYPE, viewType.name).apply()
    }
    
    fun getDefaultCardSize(): CardSize {
        val sizeString = prefs.getString(KEY_DEFAULT_CARD_SIZE, CardSize.MEDIUM.name)
        return try {
            CardSize.valueOf(sizeString ?: CardSize.MEDIUM.name)
        } catch (e: IllegalArgumentException) {
            CardSize.MEDIUM
        }
    }
    
    fun setDefaultCardSize(size: CardSize) {
        prefs.edit().putString(KEY_DEFAULT_CARD_SIZE, size.name).apply()
    }
    
    fun getDashboardColumns(): Int = prefs.getInt(KEY_DASHBOARD_COLUMNS, 2)
    fun setDashboardColumns(columns: Int) {
        prefs.edit().putInt(KEY_DASHBOARD_COLUMNS, columns.coerceIn(1, 4)).apply()
    }

    // === ACCESSIBILITY ===
    fun isReducedMotionEnabled(): Boolean = prefs.getBoolean(KEY_REDUCED_MOTION, false)
    fun setReducedMotionEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REDUCED_MOTION, enabled).apply()
    }
    
    fun isLargeTextEnabled(): Boolean = prefs.getBoolean(KEY_LARGE_TEXT, false)
    fun setLargeTextEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LARGE_TEXT, enabled).apply()
    }
    
    fun isScreenReaderMode(): Boolean = prefs.getBoolean(KEY_SCREEN_READER_MODE, false)
    fun setScreenReaderMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SCREEN_READER_MODE, enabled).apply()
    }

    // === DASHBOARD CUSTOMIZATION ===
    fun getHiddenModules(): Set<String> = prefs.getStringSet(KEY_HIDDEN_MODULES, emptySet()) ?: emptySet()
    fun setHiddenModules(modules: Set<String>) {
        prefs.edit().putStringSet(KEY_HIDDEN_MODULES, modules).apply()
    }
    
    fun hideModule(moduleName: String) {
        val currentHidden = getHiddenModules().toMutableSet()
        currentHidden.add(moduleName)
        setHiddenModules(currentHidden)
    }
    
    fun showModule(moduleName: String) {
        val currentHidden = getHiddenModules().toMutableSet()
        currentHidden.remove(moduleName)
        setHiddenModules(currentHidden)
    }
    
    fun getFavoriteModules(): Set<String> = prefs.getStringSet(KEY_FAVORITE_MODULES, emptySet()) ?: emptySet()
    fun setFavoriteModules(modules: Set<String>) {
        prefs.edit().putStringSet(KEY_FAVORITE_MODULES, modules).apply()
    }
    
    fun toggleFavoriteModule(moduleName: String) {
        val currentFavorites = getFavoriteModules().toMutableSet()
        if (currentFavorites.contains(moduleName)) {
            currentFavorites.remove(moduleName)
        } else {
            currentFavorites.add(moduleName)
        }
        setFavoriteModules(currentFavorites)
    }
    
    fun getModuleOrder(): List<String> {
        val orderString = prefs.getString(KEY_MODULE_ORDER, "")
        return if (orderString.isNullOrEmpty()) {
            emptyList()
        } else {
            orderString.split(",").filter { it.isNotEmpty() }
        }
    }
    
    fun setModuleOrder(modules: List<String>) {
        prefs.edit().putString(KEY_MODULE_ORDER, modules.joinToString(",")).apply()
    }

    // === PRIVACY & SECURITY ===
    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }
    
    fun getAutoLockTimeout(): Long = prefs.getLong(KEY_AUTO_LOCK_TIMEOUT, 300000L) // 5 minutes default
    fun setAutoLockTimeout(timeoutMs: Long) {
        prefs.edit().putLong(KEY_AUTO_LOCK_TIMEOUT, timeoutMs).apply()
    }
    
    fun areSecurityNotificationsEnabled(): Boolean = prefs.getBoolean(KEY_SECURITY_NOTIFICATIONS, true)
    fun setSecurityNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SECURITY_NOTIFICATIONS, enabled).apply()
    }

    // === PERFORMANCE & DEVELOPER ===
    fun isPerformanceModeEnabled(): Boolean = prefs.getBoolean(KEY_PERFORMANCE_MODE, false)
    fun setPerformanceModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PERFORMANCE_MODE, enabled).apply()
    }
    
    fun isDebugModeEnabled(): Boolean = prefs.getBoolean(KEY_DEBUG_MODE, false)
    fun setDebugModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DEBUG_MODE, enabled).apply()
    }
    
    fun isCrashReportingEnabled(): Boolean = prefs.getBoolean(KEY_CRASH_REPORTING, true)
    fun setCrashReportingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CRASH_REPORTING, enabled).apply()
    }

    // === USER EXPERIENCE ===
    fun isFirstLaunch(): Boolean = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
    fun setFirstLaunchCompleted() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }
    
    fun isOnboardingCompleted(): Boolean = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    fun setOnboardingCompleted() {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
    }
    
    fun getLastUpdateShown(): String = prefs.getString(KEY_LAST_UPDATE_SHOWN, "") ?: ""
    fun setLastUpdateShown(version: String) {
        prefs.edit().putString(KEY_LAST_UPDATE_SHOWN, version).apply()
    }

    // === UTILITY METHODS ===
    
    /**
     * Export all settings to a Map for backup purposes
     */
    fun exportSettings(): Map<String, Any?> {
        return prefs.all.toMap()
    }
    
    /**
     * Import settings from a Map (for restore purposes)
     * @param settings Map of setting keys to values
     * @param overrideExisting Whether to override existing settings
     */
    fun importSettings(settings: Map<String, Any?>, overrideExisting: Boolean = false) {
        val editor = prefs.edit()
        settings.forEach { (key, value) ->
            if (overrideExisting || !prefs.contains(key)) {
                when (value) {
                    is Boolean -> editor.putBoolean(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Float -> editor.putFloat(key, value)
                    is String -> editor.putString(key, value)
                    is Set<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        editor.putStringSet(key, value as Set<String>)
                    }
                }
            }
        }
        editor.apply()
    }
    
    /**
     * Reset all settings to defaults
     */
    fun resetToDefaults() {
        prefs.edit().clear().apply()
    }
    
    /**
     * Reset specific category of settings
     */
    fun resetCategory(category: SettingsCategory) {
        val editor = prefs.edit()
        when (category) {
            SettingsCategory.APPEARANCE -> {
                editor.remove(KEY_THEME_SETTING)
                editor.remove(KEY_DYNAMIC_COLORS)
                editor.remove(KEY_HIGH_CONTRAST)
                editor.remove(KEY_FONT_SIZE)
                editor.remove(KEY_LAYOUT_DENSITY)
                editor.remove(KEY_CUSTOM_ACCENT_COLOR)
            }
            SettingsCategory.AUDIO_HAPTICS -> {
                editor.remove(KEY_SOUND_ENABLED)
                editor.remove(KEY_HAPTIC_ENABLED)
                editor.remove(KEY_ANIMATION_SPEED)
            }
            SettingsCategory.NAVIGATION -> {
                editor.remove(KEY_NAVIGATION_STYLE)
                editor.remove(KEY_DASHBOARD_VIEW_TYPE)
                editor.remove(KEY_DEFAULT_CARD_SIZE)
                editor.remove(KEY_DASHBOARD_COLUMNS)
            }
            SettingsCategory.ACCESSIBILITY -> {
                editor.remove(KEY_REDUCED_MOTION)
                editor.remove(KEY_LARGE_TEXT)
                editor.remove(KEY_SCREEN_READER_MODE)
            }
            SettingsCategory.SECURITY -> {
                editor.remove(KEY_BIOMETRIC_ENABLED)
                editor.remove(KEY_AUTO_LOCK_TIMEOUT)
                editor.remove(KEY_SECURITY_NOTIFICATIONS)
            }
            SettingsCategory.DASHBOARD -> {
                editor.remove(KEY_HIDDEN_MODULES)
                editor.remove(KEY_MODULE_ORDER)
                editor.remove(KEY_FAVORITE_MODULES)
            }
        }
        editor.apply()
    }
}

// Settings Categories for reset functionality
enum class SettingsCategory {
    APPEARANCE,
    AUDIO_HAPTICS,
    NAVIGATION,
    ACCESSIBILITY,
    SECURITY,
    DASHBOARD
}