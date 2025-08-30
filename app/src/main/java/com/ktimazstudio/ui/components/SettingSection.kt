package com.ktimazstudio.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ktimazstudio.managers.SoundEffectManager

/**
 * Enhanced setting section with header and grouped items
 */
@Composable
fun SettingSection(
    title: String, 
    icon: ImageVector, 
    modifier: Modifier = Modifier, 
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Section Content
            content()
        }
    }
}

/**
 * Dropdown setting item for enum selections
 */
@Composable
fun <T : Enum<T>> SettingDropdown(
    title: String,
    description: String,
    leadingIcon: @Composable () -> Unit,
    currentValue: T,
    options: List<T>,
    onValueChange: (T) -> Unit,
    soundEffectManager: SoundEffectManager,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    SettingItem(
        title = title,
        description = description,
        leadingIcon = leadingIcon,
        onClick = {
            expanded = true
        },
        control = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatEnumName(currentValue.name),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Expand options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        soundEffectManager = soundEffectManager
    )
    
    // Dropdown Menu
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceContainer,
                RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
    ) {
        options.forEach { option ->
            DropdownMenuItem(
                text = { 
                    Text(
                        formatEnumName(option.name),
                        style = MaterialTheme.typography.bodyMedium
                    ) 
                },
                onClick = {
                    soundEffectManager.playClickSound()
                    onValueChange(option)
                    expanded = false
                },
                colors = MenuDefaults.itemColors(
                    textColor = if (option == currentValue) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

/**
 * Enhanced toggle setting with custom styling
 */
@Composable
fun SettingToggle(
    title: String,
    description: String,
    leadingIcon: @Composable () -> Unit,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    soundEffectManager: SoundEffectManager,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    SettingItem(
        title = title,
        description = description,
        leadingIcon = leadingIcon,
        onClick = if (enabled) {
            {
                onCheckedChange(!checked)
            }
        } else null,
        control = {
            Switch(
                checked = checked,
                onCheckedChange = { newValue ->
                    if (enabled) {
                        soundEffectManager.playClickSound()
                        onCheckedChange(newValue)
                    }
                },
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        soundEffectManager = if (enabled) soundEffectManager else null
    )
}

/**
 * Slider setting for numeric ranges
 */
@Composable
fun SettingSlider(
    title: String,
    description: String,
    leadingIcon: @Composable () -> Unit,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    soundEffectManager: SoundEffectManager,
    modifier: Modifier = Modifier,
    valueFormatter: (Float) -> String = { "%.1f".format(it) }
) {
    SettingItem(
        title = title,
        description = "$description • ${valueFormatter(value)}",
        leadingIcon = leadingIcon,
        control = {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = steps,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.width(120.dp)
            )
        },
        soundEffectManager = soundEffectManager,
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Action setting item with arrow indicator
 */
@Composable
fun SettingAction(
    title: String,
    description: String,
    leadingIcon: @Composable () -> Unit,
    onClick: () -> Unit,
    soundEffectManager: SoundEffectManager,
    modifier: Modifier = Modifier,
    showArrow: Boolean = true
) {
    SettingItem(
        title = title,
        description = description,
        leadingIcon = leadingIcon,
        onClick = onClick,
        control = if (showArrow) {
            {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Go to $title",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else null,
        soundEffectManager = soundEffectManager
    )
}

/**
 * Information-only setting item
 */
@Composable
fun SettingInfo(
    title: String,
    description: String,
    leadingIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    value: String? = null
) {
    SettingItem(
        title = title,
        description = description,
        leadingIcon = leadingIcon,
        control = value?.let { 
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

/**
 * Utility function to format enum names for display
 */
private fun formatEnumName(name: String): String {
    return name.replace("_", " ")
        .lowercase()
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }
}

/**
 * Animated setting divider
 */
@Composable
fun SettingDivider(
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 0.3f else 0f,
        animationSpec = tween(300),
        label = "divider_alpha"
    )
    
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = alpha),
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

/**
 * Setting group with collapsible content
 */
@Composable
fun SettingGroup(
    title: String,
    icon: ImageVector,
    initiallyExpanded: Boolean = true,
    soundEffectManager: SoundEffectManager,
    settingModifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(300),
        label = "expand_rotation"
    )
    
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f)
        ),
        modifier = settingModifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column {
            // Header
            SettingItem(
                title = title,
                description = if (expanded) "Tap to collapse" else "Tap to expand",
                leadingIcon = { 
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    ) 
                },
                onClick = {
                    expanded = !expanded
                },
                control = {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer {
                                rotationZ = rotationAngle
                            }
                    )
                },
                soundEffectManager = soundEffectManager
            )
            // Content
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
                ) {
                    content()
                }
            }
        }
    }
}