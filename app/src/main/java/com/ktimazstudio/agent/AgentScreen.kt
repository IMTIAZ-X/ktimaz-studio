package com.ktimazstudio.agent

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

// DATA MODELS
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
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatHistory(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val lastMessage: String,
    val timestamp: Long,
    val messageCount: Int = 0,
    val isPinned: Boolean = false
)

data class AppSettings(
    val isProUser: Boolean = false,
    val useCustomApi: Boolean = false,
    val currentProvider: AiProvider = AiProvider.GEMINI,
    val tokenUsage: Int = 0,
    val estimatedCost: Double = 0.0,
    val isDarkTheme: Boolean = true
)

enum class AiMode(val title: String, val promptTag: String, val icon: String, val isPro: Boolean = false) {
    STANDARD("Standard", "", "💬"),
    THINKING("Thinking", "[THINKING]", "🧠", true),
    RESEARCH("Research", "[RESEARCH]", "🔬", true),
    STUDY("Study", "[STUDY]", "📚", true),
    CODE("Code", "[CODE]", "💻", true)
}

enum class AiProvider(val title: String, val color: Color) {
    GEMINI("Google Gemini", Color(0xFF4285F4)),
    CHATGPT("OpenAI ChatGPT", Color(0xFF10A37F)),
    CLAUDE("Anthropic Claude", Color(0xFFCC785C)),
    GROK("Grok", Color(0xFF000000)),
    DEEPSEEK("DeepSeek", Color(0xFF6366F1)),
    LOCAL_LLM("Local LLM", Color(0xFF8B5CF6))
}

object AppTheme {
    const val APP_NAME = "AI Agent zzz"
    val PrimaryStart = Color(0xFF667EEA)
    val PrimaryEnd = Color(0xFF764BA2)
    val ProStart = Color(0xFFFFD89B)
    val ProEnd = Color(0xFFFF6B6B)
    val SurfaceDark = Color(0xFF0F0F1E)
    val CardDark = Color(0xFF1A1A2E)
}

// VIEWMODEL
class AgentViewModel : ViewModel() {
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _currentChat = MutableStateFlow<List<ChatMessage>>(emptyList())
    val currentChat: StateFlow<List<ChatMessage>> = _currentChat.asStateFlow()

    private val _chatHistory = MutableStateFlow<List<ChatHistory>>(
        listOf(
            ChatHistory(
                title = "Advanced Kotlin Patterns",
                lastMessage = "Let me explain...",
                timestamp = System.currentTimeMillis(),
                messageCount = 12,
                isPinned = true
            ),
            ChatHistory(
                title = "UI/UX Design",
                lastMessage = "Great question!",
                timestamp = System.currentTimeMillis() - 3600000,
                messageCount = 8
            )
        )
    )
    val chatHistory: StateFlow<List<ChatHistory>> = _chatHistory.asStateFlow()

    private val _isSidebarOpen = MutableStateFlow(true)
    val isSidebarOpen: StateFlow<Boolean> = _isSidebarOpen.asStateFlow()

    private val _isSettingsModalOpen = MutableStateFlow(false)
    val isSettingsModalOpen: StateFlow<Boolean> = _isSettingsModalOpen.asStateFlow()

    private val _selectedMode = MutableStateFlow(AiMode.STANDARD)
    val selectedMode: StateFlow<AiMode> = _selectedMode.asStateFlow()

    fun toggleSidebar() { _isSidebarOpen.value = !_isSidebarOpen.value }
    fun openSettings() { _isSettingsModalOpen.value = true }
    fun closeSettings() { _isSettingsModalOpen.value = false }
    fun toggleProPlan(isPro: Boolean) { _settings.value = _settings.value.copy(isProUser = isPro) }
    fun toggleTheme(isDark: Boolean) { _settings.value = _settings.value.copy(isDarkTheme = isDark) }
    fun setUseCustomApi(use: Boolean) { _settings.value = _settings.value.copy(useCustomApi = use) }
    fun setCurrentProvider(provider: AiProvider) { _settings.value = _settings.value.copy(currentProvider = provider) }
    fun setSelectedMode(mode: AiMode) { _selectedMode.value = mode }

    fun newChat() {
        if (_currentChat.value.isNotEmpty()) {
            val firstMessage = _currentChat.value.firstOrNull()?.text ?: "New Chat"
            _chatHistory.value = listOf(
                ChatHistory(
                    title = firstMessage.take(40),
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
            appendAiMessage("⚠️ Free plan limited to 2 attachments. Upgrade to Pro!")
            return
        }

        if (!isPro && mode.isPro) {
            appendAiMessage("🔒 ${mode.title} requires Pro. Upgrade to unlock!")
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
            return "✨ **Simulated Response**\n\nYou asked: \"${userMessage.text.take(100)}\"\n\nEnable Custom API in Settings to use real providers."
        }

        return when (userMessage.mode) {
            AiMode.THINKING -> "🧠 **Thinking Mode**\n\nAnalyzing your query...\n\n**Answer:** Here's my detailed response."
            AiMode.RESEARCH -> "🔬 **Research Mode**\n\nConducting deep research...\n\n**Findings:** Comprehensive analysis complete."
            AiMode.STUDY -> "📚 **Study Mode**\n\nLet me explain this simply...\n\n**Quiz:** Can you summarize the key points?"
            else -> "Hello! I'm **${AppTheme.APP_NAME}**. Using ${settings.currentProvider.title} on ${if (settings.isProUser) "Pro" else "Free"} plan."
        }
    }

    private fun appendAiMessage(text: String) {
        _currentChat.value = _currentChat.value + ChatMessage(text = text, isUser = false)
    }
}

// THEME
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

// MAIN SCREEN
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
                            listOf(Color(0xFF0A0A1E), Color(0xFF1A1A2E))
                        } else {
                            listOf(Color(0xFFF8F9FE), Color(0xFFE0E7FF))
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

// TOP BAR
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTopBar(viewModel: AgentViewModel) {
    val settings by viewModel.settings.collectAsState()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (settings.isDarkTheme) {
            AppTheme.CardDark.copy(alpha = 0.8f)
        } else {
            Color.White.copy(alpha = 0.9f)
        },
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.toggleSidebar() }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    AppTheme.APP_NAME,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Tokens: ${settings.tokenUsage} • \$${String.format("%.4f", settings.estimatedCost)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            if (settings.isProUser) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AppTheme.ProStart, AppTheme.ProEnd)
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("PRO", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }

            Spacer(Modifier.width(8.dp))

            IconButton(onClick = { viewModel.openSettings() }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// SIDEBAR
@Composable
fun ModernSidebar(viewModel: AgentViewModel) {
    val settings by viewModel.settings.collectAsState()
    val chatHistory by viewModel.chatHistory.collectAsState()

    Surface(
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight(),
        color = if (settings.isDarkTheme) {
            AppTheme.CardDark.copy(alpha = 0.6f)
        } else {
            Color.White.copy(alpha = 0.9f)
        }
    ) {
        Column(Modifier.padding(16.dp)) {
            Button(
                onClick = { viewModel.newChat() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("New Chat", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(Modifier.weight(1f)) {
                item {
                    Text(
                        "Recent Chats",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(chatHistory) { chat ->
                    ChatHistoryCard(chat)
                }
            }

            UserFooter(viewModel)
        }
    }
}

@Composable
fun ChatHistoryCard(chat: ChatHistory) {
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
                            colors = listOf(AppTheme.PrimaryStart, AppTheme.PrimaryEnd)
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
                Text(chat.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                Text(
                    "${chat.messageCount} messages",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
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
                                colors = listOf(
                                    Color(0xFFF093FB).copy(alpha = 0.3f),
                                    Color(0xFFF5576C).copy(alpha = 0.3f)
                                )
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("Upgrade to Pro", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "Unlock all AI modes & unlimited uploads",
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
                            colors = listOf(AppTheme.PrimaryStart, AppTheme.PrimaryEnd)
                        )
                    ),
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
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// CHAT INTERFACE
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

    Column(modifier = Modifier.fillMaxSize()) {
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
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
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
                        "Your intelligent AI workspace",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
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
fun MessageBubble(msg: ChatMessage, isDarkTheme: Boolean) {
    val alignment = if (msg.isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (msg.isUser) {
        Brush.linearGradient(colors = listOf(AppTheme.PrimaryStart, AppTheme.PrimaryEnd))
    } else {
        Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface))
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
                        .background(Brush.linearGradient(colors = listOf(Color(0xFFF093FB), Color(0xFFF5576C)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
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
                    }
                }
            }

            if (msg.isUser) {
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(colors = listOf(AppTheme.PrimaryStart, AppTheme.PrimaryEnd))),
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

// INPUT BAR
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
                        Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(18.dp))
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
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                    }

                    DropdownMenu(expanded = isMenuOpen, onDismissRequest = { isMenuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Add Image") },
                            onClick = {
                                attachedFiles.add(
                                    Attachment(
                                        name = "image.jpg",
                                        type = AttachmentType.IMAGE,
                                        content = "base64"
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
                                        content = "content"
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
                        Icon(Icons.Default.Psychology, contentDescription = "Modes", tint = MaterialTheme.colorScheme.primary)
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
                                }
                            )
                        }
                    }
                }

                TextField(
                    value = input,
                    onValueChange = onInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message ${AppTheme.APP_NAME}...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
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
                                Brush.linearGradient(colors = listOf(AppTheme.PrimaryStart, AppTheme.PrimaryEnd))
                            } else {
                                Brush.linearGradient(colors = listOf(Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.3f)))
                            }
                        )
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun AttachmentChip(attachment: Attachment, onRemove: () -> Unit) {
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
            IconButton(onClick = onRemove, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
            }
        }
    }
}

// SETTINGS MODAL
@Composable
fun ModernSettingsModal(viewModel: AgentViewModel) {
    val settings by viewModel.settings.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = { viewModel.closeSettings() },
        modifier = Modifier.widthIn(max = 700.dp),
        title = { Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black) },
        text = {
            Column {
                TabRow(selectedTabIndex = selectedTab, containerColor = Color.Transparent) {
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
                    0 -> GeneralSettings(viewModel, settings)
                    1 -> AISettings(viewModel, settings)
                    2 -> PlanSettings(viewModel, settings)
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
fun GeneralSettings(viewModel: AgentViewModel, settings: AppSettings) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Dark Mode", fontWeight = FontWeight.SemiBold)
                Text("Use dark color scheme", style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = settings.isDarkTheme, onCheckedChange = { viewModel.toggleTheme(it) })
        }
    }
}

@Composable
fun AISettings(viewModel: AgentViewModel, settings: AppSettings) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Custom API Mode", fontWeight = FontWeight.SemiBold)
                Text("Use your own API keys", style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = settings.useCustomApi, onCheckedChange = { viewModel.setUseCustomApi(it) })
        }

        Text("AI Provider", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

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
fun PlanSettings(viewModel: AgentViewModel, settings: AppSettings) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    if (settings.isProUser) "Pro Plan" else "Free Plan",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (settings.isProUser)
                        "Access to all premium features"
                    else
                        "Upgrade to unlock all AI modes",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Button(
            onClick = { viewModel.toggleProPlan(!settings.isProUser) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text(
                if (settings.isProUser) "Downgrade (Demo)" else "Upgrade to Pro",
                fontWeight = FontWeight.Bold
            )
        }

        Text("Pro Features", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        listOf(
            "Unlimited conversations",
            "Unlimited file uploads",
            "All AI modes unlocked",
            "Priority support",
            "Advanced analytics"
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