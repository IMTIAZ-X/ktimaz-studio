package com.ktimazstudio.agent.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ktimazstudio.agent.data.*
import com.ktimazstudio.agent.viewmodel.AgentViewModel

@Composable
fun ModernSidebar(viewModel: AgentViewModel) {
    val settings by viewModel.settings.collectAsState()
    val chatSessions by viewModel.chatSessions.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    var editingChatId by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .shadow(12.dp),
        color = Color(0xFF0F0F1F)
    ) {
        Column(Modifier.fillMaxSize()) {
            // Header
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp)),
                color = Color(0xFF1A1A2E)
            ) {
                Button(
                    onClick = { viewModel.newChat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF667EEA)
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        "➕",
                        null,
                        modifier = Modifier.size(20.sp.value.dp),
                        tint = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "New Chat",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Chat List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val pinnedChats = chatSessions.filter { it.isPinned }
                val regularChats = chatSessions.filter { !it.isPinned }

                if (pinnedChats.isNotEmpty()) {
                    item {
                        Text(
                            "PINNED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(pinnedChats, key = { it.id }) { chat ->
                        ChatItem(
                            chat = chat,
                            isSelected = chat.id == currentSessionId,
                            isEditing = chat.id == editingChatId,
                            onSelect = { viewModel.openChat(chat.id) },
                            onEdit = { editingChatId = chat.id },
                            onRename = {
                                viewModel.renameChat(chat.id, it)
                                editingChatId = null
                            },
                            onDelete = { viewModel.deleteChat(chat.id) },
                            onPin = { viewModel.pinChat(chat.id) }
                        )
                    }
                }

                item {
                    Text(
                        "RECENT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 4.dp)
                    )
                }

                items(regularChats, key = { it.id }) { chat ->
                    ChatItem(
                        chat = chat,
                        isSelected = chat.id == currentSessionId,
                        isEditing = chat.id == editingChatId,
                        onSelect = { viewModel.openChat(chat.id) },
                        onEdit = { editingChatId = chat.id },
                        onRename = {
                            viewModel.renameChat(chat.id, it)
                            editingChatId = null
                        },
                        onDelete = { viewModel.deleteChat(chat.id) },
                        onPin = { viewModel.pinChat(chat.id) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Footer
            SidebarFooter(viewModel, settings)
        }
    }
}

@Composable
fun ChatItem(
    chat: ChatSession,
    isSelected: Boolean,
    isEditing: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onPin: () -> Unit
) {
    var editText by remember { mutableStateOf(chat.title) }
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect() },
        color = if (isSelected) Color(0xFF667EEA).copy(alpha = 0.2f) else Color(0xFF1A1A2E),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
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
                Text("💬", fontSize = 16.sp)
            }

            if (isEditing) {
                TextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color(0xFF667EEA),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                IconButton(
                    onClick = { onRename(editText) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Text("✓", fontSize = 14.sp)
                }
            } else {
                Column(Modifier.weight(1f)) {
                    Text(
                        chat.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Text(
                        "${chat.messageCount} messages",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Text("⋮", fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
                    }

                    if (showMenu) {
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Rename", fontSize = 12.sp) },
                                onClick = { onEdit(); showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text(if (chat.isPinned) "Unpin" else "Pin", fontSize = 12.sp) },
                                onClick = { onPin(); showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", fontSize = 12.sp, color = Color(0xFFFF6B6B)) },
                                onClick = { onDelete(); showMenu = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SidebarFooter(viewModel: AgentViewModel, settings: AppSettings) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!settings.isProUser) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { viewModel.openSettings() },
                color = Brush.linearGradient(
                    listOf(
                        Color(0xFF667EEA).copy(alpha = 0.2f),
                        Color(0xFF764BA2).copy(alpha = 0.2f)
                    )
                ).let { Color(0xFF667EEA).copy(alpha = 0.1f) },
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🚀", fontSize = 16.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Upgrade to Pro",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        "Unlimited APIs & modes",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.1f),
            thickness = 1.dp
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { viewModel.openSettings() },
            color = Color(0xFF1A1A2E),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
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
                    Text("A", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
                Column(Modifier.weight(1f)) {
                    Text("Agent User", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 12.sp)
                    Text(
                        if (settings.isProUser) "Pro Account" else "Free Account",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                Text("⚙", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: RoundedCornerShape = RoundedCornerShape(4.dp),
    colors: ButtonDefaults = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .clip(shape)
            .clickable(enabled = enabled) { onClick() },
        color = colors.containerColor,
        shape = shape
    ) {
        Row(
            modifier = Modifier.padding(contentPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

object ButtonDefaults {
    @Composable
    fun buttonColors(
        containerColor: Color = Color(0xFF667EEA),
        disabledContainerColor: Color = Color.Gray
    ) = ButtonColorData(containerColor, disabledContainerColor)
}

data class ButtonColorData(val containerColor: Color, val disabledContainerColor: Color)

@Composable
fun IconButton(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier
            .clip(CircleShape)
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            content()
        }
    }
}

@Composable
fun DropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    if (expanded) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.3f)
                .shadow(8.dp),
            color = Color(0xFF1A1A2E),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(4.dp),
                content = content
            )
        }
    }
}

@Composable
fun DropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Box(modifier = Modifier.padding(12.dp)) {
            text()
        }
    }
}

@Composable
fun Surface(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    shape: RoundedCornerShape = RoundedCornerShape(0.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(color, shape),
        contentAlignment = Alignment.TopStart
    ) {
        content()
    }
}

@Composable
fun Divider(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray,
    thickness: Dp = 1.dp
) {
    Box(
        modifier = modifier
            .height(thickness)
            .background(color)
    )
}