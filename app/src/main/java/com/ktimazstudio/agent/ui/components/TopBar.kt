package com.ktimazstudio.agent.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ktimazstudio.agent.data.AppTheme
import com.ktimazstudio.agent.viewmodel.AgentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(viewModel: AgentViewModel) {
    val settings by viewModel.settings.collectAsState()
    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "pulse"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (settings.isDarkTheme) {
            AppTheme.CardDark.copy(alpha = 0.95f)
        } else {
            Color.White.copy(alpha = 0.95f)
        },
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.toggleSidebar() }) {
                Icon(
                    Icons.Default.Menu,
                    "Menu",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    AppTheme.APP_NAME,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = pulseAlpha))
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${viewModel.activeApiCount} APIs • ${settings.tokenUsage}T • \$${
                            String.format(
                                "%.4f",
                                settings.estimatedCost
                            )
                        }",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            if (settings.isProUser) {
                ProBadge()
                Spacer(Modifier.width(8.dp))
            }

            IconButton(onClick = { viewModel.openSettings() }) {
                Icon(
                    Icons.Default.AccountCircle,
                    "Profile",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ProBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(AppTheme.ProStart, AppTheme.ProEnd)))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Star,
                null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "PRO",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
            )
        }
    }
}