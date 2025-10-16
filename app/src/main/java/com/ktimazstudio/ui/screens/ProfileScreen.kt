package com.ktimazstudio.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ktimazstudio.managers.SoundEffectManager

/**
 * Composable for the enhanced Profile Screen.
 * @param username The username to display.
 * @param onLogout Callback invoked when the logout button is clicked.
 * @param soundEffectManager Manager for playing sound effects.
 */
@Composable
fun ProfileScreen(modifier: Modifier = Modifier, username: String, onLogout: () -> Unit, soundEffectManager: SoundEffectManager) {
    val profileBackgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(profileBackgroundGradient)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Profile Picture / Icon
        Box(
            modifier = Modifier
                .size(160.dp) // Larger profile picture area
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.surfaceVariant
                        ),
                        radius = 120f
                    )
                )
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape), // <-- FIXED HERE: 'border' modifier
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Profile Picture",
                modifier = Modifier.size(100.dp), // Icon size within the circle
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Username
        Text(
            text = username.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Welcome Message
        Text(
            text = "Welcome to your personalized space!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(48.dp))

        // Profile Options (Placeholder)
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .widthIn(max = 500.dp)
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                ProfileOptionItem(
                    icon = Icons.Filled.AccountBox,
                    title = "Edit Profile",
                    description = "Update your personal information.",
                    soundEffectManager = soundEffectManager
                ) {
                    //Toast.makeText(context, "Edit Profile Clicked (Placeholder)", Toast.LENGTH_SHORT).show()
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
                ProfileOptionItem(
                    icon = Icons.Filled.Lock,
                    title = "Change Password",
                    description = "Secure your account with a new password.",
                    soundEffectManager = soundEffectManager
                ) {
                    //Toast.makeText(context, "Change Password Clicked (Placeholder)", Toast.LENGTH_SHORT).show()
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
                ProfileOptionItem(
                    icon = Icons.Filled.Settings,
                    title = "Privacy Settings",
                    description = "Manage your data and privacy preferences.",
                    soundEffectManager = soundEffectManager
                ) {
                    //Toast.makeText(context, "Privacy Settings Clicked (Placeholder)", Toast.LENGTH_SHORT).show()
                }
            }
        }
        Spacer(modifier = Modifier.height(48.dp))

        // Logout Button
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.98f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "logout_button_scale"
        )
        val alpha by animateFloatAsState(
            targetValue = if (isPressed) 0.8f else 1.0f,
            animationSpec = tween(150),
            label = "logout_button_alpha"
        )

        Button(
            onClick = {
                soundEffectManager.playClickSound() // Play sound on logout click
                onLogout()
            },
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error, // Stronger error color for logout
                contentColor = MaterialTheme.colorScheme.onError
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha) // Apply press animation directly
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout Icon")
            Spacer(Modifier.width(16.dp))
            Text("Logout", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Reusable composable for an option item within the Profile Screen.
 * Includes click animation and sound.
 */
@Composable
fun ProfileOptionItem(
    icon: ImageVector,
    title: String,
    description: String,
    soundEffectManager: SoundEffectManager,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "profile_item_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1.0f,
        animationSpec = tween(150),
        label = "profile_item_alpha"
    )

    val defaultIndication = LocalIndication.current // Get the default Material indication

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha) // Apply press animation
            .clickable(
                interactionSource = interactionSource,
                indication = defaultIndication, // Explicitly pass the default indication
                onClick = {
                    soundEffectManager.playClickSound() // Play sound on item click
                    onClick()
                }
            )
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // Icon is decorative here
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = "Go to $title",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}