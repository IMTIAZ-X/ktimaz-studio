package com.ktimazstudio.agent.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object PreferenceManager {
    private lateinit var prefs: SharedPreferences
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences("agent_prefs", Context.MODE_PRIVATE)
    }
    
    // Save Settings
    fun saveSettings(settings: AppSettings) {
        try {
            val json = Json.encodeToString(settings)
            prefs.edit().putString("app_settings", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Load Settings
    fun loadSettings(): AppSettings? {
        return try {
            val json = prefs.getString("app_settings", null) ?: return null
            Json.decodeFromString<AppSettings>(json)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    // Save API Configs
    fun saveApiConfigs(configs: List<ApiConfig>) {
        try {
            val json = Json.encodeToString(configs)
            prefs.edit().putString("api_configs", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Load API Configs
    fun loadApiConfigs(): List<ApiConfig>? {
        return try {
            val json = prefs.getString("api_configs", null) ?: return null
            Json.decodeFromString<List<ApiConfig>>(json)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    // Save Chat Sessions
    fun saveChatSessions(sessions: List<ChatSession>) {
        try {
            val json = Json.encodeToString(sessions)
            prefs.edit().putString("chat_sessions", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Load Chat Sessions
    fun loadChatSessions(): List<ChatSession>? {
        return try {
            val json = prefs.getString("chat_sessions", null) ?: return null
            Json.decodeFromString<List<ChatSession>>(json)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    // Clear All
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}