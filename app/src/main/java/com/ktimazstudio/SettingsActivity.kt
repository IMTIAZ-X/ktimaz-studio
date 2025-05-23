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
import androidx.compose.foundation.background // <<< ADDED IMPORT
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Ensure this is present
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ktimazstudio.ui.theme.ktimaz
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

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
    private val sharedPreferencesName = "app_settings_futuristic_v1"
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
        return settingsMap.toMap()
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
        val newMap = _settingsStateFlow.value.toMutableMap()
        newMap[key] = value
        _settingsStateFlow.value = newMap.toMap()
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
        _settingsStateFlow.value = defaultSettingsMap.toMap()
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
                _uiState.update { currentState ->
                    currentState.copy(
                        settingsValues = persistedValues,
                        isLoading = false,
                        isAdvancedSectionEnabled = (persistedValues["advanced_enabled"] as? SettingValue.Bool)?.value ?: false
                    )
                }
            }
        }
    }

    fun updateBooleanSetting(key: String, value: Boolean) {
        viewModelScope.launch { repository.updateSetting(key, SettingValue.Bool(value)) }
    }

    fun updateStringSetting(key: String, value: String) {
        viewModelScope.launch { repository.updateSetting(key, SettingValue.Str(value)) }
    }

    fun updateFloatSetting(key: String, value: Float) {
        viewModelScope.launch { repository.updateSetting(key, SettingValue.Flt(value)) }
    }

    fun resetAllSettings() {
        viewModelScope.launch { repository.resetToDefaults() }
    }

    fun getDynamicDescriptionForKey(key: String): String? {
        val currentTheme = (_uiState.value.settingsValues["app_theme"] as? SettingValue.Str)?.value ?: "Unknown"
        return when (key) {
            "app_theme" -> "Current: $currentTheme. Visual refresh may be needed."
            "dynamic_color_enabled" -> if ((_uiState.value.settingsValues[key] as? SettingValue.Bool)?.value == true) "Colors adapt to your wallpaper." else "Using default app palette."
            else -> null
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
        val enablesKeys: List<String>? = null,
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

enum class ActionType { RESET_PREFERENCES, ABOUT_APP, PRIVACY_POLICY }

fun createSettingDefinitions(): List<SettingModel> {
    return listOf(
        SettingModel.Switch("dark_mode", "Dark Mode", Icons.Filled.BrightnessAuto, "Appearance", defaultValue = false, summaryOff = "System or light theme", summaryOn = "Dark theme active", revealsKeys = listOf("dynamic_color_enabled")),
        SettingModel.Switch("dynamic_color_enabled", "Enable Dynamic Color", Icons.Filled.ColorLens, "Appearance", defaultValue = true, dynamicDescriptionKey = "dynamic_color_enabled"),
        SettingModel.Picker("app_theme", "App Theme Accent", Icons.Filled.Palette, "Appearance", options = listOf("System Default", "Oceanic Teal", "Forest Emerald", "Sunset Coral"), defaultValue = "System Default", dynamicDescriptionKey = "app_theme"),
        SettingModel.Slider("text_size_scale", "Interface Text Scale", Icons.Filled.TextFields, "Appearance", defaultValue = 1.0f, valueRange = 0.8f..1.6f, steps = 7, valueLabelFormat = { "${(it * 100).toInt()}%" }),
        SettingModel.Switch("notifications", "Master Notifications", Icons.Filled.NotificationsActive, "Notifications", defaultValue = true, summaryOn = "Alerts are on", summaryOff = "All notifications are off", revealsKeys = listOf("notification_vibration")),
        SettingModel.Switch("notification_vibration", "Vibration & Haptics", Icons.Filled.Vibration, "Notifications", defaultValue = true, summaryOn = "Tactile feedback enabled", summaryOff = "Tactile feedback disabled"),
        SettingModel.Switch("advanced_enabled", "Enable Developer Options", Icons.Filled.BuildCircle, "Advanced", defaultValue = false, summaryOn = "Proceed with caution", summaryOff = "Standard mode", enablesKeys = listOf("experimental_feature_x")),
        SettingModel.Switch("experimental_feature_x", "Chrono-Shift Interface", Icons.Filled.HourglassTop, "Advanced", defaultValue = false, summaryOn = "Temporal UI active (beta)", summaryOff = "Standard UI timeline"),
        SettingModel.Action("reset_prefs", "Reset All Settings", Icons.Filled.RestartAlt, "System", ActionType.RESET_PREFERENCES, summary = "Restore factory settings for this app"),
        SettingModel.Action("about_app", "About This Application", Icons.Filled.Info, "System", ActionType.ABOUT_APP), // <<< FIXED: Changed from InfoOutline
        SettingModel.Action("privacy_policy", "Privacy & Data Policy", Icons.AutoMirrored.Filled.Article, "System", ActionType.PRIVACY_POLICY)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
class SettingsActivity : ComponentActivity() {

    private val repository: SettingsRepository by lazy { SharedPreferencesSettingsRepository(applicationContext) }
    private val viewModel: SettingsViewModel by lazy { SettingsViewModel(repository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ktimaz {
                val uiState by viewModel.uiState.collectAsState()
                var showResetDialog by remember { mutableStateOf(false) }
                var showPickerKey by remember { mutableStateOf<String?>(null) }
                var showAboutDialog by remember { mutableStateOf(false) }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Setting", fontWeight = FontWeight.Normal, style = MaterialTheme.typography.titleLarge) },
                            navigationIcon = { IconButton(onClick = { onBackPressedDispatcher.onBackPressed() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.background
                ) { paddingValues ->
                    if (uiState.isLoading && uiState.settingsValues.isEmpty()) {
                        Box(Modifier.fillMaxSize().padding(paddingValues), Alignment.Center) { CircularProgressIndicator(strokeWidth = 2.dp) }
                    } else {
                        SettingsScreenContent(
                            modifier = Modifier.padding(paddingValues),
                            settingDefinitions = viewModel.settingDefinitions,
                            settingsValues = uiState.settingsValues,
                            isAdvancedSectionEnabled = uiState.isAdvancedSectionEnabled,
                            onSwitchChanged = viewModel::updateBooleanSetting,
                            onSliderChanged = viewModel::updateFloatSetting,
                            onActionClicked = { actionType, _ ->
                                when (actionType) {
                                    ActionType.RESET_PREFERENCES -> showResetDialog = true
                                    ActionType.ABOUT_APP -> showAboutDialog = true
                                    else -> { /* Handle other actions */ }
                                }
                            },
                            onPickerClicked = { key -> showPickerKey = key },
                            getDynamicDescription = viewModel::getDynamicDescriptionForKey
                        )
                    }

                    if (showResetDialog) {
                        ConfirmationDialog(
                            icon = Icons.Filled.WarningAmber, title = "Confirm System Reset",
                            text = "All application configurations will be reset to their initial state. This operation is irreversible.",
                            confirmButtonText = "Proceed Reset", dismissButtonText = "Cancel",
                            onConfirm = { viewModel.resetAllSettings(); showResetDialog = false },
                            onDismiss = { showResetDialog = false }
                        )
                    }

                    @Composable
fun AboutDialog(showDialog: Boolean, onDismissRequest: () -> Unit) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            icon = {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Ktimaz Studio Interface") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Version: 3.0", style = MaterialTheme.typography.bodyMedium)
                    Text("Codename: Project Nova Genesis", style = MaterialTheme.typography.bodyMedium)
                    Text("©2024 Rightly Now all Control by Ktimaz Studio.", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "\"Pioneering tomorrow's experiences, today.\"",
                        style = MaterialTheme.typography.labelMedium,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissRequest) {
                    Text("Acknowledge")
                }
            }
        )
    }
}

                    showPickerKey?.let { pickerKey ->
                        val pickerSetting = viewModel.settingDefinitions.find { it.key == pickerKey && it is SettingModel.Picker } as? SettingModel.Picker
                        val currentValue = (uiState.settingsValues[pickerKey] as? SettingValue.Str)?.value
                        if (pickerSetting != null && currentValue != null) {
                            OptionsPickerDialog(
                                title = "Select ${pickerSetting.title}", options = pickerSetting.options,
                                selectedOption = currentValue,
                                onOptionSelected = { selected -> viewModel.updateStringSetting(pickerKey, selected); showPickerKey = null },
                                onDismiss = { showPickerKey = null }
                            )
                        }
                    }
                }
            }
        }
    }
}

val Material3EaseOutQuint = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)

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
        contentPadding = PaddingValues(bottom = 32.dp, top = 12.dp)
    ) {
        groupedSettings.forEach { (category, settingsInCategory) ->
            item(key = "header_$category", contentType = "header") {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(400, easing = LinearOutSlowInEasing)) + slideInVertically(tween(500, easing = Material3EaseOutQuint)) { -it / 3 }
                ) {
                    SettingsGroupHeader(category)
                }
            }
            itemsIndexed(settingsInCategory, key = { _, item -> item.key }, contentType = { _, item -> item::class.simpleName ?: "" }) { index, settingDefinition ->
                val isEffectivelyEnabled = when (settingDefinition.key) {
                    "experimental_feature_x" -> isAdvancedSectionEnabled && (settingsValues["advanced_enabled"] as? SettingValue.Bool)?.value == true
                    "dynamic_color_enabled" -> (settingsValues["dark_mode"] as? SettingValue.Bool)?.value == true
                    "notification_vibration" -> (settingsValues["notifications"] as? SettingValue.Bool)?.value == true
                    else -> true
                }
                val isHiddenByParent = settingDefinitions.any { parentDef ->
                    parentDef is SettingModel.Switch &&
                    parentDef.revealsKeys?.contains(settingDefinition.key) == true &&
                    (settingsValues[parentDef.key] as? SettingValue.Bool)?.value == false
                }

                AnimatedVisibility(
                    visible = !isHiddenByParent,
                    enter = fadeIn(tween(400, delayMillis = 50, easing = LinearOutSlowInEasing)) + expandVertically(tween(500, delayMillis = 50, easing = Material3EaseOutQuint)),
                    exit = fadeOut(tween(300, easing = FastOutLinearInEasing)) + shrinkVertically(tween(400, easing = FastOutLinearInEasing)),
                ) {
                     AnimatedSettingItem(indexInList = index) {
                        SettingItem(
                            settingDefinition = settingDefinition,
                            settingsValues = settingsValues,
                            isEffectivelyEnabled = isEffectivelyEnabled,
                            onSwitchChanged = onSwitchChanged,
                            onSliderChanged = onSliderChanged,
                            onActionClicked = onActionClicked,
                            onPickerClicked = onPickerClicked,
                            getDynamicDescription = getDynamicDescription
                        )
                    }
                }
                val nextIndex = index + 1
                val isNextItemHiddenByParent = if (nextIndex < settingsInCategory.size) {
                    settingDefinitions.any { parentDef ->
                        parentDef is SettingModel.Switch &&
                        parentDef.revealsKeys?.contains(settingsInCategory[nextIndex].key) == true &&
                        (settingsValues[parentDef.key] as? SettingValue.Bool)?.value == false
                    }
                } else { true }

                if (index < settingsInCategory.size - 1 && !isHiddenByParent && !isNextItemHiddenByParent) {
                     Spacer(modifier = Modifier.height(0.5.dp).fillMaxWidth().background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)).padding(horizontal = 20.dp) ) // <<< FIXED Modifier.background
                }
            }
        }
    }
}

@Composable
fun AnimatedSettingItem(indexInList: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((indexInList * 80L).coerceAtMost(450L))
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing)) +
                slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(durationMillis = 600, easing = Material3EaseOutQuint)
                ),
        exit = fadeOut(animationSpec = tween(durationMillis = 300, easing = FastOutLinearInEasing))
    ) {
        content()
    }
}

@Composable
fun SettingItem(
    settingDefinition: SettingModel,
    settingsValues: Map<String, SettingValue>,
    isEffectivelyEnabled: Boolean,
    onSwitchChanged: (key: String, isChecked: Boolean) -> Unit,
    onSliderChanged: (key: String, value: Float) -> Unit,
    onActionClicked: (actionType: ActionType, key: String) -> Unit,
    onPickerClicked: (key: String) -> Unit,
    getDynamicDescription: (key: String) -> String?
) {
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current

    val currentBoolValue = (settingsValues[settingDefinition.key] as? SettingValue.Bool)?.value
    val currentStringValue = (settingsValues[settingDefinition.key] as? SettingValue.Str)?.value
    val currentFloatValue = (settingsValues[settingDefinition.key] as? SettingValue.Flt)?.value

    var summaryTextToDisplay: String? = null
    if (settingDefinition is SettingModel.Switch) {
        summaryTextToDisplay = if (currentBoolValue == true) settingDefinition.summaryOn else settingDefinition.summaryOff
    } else if (settingDefinition is SettingModel.Action) {
        summaryTextToDisplay = settingDefinition.summary
    }
    settingDefinition.dynamicDescriptionKey?.let { dynamicKey ->
        val dynamicDesc = getDynamicDescription(dynamicKey)
        summaryTextToDisplay = dynamicDesc ?: summaryTextToDisplay
    }
    if (summaryTextToDisplay == null) {
        if (settingDefinition is SettingModel.Picker && currentStringValue != null) summaryTextToDisplay = "Selected: $currentStringValue"
        else if (settingDefinition is SettingModel.Slider && currentFloatValue != null) summaryTextToDisplay = "Value: ${settingDefinition.valueLabelFormat(currentFloatValue)}"
    }

    val itemAlpha = if (isEffectivelyEnabled) 1f else 0.45f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(itemAlpha)
            .clickable(
                enabled = isEffectivelyEnabled && settingDefinition !is SettingModel.Slider,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
            .padding(horizontal = 22.dp, vertical = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = settingDefinition.icon,
                contentDescription = settingDefinition.title,
                modifier = Modifier.size(24.dp),
                tint = if (isEffectivelyEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Spacer(Modifier.width(22.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = settingDefinition.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal),
                    color = if (isEffectivelyEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                AnimatedContent(
                    targetState = summaryTextToDisplay,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(250, delayMillis = 150, easing = LinearOutSlowInEasing)) +
                         slideInVertically(animationSpec = tween(350, easing = Material3EaseOutQuint)) { it / 2} ).togetherWith(
                            fadeOut(animationSpec = tween(200, easing = FastOutLinearInEasing))
                        )
                    }, label = "summaryAnimation"
                ) { text ->
                    if (!text.isNullOrEmpty()) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isEffectivelyEnabled) 0.9f else 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.width(18.dp))

            when (settingDefinition) {
                is SettingModel.Switch -> {
                    val checkedValue = currentBoolValue ?: settingDefinition.defaultValue
                    Switch(
                        checked = checkedValue,
                        onCheckedChange = { newCheckedState ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSwitchChanged(settingDefinition.key, newCheckedState)
                        },
                        enabled = isEffectivelyEnabled,
                        thumbContent = if (checkedValue) { { Icon(Icons.Filled.Check, "Enabled", modifier = Modifier.size(SwitchDefaults.IconSize)) } } else null,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledCheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f), // <<< FIXED ContentAlpha
                            disabledCheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            disabledUncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f), // <<< FIXED ContentAlpha
                            disabledUncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    )
                }
                is SettingModel.Picker -> currentStringValue?.let {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(IntrinsicSize.Min)) {
                        Text(it, style = MaterialTheme.typography.labelLarge, color = if (isEffectivelyEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant)
                        Icon(Icons.Filled.ArrowRight, "Open picker", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
                    }
                }
                is SettingModel.Action -> if (settingDefinition.actionType != ActionType.RESET_PREFERENCES) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Details", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> {}
            }
        }

        if (settingDefinition is SettingModel.Slider) {
            val sliderValue = currentFloatValue ?: settingDefinition.defaultValue
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                 Text(
                    text = settingDefinition.valueLabelFormat(settingDefinition.valueRange.start),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if(isEffectivelyEnabled) 0.7f else 0.4f),
                    modifier = Modifier.padding(end = 8.dp)
                )
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
                        inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        activeTickColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                        inactiveTickColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                )
                 Text(
                    text = settingDefinition.valueLabelFormat(settingDefinition.valueRange.endInclusive),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if(isEffectivelyEnabled) 0.7f else 0.4f),
                    modifier = Modifier.padding(start = 8.dp)
                )
                Text(
                    text = settingDefinition.valueLabelFormat(sliderValue),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isEffectivelyEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(60.dp).padding(start = 10.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsGroupHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp),
        color = MaterialTheme.colorScheme.tertiary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 22.dp, top = 30.dp, bottom = 14.dp)
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
        icon = { Icon(icon, null, tint = MaterialTheme.colorScheme.error) },
        title = { Text(title, style = MaterialTheme.typography.headlineSmall) },
        text = { Text(text, style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp) },
        confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text(confirmButtonText) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissButtonText) } }
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
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Column {
                Text(
                    title, style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 22.dp, bottom = 16.dp)
                )
                LazyColumn(modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth()) {
                    items(
                        items = options,
                        key = { it }
                    ) { option: String ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.large)
                                .clickable { onOptionSelected(option) }
                                .padding(vertical = 16.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (option == selectedOption) Icons.Filled.CheckCircleOutline else Icons.Filled.RadioButtonUnchecked,
                                null,
                                tint = if (option == selectedOption) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 18.dp).size(24.dp)
                            )
                            Text(option, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Dismiss", fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}
