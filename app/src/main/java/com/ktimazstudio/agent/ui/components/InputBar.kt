package com.ktimazstudio.agent.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ktimazstudio.agent.data.*
import com.ktimazstudio.agent.viewmodel.AgentViewModel
import com.ktimazstudio.agent.ui.settings.createButtonColors // Added
import com.ktimazstudio.agent.ui.settings.ButtonColorData // Added

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
    var showFileMenu by remember { mutableStateOf(false) }
    var showModeMenu by remember { mutableStateOf(false) }
    var charCount by remember { mutableStateOf(0) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                attachedFiles.add(Attachment(it.toString(), "Image", true))
            }
        }
    )
    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                attachedFiles.add(Attachment(it.toString(), "Document", false))
            }
        }
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp),
        color = Color(0xFF1A1A2E)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Attached Files/Modes Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attached Files Chips
                AnimatedVisibility(
                    visible = attachedFiles.isNotEmpty(),
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(attachedFiles) { attachment ->
                            ModernAttachmentChip(
                                attachment = attachment,
                                onRemove = { attachedFiles.remove(attachment) }
                            )
                        }
                    }
                }

                // Input Modes (AI Mode Selection)
                AnimatedVisibility(
                    visible = attachedFiles.isEmpty(),
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Mode:",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showModeMenu = true },
                                color = Color(0xFF2A2A4A),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    selectedMode.displayName,
                                    color = Color(0xFF4ECDC4),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                        // Mode Dropdown (Assuming it uses standard material components)
                        DropdownMenu(
                            expanded = showModeMenu,
                            onDismissRequest = { showModeMenu = false }
                        ) {
                            AiMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode.displayName) },
                                    onClick = {
                                        viewModel.updateSettings(settings.copy(selectedMode = mode))
                                        showModeMenu = false
                                    }
                                )
                            }
                        }
                    }
                }


                // Character Count
                Text(
                    "${input.length}/${selectedMode.maxInputLength}",
                    fontSize = 12.sp,
                    color = if (input.length > selectedMode.maxInputLength) Color.Red else Color.Gray,
                    modifier = Modifier.padding(start = 8.dp)
                )

                // Attachments Button
                Box {
                    Button(
                        onClick = { showFileMenu = true },
                        shape = CircleShape,
                        modifier = Modifier.size(32.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = createButtonColors( // Fixed L325 Argument type mismatch
                            ButtonColorData(
                                containerColor = Color(0xFF2A2A4A),
                                contentColor = Color(0xFF4ECDC4),
                                disabledContainerColor = Color.Gray.copy(alpha = 0.5f),
                                disabledContentColor = Color.DarkGray
                            )
                        )
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Attach File", modifier = Modifier.size(16.dp))
                    }

                    // File Dropdown
                    DropdownMenu(
                        expanded = showFileMenu,
                        onDismissRequest = { showFileMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Image (Max 1)") },
                            onClick = {
                                attachedFiles.removeAll { it.isImage }
                                imagePicker.launch("image/*")
                                showFileMenu = false
                            },
                            enabled = attachedFiles.count { it.isImage } < 1
                        )
                        DropdownMenuItem(
                            text = { Text("Document (Max 5)") },
                            onClick = {
                                documentPicker.launch(arrayOf("application/pdf", "text/plain"))
                                showFileMenu = false
                            },
                            enabled = attachedFiles.count { !it.isImage } < 5
                        )
                    }
                }
            }


            // Text Input
            TextField(
                value = input,
                onValueChange = { onInputChange(it) },
                placeholder = { Text("Type a message...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp, max = 200.dp)
                    .clip(RoundedCornerShape(24.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF2A2A4A),
                    unfocusedContainerColor = Color(0xFF2A2A4A),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                trailingIcon = {
                    Button(
                        onClick = onSend,
                        enabled = input.isNotBlank(),
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF667EEA)
                        )
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun ModernAttachmentChip(attachment: Attachment, onRemove: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { onRemove() })
            },
        color = Color(0xFF667EEA).copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                if (attachment.isImage) Icons.Default.Image else Icons.Default.Description,
                null,
                tint = Color(0xFF4ECDC4),
                modifier = Modifier.size(16.dp)
            )
            Text(
                attachment.name.take(15) + if (attachment.name.length > 15) "..." else "",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Icon(
                Icons.Default.Close,
                null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(14.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onRemove() })
                    }
            )
        }
    }
}