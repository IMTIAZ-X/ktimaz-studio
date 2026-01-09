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

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            if (!settings.isProUser && attachedFiles.size >= 10) {
                return@let
            }
            attachedFiles.add(
                Attachment(
                    name = "image_${System.currentTimeMillis()}.jpg",
                    type = AttachmentType.IMAGE,
                    uri = it
                )
            )
        }
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            if (!settings.isProUser && attachedFiles.size >= 10) {
                return@let
            }
            attachedFiles.add(
                Attachment(
                    name = "file_${System.currentTimeMillis()}.txt",
                    type = AttachmentType.DOCUMENT,
                    uri = it
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        AnimatedVisibility(
            visible = attachedFiles.isNotEmpty(),
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                items(attachedFiles, key = { it.id }) { attachment ->
                    ModernAttachmentChip(
                        attachment = attachment,
                        onRemove = { attachedFiles.remove(attachment) }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = selectedMode != AiMode.STANDARD,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(14.dp)),
                color = Color(0xFF667EEA).copy(alpha = 0.15f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(selectedMode.icon, fontSize = 18.sp)
                    Column(Modifier.weight(1f)) {
                        Text(
                            "${selectedMode.title} Mode",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Text(
                            selectedMode.promptTag,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.setSelectedMode(AiMode.STANDARD) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Modern Input Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp)),
            color = Color(0xFF1A1A2E),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // File Menu Button
                    Box {
                        IconButton(
                            onClick = { showFileMenu = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.AttachFile,
                                null,
                                tint = Color(0xFF4ECDC4),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (showFileMenu) {
                            DropdownMenu(
                                expanded = showFileMenu,
                                onDismissRequest = { showFileMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Image,
                                                null,
                                                modifier = Modifier.size(20.dp),
                                                tint = Color(0xFF667EEA)
                                            )
                                            Text("Upload Image")
                                        }
                                    },
                                    onClick = {
                                        imagePicker.launch("image/*")
                                        showFileMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Description,
                                                null,
                                                modifier = Modifier.size(20.dp),
                                                tint = Color(0xFF764BA2)
                                            )
                                            Text("Upload File")
                                        }
                                    },
                                    onClick = {
                                        filePicker.launch("*/*")
                                        showFileMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Mode Menu Button
                    Box {
                        IconButton(
                            onClick = { showModeMenu = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Psychology,
                                null,
                                tint = Color(0xFF764BA2),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (showModeMenu) {
                            DropdownMenu(
                                expanded = showModeMenu,
                                onDismissRequest = { showModeMenu = false }
                            ) {
                                AiMode.values().forEach { mode ->
                                    val isLocked = mode.isPro && !settings.isProUser
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(mode.icon, fontSize = 16.sp)
                                                Column(Modifier.weight(1f)) {
                                                    Text(
                                                        mode.title,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    if (isLocked) {
                                                        Text(
                                                            "Pro Only",
                                                            fontSize = 11.sp,
                                                            color = Color(0xFFFF6B6B)
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        onClick = {
                                            if (!isLocked) {
                                                viewModel.setSelectedMode(mode)
                                                showModeMenu = false
                                            }
                                        },
                                        enabled = !isLocked
                                    )
                                }
                            }
                        }
                    }

                    // Text Input
                    TextField(
                        value = input,
                        onValueChange = {
                            onInputChange(it)
                            charCount = it.length
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp, max = 120.dp),
                        placeholder = {
                            Text(
                                "Type your message...",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 14.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        textStyle = androidx.compose.material3.LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            color = Color.White
                        ),
                        maxLines = 5
                    )

                    // Send Button
                    Button(
                        onClick = onSend,
                        enabled = input.isNotBlank() || attachedFiles.isNotEmpty(),
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (input.isNotBlank() || attachedFiles.isNotEmpty())
                                Color(0xFF667EEA) else Color.Gray.copy(alpha = 0.3f),
                            disabledContainerColor = Color.Gray.copy(alpha = 0.2f)
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            Icons.Default.Send,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Character Count
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$charCount characters",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    if (attachedFiles.isNotEmpty()) {
                        Text(
                            "${attachedFiles.size}/${if (settings.isProUser) "∞" else "10"} files",
                            fontSize = 11.sp,
                            color = Color(0xFF4ECDC4)
                        )
                    }
                }
            }
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