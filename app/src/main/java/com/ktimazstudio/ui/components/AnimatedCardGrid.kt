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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ktimazstudio.R
import com.ktimazstudio.manager.SoundEffectManager

/**
 * Data class for each card's content.
 */
data class DashboardCard(
    val title: String,
    val icon: Painter,
    val description: String? = null
)

private val dashboardCards = listOf(
    DashboardCard("Dashboard", painterResource(R.drawable.dashboard_icon)),
    DashboardCard("System Config", painterResource(R.drawable.system_config_icon)),
    DashboardCard("Logs", painterResource(R.drawable.logs_icon)),
    DashboardCard("Backup", painterResource(R.drawable.backup_icon)),
    DashboardCard("Security", painterResource(R.drawable.security_icon)),
    DashboardCard("Reporting", painterResource(R.drawable.reporting_icon)),
    DashboardCard("Automation", painterResource(R.drawable.automation_icon)),
    DashboardCard("Notifications", painterResource(R.drawable.notifications_icon)),
    DashboardCard("User Management", painterResource(R.drawable.user_management_icon))
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedCardGrid(
    searchQuery: String,
    onCardClick: (String) -> Unit,
    soundEffectManager: SoundEffectManager
) {
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
                Image(
                    painter = card.icon,
                    contentDescription = card.title,
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.Fit
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
