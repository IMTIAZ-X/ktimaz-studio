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
import androidx.compose.foundation.text.KeyboardActions // FIXED: Correct import
import androidx.compose.foundation.text.KeyboardOptions // FIXED: Correct import
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
        
        val currentlyActive = settings.apiConfigs.count { it.isActive }
        
        if (!config.isActive && currentlyActive >= AppTheme.MAX_ACTIVE_APIS_PER_CHAT) {
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
                    // FIXED: padding(vertical, top) was conflicting. Changed to padding(bottom, top)
                    Text(
                        "Recent",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp, top = if (pinnedChats.isNotEmpty()) 16.dp else 8.dp)
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
// INPUT BAR & MODALS
// ═══════════════════════════════════════════════════════════════════════════════

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
    var showModeSelector by remember { mutableStateOf(false) }
    var showFilePicker by remember { mutableStateOf(false) }
    var showApiSelector by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, ambientColor = MaterialTheme.colorScheme.primary),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.padding(16.dp)) {
            // Mode Selector Row
            ModeSelectionRow(
                selectedMode = selectedMode,
                onSelect = { viewModel.setSelectedMode(it) },
                onToggleVisibility = { showModeSelector = !showModeSelector }
            )
            AnimatedVisibility(
                visible = showModeSelector,
                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
            ) {
                ExpandedModeSelector(
                    currentMode = selectedMode,
                    onSelect = {
                        viewModel.setSelectedMode(it)
                        showModeSelector = false
                    },
                    isProUser = settings.isProUser
                )
            }

            Spacer(Modifier.height(8.dp))

            // Attached Files Preview
            if (attachedFiles.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    items(attachedFiles, key = { it.id }) { attachment ->
                        AttachmentPreview(attachment).apply {
                            Box(modifier = Modifier.clickable { attachedFiles.remove(attachment) }) {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(12.dp).align(Alignment.TopEnd))
                            }
                        }
                    }
                }
            }

            // Input Field
            InputTextField(
                input = input,
                onInputChange = onInputChange,
                onSend = onSend,
                onAttachClick = { showFilePicker = true },
                onApiSelectClick = { showApiSelector = true }
            )
        }
    }

    // File Picker Logic
    if (showFilePicker) {
        val context = LocalContext.current
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri: Uri? ->
                showFilePicker = false
                uri?.let {
                    val name = it.lastPathSegment ?: "file_${System.currentTimeMillis()}"
                    val mimeType = context.contentResolver.getType(it)
                    val type = when {
                        mimeType?.startsWith("image/") == true -> AttachmentType.IMAGE
                        mimeType?.startsWith("text/") == true || mimeType == "application/json" -> AttachmentType.TEXT
                        else -> AttachmentType.DOCUMENT
                    }
                    attachedFiles.add(Attachment(name = name, type = type, uri = it))
                }
            }
        )
        // Auto-launch the picker inside a launched effect or just call launch directly
        LaunchedEffect(Unit) {
            launcher.launch("*/*")
        }
    }

    // API Selection Modal
    if (showApiSelector) {
        ApiSelectionModal(
            viewModel = viewModel,
            onClose = { showApiSelector = false }
        )
    }
}

@Composable
fun ModeSelectionRow(
    selectedMode: AiMode,
    onSelect: (AiMode) -> Unit,
    onToggleVisibility: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AssistChip(
            onClick = onToggleVisibility,
            label = { Text(selectedMode.title, fontWeight = FontWeight.SemiBold) },
            leadingIcon = { Icon(Icons.Default.Adjust, null, modifier = Modifier.size(16.dp)) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        )

        // Quick Select Chips (Standard and Code/Research)
        val quickModes = listOf(AiMode.STANDARD, AiMode.CODE)
        quickModes.forEach { mode ->
            val isSelected = mode == selectedMode
            AssistChip(
                onClick = { onSelect(mode) },
                label = { Text(mode.title, style = MaterialTheme.typography.labelMedium) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
            )
        }
    }
}

@Composable
fun ExpandedModeSelector(currentMode: AiMode, onSelect: (AiMode) -> Unit, isProUser: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text("Select AI Mode", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AiMode.entries.forEach { mode ->
                val isSelected = mode == currentMode
                val isLocked = mode.isPro && !isProUser

                ElevatedAssistChip(
                    onClick = { if (!isLocked) onSelect(mode) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                mode.title,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isLocked) {
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Default.Lock, null, modifier = Modifier.size(12.dp))
                            }
                        }
                    },
                    leadingIcon = {
                        Text(mode.icon, fontSize = 16.sp)
                    },
                    colors = AssistChipDefaults.elevatedAssistChipColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = if (isSelected) AssistChipDefaults.assistChipElevation() else null
                )
            }
        }
    }
}

@Composable
fun InputTextField(
    input: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachClick: () -> Unit,
    onApiSelectClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onAttachClick) {
            Icon(Icons.Default.AttachFile, "Attach File", tint = MaterialTheme.colorScheme.primary)
        }

        TextField(
            value = input,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ask your AI team...") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Send,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onSend = { onSend() }
            ),
        )

        IconButton(onClick = onApiSelectClick) {
            Icon(Icons.Default.Construction, "Active APIs", tint = MaterialTheme.colorScheme.primary)
        }

        Spacer(Modifier.width(4.dp))

        FloatingActionButton(
            onClick = onSend,
            modifier = Modifier.size(40.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Send, "Send", tint = Color.White)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// SETTINGS MODALS
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ModernSettingsModal(viewModel: AgentViewModel) {
    val settings by viewModel.settings.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = { viewModel.closeSettings() }) {
        Card(
            modifier = Modifier
                .fillMaxHeight(0.9f)
                .fillMaxWidth(0.9f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("App Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    IconButton(onClick = { viewModel.closeSettings() }) {
                        Icon(Icons.Default.Close, null)
                    }
                }
                Spacer(Modifier.height(16.dp))

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("General") },
                        icon = { Icon(Icons.Default.Settings, null) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("API Management") },
                        icon = { Icon(Icons.Default.Construction, null) }
                    )
                }

                Spacer(Modifier.height(16.dp))

                Box(Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> GeneralSettingsTab(viewModel, settings)
                        1 -> ApiManagementTab(viewModel, settings)
                    }
                }
            }
        }
    }
}

@Composable
fun GeneralSettingsTab(viewModel: AgentViewModel, settings: AppSettings) {
    Column(Modifier.fillMaxSize()) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Dark Theme", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Toggle between light and dark mode.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Switch(
                        checked = settings.isDarkTheme,
                        onCheckedChange = { viewModel.toggleTheme(it) }
                    )
                }
                Divider(Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Pro Subscription", fontWeight = FontWeight.SemiBold)
                        Text(
                            if (settings.isProUser) "Active. Enjoy unlimited APIs and modes." else "Upgrade to unlock all features.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (settings.isProUser) AppTheme.ProEnd else MaterialTheme.colorScheme.error
                        )
                    }
                    Button(onClick = { viewModel.toggleProPlan(!settings.isProUser) }) {
                        Text(if (settings.isProUser) "Cancel" else "Upgrade")
                    }
                }
                Divider(Modifier.padding(vertical = 8.dp))
                Column {
                    Text("Usage Stats", fontWeight = FontWeight.SemiBold)
                    Text("Tokens Used: ${settings.tokenUsage}T", style = MaterialTheme.typography.bodySmall)
                    Text("Estimated Cost: \$${String.format("%.4f", settings.estimatedCost)}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun ApiManagementTab(viewModel: AgentViewModel, settings: AppSettings) {
    val apiConfigs = settings.apiConfigs
    var showAddApiModal by remember { mutableStateOf(false) }
    var apiToEdit by remember { mutableStateOf<ApiConfig?>(null) }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Configured APIs (${apiConfigs.size} / ${if (settings.isProUser) "Unlimited" else AppTheme.FREE_API_LIMIT})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { showAddApiModal = true },
                enabled = settings.isProUser || apiConfigs.size < AppTheme.FREE_API_LIMIT
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Add API")
            }
        }
        Spacer(Modifier.height(16.dp))

        if (apiConfigs.isEmpty()) {
            Text(
                "No APIs configured. Click 'Add API' to get started.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(apiConfigs, key = { it.id }) { api ->
                    ApiConfigCard(
                        api = api,
                        onToggleActive = { viewModel.toggleApiActive(api.id) },
                        onEdit = { apiToEdit = api },
                        onDelete = { viewModel.deleteApiConfig(api.id) }
                    )
                }
            }
        }
    }

    if (showAddApiModal) {
        ApiConfigModal(
            onClose = { showAddApiModal = false },
            onSave = { config ->
                viewModel.addApiConfig(config)
                showAddApiModal = false
            }
        )
    }

    apiToEdit?.let { api ->
        ApiConfigModal(
            existingConfig = api,
            onClose = { apiToEdit = null },
            onSave = { config ->
                viewModel.updateApiConfig(api.id, config)
                apiToEdit = null
            }
        )
    }
}

@Composable
fun ApiConfigCard(
    api: ApiConfig,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = api.provider.color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    api.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Provider: ${api.provider.title} | Model: ${api.modelName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.width(8.dp))
                Switch(checked = api.isActive, onCheckedChange = { onToggleActive() })
            }
        }
    }
}

@Composable
fun ApiConfigModal(
    existingConfig: ApiConfig? = null,
    onClose: () -> Unit,
    onSave: (ApiConfig) -> Unit
) {
    var name by remember { mutableStateOf(existingConfig?.name ?: "") }
    var provider by remember { mutableStateOf(existingConfig?.provider ?: AiProvider.GEMINI) }
    var apiKey by remember { mutableStateOf(existingConfig?.apiKey ?: "") }
    var modelName by remember { mutableStateOf(existingConfig?.modelName ?: provider.defaultModel) }
    var baseUrl by remember { mutableStateOf(existingConfig?.baseUrl ?: provider.defaultUrl) }
    var systemRole by remember { mutableStateOf(existingConfig?.systemRole ?: "") }
    var showProviderDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(provider) {
        if (existingConfig == null || existingConfig.provider != provider) {
            modelName = provider.defaultModel
            baseUrl = provider.defaultUrl
        }
    }

    Dialog(onDismissRequest = onClose) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    if (existingConfig == null) "Add New API Config" else "Edit API Config",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(16.dp))

                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        TextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Configuration Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    item {
                        Box {
                            OutlinedTextField(
                                value = provider.title,
                                onValueChange = { /* read-only */ },
                                label = { Text("Provider") },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(onClick = { showProviderDropdown = true }) {
                                        Icon(Icons.Default.ArrowDropDown, null)
                                    }
                                }
                            )
                            DropdownMenu(
                                expanded = showProviderDropdown,
                                onDismissRequest = { showProviderDropdown = false }
                            ) {
                                AiProvider.entries.forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text(p.title) },
                                        onClick = {
                                            provider = p
                                            showProviderDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        SettingsApiKeyInput(
                            apiKey = apiKey,
                            onApiKeyChange = { apiKey = it }
                        )
                    }

                    item {
                        TextField(
                            value = modelName,
                            onValueChange = { modelName = it },
                            label = { Text("Model Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    item {
                        TextField(
                            value = baseUrl,
                            onValueChange = { baseUrl = it },
                            label = { Text("Base URL") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    item {
                        TextField(
                            value = systemRole,
                            onValueChange = { systemRole = it },
                            label = { Text("System Role / Instruction") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 6
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onClose) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val config = existingConfig?.copy(
                                name = name,
                                provider = provider,
                                apiKey = apiKey,
                                modelName = modelName,
                                baseUrl = baseUrl,
                                systemRole = systemRole
                            ) ?: ApiConfig(
                                name = name,
                                provider = provider,
                                apiKey = apiKey,
                                modelName = modelName,
                                baseUrl = baseUrl,
                                systemRole = systemRole
                            )
                            onSave(config)
                        },
                        enabled = name.isNotBlank() && apiKey.isNotBlank()
                    ) {
                        Text(if (existingConfig == null) "Add Config" else "Save Changes")
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsApiKeyInput(apiKey: String, onApiKeyChange: (String) -> Unit) {
    var passwordVisible by remember { mutableStateOf(false) }

    TextField(
        value = apiKey,
        onValueChange = onApiKeyChange,
        label = { Text("API Key") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Password
        ),
        keyboardActions = KeyboardActions(
            onDone = { /* Handle done action */ }
        ),
        trailingIcon = {
            val image = if (passwordVisible)
                Icons.Filled.Visibility
            else Icons.Filled.VisibilityOff

            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(image, null)
            }
        }
    )
}

@Composable
fun ApiSelectionModal(viewModel: AgentViewModel, onClose: () -> Unit) {
    val settings by viewModel.settings.collectAsState()
    
    // FIXED: Properly collected states
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val chatSessions by viewModel.chatSessions.collectAsState()
    val currentSession = chatSessions.find { it.id == currentSessionId }
    val activeApis = currentSession?.activeApis ?: emptyList()

    Dialog(onDismissRequest = onClose) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    "Select APIs for this Chat (${activeApis.size} / ${AppTheme.MAX_ACTIVE_APIS_PER_CHAT})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(16.dp))

                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val availableApis = settings.apiConfigs.filter { it.isActive }
                    if (availableApis.isEmpty()) {
                        item {
                            Text(
                                "No active APIs available in Settings.",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        items(availableApis, key = { it.id }) { api ->
                            val isSelected = activeApis.contains(api.id)
                            ApiSelectorCard(
                                api = api,
                                isSelected = isSelected,
                                onClick = {
                                    viewModel.toggleApiForCurrentChat(api.id)
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onClose) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@Composable
fun ApiSelectorCard(
    api: ApiConfig,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) api.provider.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) BorderStroke(2.dp, api.provider.color) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(api.name, fontWeight = FontWeight.SemiBold)
                Text(
                    "${api.provider.title} (${api.modelName})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, null, tint = api.provider.color)
            } else {
                Icon(Icons.Outlined.RadioButtonUnchecked, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }
        }
    }
}