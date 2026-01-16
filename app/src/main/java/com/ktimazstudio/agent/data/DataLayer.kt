package com.ktimazstudio.agent.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.room.*
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
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

// ============================================
// 1. ENUMS & DATA MODELS
// ============================================

enum class AiProvider { GEMINI, OPENAI, ANTHROPIC, GROQ, OLLAMA, CUSTOM }
enum class AiMode { CHAT, RESEARCH, CODE, IMAGE }

data class Attachment(
    val type: String,
    val content: String,
    val mimeType: String? = null
)

data class ApiConfig(
    val id: String,
    val provider: AiProvider,
    val name: String,
    val isActive: Boolean,
    val apiKey: String,
    val modelName: String,
    val baseUrl: String,
    val systemRole: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class ChatSession(
    val id: String,
    val title: String,
    val messages: MutableList<ChatMessage> = mutableListOf(),
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val activeApis: MutableList<String> = mutableListOf()
)

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val attachments: List<Attachment> = emptyList(),
    val mode: AiMode = AiMode.CHAT,
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val usedApis: List<String> = emptyList()
)

data class AppSettings(
    val isProUser: Boolean = false,
    val isDarkTheme: Boolean = true,
    val tokenUsage: Int = 0,
    val estimatedCost: Double = 0.0
)

// ============================================
// 2. ROOM DATABASE ENTITIES
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
// 3. DAOs (Advanced & KAPT Optimized)
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
// 4. DATABASE INSTANCE
// ============================================

@Database(
    entities = [ApiConfigEntity::class, ChatSessionEntity::class, ChatMessageEntity::class, AppSettingsEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AgentDatabase : RoomDatabase() {
    abstract fun apiConfigDao(): ApiConfigDao
    abstract fun chatSessionDao(): ChatSessionDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile private var instance: AgentDatabase? = null
        fun getInstance(context: Context): AgentDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AgentDatabase::class.java,
                    "agent_v2_core.db"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
        }
    }
}

// ============================================
// 5. ADVANCED SECURITY (AES-GCM)
// ============================================

class SecurityManager private constructor(context: Context) {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val alias = "AiAgentMasterKeyV2"

    init {
        if (!keyStore.containsAlias(alias)) {
            val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            keyGen.init(
                KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            )
            keyGen.generateKey()
        }
    }

    fun encrypt(text: String): String {
        if (text.isBlank()) return ""
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keyStore.getKey(alias, null) as SecretKey)
        val iv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
        val encrypted = Base64.encodeToString(cipher.doFinal(text.toByteArray()), Base64.NO_WRAP)
        return "$iv:$encrypted"
    }

    fun decrypt(encryptedData: String): String = try {
        val parts = encryptedData.split(":")
        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val data = Base64.decode(parts[1], Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, keyStore.getKey(alias, null) as SecretKey, GCMParameterSpec(128, iv))
        String(cipher.doFinal(data))
    } catch (e: Exception) { "" }

    companion object {
        @Volatile private var instance: SecurityManager? = null
        fun getInstance(context: Context) = instance ?: synchronized(this) {
            instance ?: SecurityManager(context).also { instance = it }
        }
    }
}

// ============================================
// 6. MODERN NETWORK LAYER (RETROFIT)
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
    val usage: Usage? = null
)

data class Choice(val message: MessageContent? = null)
data class Usage(@SerializedName("total_tokens") val totalTokens: Int = 0)

data class GeminiRequest(val contents: List<GeminiContent>)
data class GeminiContent(val parts: List<GeminiPart>)
data class GeminiPart(val text: String)
data class GeminiResponse(val candidates: List<GeminiCandidate>? = null)
data class GeminiCandidate(val content: GeminiContent)

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
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun create(baseUrl: String): AiApiService = Retrofit.Builder()
        .baseUrl(baseUrl.ifBlank { "https://api.openai.com/v1/" })
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AiApiService::class.java)
}

// ============================================
// 7. CLEAN ARCHITECTURE REPOSITORY
// ============================================

class AgentRepository(context: Context) {
    private val db = AgentDatabase.getInstance(context)
    private val security = SecurityManager.getInstance(context)
    private val gson = Gson()

    // API Config Methods
    fun getConfigs(): Flow<List<ApiConfig>> = db.apiConfigDao().getAllConfigs().map { entities ->
        entities.map { it.toModel(security.decrypt(it.encryptedApiKey)) }
    }

    suspend fun saveConfig(config: ApiConfig) = withContext(Dispatchers.IO) {
        db.apiConfigDao().insertConfig(config.toEntity(security.encrypt(config.apiKey)))
    }

    suspend fun deleteConfig(id: String) = withContext(Dispatchers.IO) {
        db.apiConfigDao().deleteById(id)
    }

    // Chat Methods
    fun getSessions(): Flow<List<ChatSession>> = db.chatSessionDao().getAllSessions().map { entities ->
        entities.map { it.toModel() }
    }

    suspend fun saveSession(session: ChatSession) = withContext(Dispatchers.IO) {
        db.chatSessionDao().insertSession(session.toEntity())
    }

    fun getMessages(sessionId: String): Flow<List<ChatMessage>> = 
        db.chatMessageDao().getMessagesForSession(sessionId).map { entities ->
            entities.map { it.toModel(gson) }
        }

    suspend fun saveMessage(sessionId: String, msg: ChatMessage) = withContext(Dispatchers.IO) {
        db.chatMessageDao().insertMessage(msg.toEntity(sessionId, gson))
    }

    // App Settings
    fun getSettings(): Flow<AppSettings?> = db.appSettingsDao().getSettings().map { it?.toModel() }
    
    suspend fun saveSettings(s: AppSettings) = withContext(Dispatchers.IO) {
        db.appSettingsDao().insertSettings(s.toEntity())
    }
}

// ============================================
// 8. DATA MAPPERS (EXTENSIONS)
// ============================================

fun ApiConfigEntity.toModel(key: String) = ApiConfig(id, AiProvider.valueOf(provider), name, isActive, key, modelName, baseUrl, systemRole, createdAt)
fun ApiConfig.toEntity(encKey: String) = ApiConfigEntity(id, provider.name, name, isActive, encKey, modelName, baseUrl, systemRole, createdAt)

fun ChatSessionEntity.toModel() = ChatSession(id, title, mutableListOf(), timestamp, isPinned, activeApiIds.split(",").filter { it.isNotBlank() }.toMutableList())
fun ChatSession.toEntity() = ChatSessionEntity(id, title, timestamp, isPinned, activeApis.joinToString(","))

fun ChatMessageEntity.toModel(gson: Gson) = ChatMessage(
    id = id, text = text, isUser = isUser, 
    mode = AiMode.valueOf(mode), timestamp = timestamp, 
    usedApis = usedApiNames.split(",").filter { it.isNotBlank() },
    attachments = try { gson.fromJson(attachmentsJson, Array<Attachment>::class.java).toList() } catch (e: Exception) { emptyList() }
)
fun ChatMessage.toEntity(sid: String, gson: Gson) = ChatMessageEntity(id, sid, text, isUser, mode.name, timestamp, usedApis.joinToString(","), gson.toJson(attachments))

fun AppSettingsEntity.toModel() = AppSettings(isProUser, isDarkTheme, tokenUsage, estimatedCost)
fun AppSettings.toEntity() = AppSettingsEntity(1, isProUser, isDarkTheme, tokenUsage, estimatedCost)
