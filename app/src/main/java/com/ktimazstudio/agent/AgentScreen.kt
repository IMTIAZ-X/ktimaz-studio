package com.ktimazstudio.agent

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke 
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
import androidx.compose.ui.text.input.ImeAction 
import androidx.compose.ui.text.input.KeyboardActions // FIXED: Explicitly imported
import androidx.compose.ui.text.input.KeyboardOptions // FIXED: Explicitly imported
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
                    // ADDED keyboard options and actions to confirm on 'Done' press
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onRenameConfirm(editText) }),
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
                Column(modifier = Modifier.weight(1f)) { // FIXED: Added 'modifier ='
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
                            onClick = { onRename() 
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text(if (chat.isPinned) "Unpin" else "Pin") },
                            onClick = { onPin() 
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.PushPin, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = { onDelete() 
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
                            "Unlimited APIs & all AI modes...",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "v1.0.0 (Agent App)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            IconButton(onClick = { viewModel.toggleTheme(!settings.isDarkTheme) }) {
                Icon(
                    if (settings.isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                    "Toggle Theme",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// CHAT INTERFACE
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernChatInterface(
    viewModel: AgentViewModel,
    modifier: Modifier = Modifier
) {
    val currentSession = viewModel.currentSession
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    // FIXED: Collect settings cleanly, removing the nested collectAsState calls.
    val settings by viewModel.settings.collectAsState()

    val activeApis = remember(currentSessionId, settings.apiConfigs) {
        currentSession?.activeApis?.mapNotNull { apiId ->
            settings.apiConfigs.find { it.id == apiId }
        } ?: emptyList()
    }

    val selectedMode by viewModel.selectedMode.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(currentSession?.messages?.size) {
        if (currentSession?.messages?.isNotEmpty() == true) {
            // Re-implementing correctly:
            if (currentSession.messages.last().isStreaming || currentSession.messages.last().isUser) {
                listState.animateScrollToItem(currentSession.messages.lastIndex)
            }
        }
    }

    Column(
        modifier = modifier.weight(1f).fillMaxHeight()
    ) {
        // Mode & Active APIs Display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 24.dp, end = 24.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ModeChip(selectedMode, settings.isProUser) { viewModel.setSelectedMode(it) }

            AnimatedVisibility(
                visible = activeApis.isNotEmpty(),
                enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End),
                exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.End)
            ) {
                LazyRow(
                    // FIXED: Removed redundant .padding(horizontal = 24.dp) which caused compiler confusion.
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activeApis, key = { it.id }) { api ->
                        ActiveApiChip(api, viewModel)
                    }
                }
            }
        }
        
        // Chat History
        if (currentSession == null) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Select or start a new chat.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                state = listState,
                contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(currentSession.messages, key = { it.id }) { message ->
                    ChatMessageCard(message, settings.isDarkTheme)
                }
            }
        }
        
        // Input Bar
        ModernInputBar(viewModel)
    }
}

@Composable
fun ModeChip(
    mode: AiMode, 
    isProUser: Boolean, 
    onModeSelected: (AiMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val chipColor = when (mode) {
        AiMode.STANDARD -> MaterialTheme.colorScheme.primary
        AiMode.THINKING -> Color(0xFFEAB308)
        AiMode.RESEARCH -> Color(0xFF3B82F6)
        AiMode.STUDY -> Color(0xFF6366F1)
        AiMode.CODE -> Color(0xFF10B981)
        AiMode.CREATIVE -> Color(0xFFF472B6)
    }

    Box {
        AssistChip(
            onClick = { expanded = true },
            label = {
                Text(
                    text = "${mode.icon} ${mode.title}", 
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            leadingIcon = {
                if (mode.isPro && !isProUser) {
                    Icon(
                        Icons.Default.Lock,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = chipColor
            ),
            border = null
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            AiMode.entries.forEach { modeItem ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(modeItem.title)
                            if (modeItem.isPro) {
                                Spacer(Modifier.width(8.dp))
                                ProBadge()
                            }
                        }
                    },
                    onClick = {
                        if (isProUser || !modeItem.isPro) {
                            onModeSelected(modeItem)
                        }
                        expanded = false
                    },
                    leadingIcon = { Text(modeItem.icon) }
                )
            }
        }
    }
}

@Composable
fun ActiveApiChip(
    api: ApiConfig,
    viewModel: AgentViewModel
) {
    val settings by viewModel.settings.collectAsState()
    val isApiActive = remember(settings.apiConfigs) {
        settings.apiConfigs.any { it.id == api.id && it.isActive }
    }
    val isUsedInChat = remember(viewModel.currentSession) {
        viewModel.currentSession?.activeApis?.contains(api.id) ?: false
    }

    AssistChip(
        onClick = { viewModel.toggleApiForCurrentChat(api.id) },
        label = {
            Text(
                text = api.name,
                fontWeight = FontWeight.SemiBold,
                color = if (isUsedInChat) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        },
        leadingIcon = {
            Icon(
                if (isUsedInChat) Icons.Default.CheckCircle else Icons.Outlined.AddCircle,
                null,
                tint = if (isUsedInChat) Color.White else api.provider.color.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (isUsedInChat) api.provider.color.copy(alpha = 0.9f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = if (isUsedInChat) null else BorderStroke(1.dp, api.provider.color.copy(alpha = 0.3f))
    )
}

@Composable
fun ChatMessageCard(
    message: ChatMessage,
    isDarkTheme: Boolean
) {
    val bubbleColor = if (message.isUser) {
        MaterialTheme.colorScheme.primary
    } else if (isDarkTheme) {
        AppTheme.CardDark.copy(alpha = 0.8f)
    } else {
        Color.White
    }

    val textColor = if (message.isUser) Color.White else MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
        ) {
            Card(
                modifier = Modifier.widthIn(max = 600.dp).shadow(2.dp, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = bubbleColor),
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp,
                    bottomStart = if (message.isUser) 12.dp else 0.dp,
                    bottomEnd = if (message.isUser) 0.dp else 12.dp
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (!message.isUser && message.mode != AiMode.STANDARD) {
                        Text(
                            text = message.mode.promptTag,
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Text(
                        text = message.text,
                        color = textColor,
                        fontWeight = if (message.isStreaming) FontWeight.Light else FontWeight.Normal
                    )
                    
                    if (message.attachments.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "📎 Attachments (${message.attachments.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor.copy(alpha = 0.7f)
                        )
                    }
                    
                    if (message.usedApis.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Via: ${message.usedApis.joinToString()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        
        Spacer(Modifier.height(4.dp))
        Text(
            text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            modifier = Modifier.align(if (message.isUser) Alignment.End else Alignment.Start)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernInputBar(viewModel: AgentViewModel) {
    val settings by viewModel.settings.collectAsState()
    var input by remember { mutableStateOf("") }
    var attachments by remember { mutableStateOf<List<Attachment>>(emptyList()) }
    val selectedMode by viewModel.selectedMode.collectAsState()
    
    // File picker launcher
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris ->
            attachments = uris.mapIndexed { index, uri ->
                Attachment(
                    name = uri.lastPathSegment ?: "File $index",
                    type = AttachmentType.UNKNOWN,
                    uri = uri
                )
            }
        }
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shadowElevation = 16.dp,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Attachments Preview (if any)
            if (attachments.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(attachments, key = { it.id }) { attachment ->
                        AttachmentChip(attachment) {
                            attachments = attachments.filter { it.id != attachment.id }
                        }
                    }
                }
            }
            
            // Input Field
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                // Text Input
                TextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message ${AppTheme.APP_NAME}...") },
                    maxLines = 5,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (input.isNotBlank() || attachments.isNotEmpty()) {
                                viewModel.sendUserMessage(input, attachments, selectedMode)
                                input = ""
                                attachments = emptyList()
                            }
                        }
                    )
                )

                // Send Button
                Spacer(Modifier.width(8.dp))
                FloatingActionButton(
                    onClick = {
                        if (input.isNotBlank() || attachments.isNotEmpty()) {
                            viewModel.sendUserMessage(input, attachments, selectedMode)
                            input = ""
                            attachments = emptyList()
                        }
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.Send, "Send")
                }
            }

            // Action Row (File Picker)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { fileLauncher.launch(arrayOf("*/*")) }) {
                    Icon(Icons.Default.AttachFile, "Attach File", tint = MaterialTheme.colorScheme.primary)
                }
                
                // Active API Count Indicator
                Text(
                    text = "APIs: ${viewModel.currentSession?.activeApis?.size ?: 0}/${AppTheme.MAX_ACTIVE_APIS_PER_CHAT}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun AttachmentChip(attachment: Attachment, onDelete: () -> Unit) {
    AssistChip(
        onClick = { /* Open file */ },
        label = {
            Text(attachment.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        leadingIcon = {
            Icon(Icons.Default.FileCopy, null, modifier = Modifier.size(18.dp))
        },
        trailingIcon = {
            IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp))
            }
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// SETTINGS MODAL
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ModernSettingsModal(viewModel: AgentViewModel) {
    val settings by viewModel.settings.collectAsState()

    Dialog(onDismissRequest = { viewModel.closeSettings() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Settings & Configuration",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { viewModel.closeSettings() }) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
                
                Spacer(Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    // --- General Settings ---
                    item { SettingSectionHeader("General") }
                    
                    item {
                        SettingToggle(
                            title = "Dark Theme",
                            subtitle = "Toggle between light and dark mode",
                            isChecked = settings.isDarkTheme,
                            onCheckedChange = { viewModel.toggleTheme(it) }
                        )
                    }

                    // --- API Management ---
                    item { SettingSectionHeader("API Management") }

                    item {
                        Text(
                            "Configure and manage your AI models.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    items(settings.apiConfigs, key = { it.id }) { api ->
                        ApiConfigCard(api, viewModel)
                    }
                    
                    item {
                        AddApiButton(viewModel)
                        Spacer(Modifier.height(16.dp))
                    }

                    // --- Billing & Pro Plan ---
                    item { SettingSectionHeader("Billing & Usage") }

                    item {
                        UsageMetricCard(
                            title = "Token Usage (Total)",
                            value = "${settings.tokenUsage}T",
                            icon = Icons.Default.CloudQueue
                        )
                    }
                    item {
                        UsageMetricCard(
                            title = "Estimated Cost (Total)",
                            value = "\$${String.format("%.4f", settings.estimatedCost)}",
                            icon = Icons.Default.AttachMoney
                        )
                    }
                    
                    item {
                        ProStatusCard(settings.isProUser) { viewModel.toggleProPlan(!settings.isProUser) }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingSectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 12.dp, top = 24.dp)
    )
    Divider()
}

@Composable
fun SettingToggle(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) { // FIXED: Added 'modifier ='
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Spacer(Modifier.width(16.dp))
        Switch(checked = isChecked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun UsageMetricCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) { // FIXED: Added 'modifier ='
                Text(title, style = MaterialTheme.typography.labelMedium)
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun ProStatusCard(isPro: Boolean, onUpgradeClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .clickable { onUpgradeClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isPro) Color(0xFF10B981).copy(alpha = 0.2f) else MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        ),
        border = BorderStroke(2.dp, if (isPro) Color(0xFF10B981) else MaterialTheme.colorScheme.error),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isPro) Icons.Default.CheckCircle else Icons.Default.RocketLaunch,
                    null,
                    tint = if (isPro) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isPro) "PRO Account Activated" else "Upgrade to Pro",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isPro) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                if (isPro) "Enjoy unlimited API configurations and all AI modes!" 
                else "Unlock unlimited APIs, advanced AI modes, and priority support.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ApiConfigCard(
    api: ApiConfig, 
    viewModel: AgentViewModel
) {
    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(api.name) }
    var apiKey by remember { mutableStateOf(api.apiKey) }
    var modelName by remember { mutableStateOf(api.modelName) }
    var systemRole by remember { mutableStateOf(api.systemRole) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        border = if (api.isActive) BorderStroke(2.dp, api.provider.color.copy(alpha = 0.8f)) else null,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    api.provider.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = api.provider.color
                )
                Switch(checked = api.isActive, onCheckedChange = { viewModel.toggleApiActive(api.id) })
            }
            Spacer(Modifier.height(8.dp))
            
            if (isEditing) {
                // Editing UI
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key (Sensitive)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = modelName,
                    onValueChange = { modelName = it },
                    label = { Text("Model Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = systemRole,
                    onValueChange = { systemRole = it },
                    label = { Text("System Role (Prompt)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = { viewModel.deleteApiConfig(api.id) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                        Text("Delete")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { 
                        viewModel.updateApiConfig(api.id, api.copy(
                            name = name,
                            apiKey = apiKey,
                            modelName = modelName,
                            systemRole = systemRole
                        ))
                        isEditing = false
                    }) {
                        Text("Save")
                    }
                }

            } else {
                // Display UI
                Text(api.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Model: ${api.modelName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                if (api.systemRole.isNotBlank()) {
                    Text(
                        "Role: ${api.systemRole.take(50)}${if (api.systemRole.length > 50) "..." else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = { isEditing = true }) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                }
            }
        }
    }
}

@Composable
fun AddApiButton(viewModel: AgentViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val settings by viewModel.settings.collectAsState()
    val canAddMore = settings.isProUser || settings.apiConfigs.size < AppTheme.FREE_API_LIMIT

    Box(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = { if (canAddMore) expanded = true else {/* Show warning or prompt upgrade */ } },
            modifier = Modifier.fillMaxWidth(),
            enabled = canAddMore
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text(if (canAddMore) "Add New API" else "Max APIs Reached (Free Plan)")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            AiProvider.entries.forEach { provider ->
                DropdownMenuItem(
                    text = { Text(provider.title) },
                    onClick = {
                        val newConfig = ApiConfig(
                            provider = provider,
                            name = provider.title,
                            modelName = provider.defaultModel,
                            baseUrl = provider.defaultUrl,
                            isActive = true
                        )
                        viewModel.addApiConfig(newConfig)
                        expanded = false
                    },
                    leadingIcon = { Icon(Icons.Default.Api, null, tint = provider.color) }
                )
            }
        }
    }
}