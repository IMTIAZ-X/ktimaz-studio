package com.ktimazstudio.agent.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ktimazstudio.agent.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AgentViewModel(context: Context) : ViewModel() {
    
    private val dataManager = SimpleDataManager.getInstance(context)
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
        try {
            // Load settings
            val loadedSettings = dataManager.loadSettings()
            val loadedConfigs = dataManager.loadApiConfigs()
            _settings.value = loadedSettings.copy(apiConfigs = loadedConfigs)
            
            // Load chat sessions
            val loadedSessions = dataManager.loadChatSessions()
            _chatSessions.value = if (loadedSessions.isEmpty()) {
                val newSession = ChatSession(title = "New Chat")
                listOf(newSession)
            } else {
                loadedSessions
            }
            
            _currentSessionId.value = _chatSessions.value.firstOrNull()?.id ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            newChat()
        }
    }

    // Save data whenever it changes
    private fun saveData() {
        try {
            dataManager.saveSettings(_settings.value)
            dataManager.saveApiConfigs(_settings.value.apiConfigs)
            dataManager.saveChatSessions(_chatSessions.value)
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
        _settings.value = _settings.value.copy(isProUser = isPro)
        saveData()
    }

    fun toggleTheme(isDark: Boolean) {
        _settings.value = _settings.value.copy(isDarkTheme = isDark)
        saveData()
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

            val updatedConfigs = currentSettings.apiConfigs.toMutableList()
            updatedConfigs.add(config.copy(isActive = false))

            _settings.value = currentSettings.copy(apiConfigs = updatedConfigs)
            saveData()
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
            saveData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteApiConfig(configId: String) {
        try {
            val currentSettings = _settings.value
            val updatedConfigs = currentSettings.apiConfigs.filter { it.id != configId }
            _settings.value = currentSettings.copy(apiConfigs = updatedConfigs)

            val updatedSessions = _chatSessions.value.map { session ->
                session.copy(
                    activeApis = session.activeApis.filter { it != configId }.toMutableList()
                )
            }
            _chatSessions.value = updatedSessions
            saveData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleApiActive(configId: String) {
        try {
            val currentSettings = _settings.value
            val config = currentSettings.apiConfigs.find { it.id == configId } ?: return
            val currentlyActive = currentSettings.apiConfigs.count { it.isActive }

            if (!config.isActive && currentlyActive >= AppTheme.MAX_ACTIVE_APIS_PER_CHAT) {
                return
            }

            val updatedConfigs = currentSettings.apiConfigs.map { api ->
                if (api.id == configId) api.copy(isActive = !api.isActive) else api
            }
            _settings.value = currentSettings.copy(apiConfigs = updatedConfigs)
            saveData()
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
            saveData()
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
            saveData()
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
            saveData()
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
            saveData()
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
            saveData()
        } catch (e: Exception) {
            e.printStackTrace()
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

                // Auto-generate title
                if (session.messages.size == 1 && session.title.startsWith("New Chat")) {
                    val newTitle = text.take(40) + if (text.length > 40) "..." else ""
                    renameChat(session.id, newTitle)
                }

                _chatSessions.value = _chatSessions.value.toList()
                saveData()

                // Update token usage
                _settings.value = currentSettings.copy(
                    tokenUsage = currentSettings.tokenUsage + 150,
                    estimatedCost = currentSettings.estimatedCost + 0.00075
                )

                // Show loading
                _isLoading.value = true

                // REAL API CALL
                val responses = mutableListOf<String>()
                for (api in activeApis) {
                    val result = aiProviderHandler.sendMessage(
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
                _chatSessions.value = _chatSessions.value.toList()
                saveData()

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
            AiMode.THINKING -> "[THINKING MODE] Analyze deeply: $text"
            AiMode.RESEARCH -> "[RESEARCH MODE] Research with citations: $text"
            AiMode.STUDY -> "[STUDY MODE] Explain for learning: $text"
            AiMode.CODE -> "[CODE MODE] Provide code solutions: $text"
            AiMode.CREATIVE -> "[CREATIVE MODE] Be creative: $text"
            else -> text
        }
    }

    private fun appendAiMessage(text: String) {
        try {
            currentSession?.messages?.add(ChatMessage(text = text, isUser = false))
            _chatSessions.value = _chatSessions.value.toList()
            saveData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}