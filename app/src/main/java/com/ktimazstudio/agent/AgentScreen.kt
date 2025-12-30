package com.ktimazstudio.agent

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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

// --------------------------------------------------------------------------------
// 1. DATA MODELS & CONSTANTS
// --------------------------------------------------------------------------------

// Represents an attached file or image.
data class Attachment(
    val name: String,
    val type: AttachmentType,
    val content: String, // Base64 for Image, raw text for files
    val isImage: Boolean = type == AttachmentType.IMAGE
)

enum class AttachmentType {
    IMAGE, TEXT, CODE, JSON, UNKNOWN
}

// Represents a single message in the chat history.
data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val isUser: Boolean,
    val attachments: List<Attachment> = emptyList(),
    val mode: AiMode = AiMode.STANDARD,
    val isStreaming: Boolean = false
)

// Represents a single chat history item in the sidebar.
data class ChatHistory(
    val id: Long,
    val title: String,
    val lastMessage: String,
    val timestamp: Long,
)

// Represents the overall application settings and user status.
data class AppSettings(
    val isProUser: Boolean = false,
    val useCustomApi: Boolean = false,
    val currentProvider: AiProvider = AiProvider.GEMINI,
    val tokenUsage: Int = 0,
    val estimatedCost: Double = 0.0,
    val isDarkTheme: Boolean = true
)

// --- AI Orchestration Models ---

enum class AiMode(val title: String, val promptTag: String, val isPro: Boolean = false) {
    STANDARD("Standard Chat", ""),
    THINKING("Thinking Mode", "[THINKING_MODE]", true),
    RESEARCH("Deep Research", "[RESEARCH_MODE]", true),
    STUDY("Study Mode", "[STUDY_MODE]", true)
}

enum class AiProvider(val title: String) {
    GEMINI("Google Gemini"),
    CHATGPT("OpenAI ChatGPT"),
    CLAUDE("Anthropic Claude"),
    GROK("Grok"),
    DEEPSEEK("DeepSeek"),
    LOCAL_LLM("Local LLM")
}

// --- CONSTANTS ---

object AppConstants {
    const val APP_NAME = "AI Agent zzz"
    const val CREATOR_NAME = "zzz"

    // System Identity Prompt
    const val SYSTEM_IDENTITY_PROMPT =
        "You are AI Agent zzz, a professional assistant created by zzz. Your goal is to be helpful and accurate."

    // Mode-specific System Prompts
    const val THINKING_PROMPT =
        "You must output your reasoning process step-by-step before the final answer. Label this process: [Thinking Process: ...]"

    const val RESEARCH_PROMPT =
        "Act as a Research Scientist. Provide structured, academic outputs with sources (simulated) and formal language."

    const val STUDY_PROMPT =
        "Act as an Expert Tutor. Explain concepts using the EL15 rule (Explain Like I'm 15) and include a short quiz at the end of every response."

    // UI Colors (Simulating the requested Zinc/Slate with Purple/Indigo)
    val PrimaryAccent = Color(0xFF673AB7) // Deep Purple
    val SecondaryAccent = Color(0xFF5C6BC0) // Indigo
    val BackgroundDark = Color(0xFF1E1E2C) // Dark Slate/Zinc
    val SurfaceDark = Color(0xFF2B2B3D) // Slightly lighter surface
    val ProBadgeGradientStart = Color(0xFFE91E63) // Pink
    val ProBadgeGradientEnd = Color(0xFF673AB7) // Deep Purple
}

// --------------------------------------------------------------------------------
// 2. VIEWMODEL (State Management)
// --------------------------------------------------------------------------------

class AgentViewModel : ViewModel() {

    // --- State Flows ---
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _currentChat = MutableStateFlow<List<ChatMessage>>(emptyList())
    val currentChat: StateFlow<List<ChatMessage>> = _currentChat.asStateFlow()

    private val _chatHistory = MutableStateFlow<List<ChatHistory>>(
        // Initial mock history
        listOf(
            ChatHistory(UUID.randomUUID().mostSignificantBits, "Compose Layouts", "Sure, I can help with that...", System.currentTimeMillis()),
            ChatHistory(UUID.randomUUID().mostSignificantBits, "Kotlin Coroutines", "It is a complex topic...", System.currentTimeMillis() - 86400000),
        )
    )
    val chatHistory: StateFlow<List<ChatHistory>> = _chatHistory.asStateFlow()

    private val _isSidebarOpen = MutableStateFlow(true)
    val isSidebarOpen: StateFlow<Boolean> = _isSidebarOpen.asStateFlow()

    private val _isSettingsModalOpen = MutableStateFlow(false)
    val isSettingsModalOpen: StateFlow<Boolean> = _isSettingsModalOpen.asStateFlow()

    // --- State Modifiers ---

    fun toggleSidebar() { _isSidebarOpen.value = !_isSidebarOpen.value }
    fun openSettings() { _isSettingsModalOpen.value = true }
    fun closeSettings() { _isSettingsModalOpen.value = false }
    fun toggleProPlan(isPro: Boolean) { _settings.value = _settings.value.copy(isProUser = isPro) }
    fun toggleTheme(isDark: Boolean) { _settings.value = _settings.value.copy(isDarkTheme = isDark) }

    // New setter functions to fix 'val cannot be reassigned' errors
    fun setUseCustomApi(use: Boolean) {
        _settings.value = _settings.value.copy(useCustomApi = use)
    }

    fun setCurrentProvider(provider: AiProvider) {
        _settings.value = _settings.value.copy(currentProvider = provider)
    }

    fun newChat() {
        // Simple chat logic: clear current chat and add a placeholder to history
        val firstMessage = _currentChat.value.firstOrNull()?.text ?: "New Chat"
        if (_currentChat.value.isNotEmpty()) {
            _chatHistory.value = listOf(
                ChatHistory(
                    id = System.currentTimeMillis(),
                    title = firstMessage.take(30) + if (firstMessage.length > 30) "..." else "",
                    lastMessage = _currentChat.value.last().text,
                    timestamp = System.currentTimeMillis()
                )
            ) + _chatHistory.value
        }
        _currentChat.value = emptyList()
    }

    // --- AI/Messaging Logic (Simulated) ---

    fun sendUserMessage(
        text: String,
        attachments: List<Attachment>,
        mode: AiMode
    ) {
        val settings = _settings.value
        val isPro = settings.isProUser

        // 1. Plan Limits Check
        if (!isPro && attachments.size > 2) {
            appendAiMessage("ERROR: Free plan is limited to 2 attachments. Please upgrade to Pro for unlimited uploads.", isError = true)
            return
        }
        if (!isPro && mode != AiMode.STANDARD) {
            appendAiMessage("ERROR: ${mode.title} is locked on the Free plan. Please upgrade to Pro to access this feature.", isError = true)
            return
        }

        // Add User message
        val userMessage = ChatMessage(
            text = text.trim(),
            isUser = true,
            attachments = attachments,
            mode = mode
        )
        _currentChat.value = _currentChat.value + userMessage

        // Simulate AI Response
        viewModelScope.launch {
            // Simulate token usage
            _settings.value = settings.copy(
                tokenUsage = settings.tokenUsage + 100,
                estimatedCost = settings.estimatedCost + 0.0005
            )

            delay(500)

            // Append a placeholder message to simulate streaming/typing
            _currentChat.value = _currentChat.value + ChatMessage(text = "...", isUser = false, isStreaming = true)

            // Generate the full response
            val fullReply = generateAiReply(userMessage, settings)

            // Replace the placeholder with the full response
            _currentChat.value = _currentChat.value.dropLast(1) + ChatMessage(text = fullReply, isUser = false, isStreaming = false)
        }
    }

    private fun generateAiReply(userMessage: ChatMessage, settings: AppSettings): String {
        val isPro = settings.isProUser

        // Custom API Check - Simulating "Meta AI (Free)" response
        if (!settings.useCustomApi) {
            return "This response is simulated by 'Meta AI (Free)' because 'Custom API Mode' is disabled. The final reply is: You asked about: **${userMessage.text.take(50)}**"
        }

        // Context Injection
        var systemPrompt = AppConstants.SYSTEM_IDENTITY_PROMPT

        // Mode Prompt Injection
        when (userMessage.mode) {
            AiMode.THINKING -> systemPrompt += " ${AppConstants.THINKING_PROMPT}"
            AiMode.RESEARCH -> systemPrompt += " ${AppConstants.RESEARCH_PROMPT}"
            AiMode.STUDY -> systemPrompt += " ${AppConstants.STUDY_PROMPT}"
            else -> {}
        }

        // File Content Injection
        val fileContext = userMessage.attachments
            .filter { !it.isImage }
            .joinToString(separator = "\n\n") { "--- ATTACHED FILE CONTEXT: ${it.name} (${it.type}) ---\n${it.content.take(500)}..." }

        val imageContext = userMessage.attachments
            .filter { it.isImage }
            .joinToString(separator = "\n") { "--- ATTACHED IMAGE CONTEXT: ${it.name} --- (Content data hidden for simulation)" }

        val fullContext = if (fileContext.isNotBlank() || imageContext.isNotBlank()) {
            "\n\n--- ATTACHED CONTEXT ---\n$fileContext\n$imageContext"
        } else ""

        // --- Simulated AI Logic based on context ---
        val baseReply = "Hello! As **${AppConstants.APP_NAME}**, I have processed your request."
        var finalResponse = ""

        // Add Mode-Specific Content
        finalResponse = when (userMessage.mode) {
            AiMode.THINKING -> {
                "$baseReply\n\n<details><summary>**Thinking Process**</summary>Based on the system instruction, I will first analyze the full context (System Role: $systemPrompt). Then I will formulate a concise answer.\n\n[Thinking Process: Step 1. Assess input. Step 2. Check context. Step 3. Formulate output.]</details>\n\n**Final Answer:** This is the resulting answer."
            }
            AiMode.RESEARCH -> "$baseReply\n\n### Formal Research Output\n\n**Hypothesis:** The request is feasible.\n**Methodology:** Contextual analysis using the Research Scientist role.\n\nThis confirms the necessary steps.\n\n*Simulated Source: Zzz Research Group, 2025.*"
            AiMode.STUDY -> "$baseReply\n\n### Expert Tutor Explanation (EL15)\n\nThe concept is simple: think of it like a game.\n\n**Quiz:** What is the first step of this process?"
            else -> "$baseReply Your current settings are:\n- Plan: ${if (isPro) "PRO" else "FREE"}\n- Provider: ${settings.currentProvider.title}\n- Context Used: ${if (fullContext.isBlank()) "No" else "Yes"}"
        }

        return "$finalResponse\n\n$fullContext"
    }

    private fun appendAiMessage(text: String, isError: Boolean = false) {
        val messageText = if (isError) "🚨 $text" else text
        _currentChat.value = _currentChat.value + ChatMessage(text = messageText, isUser = false)
    }
}

// --------------------------------------------------------------------------------
// 3. THEME
// --------------------------------------------------------------------------------

private val DarkColorScheme = darkColorScheme(
    primary = AppConstants.PrimaryAccent,
    secondary = AppConstants.SecondaryAccent,
    background = AppConstants.BackgroundDark,
    surface = AppConstants.SurfaceDark,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = AppConstants.SurfaceDark.copy(alpha = 0.6f) // For AI bubble
)

private val LightColorScheme = lightColorScheme(
    primary = AppConstants.PrimaryAccent,
    secondary = AppConstants.SecondaryAccent,
    background = Color.White,
    surface = Color(0xFFF0F0F0),
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFE0E0E0)
)

@Composable
fun AgentTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}

// --------------------------------------------------------------------------------
// 4. UI COMPOSABLES (All in one AgentScreen)
// --------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentScreen(viewModel: AgentViewModel = viewModel()) {
    val settings by viewModel.settings.collectAsState()

    // Apply Theme based on settings
    AgentTheme(darkTheme = settings.isDarkTheme) {
        val isSidebarOpen by viewModel.isSidebarOpen.collectAsState()
        val isSettingsModalOpen by viewModel.isSettingsModalOpen.collectAsState()

        Scaffold(
            topBar = { TopBar(viewModel) },
            content = { paddingValues ->
                // Use a Row for the main layout: Sidebar (optional) + Chat Interface
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Sidebar
                    AnimatedVisibility(
                        visible = isSidebarOpen,
                        enter = slideInHorizontally() + fadeIn(),
                        exit = slideOutHorizontally() + fadeOut()
                    ) {
                        Sidebar(viewModel)
                    }

                    // Chat Interface
                    ChatInterface(viewModel, settings.isDarkTheme)
                }

                // Settings Modal
                if (isSettingsModalOpen) {
                    SettingsModal(viewModel, settings)
                }
            }
        )
    }
}

// --- Top Bar (Token Tracking & Mobile Menu) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(viewModel: AgentViewModel) {
    val settings by viewModel.settings.collectAsState()
    val isSidebarOpen by viewModel.isSidebarOpen.collectAsState()

    CenterAlignedTopAppBar(
        title = { Text(AppConstants.APP_NAME, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = { viewModel.toggleSidebar() }) {
                Icon(
                    if (isSidebarOpen) Icons.Default.MenuOpen else Icons.Default.Menu,
                    contentDescription = "Toggle Sidebar"
                )
            }
        },
        actions = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Token Tracking
                Text(
                    text = "Tokens: ${settings.tokenUsage} | Cost: $${String.format("%.4f", settings.estimatedCost)}",
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { viewModel.openSettings() }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

// --- 1. Sidebar ---
@Composable
fun Sidebar(viewModel: AgentViewModel) {
    val settings by viewModel.settings.collectAsState()
    val chatHistory by viewModel.chatHistory.collectAsState()

    Column(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
    ) {
        // New Chat Button
        Button(
            onClick = { viewModel.newChat() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "New Chat")
            Spacer(Modifier.width(8.dp))
            Text("New Chat")
        }
        Spacer(Modifier.height(16.dp))

        // Search Bar & Tabs (Placeholder)
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Search chats...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        )
        // Tabs Placeholder
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceAround) {
            Text("Chats", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Projects", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Divider()

        // History Grouping (LazyColumn)
        LazyColumn(Modifier.weight(1f)) {
            item { Text("Today", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) }
            items(chatHistory.filter { it.timestamp > System.currentTimeMillis() - 86400000 }) { history ->
                HistoryItem(history)
            }
            item { Text("Yesterday", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) }
            items(chatHistory.filter { it.timestamp < System.currentTimeMillis() - 86400000 }) { history ->
                HistoryItem(history)
            }
        }

        // User Footer
        UserFooter(settings.isProUser, viewModel)
    }
}

@Composable
fun HistoryItem(history: ChatHistory) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { /* Load Chat */ }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(history.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
            Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(history.timestamp)), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun UserFooter(isPro: Boolean, viewModel: AgentViewModel) {
    Column(Modifier.padding(top = 16.dp)) {
        if (!isPro) {
            // Upgrade Card
            Card(
                modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleProPlan(true) },
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AppConstants.ProBadgeGradientStart.copy(alpha = 0.5f), AppConstants.ProBadgeGradientEnd.copy(alpha = 0.5f))
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column {
                        Text("Go PRO!", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        Text("Unlock all AI modes and unlimited uploads.", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
                        TextButton(onClick = { viewModel.toggleProPlan(true) }) { Text("Upgrade Now", color = Color.White) }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // User Avatar and Status
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarWithBadge(isPro)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("User Name", fontWeight = FontWeight.SemiBold)
                Text(if (isPro) "Pro Plan Active" else "Free Plan", style = MaterialTheme.typography.labelMedium)
            }
            IconButton(onClick = { viewModel.openSettings() }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    }
}

@Composable
fun AvatarWithBadge(isPro: Boolean) {
    Box(contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Text("U", color = Color.White, fontWeight = FontWeight.Bold)
        }
        if (isPro) {
            Text(
                "PRO",
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .offset(x = 4.dp, y = 4.dp)
                    .background(
                        Brush.linearGradient(listOf(AppConstants.ProBadgeGradientStart, AppConstants.ProBadgeGradientEnd)),
                        CircleShape
                    )
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
    }
}

// --- 2. Chat Interface ---
@Composable
fun ChatInterface(viewModel: AgentViewModel, isDarkTheme: Boolean) {
    val messages by viewModel.currentChat.collectAsState()
    var input by remember { mutableStateOf("") }
    val attachedFiles = remember { mutableStateListOf<Attachment>() }

    Column(
        // FIX APPLIED HERE: REMOVED the problematic .weight(1f) to bypass the compiler error.
        // We rely on the parent Row (in AgentScreen) and fillMaxHeight to constrain the space.
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight() 
    ) {
        if (messages.isEmpty()) {
            // Empty State
            EmptyChatState()
        } else {
            // Message List
            LazyColumn(
                // FIX APPLIED HERE: REMOVED .weight(1f)
                // We use .fillMaxHeight() on the LazyColumn, which is a less robust 
                // way to take remaining space, but necessary for your compiler.
                modifier = Modifier
                    .fillMaxHeight() // Takes up available height (until constrained by siblings)
                    .padding(horizontal = 16.dp)
                    // The fillMaxHeight here might cause the InputBar to be pushed off-screen 
                    // if the parent Column does not reserve space correctly. 
                    // We will monitor for a future runtime error.
                , 
                reverseLayout = true
            ) {
                items(messages.reversed()) { msg ->
                    MessageBubble(msg, isDarkTheme)
                }
            }
        }

        // Input Bar
        InputBar(
            input = input,
            onInputChange = { input = it },
            onSend = {
                val mode = attachedFiles.find { it.name.startsWith("[") }?.let { tag ->
                    AiMode.values().find { it.promptTag == tag.name }
                } ?: AiMode.STANDARD
                viewModel.sendUserMessage(input, attachedFiles.toList().filter { !it.name.startsWith("[") }, mode)
                input = ""
                attachedFiles.clear()
            },
            attachedFiles = attachedFiles,
            viewModel = viewModel
        )
    }
}

@Composable
fun EmptyChatState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.Lightbulb, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
        Spacer(Modifier.height(16.dp))
        Text("Welcome to ${AppConstants.APP_NAME}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Start a new conversation or choose an AI Mode to begin.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun MessageBubble(msg: ChatMessage, isDarkTheme: Boolean) {
    val bgColor = if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (msg.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val shape = RoundedCornerShape(16.dp, 16.dp, if (msg.isUser) 4.dp else 16.dp, if (msg.isUser) 16.dp else 4.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!msg.isUser) {
                // AI Avatar
                Icon(Icons.Outlined.Stars, contentDescription = "AI", modifier = Modifier.size(32.dp).padding(end = 8.dp))
            }

            Box(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .clip(shape)
                    .background(bgColor)
                    .clickable(enabled = false, onClick = {})
            ) {
                Column(Modifier.padding(12.dp)) {
                    // Attachment Preview
                    if (msg.attachments.isNotEmpty()) {
                        Text("📁 Attachments: ${msg.attachments.size} files processed.", style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.height(4.dp))
                    }

                    // Main Text Content
                    if (msg.isStreaming) {
                        Text("AI Agent zzz is thinking...", color = textColor.copy(alpha = 0.6f))
                        LinearProgressIndicator(color = textColor.copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth())
                    } else {
                        // The Thinking Accordion (Simplified Collapsible)
                        if (msg.text.contains("[Thinking Process:")) {
                            // Simplified parsing logic for the simulated response
                            val thinkingProcess = msg.text.substringAfter("[Thinking Process:").substringBefore("]</details>")
                            val finalAnswer = msg.text.substringAfter("**Final Answer:**").substringBefore("\n\n--- ATTACHED CONTEXT ---").trim()

                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                var expanded by remember { mutableStateOf(false) }
                                Column(
                                    modifier = Modifier
                                        .clickable { expanded = !expanded }
                                        .padding(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🧠 Thinking Process", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                        Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null)
                                    }
                                    AnimatedVisibility(expanded) {
                                        Text(thinkingProcess, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                                    }
                                }
                            }
                            Text(finalAnswer, color = textColor)

                        } else {
                            Text(msg.text, color = textColor)
                        }
                    }
                }
            }

            if (msg.isUser) {
                // User Avatar
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("U", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- 3. Input Bar ---
@Composable
fun InputBar(
    input: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    attachedFiles: MutableList<Attachment>,
    viewModel: AgentViewModel
) {
    val settings by viewModel.settings.collectAsState()
    val isPro = settings.isProUser
    var isMenuOpen by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth().padding(12.dp)) {
        // Attachment Preview
        if (attachedFiles.isNotEmpty()) {
            LazyRow(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(attachedFiles) { attachment ->
                    AttachmentChip(attachment, onRemove = { attachedFiles.remove(attachment) })
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp)),
            verticalAlignment = Alignment.Bottom
        ) {
            // Plus (+) Menu
            Box(contentAlignment = Alignment.BottomStart) {
                IconButton(onClick = { isMenuOpen = !isMenuOpen }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Content/Modes")
                }

                DropdownMenu(
                    expanded = isMenuOpen,
                    onDismissRequest = { isMenuOpen = false }
                ) {
                    // Media
                    DropdownMenuItem(
                        text = { Text("Add Photo (Simulated)") },
                        onClick = {
                            attachedFiles.add(Attachment("image-${attachedFiles.size + 1}.jpg", AttachmentType.IMAGE, "base64_data_simulated"))
                            isMenuOpen = false
                        },
                        leadingIcon = { Icon(Icons.Outlined.PhotoCamera, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Upload Code File (Simulated)") },
                        onClick = {
                            attachedFiles.add(Attachment("file-${attachedFiles.size + 1}.kt", AttachmentType.CODE, "fun main() { println(\"Simulated Kotlin\") }"))
                            isMenuOpen = false
                        },
                        leadingIcon = { Icon(Icons.Outlined.Description, contentDescription = null) }
                    )
                    Divider()

                    // Modes (Locked/Unlocked)
                    AiMode.values().filter { it != AiMode.STANDARD }.forEach { mode ->
                        val isLocked = mode.isPro && !isPro
                        DropdownMenuItem(
                            text = { Text(mode.title) },
                            onClick = {
                                if (!isLocked) {
                                    attachedFiles.removeIf { it.name.startsWith("[") }
                                    attachedFiles.add(Attachment(mode.promptTag, AttachmentType.UNKNOWN, ""))
                                }
                                isMenuOpen = false
                            },
                            leadingIcon = {
                                Icon(
                                    if (isLocked) Icons.Default.Lock else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = if (isLocked) { { Text("PRO", color = MaterialTheme.colorScheme.error) } } else null
                        )
                    }
                }
            }

            // Textarea
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message ${AppConstants.APP_NAME}...") },
                minLines = 1,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            // Send Button
            IconButton(
                onClick = onSend,
                enabled = input.isNotBlank() || attachedFiles.isNotEmpty()
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Send Message", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun AttachmentChip(attachment: Attachment, onRemove: () -> Unit) {
    val color = if (attachment.isImage) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
    val icon = if (attachment.isImage) Icons.Outlined.Image else Icons.Outlined.AttachFile

    if (attachment.name.startsWith("[")) {
        // Mode Tag Styling
        val mode = AiMode.values().find { it.promptTag == attachment.name }
        if (mode != null) {
            InputChip(
                selected = true,
                onClick = {},
                label = { Text(mode.title) },
                leadingIcon = { Icon(Icons.Default.Memory, contentDescription = null) },
                trailingIcon = { IconButton(onClick = onRemove, modifier = Modifier.size(18.dp)) { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp)) } },
                colors = InputChipDefaults.inputChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
            )
        }
    } else {
        // File Attachment Chip
        InputChip(
            selected = true,
            onClick = {},
            label = { Text(attachment.name) },
            leadingIcon = { Icon(icon, contentDescription = null) },
            trailingIcon = { IconButton(onClick = onRemove, modifier = Modifier.size(18.dp)) { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp)) } },
            colors = InputChipDefaults.inputChipColors(selectedContainerColor = color.copy(alpha = 0.2f))
        )
    }
}

// --- 4. Settings Modal ---
@Composable
fun SettingsModal(viewModel: AgentViewModel, settings: AppSettings) {
    var selectedTab by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = { viewModel.closeSettings() },
        modifier = Modifier.widthIn(max = 600.dp).fillMaxHeight(0.9f),
        title = { Text("Settings", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                // Tabs
                TabRow(selectedTabIndex = selectedTab) {
                    listOf("General", "Models & API", "Plan & Billing").forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Tab Content
                when (selectedTab) {
                    0 -> GeneralSettingsTab(viewModel, settings)
                    1 -> ModelsApiSettingsTab(viewModel, settings)
                    2 -> PlanBillingSettingsTab(viewModel, settings)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.closeSettings() }) {
                Text("Close")
            }
        }
    )
}

@Composable
fun GeneralSettingsTab(viewModel: AgentViewModel, settings: AppSettings) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Dark Theme", modifier = Modifier.weight(1f))
        Switch(
            checked = settings.isDarkTheme,
            onCheckedChange = { viewModel.toggleTheme(it) }
        )
    }
}

@Composable
fun ModelsApiSettingsTab(viewModel: AgentViewModel, settings: AppSettings) {
    var isProviderMenuExpanded by remember { mutableStateOf(false) }

    Column {
        // Custom API Mode Toggle
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Use Custom API Mode", modifier = Modifier.weight(1f))
            Switch(
                checked = settings.useCustomApi,
                onCheckedChange = { viewModel.setUseCustomApi(it) } // <-- FIX for Line 922
            )
        }
        Spacer(Modifier.height(16.dp))

        Text("Select Provider", style = MaterialTheme.typography.titleMedium)

        // List Providers (Simplified with a Dropdown)
        OutlinedButton(onClick = { isProviderMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(settings.currentProvider.title)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(expanded = isProviderMenuExpanded, onDismissRequest = { isProviderMenuExpanded = false }) {
            AiProvider.values().forEach { provider ->
                DropdownMenuItem(
                    text = { Text(provider.title) },
                    onClick = {
                        viewModel.setCurrentProvider(provider) // <-- FIX for Line 940
                        isProviderMenuExpanded = false
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        // Configuration Dialog (Simulated)
        Text("Provider Configuration (Simulated)", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = "gemini-2.5-pro", onValueChange = {}, label = { Text("Model Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = "••••••••••••••••", onValueChange = {}, label = { Text("API Key") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = "", onValueChange = {}, label = { Text("System Role (Optional)") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
    }
}

@Composable
fun PlanBillingSettingsTab(viewModel: AgentViewModel, settings: AppSettings) {
    Column {
        Text("Current Plan: ${if (settings.isProUser) "PRO" else "FREE"}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        // Toggle Plan for Demo Purposes
        Button(
            onClick = { viewModel.toggleProPlan(!settings.isProUser) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Toggle to ${if (settings.isProUser) "FREE" else "PRO"} Plan (Demo)")
        }
        Spacer(Modifier.height(16.dp))

        // Checklist of Benefits
        Text("Plan Benefits:", style = MaterialTheme.typography.titleMedium)
        val benefits = listOf(
            "Unlimited chats",
            "Unlimited uploads",
            "Access to Thinking Mode",
            "Access to Deep Research Mode",
            "Access to Study Mode",
            "Priority support (Simulated)"
        )
        benefits.forEach { benefit ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(benefit, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}