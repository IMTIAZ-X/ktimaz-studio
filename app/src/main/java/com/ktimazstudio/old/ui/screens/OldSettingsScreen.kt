package com.ktimazstudio.old.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ktimazstudio.BuildConfig
import com.ktimazstudio.managers.SoundEffectManager
import com.ktimazstudio.managers.SharedPreferencesManager
import com.ktimazstudio.ui.components.SettingItem

/**
 * Classic Settings Screen - Simple list-based layout
 */
@Composable
fun OldSettingsScreen(
    modifier: Modifier = Modifier,
    soundEffectManager: SoundEffectManager,
    sharedPrefsManager: SharedPreferencesManager
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    var showNewUiDialog by remember { mutableStateOf(false) }
    
    val isSoundEnabled = remember { mutableStateOf(sharedPrefsManager.isSoundEnabled()) }
    val currentThemeSetting = remember { mutableStateOf(sharedPrefsManager.getThemeSetting()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Classic Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Switch to New UI
                SettingItem(
                    title = "Modern Interface",
                    description = "Switch to the new enhanced interface",
                    leadingIcon = { 
                        Icon(Icons.Filled.NewReleases, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    control = { 
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Switch to new UI", tint = MaterialTheme.colorScheme.onSurfaceVariant) 
                    },
                    onClick = {
                        showNewUiDialog = true
                    },
                    soundEffectManager = soundEffectManager
                )
                
                HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

                // Sound Effects
                SettingItem(
                    title = "Sound Effects",
                    description = "Enable click sounds",
                    leadingIcon = { 
                        Icon(
                            if (isSoundEnabled.value) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    },
                    control = {
                        Switch(
                            checked = isSoundEnabled.value,
                            onCheckedChange = { enabled ->
                                sharedPrefsManager.setSoundEnabled(enabled)
                                isSoundEnabled.value = enabled
                                if (enabled) soundEffectManager.playClickSound()
                            }
                        )
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

                // About
                SettingItem(
                    title = "About",
                    description = "Application information",
                    leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                    control = { Icon(Icons.Filled.ChevronRight, contentDescription = "View About", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    onClick = {
                        showAboutDialog = true
                    },
                    soundEffectManager = soundEffectManager
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

                // Version Info
                SettingItem(
                    title = "Version",
                    description = "${BuildConfig.VERSION_NAME} (Classic Mode)",
                    leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) }
                )
            }
        }
    }

    // New UI Switch Dialog
    if (showNewUiDialog) {
        AlertDialog(
            onDismissRequest = { showNewUiDialog = false },
            icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null) },
            title = { Text("Switch to Modern Interface?") },
            text = { 
                Text("This will enable the new modern interface with enhanced features, animations, and customization options.")
            },
            confirmButton = {
                TextButton(onClick = {
                    soundEffectManager.playClickSound()
                    sharedPrefsManager.setOldUiEnabled(false)
                    showNewUiDialog = false
                }) {
                    Text("Switch to Modern")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    soundEffectManager.playClickSound()
                    showNewUiDialog = false
                }) {
                    Text("Stay Classic")
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = { Icon(Icons.Filled.Info, contentDescription = "About App Icon") },
            title = { Text("About Ktimaz Studio (Classic)") },
            text = { 
                Text("Version: ${BuildConfig.VERSION_NAME}\n\nYou are using the classic interface. Switch to the modern interface in settings for enhanced features.")
            },
            confirmButton = { 
                TextButton(onClick = {
                    soundEffectManager.playClickSound()
                    showAboutDialog = false
                }) { 
                    Text("Close") 
                } 
            }
        )
    }
}