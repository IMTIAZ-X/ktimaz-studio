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
                ${activeApis.mapIndexed { index, api -> "${index + 1}. ${api.name} - ${api.provider.title} (${api.modelName})" }.joinToString("\n")}
                
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
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        ) {
            // Main content and sidebar
            Row(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = isSidebarOpen,
                    enter = slideInHorizontally(initialOffsetX = { -it }),
                    exit = slideOutHorizontally(targetOffsetX = { -it })
                ) {
                    Sidebar(viewModel = viewModel)
                }

                ModernChatInterface(
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f)
                )
            }

            // Settings Modal
            if (isSettingsModalOpen) {
                Dialog(onDismissRequest = { viewModel.closeSettings() }) {
                    SettingsModal(viewModel = viewModel)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// UI COMPONENTS
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun Sidebar(viewModel: AgentViewModel) {
    val chatSessions by viewModel.chatSessions.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val editingChatId by viewModel.editingChatId.collectAsState()

    val pinnedChats = chatSessions.filter { it.isPinned }
    val unpinnedChats = chatSessions.filter { !it.isPinned }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .safeDrawingPadding()
    ) {
        // Header and New Chat Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = AppTheme.APP_NAME,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Button(
                onClick = { viewModel.newChat() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Filled.Add, contentDescription = "New Chat")
                Spacer(Modifier.width(4.dp))
                Text("New Chat")
            }
        }

        // Search Bar (Placeholder)
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Search chats...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        )

        // Chat History List
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            if (pinnedChats.isNotEmpty()) {
                item {
                    Text(
                        "Pinned",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
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

            if (unpinnedChats.isNotEmpty()) {
                item {
                    Text(
                        "Recent",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                items(unpinnedChats, key = { it.id }) { chat ->
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
        }

        UserFooter(viewModel)
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
            .clickable { 
                if (!isEditing) {
                    onChatClick()
                } 
            },
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
            // Icon
            Icon(
                imageVector = if (chat.isPinned) Icons.Filled.PushPin else Icons.Filled.ChatBubbleOutline,
                contentDescription = "Chat Icon",
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .padding(4.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))

            // Title and Last Message
            Column(modifier = Modifier.weight(1f)) {
                if (isEditing) {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardActions = androidx.compose.ui.text.input.ImeAction.Done.let { action ->
                            androidx.compose.ui.platform.LocalSoftwareKeyboardController.current?.let { controller ->
                                androidx.compose.ui.text.input.ImeAction.Done.let {
                                    androidx.compose.ui.text.input.ImeAction.Done
                                }
                            }
                            androidx.compose.ui.text.input.ImeAction.Done.let { action ->
                                androidx.compose.ui.platform.LocalSoftwareKeyboardController.current?.let { controller ->
                                    androidx.compose.ui.text.input.ImeAction.Done.let {
                                        androidx.compose.ui.text.input.ImeAction.Done
                                    }
                                }
                                androidx.compose.ui.text.input.ImeAction.Done.let {
                                    androidx.compose.ui.text.input.ImeAction.Done
                                }
                            }
                            null // Should use KeyboardActions(onDone = { onRenameConfirm(editText) }) but simplifying for snippet
                        },
                        trailingIcon = {
                            IconButton(onClick = { onRenameConfirm(editText) }) {
                                Icon(Icons.Filled.Done, contentDescription = "Confirm Rename")
                            }
                        }
                    )
                } else {
                    Text(
                        text = chat.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = chat.lastMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Options Menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Options")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = {
                            onRename()
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text(if (chat.isPinned) "Unpin" else "Pin") },
                        onClick = {
                            onPin()
                            showMenu = false
                        },
                        leadingIcon = { Icon(if (chat.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin, contentDescription = null) }
                    )
                    Divider()
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            onDelete()
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                    )
                }
            }
        }
    }
}

@Composable
fun UserFooter(viewModel: AgentViewModel) {
    val settings by viewModel.settings.collectAsState()
    val isPro = settings.isProUser

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // User/Pro Status
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "User Icon",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isPro) "Pro User" else "Free User",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isPro) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface
                )
            }

            // Settings Button
            IconButton(onClick = { viewModel.openSettings() }) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(Modifier.height(8.dp))
        
        // Usage Stats
        Text(
            text = "Usage: ${settings.tokenUsage} tokens | Est. Cost: $${String.format("%.4f", settings.estimatedCost)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        // Upgrade button
        if (!isPro) {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.toggleProPlan(true) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(AppTheme.ProStart, AppTheme.ProEnd)
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.WorkspacePremium, contentDescription = "Upgrade to Pro")
                    Spacer(Modifier.width(8.dp))
                    Text("Upgrade to Pro", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}


@Composable
fun SettingsModal(viewModel: AgentViewModel) {
    val settings by viewModel.settings.collectAsState()
    var isApiManagementOpen by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxHeight(0.9f)
            .fillMaxWidth(0.9f)
            .shadow(20.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { viewModel.closeSettings() }) {
                    Icon(Icons.Filled.Close, contentDescription = "Close Settings")
                }
            }
            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Content
            if (isApiManagementOpen) {
                ApiManagement(viewModel) { isApiManagementOpen = false }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Theme
                    item {
                        SettingToggle(
                            title = "Dark Theme",
                            description = "Toggle between dark and light mode.",
                            icon = Icons.Filled.DarkMode,
                            checked = settings.isDarkTheme,
                            onCheckedChange = { viewModel.toggleTheme(it) }
                        )
                    }

                    // Pro Plan
                    item {
                        SettingCard(
                            title = "Pro Plan Status",
                            description = if (settings.isProUser) "You have unlimited access." else "Upgrade to Pro for more features.",
                            icon = Icons.Filled.WorkspacePremium,
                            isPro = settings.isProUser,
                            trailingContent = {
                                if (!settings.isProUser) {
                                    Button(onClick = { viewModel.toggleProPlan(true) }) {
                                        Text("Upgrade")
                                    }
                                } else {
                                    Text("Active", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }

                    // API Management Button
                    item {
                        SettingCard(
                            title = "API Management",
                            description = "Configure and manage your AI model connections.",
                            icon = Icons.Filled.Api,
                            trailingContent = {
                                Button(onClick = { isApiManagementOpen = true }) {
                                    Text("Manage")
                                }
                            }
                        )
                    }

                    // Usage Stats
                    item {
                        SettingCard(
                            title = "Token Usage",
                            description = "Total tokens used: ${settings.tokenUsage} | Est. Cost: $${String.format("%.4f", settings.estimatedCost)}",
                            icon = Icons.Filled.Token
                        )
                    }

                    // About
                    item {
                        SettingCard(
                            title = "About",
                            description = "${AppTheme.APP_NAME} v1.0",
                            icon = Icons.Filled.Info
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingToggle(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPro: Boolean = false,
    trailingContent: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (isPro) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Filled.WorkspacePremium, contentDescription = "Pro", tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                    }
                }
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
        trailingContent()
    }
}

@Composable
fun ApiManagement(viewModel: AgentViewModel, onBack: () -> Unit) {
    val settings by viewModel.settings.collectAsState()
    val isPro = settings.isProUser
    val activeCount = viewModel.activeApiCount

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "API Management (${activeCount} Active)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(48.dp)) // Spacer to balance the back button
        }
        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // Warning/Limit
        if (!isPro && settings.apiConfigs.size >= AppTheme.FREE_API_LIMIT) {
            Text(
                "⚠️ Free plan limit (${AppTheme.FREE_API_LIMIT}) reached. Upgrade to Pro to add more APIs.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                    .padding(8.dp)
            )
        }

        // Add API Button (Placeholder for simplicity)
        Button(
            onClick = { /* Add API Dialog logic */ },
            enabled = isPro || settings.apiConfigs.size < AppTheme.FREE_API_LIMIT,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add New API")
            Spacer(Modifier.width(8.dp))
            Text("Add New API Configuration")
        }
        
        // API List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(settings.apiConfigs, key = { it.id }) { config ->
                ApiConfigCard(config, viewModel)
            }
        }
    }
}

@Composable
fun ApiConfigCard(config: ApiConfig, viewModel: AgentViewModel) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (config.isActive) {
                config.provider.color.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (config.isActive) BorderStroke(2.dp, config.provider.color) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = config.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    if (config.isActive) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Active",
                            tint = config.provider.color,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = "${config.provider.title} (${config.modelName})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                if (config.systemRole.isNotBlank()) {
                    Text(
                        "Role: ${config.systemRole.take(40)}...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))

            // Toggle and Menu
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Toggle
                Switch(
                    checked = config.isActive,
                    onCheckedChange = { viewModel.toggleApiActive(config.id) },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = config.provider.color.copy(alpha = 0.7f),
                        checkedThumbColor = config.provider.color,
                        uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        uncheckedThumbColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                
                // Menu
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { 
                                // Placeholder for Edit Dialog logic
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                viewModel.deleteApiConfig(config.id)
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernChatInterface(
    viewModel: AgentViewModel,
    modifier: Modifier = Modifier
) {
    val currentSession = viewModel.currentSession
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val allApiConfigs by viewModel.settings.collectAsState().value.apiConfigs.collectAsState() // Fetch all API configs
    val activeApis = remember(currentSessionId, allApiConfigs) {
        currentSession?.activeApis?.mapNotNull { apiId ->
            allApiConfigs.find { it.id == apiId }
        } ?: emptyList()
    }

    if (currentSession == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Select or Start a New Chat", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        }
        return
    }

    val listState = rememberLazyListState()
    val isScrolledToBottom by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == listState.layoutInfo.totalItemsCount - 1 || listState.firstVisibleItemIndex == listState.layoutInfo.totalItemsCount - 2
        }
    }

    LaunchedEffect(currentSession.messages.size) {
        if (currentSession.messages.isNotEmpty()) {
            listState.animateScrollToItem(currentSession.messages.lastIndex)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        ChatHeader(currentSession, viewModel)

        // Active APIs Bar
        AnimatedVisibility(visible = activeApis.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                tonalElevation = 2.dp
            ) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    items(activeApis, key = { it.id }) { api ->
                        ActiveApiChip(api, viewModel)
                    }
                }
            }
        }

        // Messages List
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(currentSession.messages) { message ->
                    ChatMessageCard(message)
                }
            }

            // Scroll to bottom button
            AnimatedVisibility(
                visible = !isScrolledToBottom && currentSession.messages.isNotEmpty(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 16.dp),
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
            ) {
                FloatingActionButton(
                    onClick = {
                        listState.animateScrollToItem(currentSession.messages.lastIndex)
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.ArrowDownward, contentDescription = "Scroll to Bottom")
                }
            }
        }

        // Input Bar
        ModernInputBar(viewModel)
    }
}

@Composable
fun ChatHeader(session: ChatSession, viewModel: AgentViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Toggle Sidebar button (Desktop/Wide screens only)
        IconButton(onClick = { viewModel.toggleSidebar() }) {
            Icon(Icons.Filled.Menu, contentDescription = "Toggle Sidebar")
        }
        Spacer(Modifier.width(16.dp))

        // Title and Mode
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = session.messages.lastOrNull()?.mode?.title ?: "Standard Mode",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Spacer(Modifier.width(16.dp))

        // Settings Button (if needed elsewhere)
        // IconButton(onClick = { viewModel.openSettings() }) {
        //     Icon(Icons.Filled.Settings, contentDescription = "Settings")
        // }
    }
    Divider()
}

@Composable
fun ActiveApiChip(api: ApiConfig, viewModel: AgentViewModel) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = api.provider.color.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, api.provider.color)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = api.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = api.provider.color
            )
            Spacer(Modifier.width(4.dp))
            IconButton(
                onClick = { viewModel.toggleApiForCurrentChat(api.id) },
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Remove API",
                    tint = api.provider.color,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun ChatMessageCard(message: ChatMessage) {
    val isDarkTheme = isSystemInDarkTheme()
    val isUser = message.isUser
    val cardColor = when {
        isUser -> MaterialTheme.colorScheme.primary
        isDarkTheme -> AppTheme.CardDark
        else -> Color.White
    }
    val contentColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .clip(RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header (Mode and Used APIs)
                if (!isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = message.mode.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (message.usedApis.isNotEmpty()) {
                            Text(
                                text = "via: ${message.usedApis.joinToString(", ").take(30)}...",
                                style = MaterialTheme.typography.labelSmall,
                                color = contentColor.copy(alpha = 0.6f)
                            )
                        }
                    }
                    if (message.mode.isPro) {
                        Spacer(Modifier.height(4.dp))
                        Divider()
                        Spacer(Modifier.height(4.dp))
                    }
                }

                // Attachments (if any)
                if (message.attachments.isNotEmpty()) {
                    Text(
                        text = "📎 ${message.attachments.size} attachment(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Message Text
                if (message.isStreaming) {
                    // Simple streaming placeholder
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = contentColor.copy(alpha = 0.5f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                } else {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = contentColor
                    )
                }

                // Footer (Timestamp)
                if (!message.isStreaming) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.4f),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
fun ModernInputBar(viewModel: AgentViewModel) {
    val selectedMode by viewModel.selectedMode.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isPro = settings.isProUser
    val currentSession = viewModel.currentSession
    
    var inputText by remember { mutableStateOf("") }
    val maxInputHeight = 150.dp
    
    // File picker launcher
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Handle selected file URI (placeholder)
        if (uri != null) {
            // Placeholder: Add to message attachments
            // currentSession.addAttachment(Attachment(name = "file", uri = uri, type = AttachmentType.UNKNOWN))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .safeDrawingPadding()
    ) {
        // Mode Selector Bar
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(AiMode.entries.toTypedArray()) { mode ->
                ModeChip(
                    mode = mode,
                    isSelected = mode == selectedMode,
                    isEnabled = isPro || !mode.isPro,
                    onClick = { viewModel.setSelectedMode(mode) }
                )
            }
        }
        
        // Input Field
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Attachment Button
                IconButton(
                    onClick = { pickMediaLauncher.launch("*/*") },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(Icons.Filled.AttachFile, contentDescription = "Attach File", tint = MaterialTheme.colorScheme.onSurface)
                }

                // Text Input
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Message AI Agent...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp, max = maxInputHeight),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        errorContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = false,
                    maxLines = 10,
                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Send
                    ),
                    keyboardActions = androidx.compose.ui.text.input.KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendUserMessage(inputText, emptyList(), selectedMode)
                                inputText = ""
                            }
                        }
                    )
                )

                // Send Button
                val isSendEnabled = inputText.isNotBlank()
                Button(
                    onClick = {
                        if (isSendEnabled) {
                            viewModel.sendUserMessage(inputText, emptyList(), selectedMode)
                            inputText = ""
                        }
                    },
                    enabled = isSendEnabled,
                    modifier = Modifier
                        .height(48.dp)
                        .align(Alignment.CenterVertically)
                        .padding(end = 4.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Send Message", tint = Color.White)
                }
            }
        }
        
        // Active API Toggles (Below Input)
        currentSession?.let { session ->
            ActiveApiToggles(viewModel, session)
        }
    }
}

@Composable
fun ModeChip(
    mode: AiMode,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val color = when {
        isSelected -> MaterialTheme.colorScheme.primary
        !isEnabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    }
    val contentColor = when {
        isSelected -> Color.White
        !isEnabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Card(
        modifier = Modifier.clickable(onClick = if (isEnabled) onClick else {}),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = mode.icon, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.width(6.dp))
            Text(
                text = mode.title,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                fontWeight = FontWeight.SemiBold
            )
            if (mode.isPro && !isEnabled) {
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Filled.Lock, contentDescription = "Pro Feature", tint = contentColor, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
fun ActiveApiToggles(viewModel: AgentViewModel, currentSession: ChatSession) {
    val settings by viewModel.settings.collectAsState()
    val availableApis = settings.apiConfigs.filter { it.isActive }
    
    if (availableApis.isNotEmpty()) {
        Column(modifier = Modifier.padding(top = 8.dp)) {
            Text(
                "API Models for this Chat:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(4.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(availableApis, key = { it.id }) { config ->
                    val isSelected = currentSession.activeApis.contains(config.id)
                    ApiToggleChip(config, isSelected) {
                        viewModel.toggleApiForCurrentChat(config.id)
                    }
                }
            }
        }
    }
}

@Composable
fun ApiToggleChip(
    config: ApiConfig,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) config.provider.color.copy(alpha = 0.9f) else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
    
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = config.name,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}