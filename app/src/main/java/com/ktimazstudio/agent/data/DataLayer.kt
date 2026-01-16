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
import retrofit2.http.*
import java.security.KeyStore
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

// ============================================
// 1. ENUMS & UI MODELS (এগুলো না থাকলে MissingType এরর হয়)
// ============================================

enum class AiProvider { GEMINI, OPENAI, ANTHROPIC, GROQ, OLLAMA, CUSTOM }
enum class AiMode { CHAT, RESEARCH, CODE, IMAGE }

data class Attachment(val type: String, val content: String)

data class ApiConfig(
    val id: String,
    val provider: AiProvider,
    val name: String,
    val isActive: Boolean,
    val apiKey: String,
    val modelName: String,
    val baseUrl: String,
    val systemRole: String,
    val createdAt: Long
)

data class ChatSession(
    val id: String,
    val title: String,
    val messages: MutableList<ChatMessage> = mutableListOf(),
    val timestamp: Long,
    val isPinned: Boolean,
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
    val estimatedCost: Double = 0.0,
    val apiConfigs: List<ApiConfig> = emptyList()
)

// ============================================
// 2. ROOM ENTITIES
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
// 3. DAOs (FIXED: returning Long/Int for KSP)
// ============================================

@Dao
interface ApiConfigDao {
    @Query("SELECT * FROM api_configs ORDER BY createdAt DESC")
    fun getAllConfigs(): Flow<List<ApiConfigEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: ApiConfigEntity): Long
    
    @Update
    suspend fun updateConfig(config: ApiConfigEntity): Int
    
    @Query("DELETE FROM api_configs WHERE id = :id")
    suspend fun deleteById(id: String): Int
    
    @Query("SELECT COUNT(*) FROM api_configs")
    suspend fun getCount(): Int
}

@Dao
interface ChatSessionDao {
    @Query("SELECT * FROM chat_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<ChatSessionEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity): Long
    
    @Update
    suspend fun updateSession(session: ChatSessionEntity): Int
    
    @Query("DELETE FROM chat_sessions WHERE id = :id")
    suspend fun deleteById(id: String): Int
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long
    
    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String): Int
}

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettings(): Flow<AppSettingsEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettingsEntity): Long
}

// ============================================
// 4. DATABASE & SECURITY
// ============================================

@Database(entities = [ApiConfigEntity::class, ChatSessionEntity::class, ChatMessageEntity::class, AppSettingsEntity::class], version = 1, exportSchema = false)
abstract class AgentDatabase : RoomDatabase() {
    abstract fun apiConfigDao(): ApiConfigDao
    abstract fun chatSessionDao(): ChatSessionDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile private var instance: AgentDatabase? = null
        fun getInstance(context: Context): AgentDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AgentDatabase::class.java, "agent_v3.db")
                .fallbackToDestructiveMigration().build().also { instance = it }
        }
    }
}

class SecurityManager private constructor(context: Context) {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val alias = "AiAgentKeyV4"

    init {
        if (!keyStore.containsAlias(alias)) {
            val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            keyGen.init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
            keyGen.generateKey()
        }
    }

    fun encrypt(text: String): String {
        if (text.isEmpty()) return ""
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keyStore.getKey(alias, null) as SecretKey)
        return Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" + Base64.encodeToString(cipher.doFinal(text.toByteArray()), Base64.NO_WRAP)
    }

    fun decrypt(data: String): String = try {
        val parts = data.split(":")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, keyStore.getKey(alias, null) as SecretKey, GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP)))
        String(cipher.doFinal(Base64.decode(parts[1], Base64.NO_WRAP)))
    } catch (e: Exception) { "" }

    companion object {
        fun getInstance(context: Context) = SecurityManager(context)
    }
}

// ============================================
// 5. REPOSITORY & NETWORK
// ============================================

class AgentRepository(context: Context) {
    private val db = AgentDatabase.getInstance(context)
    private val security = SecurityManager.getInstance(context)
    private val gson = Gson()

    fun getConfigs() = db.apiConfigDao().getAllConfigs().map { entities ->
        entities.map { it.toModel(security.decrypt(it.encryptedApiKey)) }
    }

    suspend fun saveConfig(c: ApiConfig) = db.apiConfigDao().insertConfig(c.toEntity(security.encrypt(c.apiKey)))
    
    suspend fun deleteConfig(id: String) = db.apiConfigDao().deleteById(id)

    fun getSessions() = db.chatSessionDao().getAllSessions().map { entities -> entities.map { it.toModel() } }
    
    suspend fun saveSession(s: ChatSession) = db.chatSessionDao().insertSession(s.toEntity())

    fun getMessages(sid: String) = db.chatMessageDao().getMessagesForSession(sid).map { entities -> entities.map { it.toModel(gson) } }
    
    suspend fun saveMessage(sid: String, m: ChatMessage) = db.chatMessageDao().insertMessage(m.toEntity(sid, gson))

    fun getSettings() = db.appSettingsDao().getSettings().map { it?.toModel() }
    
    suspend fun saveSettings(s: AppSettings) = db.appSettingsDao().insertSettings(s.toEntity())
}

// ============================================
// 6. MAPPING EXTENSIONS
// ============================================

fun ApiConfigEntity.toModel(key: String) = ApiConfig(id, AiProvider.valueOf(provider), name, isActive, key, modelName, baseUrl, systemRole, createdAt)
fun ApiConfig.toEntity(enc: String) = ApiConfigEntity(id, provider.name, name, isActive, enc, modelName, baseUrl, systemRole, createdAt)

fun ChatSessionEntity.toModel() = ChatSession(id, title, mutableListOf(), timestamp, isPinned, activeApiIds.split(",").filter { it.isNotBlank() }.toMutableList())
fun ChatSession.toEntity() = ChatSessionEntity(id, title, timestamp, isPinned, activeApis.joinToString(","))

fun ChatMessageEntity.toModel(gson: Gson) = ChatMessage(id, text, isUser, emptyList(), AiMode.valueOf(mode), false, timestamp, usedApiNames.split(",").filter { it.isNotBlank() })
fun ChatMessage.toEntity(sid: String, gson: Gson) = ChatMessageEntity(id, sid, text, isUser, mode.name, timestamp, usedApis.joinToString(","), gson.toJson(attachments))

fun AppSettingsEntity.toModel() = AppSettings(isProUser, isDarkTheme, tokenUsage, estimatedCost)
fun AppSettings.toEntity() = AppSettingsEntity(1, isProUser, isDarkTheme, tokenUsage, estimatedCost)
