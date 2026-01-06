package com.ktimazstudio.agent.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ktimazstudio.agent.data.*
import com.ktimazstudio.agent.viewmodel.AgentViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatInterface(viewModel: AgentViewModel) {
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val chatSessions by viewModel.chatSessions.collectAsState()
    val currentSession = chatSessions.find { it.id == currentSessionId }
    val messages = currentSession?.messages ?: emptyList()
    val settings by viewModel.settings.collectAsState()
    var input by remember { mutableStateOf("") }
    val attachedFiles = remember { mutableStateListOf<com.ktimazstudio.agent.data.Attachment>() }
    val selectedMode by viewModel.selectedMode.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(0) }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (currentSession != null && currentSession.activeApis.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                tonalElevation = 2.dp
            ) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        Text("Active APIs:", style = MaterialTheme.typography.labelMedium)
                    }
                    items(currentSession.activeApis) { apiId ->
                        val apiConfig = settings.apiConfigs.find { it.id == apiId }
                        if (apiConfig != null) {
                            ApiChip(api = apiConfig, onRemove = { viewModel.toggleApiForCurrentChat(apiId) })
                        }
                    }
                }
            }
        }

        if (messages.isEmpty()) {
            Box(modifier = Modifier
                .fillMaxSize()
                .weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(24.dp))
                    Text("Welcome to ${AppTheme.APP_NAME}", style = MaterialTheme.typography.headlineMedium)
                    if (currentSession?.activeApis?.isEmpty() == true) {
                        Spacer(Modifier.height(16.dp))
                        Text("⚠️ No APIs active", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.openSettings() }) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add APIs")
                        }
                    }
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
                if (input.isNotBlank() || attachedFiles.isNotEmpty()) {
                    viewModel.sendUserMessage(input, attachedFiles.toList(), selectedMode)
                    input = ""
                    attachedFiles.clear()
                }
            },
            attachedFiles = attachedFiles,
            viewModel = viewModel,
            selectedMode = selectedMode
        )
    }
}