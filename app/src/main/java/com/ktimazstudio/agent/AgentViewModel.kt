package com.ktimazstudio.agent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class AgentMessage(val text: String, val isUser: Boolean)

class AgentViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<AgentMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    // append helper
    private fun append(msg: AgentMessage) {
        _messages.value = _messages.value + msg
    }

    fun sendUserMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        // add user message immediately
        append(AgentMessage(trimmed, isUser = true))

        // simulate AI processing & reply
        viewModelScope.launch {
            delay(600) // small UI delay for feel
            val reply = generateReply(trimmed)
            append(AgentMessage(reply, isUser = false))
        }
    }

    private fun generateReply(input: String): String {
        val lower = input.lowercase()
        // very small rule-based responses — extend as needed
        return when {
            listOf("hi", "hello", "hey").any { lower.contains(it) } ->
                "Hello! I'm your Agent. How can I help you today?"
            lower.contains("time") ->
                "Current time: ${LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))}"
            lower.contains("help") ->
                "Tell me what you want to do (example: 'search', 'open settings', 'tell me a joke')."
            lower.contains("joke") ->
                "Why did the developer go broke? Because they used up all their cache. 😄"
            lower.contains("reverse") ->
                // if user says "reverse some text" -> reverse the rest
                input.substringAfter("reverse", "").trim().ifEmpty { "Say 'reverse <text>' to reverse text." }
                    .let { it.ifEmpty { "Say 'reverse <text>' to reverse text." } }
                    .reversed()
            else ->
                // default echo-style reply
                "You said: \"$input\" — I can echo, show time, or answer simple commands. Try 'time' or 'joke'."
        }
    }
}