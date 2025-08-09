package com.ktimazstudio.ui.screens

import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.ktimazstudio.R
import com.ktimazstudio.enums.*
import com.ktimazstudio.managers.SoundEffectManager
import com.ktimazstudio.managers.EnhancedSharedPreferencesManager
import java.text.SimpleDateFormat
import java.util.*

// Data classes for enhanced cards
data class ModuleCard(
    val id: String,
    val title: String,
    val description: String,
    val icon: Int = R.mipmap.ic_launcher_round,
    val category: ModuleCategory,
    val status: ModuleStatus = ModuleStatus.READY,
    val progress: Float = 0f,
    val lastUsed: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val quickActions: List<QuickAction> = emptyList()
)

enum class ModuleCategory(val displayName: String, val color: Color) {
    SECURITY("Security", Color(0xFF673AB7)),
    MEDIA("Media", Color(0xFF2196F3)),
    TOOLS("Tools", Color(0xFF4CAF50)),
    NETWORK("Network", Color(0xFFFF9800)),
    AI("AI & ML", Color(0xFF9C27B0)),
    SYSTEM("System", Color(0xFF795548))
}

enum class ModuleStatus {
    READY,
    LOADING,
    ERROR,
    UPDATING,
    OFFLINE
}

data class QuickAction(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val action: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedDashboardScreen(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onCardClick: (String) -> Unit,
    soundEffectManager: SoundEffectManager,
    sharedPrefsManager: EnhancedSharedPreferencesManager
) {
    val haptic = LocalHapticFeedback.current
    
    // Get user preferences
    val viewType by remember { mutableStateOf(sharedPrefsManager.getDashboardViewType()) }
    val cardSize by remember { mutableStateOf(sharedPrefsManager.getDefaultCardSize()) }
    val hiddenModules = remember { mutableStateOf(sharedPrefsManager.getHiddenModules()) }
    val favoriteModules = remember { mutableStateOf(sharedPrefsManager.getFavoriteModules()) }
    val isReducedMotion = remember { mutableStateOf(sharedPrefsManager.isReducedMotionEnabled()) }

    // Enhanced module cards with real data
    val allCards = remember {
        listOf(
            ModuleCard(
                id = "spectrum_analyzer",
                title = "Spectrum Analyzer",
                description = "Real-time audio spectrum analysis",
                category = ModuleCategory.MEDIA,
                status = ModuleStatus.READY,
                quickActions = listOf(
                    QuickAction("Record", Icons.Filled.FiberManualRecord) { /* Record action */ },
                    QuickAction("Settings", Icons.Filled.Settings) { /* Settings action */ }
                )
            ),
            ModuleCard(
                id = "image_synthesizer",
                title = "Image Synthesizer",
                description = "AI-powered image generation",
                category = ModuleCategory.AI,
                status = ModuleStatus.UPDATING,
                progress = 0.65f
            ),
            ModuleCard(
                id = "holovid_player",
                title = "Holovid Player",
                description = "Immersive holographic video playback",
                category = ModuleCategory.MEDIA,
                status = ModuleStatus.READY
            ),
            ModuleCard(
                id = "neural_net_link",
                title = "Neural Net Link",
                description = "Direct neural network interface",
                category = ModuleCategory.AI,
                status = ModuleStatus.OFFLINE
            ),
            ModuleCard(
                id = "encrypted_notes",
                title = "Encrypted Notes",
                description = "Quantum-encrypted note storage",
                category = ModuleCategory.SECURITY,
                status = ModuleStatus.READY,
                quickActions = listOf(
                    QuickAction("New Note", Icons.Filled.Add) { /* New note action */ },
                    QuickAction("Search", Icons.Filled.Search) { /* Search action */ }
                )
            ),
            ModuleCard(
                id = "quantum_web",
                title = "Quantum Web",
                description = "Next-gen quantum internet browser",
                category = ModuleCategory.NETWORK,
                status = ModuleStatus.LOADING
            ),
            ModuleCard(
                id = "bio_scanner",
                title = "Bio Scanner",
                description = "Advanced biometric analysis",
                category = ModuleCategory.SECURITY,
                status = ModuleStatus.ERROR
            ),
            ModuleCard(
                id = "interface_designer",
                title = "Interface Designer",
                description = "Holographic UI design studio",
                category = ModuleCategory.TOOLS,
                status = ModuleStatus.READY
            ),
            ModuleCard(
                id = "sonic_emitter",
                title = "Sonic Emitter",
                description = "Directional sound wave generator",
                category = ModuleCategory.MEDIA,
                status = ModuleStatus.READY
            ),
            ModuleCard(
                id = "ai_core_access",
                title = "AI Core Access",
                description = "Central AI system control",
                category = ModuleCategory.AI,
                status = ModuleStatus.READY
            ),
            ModuleCard(
                id = "system_config",
                title = "System Config",
                description = "Advanced system configuration",
                category = ModuleCategory.SYSTEM,
                status = ModuleStatus.READY
            )
        )
    }

    // Filter cards based on search and user preferences
    val filteredCards = remember(allCards, searchQuery, hiddenModules.value, favoriteModules.value) {
        allCards.filter { card ->
            // Filter by search query
            val matchesSearch = if (searchQuery.isBlank()) true else {
                card.title.contains(searchQuery, ignoreCase = true) ||
                card.description.contains(searchQuery, ignoreCase = true) ||
                card.category.displayName.contains(searchQuery, ignoreCase = true)
            }
            
            // Filter hidden modules
            val notHidden = !hiddenModules.value.contains(card.id)
            
            matchesSearch && notHidden
        }.sortedWith(compareByDescending<ModuleCard> { favoriteModules.value.contains(it.id) }
            .thenByDescending { it.lastUsed })
    }

    // Separate favorites for special display
    val favoriteCards = filteredCards.filter { favoriteModules.value.contains(it.id) }
    val regularCards = filteredCards.filter { !favoriteModules.value.contains(it.id) }

    Column(modifier = modifier.fillMaxSize()) {
        // Quick Stats Header
        QuickStatsHeader(
            totalModules = allCards.size,
            activeModules = allCards.count { it.status == ModuleStatus.READY },
            favoriteCount = favoriteCards.size
        )

        // Favorites Section
        if (favoriteCards.isNotEmpty()) {
            Text(
                text = "⭐ Favorites",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favoriteCards) { card ->
                    CompactModuleCard(
                        card = card,
                        onClick = { onCardClick(card.title) },
                        onFavoriteToggle = {
                            sharedPrefsManager.toggleFavoriteModule(card.id)
                            favoriteModules.value = sharedPrefsManager.getFavoriteModules()
                        },
                        soundEffectManager = soundEffectManager,
                        isReducedMotion = isReducedMotion.value
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Recently Used Section
        val recentCards = regularCards.take(3)
        if (recentCards.isNotEmpty()) {
            Text(
                text = "🕒 Recently Used",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recentCards) { card ->
                    CompactModuleCard(
                        card = card,
                        onClick = { onCardClick(card.title) },
                        onFavoriteToggle = {
                            sharedPrefsManager.toggleFavoriteModule(card.id)
                            favoriteModules.value = sharedPrefsManager.getFavoriteModules()
                        },
                        soundEffectManager = soundEffectManager,
                        isReducedMotion = isReducedMotion.value
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        // All Modules Section
        Text(
            text = "📱 All Modules",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        // Main Cards Grid/List
        when (viewType) {
            DashboardViewType.GRID -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = getCardWidth(cardSize)),
                    contentPadding = PaddingValues(horizontal = 20.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(filteredCards, key = { _, card -> card.id }) { index, card ->
                        SmartModuleCard(
                            card = card,
                            index = index,
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onCardClick(card.title)
                            },
                            onFavoriteToggle = {
                                sharedPrefsManager.toggleFavoriteModule(card.id)
                                favoriteModules.value = sharedPrefsManager.getFavoriteModules()
                            },
                            soundEffectManager = soundEffectManager,
                            cardSize = cardSize,
                            isReducedMotion = isReducedMotion.value
                        )
                    }
                }
            }
            DashboardViewType.LIST -> {
                // List view implementation would go here
            }
            DashboardViewType.COMPACT_GRID -> {
                // Compact grid implementation would go here
            }
        }
    }
}

@Composable
fun QuickStatsHeader(
    totalModules: Int,
    activeModules: Int,
    favoriteCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = totalModules.toString(),
                label = "Total",
                icon = Icons.Filled.Apps
            )
            StatItem(
                value = activeModules.toString(),
                label = "Active",
                icon = Icons.Filled.CheckCircle,
                iconTint = Color(0xFF4CAF50)
            )
            StatItem(
                value = favoriteCount.toString(),
                label = "Favorites",
                icon = Icons.Filled.Star,
                iconTint = Color(0xFFFFC107)
            )
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartModuleCard(
    card: ModuleCard,
    index: Int,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    soundEffectManager: SoundEffectManager,
    cardSize: CardSize,
    isReducedMotion: Boolean
) {
    var itemVisible by remember { mutableStateOf(false) }
    var showQuickActions by remember { mutableStateOf(false) }
    
    LaunchedEffect(key1 = card.id) {
        delay(if (isReducedMotion) 50L else index * 70L + 100L)
        itemVisible = true
    }

    val animationSpec = if (isReducedMotion) {
        tween(100)
    } else {
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    }

    AnimatedVisibility(
        visible = itemVisible,
        enter = if (isReducedMotion) fadeIn(tween(100)) else {
            fadeIn(animationSpec = animationSpec) +
            slideInVertically(
                animationSpec = animationSpec,
                initialOffsetY = { it / 2 }
            ) +
            scaleIn(
                animationSpec = animationSpec,
                initialScale = 0.75f
            )
        }
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        
        val cardHeight = when (cardSize) {
            CardSize.SMALL -> 140.dp
            CardSize.MEDIUM -> 170.dp
            CardSize.LARGE -> 200.dp
        }

        Card(
            onClick = onClick,
            interactionSource = interactionSource,
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
            ),
            border = BorderStroke(
                width = 1.dp,
                color = card.category.color.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp,
                pressedElevation = 8.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .graphicsLayer {
                    scaleX = if (isPressed && !isReducedMotion) 0.98f else 1f
                    scaleY = if (isPressed && !isReducedMotion) 0.98f else 1f
                }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header with status and favorite
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Status indicator
                        StatusIndicator(status = card.status)
                        
                        // Favorite button
                        IconButton(
                            onClick = {
                                soundEffectManager.playClickSound()
                                onFavoriteToggle()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (card.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = "Toggle Favorite",
                                tint = if (card.isFavorite) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Content
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Icon with category color background
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(card.category.color.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = card.icon),
                                contentDescription = card.title,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = card.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        if (cardSize != CardSize.SMALL) {
                            Text(
                                text = card.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // Progress bar for updating modules
                    if (card.status == ModuleStatus.UPDATING) {
                        Column {
                            LinearProgressIndicator(
                                progress = card.progress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = card.category.color,
                                trackColor = card.category.color.copy(alpha = 0.3f)
                            )
                            Text(
                                text = "${(card.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // Category badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = card.category.color.copy(alpha = 0.2f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = card.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = card.category.color,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Quick Actions Overlay
                AnimatedVisibility(
                    visible = showQuickActions && card.quickActions.isNotEmpty(),
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            card.quickActions.forEach { action ->
                                IconButton(
                                    onClick = {
                                        soundEffectManager.playClickSound()
                                        action.action()
                                        showQuickActions = false
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = action.icon,
                                        contentDescription = action.label,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactModuleCard(
    card: ModuleCard,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    soundEffectManager: SoundEffectManager,
    isReducedMotion: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(120.dp)
            .height(100.dp)
            .graphicsLayer {
                scaleX = if (isPressed && !isReducedMotion) 0.95f else 1f
                scaleY = if (isPressed && !isReducedMotion) 0.95f else 1f
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Status and favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusIndicator(status = card.status, size = 8.dp)
                if (card.isFavorite) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Favorite",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(card.category.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = card.icon),
                    contentDescription = card.title,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Title
            Text(
                text = card.title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StatusIndicator(
    status: ModuleStatus,
    size: androidx.compose.ui.unit.Dp = 12.dp
) {
    val (color, isAnimated) = when (status) {
        ModuleStatus.READY -> Color(0xFF4CAF50) to false
        ModuleStatus.LOADING -> Color(0xFF2196F3) to true
        ModuleStatus.ERROR -> Color(0xFFF44336) to false
        ModuleStatus.UPDATING -> Color(0xFFFF9800) to true
        ModuleStatus.OFFLINE -> Color(0xFF9E9E9E) to false
    }

    val alpha by if (isAnimated) {
        rememberInfiniteTransition(label = "status_indicator").animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "status_alpha"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}

// Utility functions
private fun getCardWidth(cardSize: CardSize): androidx.compose.ui.unit.Dp {
    return when (cardSize) {
        CardSize.SMALL -> 140.dp
        CardSize.MEDIUM -> 160.dp
        CardSize.LARGE -> 180.dp
    }
}