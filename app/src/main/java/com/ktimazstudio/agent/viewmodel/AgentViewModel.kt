package com.ktimazstudio.agent.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ktimazstudio.agent.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class AgentViewModel(context: Context) : ViewModel() {
    
    private val dataManager: SimpleDataManager = SimpleDataManager.getInstance(context.applicationContext)
    private val aiHandler = AiProviderHandler()
    
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _chatSessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val chatSessions: StateFlow<List<ChatSession>> = _chatSessions.asStateFlow()

    private val _currentSessionId = MutableStateFlow("")
    val currentSessionId: StateFlow<String> = _currentSessionId.asStateFlow()

    private val _isSidebarOpen = MutableStateFlow(true)
    val isSidebarOpen: StateFlow<Boolean> = _isSidebarOpen.asStateFlow()

    private val _isSettingsModalOpen = MutableStateFlow(false)
    val isSettingsModalOpen: StateFlow<Boolean> = _isSettingsModalOpen.asStateFlow()

    private val _selectedMode = MutableStateFlow(AiMode.STANDARD)
    val selectedMode: StateFlow<AiMode> = _selectedMode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val currentSession: ChatSession?
        get() = try { _chatSessions.value.find { it.id == _currentSessionId.value } } catch (e: Exception) { null }

    val activeApiCount: Int
        get() = try { _settings.value.apiConfigs.count { it.isActive } } catch (e: Exception) { 0 }

    init {
        loadAllData()
    }

    private fun loadAllData() {
        viewModelScope.launch {
            try {
                // Load settings
                val loadedSettings = dataManager.loadSettings()
                val loadedConfigs = dataManager.loadApiConfigs()
                _settings.value = loadedSettings.copy(apiConfigs = loadedConfigs)
                
                // Load sessions
                val loadedSessions = dataManager.loadChatSessions()
                if (loadedSessions.isEmpty()) {
                    val newSession = ChatSession(title = "New Chat")
                    _chatSessions.value = listOf(newSession)
                    _currentSessionId.value = newSession.id
                    saveAllData()
                } else {
                    _chatSessions.value = loadedSessions
                    _currentSessionId.value = loadedSessions.firstOrNull()?.id ?: ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val defaultSession = ChatSession(title = "New Chat")
                _chatSessions.value = listOf(defaultSession)
                _currentSessionId.value = defaultSession.id
            }
        }
    }

    private fun saveAllData() {
        viewModelScope.launch {
            try {
                dataManager.saveSettings(_settings.value)
                dataManager.saveApiConfigs(_settings.value.apiConfigs)
                dataManager.saveChatSessions(_chatSessions.value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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
        saveAllData()
    }

    fun toggleTheme(isDark: Boolean) {
        _settings.value = _settings.value.copy(isDarkTheme = isDark)
        saveAllData()
    }

    fun setSelectedMode(mode: AiMode) {
        _selectedMode.value = mode
    }

    fun addApiConfig(config: ApiConfig): Boolean {
        return try {
            val current = _settings.value
            if (!current.isProUser && current.apiConfigs.size >= AppTheme.FREE_API_LIMIT) return false
            if (config.apiKey.isBlank()) return false

            val updated = current.apiConfigs.toMutableList()
            updated.add(config.copy(isActive = false))
            _settings.value = current.copy(apiConfigs = updated)
            saveAllData()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun updateApiConfig(configId: String, updatedConfig: ApiConfig) {
        try {
            val current = _settings.value
            val updated = current.apiConfigs.map {
                if (it.id == configId) updatedConfig.copy(id = configId) else it
            }
            _settings.value = current.copy(apiConfigs = updated)
            saveAllData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteApiConfig(configId: String) {
        try {
            val current = _settings.value
            val updated = current.apiConfigs.filter { it.id != configId }
            _settings.value = current.copy(apiConfigs = updated)

            val updatedSessions = _chatSessions.value.map { session ->
                session.copy(activeApis = session.activeApis.filter { it != configId }.toMutableList())
            }
            _chatSessions.value = updatedSessions
            saveAllData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleApiActive(configId: String) {
        try {
            val current = _settings.value
            val config = current.apiConfigs.find { it.id == configId } ?: return
            val activeCount = current.apiConfigs.count { it.isActive }

            if (!config.isActive && activeCount >= AppTheme.MAX_ACTIVE_APIS_PER_CHAT) return

            val updated = current.apiConfigs.map {
                if (it.id == configId) it.copy(isActive = !it.isActive) else it
            }
            _settings.value = current.copy(apiConfigs = updated)
            saveAllData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleApiForCurrentChat(configId: String) {
        try {
            val session = currentSession ?: return
            val apis = session.activeApis.toMutableList()

            if (apis.contains(configId)) {
                apis.remove(configId)
            } else {
                if (apis.size >= AppTheme.MAX_ACTIVE_APIS_PER_CHAT) apis.removeAt(0)
                apis.add(configId)
            }

            val updated = _chatSessions.value.map {
                if (it.id == session.id) it.copy(activeApis = apis) else it
            }
            _chatSessions.value = updated
            saveAllData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun newChat() {
        try {
            val newSession = ChatSession(title = "New Chat")
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

    fun renameChat(sessionId: String, newTitle: String) {
        try {
            val updated = _chatSessions.value.map {
                if (it.id == sessionId) it.copy(title = newTitle) else it
            }
            _chatSessions.value = updated
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
                if (_chatSessions.value.isEmpty()) newChat()
            }
            saveAllData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pinChat(sessionId: String) {
        try {
            val updated = _chatSessions.value.map {
                if (it.id == sessionId) it.copy(isPinned = !it.isPinned) else it
            }
            _chatSessions.value = updated
            saveAllData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendUserMessage(text: String, attachments: List<Attachment>, mode: AiMode) {
        viewModelScope.launch {
            try {
                val current = _settings.value
                val session = currentSession ?: run { newChat(); return@launch }

                if (!current.isProUser && attachments.size > 10) {
                    addAiMessage("Free plan: 10 attachments max")
                    return@launch
                }

                if (!current.isProUser && mode.isPro) {
                    addAiMessage("${mode.title} Mode requires Pro")
                    return@launch
                }

                val activeApis = current.apiConfigs.filter {
                    it.isActive && session.activeApis.contains(it.id) && it.apiKey.isNotBlank()
                }

                if (activeApis.isEmpty()) {
                    addAiMessage("No active APIs. Add APIs in Settings.")
                    return@launch
                }

                // Add user message
                val userMsg = ChatMessage(text = text.trim(), isUser = true, attachments = attachments, mode = mode)
                session.messages.add(userMsg)

                // Auto-title
                if (session.messages.size == 1 && session.title.startsWith("New Chat")) {
                    renameChat(session.id, text.take(40) + if (text.length > 40) "..." else "")
                }

                _chatSessions.value = _chatSessions.value.toList()
                saveAllData()

                // Update stats
                _settings.value = current.copy(
                    tokenUsage = current.tokenUsage + 150,
                    estimatedCost = current.estimatedCost + 0.00075
                )

                _isLoading.value = true

                // Call APIs
                val responses = mutableListOf<String>()
                for (api in activeApis) {
                    val result = aiHandler.sendMessage(api, buildPrompt(text, mode), api.systemRole)
                    result.fold(
                        onSuccess = { response -> 
                            responses.add("**${api.name}**: $response")
                        },
                        onFailure = { error -> 
                            responses.add("**${api.name}** Error: ${error.message}")
                        }
                    )
                }

                _isLoading.value = false

                // Add AI reply
                val aiMsg = ChatMessage(
                    text = responses.joinToString("\n\n---\n\n"),
                    isUser = false,
                    mode = mode,
                    usedApis = activeApis.map { it.name }
                )
                session.messages.add(aiMsg)
                _chatSessions.value = _chatSessions.value.toList()
                saveAllData()

            } catch (e: Exception) {
                e.printStackTrace()
                _isLoading.value = false
                addAiMessage("Error: ${e.message}")
            }
        }
    }

    private fun buildPrompt(text: String, mode: AiMode): String {
        return when (mode) {
            AiMode.THINKING -> "[THINK DEEPLY] $text"
            AiMode.RESEARCH -> "[RESEARCH] $text"
            AiMode.STUDY -> "[STUDY] $text"
            AiMode.CODE -> "[CODE] $text"
            AiMode.CREATIVE -> "[CREATIVE] $text"
            else -> text
        }
    }

    private fun addAiMessage(text: String) {
        try {
            currentSession?.messages?.add(ChatMessage(text = text, isUser = false))
            _chatSessions.value = _chatSessions.value.toList()
            saveAllData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}