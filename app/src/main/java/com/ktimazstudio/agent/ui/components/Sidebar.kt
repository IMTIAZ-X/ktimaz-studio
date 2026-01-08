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
import androidx.compose.material3.*
import androidx.compose.ui.unit.Dp
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import com.ktimazstudio.agent.data.*
import com.ktimazstudio.agent.viewmodel.AgentViewModel
import androidx.compose.material.icons.Icons // Added
import androidx.compose.material.icons.filled.Add // Added
import androidx.compose.material.icons.filled.Settings // Added
import androidx.compose.material.icons.filled.Delete // Added
import androidx.compose.material.icons.filled.MoreVert // Added
import androidx.compose.material.icons.filled.Close // Added
import com.ktimazstudio.agent.ui.settings.createButtonColors // Added
import com.ktimazstudio.agent.ui.settings.ButtonColorData // Added
import com.ktimazstudio.agent.ui.settings.TextFieldColorData // Added
import com.ktimazstudio.agent.ui.settings.createTextFieldColors // Added

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
                    colors = createButtonColors( // Fixed L54 Argument type mismatch
                        ButtonColorData(
                            containerColor = Color(0xFF667EEA),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF667EEA).copy(alpha = 0.5f),
                            disabledContentColor = Color.White.copy(alpha = 0.5f)
                        )
                    )
                ) {
                    Icon( // Fixed L59 Unresolved reference 'Icon'
                        Icons.Default.Add,
                        contentDescription = "New Chat",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text( // Fixed L66 Unresolved reference 'Text'
                        "New Chat",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Chat List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chatSessions) { session ->
                    val isSelected = session.id == currentSessionId
                    val chatTitle = session.title.ifBlank { "New Chat" }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.loadSession(session.id)
                                editingChatId = null
                            },
                        color = if (isSelected) Color(0xFF2A2A4A) else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Chat Title
                            if (session.id == editingChatId) {
                                // Editing TextField
                                var newTitle by remember { mutableStateOf(session.title) }

                                TextField( // Fixed L198 Unresolved reference 'TextField'
                                    value = newTitle,
                                    onValueChange = { newTitle = it },
                                    label = { Text("Edit Title") }, // Fixed L194 Unresolved reference 'Text'
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .padding(end = 8.dp),
                                    colors = createTextFieldColors( // Fixed L203 Unresolved reference 'TextFieldDefaults'
                                        TextFieldColorData(
                                            focusedContainerColor = Color(0xFF1A1A2E),
                                            unfocusedContainerColor = Color(0xFF1A1A2E),
                                            focusedIndicatorColor = Color(0xFF4ECDC4),
                                            unfocusedIndicatorColor = Color(0xFF4ECDC4).copy(alpha = 0.5f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                )
                                IconButton(onClick = {
                                    if (newTitle.isNotBlank()) {
                                        viewModel.updateSessionTitle(session.id, newTitle)
                                    }
                                    editingChatId = null
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Done")
                                }

                            } else {
                                Text( // Fixed L90 Unresolved reference 'Text'
                                    chatTitle,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                // Menu Icon
                                var showMenu by remember { mutableStateOf(false) }
                                Box {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "More options",
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable { showMenu = true }
                                    )
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Edit") }, // Fixed L216 Unresolved reference 'Text'
                                            onClick = { editingChatId = session.id; showMenu = false }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Delete") }, // Fixed L220 Unresolved reference 'Text'
                                            onClick = {
                                                viewModel.deleteSession(session.id)
                                                showMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Settings and Clear All
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = Color.Gray.copy(alpha = 0.2f),
                thickness = 1.dp
            )
            Spacer(Modifier.height(8.dp))

            // Settings Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { viewModel.openSettings() },
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text( // Fixed L240 Unresolved reference 'Text'
                        "Settings",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Clear All Button
            val redButtonColors = ButtonColorData( // Fixed L371/L379: This line is just a data class instantiation
                containerColor = Color.Red.copy(alpha = 0.2f),
                contentColor = Color.Red,
                disabledContainerColor = Color.Red.copy(alpha = 0.1f),
                disabledContentColor = Color.Red.copy(alpha = 0.5f)
            )

            Button(
                onClick = { viewModel.deleteAllSessions() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = createButtonColors(redButtonColors), // Fixed L371 type mismatch
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Clear All",
                    tint = Color.Red.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text( // Fixed L359 Unresolved reference 'Text'
                    "Clear All Chats",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// Custom/Material 2 Dropdown functions (original code that shadows material3 ones)
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
    thickness: Dp = 1.dp // Fixed L477 Unresolved reference 'Dp' by ensuring it is imported correctly (it was, but explicit import fixed the issue)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color)
    )
}

@Composable
fun Spacer(modifier: Modifier) {
    Box(modifier = modifier)
}