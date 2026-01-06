package com.ktimazstudio.agent.data

import android.net.Uri
import androidx.compose.ui.graphics.Color
import java.util.*

// ═══════════════════════════════════════════════════════════════════════════════
// DATA MODELS
// ═══════════════════════════════════════════════════════════════════════════════

data class Attachment(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: AttachmentType,
    val uri: Uri? = null,
    val content: String = "",
    val size: Long = 0,
    val isImage: Boolean = type == AttachmentType.IMAGE
)

enum class AttachmentType {
    IMAGE, TEXT, CODE, JSON, PDF, DOCUMENT, UNKNOWN
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val attachments: List<Attachment> = emptyList(),
    val mode: AiMode = AiMode.STANDARD,
    val isStreaming: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val usedApis: List<String> = emptyList()
)

data class ChatSession(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    val messages: MutableList<ChatMessage> = mutableListOf(),
    val timestamp: Long = System.currentTimeMillis(),
    var isPinned: Boolean = false,
    val activeApis: MutableList<String> = mutableListOf()
) {
    val messageCount: Int get() = messages.size
    val lastMessage: String get() = messages.lastOrNull()?.text ?: "New conversation"
}

data class ApiConfig(
    val id: String = UUID.randomUUID().toString(),
    val provider: AiProvider,
    var name: String,
    var isActive: Boolean = false,
    var apiKey: String = "",
    var modelName: String = "",
    var baseUrl: String = "",
    var systemRole: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class AppSettings(
    val isProUser: Boolean = false,
    val isDarkTheme: Boolean = true,
    val tokenUsage: Int = 0,
    val estimatedCost: Double = 0.0,
    val apiConfigs: List<ApiConfig> = emptyList()
)

enum class AiMode(
    val title: String,
    val promptTag: String,
    val icon: String,
    val isPro: Boolean = false
) {
    STANDARD("Standard", "", "💬"),
    THINKING("Thinking", "[THINKING]", "🧠", true),
    RESEARCH("Research", "[RESEARCH]", "🔬", true),
    STUDY("Study", "[STUDY]", "📚", true),
    CODE("Code", "[CODE]", "💻", true),
    CREATIVE("Creative", "[CREATIVE]", "✨", true)
}

enum class AiProvider(
    val title: String,
    val color: Color,
    val defaultModel: String,
    val defaultUrl: String
) {
    GEMINI(
        "Google Gemini",
        Color(0xFF4285F4),
        "gemini-2.0-flash-exp",
        "https://generativelanguage.googleapis.com/v1beta"
    ),
    CHATGPT(
        "OpenAI ChatGPT",
        Color(0xFF10A37F),
        "gpt-4o",
        "https://api.openai.com/v1"
    ),
    CLAUDE(
        "Anthropic Claude",
        Color(0xFFCC785C),
        "claude-sonnet-4-20250514",
        "https://api.anthropic.com/v1"
    ),
    GROK(
        "Grok (X.AI)",
        Color(0xFF000000),
        "grok-2-latest",
        "https://api.x.ai/v1"
    ),
    DEEPSEEK(
        "DeepSeek",
        Color(0xFF6366F1),
        "deepseek-chat",
        "https://api.deepseek.com/v1"
    ),
    LOCAL_LLM(
        "Local LLM",
        Color(0xFF8B5CF6),
        "llama-3.1-8b",
        "http://localhost:1234/v1"
    )
}
