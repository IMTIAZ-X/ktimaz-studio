package com.ktimazstudio.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ktimazstudio.BuildConfig
import com.ktimazstudio.enums.ThemeSetting
import com.ktimazstudio.managers.SoundEffectManager
import com.ktimazstudio.managers.SharedPreferencesManager
import com.ktimazstudio.ui.components.SettingItem

import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Brush

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.media.AudioManager

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.animation.core.animateFloatAsState
import kotlinx.coroutines.launch


@Composable
fun SettingsScreen(modifier: Modifier = Modifier, soundEffectManager: SoundEffectManager, sharedPrefsManager: SharedPreferencesManager) {
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showChangelogDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager


    // State for theme and sound settings
    val currentThemeSetting = remember { mutableStateOf(sharedPrefsManager.getThemeSetting()) }
    val isSoundEnabled = remember { mutableStateOf(sharedPrefsManager.isSoundEnabled()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Application Settings",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
        )

        var notificationsEnabled by remember { mutableStateOf(true) }
        SettingItem(
            title = "Enable Notifications",
            description = "Receive updates and alerts.",
            leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)},
            control = {
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = {
                        soundEffectManager.playClickSound() // Play sound on switch toggle
                        notificationsEnabled = it
                    }
                )
            }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

        // Theme Changer Setting
        SettingItem(
            title = "App Theme",
            description = "Change the visual theme of the application.",
            leadingIcon = { Icon(Icons.Filled.ColorLens, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)},
            control = {
                // Dropdown menu for theme selection
                var expanded by remember { mutableStateOf(false) }
                TextButton(onClick = {
                    soundEffectManager.playClickSound()
                    expanded = true
                }) {
                    Text(currentThemeSetting.value.name.replace("_", " "), style = MaterialTheme.typography.bodyMedium)
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Expand theme options")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    ThemeSetting.values().forEach { theme ->
                        DropdownMenuItem(
                            text = { Text(theme.name.replace("_", " ")) },
                            onClick = {
                                soundEffectManager.playClickSound()
                                sharedPrefsManager.setThemeSetting(theme)
                                currentThemeSetting.value = theme // Update local state
                                expanded = false
                            }
                        )
                    }
                }
            }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

        // Sound On/Off Setting
        SettingItem(
            title = "Sound Effects",
            description = "Enable or disable click sounds and other effects.",
            leadingIcon = { Icon(if (isSoundEnabled.value) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)},
            control = {
                Switch(
                    checked = isSoundEnabled.value,
                    onCheckedChange = {
                        sharedPrefsManager.setSoundEnabled(it)
                        isSoundEnabled.value = it // Update local state
                        if (it) soundEffectManager.playClickSound() // Play sound only if enabling
                    }
                )
            }
        )
        
// ---------- Samsung One UI 8.5 style custom SeekBar ----------
val scope = rememberCoroutineScope()
val density = LocalDensity.current

// visual sizes
val trackHeightDp: Dp = 6.dp
val thumbRadiusDp: Dp = 12.dp
val horizontalPaddingDp: Dp = 12.dp

// animated value for smooth thumb movement
val animatedLevel by animateFloatAsState(targetValue = soundLevel, animationSpec = tween(200))

Box(
    modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .padding(horizontal = 8.dp),
    contentAlignment = Alignment.Center
) {
    // Draw the track and thumb with Canvas and handle gestures
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .pointerInput(Unit) {
                // tap to set
                detectTapGestures { pos ->
                    val widthPx = size.width - with(density) { horizontalPaddingDp.toPx() * 2f }
                    val x = (pos.x - with(density) { horizontalPaddingDp.toPx() }).coerceIn(0f, widthPx)
                    val newLevel = (x / widthPx).coerceIn(0f, 1f)
                    // update state and save + apply volume
                    scope.launch {
                        soundLevel = newLevel
                        sharedPrefsManager.setSoundLevel(newLevel)
                        val maxVol = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
                        val newVol = (newLevel * maxVol).toInt().coerceIn(0, maxVol)
                        audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, newVol, 0)
                    }
                }
            }
            .pointerInput(Unit) {
                // drag to change
                detectDragGestures { change, _ ->
                    val localX = change.position.x
                    val widthPx = size.width - with(density) { horizontalPaddingDp.toPx() * 2f }
                    val x = (localX - with(density) { horizontalPaddingDp.toPx() }).coerceIn(0f, widthPx)
                    val newLevel = (x / widthPx).coerceIn(0f, 1f)
                    scope.launch {
                        soundLevel = newLevel
                        sharedPrefsManager.setSoundLevel(newLevel)
                        val maxVol = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
                        val newVol = (newLevel * maxVol).toInt().coerceIn(0, maxVol)
                        audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, newVol, 0)
                    }
                }
            }
    ) {
        val trackHeightPx = with(density) { trackHeightDp.toPx() }
        val thumbRadiusPx = with(density) { thumbRadiusDp.toPx() }
        val hp = with(density) { horizontalPaddingDp.toPx() }

        val widthPx = size.width - hp * 2f
        val centerY = size.height / 2f

        // compute positions
        val activeWidth = animatedLevel * widthPx
        val thumbCx = hp + activeWidth
        val thumbCy = centerY

        // OneUI-style gradient for active track
        val activeBrush = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF2D92FF), // blue
                Color(0xFF04A915)  // green-ish
            ),
            startX = hp,
            endX = hp + widthPx
        )

        // inactive track
        drawRoundRect(
            color = Color(0xFFDDE3E9), // subtle track background (light gray)
            topLeft = Offset(hp, centerY - trackHeightPx / 2f),
            size = Size(widthPx, trackHeightPx),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeightPx / 2f, trackHeightPx / 2f)
        )

        // active track (gradient)
        drawRoundRect(
            brush = activeBrush,
            topLeft = Offset(hp, centerY - trackHeightPx / 2f),
            size = Size(activeWidth, trackHeightPx),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeightPx / 2f, trackHeightPx / 2f)
        )

        // subtle glow/halo under thumb (one ui feel)
        drawCircle(
            color = Color(0x332D92FF), // translucent blue halo
            radius = thumbRadiusPx * 1.6f,
            center = Offset(thumbCx, thumbCy)
        )

        // thumb (solid)
        drawCircle(
            color = Color.White,
            radius = thumbRadiusPx,
            center = Offset(thumbCx, thumbCy),
            style = androidx.compose.ui.graphics.drawscope.Fill
        )

        // thumb border
        drawCircle(
            color = Color(0xFFB8C3D6), // light border
            radius = thumbRadiusPx,
            center = Offset(thumbCx, thumbCy),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
        )
    }

    // Optional: show numeric percentage on right side (small chip)
    Box(modifier = Modifier
        .align(Alignment.CenterEnd)
        .padding(end = 8.dp)
        .background(color = Color(0x12000000), shape = RoundedCornerShape(8.dp))
        .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = "${(soundLevel * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}
// ---------- end custom seekbar ----------


        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

        var showAccountDialog by remember { mutableStateOf(false) }
        SettingItem(
            title = "Account Preferences",
            description = "Manage your account details.",
            leadingIcon = { Icon(Icons.Filled.AccountBox, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)},
            control = { Icon(Icons.Filled.ChevronRight, contentDescription = "Go to account preferences", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            onClick = {
                soundEffectManager.playClickSound() // Play sound on item click
                showAccountDialog = true
            }
        )
        if (showAccountDialog) {
            AlertDialog(
                onDismissRequest = { showAccountDialog = false },
                icon = { Icon(Icons.Filled.AccountBox, contentDescription = null)},
                title = { Text("Account Preferences") },
                text = { Text("Account settings details would appear here or navigate to a dedicated screen. This is a placeholder.") },
                confirmButton = { TextButton(onClick = {
                    soundEffectManager.playClickSound() // Play sound on dialog button click
                    showAccountDialog = false
                }) { Text("OK") } }
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

        SettingItem(
            title = "About",
            description = "Information about this application.",
            leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)},
            control = { Icon(Icons.Filled.ChevronRight, contentDescription = "View About", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            onClick = {
                soundEffectManager.playClickSound() // Play sound on item click
                showAboutDialog = true
            }
        )
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                icon = { Icon(Icons.Filled.Info, contentDescription = "About App Icon")},
                title = { Text("About " + "App Name") }, // TODO: Replace with actual app name
                text = { Text("Version: ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})\n\nDeveloped by Ktimaz Studio.\n\nThis application is a demonstration of various Android and Jetpack Compose features. Thank you for using our app!") },
                confirmButton = { TextButton(onClick = {
                    soundEffectManager.playClickSound() // Play sound on dialog button click
                    showAboutDialog = false
                }) { Text("Close") } }
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

        SettingItem(
            title = "Privacy Policy",
            description = "Read our privacy policy.",
            leadingIcon = { Icon(Icons.Filled.Policy, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)},
            control = { Icon(Icons.Filled.ChevronRight, contentDescription = "View Privacy Policy", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            onClick = {
                soundEffectManager.playClickSound() // Play sound on item click
                showPrivacyDialog = true
            }
        )
        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                icon = { Icon(Icons.Filled.Policy, contentDescription = "Privacy Policy Icon")},
                title = { Text("Privacy Policy") },
                text = { Text("Placeholder for Privacy Policy text. In a real application, this would contain the full policy details or link to a web page.\n\nWe are committed to protecting your privacy. Our policy outlines how we collect, use, and safeguard your information.") },
                confirmButton = { TextButton(onClick = {
                    soundEffectManager.playClickSound() // Play sound on dialog button click
                    showPrivacyDialog = false
                }) { Text("Close") } }
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

        // Changelog Item
        SettingItem(
            title = "Changelog",
            description = "See what's new in this version.",
            leadingIcon = { Icon(Icons.Filled.HistoryEdu, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)},
            control = { Icon(Icons.Filled.ChevronRight, contentDescription = "View Changelog", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            onClick = {
                soundEffectManager.playClickSound() // Play sound on item click
                showChangelogDialog = true
            }
        )
        if (showChangelogDialog) {
            AlertDialog(
                onDismissRequest = { showChangelogDialog = false },
                icon = { Icon(Icons.Filled.HistoryEdu, contentDescription = "Changelog Icon", modifier = Modifier.size(28.dp))},
                title = { Text("What's New - v${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.titleLarge) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text("Version ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                        Text("✨ New Features:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                        Text(" • Added persistent login with auto-login.", style = MaterialTheme.typography.bodyMedium)
                        Text(" • Implemented Logout functionality.", style = MaterialTheme.typography.bodyMedium)
                        Text(" • Enhanced VPN detection with a Material 3 dialog.", style = MaterialTheme.typography.bodyMedium)
                        Text(" • Added 'About', 'Privacy Policy', and 'Changelog' to Settings.", style = MaterialTheme.typography.bodyMedium)
                        Text(" • Implemented basic reverse engineering detection (debugger, emulator, root, APK tampering).", style = MaterialTheme.typography.bodyMedium)
                        Text(" • Added click sound effects and beautiful press animations.", style = MaterialTheme.typography.bodyMedium)
                        Text(" • Implemented search functionality in the Dashboard.", style = MaterialTheme.typography.bodyMedium)
                        Text(" • Added tooltips for new users on Dashboard cards.", style = MaterialTheme.typography.bodyMedium)
                        Text(" • Added Theme Changer (Light, Dark, System, Battery Saver).", style = MaterialTheme.typography.bodyMedium) // New Changelog entry
                        Text(" • Added Sound Effects On/Off setting.", style = MaterialTheme.typography.bodyMedium) // New Changelog entry
                        Text(" • Improved UI sizing consistency across devices.", style = MaterialTheme.typography.bodyMedium) // New Changelog entry
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("🐛 Bug Fixes & Improvements:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
                        Text(" • Addressed various icon resolution and deprecation warnings.", style = MaterialTheme.typography.bodyMedium)
                        Text(" • Polished Login screen UX and Navigation Rail visuals.", style = MaterialTheme.typography.bodyMedium)
                        Text(" • Profile screen now shows username and placeholder picture.", style = MaterialTheme.typography.bodyMedium)
                        Text(" • General UI/UX tweaks for a more expressive Material 3 feel.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Thank you for updating!", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = { TextButton(onClick = {
                    soundEffectManager.playClickSound() // Play sound on dialog button click
                    showChangelogDialog = false
                }) { Text("Awesome!") } },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))


        SettingItem(
            title = "App Version",
            description = "${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
            leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)},
            control = {}
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
    }
}