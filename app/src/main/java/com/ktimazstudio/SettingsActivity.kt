package com.ktimazstudio

// General Compose imports
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication // Added for LocalIndication.current
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale // Added for Modifier.scale()
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ktimazstudio.ui.theme.ktimaz // Your app's theme
import kotlinx.coroutines.delay

// Material Icons - IMPORTANT: Ensure you have the 'androidx.compose.material:material-icons-extended'
// dependency in your app's build.gradle file for many of these icons.
// e.g., implementation("androidx.compose.material:material-icons-extended:1.6.7") // Use your BOM version
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build // This was not explicitly used in getInitialSettings, but good to have if planned
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Vibration


// --- Data Models for Settings (ViewModel Layer) ---

sealed class SettingModel(
    open val key: String,
    open val title: String,
    open val icon: ImageVector,
    open val description: String? = null,
    open val enabled: Boolean = true
) {
    data class Switch(
        override val key: String,
        override val title: String,
        override val icon: ImageVector,
        override val description: String? = null,
        override val enabled: Boolean = true,
        val isChecked: Boolean,
        val revealsKey: String? = null
    ) : SettingModel(key, title, icon, description, enabled)

    data class Action(
        override val key: String,
        override val title: String,
        override val icon: ImageVector,
        override val description: String? = null,
        override val enabled: Boolean = true,
        val actionType: ActionType
    ) : SettingModel(key, title, icon, description, enabled)

    data class Picker(
        override val key: String,
        override val title: String,
        override val icon: ImageVector,
        override val description: String? = null,
        override val enabled: Boolean = true,
        val options: List<String>,
        val selectedOption: String
    ) : SettingModel(key, title, icon, description, enabled)

    data class SubSwitch(
        override val key: String,
        override val title: String,
        override val icon: ImageVector,
        override val description: String? = null,
        val isChecked: Boolean,
        val parentKey: String
    ) : SettingModel(key, title, icon, description, true)
}

enum class ActionType {
    RESET_PREFERENCES,
    VIEW_PROFILE,
    LOG_OUT,
    PRIVACY_POLICY,
    TERMS_OF_SERVICE,
    ABOUT_APP
}

// --- Mocked Settings Data (Would come from a ViewModel) ---
fun getInitialSettings(): List<SettingModel> {
    return listOf(
        SettingModel.Switch("dark_mode", "Dark Mode", Icons.Filled.DarkMode, "Enable a dark theme for the app.", isChecked = false, revealsKey = "custom_dark_theme_color"),
        SettingModel.SubSwitch("custom_dark_theme_color", "Use Custom Accent", Icons.Filled.ColorLens, "Apply a custom accent color in dark mode.", isChecked = false, parentKey = "dark_mode"),
        SettingModel.Switch("notifications", "Enable Notifications", Icons.Filled.Notifications, "Receive alerts and updates.", isChecked = true, revealsKey = "notification_vibration"), // Corrected revealsKey
        SettingModel.SubSwitch("notification_vibration", "Vibrate for Notifications", Icons.Filled.Vibration, isChecked = true, parentKey = "notifications"),
        SettingModel.Picker("app_theme", "App Theme", Icons.Filled.SettingsApplications, "Choose the primary color theme.", options = listOf("System Default", "Blue", "Green", "Purple"), selectedOption = "System Default"),
        SettingModel.Action("reset_prefs", "Reset Preferences", Icons.Filled.Restore, "Revert all settings to their default values.", actionType = ActionType.RESET_PREFERENCES),
        SettingModel.Action("profile", "View Profile", Icons.Filled.AccountCircle, actionType = ActionType.VIEW_PROFILE),
        SettingModel.Action("privacy_policy", "Privacy Policy", Icons.Filled.Shield, actionType = ActionType.PRIVACY_POLICY),
        SettingModel.Action("terms_of_service", "Terms of Service", Icons.Filled.Policy, actionType = ActionType.TERMS_OF_SERVICE),
        SettingModel.Action("about_app", "About App", Icons.Filled.Info, actionType = ActionType.ABOUT_APP),
        SettingModel.Action("logout", "Log Out", Icons.AutoMirrored.Filled.Logout, actionType = ActionType.LOG_OUT, enabled = true)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ktimaz {
                var settingsList by remember { mutableStateOf(getInitialSettings()) }
                var showResetDialog by remember { mutableStateOf(false) }
                var showThemePickerDialog by remember { mutableStateOf(false) }
                var currentThemePickerKey by remember { mutableStateOf<String?>(null) }

                fun updateSetting(updatedSetting: SettingModel) {
                    settingsList = settingsList.map {
                        if (it.key == updatedSetting.key) updatedSetting else it
                    }
                }

                fun handleSwitchChange(key: String, newCheckedState: Boolean) {
                    val settingToUpdate = settingsList.find { it.key == key }
                    when (settingToUpdate) {
                        is SettingModel.Switch -> {
                            updateSetting(settingToUpdate.copy(isChecked = newCheckedState))
                            if (settingToUpdate.revealsKey != null && !newCheckedState) {
                                val subSetting = settingsList.find { it.key == settingToUpdate.revealsKey && it is SettingModel.SubSwitch } as? SettingModel.SubSwitch
                                subSetting?.let { updateSetting(it.copy(isChecked = false)) }
                            }
                        }
                        is SettingModel.SubSwitch -> updateSetting(settingToUpdate.copy(isChecked = newCheckedState))
                        else -> {}
                    }
                }

                fun handlePickerChange(key: String, newSelectedOption: String) {
                    val settingToUpdate = settingsList.find { it.key == key } as? SettingModel.Picker
                    settingToUpdate?.let {
                        updateSetting(it.copy(selectedOption = newSelectedOption))
                    }
                }

                fun handleAction(actionType: ActionType, settingKey: String) {
                    when (actionType) {
                        ActionType.RESET_PREFERENCES -> showResetDialog = true
                        ActionType.VIEW_PROFILE -> { /* Navigate to profile */ }
                        ActionType.LOG_OUT -> { /* Perform logout */ }
                        else -> { /* Log or placeholder for other actions */ }
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Settings") },
                            navigationIcon = {
                                IconButton(onClick = { onBackPressedDispatcher.onBackPressed() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                            )
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.background
                ) { paddingValues ->
                    SettingsScreenContent(
                        modifier = Modifier.padding(paddingValues),
                        settings = settingsList,
                        onSwitchChanged = ::handleSwitchChange,
                        onActionClicked = ::handleAction,
                        onPickerClicked = { setting ->
                            currentThemePickerKey = setting.key
                            showThemePickerDialog = true
                        }
                    )

                    if (showResetDialog) {
                        ConfirmationDialog(
                            title = "Reset Preferences?",
                            text = "This will reset all settings to their default values. This action cannot be undone.",
                            confirmButtonText = "Reset",
                            dismissButtonText = "Cancel",
                            onConfirm = {
                                settingsList = getInitialSettings()
                                showResetDialog = false
                            },
                            onDismiss = { showResetDialog = false }
                        )
                    }

                    if (showThemePickerDialog) {
                        val pickerSetting = settingsList.find { it.key == currentThemePickerKey } as? SettingModel.Picker
                        pickerSetting?.let {
                            OptionsPickerDialog(
                                title = "Select ${it.title}",
                                options = it.options,
                                selectedOption = it.selectedOption,
                                onOptionSelected = { selected ->
                                    handlePickerChange(it.key, selected)
                                    showThemePickerDialog = false
                                },
                                onDismiss = { showThemePickerDialog = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    settings: List<SettingModel>,
    onSwitchChanged: (key: String, isChecked: Boolean) -> Unit,
    onActionClicked: (actionType: ActionType, key: String) -> Unit,
    onPickerClicked: (setting: SettingModel.Picker) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item { SettingsGroupHeader("General") }
        val generalSettings = settings.filter {
            it.key == "dark_mode" || it.key == "notifications" || it.key == "app_theme" ||
            (it is SettingModel.SubSwitch && (it.parentKey == "dark_mode" || it.parentKey == "notifications"))
        }
        itemsIndexed(generalSettings, key = { _, item -> item.key }) { index, setting ->
            AnimatedSettingItem(index = index) {
                SettingItem(setting, onSwitchChanged, onActionClicked, onPickerClicked, settings)
            }
            if (setting is SettingModel.Switch && setting.isChecked && setting.revealsKey != null) {
                val subSetting = settings.find { it.key == setting.revealsKey && it is SettingModel.SubSwitch }
                subSetting?.let {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 300, delayMillis = 150)) + expandVertically(animationSpec = tween(durationMillis = 400)),
                        exit = fadeOut(animationSpec = tween(durationMillis = 200)) + shrinkVertically(animationSpec = tween(durationMillis = 300))
                    ) {
                        Box(modifier = Modifier.padding(start = 24.dp)) {
                             SettingItem(it, onSwitchChanged, onActionClicked, onPickerClicked, settings)
                        }
                    }
                }
            }
            // Simplified divider logic for clarity, you might want to refine this
            if (index < generalSettings.size - 1 || (setting is SettingModel.Switch && setting.isChecked && setting.revealsKey != null && generalSettings.getOrNull(index+1)?.key != setting.revealsKey)) {
                 HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            }
        }

        item { SettingsGroupHeader("Account & Information") }
        // Crude filtering for demo; ensure SubSwitch items are not duplicated if their parents are also in this group
        val accountSettings = settings.filterNot { it in generalSettings || (it is SettingModel.SubSwitch && it.parentKey in generalSettings.map { gs -> gs.key }) }
        itemsIndexed(accountSettings, key = { _, item -> item.key }) { index, setting ->
            AnimatedSettingItem(index = index) {
                SettingItem(setting, onSwitchChanged, onActionClicked, onPickerClicked, settings)
            }
            if (index < accountSettings.size - 1) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            }
        }
    }
}

@Composable
fun AnimatedSettingItem(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay( (index * 50L).coerceAtMost(300L) )
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 300)) + slideInHorizontally(
            initialOffsetX = { it / 2 },
            animationSpec = tween(durationMillis = 400, easing = EaseOutCubic)
        ),
        exit = fadeOut(animationSpec = tween(durationMillis = 200))
    ) {
        content()
    }
}

@Composable
fun SettingItem(
    setting: SettingModel,
    onSwitchChanged: (key: String, isChecked: Boolean) -> Unit,
    onActionClicked: (actionType: ActionType, key: String) -> Unit,
    onPickerClicked: (setting: SettingModel.Picker) -> Unit,
    allSettings: List<SettingModel>
) {
    val itemModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)

    val interactionSource = remember { MutableInteractionSource() }

    val isEffectivelyEnabled = when (setting) {
        is SettingModel.SubSwitch -> {
            val parent = allSettings.find { it.key == setting.parentKey } as? SettingModel.Switch
            (parent?.isChecked ?: false) && setting.enabled // Sub-setting also respects its own enabled flag
        }
        else -> setting.enabled
    }
    val currentContentAlpha = if (isEffectivelyEnabled) LocalContentColor.current.alpha else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f).alpha


    Row(
        modifier = itemModifier
            .clickable(
                enabled = isEffectivelyEnabled,
                onClick = {
                    when (setting) {
                        is SettingModel.Switch -> onSwitchChanged(setting.key, !setting.isChecked)
                        is SettingModel.SubSwitch -> onSwitchChanged(setting.key, !setting.isChecked)
                        is SettingModel.Action -> onActionClicked(setting.actionType, setting.key)
                        is SettingModel.Picker -> onPickerClicked(setting)
                    }
                },
                interactionSource = interactionSource,
                indication = LocalIndication.current
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = setting.icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp).padding(end = if (setting is SettingModel.SubSwitch) 8.dp else 16.dp),
            tint = if (isEffectivelyEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = currentContentAlpha)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = setting.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isEffectivelyEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = currentContentAlpha)
            )
            setting.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isEffectivelyEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = currentContentAlpha)
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        when (setting) {
            is SettingModel.Switch -> Switch(checked = setting.isChecked, onCheckedChange = { onSwitchChanged(setting.key, it) }, enabled = isEffectivelyEnabled)
            is SettingModel.SubSwitch -> Switch(checked = setting.isChecked, onCheckedChange = { onSwitchChanged(setting.key, it) }, enabled = isEffectivelyEnabled, modifier = Modifier.scale(0.9f))
            is SettingModel.Picker -> Text(setting.selectedOption, style = MaterialTheme.typography.bodyMedium, color = if (isEffectivelyEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = currentContentAlpha))
            is SettingModel.Action -> { /* Icon or chevron for actions can be added here */ }
        }
    }
}

@Composable
fun SettingsGroupHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    )
}

@Composable
fun ConfirmationDialog(
    title: String,
    text: String,
    confirmButtonText: String,
    dismissButtonText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmButtonText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissButtonText) }
        }
    )
}

@Composable
fun OptionsPickerDialog(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
                options.forEach { option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOptionSelected(option) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (option == selectedOption) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (option == selectedOption) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Text(option, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Spacer(Modifier.height(16.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

// Note: Replaced direct ContentAlpha.disabled with MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
// as ContentAlpha is more of a Material 2 concept. Material 3 handles this via component color roles.
// The alpha value 0.38f is a common disabled alpha.
