package com.ktimazstudio.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.ktimazstudio.managers.SoundEffectManager

/**
 * Reusable composable for a setting item.
 * Includes click animation and sound if onClick is provided.
 */
@Composable
fun SettingItem(
    title: String,
    description: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    control: @Composable (() -> Unit)? = null,
    soundEffectManager: SoundEffectManager? = null // Optional sound manager
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "setting_item_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.8f else 1.0f,
        animationSpec = tween(150),
        label = "setting_item_alpha"
    )

    val defaultIndication = LocalIndication.current // Get the default Material indication

    val itemModifier = Modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha) // Apply press animation
            .clickable(
                interactionSource = interactionSource,
                indication = defaultIndication, // Explicitly pass the default indication
                onClick = {
                    soundEffectManager?.playClickSound() // Play sound if manager provided
                    onClick()
                }
            ) else Modifier)
        .padding(vertical = 16.dp, horizontal = 8.dp)

    Row(
        modifier = itemModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Box(modifier = Modifier.padding(end = 16.dp).size(24.dp), contentAlignment = Alignment.Center) {
                leadingIcon()
            }
        }
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            if (description != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (control != null) {
            Box(modifier = Modifier.padding(start = 8.dp)) {
                control()
            }
        }
    }
}