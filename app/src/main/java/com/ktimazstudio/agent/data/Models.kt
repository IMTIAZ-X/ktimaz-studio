package com.ktimazstudio.agent.data

import android.net.Uri
import androidx.compose.ui.graphics.Color
import java.util.*

data class Attachment(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: AttachmentType,
    val uri: Uri? = null,
    val content: String = "",
    val size: Long = 0
) {
    val isImage: Boolean get() = type == AttachmentType.IMAGE
}

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
    val name: String,
    val isActive: Boolean = false,
    val apiKey: String = "",
    val modelName: String = "",
    val baseUrl: String = "",
    val systemRole: String = "",
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
    val defaultModel: String,
    val defaultUrl: String
) {
    GEMINI(
        "Google Gemini",
        "gemini-2.0-flash-exp",
        "https://generativelanguage.googleapis.com/v1beta"
    ),
    CHATGPT(
        "OpenAI ChatGPT",
        "gpt-4o",
        "https://api.openai.com/v1"
    ),
    CLAUDE(
        "Anthropic Claude",
        "claude-sonnet-4-20250514",
        "https://api.anthropic.com/v1"
    ),
    GROK(
        "Grok (X.AI)",
        "grok-2-latest",
        "https://api.x.ai/v1"
    ),
    DEEPSEEK(
        "DeepSeek",
        "deepseek-chat",
        "https://api.deepseek.com/v1"
    ),
    LOCAL_LLM(
        "Local LLM",
        "llama-3.1-8b",
        "http://localhost:1234/v1"
    );

    val color: Color
        get() = when (this) {
            GEMINI -> Color(0xFF4285F4)
            CHATGPT -> Color(0xFF10A37F)
            CLAUDE -> Color(0xFFCC785C)
            GROK -> Color(0xFF1DA1F2)
            DEEPSEEK -> Color(0xFF6366F1)
            LOCAL_LLM -> Color(0xFF8B5CF6)
        }
}