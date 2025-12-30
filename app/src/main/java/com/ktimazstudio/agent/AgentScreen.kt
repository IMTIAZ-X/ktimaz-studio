package com.ktimazstudio.agent

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
// MODERN DATA MODELS & CONSTANTS
// ═══════════════════════════════════════════════════════════════════════════════

data class Attachment(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: AttachmentType,
    val content: String,
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
    val reactions: List<String> = emptyList(),
    val isEdited: Boolean = false
)

data class ChatHistory(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val lastMessage: String,
    val timestamp: Long,
    val messageCount: Int = 0,
    val isPinned: Boolean = false,
    val tags: List<String> = emptyList()
)

data class AppSettings(
    val isProUser: Boolean = false,
    val useCustomApi: Boolean = false,
    val currentProvider: AiProvider = AiProvider.GEMINI,
    val tokenUsage: Int = 0,
    val estimatedCost: Double = 0.0,
    val isDarkTheme: Boolean = true,
    val enableAnimations: Boolean = true,
    val enableSoundEffects: Boolean = false,
    val autoSaveChats: Boolean = true,
    val compactMode: Boolean = false
)

enum class AiMode(val title: String, val promptTag: String, val icon: String, val isPro: Boolean = false) {
    STANDARD("Standard Chat", "", "💬"),
    THINKING("Thinking Mode", "[THINKING_MODE]", "🧠", true),
    RESEARCH("Deep Research", "[RESEARCH_MODE]", "🔬", true),
    STUDY("Study Mode", "[STUDY_MODE]", "📚", true),
    CODE("Code Assistant", "[CODE_MODE]", "💻", true),
    CREATIVE("Creative Writing", "[CREATIVE_MODE]", "✨", true)
}

enum class AiProvider(val title: String, val color: Color) {
    GEMINI("Google Gemini", Color(0xFF4285F4)),
    CHATGPT("OpenAI ChatGPT", Color(0xFF10A37F)),
    CLAUDE("Anthropic Claude", Color(0xFFCC785C)),
    GROK("Grok", Color(0xFF000000)),
    DEEPSEEK("DeepSeek", Color(0xFF6366F1)),
    LOCAL_LLM("Local LLM", Color(0xFF8B5CF6))
}

object ModernTheme {
    const val APP_NAME = "AI Agent zzz"
    const val CREATOR_NAME = "zzz"

    // Glassmorphism colors
    val GlassBackground = Color(0x33FFFFFF)
    val GlassBorder = Color(0x44FFFFFF)
    
    // Gradient colors
    val PrimaryGradientStart = Color(0xFF667EEA)
    val PrimaryGradientEnd = Color(0xFF764BA2)
    val SecondaryGradientStart = Color(0xFFF093FB)
    val SecondaryGradientEnd = Color(0xFFF5576C)
    val ProGradientStart = Color(0xFFFFD89B)
    val ProGradientEnd = Color(0xFFFF6B6B)
    
    // Surface colors
    val SurfaceDark = Color(0xFF0F0F1E)
    val SurfaceLight = Color(0xFFF8F9FE)
    val CardDark = Color(0xFF1A1A2E)
    val CardLight = Color(0xFFFFFFFF)
}

// ═══════════════════════════════════════════════════════════════════════════════
// ADVANCED VIEWMODEL WITH ENHANCED STATE MANAGEMENT
// ═══════════════════════════════════════════════════════════════════════════════

class AgentViewModel : ViewModel() {
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _currentChat = MutableStateFlow<List<ChatMessage>>(emptyList())
    val currentChat: StateFlow<List<ChatMessage>> = _currentChat.asStateFlow()

    private val _chatHistory = MutableStateFlow<List<ChatHistory>>(
        listOf(
            ChatHistory(
                title = "Advanced Kotlin Patterns",
                lastMessage = "Let me explain sealed classes...",
                timestamp = System.currentTimeMillis(),
                messageCount = 12,
                isPinned = true,
                tags = listOf("Kotlin", "Programming")
            ),
            ChatHistory(
                title = "UI/UX Design Principles",
                lastMessage = "Glassmorphism is trending...",
                timestamp = System.currentTimeMillis() - 3600000,
                messageCount = 8,
                tags = listOf("Design", "UI")
            )
        )
    )
    val chatHistory: StateFlow<List<ChatHistory>> = _chatHistory.asStateFlow()

    private val _isSidebarOpen = MutableStateFlow(true)
    val isSidebarOpen: StateFlow<Boolean> = _isSidebarOpen.asStateFlow()

    private val _isSettingsModalOpen = MutableStateFlow(false)
    val isSettingsModalOpen: StateFlow<Boolean> = _isSettingsModalOpen.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedMode = MutableStateFlow(AiMode.STANDARD)
    val selectedMode: StateFlow<AiMode> = _selectedMode.asStateFlow()

    fun toggleSidebar() { _isSidebarOpen.value = !_isSidebarOpen.value }
    fun openSettings() { _isSettingsModalOpen.value = true }
    fun closeSettings() { _isSettingsModalOpen.value = false }
    fun toggleProPlan(isPro: Boolean) { _settings.value = _settings.value.copy(isProUser = isPro) }
    fun toggleTheme(isDark: Boolean) { _settings.value = _settings.value.copy(isDarkTheme = isDark) }
    fun setUseCustomApi(use: Boolean) { _settings.value = _settings.value.copy(useCustomApi = use) }
    fun setCurrentProvider(provider: AiProvider) { _settings.value = _settings.value.copy(currentProvider = provider) }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setSelectedMode(mode: AiMode) { _selectedMode.value = mode }

    fun newChat() {
        if (_currentChat.value.isNotEmpty()) {
            val firstMessage = _currentChat.value.firstOrNull()?.text ?: "New Chat"
            _chatHistory.value = listOf(
                ChatHistory(
                    title = firstMessage.take(40) + if (firstMessage.length > 40) "..." else "",
                    lastMessage = _currentChat.value.last().text,
                    timestamp = System.currentTimeMillis(),
                    messageCount = _currentChat.value.size
                )
            ) + _chatHistory.value
        }
        _currentChat.value = emptyList()
        _selectedMode.value = AiMode.STANDARD
    }

    fun sendUserMessage(text: String, attachments: List<Attachment>, mode: AiMode) {
        val settings = _settings.value
        val isPro = settings.isProUser

        if (!isPro && attachments.size > 2) {
            appendAiMessage("⚠️ Free plan limited to 2 attachments. Upgrade to Pro for unlimited uploads.", isError = true)
            return
        }

        if (!isPro && mode.isPro) {
            appendAiMessage("🔒 ${mode.title} requires Pro. Upgrade to unlock all AI modes!", isError = true)
            return
        }

        val userMessage = ChatMessage(
            text = text.trim(),
            isUser = true,
            attachments = attachments,
            mode = mode
        )
        _currentChat.value = _currentChat.value + userMessage

        viewModelScope.launch {
            _settings.value = settings.copy(
                tokenUsage = settings.tokenUsage + 150,
                estimatedCost = settings.estimatedCost + 0.00075
            )

            delay(800)
            _currentChat.value = _currentChat.value + ChatMessage(
                text = "...",
                isUser = false,
                isStreaming = true
            )

            delay(1500)
            val reply = generateAiReply(userMessage, settings)
            _currentChat.value = _currentChat.value.dropLast(1) + ChatMessage(
                text = reply,
                isUser = false,
                mode = mode
            )
        }
    }

    private fun generateAiReply(userMessage: ChatMessage, settings: AppSettings): String {
        if (!settings.useCustomApi) {
            return "✨ **Simulated Meta AI Response**\n\nYou asked: \"${userMessage.text.take(100)}\"\n\nThis is a demo response. Enable Custom API in Settings to use real AI providers."
        }

        return when (userMessage.mode) {
            AiMode.THINKING -> """
                🧠 **Thinking Mode Activated**
                
                **Analysis Process:**
                1. Understanding the query context
                2. Breaking down key components
                3. Formulating structured response
                
                **Final Answer:** Based on deep reasoning, here's my comprehensive response to your question about: "${userMessage.text.take(50)}..."
            """.trimIndent()
            
            AiMode.RESEARCH -> """
                🔬 **Deep Research Mode**
                
                **Research Summary:**
                Topic: ${userMessage.text.take(50)}
                
                **Key Findings:**
                • Comprehensive analysis completed
                • Multiple sources consulted (simulated)
                • Evidence-based conclusions drawn
                
                **Conclusion:** This research indicates significant implications for your query.
            """.trimIndent()
            
            AiMode.STUDY -> """
                📚 **Study Mode - Expert Tutor**
                
                **Lesson Plan:**
                Let me break this down simply...
                
                **Key Concepts:**
                ✓ Core principle explained
                ✓ Practical applications shown
                ✓ Common misconceptions clarified
                
                **Quiz Question:** Can you summarize the main point in your own words?
            """.trimIndent()
            
            else -> "Hello! I'm **${ModernTheme.APP_NAME}**, ready to assist you. You're using the ${settings.currentProvider.title} provider on ${if (settings.isProUser) "Pro" else "Free"} plan."
        }
    }

    private fun appendAiMessage(text: String, isError: Boolean = false) {
        _currentChat.value = _currentChat.value + ChatMessage(
            text = text,
            isUser = false
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// MODERN THEME SYSTEM
// ═══════════════════════════════════════════════════════════════════════════════

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF667EEA),
    secondary = Color(0xFFF093FB),
    background = ModernTheme.SurfaceDark,
    surface = ModernTheme.CardDark,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF667EEA),
    secondary = Color(0xFFF093FB),
    background = ModernTheme.SurfaceLight,
    surface = ModernTheme.CardLight,
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun ModernAgentTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// MAIN SCREEN WITH GLASSMORPHISM & ANIMATIONS
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
// GLASSMORPHIC TOP BAR
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTopBar(viewModel: AgentViewModel) {
    val settings by viewModel.settings.collectAsState()
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp),
        color = if (settings.isDarkTheme) {
            ModernTheme.CardDark.copy(alpha = 0.8f)
        } else {
            ModernTheme.CardLight.copy(alpha = 0.9f)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.toggleSidebar() }) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    ModernTheme.APP_NAME,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                ModernTheme.PrimaryGradientStart,
                                ModernTheme.PrimaryGradientEnd
                            )
                        )
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                            .scale(pulseAlpha)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Active • ${settings.tokenUsage} tokens • \$${String.format("%.4f", settings.estimatedCost)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            if (settings.isProUser) {
                ProBadge()
            }

            Spacer(Modifier.width(8.dp))

            IconButton(onClick = { viewModel.openSettings() }) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ProBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        ModernTheme.ProGradientStart,
                        ModernTheme.ProGradientEnd
                    )
                )
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Build,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "PRO",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// MODERN SIDEBAR WITH SEARCH & GROUPING
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ModernSidebar(viewModel: AgentViewModel) {
    val settings by viewModel.settings.collectAsState()
    val chatHistory by viewModel.chatHistory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Surface(
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight(),
        color = if (settings.isDarkTheme) {
            ModernTheme.CardDark.copy(alpha = 0.6f)
        } else {
            ModernTheme.CardLight.copy(alpha = 0.9f)
        }
    ) {
        Column(Modifier.padding(16.dp)) {
            Button(
                onClick = { viewModel.newChat() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("New Chat", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search chats...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                  //  focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                  //  unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                  
                  focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                  unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                )
            )

            Spacer(Modifier.height(16.dp))

            LazyColumn(Modifier.weight(1f)) {
                item {
                    Text(
                        "Pinned",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(chatHistory.filter { it.isPinned }) { chat ->
                    ModernChatHistoryCard(chat)
                }
                
                item {
                    Text(
                        "Recent",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp, top = 16.dp)
                    )
                }
                items(chatHistory.filter { !it.isPinned }) { chat ->
                    ModernChatHistoryCard(chat)
                }
            }

            ModernUserFooter(viewModel)
        }
    }
}

@Composable
fun ModernChatHistoryCard(chat: ChatHistory) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                ModernTheme.PrimaryGradientStart,
                                ModernTheme.PrimaryGradientEnd
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ChatBubble,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    chat.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${chat.messageCount} messages",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    if (chat.tags.isNotEmpty()) {
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "• ${chat.tags.first()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (chat.isPinned) {
                Icon(
                    Icons.Default.PushPin,
                    contentDescription = "Pinned",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ModernUserFooter(viewModel: AgentViewModel) {
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
                                colors = listOf(
                                    ModernTheme.SecondaryGradientStart.copy(alpha = 0.3f),
                                    ModernTheme.SecondaryGradientEnd.copy(alpha = 0.3f)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            ModernTheme.GlassBorder,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Rocket,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Upgrade to Pro",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Unlock all AI modes, unlimited uploads & priority support",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                ModernTheme.PrimaryGradientStart,
                                ModernTheme.PrimaryGradientEnd
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "A",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    "Agent User",
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (settings.isProUser) "Pro Account" else "Free Account",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (settings.isProUser) {
                        ModernTheme.ProGradientStart
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// MODERN CHAT INTERFACE WITH ENHANCED UX
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ModernChatInterface(viewModel: AgentViewModel) {
    val messages by viewModel.currentChat.collectAsState()
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

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (messages.isEmpty()) {
            ModernEmptyState(viewModel)
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                reverseLayout = true,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages.reversed(), key = { it.id }) { msg ->
                    ModernMessageBubble(msg, settings.isDarkTheme)
                }
            }
        }

        ModernInputBar(
            input = input,
            onInputChange = { input = it },
            onSend = {
                viewModel.sendUserMessage(input, attachedFiles.toList(), selectedMode)
                input = ""
                attachedFiles.clear()
            },
            attachedFiles = attachedFiles,
            viewModel = viewModel,
            selectedMode = selectedMode
        )
    }
}

@Composable
fun ModernEmptyState(viewModel: AgentViewModel) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = offsetY.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                ModernTheme.PrimaryGradientStart.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Welcome to ${ModernTheme.APP_NAME}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Your intelligent AI workspace awaits",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AiMode.values().take(3).forEach { mode ->
                    ModernModeChip(mode, onClick = { viewModel.setSelectedMode(mode) })
                }
            }
        }
    }
}

@Composable
fun ModernModeChip(mode: AiMode, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(mode.icon, fontSize = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                mode.title.split(" ").first(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ModernMessageBubble(msg: ChatMessage, isDarkTheme: Boolean) {
    val alignment = if (msg.isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (msg.isUser) {
        Brush.linearGradient(
            colors = listOf(
                ModernTheme.PrimaryGradientStart,
                ModernTheme.PrimaryGradientEnd
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surface
            )
        )
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
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    ModernTheme.SecondaryGradientStart,
                                    ModernTheme.SecondaryGradientEnd
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Psychology,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
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
                            items(msg.attachments) { attachment ->
                                ModernAttachmentPreview(attachment)
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
                                color = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Text(
                            msg.text,
                            color = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            if (msg.isUser) {
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    ModernTheme.PrimaryGradientStart,
                                    ModernTheme.PrimaryGradientEnd
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "A",
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        if (!msg.isStreaming) {
            Spacer(Modifier.height(4.dp))
            Text(
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = if (msg.isUser) 52.dp else 52.dp)
            )
        }
    }
}

@Composable
fun ModernAttachmentPreview(attachment: Attachment) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (attachment.isImage) Icons.Default.Image else Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                attachment.name,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 100.dp)
            )
        }
    }
}

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
                    ModernAttachmentChip(attachment, onRemove = { attachedFiles.remove(attachment) })
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
                    Text(
                        "${selectedMode.title} Active",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.setSelectedMode(AiMode.STANDARD) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove mode", modifier = Modifier.size(18.dp))
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
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = isMenuOpen,
                        onDismissRequest = { isMenuOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add Image") },
                            onClick = {
                                attachedFiles.add(
                                    Attachment(
                                        name = "image_${System.currentTimeMillis()}.jpg",
                                        type = AttachmentType.IMAGE,
                                        content = "base64_sim"
                                    )
                                )
                                isMenuOpen = false
                            },
                            leadingIcon = { Icon(Icons.Default.Image, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Add File") },
                            onClick = {
                                attachedFiles.add(
                                    Attachment(
                                        name = "document.pdf",
                                        type = AttachmentType.PDF,
                                        content = "content_sim"
                                    )
                                )
                                isMenuOpen = false
                            },
                            leadingIcon = { Icon(Icons.Default.AttachFile, null) }
                        )
                    }
                }

                Box {
                    IconButton(onClick = { isModeMenuOpen = !isModeMenuOpen }) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = "AI Modes",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = isModeMenuOpen,
                        onDismissRequest = { isModeMenuOpen = false }
                    ) {
                        AiMode.values().forEach { mode ->
                            val isLocked = mode.isPro && !settings.isProUser
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(mode.icon)
                                        Spacer(Modifier.width(8.dp))
                                        Text(mode.title)
                                    }
                                },
                                onClick = {
                                    if (!isLocked) {
                                        viewModel.setSelectedMode(mode)
                                        isModeMenuOpen = false
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        if (isLocked) Icons.Default.Lock else Icons.Default.CheckCircle,
                                        null,
                                        tint = if (isLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = if (isLocked) {
                                    { ProBadge() }
                                } else null
                            )
                        }
                    }
                }

                TextField(
                    value = input,
                    onValueChange = onInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message ${ModernTheme.APP_NAME}...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    minLines = 1,
                    maxLines = 5
                )

                IconButton(
                    onClick = onSend,
                    enabled = input.isNotBlank() || attachedFiles.isNotEmpty(),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (input.isNotBlank() || attachedFiles.isNotEmpty()) {
                                Brush.linearGradient(
                                    colors = listOf(
                                        ModernTheme.PrimaryGradientStart,
                                        ModernTheme.PrimaryGradientEnd
                                    )
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.3f))
                                )
                            }
                        )
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ModernAttachmentChip(attachment: Attachment, onRemove: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (attachment.isImage) Icons.Default.Image else Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(6.dp))
            Text(
                attachment.name,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 120.dp)
            )
            Spacer(Modifier.width(4.dp))
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// MODERN SETTINGS MODAL
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ModernSettingsModal(viewModel: AgentViewModel) {
    val settings by viewModel.settings.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = { viewModel.closeSettings() },
        modifier = Modifier.widthIn(max = 700.dp),
        title = {
            Text(
                "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Column {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent
                ) {
                    listOf("General", "AI Models", "Plan").forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                when (selectedTab) {
                    0 -> ModernGeneralSettings(viewModel, settings)
                    1 -> ModernAISettings(viewModel, settings)
                    2 -> ModernPlanSettings(viewModel, settings)
                }
            }
        },
        confirmButton = {
            Button(onClick = { viewModel.closeSettings() }) {
                Text("Done")
            }
        }
    )
}

@Composable
fun ModernGeneralSettings(viewModel: AgentViewModel, settings: AppSettings) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SettingRow(
            title = "Dark Mode",
            subtitle = "Use dark color scheme",
            trailing = {
                Switch(
                    checked = settings.isDarkTheme,
                    onCheckedChange = { viewModel.toggleTheme(it) }
                )
            }
        )

        SettingRow(
            title = "Animations",
            subtitle = "Enable UI animations",
            trailing = {
                Switch(
                    checked = settings.enableAnimations,
                    onCheckedChange = { }
                )
            }
        )

        SettingRow(
            title = "Auto-Save",
            subtitle = "Automatically save chat history",
            trailing = {
                Switch(
                    checked = settings.autoSaveChats,
                    onCheckedChange = { }
                )
            }
        )
    }
}

@Composable
fun ModernAISettings(viewModel: AgentViewModel, settings: AppSettings) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SettingRow(
            title = "Custom API Mode",
            subtitle = "Use your own API keys",
            trailing = {
                Switch(
                    checked = settings.useCustomApi,
                    onCheckedChange = { viewModel.setUseCustomApi(it) }
                )
            }
        )

        Text(
            "AI Provider",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        AiProvider.values().forEach { provider ->
            Card(
                onClick = { viewModel.setCurrentProvider(provider) },
                colors = CardDefaults.cardColors(
                    containerColor = if (settings.currentProvider == provider) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(provider.color)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(provider.title, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun ModernPlanSettings(viewModel: AgentViewModel, settings: AppSettings) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (settings.isProUser) "Pro Plan" else "Free Plan",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black
                    )
                    if (settings.isProUser) {
                        Spacer(Modifier.width(8.dp))
                        ProBadge()
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    if (settings.isProUser) 
                        "You have access to all premium features" 
                    else 
                        "Upgrade to unlock all AI modes and unlimited uploads",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Button(
            onClick = { viewModel.toggleProPlan(!settings.isProUser) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text(
                if (settings.isProUser) "Downgrade to Free (Demo)" else "Upgrade to Pro",
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            "Pro Features",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        listOf(
            "Unlimited chat conversations",
            "Unlimited file uploads",
            "All AI modes (Thinking, Research, Study, Code)",
            "Priority response times",
            "Advanced analytics",
            "Custom AI personalities"
        ).forEach { feature ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(feature)
            }
        }
    }
}

@Composable
fun SettingRow(
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        trailing()
    }
}