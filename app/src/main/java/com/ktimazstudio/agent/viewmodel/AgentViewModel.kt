package com.ktimazstudio.agent.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ktimazstudio.agent.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AgentViewModel(context: Context) : ViewModel() {
    
    // Use applicationContext to prevent memory leaks
    private val dataManager = SimpleDataManager.getInstance(context.applicationContext)
    private val aiProviderHandler = AiProviderHandler()
    
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
        get() = _chatSessions.value.find { it.id == _currentSessionId.value }

    val activeApiCount: Int
        get() = _settings.value.apiConfigs.count { it.isActive }

    init {
        // Wrap initialization in try-catch to prevent app-open crash
        try {
            loadPersistedData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadPersistedData() {
        try {
            val loadedSettings = dataManager.loadSettings()
            val loadedConfigs = dataManager.loadApiConfigs()
            _settings.value = loadedSettings.copy(apiConfigs = loadedConfigs)
            
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

    private fun saveData() {
        try {
            dataManager.saveSettings(_settings.value)
            dataManager.saveApiConfigs(_settings.value.apiConfigs)
            dataManager.saveChatSessions(_chatSessions.value)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleSidebar() { _isSidebarOpen.value = !_isSidebarOpen.value }
    fun openSettings() { _isSettingsModalOpen.value = true }
    fun closeSettings() { _isSettingsModalOpen.value = false }
    fun toggleProPlan(isPro: Boolean) { _settings.value = _settings.value.copy(isProUser = isPro); saveData() }
    fun toggleTheme(isDark: Boolean) { _settings.value = _settings.value.copy(isDarkTheme = isDark); saveData() }
    fun setSelectedMode(mode: AiMode) { _selectedMode.value = mode }

    fun addApiConfig(config: ApiConfig): Boolean {
        return try {
            val currentSettings = _settings.value
            if (!currentSettings.isProUser && currentSettings.apiConfigs.size >= 5) return false // Use hardcoded limit if AppTheme missing
            if (config.apiKey.isBlank()) return false
            val updatedConfigs = currentSettings.apiConfigs.toMutableList()
            updatedConfigs.add(config.copy(isActive = false))
            _settings.value = currentSettings.copy(apiConfigs = updatedConfigs)
            saveData()
            true
        } catch (e: Exception) { e.printStackTrace(); false }
    }

    fun updateApiConfig(configId: String, updatedConfig: ApiConfig) {
        try {
            val currentSettings = _settings.value
            val updatedConfigs = currentSettings.apiConfigs.map { api ->
                if (api.id == configId) updatedConfig.copy(id = configId) else api
            }
            _settings.value = currentSettings.copy(apiConfigs = updatedConfigs)
            saveData()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun deleteApiConfig(configId: String) {
        try {
            val currentSettings = _settings.value
            val updatedConfigs = currentSettings.apiConfigs.filter { it.id != configId }
            _settings.value = currentSettings.copy(apiConfigs = updatedConfigs)
            val updatedSessions = _chatSessions.value.map { session ->
                session.copy(activeApis = session.activeApis.filter { it != configId }.toMutableList())
            }
            _chatSessions.value = updatedSessions
            saveData()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun toggleApiActive(configId: String) {
        try {
            val currentSettings = _settings.value
            val config = currentSettings.apiConfigs.find { it.id == configId } ?: return
            val currentlyActive = currentSettings.apiConfigs.count { it.isActive }
            if (!config.isActive && currentlyActive >= 3) return 
            val updatedConfigs = currentSettings.apiConfigs.map { api ->
                if (api.id == configId) api.copy(isActive = !api.isActive) else api
            }
            _settings.value = currentSettings.copy(apiConfigs = updatedConfigs)
            saveData()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun toggleApiForCurrentChat(configId: String) {
        try {
            val session = currentSession ?: return
            val mutableApis = session.activeApis.toMutableList()
            if (mutableApis.contains(configId)) {
                mutableApis.remove(configId)
            } else {
                if (mutableApis.size >= 3) mutableApis.removeAt(0)
                mutableApis.add(configId)
            }
            val updatedSessions = _chatSessions.value.map { s ->
                if (s.id == session.id) s.copy(activeApis = mutableApis) else s
            }
            _chatSessions.value = updatedSessions
            saveData()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun newChat() {
        try {
            val newSession = ChatSession(title = "New Chat")
            _chatSessions.value = listOf(newSession) + _chatSessions.value
            _currentSessionId.value = newSession.id
            _selectedMode.value = AiMode.STANDARD
            saveData()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun openChat(sessionId: String) { _currentSessionId.value = sessionId }
    fun renameChat(sessionId: String, newTitle: String) {
        try {
            val updatedSessions = _chatSessions.value.map { chat ->
                if (chat.id == sessionId) chat.copy(title = newTitle) else chat
            }
            _chatSessions.value = updatedSessions
            saveData()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun deleteChat(sessionId: String) {
        try {
            _chatSessions.value = _chatSessions.value.filter { it.id != sessionId }
            if (_currentSessionId.value == sessionId) {
                _currentSessionId.value = _chatSessions.value.firstOrNull()?.id ?: ""
                if (_chatSessions.value.isEmpty()) newChat()
            }
            saveData()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun pinChat(sessionId: String) {
        try {
            val updatedSessions = _chatSessions.value.map { chat ->
                if (chat.id == sessionId) chat.copy(isPinned = !chat.isPinned) else chat
            }
            _chatSessions.value = updatedSessions
            saveData()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun sendUserMessage(text: String, attachments: List<Attachment>, mode: AiMode) {
        viewModelScope.launch {
            try {
                val currentSettings = _settings.value
                val session = currentSession ?: return@launch
                
                val userMessage = ChatMessage(text = text.trim(), isUser = true, attachments = attachments, mode = mode)
                session.messages.add(userMessage)
                
                if (session.messages.size == 1 && session.title.startsWith("New Chat")) {
                    renameChat(session.id, text.take(40))
                }
                _chatSessions.value = _chatSessions.value.toList()
                _isLoading.value = true

                val activeApis = currentSettings.apiConfigs.filter { it.isActive && session.activeApis.contains(it.id) }
                val responses = mutableListOf<String>()
                
                for (api in activeApis) {
                    aiProviderHandler.sendMessage(api, buildPrompt(text, mode), api.systemRole).fold(
                        onSuccess = { responses.add("**${api.name}**: $it") },
                        onFailure = { responses.add("**${api.name}** Error: ${it.message}") }
                    )
                }

                _isLoading.value = false
                session.messages.add(ChatMessage(text = responses.joinToString("\n\n---\n\n"), isUser = false, mode = mode))
                _chatSessions.value = _chatSessions.value.toList()
                saveData()
            } catch (e: Exception) {
                _isLoading.value = false
                e.printStackTrace()
            }
        }
    }

    private fun buildPrompt(text: String, mode: AiMode): String = text
    private fun appendAiMessage(text: String) {
        currentSession?.messages?.add(ChatMessage(text = text, isUser = false))
        _chatSessions.value = _chatSessions.value.toList()
    }
}

// THIS IS CRITICAL TO PREVENT CRASH ON STARTUP
class AgentViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AgentViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
