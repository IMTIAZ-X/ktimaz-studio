package com.ktimazstudio.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.ktimazstudio.ComingActivity
import com.ktimazstudio.SettingsActivity
import com.ktimazstudio.enums.*
import com.ktimazstudio.managers.SoundEffectManager
import com.ktimazstudio.managers.SharedPreferencesManager

data class ModuleItem(
    val title: String,
    val description: String,
    val category: ModuleCategory,
    val isPremium: Boolean = false,
    val isNew: Boolean = false,
    val progress: Float? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedDashboardScreen(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onCardClick: (String) -> Unit,
    soundEffectManager: SoundEffectManager,
    sharedPrefsManager: SharedPreferencesManager
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    // Get user preferences
    val dashboardViewType = remember { mutableStateOf(sharedPrefsManager.getDashboardViewType()) }
    val cardSize = remember { mutableStateOf(sharedPrefsManager.getCardSize()) }
    val layoutDensity = remember { mutableStateOf(sharedPrefsManager.getLayoutDensity()) }
    
    // Enhanced module data
    val modules = remember {
        listOf(
            ModuleItem("Spectrum Analyzer", "Advanced audio visualization", ModuleCategory.MEDIA, isPremium = true),
            ModuleItem("Neural Network", "AI processing hub", ModuleCategory.ANALYTICS, isNew = true),
            ModuleItem("Quantum Encryptor", "Military-grade security", ModuleCategory.SECURITY, isPremium = true),
            ModuleItem("Holovid Player", "3D media experience", ModuleCategory.MEDIA),
            ModuleItem("Bio Scanner", "Biometric analysis", ModuleCategory.SECURITY, progress = 0.75f),
            ModuleItem("Code Generator", "Development tools", ModuleCategory.TOOLS),
            ModuleItem("System Monitor", "Performance tracking", ModuleCategory.SYSTEM, progress = 0.45f),
            ModuleItem("Creative Suite", "Design & editing tools", ModuleCategory.CREATIVE, isPremium = true),
            ModuleItem("Network Probe", "Connection analysis", ModuleCategory.NETWORK),
            ModuleItem("Data Visualizer", "Chart & graph creator", ModuleCategory.ANALYTICS, isNew = true),
            ModuleItem("Game Center", "Entertainment hub", ModuleCategory.ENTERTAINMENT),
            ModuleItem("Productivity Kit", "Work enhancement tools", ModuleCategory.PRODUCTIVITY)
        )
    }
    
    // Filter modules based on search
    val filteredModules = remember(modules, searchQuery) {
        if (searchQuery.isBlank()) {
            modules
        } else {
            modules.filter { 
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true) ||
                it.category.displayName.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    // Group modules by category
    val groupedModules = remember(filteredModules) {
        filteredModules.groupBy { it.category }
    }
    
    // Selected category filter
    var selectedCategory by remember { mutableStateOf<ModuleCategory?>(null) }
    
    val displayModules = if (selectedCategory != null) {
        filteredModules.filter { it.category == selectedCategory }
    } else {
        filteredModules
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Category Filter Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    onClick = {
                        soundEffectManager.playClickSound()
                        selectedCategory = null
                    },
                    label = { Text("All") },
                    selected = selectedCategory == null,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Apps,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
            
            items(ModuleCategory.values().filter { category ->
                groupedModules.containsKey(category)
            }) { category ->
                FilterChip(
                    onClick = {
                        soundEffectManager.playClickSound()
                        selectedCategory = if (selectedCategory == category) null else category
                    },
                    label = { Text(category.displayName) },
                    selected = selectedCategory == category,
                    leadingIcon = {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }

        // Dashboard Content based on view type
        when (dashboardViewType.value) {
            DashboardViewType.GRID -> {
                ModernGridLayout(
                    modules = displayModules,
                    cardSize = cardSize.value,
                    layoutDensity = layoutDensity.value,
                    onCardClick = { module ->
                        handleModuleClick(module, context, soundEffectManager, haptic)
                    }
                )
            }
            DashboardViewType.LIST -> {
                ModernListLayout(
                    modules = displayModules,
                    layoutDensity = layoutDensity.value,
                    onCardClick = { module ->
                        handleModuleClick(module, context, soundEffectManager, haptic)
                    }
                )
            }
            DashboardViewType.CARD_CAROUSEL -> {
                CardCarouselLayout(
                    modules = displayModules,
                    cardSize = cardSize.value,
                    onCardClick = { module ->
                        handleModuleClick(module, context, soundEffectManager, haptic)
                    }
                )
            }
            DashboardViewType.MASONRY -> {
                MasonryLayout(
                    modules = displayModules,
                    onCardClick = { module ->
                        handleModuleClick(module, context, soundEffectManager, haptic)
                    }
                )
            }
        }
    }
}

@Composable
private fun ModernGridLayout(
    modules: List<ModuleItem>,
    cardSize: CardSize,
    layoutDensity: LayoutDensity,
    onCardClick: (ModuleItem) -> Unit
) {
    val spacing = when (layoutDensity) {
        LayoutDensity.COMPACT -> 8.dp
        LayoutDensity.MEDIUM -> 12.dp
        LayoutDensity.COMFORTABLE -> 16.dp
        LayoutDensity.SPACIOUS -> 24.dp
    }
    
    val columns = when (cardSize) {
        CardSize.SMALL -> GridCells.Adaptive(minSize = 140.dp)
        CardSize.MEDIUM -> GridCells.Adaptive(minSize = 160.dp)
        CardSize.LARGE -> GridCells.Adaptive(minSize = 200.dp)
        CardSize.EXTRA_LARGE -> GridCells.Adaptive(minSize = 240.dp)
    }

    LazyVerticalGrid(
        columns = columns,
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(modules, key = { _, module -> module.title }) { index, module ->
            var itemVisible by remember { mutableStateOf(false) }
            
            LaunchedEffect(key1 = module.title) {
                delay(index * 50L + 100L)
                itemVisible = true
            }

            AnimatedVisibility(
                visible = itemVisible,
                enter = fadeIn(animationSpec = spring(dampingRatio = 0.7f)) +
                        slideInVertically(
                            animationSpec = spring(dampingRatio = 0.8f),
                            initialOffsetY = { it / 3 }
                        ) +
                        scaleIn(
                            animationSpec = spring(dampingRatio = 0.7f),
                            initialScale = 0.8f
                        )
            ) {
                EnhancedModuleCard(
                    module = module,
                    cardSize = cardSize,
                    onClick = { onCardClick(module) }
                )
            }
        }
    }
}

@Composable
private fun ModernListLayout(
    modules: List<ModuleItem>,
    layoutDensity: LayoutDensity,
    onCardClick: (ModuleItem) -> Unit
) {
    val spacing = when (layoutDensity) {
        LayoutDensity.COMPACT -> 4.dp
        LayoutDensity.MEDIUM -> 8.dp
        LayoutDensity.COMFORTABLE -> 12.dp
        LayoutDensity.SPACIOUS -> 16.dp
    }

    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(spacing),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(modules, key = { _, module -> module.title }) { index, module ->
            var itemVisible by remember { mutableStateOf(false) }
            
            LaunchedEffect(key1 = module.title) {
                delay(index * 30L + 100L)
                itemVisible = true
            }

            AnimatedVisibility(
                visible = itemVisible,
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { it / 2 })
            ) {
                ModuleListItem(
                    module = module,
                    onClick = { onCardClick(module) }
                )
            }
        }
    }
}

@Composable
private fun CardCarouselLayout(
    modules: List<ModuleItem>,
    cardSize: CardSize,
    onCardClick: (ModuleItem) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(modules, key = { it.title }) { module ->
            EnhancedModuleCard(
                module = module,
                cardSize = cardSize,
                onClick = { onCardClick(module) },
                modifier = Modifier.width(cardSize.dpSize.dp)
            )
        }
    }
}

@Composable
private fun MasonryLayout(
    modules: List<ModuleItem>,
    onCardClick: (ModuleItem) -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(8.dp),
        verticalItemSpacing = 12.dp,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(modules, key = { it.title }) { module ->
            val randomHeight = remember { (120..200).random() }
            
            EnhancedModuleCard(
                module = module,
                cardSize = CardSize.MEDIUM,
                onClick = { onCardClick(module) },
                modifier = Modifier.height(randomHeight.dp)
            )
        }
    }
}

@Composable
private fun EnhancedModuleCard(
    module: ModuleItem,
    cardSize: CardSize,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "card_scale"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (pressed) 8.dp else 4.dp,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "card_elevation"
    )

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        modifier = modifier
            .fillMaxWidth()
            .height(cardSize.dpSize.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            module.category.icon.defaultTint.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header with category icon and badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = module.category.icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (module.isPremium) {
                            AssistChip(
                                onClick = { },
                                label = { Text("Pro", fontSize = 10.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.height(20.dp)
                            )
                        }
                        if (module.isNew) {
                            AssistChip(
                                onClick = { },
                                label = { Text("New", fontSize = 10.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                ),
                                modifier = Modifier.height(20.dp)
                            )
                        }
                    }
                }
                
                // Content
                Column {
                    Text(
                        text = module.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = module.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                    
                    // Progress indicator if available
                    module.progress?.let { progress ->
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                }
            }
            
            // Floating action indicator
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Launch",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun ModuleListItem(
    module: ModuleItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    
    val alpha by animateFloatAsState(
        targetValue = if (pressed) 0.8f else 1f,
        animationSpec = tween(150),
        label = "list_item_alpha"
    )

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = module.category.icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = module.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (module.isPremium) {
                            AssistChip(
                                onClick = { },
                                label = { Text("Pro", fontSize = 10.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.height(20.dp)
                            )
                        }
                        if (module.isNew) {
                            AssistChip(
                                onClick = { },
                                label = { Text("New", fontSize = 10.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                ),
                                modifier = Modifier.height(20.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = module.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Progress indicator
                module.progress?.let { progress ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Action icon
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Open",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun handleModuleClick(
    module: ModuleItem,
    context: android.content.Context,
    soundEffectManager: SoundEffectManager,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    soundEffectManager.playClickSound()
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    
    when (module.title) {
        "System Monitor" -> {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }
        else -> {
            context.startActivity(
                Intent(context, ComingActivity::class.java).putExtra("CARD_TITLE", module.title)
            )
        }
    }
}

// Extension property for getting default tint color from category
private val androidx.compose.ui.graphics.vector.ImageVector.defaultTint: androidx.compose.ui.graphics.Color
    @Composable get() = MaterialTheme.colorScheme.primary