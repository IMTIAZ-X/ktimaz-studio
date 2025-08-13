package com.ktimazstudio.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ktimazstudio.BuildConfig
import com.ktimazstudio.enums.*
import com.ktimazstudio.managers.SoundEffectManager
import com.ktimazstudio.managers.SharedPreferencesManager
import com.ktimazstudio.ui.components.SettingItem
import com.ktimazstudio.ui.components.SettingSection
import com.ktimazstudio.ui.components.SettingDropdown

@Composable
fun EnhancedSettingsScreen(
    modifier: Modifier = Modifier,
    soundEffectManager: SoundEffectManager,
    sharedPrefsManager: SharedPreferencesManager
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    var showOldUiDialog by remember { mutableStateOf(false) }
    
    // Settings states
    val isOldUiEnabled = remember { mutableStateOf(sharedPrefsManager.isOldUiEnabled()) }
    val currentThemeSetting = remember { mutableStateOf(sharedPrefsManager.getThemeSetting()) }
    val isSoundEnabled = remember { mutableStateOf(sharedPrefsManager.isSoundEnabled()) }
    val navigationStyle = remember { mutableStateOf(sharedPrefsManager.getNavigationStyle()) }
    val layoutDensity = remember { mutableStateOf(sharedPrefsManager.getLayoutDensity()) }
    val animationSpeed = remember { mutableStateOf(sharedPrefsManager.getAnimationSpeed()) }
    val dashboardViewType = remember { mutableStateOf(sharedPrefsManager.getDashboardViewType()) }
    val cardSize = remember { mutableStateOf(sharedPrefsManager.getCardSize()) }
    val isHapticEnabled = remember { mutableStateOf(sharedPrefsManager.isHapticFeedbackEnabled()) }
    val isSecureMode = remember { mutableStateOf(sharedPrefsManager.isSecureModeEnabled()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Enhanced Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
        )

        // UI Style Section
        SettingSection(
            title = "Interface & Experience",
            icon = Icons.Filled.Palette
        ) {
            // Old UI Toggle with confirmation
            SettingItem(
                title = "Classic Interface",
                description = if (isOldUiEnabled.value) "Using classic UI style" else "Using modern interface",
                leadingIcon = { 
                    Icon(
                        if (isOldUiEnabled.value) Icons.Filled.ToggleOn else Icons.Filled.ToggleOff, 
                        contentDescription = null, 
                        tint = if (isOldUiEnabled.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                },
                onClick = {
                    if (!isOldUiEnabled.value) {
                        showOldUiDialog = true
                    } else {
                        sharedPrefsManager.setOldUiEnabled(false)
                        isOldUiEnabled.value = false
                    }
                },
                soundEffectManager = soundEffectManager
            )
            
            if (showOldUiDialog) {
                AlertDialog(
                    onDismissRequest = { showOldUiDialog = false },
                    icon = { Icon(Icons.Filled.Info, contentDescription = null) },
                    title = { Text("Switch to Classic Interface?") },
                    text = { 
                        Text("This will switch to the classic UI with older styling and features. You can switch back anytime from settings.")
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            soundEffectManager.playClickSound()
                            sharedPrefsManager.setOldUiEnabled(true)
                            isOldUiEnabled.value = true
                            showOldUiDialog = false
                        }) {
                            Text("Switch to Classic")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            soundEffectManager.playClickSound()
                            showOldUiDialog = false
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Theme Setting
            SettingDropdown(
                title = "App Theme",
                description = "Choose your preferred theme",
                leadingIcon = { Icon(Icons.Filled.ColorLens, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                currentValue = currentThemeSetting.value,
                options = ThemeSetting.values().toList(),
                onValueChange = { theme ->
                    sharedPrefsManager.setThemeSetting(theme)
                    currentThemeSetting.value = theme
                },
                soundEffectManager = soundEffectManager
            )

            // Navigation Style
            SettingDropdown(
                title = "Navigation Style",
                description = "Choose navigation layout",
                leadingIcon = { Icon(Icons.Filled.Navigation, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                currentValue = navigationStyle.value,
                options = NavigationStyle.values().toList(),
                onValueChange = { style ->
                    sharedPrefsManager.setNavigationStyle(style)
                    navigationStyle.value = style
                },
                soundEffectManager = soundEffectManager
            )

            // Layout Density
            SettingDropdown(
                title = "Layout Density",
                description = "Adjust spacing and density",
                leadingIcon = { Icon(Icons.Filled.ViewCompact, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                currentValue = layoutDensity.value,
                options = LayoutDensity.values().toList(),
                onValueChange = { density ->
                    sharedPrefsManager.setLayoutDensity(density)
                    layoutDensity.value = density
                },
                soundEffectManager = soundEffectManager
            )

            // Dashboard View Type
            SettingDropdown(
                title = "Dashboard Layout",
                description = "Choose dashboard display style",
                leadingIcon = { Icon(Icons.Filled.Dashboard, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                currentValue = dashboardViewType.value,
                options = DashboardViewType.values().toList(),
                onValueChange = { viewType ->
                    sharedPrefsManager.setDashboardViewType(viewType)
                    dashboardViewType.value = viewType
                },
                soundEffectManager = soundEffectManager
            )

            // Card Size
            SettingDropdown(
                title = "Card Size",
                description = "Adjust module card size",
                leadingIcon = { Icon(Icons.Filled.CropFree, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                currentValue = cardSize.value,
                options = CardSize.values().toList(),
                onValueChange = { size ->
                    sharedPrefsManager.setCardSize(size)
                    cardSize.value = size
                },
                soundEffectManager = soundEffectManager
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interaction & Feedback Section
        SettingSection(
            title = "Interaction & Feedback",
            icon = Icons.Filled.TouchApp
        ) {
            // Sound Effects
            SettingItem(
                title = "Sound Effects",
                description = "Enable click sounds and audio feedback",
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

            // Haptic Feedback
            SettingItem(
                title = "Haptic Feedback",
                description = "Enable vibration feedback",
                leadingIcon = { Icon(Icons.Filled.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                control = {
                    Switch(
                        checked = isHapticEnabled.value,
                        onCheckedChange = { enabled ->
                            sharedPrefsManager.setHapticFeedbackEnabled(enabled)
                            isHapticEnabled.value = enabled
                        }
                    )
                }
            )

            // Animation Speed
            SettingDropdown(
                title = "Animation Speed",
                description = "Adjust interface animation speed",
                leadingIcon = { Icon(Icons.Filled.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                currentValue = animationSpeed.value,
                options = AnimationSpeed.values().toList(),
                onValueChange = { speed ->
                    sharedPrefsManager.setAnimationSpeed(speed)
                    animationSpeed.value = speed
                },
                soundEffectManager = soundEffectManager
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Security & Privacy Section
        SettingSection(
            title = "Security & Privacy",
            icon = Icons.Filled.Security
        ) {
            // Secure Mode
            SettingItem(
                title = "Enhanced Security",
                description = if (isSecureMode.value) "Advanced security checks enabled" else "Basic security only",
                leadingIcon = { 
                    Icon(
                        Icons.Filled.Shield, 
                        contentDescription = null, 
                        tint = if (isSecureMode.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                },
                control = {
                    Switch(
                        checked = isSecureMode.value,
                        onCheckedChange = { enabled ->
                            sharedPrefsManager.setSecureModeEnabled(enabled)
                            isSecureMode.value = enabled
                        }
                    )
                }
            )

            var showDebugDialog by remember { mutableStateOf(false) }
            val isDebugMode = remember { mutableStateOf(sharedPrefsManager.isDebugModeEnabled()) }

            // Debug Mode (Developer Option)
            SettingItem(
                title = "Developer Options",
                description = if (isDebugMode.value) "Debug mode active" else "Debug mode disabled",
                leadingIcon = { Icon(Icons.Filled.DeveloperMode, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                onClick = {
                    if (!isDebugMode.value) {
                        showDebugDialog = true
                    } else {
                        sharedPrefsManager.setDebugModeEnabled(false)
                        isDebugMode.value = false
                    }
                },
                soundEffectManager = soundEffectManager
            )

            if (showDebugDialog) {
                AlertDialog(
                    onDismissRequest = { showDebugDialog = false },
                    icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    title = { Text("Enable Developer Options?") },
                    text = { 
                        Text("This will enable debug features and may reduce security. Only enable if you are a developer.")
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            soundEffectManager.playClickSound()
                            sharedPrefsManager.setDebugModeEnabled(true)
                            isDebugMode.value = true
                            showDebugDialog = false
                        }) {
                            Text("Enable")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            soundEffectManager.playClickSound()
                            showDebugDialog = false
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System Information Section
        SettingSection(
            title = "System Information",
            icon = Icons.Filled.Info
        ) {
            // About
            SettingItem(
                title = "About Application",
                description = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                control = { Icon(Icons.Filled.ChevronRight, contentDescription = "View About", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                onClick = {
                    showAboutDialog = true
                },
                soundEffectManager = soundEffectManager
            )

            if (showAboutDialog) {
                AboutDialog(
                    onDismiss = { showAboutDialog = false },
                    soundEffectManager = soundEffectManager
                )
            }

            // Device Info
            SettingItem(
                title = "Device Information",
                description = "${android.os.Build.MODEL} • Android ${android.os.Build.VERSION.RELEASE}",
                leadingIcon = { Icon(Icons.Filled.PhoneAndroid, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) }
            )

            // Build Information
            SettingItem(
                title = "Build Information",
                description = "${BuildConfig.BUILD_TYPE} • ${if (BuildConfig.DEBUG) "Debug" else "Release"}",
                leadingIcon = { Icon(Icons.Filled.Build, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) }
            )
        }

        // Bottom padding
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun AboutDialog(
    onDismiss: () -> Unit,
    soundEffectManager: SoundEffectManager
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Info, contentDescription = "About App Icon") },
        title = { 
            Text(
                "Ktimaz Studio App", 
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column {
                Text(
                    "Version: ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    "🚀 Enhanced Features:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                val features = listOf(
                    "• Modern Material 3 Design",
                    "• Advanced Security System",
                    "• Customizable Interface",
                    "• Multiple Navigation Styles",
                    "• Comprehensive Settings",
                    "• Classic UI Compatibility",
                    "• Enhanced Animations",
                    "• Developer Options"
                )
                
                features.forEach { feature ->
                    Text(
                        feature,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Developed by Ktimaz Studio with ❤️",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    "Thank you for using our application!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                soundEffectManager.playClickSound()
                onDismiss()
            }) {
                Text("Close")
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
    )
}