package com.ktimazstudio.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ktimazstudio.R
import com.ktimazstudio.manager.SoundEffectManager
import kotlinx.coroutines.delay

/**
 * Data class for each card's content.
 */
data class DashboardCard(
    val title: String,
    val icon: ImageVector,
    val description: String? = null
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedCardGrid(
    searchQuery: String,
    onCardClick: (String) -> Unit,
    soundEffectManager: SoundEffectManager
) {
    // This list is initialized inside the composable to use remember and painterResource
    val dashboardCards = remember {
        listOf(
            DashboardCard("Dashboard", Icons.Default.Dashboard),
            DashboardCard("System Config", Icons.Default.Build),
            DashboardCard("Logs", Icons.Default.Info),
            DashboardCard("Backup", Icons.Default.Star),
            DashboardCard("Security", Icons.Default.Lock),
            DashboardCard("Reporting", Icons.Default.Report),
            DashboardCard("Automation", Icons.Default.Settings),
            DashboardCard("Notifications", Icons.Default.Notifications),
            DashboardCard("User Management", Icons.Default.Person)
        )
    }
    
    val filteredCards = remember(searchQuery) {
        dashboardCards.filter {
            it.title.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 180.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(filteredCards) { index, card ->
            AnimatedCardItem(
                card = card,
                onCardClick = onCardClick,
                soundEffectManager = soundEffectManager,
                animationDelay = index * 50
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedCardItem(
    card: DashboardCard,
    onCardClick: (String) -> Unit,
    soundEffectManager: SoundEffectManager,
    animationDelay: Int
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)) +
                scaleIn(initialScale = 0.8f, animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)),
        exit = fadeOut(animationSpec = tween(durationMillis = 150))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(20.dp))
                .clickable {
                    onCardClick(card.title)
                    soundEffectManager.playClickSound()
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = card.icon,
                    contentDescription = card.title,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                if (card.description != null) {
                    Text(
                        text = card.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
