package com.ktimazstudio.agent.data

import android.content.Context
import android.content.SharedPreferences

object PreferenceManager {
    private lateinit var prefs: SharedPreferences
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences("agent_prefs", Context.MODE_PRIVATE)
    }
    
    fun saveSettings(settings: AppSettings) {
        prefs.edit().apply {
            putBoolean("is_pro_user", settings.isProUser)
            putBoolean("is_dark_theme", settings.isDarkTheme)
            putInt("token_usage", settings.tokenUsage)
            putFloat("estimated_cost", settings.estimatedCost.toFloat())
            apply()
        }
    }
    
    fun loadSettings(): AppSettings {
        return AppSettings(
            isProUser = prefs.getBoolean("is_pro_user", false),
            isDarkTheme = prefs.getBoolean("is_dark_theme", true),
            tokenUsage = prefs.getInt("token_usage", 0),
            estimatedCost = prefs.getFloat("estimated_cost", 0f).toDouble(),
            apiConfigs = emptyList()
        )
    }
    
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}