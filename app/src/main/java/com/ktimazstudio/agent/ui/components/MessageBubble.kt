package com.ktimazstudio.agent.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ktimazstudio.agent.data.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageBubble(msg: ChatMessage, isDarkTheme: Boolean) {
    val alignment = if (msg.isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (msg.isUser) {
        Brush.linearGradient(listOf(AppTheme.PrimaryStart, AppTheme.PrimaryEnd))
    } else {
        Brush.linearGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface))
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Row(
            horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            if (!msg.isUser) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFF093FB), Color(0xFFF5576C)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Psychology, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
            }

            Box(
                modifier = Modifier.widthIn(max = 600.dp)
                    .shadow(4.dp, RoundedCornerShape(20.dp, 20.dp, if (msg.isUser) 4.dp else 20.dp, if (msg.isUser) 20.dp else 4.dp))
                    .clip(RoundedCornerShape(20.dp, 20.dp, if (msg.isUser) 4.dp else 20.dp, if (msg.isUser) 20.dp else 4.dp))
                    .background(bubbleColor)
            ) {
                Column(Modifier.padding(16.dp)) {
                    if (msg.attachments.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            items(msg.attachments, key = { it.id }) { attachment ->
                                AttachmentPreview(attachment, isInMessage = true)
                            }
                        }
                    }

                    if (msg.isStreaming) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp,
                                color = if (msg.isUser) Color.White else MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Thinking...", color = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    } else {
                        Text(msg.text, color = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge)
                        
                        if (msg.usedApis.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                msg.usedApis.forEach { apiName ->
                                    Surface(
                                        color = if (msg.isUser) Color.White.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(apiName, style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            color = if (msg.isUser) Color.White.copy(alpha = 0.8f)
                                            else MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (msg.isUser) {
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(AppTheme.PrimaryStart, AppTheme.PrimaryEnd))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("A", color = Color.White, fontWeight = FontWeight.Black)
                }
            }
        }

        if (!msg.isStreaming) {
            Spacer(Modifier.height(4.dp))
            Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 52.dp))
        }
    }
}

@Composable
fun AttachmentPreview(attachment: Attachment, isInMessage: Boolean = false) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isInMessage) Color.White.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (attachment.isImage) Icons.Default.Image else Icons.Default.Description, null,
                modifier = Modifier.size(20.dp),
                tint = if (isInMessage) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(6.dp))
            Text(attachment.name, style = MaterialTheme.typography.labelSmall, maxLines = 1,
                overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 100.dp),
                color = if (isInMessage) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun ApiChip(api: ApiConfig, onRemove: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = api.provider.color.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(api.provider.color))
            Spacer(Modifier.width(6.dp))
            Text(api.name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onRemove, modifier = Modifier.size(16.dp)) {
                Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(12.dp))
            }
        }
    }
}