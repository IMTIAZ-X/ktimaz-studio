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
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0F1F),
                        Color(0xFF1A1A2E),
                        Color(0xFF0F0F1F)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (currentSession != null && currentSession.activeApis.isNotEmpty()) {
                ModernActiveApiBar(
                    apis = currentSession.activeApis,
                    settings = settings,
                    onRemove = { viewModel.toggleApiForCurrentChat(it) }
                )
            }

            if (messages.isEmpty()) {
                // FIX: Pass the weight modifier to WelcomeScreen from the ColumnScope
                WelcomeScreen(
                    viewModel, 
                    currentSession, 
                    AppTheme.APP_NAME,
                    modifier = Modifier.weight(1f, fill = true)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    state = listState,
                    reverseLayout = true,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages.reversed(), key = { it.id }) { msg ->
                        ModernMessageBubble(msg)
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
}

@Composable
fun ModernActiveApiBar(
    apis: List<String>,
    settings: AppSettings,
    onRemove: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp),
        color = Color(0xFF1A1A2E).copy(alpha = 0.95f),
        tonalElevation = 0.dp
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Cloud,
                    contentDescription = null,
                    tint = Color(0xFF4ECDC4),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Active APIs",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFFFFF).copy(alpha = 0.7f)
                )
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(apis) { apiId ->
                    val apiConfig = settings.apiConfigs.find { it.id == apiId }
                    apiConfig?.let {
                        ModernApiChip(
                            api = it,
                            onRemove = { onRemove(apiId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernApiChip(api: ApiConfig, onRemove: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { onRemove() })
            },
        color = api.provider.color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(api.provider.color)
            )
            Text(
                api.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
fun WelcomeScreen(
    viewModel: AgentViewModel, 
    session: ChatSession?, 
    appName: String,
    modifier: Modifier = Modifier // FIX: Added modifier parameter
) {
    Column(
        modifier = modifier.fillMaxSize(), // FIX: Use the passed modifier and chain fillMaxSize
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF667EEA),
                            Color(0xFF764BA2)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Psychology,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(Modifier.height(32.dp))

        Text(
            "Welcome to $appName",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "Your Advanced AI Agent Assistant",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(24.dp))

        if (session?.activeApis?.isEmpty() == true) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .clip(RoundedCornerShape(16.dp)),
                color = Color(0xFFFF6B6B).copy(alpha = 0.1f),
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFFF6B6B),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No APIs Active",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Configure APIs in settings to begin",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.openSettings() },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667EEA)
                )
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Open Settings", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ModernMessageBubble(msg: ChatMessage) {
    val isUser = msg.isUser
    val bubbleColor = if (isUser) {
        Brush.linearGradient(
            listOf(Color(0xFF667EEA), Color(0xFF764BA2))
        )
    } else {
        Brush.linearGradient(
            listOf(Color(0xFF1F2937), Color(0xFF111827))
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 20.dp
                    )
                )
                .background(bubbleColor)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 20.dp
                    ),
                    clip = true
                )
                .padding(16.dp)
        ) {
            Column {
                Text(
                    msg.text,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                if (msg.usedApis.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        msg.usedApis.forEach { api ->
                            Surface(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                tonalElevation = 0.dp
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

                if (msg.isStreaming) {
                    Spacer(Modifier.height(8.dp))
                    LoadingDots()
                }
            }
        }
    }

    Spacer(Modifier.height(4.dp))
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