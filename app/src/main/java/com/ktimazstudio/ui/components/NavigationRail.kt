package com.ktimazstudio.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.ktimazstudio.enums.Screen
import com.ktimazstudio.managers.SoundEffectManager
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun AppNavigationRail(
    selectedDestination: Screen,
    onDestinationSelected: (Screen) -> Unit,
    isExpanded: Boolean,
    onMenuClick: () -> Unit,
    soundEffectManager: SoundEffectManager, // Pass sound manager
    shape = RoundedCornerShape(28.dp),
    modifier: Modifier = Modifier
) {
    val destinations = listOf(Screen.Dashboard, Screen.AppSettings, Screen.Profile)
    val railWidth by animateDpAsState(
        targetValue = if (isExpanded) 180.dp else 80.dp,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "nav_rail_width_anim"
    )
    val railContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp).copy(alpha = 0.95f)

    NavigationRail(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxHeight()
            .width(railWidth)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        containerColor = railContainerColor,
        header = {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.9f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "menu_icon_scale"
            )

            // MODIFIED: Pass interactionSource directly to IconButton
            IconButton(
                onClick = onMenuClick,
                interactionSource = interactionSource, // Pass interactionSource here
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale) // Apply press animation
            ) {
                AnimatedContent(
                    targetState = isExpanded,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(200, delayMillis = 150)) + scaleIn(initialScale = 0.8f, animationSpec = tween(200, delayMillis = 150)) togetherWith
                                fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.8f, animationSpec = tween(150))
                    }, label = "menu_icon_transition"
                ) { expanded ->
                    Icon(
                        imageVector = if (expanded) Icons.AutoMirrored.Filled.MenuOpen else Icons.Filled.Menu,
                        contentDescription = if (expanded) "Collapse Menu" else "Expand Menu",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) {
        Spacer(Modifier.weight(0.05f))
        destinations.forEach { screen ->
            val isSelected = selectedDestination == screen
            val iconScale by animateFloatAsState(
                targetValue = if (isSelected) 1.1f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                label = "nav_item_icon_scale_anim"
            )
            val indicatorColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else Color.Transparent
            val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

            NavigationRailItem(
                selected = isSelected,
                onClick = { onDestinationSelected(screen) },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.label,
                        modifier = Modifier.graphicsLayer(scaleX = iconScale, scaleY = iconScale)
                    )
                },
                label = {
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn(animationSpec = tween(200, delayMillis = 150)) + expandHorizontally(animationSpec = tween(300, delayMillis = 100), expandFrom = Alignment.Start),
                        exit = fadeOut(animationSpec = tween(150)) + shrinkHorizontally(animationSpec = tween(250), shrinkTowards = Alignment.Start)
                    ) { Text(screen.label, maxLines = 1, style = MaterialTheme.typography.labelMedium) }
                },
                alwaysShowLabel = isExpanded,
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = contentColor,
                    selectedTextColor = contentColor,
                    indicatorColor = indicatorColor,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(vertical = 6.dp).height(56.dp)
            )
            if (destinations.last() != screen) {
                Spacer(Modifier.height(6.dp))
            }
        }
        Spacer(Modifier.weight(1f))
    }
}