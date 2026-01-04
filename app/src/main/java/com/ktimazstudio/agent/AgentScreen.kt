package com.ktimazstudio.agent

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
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
    val usedApis: List<String> = emptyList() // Track which APIs were used
)

data class ChatSession(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    val messages: MutableList<ChatMessage> = mutableListOf(),
    val timestamp: Long = System.currentTimeMillis(),
    var isPinned: Boolean = false,
    val activeApis: MutableList<String> = mutableListOf() // Track active APIs for this chat (max 5)
) {
    val messageCount: Int get() = messages.size
    val lastMessage: String get() = messages.lastOrNull()?.text ?: "New conversation"
}

data class ApiConfig(
    val id: String = UUID.randomUUID().toString(),
    val provider: AiProvider,
    var name: String, // Custom name like "My GPT-4", "Work Gemini"
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

enum class AiMode(val title: String, val promptTag: String, val icon: String, val isPro: Boolean = false) {
    STANDARD("Standard", "", "💬"),
    THINKING("Thinking", "[THINKING]", "🧠", true),
    RESEARCH("Research", "[RESEARCH]", "🔬", true),
    STUDY("Study", "[STUDY]", "📚", true),
    CODE("Code", "[CODE]", "💻", true),
    CREATIVE("Creative", "[CREATIVE]", "✨", true)
}

enum class AiProvider(val title: String, val color: Color, val defaultModel: String, val defaultUrl: String) {
    GEMINI("Google Gemini", Color(0xFF4285F4), "gemini-2.0-flash-exp", "https://generativelanguage.googleapis.com/v1beta"),
    CHATGPT("OpenAI ChatGPT", Color(0xFF10A37F), "gpt-4o", "https://api.openai.com/v1"),
    CLAUDE("Anthropic Claude", Color(0xFFCC785C), "claude-sonnet-4-20250514", "https://api.anthropic.com/v1"),
    GROK("Grok (X.AI)", Color(0xFF000000), "grok-2-latest", "https://api.x.ai/v1"),
    DEEPSEEK("DeepSeek", Color(0xFF6366F1), "deepseek-chat", "https://api.deepseek.com/v1"),
    LOCAL_LLM("Local LLM", Color(0xFF8B5CF6), "llama-3.1-8b", "http://localhost:1234/v1")
}

object AppTheme {
    const val APP_NAME = "AI Agent zzz"
    const val FREE_API_LIMIT = 5
    const val MAX_ACTIVE_APIS_PER_CHAT = 5
    
    val PrimaryStart = Color(0xFF667EEA)
    val PrimaryEnd = Color(0xFF764BA2)
    val ProStart = Color(0xFFFFD89B)
    val ProEnd = Color(0xFFFF6B6B)
    val SurfaceDark = Color(0xFF0F0F1E)
    val CardDark = Color(0xFF1A1A2E)
}

// ═══════════════════════════════════════════════════════════════════════════════
// VIEWMODEL
// ═══════════════════════════════════════════════════════════════════════════════

class AgentViewModel : ViewModel() {
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _chatSessions = MutableStateFlow<List<ChatSession>>(listOf(ChatSession(title = "New Chat")))
    val chatSessions: StateFlow<List<ChatSession>> = _chatSessions.asStateFlow()

    private val _currentSessionId = MutableStateFlow(_chatSessions.value.first().id)
    val currentSessionId: StateFlow<String> = _currentSessionId.asStateFlow()

    private val _isSidebarOpen = MutableStateFlow(true)
    val isSidebarOpen: StateFlow<Boolean> = _isSidebarOpen.asStateFlow()

    private val _isSettingsModalOpen = MutableStateFlow(false)
    val isSettingsModalOpen: StateFlow<Boolean> = _isSettingsModalOpen.asStateFlow()

    private val _selectedMode = MutableStateFlow(AiMode.STANDARD)
    val selectedMode: StateFlow<AiMode> = _selectedMode.asStateFlow()

    private val _editingChatId = MutableStateFlow<String?>(null)
    val editingChatId: StateFlow<String?> = _editingChatId.asStateFlow()

    val currentSession: ChatSession?
        get() = _chatSessions.value.find { it.id == _currentSessionId.value }

    val activeApiCount: Int
        get() = _settings.value.apiConfigs.count { it.isActive }

    fun toggleSidebar() { _isSidebarOpen.value = !_isSidebarOpen.value }
    fun openSettings() { _isSettingsModalOpen.value = true }
    fun closeSettings() { _isSettingsModalOpen.value = false }
    fun toggleProPlan(isPro: Boolean) { _settings.value = _settings.value.copy(isProUser = isPro) }
    fun toggleTheme(isDark: Boolean) { _settings.value = _settings.value.copy(isDarkTheme = isDark) }
    fun setSelectedMode(mode: AiMode) { _selectedMode.value = mode }

    fun addApiConfig(config: ApiConfig): Boolean {
        val settings = _settings.value
        val isPro = settings.isProUser
        
        if (!isPro && settings.apiConfigs.size >= AppTheme.FREE_API_LIMIT) {
            return false // Cannot add more APIs on free plan
        }
        
        _settings.value = settings.copy(apiConfigs = settings.apiConfigs + config)
        return true
    }

    fun updateApiConfig(configId: String, updatedConfig: ApiConfig) {
        val settings = _settings.value
        _settings.value = settings.copy(
            apiConfigs = settings.apiConfigs.map { 
                if (it.id == configId) updatedConfig else it 
            }
        )
    }

    fun deleteApiConfig(configId: String) {
        val settings = _settings.value
        _settings.value = settings.copy(
            apiConfigs = settings.apiConfigs.filter { it.id != configId }
        )
        
        // Remove from all chat sessions
        _chatSessions.value.forEach { session ->
            session.activeApis.remove(configId)
        }
        _chatSessions.value = _chatSessions.value.toList()
    }

    fun toggleApiActive(configId: String) {
        val settings = _settings.value
        val config = settings.apiConfigs.find { it.id == configId } ?: return
        
        // Count currently active APIs
        val currentlyActive = settings.apiConfigs.count { it.isActive }
        
        if (!config.isActive && currentlyActive >= AppTheme.MAX_ACTIVE_APIS_PER_CHAT) {
            // Cannot activate more than 5 APIs
            return
        }
        
        _settings.value = settings.copy(
            apiConfigs = settings.apiConfigs.map {
                if (it.id == configId) it.copy(isActive = !it.isActive) else it
            }
        )
    }

    fun toggleApiForCurrentChat(configId: String) {
        val session = currentSession ?: return
        
        if (session.activeApis.contains(configId)) {
            session.activeApis.remove(configId)
        } else {
            if (session.activeApis.size >= AppTheme.MAX_ACTIVE_APIS_PER_CHAT) {
                // Remove oldest API
                session.activeApis.removeAt(0)
            }
            session.activeApis.add(configId)
        }
        
        _chatSessions.value = _chatSessions.value.toList()
    }

    fun newChat() {
        val newSession = ChatSession(title = "New Chat ${_chatSessions.value.size + 1}")
        _chatSessions.value = listOf(newSession) + _chatSessions.value
        _currentSessionId.value = newSession.id
        _selectedMode.value = AiMode.STANDARD
    }

    fun openChat(sessionId: String) {
        _currentSessionId.value = sessionId
    }

    fun startEditingChat(chatId: String) {
        _editingChatId.value = chatId
    }

    fun renameChat(sessionId: String, newTitle: String) {
        _chatSessions.value = _chatSessions.value.map { session ->
            if (session.id == sessionId) {
                session.copy(title = newTitle)
            } else session
        }
        _editingChatId.value = null
    }

    fun deleteChat(sessionId: String) {
        _chatSessions.value = _chatSessions.value.filter { it.id != sessionId }
        if (_currentSessionId.value == sessionId) {
            _currentSessionId.value = _chatSessions.value.firstOrNull()?.id ?: ""
            if (_chatSessions.value.isEmpty()) {
                newChat()
            }
        }
    }

    fun pinChat(sessionId: String) {
        _chatSessions.value = _chatSessions.value.map { session ->
            if (session.id == sessionId) {
                session.copy(isPinned = !session.isPinned)
            } else session
        }
    }

    fun sendUserMessage(text: String, attachments: List<Attachment>, mode: AiMode) {
        val settings = _settings.value
        val isPro = settings.isProUser
        val currentSession = this.currentSession ?: return

        if (!isPro && attachments.size > 10) {
            appendAiMessage("⚠️ Free plan limited to 10 attachments. Upgrade to Pro!")
            return
        }

        if (!isPro && mode.isPro) {
            appendAiMessage("🔒 ${mode.title} Mode requires Pro. Upgrade to unlock!")
            return
        }

        // Get active APIs for this chat
        val activeApis = settings.apiConfigs.filter { 
            it.isActive && currentSession.activeApis.contains(it.id) 
        }

        if (activeApis.isEmpty()) {
            appendAiMessage("⚠️ No active APIs configured for this chat. Please:\n1. Go to Settings → API Management\n2. Add and activate API configurations\n3. Enable them for this chat")
            return
        }

        val userMessage = ChatMessage(
            text = text.trim(),
            isUser = true,
            attachments = attachments,
            mode = mode
        )
        
        currentSession.messages.add(userMessage)
        
        if (currentSession.messages.size == 1 && currentSession.title.startsWith("New Chat")) {
            currentSession.title = text.take(40) + if (text.length > 40) "..." else ""
        }
        
        _chatSessions.value = _chatSessions.value.toList()

        viewModelScope.launch {
            _settings.value = settings.copy(
                tokenUsage = settings.tokenUsage + 150,
                estimatedCost = settings.estimatedCost + 0.00075
            )

            delay(800)
            currentSession.messages.add(
                ChatMessage(text = "...", isUser = false, isStreaming = true)
            )
            _chatSessions.value = _chatSessions.value.toList()

            delay(1500)
            currentSession.messages.removeLast()
            val reply = generateAiReply(userMessage, activeApis)
            currentSession.messages.add(
                ChatMessage(
                    text = reply, 
                    isUser = false, 
                    mode = mode,
                    usedApis = activeApis.map { it.name }
                )
            )
            _chatSessions.value = _chatSessions.value.toList()
        }
    }

    private fun generateAiReply(userMessage: ChatMessage, activeApis: List<ApiConfig>): String {
        val attachmentInfo = if (userMessage.attachments.isNotEmpty()) {
            "\n\n📎 **Attachments:** ${userMessage.attachments.size} file(s) processed"
        } else ""

        val apiInfo = buildString {
            append("\n\n**Active APIs (${activeApis.size}):**\n")
            activeApis.forEach { api ->
                append("• ${api.name} (${api.provider.title})\n")
                append("  Model: ${api.modelName}\n")
                if (api.systemRole.isNotBlank()) {
                    append("  Role: ${api.systemRole.take(50)}...\n")
                }
            }
        }

        return when (userMessage.mode) {
            AiMode.THINKING -> """
                🧠 **Thinking Mode Activated**
                
                **Deep Analysis Process:**
                1. Understanding query context
                2. Breaking down components
                3. Evaluating perspectives
                4. Synthesizing conclusions
                
                **Final Answer:** Based on deep reasoning with ${activeApis.size} AI model(s), here's my comprehensive response to: "${userMessage.text.take(80)}..."$attachmentInfo$apiInfo
            """.trimIndent()
            
            AiMode.RESEARCH -> """
                🔬 **Deep Research Mode**
                
                **Research Summary:**
                Topic: ${userMessage.text.take(50)}
                
                **Key Findings:**
                • Comprehensive analysis completed
                • Multiple AI models consulted
                • Evidence-based conclusions
                
                **Collaborating Models:** ${activeApis.joinToString { it.name }}$attachmentInfo$apiInfo
            """.trimIndent()
            
            AiMode.STUDY -> """
                📚 **Study Mode - Expert Tutor**
                
                Let me break this down simply using ${activeApis.size} AI assistant(s)...
                
                **Key Concepts:**
                ✓ Core principles explained
                ✓ Practical examples
                ✓ Common misconceptions clarified
                
                **Quick Quiz:** Can you explain the main point?$attachmentInfo$apiInfo
            """.trimIndent()
            
            AiMode.CODE -> """
                💻 **Code Assistant Mode**
                
                **Technical Analysis:**
                ```kotlin
                // AI-powered coding assistance
                fun analyzeCode() {
                    println("Using ${activeApis.size} AI models")
                }
                ```
                
                **Solution:** Optimized approach provided.$attachmentInfo$apiInfo
            """.trimIndent()
            
            AiMode.CREATIVE -> """
                ✨ **Creative Writing Mode**
                
                Once upon a time, ${activeApis.size} AI minds came together...
                
                Your imagination meets collaborative AI creativity.$attachmentInfo$apiInfo
            """.trimIndent()
            
            else -> """
                Hello! I'm **${AppTheme.APP_NAME}** with ${activeApis.size} active AI model(s) working together.
                
                **Current Configuration:**
                ${activeApis.mapIndexed { index, api -> 
                    "${index + 1}. ${api.name} - ${api.provider.title} (${api.modelName})"
                }.joinToString("\n")}
                
                How can we assist you today?$attachmentInfo
            """.trimIndent()
        }
    }

    private fun appendAiMessage(text: String) {
        val currentSession = this.currentSession ?: return
        currentSession.messages.add(ChatMessage(text = text, isUser = false))
        _chatSessions.value = _chatSessions.value.toList()
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// THEME
// ═══════════════════════════════════════════════════════════════════════════════

private val DarkColorScheme = darkColorScheme(
    primary = AppTheme.PrimaryStart,
    background = AppTheme.SurfaceDark,
    surface = AppTheme.CardDark,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = AppTheme.PrimaryStart,
    background = Color(0xFFF8F9FE),
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun ModernAgentTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// MAIN SCREEN
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentScreen(viewModel: AgentViewModel = viewModel()) {
    val settings by viewModel.settings.collectAsState()

    ModernAgentTheme(darkTheme = settings.isDarkTheme) {
        val isSidebarOpen by viewModel.isSidebarOpen.collectAsState()
        val isSettingsModalOpen by viewModel.isSettingsModalOpen.collectAsState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = if (settings.isDarkTheme) {
                            listOf(Color(0xFF0A0A1E), Color(0xFF1A1A2E), Color(0xFF0F0F1E))
                        } else {
                            listOf(Color(0xFFF8F9FE), Color(0xFFEEF2FF), Color(0xFFE0E7FF))
                        }
                    )
                )
        ) {
            Scaffold(
                topBar = { ModernTopBar(viewModel) },
                containerColor = Color.Transparent,
                content = { paddingValues ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        AnimatedVisibility(
                            visible = isSidebarOpen,
                            enter = slideInHorizontally() + fadeIn(),
                            exit = slideOutHorizontally() + fadeOut()
                        ) {
                            ModernSidebar(viewModel)
                        }

                        ModernChatInterface(viewModel)
                    }

                    if (isSettingsModalOpen) {
                        ModernSettingsModal(viewModel)
                    }
                }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// TOP BAR
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTopBar(viewModel: AgentViewModel) {
    val settings by viewModel.settings.collectAsState()
    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "pulse"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (settings.isDarkTheme) {
            AppTheme.CardDark.copy(alpha = 0.95f)
        } else {
            Color.White.copy(alpha = 0.95f)
        },
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.toggleSidebar() }) {
                Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    AppTheme.APP_NAME,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = pulseAlpha))
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${viewModel.activeApiCount} APIs • ${settings.tokenUsage}T • \$${String.format("%.4f", settings.estimatedCost)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            if (settings.isProUser) {
                ProBadge()
                Spacer(Modifier.width(8.dp))
            }

            IconButton(onClick = { viewModel.openSettings() }) {
                Icon(Icons.Default.AccountCircle, "Profile", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun ProBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(AppTheme.ProStart, AppTheme.ProEnd)))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("PRO", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// SIDEBAR
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ModernSidebar(viewModel: AgentViewModel) {
    val settings by viewModel.settings.collectAsState()
    val chatSessions by viewModel.chatSessions.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val editingChatId by viewModel.editingChatId.collectAsState()

    Surface(
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight(),
        color = if (settings.isDarkTheme) {
            AppTheme.CardDark.copy(alpha = 0.8f)
        } else {
            Color.White.copy(alpha = 0.95f)
        }
    ) {
        Column(Modifier.padding(16.dp)) {
            Button(
                onClick = { viewModel.newChat() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("New Chat", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(Modifier.weight(1f)) {
                val pinnedChats = chatSessions.filter { it.isPinned }
                val regularChats = chatSessions.filter { !it.isPinned }

                if (pinnedChats.isNotEmpty()) {
                    item {
                        Text(
                            "Pinned",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(pinnedChats, key = { it.id }) { chat ->
                        ChatHistoryCard(
                            chat = chat,
                            isSelected = chat.id == currentSessionId,
                            isEditing = chat.id == editingChatId,
                            onChatClick = { viewModel.openChat(chat.id) },
                            onRename = { viewModel.startEditingChat(chat.id) },
                            onRenameConfirm = { newTitle -> viewModel.renameChat(chat.id, newTitle) },
                            onDelete = { viewModel.deleteChat(chat.id) },
                            onPin = { viewModel.pinChat(chat.id) }
                        )
                    }
                }

                item {
                    Text(
                        "Recent",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp, top = if (pinnedChats.isNotEmpty()) 16.dp else 0.dp)
                    )
                }
                items(regularChats, key = { it.id }) { chat ->
                    ChatHistoryCard(
                        chat = chat,
                        isSelected = chat.id == currentSessionId,
                        isEditing = chat.id == editingChatId,
                        onChatClick = { viewModel.openChat(chat.id) },
                        onRename = { viewModel.startEditingChat(chat.id) },
                        onRenameConfirm = { newTitle -> viewModel.renameChat(chat.id, newTitle) },
                        onDelete = { viewModel.deleteChat(chat.id) },
                        onPin = { viewModel.pinChat(chat.id) }
                    )
                }
            }

            UserFooter(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHistoryCard(
    chat: ChatSession,
    isSelected: Boolean,
    isEditing: Boolean,
    onChatClick: () -> Unit,
    onRename: () -> Unit,
    onRenameConfirm: (String) -> Unit,
    onDelete: () -> Unit,
    onPin: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf(chat.title) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onChatClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            }
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // FIX APPLIED HERE: Removed duplicated/malformed Box block
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(listOf(AppTheme.PrimaryStart, AppTheme.PrimaryEnd))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ChatBubble,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            if (isEditing) {
                TextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                IconButton(onClick = { onRenameConfirm(editText) }) {
                    Icon(Icons.Default.Check, "Save", tint = MaterialTheme.colorScheme.primary)
                }
            } else {
                Column(Modifier.weight(1f)) {
                    Text(
                        chat.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${chat.messageCount} msgs",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        if (chat.activeApis.isNotEmpty()) {
                            Text(
                                " • ${chat.activeApis.size} APIs",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Menu", modifier = Modifier.size(20.dp))
                    }

                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Rename") },
                            onClick = {
                                onRename()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text(if (chat.isPinned) "Unpin" else "Pin") },
                            onClick = {
                                onPin()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.PushPin, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                onDelete()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.error)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserFooter(viewModel: AgentViewModel) {
    val settings by viewModel.settings.collectAsState()

    Column {
        if (!settings.isProUser) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openSettings() },
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFFF093FB).copy(alpha = 0.3f),
                                    Color(0xFFF5576C).copy(alpha = 0.3f)
                                )
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Rocket, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Upgrade to Pro", fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Unlimited APIs & all AI modes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.openSettings() }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(AppTheme.PrimaryStart, AppTheme.PrimaryEnd))),
                contentAlignment = Alignment.Center
            ) {
                Text("A", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text("Agent User", fontWeight = FontWeight.SemiBold)
                Text(
                    if (settings.isProUser) "Pro Account" else "Free Account",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (settings.isProUser) AppTheme.ProStart else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Icon(Icons.Default.Settings, "Settings", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// CHAT INTERFACE
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ModernChatInterface(viewModel: AgentViewModel) {
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val chatSessions by viewModel.chatSessions.collectAsState()
    val currentSession = chatSessions.find { it.id == currentSessionId }
    val messages = currentSession?.messages ?: emptyList()
    val settings by viewModel.settings.collectAsState()
    var input by remember { mutableStateOf("") }
    val attachedFiles = remember { mutableStateListOf<Attachment>() }
    val selectedMode by viewModel.selectedMode.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Active APIs Bar for current chat
        if (currentSession != null && currentSession.activeApis.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                tonalElevation = 2.dp
            ) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            "Active APIs:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                    items(currentSession.activeApis) { apiId ->
                        val apiConfig = settings.apiConfigs.find { it.id == apiId }
                        if (apiConfig != null) {
                            ApiChip(
                                api = apiConfig,
                                onRemove = { viewModel.toggleApiForCurrentChat(apiId) }
                            )
                        }
                    }
                }
            }
        }

        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Welcome to ${AppTheme.APP_NAME}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Multi-AI collaborative workspace",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(16.dp))
                    if (currentSession?.activeApis?.isEmpty() == true) {
                        Text(
                            "⚠️ No APIs active for this chat",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.openSettings() }) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add APIs")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                reverseLayout = true,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages.reversed(), key = { it.id }) { msg ->
                    MessageBubble(msg, settings.isDarkTheme)
                }
            }
        }

        ModernInputBar(
            input = input,
            onInputChange = { input = it },
            onSend = {
                if (input.isNotBlank() || attachedFiles.isNotEmpty()) {
                    viewModel.sendUserMessage(input, attachedFiles.toList(), selectedMode)
                    input = ""
                    attachedFiles.clear()
                }
            },
            attachedFiles = attachedFiles,
            viewModel = viewModel,
            selectedMode = selectedMode
        )
    }
}

@Composable
fun ApiChip(api: ApiConfig, onRemove: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = api.provider.color.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(api.provider.color)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                api.name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onRemove, modifier = Modifier.size(16.dp)) {
                Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
fun MessageBubble(msg: ChatMessage, isDarkTheme: Boolean) {
    val alignment = if (msg.isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (msg.isUser) {
        Brush.linearGradient(listOf(AppTheme.PrimaryStart, AppTheme.PrimaryEnd))
    } else {
        Brush.linearGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            if (!msg.isUser) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFF093FB), Color(0xFFF5576C)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Psychology, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
            }

            Box(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    // FIX APPLIED HERE: Completed the shape definition
                    .shadow(4.dp, RoundedCornerShape(20.dp, 20.dp, if (msg.isUser) 4.dp else 20.dp, if (msg.isUser) 20.dp else 4.dp))
                    .clip(RoundedCornerShape(20.dp, 20.dp, if (msg.isUser) 4.dp else 20.dp, if (msg.isUser) 20.dp else 4.dp))
                    .background(bubbleColor)
            ) {
                Column(Modifier.padding(16.dp)) {
                    if (msg.attachments.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            items(msg.attachments, key = { it.id }) { attachment ->
                                AttachmentPreview(attachment, isInMessage = true)
                            }
                        }
                    }
                    if (msg.isStreaming) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = if (msg.isUser) Color.White else MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Thinking...",
                                color = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Text(
                            msg.text,
                            color = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (msg.usedApis.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                msg.usedApis.forEach { apiName ->
                                    Surface(
                                        color = if (msg.isUser) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            apiName,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            color = if (msg.isUser) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (msg.isUser) {
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(AppTheme.PrimaryStart, AppTheme.PrimaryEnd))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("A", color = Color.White, fontWeight = FontWeight.Black)
                }
            }
        }
        if (!msg.isStreaming) {
            Spacer(Modifier.height(4.dp))
            Text(
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 52.dp)
            )
        }
    }
}

@Composable
fun AttachmentPreview(attachment: Attachment, isInMessage: Boolean = false) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isInMessage) {
                Color.White.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (attachment.isImage) Icons.Default.Image else Icons.Default.Description,
                null,
                modifier = Modifier.size(20.dp),
                tint = if (isInMessage) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(6.dp))
            Text(
                attachment.name,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 100.dp),
                color = if (isInMessage) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// INPUT BAR UTILITY (FIX: MISSING COMPOSABLE)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun AttachmentChip(attachment: Attachment, onRemove: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (attachment.isImage) Icons.Default.Image else Icons.Default.Description,
                null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(6.dp))
            Text(
                attachment.name.take(20) + if (attachment.name.length > 20) "..." else "",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onRemove, modifier = Modifier.size(16.dp)) {
                Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(12.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// INPUT BAR
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernInputBar(
    input: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    attachedFiles: MutableList<Attachment>,
    viewModel: AgentViewModel,
    selectedMode: AiMode
) {
    val settings by viewModel.settings.collectAsState()
    var isMenuOpen by remember { mutableStateOf(false) }
    var isModeMenuOpen by remember { mutableStateOf(false) }
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = "file_${System.currentTimeMillis()}.txt"
            attachedFiles.add(
                Attachment(
                    name = fileName,
                    type = AttachmentType.DOCUMENT,
                    uri = it
                )
            )
        }
    }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = "image_${System.currentTimeMillis()}.jpg"
            attachedFiles.add(
                Attachment(
                    name = fileName,
                    type = AttachmentType.IMAGE,
                    uri = it
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (attachedFiles.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                items(attachedFiles.toList(), key = { it.id }) { attachment ->
                    AttachmentChip(attachment, onRemove = { attachedFiles.remove(attachment) })
                }
            }
        }
        if (selectedMode != AiMode.STANDARD) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedMode.icon, fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("${selectedMode.title} Active", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { viewModel.setSelectedMode(AiMode.STANDARD) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Box {
                    IconButton(onClick = { isMenuOpen = !isMenuOpen }) {
                        Icon(Icons.Default.Add, "Add", tint = MaterialTheme.colorScheme.primary)
                    }
                    DropdownMenu(expanded = isMenuOpen, onDismissRequest = { isMenuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Upload Image") },
                            onClick = { 
                                if (!settings.isProUser && attachedFiles.size >= 10) { 
                                    // Handled by sendMessage 
                                } else { 
                                    imagePicker.launch("image/*") 
                                } 
                                isMenuOpen = false 
                            },
                            leadingIcon = { Icon(Icons.Default.Image, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Upload File") },
                            onClick = { 
                                if (!settings.isProUser && attachedFiles.size >= 10) { 
                                    // Handled by sendMessage 
                                } else { 
                                    filePicker.launch("*/*") 
                                } 
                                isMenuOpen = false 
                            },
                            leadingIcon = { Icon(Icons.Default.AttachFile, null) }
                        )
                    }
                }
                
                Box {
                    IconButton(onClick = { isModeMenuOpen = !isModeMenuOpen }) {
                        Icon(Icons.Default.Psychology, "Modes", tint = MaterialTheme.colorScheme.primary)
                    }
                    DropdownMenu(expanded = isModeMenuOpen, onDismissRequest = { isModeMenuOpen = false }) {
                        AiMode.values().forEach { mode ->
                            val isLocked = mode.isPro && !settings.isProUser
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) { 
                                        Text(mode.icon) 
                                        Spacer(Modifier.width(8.dp)) 
                                        Text(mode.title) 
                                        // FIX APPLIED HERE: Completed the truncated dropdown item content
                                        if (isLocked) { 
                                            Spacer(Modifier.width(8.dp))
                                            Icon(
                                                Icons.Default.Lock, 
                                                "Pro Feature", 
                                                tint = Color.Gray, 
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    } 
                                },
                                onClick = {
                                    if (!isLocked) {
                                        viewModel.setSelectedMode(mode)
                                        isModeMenuOpen = false
                                    }
                                },
                                enabled = !isLocked,
                                trailingIcon = if (isLocked) ({ ProBadge() }) else null
                            )
                        } 
                    } 
                } 

                OutlinedTextField(
                    value = input,
                    onValueChange = onInputChange,
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    placeholder = { Text("Message ${selectedMode.title}...") },
                    singleLine = false,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    )
                )

                IconButton(
                    onClick = onSend,
                    enabled = input.isNotBlank() || attachedFiles.isNotEmpty(),
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                listOf(AppTheme.PrimaryStart, AppTheme.PrimaryEnd)
                            ), CircleShape
                        )
                        .size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Send,
                        "Send",
                        tint = Color.White
                    )
                }
            } 
        } 
    } 
}