package com.ktimazstudio.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.ktimazstudio.enums.Screen
import com.ktimazstudio.enums.NavigationStyle
import com.ktimazstudio.managers.SoundEffectManager
import com.ktimazstudio.managers.EnhancedSharedPreferencesManager

@Composable
fun AdaptiveNavigation(
    selectedDestination: Screen,
    onDestinationSelected: (Screen) -> Unit,
    soundEffectManager: SoundEffectManager,
    sharedPrefsManager: EnhancedSharedPreferencesManager,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isTablet = screenWidth >= 600 // Tablet breakpoint
    
    val userNavigationStyle = sharedPrefsManager.getNavigationStyle()
    
    // Determine actual navigation style based on user preference and screen size
    val actualNavigationStyle = when (userNavigationStyle) {
        NavigationStyle.AUTO -> {
            when {
                screenWidth >= 840 -> NavigationStyle.PERSISTENT_DRAWER  // Large tablets/desktop
                screenWidth >= 600 -> NavigationStyle.NAVIGATION_RAIL    // Small tablets
                else -> NavigationStyle.BOTTOM_BAR                       // Phones
            }
        }
        else -> userNavigationStyle
    }

    when (actualNavigationStyle) {
        NavigationStyle.BOTTOM_BAR -> {
            BottomNavigationBar(
                selectedDestination = selectedDestination,
                onDestinationSelected = onDestinationSelected,
                soundEffectManager = soundEffectManager,
                modifier = modifier
            )
        }
        NavigationStyle.NAVIGATION_RAIL -> {
            var isExpanded by remember { mutableStateOf(false) }
            NavigationRailContainer(
                selectedDestination = selectedDestination,
                onDestinationSelected = onDestinationSelected,
                isExpanded = isExpanded,
                onMenuClick = { 
                    soundEffectManager.playClickSound()
                    isExpanded = !isExpanded 
                },
                soundEffectManager = soundEffectManager,
                modifier = modifier
            )
        }
        NavigationStyle.PERSISTENT_DRAWER -> {
            PersistentDrawerNavigation(
                selectedDestination = selectedDestination,
                onDestinationSelected = onDestinationSelected,
                soundEffectManager = soundEffectManager,
                modifier = modifier
            )
        }
        NavigationStyle.AUTO -> {
            // This case is handled above, but kept for completeness
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedDestination: Screen,
    onDestinationSelected: (Screen) -> Unit,
    soundEffectManager: SoundEffectManager,
    modifier: Modifier = Modifier
) {
    val destinations = listOf(Screen.Dashboard, Screen.AppSettings, Screen.Profile)

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        tonalElevation = 3.dp
    ) {
        destinations.forEach { screen ->
            val isSelected = selectedDestination == screen
            
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    soundEffectManager.playClickSound()
                    onDestinationSelected(screen)
                },
                icon = {
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.1f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "bottom_nav_icon_scale"
                    )
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.label,
                        modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
                    )
                },
                label = { 
                    Text(
                        text = screen.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun NavigationRailContainer(
    selectedDestination: Screen,
    onDestinationSelected: (Screen) -> Unit,
    isExpanded: Boolean,
    onMenuClick: () -> Unit,
    soundEffectManager: SoundEffectManager,
    modifier: Modifier = Modifier
) {
    val destinations = listOf(Screen.Dashboard, Screen.AppSettings, Screen.Profile)
    val railWidth by animateDpAsState(
        targetValue = if (isExpanded) 200.dp else 80.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "nav_rail_width"
    )

    NavigationRail(
        modifier = modifier
            .fillMaxHeight()
            .width(railWidth)
            .statusBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
        header = {
            NavigationRailHeader(
                isExpanded = isExpanded,
                onMenuClick = onMenuClick
            )
        }
    ) {
        Spacer(Modifier.weight(0.1f))
        
        destinations.forEach { screen ->
            val isSelected = selectedDestination == screen
            
            NavigationRailItem(
                selected = isSelected,
                onClick = {
                    soundEffectManager.playClickSound()
                    onDestinationSelected(screen)
                },
                icon = {
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.1f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "rail_icon_scale"
                    )
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.label,
                        modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
                    )
                },
                label = {
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn(tween(200, delayMillis = 100)) + 
                               expandHorizontally(tween(300), expandFrom = Alignment.Start),
                        exit = fadeOut(tween(150)) + 
                              shrinkHorizontally(tween(250), shrinkTowards = Alignment.Start)
                    ) {
                        Text(
                            text = screen.label,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                    }
                },
                alwaysShowLabel = isExpanded,
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .height(56.dp)
            )
        }
        
        Spacer(Modifier.weight(1f))
    }
}

@Composable
fun NavigationRailHeader(
    isExpanded: Boolean,
    onMenuClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "menu_button_scale"
    )

    IconButton(
        onClick = onMenuClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .padding(bottom = 16.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
    ) {
        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = {
                fadeIn(tween(200, delayMillis = 100)) + 
                scaleIn(initialScale = 0.8f, animationSpec = tween(200, delayMillis = 100)) togetherWith
                fadeOut(tween(150)) + 
                scaleOut(targetScale = 0.8f, animationSpec = tween(150))
            },
            label = "menu_icon_transition"
        ) { expanded ->
            Icon(
                imageVector = if (expanded) Icons.AutoMirrored.Filled.MenuOpen else Icons.Filled.Menu,
                contentDescription = if (expanded) "Collapse Menu" else "Expand Menu",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersistentDrawerNavigation(
    selectedDestination: Screen,
    onDestinationSelected: (Screen) -> Unit,
    soundEffectManager: SoundEffectManager,
    modifier: Modifier = Modifier
) {
    val destinations = listOf(Screen.Dashboard, Screen.AppSettings, Screen.Profile)

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // Header Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "App Logo",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Navigation",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Navigation Items
            destinations.forEach { screen ->
                val isSelected = selectedDestination == screen
                
                NavigationDrawerItem(
                    label = { 
                        Text(
                            text = screen.label,
                            style = MaterialTheme.typography.titleSmall
                        )
                    },
                    selected = isSelected,
                    onClick = {
                        soundEffectManager.playClickSound()
                        onDestinationSelected(screen)
                    },
                    icon = {
                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.1f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "drawer_icon_scale"
                        )
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.label,
                            modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
                        )
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Footer information
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            Text(
                text = "Persistent Navigation",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}