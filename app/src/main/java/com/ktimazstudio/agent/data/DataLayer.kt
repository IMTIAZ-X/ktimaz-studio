package com.ktimazstudio.agent.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.room.*
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
// FIX: Removed wildcard import to prevent @Query conflict
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import java.security.KeyStore
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

// ============================================
// ROOM DATABASE - ENTITIES
// ============================================

@Entity(tableName = "api_configs")
data class ApiConfigEntity(
    @PrimaryKey val id: String,
    val provider: String,
    val name: String,
    val isActive: Boolean,
    val encryptedApiKey: String,
    val modelName: String,
    val baseUrl: String,
    val systemRole: String,
    val createdAt: Long
)

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val timestamp: Long,
    val isPinned: Boolean,
    val activeApiIds: String
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val text: String,
    val isUser: Boolean,
    val mode: String,
    val timestamp: Long,
    val usedApiNames: String,
    val attachmentsJson: String
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val isProUser: Boolean,
    val isDarkTheme: Boolean,
    val tokenUsage: Int,
    val estimatedCost: Double
)

// ============================================
// ROOM DATABASE - DAOs
// ============================================

@Dao
interface ApiConfigDao {
    @Query("SELECT * FROM api_configs ORDER BY createdAt DESC")
    fun getAllConfigs(): Flow<List<ApiConfigEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: ApiConfigEntity)
    
    @Update
    suspend fun updateConfig(config: ApiConfigEntity)
    
    @Query("DELETE FROM api_configs WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("SELECT COUNT(*) FROM api_configs")
    suspend fun getCount(): Int
}

@Dao
interface ChatSessionDao {
    @Query("SELECT * FROM chat_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<ChatSessionEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity)
    
    @Update
    suspend fun updateSession(session: ChatSessionEntity)
    
    @Query("DELETE FROM chat_sessions WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)
    
    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)
}

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettings(): Flow<AppSettingsEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettingsEntity)
}

// ============================================
// ROOM DATABASE
// ============================================

@Database(
    entities = [
        ApiConfigEntity::class,
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AgentDatabase : RoomDatabase() {
    abstract fun apiConfigDao(): ApiConfigDao
    abstract fun chatSessionDao(): ChatSessionDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun appSettingsDao(): AppSettingsDao
    
    companion object {
        @Volatile
        private var INSTANCE: AgentDatabase? = null
        
        fun getInstance(context: Context): AgentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AgentDatabase::class.java,
                    "agent_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// ============================================
// SECURITY - API KEY ENCRYPTION
// ============================================

class SecurityManager(context: Context) {
    
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
            throw SecurityException("Encryption failed: ${e.message}")
        }
    }
    
    fun decryptApiKey(encryptedText: String): String {
        if (encryptedText.isBlank()) return ""
        
        try {
            val parts = encryptedText.split(IV_SEPARATOR)
            if (parts.size != 2) throw IllegalArgumentException("Invalid encrypted format")
            
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val encryptedBytes = Base64.decode(parts[1], Base64.NO_WRAP)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
            
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
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
    val temperature: Double = 0.7,
    val stream: Boolean = false
)

data class MessageContent(
    val role: String,
    val content: String
)

data class UniversalAiResponse(
    val id: String? = null,
    val choices: List<Choice>? = null,
    val usage: Usage? = null,
    val error: ErrorDetail? = null
)

data class Choice(
    val message: MessageContent? = null,
    val text: String? = null,
    @SerializedName("finish_reason")
    val finishReason: String? = null
)

data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int = 0,
    @SerializedName("completion_tokens")
    val completionTokens: Int = 0,
    @SerializedName("total_tokens")
    val totalTokens: Int = 0
)

data class ErrorDetail(
    val message: String,
    val type: String? = null
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
        // FIX: Use fully qualified name to avoid conflict with Room @Query
        @retrofit2.http.Query("key") apiKey: String,
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
    
    private suspend fun handleGemini(
        config: ApiConfig,
        message: String
    ): Result<String> {
        try {
            val service = AiApiClient.createService(config.baseUrl)
            
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = message))
                    )
                )
            )
            
            val response = service.geminiGenerate(
                model = config.modelName,
                apiKey = config.apiKey,
                request = request
            )
            
            if (response.isSuccessful) {
                val body = response.body()
                val text = body?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                return if (text != null) {
                    Result.success(text)
                } else {
                    Result.failure(Exception("Empty response from Gemini"))
                }
            } else {
                return Result.failure(Exception("Gemini API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
    
    private suspend fun handleUniversalApi(
        config: ApiConfig,
        message: String,
        systemRole: String
    ): Result<String> {
        try {
            val service = AiApiClient.createService(config.baseUrl)
            
            val messages = mutableListOf<MessageContent>()
            if (systemRole.isNotBlank()) {
                messages.add(MessageContent(role = "system", content = systemRole))
            }
            messages.add(MessageContent(role = "user", content = message))
            
            val request = UniversalAiRequest(
                model = config.modelName,
                messages = messages
            )
            
            val response = service.chatCompletion(
                authorization = "Bearer ${config.apiKey}",
                request = request
            )
            
            if (response.isSuccessful) {
                val body = response.body()
                val text = body?.choices?.firstOrNull()?.message?.content
                return if (text != null) {
                    Result.success(text)
                } else {
                    Result.failure(Exception("Empty response from API"))
                }
            } else {
                return Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}

// ============================================
// REPOSITORY
// ============================================

class AgentRepository(
    private val database: AgentDatabase,
    private val securityManager: SecurityManager
) {
    private val gson = Gson()
    
    companion object {
        @Volatile
        private var INSTANCE: AgentRepository? = null
        
        fun getInstance(context: Context): AgentRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = AgentRepository(
                    AgentDatabase.getInstance(context),
                    SecurityManager.getInstance(context)
                )
                INSTANCE = instance
                instance
            }
        }
    }
    
    // API Configs
    fun getAllApiConfigs(): Flow<List<ApiConfig>> {
        return database.apiConfigDao().getAllConfigs().map { entities ->
            entities.map { entity ->
                ApiConfig(
                    id = entity.id,
                    provider = AiProvider.valueOf(entity.provider),
                    name = entity.name,
                    isActive = entity.isActive,
                    apiKey = try {
                        securityManager.decryptApiKey(entity.encryptedApiKey)
                    } catch (e: Exception) {
                        ""
                    },
                    modelName = entity.modelName,
                    baseUrl = entity.baseUrl,
                    systemRole = entity.systemRole,
                    createdAt = entity.createdAt
                )
            }
        }
    }
    
    suspend fun insertApiConfig(config: ApiConfig) {
        val entity = ApiConfigEntity(
            id = config.id,
            provider = config.provider.name,
            name = config.name,
            isActive = config.isActive,
            encryptedApiKey = securityManager.encryptApiKey(config.apiKey),
            modelName = config.modelName,
            baseUrl = config.baseUrl,
            systemRole = config.systemRole,
            createdAt = config.createdAt
        )
        database.apiConfigDao().insertConfig(entity)
    }
    
    suspend fun updateApiConfig(config: ApiConfig) {
        val entity = ApiConfigEntity(
            id = config.id,
            provider = config.provider.name,
            name = config.name,
            isActive = config.isActive,
            encryptedApiKey = securityManager.encryptApiKey(config.apiKey),
            modelName = config.modelName,
            baseUrl = config.baseUrl,
            systemRole = config.systemRole,
            createdAt = config.createdAt
        )
        database.apiConfigDao().updateConfig(entity)
    }
    
    suspend fun deleteApiConfig(id: String) {
        database.apiConfigDao().deleteById(id)
    }
    
    suspend fun getApiConfigCount(): Int {
        return database.apiConfigDao().getCount()
    }
    
    // Chat Sessions
    fun getAllChatSessions(): Flow<List<ChatSession>> {
        return database.chatSessionDao().getAllSessions().map { entities ->
            entities.map { entity ->
                ChatSession(
                    id = entity.id,
                    title = entity.title,
                    timestamp = entity.timestamp,
                    isPinned = entity.isPinned,
                    activeApis = entity.activeApiIds.split(",").filter { it.isNotBlank() }.toMutableList()
                )
            }
        }
    }
    
    suspend fun insertChatSession(session: ChatSession) {
        val entity = ChatSessionEntity(
            id = session.id,
            title = session.title,
            timestamp = session.timestamp,
            isPinned = session.isPinned,
            activeApiIds = session.activeApis.joinToString(",")
        )
        database.chatSessionDao().insertSession(entity)
    }
    
    suspend fun updateChatSession(session: ChatSession) {
        val entity = ChatSessionEntity(
            id = session.id,
            title = session.title,
            timestamp = session.timestamp,
            isPinned = session.isPinned,
            activeApiIds = session.activeApis.joinToString(",")
        )
        database.chatSessionDao().updateSession(entity)
    }
    
    suspend fun deleteChatSession(id: String) {
        database.chatMessageDao().deleteMessagesForSession(id)
        database.chatSessionDao().deleteById(id)
    }
    
    // Messages
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>> {
        return database.chatMessageDao().getMessagesForSession(sessionId).map { entities ->
            entities.map { entity ->
                ChatMessage(
                    id = entity.id,
                    text = entity.text,
                    isUser = entity.isUser,
                    mode = AiMode.valueOf(entity.mode),
                    timestamp = entity.timestamp,
                    usedApis = entity.usedApiNames.split(",").filter { it.isNotBlank() },
                    attachments = try {
                        gson.fromJson(entity.attachmentsJson, Array<Attachment>::class.java).toList()
                    } catch (e: Exception) {
                        emptyList()
                    }
                )
            }
        }
    }
    
    suspend fun insertMessage(sessionId: String, message: ChatMessage) {
        val entity = ChatMessageEntity(
            id = message.id,
            sessionId = sessionId,
            text = message.text,
            isUser = message.isUser,
            mode = message.mode.name,
            timestamp = message.timestamp,
            usedApiNames = message.usedApis.joinToString(","),
            attachmentsJson = gson.toJson(message.attachments)
        )
        database.chatMessageDao().insertMessage(entity)
    }
    
    // App Settings
    fun getAppSettings(): Flow<AppSettings?> {
        return database.appSettingsDao().getSettings().map { entity ->
            entity?.let {
                AppSettings(
                    isProUser = it.isProUser,
                    isDarkTheme = it.isDarkTheme,
                    tokenUsage = it.tokenUsage,
                    estimatedCost = it.estimatedCost,
                    apiConfigs = emptyList()
                )
            }
        }
    }
    
    suspend fun saveAppSettings(settings: AppSettings) {
        val entity = AppSettingsEntity(
            id = 1,
            isProUser = settings.isProUser,
            isDarkTheme = settings.isDarkTheme,
            tokenUsage = settings.tokenUsage,
            estimatedCost = settings.estimatedCost
        )
        database.appSettingsDao().insertSettings(entity)
    }
}
