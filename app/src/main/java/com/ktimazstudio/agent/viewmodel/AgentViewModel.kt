package com.ktimazstudio.agent.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ktimazstudio.agent.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AgentViewModel : ViewModel() {
    // Settings State with immutable updates
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    // Chat Sessions State
    private val _chatSessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val chatSessions: StateFlow<List<ChatSession>> = _chatSessions.asStateFlow()

    // Current Session ID
    private val _currentSessionId = MutableStateFlow("")
    val currentSessionId: StateFlow<String> = _currentSessionId.asStateFlow()

    // UI State
    private val _isSidebarOpen = MutableStateFlow(true)
    val isSidebarOpen: StateFlow<Boolean> = _isSidebarOpen.asStateFlow()

    private val _isSettingsModalOpen = MutableStateFlow(false)
    val isSettingsModalOpen: StateFlow<Boolean> = _isSettingsModalOpen.asStateFlow()

    // Selected Mode
    private val _selectedMode = MutableStateFlow(AiMode.STANDARD)
    val selectedMode: StateFlow<AiMode> = _selectedMode.asStateFlow()

    // Computed Properties
    val currentSession: ChatSession?
        get() = _chatSessions.value.find { it.id == _currentSessionId.value }

    val activeApiCount: Int
        get() = _settings.value.apiConfigs.count { it.isActive }

    init {
        initializeApp()
    }

    private fun initializeApp() {
        try {
            // Create initial chat session
            val initialSession = ChatSession(title = "New Chat")
            _chatSessions.value = listOf(initialSession)
            _currentSessionId.value = initialSession.id

            // Initialize with default API if none exist
            if (_settings.value.apiConfigs.isEmpty()) {
                _settings.value = _settings.value.copy(
                    apiConfigs = listOf(
                        ApiConfig(
                            provider = AiProvider.GEMINI,
                            name = "Gemini Default",
                            apiKey = "",
                            modelName = AiProvider.GEMINI.defaultModel,
                            baseUrl = AiProvider.GEMINI.defaultUrl,
                            isActive = false
                        )
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // UI Actions
    fun toggleSidebar() {
        _isSidebarOpen.value = !_isSidebarOpen.value
    }

    fun openSettings() {
        _isSettingsModalOpen.value = true
    }

    fun closeSettings() {
        _isSettingsModalOpen.value = false
    }

    fun toggleProPlan(isPro: Boolean) {
        try {
            _settings.value = _settings.value.copy(isProUser = isPro)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleTheme(isDark: Boolean) {
        try {
            _settings.value = _settings.value.copy(isDarkTheme = isDark)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setSelectedMode(mode: AiMode) {
        _selectedMode.value = mode
    }

    // API Management - CRITICAL FIX: Proper immutable state updates
    fun addApiConfig(config: ApiConfig): Boolean {
        return try {
            val currentSettings = _settings.value

            // Validate pro user limits
            if (!currentSettings.isProUser && currentSettings.apiConfigs.size >= AppTheme.FREE_API_LIMIT) {
                return false
            }

            // Validate API key
            if (config.apiKey.isBlank()) {
                return false
            }

            // CRITICAL: Create new immutable list
            val updatedConfigs = currentSettings.apiConfigs.toMutableList()
            updatedConfigs.add(config.copy(isActive = false))

            // CRITICAL: Update state immutably
            _settings.value = currentSettings.copy(apiConfigs = updatedConfigs.toList())
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun updateApiConfig(configId: String, updatedConfig: ApiConfig) {
        try {
            val currentSettings = _settings.value
            val updatedConfigs = currentSettings.apiConfigs.map { api ->
                if (api.id == configId) updatedConfig.copy(id = configId) else api
            }
            _settings.value = currentSettings.copy(apiConfigs = updatedConfigs)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteApiConfig(configId: String) {
        try {
            // Remove from settings
            val currentSettings = _settings.value
            val updatedConfigs = currentSettings.apiConfigs.filter { it.id != configId }
            _settings.value = currentSettings.copy(apiConfigs = updatedConfigs)

            // Remove from all chat sessions
            val updatedSessions = _chatSessions.value.map { session ->
                session.copy(
                    activeApis = session.activeApis.filter { it != configId }.toMutableList()
                )
            }
            _chatSessions.value = updatedSessions
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleApiActive(configId: String) {
        try {
            val currentSettings = _settings.value
            val config = currentSettings.apiConfigs.find { it.id == configId } ?: return
            val currentlyActive = currentSettings.apiConfigs.count { it.isActive }

            // Prevent activating more than max allowed
            if (!config.isActive && currentlyActive >= AppTheme.MAX_ACTIVE_APIS_PER_CHAT) {
                return
            }

            val updatedConfigs = currentSettings.apiConfigs.map { api ->
                if (api.id == configId) api.copy(isActive = !api.isActive) else api
            }
            _settings.value = currentSettings.copy(apiConfigs = updatedConfigs)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleApiForCurrentChat(configId: String) {
        try {
            val session = currentSession ?: return
            val mutableApis = session.activeApis.toMutableList()

            if (mutableApis.contains(configId)) {
                mutableApis.remove(configId)
            } else {
                if (mutableApis.size >= AppTheme.MAX_ACTIVE_APIS_PER_CHAT) {
                    mutableApis.removeAt(0)
                }
                mutableApis.add(configId)
            }

            val updatedSessions = _chatSessions.value.map { s ->
                if (s.id == session.id) s.copy(activeApis = mutableApis) else s
            }
            _chatSessions.value = updatedSessions
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Chat Management
    fun newChat() {
        try {
            val newSession = ChatSession(title = "New Chat")
            _chatSessions.value = listOf(newSession) + _chatSessions.value
            _currentSessionId.value = newSession.id
            _selectedMode.value = AiMode.STANDARD
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openChat(sessionId: String) {
        _currentSessionId.value = sessionId
    }

    fun renameChat(sessionId: String, newTitle: String) {
        try {
            val updatedSessions = _chatSessions.value.map { chat ->
                if (chat.id == sessionId) chat.copy(title = newTitle) else chat
            }
            _chatSessions.value = updatedSessions
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteChat(sessionId: String) {
        try {
            _chatSessions.value = _chatSessions.value.filter { it.id != sessionId }

            if (_currentSessionId.value == sessionId) {
                _currentSessionId.value = _chatSessions.value.firstOrNull()?.id ?: ""
                if (_chatSessions.value.isEmpty()) {
                    newChat()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pinChat(sessionId: String) {
        try {
            val updatedSessions = _chatSessions.value.map { chat ->
                if (chat.id == sessionId) chat.copy(isPinned = !chat.isPinned) else chat
            }
            _chatSessions.value = updatedSessions
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Message Sending - CRASH FIX: Proper null checks and state updates
    fun sendUserMessage(text: String, attachments: List<Attachment>, mode: AiMode) {
        viewModelScope.launch {
            try {
                val currentSettings = _settings.value
                val session = currentSession

                // CRITICAL: Null check to prevent crash
                if (session == null) {
                    newChat()
                    return@launch
                }

                // Validate attachments
                if (!currentSettings.isProUser && attachments.size > 10) {
                    appendAiMessage("Free plan limited to 10 attachments per message")
                    return@launch
                }

                // Validate mode access
                if (!currentSettings.isProUser && mode.isPro) {
                    appendAiMessage("${mode.title} Mode requires Pro account")
                    return@launch
                }

                // Get active APIs - CRITICAL: Filter only valid, active APIs
                val activeApis = currentSettings.apiConfigs.filter { api ->
                    api.isActive && 
                    session.activeApis.contains(api.id) && 
                    api.apiKey.isNotBlank()
                }

                if (activeApis.isEmpty()) {
                    appendAiMessage("No active APIs configured. Please add and activate APIs in Settings.")
                    return@launch
                }

                // Add user message
                val userMessage = ChatMessage(
                    text = text.trim(),
                    isUser = true,
                    attachments = attachments,
                    mode = mode
                )

                session.messages.add(userMessage)

                // Auto-generate title from first message
                if (session.messages.size == 1 && session.title.startsWith("New Chat")) {
                    session.title = text.take(40) + if (text.length > 40) "..." else ""
                }

                // CRITICAL: Trigger state update
                _chatSessions.value = _chatSessions.value.toList()

                // Update token usage
                _settings.value = currentSettings.copy(
                    tokenUsage = currentSettings.tokenUsage + 150,
                    estimatedCost = currentSettings.estimatedCost + 0.00075
                )

                // Simulate AI response
                delay(800)

                // Add loading indicator
                session.messages.add(
                    ChatMessage(text = "Thinking...", isUser = false, isStreaming = true)
                )
                _chatSessions.value = _chatSessions.value.toList()

                delay(1500)

                // Remove loading
                if (session.messages.isNotEmpty() && session.messages.last().isStreaming) {
                    session.messages.removeLast()
                }

                // Generate and add AI reply
                val reply = generateAiReply(userMessage, activeApis)
                session.messages.add(
                    ChatMessage(
                        text = reply,
                        isUser = false,
                        mode = mode,
                        usedApis = activeApis.map { it.name }
                    )
                )

                // CRITICAL: Final state update
                _chatSessions.value = _chatSessions.value.toList()

            } catch (e: Exception) {
                e.printStackTrace()
                // Add error message to chat
                currentSession?.messages?.add(
                    ChatMessage(
                        text = "Error: ${e.message ?: "Unknown error occurred"}",
                        isUser = false
                    )
                )
                _chatSessions.value = _chatSessions.value.toList()
            }
        }
    }

    private fun generateAiReply(userMessage: ChatMessage, activeApis: List<ApiConfig>): String {
        return when (userMessage.mode) {
            AiMode.THINKING -> """
                🧠 **Deep Thinking Mode**
                
                Analyzing your query: "${userMessage.text.take(50)}${if (userMessage.text.length > 50) "..." else ""}"
                
                Using ${activeApis.size} AI model(s):
                ${activeApis.joinToString("\n") { "• ${it.name} (${it.provider.title})" }}
                
                ✓ Analysis complete
            """.trimIndent()

            AiMode.RESEARCH -> """
                🔬 **Research Mode**
                
                Topic: "${userMessage.text.take(50)}${if (userMessage.text.length > 50) "..." else ""}"
                
                Research powered by:
                ${activeApis.joinToString("\n") { "• ${it.name} - ${it.modelName}" }}
                
                ✓ Research findings ready
            """.trimIndent()

            else -> """
                Hello! I'm your AI Agent Assistant.
                
                📊 Active APIs: ${activeApis.size}
                ${activeApis.mapIndexed { idx, api -> "${idx + 1}. ${api.name} (${api.provider.title})" }.joinToString("\n")}
                
                I'm ready to help with: ${userMessage.text.take(60)}${if (userMessage.text.length > 60) "..." else ""}
            """.trimIndent()
        }
    }

    private fun appendAiMessage(text: String) {
        try {
            currentSession?.messages?.add(ChatMessage(text = text, isUser = false))
            _chatSessions.value = _chatSessions.value.toList()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
