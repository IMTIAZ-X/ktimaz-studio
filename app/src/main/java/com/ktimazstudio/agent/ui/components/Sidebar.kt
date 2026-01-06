package com.ktimazstudio.agent.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val editingChatId by viewModel.editingChatId.collectAsState()

    Surface(
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight(),
        color = if (settings.isDarkTheme) AppTheme.CardDark.copy(alpha = 0.8f)
        else Color.White.copy(alpha = 0.95f)
    ) {
        Column(Modifier.padding(16.dp)) {
            Button(
                onClick = { viewModel.newChat() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("New Chat", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(Modifier.weight(1f)) {
                val pinnedChats = chatSessions.filter { it.isPinned }
                val regularChats = chatSessions.filter { !it.isPinned }

                if (pinnedChats.isNotEmpty()) {
                    item {
                        Text("Pinned", style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                    }
                    items(pinnedChats, key = { it.id }) { chat ->
                        ChatHistoryCard(
                            chat, 
                            chat.id == currentSessionId, 
                            chat.id == editingChatId,
                            { viewModel.openChat(chat.id) }, 
                            { viewModel.startEditingChat(chat.id) },
                            { viewModel.renameChat(chat.id, it) }, 
                            { viewModel.deleteChat(chat.id) },
                            { viewModel.pinChat(chat.id) }
                        )
                    }
                }

                item {
                    Text("Recent", style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp, top = if (pinnedChats.isNotEmpty()) 16.dp else 0.dp))
                }
                items(regularChats, key = { it.id }) { chat ->
                    ChatHistoryCard(
                        chat, 
                        chat.id == currentSessionId, 
                        chat.id == editingChatId,
                        { viewModel.openChat(chat.id) }, 
                        { viewModel.startEditingChat(chat.id) },
                        { viewModel.renameChat(chat.id, it) }, 
                        { viewModel.deleteChat(chat.id) },
                        { viewModel.pinChat(chat.id) }
                    )
                }
            }

            UserFooter(viewModel)
        }
    }
}

@Composable
fun ChatHistoryCard(
    chat: ChatSession, 
    isSelected: Boolean, 
    isEditing: Boolean,
    onChatClick: () -> Unit, 
    onRename: () -> Unit, 
    onRenameConfirm: (String) -> Unit,
    onDelete: () -> Unit, 
    onPin: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf(chat.title) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onChatClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(listOf(AppTheme.PrimaryStart, AppTheme.PrimaryEnd))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ChatBubble, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }

            Spacer(Modifier.width(12.dp))

            if (isEditing) {
                TextField(
                    value = editText, 
                    onValueChange = { editText = it },
                    modifier = Modifier.weight(1f), 
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
                IconButton(onClick = { onRenameConfirm(editText) }) {
                    Icon(Icons.Default.Check, "Save")
                }
            } else {
                Column(Modifier.weight(1f)) {
                    Text(chat.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${chat.messageCount} msgs", style = MaterialTheme.typography.labelSmall)
                        if (chat.activeApis.isNotEmpty()) {
                            Text(" • ${chat.activeApis.size} APIs", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Menu", modifier = Modifier.size(20.dp))
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Rename") }, onClick = { onRename(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Edit, null) })
                        DropdownMenuItem(text = { Text(if (chat.isPinned) "Unpin" else "Pin") },
                            onClick = { onPin(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.PushPin, null) })
                        DropdownMenuItem(text = { Text("Delete") }, onClick = { onDelete(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) })
                    }
                }
            }
        }
    }
}

@Composable
fun UserFooter(viewModel: AgentViewModel) {
    val settings by viewModel.settings.collectAsState()
    
    Column {
        if (!settings.isProUser) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openSettings() },
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(
                            Color(0xFFF093FB).copy(alpha = 0.3f),
                            Color(0xFFF5576C).copy(alpha = 0.3f)
                        )))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Rocket, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Upgrade to Pro", fontWeight = FontWeight.Bold)
                        }
                        Text("Unlimited APIs & all AI modes", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.openSettings() }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(AppTheme.PrimaryStart, AppTheme.PrimaryEnd))),
                contentAlignment = Alignment.Center
            ) {
                Text("A", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Agent User", fontWeight = FontWeight.SemiBold)
                Text(if (settings.isProUser) "Pro Account" else "Free Account",
                    style = MaterialTheme.typography.labelMedium)
            }
            Icon(Icons.Default.Settings, "Settings", tint = MaterialTheme.colorScheme.primary)
        }
    }
}