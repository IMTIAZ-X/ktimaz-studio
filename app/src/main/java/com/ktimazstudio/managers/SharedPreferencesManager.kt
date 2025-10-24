package com.ktimazstudio.managers

import android.content.Context
import android.content.SharedPreferences
import com.ktimazstudio.enums.ThemeSetting

// --- SharedPreferencesManager ---
/**
 * Manages user login status, username, theme settings, and sound settings
 * using SharedPreferences for persistent storage.
 */
class SharedPreferencesManager(context: Context) {
    val prefs: SharedPreferences = context.getSharedPreferences("AppPrefsKtimazStudio", Context.MODE_PRIVATE)
    private val context: Context = context // Keep context for theme checks

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in_key"
        private const val KEY_USERNAME = "username_key"
        const val KEY_THEME_SETTING = "theme_setting_key" // Made public
        const val KEY_SOUND_ENABLED = "sound_enabled_key" // Made public
    }

    /**
     * Checks if a user is currently logged in.
     * return true if a user is logged in, false otherwise.
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Sets the login status of the user. If logging in, the username is also stored.
     * If logging out, the username is removed.
     * @param loggedIn The new login status.
     * @param username The username to store if logging in. Null if logging out.
     */
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

    /**
     * Retrieves the username of the currently logged-in user.
     * return The username string, or null if no user is logged in.
     */
    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    /**
     * Retrieves the current theme setting.
     * return The ThemeSetting enum value. Defaults to SYSTEM.
     */
   /* 
   fun getThemeSetting(): ThemeSetting {
        val themeString = prefs.getString(KEY_THEME_SETTING, ThemeSetting.SYSTEM.name)
        return try {
            ThemeSetting.valueOf(themeString ?: ThemeSetting.SYSTEM.name)
        } catch (e: IllegalArgumentException) {
            ThemeSetting.SYSTEM // Fallback to system if stored value is invalid
        }
    }
    */

    /**
     * Sets the new theme setting.
     * @param themeSetting The ThemeSetting enum value to store.
     */
    fun setThemeSetting(themeSetting: ThemeSetting) {
        prefs.edit().putString(KEY_THEME_SETTING, themeSetting.name).apply()
    }

    /**
     * Checks if sound effects are enabled.
     * return true if sound is enabled, false otherwise. Defaults to true.
     */
    fun isSoundEnabled(): Boolean {
        return prefs.getBoolean(KEY_SOUND_ENABLED, true) // Default to true
    }

    /**
     * Sets the sound effects enabled status.
     * @param enabled The new sound status.
     */
    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }
}