package com.ktimazstudio.agent.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ktimazstudio.agent.data.*
import com.ktimazstudio.agent.viewmodel.AgentViewModel
import kotlinx.coroutines.launch
import com.ktimazstudio.agent.ui.settings.createButtonColors // Added
import com.ktimazstudio.agent.ui.settings.ButtonColorData // Added

@Composable
fun ChatInterface(viewModel: AgentViewModel) {
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val chatSessions by viewModel.chatSessions.collectAsState()
    val currentSession = chatSessions.find { it.id == currentSessionId }
    val messages = currentSession?.messages ?: emptyList()
    val settings by viewModel.settings.collectAsState()
    var input by remember { mutableStateOf("") }
    val attachedFiles = remember { mutableStateListOf<Attachment>() }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Chat Header (optional: show current session info, mode, etc.)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp),
            color = Color(0xFF1A1A2E)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (settings.showSidebar) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = "Toggle Sidebar",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { viewModel.toggleSidebar() }
                    )
                    Spacer(Modifier.width(16.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        currentSession?.title ?: "New Chat",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        settings.selectedMode.displayName,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Stop Generating Button
                if (isLoading) {
                    Button(
                        onClick = { viewModel.stopGeneration() },
                        colors = createButtonColors( // Fixed L287 Argument type mismatch
                            ButtonColorData(
                                containerColor = Color.Red,
                                contentColor = Color.White,
                                disabledContainerColor = Color.Red.copy(alpha = 0.5f),
                                disabledContentColor = Color.White.copy(alpha = 0.5f)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Stop Generating")
                    }
                }
            }
        }

        // Message List
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f) // Fixed L199 Expression 'weight' of type 'Modifier' cannot be invoked as a function.
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    ChatMessage(message = message)
                }
            }
        }

        // Input Bar
        ModernInputBar(
            input = input,
            onInputChange = { input = it },
            onSend = {
                if (input.isNotBlank()) {
                    viewModel.sendMessage(input, attachedFiles.toList())
                    input = ""
                    attachedFiles.clear()
                    coroutineScope.launch {
                        // Wait for the new item to be added before scrolling
                        listState.animateScrollToItem(messages.size)
                    }
                }
            },
            attachedFiles = attachedFiles,
            viewModel = viewModel,
            selectedMode = settings.selectedMode
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun ChatMessage(message: Message) {
    val isUser = message.sender == Sender.USER
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isUser) Color(0xFF667EEA) else Color(0xFF2A2A4A)
    val textColor = Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 0.8f.dp)
                .clip(RoundedCornerShape(12.dp)),
            color = bubbleColor,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                // Attachments
                if (message.attachments.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        items(message.attachments) { attachment ->
                            Surface(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (attachment.isImage) Icons.Default.Image else Icons.Default.Description,
                                        null,
                                        tint = Color(0xFF4ECDC4),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        attachment.name.take(10) + if (attachment.name.length > 10) "..." else "",
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Message Text
                if (isUser) {
                    Text(
                        message.text,
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                } else {
                    val brush = Brush.verticalGradient(
                        colors = listOf(Color.White, Color.White.copy(alpha = 0.9f))
                    )
                    Text(
                        message.text,
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )

                    // API Callout
                    if (message.usedApis.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Used APIs:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            message.usedApis.forEach { api ->
                                Surface(
                                    color = Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        api,
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    if (message.isStreaming) {
                        Spacer(Modifier.height(8.dp))
                        LoadingDots()
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))
    }
}

@Composable
fun LoadingDots() {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.6f))
            )
        }
    }
}