package com.ktimazstudio.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ktimazstudio.data.Screen
import com.ktimazstudio.manager.SoundEffectManager

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AppNavigationRail(
    selectedDestination: Screen,
    onDestinationSelected: (Screen) -> Unit,
    isExpanded: Boolean,
    onMenuClick: () -> Unit,
    soundEffectManager: SoundEffectManager
) {
    // Only show the rail on smaller screens or when explicitly requested
    AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn() + slideInHorizontally(),
        exit = fadeOut() + slideOutHorizontally()
    ) {
        NavigationRail(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                .fillMaxHeight(),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            header = {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        ) {
            Column(
                modifier = Modifier.fillMaxHeight().padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(64.dp)) // Offset for the header
                Screen.Dashboard.let {
                    NavigationRailItem(
                        selected = selectedDestination.route == it.route,
                        onClick = {
                            onDestinationSelected(it)
                            soundEffectManager.playClickSound()
                        },
                        icon = { Icon(imageVector = it.icon, contentDescription = it.label) },
                        label = { Text(it.label) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }
                Screen.AppSettings.let {
                    NavigationRailItem(
                        selected = selectedDestination.route == it.route,
                        onClick = {
                            onDestinationSelected(it)
                            soundEffectManager.playClickSound()
                        },
                        icon = { Icon(imageVector = it.icon, contentDescription = it.label) },
                        label = { Text(it.label) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }
                Screen.Profile.let {
                    NavigationRailItem(
                        selected = selectedDestination.route == it.route,
                        onClick = {
                            onDestinationSelected(it)
                            soundEffectManager.playClickSound()
                        },
                        icon = { Icon(imageVector = it.icon, contentDescription = it.label) },
                        label = { Text(it.label) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }
            }
        }
    }
}
