package com.ktimazstudio.agent.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
fun ModernInputBar(
    input: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    attachedFiles: MutableList<Attachment>,
    viewModel: AgentViewModel,
    selectedMode: AiMode
) {
    val settings by viewModel.settings.collectAsState()
    var isMenuOpen by remember { mutableStateOf(false) }
    var isModeMenuOpen by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            attachedFiles.add(Attachment(name = "image_${System.currentTimeMillis()}.jpg",
                type = AttachmentType.IMAGE, uri = it))
        }
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            attachedFiles.add(Attachment(name = "file_${System.currentTimeMillis()}.txt",
                type = AttachmentType.DOCUMENT, uri = it))
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        if (attachedFiles.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                items(attachedFiles.toList(), key = { it.id }) { attachment ->
                    AttachmentChip(attachment, onRemove = { attachedFiles.remove(attachment) })
                }
            }
        }

        if (selectedMode != AiMode.STANDARD) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(selectedMode.icon, fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("${selectedMode.title} Active", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.setSelectedMode(AiMode.STANDARD) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.Bottom) {
                Box {
                    IconButton(onClick = { isMenuOpen = !isMenuOpen }) {
                        Icon(Icons.Default.Add, "Add", tint = MaterialTheme.colorScheme.primary)
                    }
                    DropdownMenu(expanded = isMenuOpen, onDismissRequest = { isMenuOpen = false }) {
                        DropdownMenuItem(text = { Text("Upload Image") },
                            onClick = { imagePicker.launch("image/*"); isMenuOpen = false },
                            leadingIcon = { Icon(Icons.Default.Image, null) })
                        DropdownMenuItem(text = { Text("Upload File") },
                            onClick = { filePicker.launch("*/*"); isMenuOpen = false },
                            leadingIcon = { Icon(Icons.Default.AttachFile, null) })
                    }
                }

                Box {
                    IconButton(onClick = { isModeMenuOpen = !isModeMenuOpen }) {
                        Icon(Icons.Default.Psychology, "Modes", tint = MaterialTheme.colorScheme.primary)
                    }
                    DropdownMenu(expanded = isModeMenuOpen, onDismissRequest = { isModeMenuOpen = false }) {
                        AiMode.values().forEach { mode ->
                            val isLocked = mode.isPro && !settings.isProUser
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(mode.icon)
                                        Spacer(Modifier.width(8.dp))
                                        Text(mode.title)
                                        if (isLocked) { Spacer(Modifier.width(8.dp)); ProBadge() }
                                    }
                                },
                                onClick = { if (!isLocked) { viewModel.setSelectedMode(mode); isModeMenuOpen = false } },
                                leadingIcon = {
                                    Icon(if (isLocked) Icons.Default.Lock else Icons.Default.CheckCircle, null,
                                        tint = if (isLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                                }
                            )
                        }
                    }
                }

                TextField(
                    value = input, onValueChange = onInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message ${AppTheme.APP_NAME}...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    minLines = 1, maxLines = 5
                )

                IconButton(
                    onClick = onSend,
                    enabled = input.isNotBlank() || attachedFiles.isNotEmpty(),
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(
                        if (input.isNotBlank() || attachedFiles.isNotEmpty())
                            Brush.linearGradient(listOf(AppTheme.PrimaryStart, AppTheme.PrimaryEnd))
                        else Brush.linearGradient(listOf(Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.3f)))
                    )
                ) {
                    Icon(Icons.Default.Send, "Send", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun AttachmentChip(attachment: Attachment, onRemove: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (attachment.isImage) Icons.Default.Image else Icons.Default.Description, null,
                modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(6.dp))
            Text(attachment.name, style = MaterialTheme.typography.labelMedium, maxLines = 1,
                overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 120.dp))
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onRemove, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(14.dp))
            }
        }
    }
}