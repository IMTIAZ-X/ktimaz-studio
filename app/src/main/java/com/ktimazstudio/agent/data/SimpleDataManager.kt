package com.ktimazstudio.agent.data

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.security.KeyStore
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import com.google.gson.annotations.SerializedName

// ============================================
// DATA MANAGER - SharedPreferences
// ============================================

class SimpleDataManager private constructor(context: Context) {
    
    private val prefs: SharedPreferences
    private val gson = Gson()
    private val securityManager: SecurityManager
    
    companion object {
        @Volatile
        private var INSTANCE: SimpleDataManager? = null
        
        fun getInstance(context: Context): SimpleDataManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SimpleDataManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
    
    init {
        prefs = context.getSharedPreferences("agent_prefs", Context.MODE_PRIVATE)
        securityManager = SecurityManager.getInstance(context)
    }
    
    // Save API Configs
    fun saveApiConfigs(configs: List<ApiConfig>) {
        try {
            val configsToSave = configs.map { config ->
                mapOf(
                    "id" to config.id,
                    "provider" to config.provider.name,
                    "name" to config.name,
                    "isActive" to config.isActive,
                    "apiKey" to securityManager.encryptApiKey(config.apiKey),
                    "modelName" to config.modelName,
                    "baseUrl" to config.baseUrl,
                    "systemRole" to config.systemRole,
                    "createdAt" to config.createdAt
                )
            }
            val json = gson.toJson(configsToSave)
            prefs.edit().putString("api_configs", json).commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Load API Configs
    fun loadApiConfigs(): List<ApiConfig> {
        try {
            val json = prefs.getString("api_configs", null) ?: return emptyList()
            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
            val configMaps: List<Map<String, Any>> = gson.fromJson(json, type) ?: return emptyList()
            
            return configMaps.mapNotNull { map ->
                try {
                    ApiConfig(
                        id = map["id"] as? String ?: return@mapNotNull null,
                        provider = AiProvider.valueOf(map["provider"] as? String ?: "GEMINI"),
                        name = map["name"] as? String ?: "",
                        isActive = map["isActive"] as? Boolean ?: false,
                        apiKey = securityManager.decryptApiKey(map["apiKey"] as? String ?: ""),
                        modelName = map["modelName"] as? String ?: "",
                        baseUrl = map["baseUrl"] as? String ?: "",
                        systemRole = map["systemRole"] as? String ?: "",
                        createdAt = (map["createdAt"] as? Double)?.toLong() ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
    
    // Save Chat Sessions
    fun saveChatSessions(sessions: List<ChatSession>) {
        try {
            val sessionsToSave = sessions.map { session ->
                mapOf(
                    "id" to session.id,
                    "title" to session.title,
                    "timestamp" to session.timestamp,
                    "isPinned" to session.isPinned,
                    "activeApis" to session.activeApis.joinToString(","),
                    "messages" to session.messages.map { msg ->
                        mapOf(
                            "id" to msg.id,
                            "text" to msg.text,
                            "isUser" to msg.isUser,
                            "mode" to msg.mode.name,
                            "timestamp" to msg.timestamp,
                            "usedApis" to msg.usedApis.joinToString(","),
                            "isStreaming" to msg.isStreaming
                        )
                    }
                )
            }
            val json = gson.toJson(sessionsToSave)
            prefs.edit().putString("chat_sessions", json).commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Load Chat Sessions
    fun loadChatSessions(): List<ChatSession> {
        try {
            val json = prefs.getString("chat_sessions", null) ?: return emptyList()
            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
            val sessionMaps: List<Map<String, Any>> = gson.fromJson(json, type) ?: return emptyList()
            
            return sessionMaps.mapNotNull { map ->
                try {
                    val messagesList = map["messages"] as? List<Map<String, Any>> ?: emptyList()
                    val messages = messagesList.mapNotNull { msgMap ->
                        try {
                            ChatMessage(
                                id = msgMap["id"] as? String ?: return@mapNotNull null,
                                text = msgMap["text"] as? String ?: "",
                                isUser = msgMap["isUser"] as? Boolean ?: false,
                                mode = AiMode.valueOf(msgMap["mode"] as? String ?: "STANDARD"),
                                timestamp = (msgMap["timestamp"] as? Double)?.toLong() ?: System.currentTimeMillis(),
                                usedApis = (msgMap["usedApis"] as? String ?: "").split(",").filter { it.isNotBlank() },
                                isStreaming = msgMap["isStreaming"] as? Boolean ?: false
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }.toMutableList()
                    
                    ChatSession(
                        id = map["id"] as? String ?: return@mapNotNull null,
                        title = map["title"] as? String ?: "New Chat",
                        timestamp = (map["timestamp"] as? Double)?.toLong() ?: System.currentTimeMillis(),
                        isPinned = map["isPinned"] as? Boolean ?: false,
                        activeApis = (map["activeApis"] as? String ?: "").split(",").filter { it.isNotBlank() }.toMutableList(),
                        messages = messages
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
    
    // Save Settings
    fun saveSettings(settings: AppSettings) {
        try {
            prefs.edit().apply {
                putBoolean("is_pro_user", settings.isProUser)
                putBoolean("is_dark_theme", settings.isDarkTheme)
                putInt("token_usage", settings.tokenUsage)
                putFloat("estimated_cost", settings.estimatedCost.toFloat())
                commit()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Load Settings
    fun loadSettings(): AppSettings {
        return try {
            AppSettings(
                isProUser = prefs.getBoolean("is_pro_user", false),
                isDarkTheme = prefs.getBoolean("is_dark_theme", true),
                tokenUsage = prefs.getInt("token_usage", 0),
                estimatedCost = prefs.getFloat("estimated_cost", 0f).toDouble(),
                apiConfigs = emptyList()
            )
        } catch (e: Exception) {
            AppSettings()
        }
    }
    
    fun clearAll() {
        prefs.edit().clear().commit()
    }
}

// ============================================
// SECURITY MANAGER
// ============================================

class SecurityManager private constructor(context: Context) {
    
    companion object {
        private const val KEY_ALIAS = "agent_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val SEPARATOR = "|||"
        
        @Volatile
        private var INSTANCE: SecurityManager? = null
        
        fun getInstance(context: Context): SecurityManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SecurityManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
    
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    
    init {
        createKeyIfNotExists()
    }
    
    private fun createKeyIfNotExists() {
        try {
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
                val spec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
                keyGenerator.init(spec)
                keyGenerator.generateKey()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun encryptApiKey(plainText: String): String {
        if (plainText.isBlank()) return ""
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, keyStore.getKey(KEY_ALIAS, null) as SecretKey)
            val iv = cipher.iv
            val encrypted = cipher.doFinal(plainText.toByteArray())
            return Base64.encodeToString(iv, Base64.NO_WRAP) + SEPARATOR + Base64.encodeToString(encrypted, Base64.NO_WRAP)
        } catch (e: Exception) {
            return plainText
        }
    }
    
    fun decryptApiKey(encryptedText: String): String {
        if (encryptedText.isBlank() || !encryptedText.contains(SEPARATOR)) return encryptedText
        try {
            val parts = encryptedText.split(SEPARATOR)
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val encrypted = Base64.decode(parts[1], Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, keyStore.getKey(KEY_ALIAS, null) as SecretKey, GCMParameterSpec(128, iv))
            return String(cipher.doFinal(encrypted))
        } catch (e: Exception) {
            return encryptedText
        }
    }
}

// ============================================
// RETROFIT MODELS
// ============================================

data class UniversalAiRequest(
    val model: String,
    val messages: List<MessageContent>,
    @SerializedName("max_tokens") val maxTokens: Int = 1000,
    val temperature: Double = 0.7
)

data class MessageContent(val role: String, val content: String)

data class UniversalAiResponse(
    val choices: List<Choice>? = null,
    val error: ErrorDetail? = null
)

data class Choice(val message: MessageContent? = null)
data class ErrorDetail(val message: String)

data class GeminiRequest(val contents: List<GeminiContent>)
data class GeminiContent(val parts: List<GeminiPart>)
data class GeminiPart(val text: String)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiError? = null
)
data class GeminiCandidate(val content: GeminiContent)
data class GeminiError(val message: String)

// ============================================
// RETROFIT SERVICE
// ============================================

interface AiApiService {
    @POST("chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") auth: String,
        @Body request: UniversalAiRequest
    ): Response<UniversalAiResponse>
    
    @POST("models/{model}:generateContent")
    suspend fun geminiGenerate(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}

object AiApiClient {
    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    fun create(baseUrl: String): AiApiService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AiApiService::class.java)
    }
}

// ============================================
// AI PROVIDER HANDLER
// ============================================

class AiProviderHandler {
    suspend fun sendMessage(config: ApiConfig, message: String, systemRole: String = ""): Result<String> {
        return try {
            when (config.provider) {
                AiProvider.GEMINI -> handleGemini(config, message)
                else -> handleUniversal(config, message, systemRole)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun handleGemini(config: ApiConfig, message: String): Result<String> {
        return try {
            val service = AiApiClient.create(config.baseUrl)
            val request = GeminiRequest(listOf(GeminiContent(listOf(GeminiPart(message)))))
            val response = service.geminiGenerate(config.modelName, config.apiKey, request)
            
            if (response.isSuccessful) {
                val text = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (text != null) Result.success(text)
                else Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun handleUniversal(config: ApiConfig, message: String, systemRole: String): Result<String> {
        return try {
            val service = AiApiClient.create(config.baseUrl)
            val messages = mutableListOf<MessageContent>()
            if (systemRole.isNotBlank()) messages.add(MessageContent("system", systemRole))
            messages.add(MessageContent("user", message))
            
            val request = UniversalAiRequest(config.modelName, messages)
            val response = service.chatCompletion("Bearer ${config.apiKey}", request)
            
            if (response.isSuccessful) {
                val text = response.body()?.choices?.firstOrNull()?.message?.content
                if (text != null) Result.success(text)
                else Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}