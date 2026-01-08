package com.ktimazstudio.agent.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val attachedFiles = remember { mutableStateListOf<Attachment>() }
    val selectedMode by viewModel.selectedMode.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(0) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0F0F1F), Color(0xFF1A1A2E), Color(0xFF0F0F1F))))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Active APIs Bar
            if (currentSession != null && currentSession.activeApis.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp)
                        .background(Color(0xFF1A1A2E))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("☁", fontSize = 16.sp)
                            Text("Active APIs", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFFFFF).copy(alpha = 0.7f))
                        }
                    }
                }
            }

            // Messages Area
            if (messages.isEmpty()) {
                WelcomeScreen(viewModel, currentSession)
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    state = listState,
                    reverseLayout = true,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages.reversed().size) { index ->
                        val msg = messages.reversed()[index]
                        ModernMessageBubble(msg)
                    }
                }
            }

            // Input Bar
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
}

@Composable
fun WelcomeScreen(viewModel: AgentViewModel, session: ChatSession?) {
    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2)))),
                contentAlignment = Alignment.Center
            ) {
                Text("✨", fontSize = 50.sp)
            }

            Spacer(Modifier.height(32.dp))

            Text("Welcome to AI Agent", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)

            Spacer(Modifier.height(12.dp))

            Text("Your Advanced AI Assistant", fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Medium)

            Spacer(Modifier.height(24.dp))

            if (session?.activeApis?.isEmpty() == true) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFF6B6B).copy(alpha = 0.1f))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚠", fontSize = 24.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("No APIs Active", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Configure APIs in settings", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.openSettings() },
                    modifier = Modifier.fillMaxWidth(0.8f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF667EEA))
                ) {
                    Text("⚙ Open Settings", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ModernMessageBubble(msg: ChatMessage) {
    val isUser = msg.isUser
    val bubbleColor = if (isUser) Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2)))
    else Brush.linearGradient(listOf(Color(0xFF1F2937), Color(0xFF111827)))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start) {
        Box(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = if (isUser) 20.dp else 4.dp, bottomEnd = if (isUser) 4.dp else 20.dp))
                .background(bubbleColor)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp), clip = true)
                .padding(16.dp)
        ) {
            Column {
                Text(msg.text, color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)

                if (msg.usedApis.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        msg.usedApis.forEach { api ->
                            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text(api, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                if (msg.isStreaming) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(3) {
                            Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(Color.White.copy(alpha = 0.6f)))
                        }
                    }
                }
            }
        }
    }

    Spacer(Modifier.height(4.dp))
}