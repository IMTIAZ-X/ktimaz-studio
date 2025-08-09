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
import com.ktimazstudio.enums.NavigationStyle
import com.ktimazstudio.enums.LayoutDensity
import com.ktimazstudio.enums.AnimationSpeed
import com.ktimazstudio.managers.SoundEffectManager
import com.ktimazstudio.managers.SharedPreferencesManager
import com.ktimazstudio.ui.components.SettingItem

@Composable
fun EnhancedSettingsScreen(
    modifier: Modifier = Modifier, 
    soundEffectManager: SoundEffectManager, 
    sharedPrefsManager: SharedPreferencesManager
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showChangelogDialog by remember { mutableStateOf(false) }
    var showSecurityDashboard by remember { mutableStateOf(false) }
    var showDeveloperOptions by remember { mutableStateOf(false) }

    // Enhanced State Management
    val currentThemeSetting = remember { mutableStateOf(sharedPrefsManager.getThemeSetting()) }
    val isSoundEnabled = remember { mutableStateOf(sharedPrefsManager.isSoundEnabled()) }
    val currentNavigationStyle = remember { mutableStateOf(sharedPrefsManager.getNavigationStyle()) }
    val currentLayoutDensity = remember { mutableStateOf(sharedPrefsManager.getLayoutDensity()) }
    val currentAnimationSpeed = remember { mutableStateOf(sharedPrefsManager.getAnimationSpeed()) }
    val currentFontSize = remember { mutableStateOf(sharedPrefsManager.getFontSize()) }
    val isHapticEnabled = remember { mutableStateOf(sharedPrefsManager.isHapticEnabled()) }
    val isDynamicColorsEnabled = remember { mutableStateOf(sharedPrefsManager.isDynamicColorsEnabled()) }
    val isHighContrastEnabled = remember { mutableStateOf(sharedPrefsManager.isHighContrastEnabled()) }
    val isReducedMotionEnabled = remember { mutableStateOf(sharedPrefsManager.isReducedMotionEnabled()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
        )

        // 🎨 APPEARANCE & THEMES SECTION
        SettingsCategory(
            title = "🎨 Appearance & Themes",
            icon = Icons.Filled.ColorLens
        ) {
            // App Theme Setting
            SettingItem(
                title = "App Theme",
                description = "Choose your preferred theme",
                leadingIcon = { Icon(Icons.Filled.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                control = {
                    var expanded by remember { mutableStateOf(false) }
                    TextButton(onClick = {
                        soundEffectManager.playClickSound()
                        expanded = true
                    }) {
                        Text(currentThemeSetting.value.name.replace("_", " "))
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Expand")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        ThemeSetting.values().forEach { theme ->
                            DropdownMenuItem(
                                text = { Text(theme.name.replace("_", " ")) },
                                onClick = {
                                    soundEffectManager.playClickSound()
                                    sharedPrefsManager.setThemeSetting(theme)
                                    currentThemeSetting.value = theme
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            )

            // Dynamic Colors (Android 12+)
            SettingItem(
                title = "Dynamic Colors",
                description = "Use system wallpaper colors (Android 12+)",
                leadingIcon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                control = {
                    Switch(
                        checked = isDynamicColorsEnabled.value,
                        onCheckedChange = {
                            sharedPrefsManager.setDynamicColorsEnabled(it)
                            isDynamicColorsEnabled.value = it
                            if (it) soundEffectManager.playClickSound()
                        }
                    )
                }
            )

            // High Contrast Mode
            SettingItem(
                title = "High Contrast",
                description = "Improve readability with enhanced contrast",
                leadingIcon = { Icon(Icons.Filled.Contrast, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                control = {
                    Switch(
                        checked = isHighContrastEnabled.value,
                        onCheckedChange = {
                            sharedPrefsManager.setHighContrastEnabled(it)
                            isHighContrastEnabled.value = it
                            if (it) soundEffectManager.playClickSound()
                        }
                    )
                }
            )

            // Layout Density
            SettingItem(
                title = "Layout Density",
                description = "Adjust information density",
                leadingIcon = { Icon(Icons.Filled.ViewComfy, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                control = {
                    var expanded by remember { mutableStateOf(false) }
                    TextButton(onClick = {
                        soundEffectManager.playClickSound()
                        expanded = true
                    }) {
                        Text(currentLayoutDensity.value.displayName)
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Expand")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        LayoutDensity.values().forEach { density ->
                            DropdownMenuItem(
                                text = { Text(density.displayName) },
                                onClick = {
                                    soundEffectManager.playClickSound()
                                    sharedPrefsManager.setLayoutDensity(density)
                                    currentLayoutDensity.value = density
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            )

            // Font Size
            SettingItem(
                title = "Font Size",
                description = "Adjust text size for better readability",
                leadingIcon = { Icon(Icons.Filled.FormatSize, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                control = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${currentFontSize.value}%", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Slider(
                            value = currentFontSize.value.toFloat(),
                            onValueChange = { 
                                val newSize = it.toInt()
                                currentFontSize.value = newSize
                                sharedPrefsManager.setFontSize(newSize)
                            },
                            valueRange = 80f..120f,
                            steps = 7,
                            modifier = Modifier.width(120.dp)
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔊 AUDIO & HAPTICS SECTION
        SettingsCategory(
            title = "🔊 Audio & Haptics",
            icon = Icons.Filled.VolumeUp
        ) {
            // Sound Effects
            SettingItem(
                title = "Sound Effects",
                description = "Enable click sounds and audio feedback",
                leadingIcon = { Icon(if (isSoundEnabled.value) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                control = {
                    Switch(
                        checked = isSoundEnabled.value,
                        onCheckedChange = {
                            sharedPrefsManager.setSoundEnabled(it)
                            isSoundEnabled.value = it
                            if (it) soundEffectManager.playClickSound()
                        }
                    )
                }
            )

            // Haptic Feedback
            SettingItem(
                title = "Haptic Feedback",
                description = "Enable vibration feedback for interactions",
                leadingIcon = { Icon(Icons.Filled.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                control = {
                    Switch(
                        checked = isHapticEnabled.value,
                        onCheckedChange = {
                            sharedPrefsManager.setHapticEnabled(it)
                            isHapticEnabled.value = it
                            if (it) soundEffectManager.playClickSound()
                        }
                    )
                }
            )

            // Animation Speed
            SettingItem(
                title = "Animation Speed",
                description = "Adjust interface animation speed",
                leadingIcon = { Icon(Icons.Filled.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                control = {
                    var expanded by remember { mutableStateOf(false) }
                    TextButton(onClick = {
                        soundEffectManager.playClickSound()
                        expanded = true
                    }) {
                        Text(currentAnimationSpeed.value.displayName)
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Expand")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        AnimationSpeed.values().forEach { speed ->
                            DropdownMenuItem(
                                text = { Text(speed.displayName) },
                                onClick = {
                                    soundEffectManager.playClickSound()
                                    sharedPrefsManager.setAnimationSpeed(speed)
                                    currentAnimationSpeed.value = speed
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔒 SECURITY & PRIVACY SECTION
        SettingsCategory(
            title = "🔒 Security & Privacy",
            icon = Icons.Filled.Security
        ) {
            // Security Dashboard
            SettingItem(
                title = "Security Dashboard",
                description = "View device security status and threats",
                leadingIcon = { Icon(Icons.Filled.Dashboard, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                control = { Icon(Icons.Filled.ChevronRight, contentDescription = "Open Security Dashboard") },
                onClick = {
                    soundEffectManager.playClickSound()
                    showSecurityDashboard = true
                }
            )

            // Privacy Controls
            SettingItem(
                title = "Privacy Controls",
                description = "Manage data usage and permissions",
                leadingIcon = { Icon(Icons.Filled.PrivacyTip, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                control = { Icon(Icons.Filled.ChevronRight, contentDescription = "Open Privacy Controls") },
                onClick = {
                    soundEffectManager.playClickSound()
                    // Navigate to Privacy Controls
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 📱 NAVIGATION & LAYOUT SECTION
        SettingsCategory(
            title = "📱 Navigation & Layout",
            icon = Icons.Filled.Navigation
        ) {
            // Navigation Style
            SettingItem(
                title = "Navigation Style",
                description = "Choose between bottom bar, rail, or drawer",
                leadingIcon = { Icon(Icons.Filled.Menu, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                control = {
                    var expanded by remember { mutableStateOf(false) }
                    TextButton(onClick = {
                        soundEffectManager.playClickSound()
                        expanded = true
                    }) {
                        Text(currentNavigationStyle.value.displayName)
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Expand")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        NavigationStyle.values().forEach { style ->
                            DropdownMenuItem(
                                text = { Text(style.displayName) },
                                onClick = {
                                    soundEffectManager.playClickSound()
                                    sharedPrefsManager.setNavigationStyle(style)
                                    currentNavigationStyle.value = style
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            )

            // Dashboard Customization
            SettingItem(
                title = "Dashboard Layout",
                description = "Customize dashboard appearance and modules",
                leadingIcon = { Icon(Icons.Filled.GridView, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                control = { Icon(Icons.Filled.ChevronRight, contentDescription = "Customize Dashboard") },
                onClick = {
                    soundEffectManager.playClickSound()
                    // Navigate to Dashboard Customization
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ♿ ACCESSIBILITY SECTION
        SettingsCategory(
            title = "♿ Accessibility",
            icon = Icons.Filled.Accessibility
        ) {
            // Reduced Motion
            SettingItem(
                title = "Reduce Motion",
                description = "Minimize animations for motion sensitivity",
                leadingIcon = { Icon(Icons.Filled.SlowMotionVideo, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                control = {
                    Switch(
                        checked = isReducedMotionEnabled.value,
                        onCheckedChange = {
                            sharedPrefsManager.setReducedMotionEnabled(it)
                            isReducedMotionEnabled.value = it
                            if (it) soundEffectManager.playClickSound()
                        }
                    )
                }
            )

            // Screen Reader Support
            SettingItem(
                title = "Screen Reader",
                description = "Optimize for screen reader accessibility",
                leadingIcon = { Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                control = { Icon(Icons.Filled.ChevronRight, contentDescription = "Screen Reader Settings") },
                onClick = {
                    soundEffectManager.playClickSound()
                    // Navigate to Screen Reader Settings
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔧 DEVELOPER OPTIONS (Hidden by default)
        if (showDeveloperOptions) {
            SettingsCategory(
                title = "🔧 Developer Options",
                icon = Icons.Filled.DeveloperMode
            ) {
                // Performance Monitoring
                SettingItem(
                    title = "Performance Monitor",
                    description = "Show FPS and memory usage overlay",
                    leadingIcon = { Icon(Icons.Filled.MonitorHeart, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                    control = {
                        var enabled by remember { mutableStateOf(false) }
                        Switch(
                            checked = enabled,
                            onCheckedChange = { enabled = it }
                        )
                    }
                )
            }
        }

        // Developer Options Toggle (Hidden - tap 7 times on version)
        var developerTaps by remember { mutableStateOf(0) }
        SettingItem(
            title = "App Version",
            description = "${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
            leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
            control = {},
            onClick = {
                developerTaps++
                if (developerTaps >= 7) {
                    showDeveloperOptions = true
                    soundEffectManager.playClickSound()
                }
            }
        )

        // Standard Dialogs (About, Privacy, etc.)
        StandardSettingsDialogs(
            showAboutDialog = showAboutDialog,
            showPrivacyDialog = showPrivacyDialog,
            showChangelogDialog = showChangelogDialog,
            onDismissAbout = { showAboutDialog = false },
            onDismissPrivacy = { showPrivacyDialog = false },
            onDismissChangelog = { showChangelogDialog = false },
            soundEffectManager = soundEffectManager
        )
    }
}

@Composable
fun SettingsCategory(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
            content()
        }
    }
}

@Composable
fun StandardSettingsDialogs(
    showAboutDialog: Boolean,
    showPrivacyDialog: Boolean,
    showChangelogDialog: Boolean,
    onDismissAbout: () -> Unit,
    onDismissPrivacy: () -> Unit,
    onDismissChangelog: () -> Unit,
    soundEffectManager: SoundEffectManager
) {
    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = onDismissAbout,
            icon = { Icon(Icons.Filled.Info, contentDescription = "About App Icon") },
            title = { Text("About App") },
            text = { 
                Text("Version: ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})\n\nDeveloped by Ktimaz Studio.\n\nThis application showcases modern Android development with enhanced security features.")
            },
            confirmButton = { 
                TextButton(onClick = {
                    soundEffectManager.playClickSound()
                    onDismissAbout()
                }) { Text("Close") }
            }
        )
    }
    
    // Add other dialogs as needed...
}