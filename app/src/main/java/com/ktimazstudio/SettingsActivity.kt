package com.ktimazstudio

import android.content.Context
import android.os.Bundle
import android.util.Log // Import for logging
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType // For haptics
import androidx.compose.ui.platform.LocalHapticFeedback // For haptics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ktimazstudio.ui.theme.ktimaz // Your app's theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Lifecycle KTX for viewModelScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

// Material Icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*


// --- 1. Persistence Layer (SharedPreferences) ---

sealed class SettingValue {
    data class Bool(val value: Boolean) : SettingValue()
    data class Str(val value: String) : SettingValue()
    data class Flt(val value: Float) : SettingValue()
}

interface SettingsRepository {
    fun getSettingsFlow(): Flow<Map<String, SettingValue>>
    suspend fun updateSetting(key: String, value: SettingValue)
    suspend fun resetToDefaults()
}

class SharedPreferencesSettingsRepository(private val context: Context) : SettingsRepository {
    private val sharedPreferencesName = "app_settings_main_v2" // Changed name to ensure fresh start if needed
    private val sharedPreferences = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)

    private val _settingsStateFlow = MutableStateFlow<Map<String, SettingValue>>(loadSettingsFromPrefs())

    private fun loadSettingsFromPrefs(): Map<String, SettingValue> {
        val settingsMap = mutableMapOf<String, SettingValue>()
        val allSettingDefs = createSettingDefinitions()

        allSettingDefs.forEach { def ->
            when (def) {
                is SettingModel.Switch -> settingsMap[def.key] = SettingValue.Bool(sharedPreferences.getBoolean(def.key, def.defaultValue))
                is SettingModel.Picker -> settingsMap[def.key] = SettingValue.Str(sharedPreferences.getString(def.key, def.defaultValue) ?: def.defaultValue)
                is SettingModel.Slider -> settingsMap[def.key] = SettingValue.Flt(sharedPreferences.getFloat(def.key, def.defaultValue))
                is SettingModel.Action -> { /* No persistent value */ }
            }
        }
        // Log.d("SettingsRepo", "Initial load from Prefs: $settingsMap")
        return settingsMap.toMap() // Ensure it's an immutable map
    }

    override fun getSettingsFlow(): Flow<Map<String, SettingValue>> = _settingsStateFlow.asStateFlow()

    override suspend fun updateSetting(key: String, value: SettingValue) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().apply {
                when (value) {
                    is SettingValue.Bool -> putBoolean(key, value.value)
                    is SettingValue.Str -> putString(key, value.value)
                    is SettingValue.Flt -> putFloat(key, value.value)
                }
                apply()
            }
        }
        // CRITICAL: Emit a new map instance to trigger StateFlow update
        val newMap = _settingsStateFlow.value.toMutableMap()
        newMap[key] = value
        _settingsStateFlow.value = newMap.toMap() // This creates a new immutable map
        // Log.d("SettingsRepo", "Updated key '$key' to $value. New map emitted via StateFlow.")
    }

    override suspend fun resetToDefaults() {
        val defaultSettingsMap = mutableMapOf<String, SettingValue>()
        createSettingDefinitions().forEach { def ->
            when (def) {
                is SettingModel.Switch -> defaultSettingsMap[def.key] = SettingValue.Bool(def.defaultValue)
                is SettingModel.Picker -> defaultSettingsMap[def.key] = SettingValue.Str(def.defaultValue)
                is SettingModel.Slider -> defaultSettingsMap[def.key] = SettingValue.Flt(def.defaultValue)
                else -> {}
            }
        }

        withContext(Dispatchers.IO) {
            val editor = sharedPreferences.edit()
            defaultSettingsMap.forEach { (key, settingValue) ->
                when (settingValue) {
                    is SettingValue.Bool -> editor.putBoolean(key, settingValue.value)
                    is SettingValue.Str -> editor.putString(key, settingValue.value)
                    is SettingValue.Flt -> editor.putFloat(key, settingValue.value)
                }
            }
            editor.apply()
        }
        _settingsStateFlow.value = defaultSettingsMap.toMap() // Emit the complete map of defaults
        // Log.d("SettingsRepo", "Reset to defaults. New map emitted: ${_settingsStateFlow.value}")
    }
}


// --- 2. ViewModel Layer ---
class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val settingDefinitions: List<SettingModel> = createSettingDefinitions()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            repository.getSettingsFlow().collect { persistedValues ->
                // Log.d("SettingsViewModel", "Collected from repo: $persistedValues")
                _uiState.update { currentState ->
                    currentState.copy(
                        settingsValues = persistedValues, // This must be a new map instance from repo
                        isLoading = false,
                        isAdvancedSectionEnabled = (persistedValues["advanced_enabled"] as? SettingValue.Bool)?.value ?: false
                    )
                }
            }
        }
    }

    fun updateBooleanSetting(key: String, value: Boolean) {
        // Log.d("SettingsViewModel", "Updating boolean setting: $key to $value")
        viewModelScope.launch { repository.updateSetting(key, SettingValue.Bool(value)) }
    }

    fun updateStringSetting(key: String, value: String) {
        // Log.d("SettingsViewModel", "Updating string setting: $key to $value")
        viewModelScope.launch { repository.updateSetting(key, SettingValue.Str(value)) }
    }

    fun updateFloatSetting(key: String, value: Float) {
        // Log.d("SettingsViewModel", "Updating float setting: $key to $value")
        viewModelScope.launch { repository.updateSetting(key, SettingValue.Flt(value)) }
    }

    fun resetAllSettings() {
        // Log.d("SettingsViewModel", "Resetting all settings")
        viewModelScope.launch { repository.resetToDefaults() }
    }

    fun getDynamicDescriptionForKey(key: String): String? {
        return if (key == "app_theme") {
            val currentTheme = (_uiState.value.settingsValues["app_theme"] as? SettingValue.Str)?.value
            "Current: $currentTheme"
        } else {
            null
        }
    }
}

data class SettingsUiState(
    val settingsValues: Map<String, SettingValue> = emptyMap(),
    val isLoading: Boolean = true,
    val isAdvancedSectionEnabled: Boolean = false
)

// --- 3. UI Layer (Activity, Composables, Setting Models) ---

sealed class SettingModel(
    open val key: String,
    open val title: String,
    open val icon: ImageVector,
    open val category: String,
    open val dynamicDescriptionKey: String? = null
) {
    data class Switch(
        override val key: String, override val title: String, override val icon: ImageVector, override val category: String,
        val defaultValue: Boolean,
        val summaryOff: String? = null, val summaryOn: String? = null,
        val revealsKeys: List<String>? = null,
        val enablesKeys: List<String>? = null, // For future use if one switch enables/disables another directly
        override val dynamicDescriptionKey: String? = null
    ) : SettingModel(key, title, icon, category, dynamicDescriptionKey)

    data class Action(
        override val key: String, override val title: String, override val icon: ImageVector, override val category: String,
        val actionType: ActionType,
        val summary: String? = null,
        override val dynamicDescriptionKey: String? = null
    ) : SettingModel(key, title, icon, category, dynamicDescriptionKey)

    data class Picker(
        override val key: String, override val title: String, override val icon: ImageVector, override val category: String,
        val options: List<String>,
        val defaultValue: String,
        override val dynamicDescriptionKey: String? = null
    ) : SettingModel(key, title, icon, category, dynamicDescriptionKey)

    data class Slider(
        override val key: String, override val title: String, override val icon: ImageVector, override val category: String,
        val defaultValue: Float,
        val valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        val steps: Int = 0,
        val valueLabelFormat: (Float) -> String = { "%.1f".format(it) },
        override val dynamicDescriptionKey: String? = null
    ) : SettingModel(key, title, icon, category, dynamicDescriptionKey)
}

enum class ActionType { RESET_PREFERENCES, VIEW_PROFILE, LOG_OUT, PRIVACY_POLICY, ABOUT_APP }

// Centralized definition of all settings structure
fun createSettingDefinitions(): List<SettingModel> {
    return listOf(
        SettingModel.Switch("dark_mode", "Dark Mode", Icons.Filled.BrightnessMedium, "Appearance", defaultValue = false, summaryOff = "Light theme", summaryOn = "Dark theme", revealsKeys = listOf("custom_dark_theme_color")),
        SettingModel.Switch("custom_dark_theme_color", "Use Custom Accent", Icons.Filled.ColorLens, "Appearance", defaultValue = false, summaryOn = "Custom accent active", summaryOff = "Default accent"),
        SettingModel.Picker("app_theme", "App Theme", Icons.Filled.Palette, "Appearance", options = listOf("System Default", "Ocean Blue", "Forest Green", "Sunset Purple"), defaultValue = "System Default", dynamicDescriptionKey = "app_theme"),
        SettingModel.Slider("text_size_scale", "Text Size", Icons.Filled.FormatSize, "Appearance", defaultValue = 1.0f, valueRange = 0.8f..1.5f, steps = 6, valueLabelFormat = { "${(it * 100).toInt()}%" }),

        SettingModel.Switch("notifications", "Enable Notifications", Icons.Filled.Notifications, "Notifications", defaultValue = true, summaryOn = "Alerts are on", summaryOff = "Alerts are off", revealsKeys = listOf("notification_vibration")),
        SettingModel.Switch("notification_vibration", "Vibrate for Notifications", Icons.Filled.Vibration, "Notifications", defaultValue = true, summaryOn = "Haptics enabled", summaryOff = "Haptics disabled"),

        SettingModel.Switch("advanced_enabled", "Enable Advanced Options", Icons.Filled.Tune, "Advanced", defaultValue = false, summaryOn = "Unlocked", summaryOff = "Locked", enablesKeys = listOf("experimental_feature_x")),
        SettingModel.Switch("experimental_feature_x", "Quantum Entanglement Sync", Icons.Filled.Science, "Advanced", defaultValue = false, summaryOn = "Q-Sync active (highly experimental!)", summaryOff = "Q-Sync inactive"),

        SettingModel.Action("reset_prefs", "Reset All Settings", Icons.Filled.RestartAlt, "System", ActionType.RESET_PREFERENCES, summary = "Revert to application defaults"),
        SettingModel.Action("about_app", "About App", Icons.Filled.Info, "System", ActionType.ABOUT_APP),
        SettingModel.Action("privacy_policy", "Privacy Policy", Icons.AutoMirrored.Filled.Article, "System", ActionType.PRIVACY_POLICY)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
class SettingsActivity : ComponentActivity() {

    private val repository: SettingsRepository by lazy {
        SharedPreferencesSettingsRepository(applicationContext)
    }
    private val viewModel: SettingsViewModel by lazy {
        SettingsViewModel(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ktimaz {
                val uiState by viewModel.uiState.collectAsState()
                // Local UI states for dialogs are managed within the Activity/Screen that shows them
                var showResetDialog by remember { mutableStateOf(false) }
                var showPickerKey by remember { mutableStateOf<String?>(null) }

                // Log uiState changes in Compose
                // LaunchedEffect(uiState.settingsValues) {
                //     Log.d("SettingsActivityUI", "settingsValues recomposed: ${uiState.settingsValues}")
                // }


                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Settings ✨", fontWeight = FontWeight.Medium) }, // Added an emoji for fun
                            navigationIcon = { IconButton(onClick = { onBackPressedDispatcher.onBackPressed() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.background // Use theme's background
                ) { paddingValues ->
                    // Show loading indicator only if settings are truly not yet available
                    if (uiState.isLoading && uiState.settingsValues.isEmpty()) {
                        Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) { CircularProgressIndicator() }
                    } else {
                        SettingsScreenContent(
                            modifier = Modifier.padding(paddingValues),
                            settingDefinitions = viewModel.settingDefinitions,
                            settingsValues = uiState.settingsValues,
                            isAdvancedSectionEnabled = uiState.isAdvancedSectionEnabled, // Pass derived state
                            onSwitchChanged = viewModel::updateBooleanSetting,
                            onSliderChanged = viewModel::updateFloatSetting,
                            onActionClicked = { actionType, _ ->
                                if (actionType == ActionType.RESET_PREFERENCES) showResetDialog = true
                                // else: handle other actions (e.g., navigation to other screens)
                            },
                            onPickerClicked = { key -> showPickerKey = key },
                            getDynamicDescription = viewModel::getDynamicDescriptionForKey
                        )
                    }

                    if (showResetDialog) {
                        ConfirmationDialog(
                            icon = Icons.Filled.RestartAlt, title = "Reset Preferences?",
                            text = "All settings will revert to their original defaults. This cannot be undone.",
                            confirmButtonText = "Reset Now", dismissButtonText = "Cancel",
                            onConfirm = {
                                viewModel.resetAllSettings() // ViewModel handles resetting via repository
                                showResetDialog = false
                            },
                            onDismiss = { showResetDialog = false }
                        )
                    }

                    showPickerKey?.let { pickerKey ->
                        val pickerSetting = viewModel.settingDefinitions.find { it.key == pickerKey && it is SettingModel.Picker } as? SettingModel.Picker
                        val currentValue = (uiState.settingsValues[pickerKey] as? SettingValue.Str)?.value
                        if (pickerSetting != null && currentValue != null) {
                            OptionsPickerDialog(
                                title = "Select ${pickerSetting.title}", options = pickerSetting.options,
                                selectedOption = currentValue,
                                onOptionSelected = { selected ->
                                    viewModel.updateStringSetting(pickerKey, selected)
                                    showPickerKey = null // Close dialog
                                },
                                onDismiss = { showPickerKey = null } // Close dialog
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- UI Composables (largely same, with minor tweaks for clarity and haptics) ---

val Material3EaseOutExpo = CubicBezierEasing(0.16f, 1f, 0.3f, 1f) // Material 3 standard expressive curve

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    settingDefinitions: List<SettingModel>,
    settingsValues: Map<String, SettingValue>,
    isAdvancedSectionEnabled: Boolean,
    onSwitchChanged: (key: String, isChecked: Boolean) -> Unit,
    onSliderChanged: (key: String, value: Float) -> Unit,
    onActionClicked: (actionType: ActionType, key: String) -> Unit,
    onPickerClicked: (key: String) -> Unit,
    getDynamicDescription: (key: String) -> String?
) {
    val groupedSettings = settingDefinitions.groupBy { it.category }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp) // More bottom padding
    ) {
        groupedSettings.forEach { (category, settingsInCategory) ->
            item(key = "header_$category") {
                SettingsGroupHeader(category)
            }
            itemsIndexed(settingsInCategory, key = { _, item -> item.key }) { index, settingDefinition ->
                val isEffectivelyEnabled = when (settingDefinition.key) {
                    "experimental_feature_x" -> isAdvancedSectionEnabled // This item depends on the "advanced_enabled" switch
                    // Add more complex enablement logic here if needed for other settings
                    else -> true // Default to true
                }
                // Determine if the setting itself is a child that should be revealed by a parent switch
                val isHiddenChild = settingDefinitions.any { parentDef ->
                    parentDef is SettingModel.Switch && // Parent must be a switch
                    parentDef.revealsKeys?.contains(settingDefinition.key) == true && // This setting is in parent's reveal list
                    (settingsValues[parentDef.key] as? SettingValue.Bool)?.value == false // Parent switch is OFF
                }

                AnimatedVisibility(
                    visible = !isHiddenChild, // Show if not a hidden child
                    enter = fadeIn(tween(350, easing = LinearOutSlowInEasing)) + expandVertically(tween(450, easing = Material3EaseOutExpo)),
                    exit = fadeOut(tween(250, easing = FastOutLinearInEasing)) + shrinkVertically(tween(350, easing = FastOutLinearInEasing)),
                ) {
                     AnimatedSettingItem(indexInList = index) {
                        SettingItem(
                            settingDefinition = settingDefinition,
                            settingsValues = settingsValues,
                            isEffectivelyEnabled = isEffectivelyEnabled, // Pass calculated enabled state
                            onSwitchChanged = onSwitchChanged,
                            onSliderChanged = onSliderChanged,
                            onActionClicked = onActionClicked,
                            onPickerClicked = onPickerClicked,
                            getDynamicDescription = getDynamicDescription
                        )
                    }
                }
                 // Add divider unless it's the last item in the category OR if it's hidden (to avoid double dividers if next item is also hidden)
                if (index < settingsInCategory.size - 1 && !isHiddenChild) {
                    val nextItemIsAlsoHidden = (index + 1 < settingsInCategory.size) && settingDefinitions.any { parentDef ->
                        parentDef is SettingModel.Switch &&
                        parentDef.revealsKeys?.contains(settingsInCategory[index + 1].key) == true &&
                        (settingsValues[parentDef.key] as? SettingValue.Bool)?.value == false
                    }
                    if(!nextItemIsAlsoHidden) { // Avoid divider if next item is a hidden child that won't show
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), // Softer divider
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedSettingItem(indexInList: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        // Staggered delay, more pronounced for early items, caps for later ones
        delay((indexInList * 70L).coerceAtMost(400L))
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 450, easing = LinearOutSlowInEasing)) +
                slideInVertically(
                    initialOffsetY = { it / 5 }, // Slightly more slide
                    animationSpec = tween(durationMillis = 550, easing = Material3EaseOutExpo)
                ),
        exit = fadeOut(animationSpec = tween(durationMillis = 250, easing = FastOutLinearInEasing))
    ) {
        content()
    }
}

@Composable
fun SettingItem(
    settingDefinition: SettingModel,
    settingsValues: Map<String, SettingValue>,
    isEffectivelyEnabled: Boolean, // Use this passed-in value
    onSwitchChanged: (key: String, isChecked: Boolean) -> Unit,
    onSliderChanged: (key: String, value: Float) -> Unit,
    onActionClicked: (actionType: ActionType, key: String) -> Unit,
    onPickerClicked: (key: String) -> Unit,
    getDynamicDescription: (key: String) -> String?
) {
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current

    // Derive current value for this specific setting from the map
    val currentBoolValue = (settingsValues[settingDefinition.key] as? SettingValue.Bool)?.value
    val currentStringValue = (settingsValues[settingDefinition.key] as? SettingValue.Str)?.value
    val currentFloatValue = (settingsValues[settingDefinition.key] as? SettingValue.Flt)?.value

    // Log to see if this SettingItem is getting the correct current value
    // LaunchedEffect(currentBoolValue, currentStringValue, currentFloatValue) {
    //    Log.d("SettingItemUI", "Key '${settingDefinition.key}' recomposed with Bool: $currentBoolValue, Str: $currentStringValue, Flt: $currentFloatValue, Enabled: $isEffectivelyEnabled")
    // }


    var summaryText: String? = null
    if (settingDefinition is SettingModel.Switch) {
        summaryText = if (currentBoolValue == true) settingDefinition.summaryOn else settingDefinition.summaryOff
    } else if (settingDefinition is SettingModel.Action) {
        summaryText = settingDefinition.summary
    }
    settingDefinition.dynamicDescriptionKey?.let {
        summaryText = getDynamicDescription(it) ?: summaryText
    }

    val itemAlpha = if (isEffectivelyEnabled) 1f else 0.5f // More pronounced disabled state visually

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(itemAlpha) // Apply alpha for disabled state to the whole item
            .clickable(
                enabled = isEffectivelyEnabled && settingDefinition !is SettingModel.Slider,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) // Subtle haptic
                    when (settingDefinition) {
                        is SettingModel.Switch -> currentBoolValue?.let { onSwitchChanged(settingDefinition.key, !it) }
                        is SettingModel.Action -> onActionClicked(settingDefinition.actionType, settingDefinition.key)
                        is SettingModel.Picker -> onPickerClicked(settingDefinition.key)
                        else -> {}
                    }
                },
                interactionSource = interactionSource,
                indication = LocalIndication.current
            )
            .padding(horizontal = 20.dp, vertical = 18.dp) // Slightly more vertical padding
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = settingDefinition.icon,
                contentDescription = settingDefinition.title, // Better accessibility
                modifier = Modifier.size(24.dp),
                tint = if (isEffectivelyEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = settingDefinition.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isEffectivelyEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                AnimatedContent(
                    targetState = summaryText,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220, delayMillis = 120, easing = LinearOutSlowInEasing)) +
                         slideInVertically(animationSpec = tween(330, easing = Material3EaseOutExpo)) { it / 2} ).togetherWith(
                            fadeOut(animationSpec = tween(180, easing = FastOutLinearInEasing))
                        )
                    }, label = "summaryAnimation"
                ) { text ->
                    if (!text.isNullOrEmpty()) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 3.dp) // Slightly more space
                        )
                    }
                }
            }
            Spacer(Modifier.width(16.dp))

            when (settingDefinition) {
                is SettingModel.Switch -> {
                    // Ensure currentBoolValue is not null before passing to Switch
                    // If it's null, it means data isn't loaded for this key, use default or a placeholder
                    val checkedValue = currentBoolValue ?: settingDefinition.defaultValue
                    Switch(
                        checked = checkedValue,
                        onCheckedChange = { newCheckedState ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress) // Different haptic for direct switch
                            onSwitchChanged(settingDefinition.key, newCheckedState)
                        },
                        enabled = isEffectivelyEnabled,
                        thumbContent = if (checkedValue) { { Icon(Icons.Filled.Done, "Enabled", modifier = Modifier.size(SwitchDefaults.IconSize)) } } else null
                    )
                }
                is SettingModel.Picker -> currentStringValue?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = if (isEffectivelyEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant)
                        Icon(Icons.Filled.ArrowDropDown, "Open picker", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                is SettingModel.Action -> if (settingDefinition.actionType != ActionType.RESET_PREFERENCES) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Perform action", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> {}
            }
        }

        if (settingDefinition is SettingModel.Slider) {
            // Ensure currentFloatValue is not null, use default if not found (though it should be from repo)
            val sliderValue = currentFloatValue ?: settingDefinition.defaultValue
            Spacer(Modifier.height(10.dp)) // More space for slider
            Row(verticalAlignment = Alignment.CenterVertically) {
                Slider(
                    value = sliderValue,
                    onValueChange = { newValue -> onSliderChanged(settingDefinition.key, newValue) },
                    onValueChangeFinished = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) },
                    valueRange = settingDefinition.valueRange,
                    steps = settingDefinition.steps,
                    enabled = isEffectivelyEnabled,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), // Softer inactive
                        disabledThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                )
                Spacer(Modifier.width(18.dp))
                Text(
                    text = settingDefinition.valueLabelFormat(sliderValue),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isEffectivelyEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(55.dp) // Wider for "100%"
                )
            }
        }
    }
}

@Composable
fun SettingsGroupHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp), // Bolder
        color = MaterialTheme.colorScheme.primary, // Or secondary for less emphasis
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 12.dp) // Adjusted padding
    )
}

@Composable
fun ConfirmationDialog(
    icon: ImageVector, title: String, text: String,
    confirmButtonText: String, dismissButtonText: String,
    onConfirm: () -> Unit, onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(icon, null, tint = MaterialTheme.colorScheme.secondary) }, // Different color for icon
        title = { Text(title, style = MaterialTheme.typography.headlineSmall) },
        text = { Text(text, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = { Button(onClick = onConfirm) { Text(confirmButtonText) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissButtonText) } } // TextButton for dismiss
    )
}

@Composable
fun OptionsPickerDialog(
    title: String, options: List<String>, selectedOption: String,
    onOptionSelected: (String) -> Unit, onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
            tonalElevation = 6.dp
        ) {
            Column { // Allow vertical scrolling if many options
                Text(
                    title, style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp)
                )
                LazyColumn(modifier = Modifier.padding(horizontal = 12.dp)) { // Use LazyColumn for options
                    items(options) { option ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { onOptionSelected(option) }
                                .padding(vertical = 14.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (option == selectedOption) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                                null,
                                tint = if (option == selectedOption) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 16.dp).size(24.dp)
                            )
                            Text(option, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 16.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                }
            }
        }
    }
}
