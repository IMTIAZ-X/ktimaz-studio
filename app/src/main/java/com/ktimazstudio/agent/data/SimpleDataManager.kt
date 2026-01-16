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
// SIMPLE PERSISTENCE - SharedPreferences
// ============================================

class SimpleDataManager private constructor(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("agent_data", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val securityManager = SecurityManager.getInstance(context)
    
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
    
    // Save API Configs
    fun saveApiConfigs(configs: List<ApiConfig>) {
        val encryptedConfigs = configs.map { config ->
            config.copy(apiKey = securityManager.encryptApiKey(config.apiKey))
        }
        val json = gson.toJson(encryptedConfigs)
        prefs.edit().putString("api_configs", json).apply()
    }
    
    // Load API Configs
    fun loadApiConfigs(): List<ApiConfig> {
        val json = prefs.getString("api_configs", null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<ApiConfig>>() {}.type
            val configs: List<ApiConfig> = gson.fromJson(json, type)
            configs.map { config ->
                config.copy(apiKey = securityManager.decryptApiKey(config.apiKey))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Save Chat Sessions
    fun saveChatSessions(sessions: List<ChatSession>) {
        val json = gson.toJson(sessions)
        prefs.edit().putString("chat_sessions", json).apply()
    }
    
    // Load Chat Sessions
    fun loadChatSessions(): List<ChatSession> {
        val json = prefs.getString("chat_sessions", null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<ChatSession>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Save App Settings
    fun saveSettings(settings: AppSettings) {
        prefs.edit().apply {
            putBoolean("is_pro_user", settings.isProUser)
            putBoolean("is_dark_theme", settings.isDarkTheme)
            putInt("token_usage", settings.tokenUsage)
            putFloat("estimated_cost", settings.estimatedCost.toFloat())
            apply()
        }
    }
    
    // Load App Settings
    fun loadSettings(): AppSettings {
        return AppSettings(
            isProUser = prefs.getBoolean("is_pro_user", false),
            isDarkTheme = prefs.getBoolean("is_dark_theme", true),
            tokenUsage = prefs.getInt("token_usage", 0),
            estimatedCost = prefs.getFloat("estimated_cost", 0f).toDouble(),
            apiConfigs = emptyList()
        )
    }
    
    // Clear all data
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}

// ============================================
// SECURITY - API KEY ENCRYPTION
// ============================================

class SecurityManager private constructor(context: Context) {
    
    companion object {
        private const val KEY_ALIAS = "agent_api_key_alias"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_SEPARATOR = "]]]"
        
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
    
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }
    
    init {
        createKeyIfNotExists()
    }
    
    private fun createKeyIfNotExists() {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )
            
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }
    
    private fun getSecretKey(): SecretKey {
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }
    
    fun encryptApiKey(plainText: String): String {
        if (plainText.isBlank()) return ""
        
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            
            return Base64.encodeToString(iv, Base64.NO_WRAP) + 
                   IV_SEPARATOR + 
                   Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            return plainText // Fallback to plaintext if encryption fails
        }
    }
    
    fun decryptApiKey(encryptedText: String): String {
        if (encryptedText.isBlank()) return ""
        if (!encryptedText.contains(IV_SEPARATOR)) return encryptedText // Already plaintext
        
        try {
            val parts = encryptedText.split(IV_SEPARATOR)
            if (parts.size != 2) return encryptedText
            
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val encryptedBytes = Base64.decode(parts[1], Base64.NO_WRAP)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
            
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            return encryptedText // Return as-is if decryption fails
        }
    }
}

// ============================================
// RETROFIT - NETWORK MODELS
// ============================================

data class UniversalAiRequest(
    val model: String,
    val messages: List<MessageContent>,
    @SerializedName("max_tokens")
    val maxTokens: Int = 1000,
    val temperature: Double = 0.7
)

data class MessageContent(
    val role: String,
    val content: String
)

data class UniversalAiResponse(
    val id: String? = null,
    val choices: List<Choice>? = null,
    val error: ErrorDetail? = null
)

data class Choice(
    val message: MessageContent? = null,
    @SerializedName("finish_reason")
    val finishReason: String? = null
)

data class ErrorDetail(
    val message: String
)

data class GeminiRequest(
    val contents: List<GeminiContent>
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiError? = null
)

data class GeminiCandidate(
    val content: GeminiContent
)

data class GeminiError(
    val message: String
)

// ============================================
// RETROFIT - API SERVICE
// ============================================

interface AiApiService {
    
    @POST("chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authorization: String,
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
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    fun createService(baseUrl: String): AiApiService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AiApiService::class.java)
    }
}

// ============================================
// AI PROVIDER HANDLER
// ============================================

class AiProviderHandler {
    
    suspend fun sendMessage(
        apiConfig: ApiConfig,
        userMessage: String,
        systemRole: String = ""
    ): Result<String> {
        return try {
            when (apiConfig.provider) {
                AiProvider.GEMINI -> handleGemini(apiConfig, userMessage)
                else -> handleUniversalApi(apiConfig, userMessage, systemRole)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun handleGemini(config: ApiConfig, message: String): Result<String> {
        try {
            val service = AiApiClient.createService(config.baseUrl)
            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = message))))
            )
            
            val response = service.geminiGenerate(config.modelName, config.apiKey, request)
            
            if (response.isSuccessful) {
                val text = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                return if (text != null) Result.success(text)
                else Result.failure(Exception("Empty response from Gemini"))
            } else {
                return Result.failure(Exception("Gemini API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
    
    private suspend fun handleUniversalApi(config: ApiConfig, message: String, systemRole: String): Result<String> {
        try {
            val service = AiApiClient.createService(config.baseUrl)
            val messages = mutableListOf<MessageContent>()
            if (systemRole.isNotBlank()) {
                messages.add(MessageContent(role = "system", content = systemRole))
            }
            messages.add(MessageContent(role = "user", content = message))
            
            val request = UniversalAiRequest(model = config.modelName, messages = messages)
            val response = service.chatCompletion("Bearer ${config.apiKey}", request)
            
            if (response.isSuccessful) {
                val text = response.body()?.choices?.firstOrNull()?.message?.content
                return if (text != null) Result.success(text)
                else Result.failure(Exception("Empty response"))
            } else {
                return Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
