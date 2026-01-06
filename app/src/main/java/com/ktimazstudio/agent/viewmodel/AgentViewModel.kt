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
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _chatSessions = MutableStateFlow<List<ChatSession>>(
        listOf(ChatSession(title = "New Chat"))
    )
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
        if (!settings.isProUser && settings.apiConfigs.size >= AppTheme.FREE_API_LIMIT) {
            return false
        }
        _settings.value = settings.copy(apiConfigs = settings.apiConfigs + config)
        return true
    }

    fun updateApiConfig(configId: String, updatedConfig: ApiConfig) {
        _settings.value = _settings.value.copy(
            apiConfigs = _settings.value.apiConfigs.map {
                if (it.id == configId) updatedConfig else it
            }
        )
    }

    fun deleteApiConfig(configId: String) {
        _settings.value = _settings.value.copy(
            apiConfigs = _settings.value.apiConfigs.filter { it.id != configId }
        )
        _chatSessions.value.forEach { it.activeApis.remove(configId) }
        _chatSessions.value = _chatSessions.value.toList()
    }

    fun toggleApiActive(configId: String) {
        val config = _settings.value.apiConfigs.find { it.id == configId } ?: return
        val currentlyActive = _settings.value.apiConfigs.count { it.isActive }
        
        if (!config.isActive && currentlyActive >= AppTheme.MAX_ACTIVE_APIS_PER_CHAT) return
        
        _settings.value = _settings.value.copy(
            apiConfigs = _settings.value.apiConfigs.map {
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
        _chatSessions.value = _chatSessions.value.map {
            if (it.id == sessionId) it.copy(title = newTitle) else it
        }
        _editingChatId.value = null
    }

    fun deleteChat(sessionId: String) {
        _chatSessions.value = _chatSessions.value.filter { it.id != sessionId }
        if (_currentSessionId.value == sessionId) {
            _currentSessionId.value = _chatSessions.value.firstOrNull()?.id ?: ""
            if (_chatSessions.value.isEmpty()) newChat()
        }
    }

    fun pinChat(sessionId: String) {
        _chatSessions.value = _chatSessions.value.map {
            if (it.id == sessionId) it.copy(isPinned = !it.isPinned) else it
        }
    }

    fun sendUserMessage(text: String, attachments: List<Attachment>, mode: AiMode) {
        val settings = _settings.value
        val currentSession = this.currentSession ?: return

        if (!settings.isProUser && attachments.size > 10) {
            appendAiMessage("⚠️ Free plan limited to 10 attachments. Upgrade to Pro!")
            return
        }

        if (!settings.isProUser && mode.isPro) {
            appendAiMessage("🔒 ${mode.title} Mode requires Pro. Upgrade to unlock!")
            return
        }

        val activeApis = settings.apiConfigs.filter {
            it.isActive && currentSession.activeApis.contains(it.id)
        }

        if (activeApis.isEmpty()) {
            appendAiMessage("⚠️ No active APIs configured. Go to Settings → API Management")
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
            currentSession.messages.add(ChatMessage(text = "...", isUser = false, isStreaming = true))
            _chatSessions.value = _chatSessions.value.toList()

            delay(1500)
            currentSession.messages.removeLast()
            val reply = generateAiReply(userMessage, activeApis)
            currentSession.messages.add(
                ChatMessage(text = reply, isUser = false, mode = mode, usedApis = activeApis.map { it.name })
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
                
                **Final Answer:** Based on deep reasoning with ${activeApis.size} AI model(s)$attachmentInfo$apiInfo
            """.trimIndent()
            
            AiMode.RESEARCH -> """
                🔬 **Deep Research Mode**
                
                **Research Summary:** Topic analyzed
                
                **Key Findings:**
                • Comprehensive analysis completed
                • Multiple AI models consulted
                • Evidence-based conclusions
                
                **Collaborating Models:** ${activeApis.joinToString { it.name }}$attachmentInfo$apiInfo
            """.trimIndent()
            
            else -> """
                Hello! I'm **${AppTheme.APP_NAME}** with ${activeApis.size} active AI model(s) working together.
                
                **Current Configuration:**
                ${activeApis.mapIndexed { index, api -> 
                    "${index + 1}. ${api.name} - ${api.provider.title} (${api.modelName})"
                }.joinToString("\n")}
                
                How can we assist you today?$attachmentInfo
            """.trimIndent()
        }
    }

    private fun appendAiMessage(text: String) {
        currentSession?.messages?.add(ChatMessage(text = text, isUser = false))
        _chatSessions.value = _chatSessions.value.toList()
    }
}
