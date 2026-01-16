package com.ktimazstudio.agent.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ktimazstudio.agent.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AgentViewModel(context: Context) : ViewModel() {
    
    private val repository = AgentRepository.getInstance(context)
    private val aiProviderHandler = AiProviderHandler()
    
    // Settings State
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

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Computed Properties
    val currentSession: ChatSession?
        get() = _chatSessions.value.find { it.id == _currentSessionId.value }

    val activeApiCount: Int
        get() = _settings.value.apiConfigs.count { it.isActive }

    init {
        loadPersistedData()
    }

    private fun loadPersistedData() {
        viewModelScope.launch {
            try {
                // Load app settings
                appContainer.repository.getAppSettings().collect { settings ->
                    if (settings != null) {
                        _settings.value = settings.copy(
                            apiConfigs = _settings.value.apiConfigs
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        viewModelScope.launch {
            try {
                // Load API configs
                appContainer.repository.getAllApiConfigs().collect { configs ->
                    _settings.value = _settings.value.copy(apiConfigs = configs)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        viewModelScope.launch {
            try {
                // Load chat sessions
                appContainer.repository.getAllChatSessions().collect { sessions ->
                    _chatSessions.value = sessions
                    if (_currentSessionId.value.isEmpty() && sessions.isNotEmpty()) {
                        _currentSessionId.value = sessions.first().id
                    } else if (sessions.isEmpty()) {
                        newChat()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                newChat()
            }
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
        viewModelScope.launch {
            try {
                _settings.value = _settings.value.copy(isProUser = isPro)
                appContainer.repository.saveAppSettings(_settings.value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            try {
                _settings.value = _settings.value.copy(isDarkTheme = isDark)
                appContainer.repository.saveAppSettings(_settings.value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setSelectedMode(mode: AiMode) {
        _selectedMode.value = mode
    }

    // API Management
    fun addApiConfig(config: ApiConfig): Boolean {
        return try {
            val currentSettings = _settings.value

            if (!currentSettings.isProUser && currentSettings.apiConfigs.size >= AppTheme.FREE_API_LIMIT) {
                return false
            }

            if (config.apiKey.isBlank()) {
                return false
            }

            viewModelScope.launch {
                try {
                    appContainer.repository.insertApiConfig(config.copy(isActive = false))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun updateApiConfig(configId: String, updatedConfig: ApiConfig) {
        viewModelScope.launch {
            try {
                appContainer.repository.updateApiConfig(updatedConfig.copy(id = configId))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteApiConfig(configId: String) {
        viewModelScope.launch {
            try {
                appContainer.repository.deleteApiConfig(configId)

                // Remove from all chat sessions
                val updatedSessions = _chatSessions.value.map { session ->
                    session.copy(
                        activeApis = session.activeApis.filter { it != configId }.toMutableList()
                    )
                }
                _chatSessions.value = updatedSessions
                updatedSessions.forEach { appContainer.repository.updateChatSession(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleApiActive(configId: String) {
        viewModelScope.launch {
            try {
                val currentSettings = _settings.value
                val config = currentSettings.apiConfigs.find { it.id == configId } ?: return@launch
                val currentlyActive = currentSettings.apiConfigs.count { it.isActive }

                if (!config.isActive && currentlyActive >= AppTheme.MAX_ACTIVE_APIS_PER_CHAT) {
                    return@launch
                }

                val updatedConfig = config.copy(isActive = !config.isActive)
                appContainer.repository.updateApiConfig(updatedConfig)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleApiForCurrentChat(configId: String) {
        viewModelScope.launch {
            try {
                val session = currentSession ?: return@launch
                val mutableApis = session.activeApis.toMutableList()

                if (mutableApis.contains(configId)) {
                    mutableApis.remove(configId)
                } else {
                    if (mutableApis.size >= AppTheme.MAX_ACTIVE_APIS_PER_CHAT) {
                        mutableApis.removeAt(0)
                    }
                    mutableApis.add(configId)
                }

                val updatedSession = session.copy(activeApis = mutableApis)
                appContainer.repository.updateChatSession(updatedSession)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Chat Management
    fun newChat() {
        viewModelScope.launch {
            try {
                val newSession = ChatSession(title = "New Chat")
                appContainer.repository.insertChatSession(newSession)
                _currentSessionId.value = newSession.id
                _selectedMode.value = AiMode.STANDARD
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun openChat(sessionId: String) {
        _currentSessionId.value = sessionId
    }

    fun renameChat(sessionId: String, newTitle: String) {
        viewModelScope.launch {
            try {
                val session = _chatSessions.value.find { it.id == sessionId } ?: return@launch
                val updatedSession = session.copy(title = newTitle)
                appContainer.repository.updateChatSession(updatedSession)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteChat(sessionId: String) {
        viewModelScope.launch {
            try {
                appContainer.repository.deleteChatSession(sessionId)

                if (_currentSessionId.value == sessionId) {
                    val remaining = _chatSessions.value.filter { it.id != sessionId }
                    _currentSessionId.value = remaining.firstOrNull()?.id ?: ""
                    if (remaining.isEmpty()) {
                        newChat()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun pinChat(sessionId: String) {
        viewModelScope.launch {
            try {
                val session = _chatSessions.value.find { it.id == sessionId } ?: return@launch
                val updatedSession = session.copy(isPinned = !session.isPinned)
                appContainer.repository.updateChatSession(updatedSession)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // REAL AI MESSAGE SENDING
    fun sendUserMessage(text: String, attachments: List<Attachment>, mode: AiMode) {
        viewModelScope.launch {
            try {
                val currentSettings = _settings.value
                val session = currentSession

                if (session == null) {
                    newChat()
                    return@launch
                }

                if (!currentSettings.isProUser && attachments.size > 10) {
                    appendAiMessage("Free plan limited to 10 attachments per message")
                    return@launch
                }

                if (!currentSettings.isProUser && mode.isPro) {
                    appendAiMessage("${mode.title} Mode requires Pro account")
                    return@launch
                }

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
                appContainer.repository.insertMessage(session.id, userMessage)

                // Auto-generate title
                if (session.messages.size == 1 && session.title.startsWith("New Chat")) {
                    val newTitle = text.take(40) + if (text.length > 40) "..." else ""
                    renameChat(session.id, newTitle)
                }

                _chatSessions.value = _chatSessions.value.toList()

                // Update token usage
                _settings.value = currentSettings.copy(
                    tokenUsage = currentSettings.tokenUsage + 150,
                    estimatedCost = currentSettings.estimatedCost + 0.00075
                )
                appContainer.repository.saveAppSettings(_settings.value)

                // Show loading
                _isLoading.value = true

                // REAL API CALL
                val responses = mutableListOf<String>()
                for (api in activeApis) {
                    val result = appContainer.aiProviderHandler.sendMessage(
                        apiConfig = api,
                        userMessage = buildPrompt(text, mode),
                        systemRole = api.systemRole
                    )

                    result.fold(
                        onSuccess = { response ->
                            responses.add("**${api.name}**: $response")
                        },
                        onFailure = { error ->
                            responses.add("**${api.name}** (Error): ${error.message}")
                        }
                    )
                }

                _isLoading.value = false

                // Add AI reply
                val aiMessage = ChatMessage(
                    text = responses.joinToString("\n\n---\n\n"),
                    isUser = false,
                    mode = mode,
                    usedApis = activeApis.map { it.name }
                )

                session.messages.add(aiMessage)
                appContainer.repository.insertMessage(session.id, aiMessage)
                _chatSessions.value = _chatSessions.value.toList()

            } catch (e: Exception) {
                e.printStackTrace()
                _isLoading.value = false
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

    private fun buildPrompt(text: String, mode: AiMode): String {
        return when (mode) {
            AiMode.THINKING -> "[THINKING MODE] Analyze deeply and provide step-by-step reasoning: $text"
            AiMode.RESEARCH -> "[RESEARCH MODE] Provide comprehensive research with citations: $text"
            AiMode.STUDY -> "[STUDY MODE] Explain clearly with examples for learning: $text"
            AiMode.CODE -> "[CODE MODE] Provide code solutions with explanations: $text"
            AiMode.CREATIVE -> "[CREATIVE MODE] Generate creative and imaginative content: $text"
            else -> text
        }
    }

    private fun appendAiMessage(text: String) {
        try {
            currentSession?.let { session ->
                val message = ChatMessage(text = text, isUser = false)
                session.messages.add(message)
                viewModelScope.launch {
                    appContainer.repository.insertMessage(session.id, message)
                }
                _chatSessions.value = _chatSessions.value.toList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}