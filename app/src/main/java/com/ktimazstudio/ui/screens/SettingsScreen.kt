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
        
// --- OneUI 8.5 Style Volume SeekBar ---
Spacer(modifier = Modifier.height(12.dp))

var soundLevel by remember { mutableStateOf(sharedPrefsManager.getSoundLevel()) }

val animatedTrackColor by animateColorAsState(
    targetValue = when {
        soundLevel > 0.7f -> Color(0xFF007AFF)
        soundLevel > 0.3f -> Color(0xFF00BFA6)
        else -> Color(0xFFB0BEC5)
    },
    animationSpec = tween(durationMillis = 300)
)

Column(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text(
        text = "Sound Level: ${(soundLevel * 100).toInt()}%",
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
    )

    Slider(
        value = soundLevel,
        onValueChange = { value ->
            soundLevel = value
            sharedPrefsManager.setSoundLevel(value)

            // ✅ Apply actual system music volume
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val newVolume = (value * maxVolume).toInt().coerceIn(0, maxVolume)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
        },
        valueRange = 0f..1f,
        steps = 0,
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = animatedTrackColor,
            inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

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