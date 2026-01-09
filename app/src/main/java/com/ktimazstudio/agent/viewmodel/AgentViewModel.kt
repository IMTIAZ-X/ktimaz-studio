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
    // Settings State
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    // Chat Sessions State
    private val _chatSessions = MutableStateFlow<List<ChatSession>>(
        listOf(ChatSession(title = "New Chat"))
    )
    val chatSessions: StateFlow<List<ChatSession>> = _chatSessions.asStateFlow()

    // Current Session ID
    private val _currentSessionId = MutableStateFlow(_chatSessions.value.firstOrNull()?.id ?: "")
    val currentSessionId: StateFlow<String> = _currentSessionId.asStateFlow()

    // UI State
    private val _isSidebarOpen = MutableStateFlow(true)
    val isSidebarOpen: StateFlow<Boolean> = _isSidebarOpen.asStateFlow()

    private val _isSettingsModalOpen = MutableStateFlow(false)
    val isSettingsModalOpen: StateFlow<Boolean> = _isSettingsModalOpen.asStateFlow()

    // Selected Mode
    private val _selectedMode = MutableStateFlow(AiMode.STANDARD)
    val selectedMode: StateFlow<AiMode> = _selectedMode.asStateFlow()

    // Editing State
    private val _editingChatId = MutableStateFlow<String?>(null)
    val editingChatId: StateFlow<String?> = _editingChatId.asStateFlow()

    val currentSession: ChatSession?
        get() = _chatSessions.value.find { it.id == _currentSessionId.value }

    val activeApiCount: Int
        get() = _settings.value.apiConfigs.count { it.isActive }

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        // Initialize with default APIs if empty
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
    }

    // Save all data
    fun saveAllData() {
        PreferenceManager.saveBasicSettings(_settings.value)
        PreferenceManager.saveApiConfigs(_settings.value.apiConfigs)
        PreferenceManager.saveChatSessions(_chatSessions.value)
    }

    // Load all data
    fun loadAllData() {
        val loadedSettings = PreferenceManager.loadBasicSettings().copy(
            apiConfigs = PreferenceManager.loadApiConfigs()
        )
        _settings.value = loadedSettings
        
        val loadedSessions = PreferenceManager.loadChatSessions()
        if (loadedSessions.isNotEmpty()) {
            _chatSessions.value = loadedSessions
            _currentSessionId.value = loadedSessions.first().id
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
        saveAllData()
    }

    fun toggleProPlan(isPro: Boolean) {
        _settings.value = _settings.value.copy(isProUser = isPro)
        saveAllData()
    }

    fun toggleTheme(isDark: Boolean) {
        _settings.value = _settings.value.copy(isDarkTheme = isDark)
        saveAllData()
    }

    fun setSelectedMode(mode: AiMode) {
        _selectedMode.value = mode
    }

    // API Management
    fun addApiConfig(config: ApiConfig): Boolean {
        val currentSettings = _settings.value
        
        // Check limits
        if (!currentSettings.isProUser && currentSettings.apiConfigs.size >= AppTheme.FREE_API_LIMIT) {
            return false
        }

        // Validate API key
        if (config.apiKey.isBlank()) {
            return false
        }

        try {
            // Add new API config
            val newApiConfigs = currentSettings.apiConfigs + config.copy(isActive = false)
            _settings.value = currentSettings.copy(apiConfigs = newApiConfigs)
            saveAllData()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun updateApiConfig(configId: String, updatedConfig: ApiConfig) {
        try {
            _settings.value = _settings.value.copy(
                apiConfigs = _settings.value.apiConfigs.map { api ->
                    if (api.id == configId) updatedConfig.copy(id = configId) else api
                }
            )
            saveAllData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteApiConfig(configId: String) {
        try {
            // Remove from settings
            _settings.value = _settings.value.copy(
                apiConfigs = _settings.value.apiConfigs.filter { it.id != configId }
            )
            
            // Remove from all chat sessions
            _chatSessions.value = _chatSessions.value.map { session ->
                session.copy(
                    activeApis = session.activeApis.filter { it != configId }.toMutableList()
                )
            }
            saveAllData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleApiActive(configId: String) {
        try {
            val config = _settings.value.apiConfigs.find { it.id == configId } ?: return
            val currentlyActive = _settings.value.apiConfigs.count { it.isActive }
            
            if (!config.isActive && currentlyActive >= AppTheme.MAX_ACTIVE_APIS_PER_CHAT) {
                return
            }
            
            _settings.value = _settings.value.copy(
                apiConfigs = _settings.value.apiConfigs.map { api ->
                    if (api.id == configId) api.copy(isActive = !api.isActive) else api
                }
            )
            saveAllData()
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
            
            // Update session
            _chatSessions.value = _chatSessions.value.map { s ->
                if (s.id == session.id) s.copy(activeApis = mutableApis) else s
            }
            saveAllData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Chat Management
    fun newChat() {
        try {
            val newSession = ChatSession(title = "New Chat ${System.currentTimeMillis()}")
            _chatSessions.value = listOf(newSession) + _chatSessions.value
            _currentSessionId.value = newSession.id
            _selectedMode.value = AiMode.STANDARD
            saveAllData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openChat(sessionId: String) {
        _currentSessionId.value = sessionId
    }

    fun startEditingChat(chatId: String) {
        _editingChatId.value = chatId
    }

    fun renameChat(sessionId: String, newTitle: String) {
        try {
            _chatSessions.value = _chatSessions.value.map { chat ->
                if (chat.id == sessionId) chat.copy(title = newTitle) else chat
            }
            _editingChatId.value = null
            saveAllData()
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
            saveAllData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pinChat(sessionId: String) {
        try {
            _chatSessions.value = _chatSessions.value.map { chat ->
                if (chat.id == sessionId) chat.copy(isPinned = !chat.isPinned) else chat
            }
            saveAllData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Message Sending
    fun sendUserMessage(text: String, attachments: List<Attachment>, mode: AiMode) {
        try {
            val settings = _settings.value
            val currentSession = this.currentSession ?: return

            // Validation
            if (!settings.isProUser && attachments.size > 10) {
                appendAiMessage("Free plan limited to 10 attachments per message")
                return
            }

            if (!settings.isProUser && mode.isPro) {
                appendAiMessage("${mode.title} Mode requires Pro account")
                return
            }

            val activeApis = settings.apiConfigs.filter { api ->
                api.isActive && currentSession.activeApis.contains(api.id)
            }

            if (activeApis.isEmpty()) {
                appendAiMessage("No active APIs. Configure in Settings → API Management")
                return
            }

            // Add user message
            val userMessage = ChatMessage(
                text = text.trim(),
                isUser = true,
                attachments = attachments,
                mode = mode
            )
            
            currentSession.messages.add(userMessage)
            
            // Update title if first message
            if (currentSession.messages.size == 1 && currentSession.title.startsWith("New Chat")) {
                currentSession.title = text.take(40) + if (text.length > 40) "..." else ""
            }
            
            // Update state
            _chatSessions.value = _chatSessions.value.toList()
            saveAllData()

            // Simulate AI response
            viewModelScope.launch {
                try {
                    _settings.value = settings.copy(
                        tokenUsage = settings.tokenUsage + 150,
                        estimatedCost = settings.estimatedCost + 0.00075
                    )
                    saveAllData()

                    delay(800)
                    
                    // Add loading message
                    currentSession.messages.add(
                        ChatMessage(text = "...", isUser = false, isStreaming = true)
                    )
                    _chatSessions.value = _chatSessions.value.toList()

                    delay(1500)
                    
                    // Remove loading and add response
                    if (currentSession.messages.isNotEmpty()) {
                        currentSession.messages.removeLast()
                    }
                    
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
                    saveAllData()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun generateAiReply(userMessage: ChatMessage, activeApis: List<ApiConfig>): String {
        return when (userMessage.mode) {
            AiMode.THINKING -> """
                **Thinking Mode**
                
                Analyzing: "${userMessage.text.take(50)}"
                
                Using ${activeApis.size} AI model(s):
                ${activeApis.joinToString("\n") { "• ${it.name}" }}
                
                Response generated successfully.
            """.trimIndent()
            
            AiMode.RESEARCH -> """
                **Research Mode**
                
                Topic: "${userMessage.text.take(50)}"
                
                Analysis by:
                ${activeApis.joinToString("\n") { "• ${it.name} (${it.modelName})" }}
                
                Key findings documented.
            """.trimIndent()
            
            else -> """
                Hello! I'm your AI Agent Assistant.
                
                Processing with ${activeApis.size} active API(s):
                ${activeApis.mapIndexed { idx, api -> "${idx + 1}. ${api.name}" }.joinToString("\n")}
                
                Ready to help with: ${userMessage.text.take(60)}
            """.trimIndent()
        }
    }

    private fun appendAiMessage(text: String) {
        try {
            currentSession?.messages?.add(ChatMessage(text = text, isUser = false))
            _chatSessions.value = _chatSessions.value.toList()
            saveAllData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}