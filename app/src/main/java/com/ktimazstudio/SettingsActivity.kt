package com.ktimazstudio

// General Compose imports
import android.os.Bundle
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ktimazstudio.ui.theme.ktimaz // Your app's theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Material Icons (ensure 'androidx.compose.material:material-icons-extended' dependency)
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Notes // Example for dynamic description
import androidx.compose.material.icons.filled.* // Includes common icons like AccountCircle, Notifications, Info, etc.

// --- 1. Persistence Layer (Conceptual: Repository & Data Models) ---

// Represents different types of settings data to be persisted
sealed class SettingValue {
    data class Bool(val value: Boolean) : SettingValue()
    data class Str(val value: String) : SettingValue()
    data class Flt(val value: Float) : SettingValue()
}

// Interface for our settings data source (e.g., DataStore)
interface SettingsRepository {
    fun getSettingsFlow(): Flow<Map<String, SettingValue>>
    suspend fun updateSetting(key: String, value: SettingValue)
}

// Mock implementation for demonstration
class MockSettingsRepository : SettingsRepository {
    private val initialMockData = mutableMapOf(
        "dark_mode" to SettingValue.Bool(false),
        "custom_dark_theme_color" to SettingValue.Bool(false),
        "notifications" to SettingValue.Bool(true),
        "notification_vibration" to SettingValue.Bool(true),
        "app_theme" to SettingValue.Str("System Default"),
        "text_size_scale" to SettingValue.Flt(1.0f), // New slider setting
        "advanced_enabled" to SettingValue.Bool(false), // Enables a group of settings
        "experimental_feature_x" to SettingValue.Bool(false) // Dependent on 'advanced_enabled'
        // ... add initial defaults for all settings
    )
    private val settingsFlow = MutableStateFlow(initialMockData.toMap()) // Expose as immutable map

    override fun getSettingsFlow(): Flow<Map<String, SettingValue>> = settingsFlow.asStateFlow()

    override suspend fun updateSetting(key: String, value: SettingValue) {
        initialMockData[key] = value
        settingsFlow.value = initialMockData.toMap() // Emit new state
        // In a real app: dataStore.edit { preferences -> preferences[dataStoreKey(key)] = ... }
        println("MockSettingsRepository: Updated $key to $value")
    }
}


// --- 2. ViewModel Layer ---
class SettingsViewModel(private val repository: SettingsRepository) : androidx.lifecycle.ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // This defines the structure and default visual properties of settings items
    // The actual values (isChecked, selectedOption, sliderValue) come from _uiState.settingsValues
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
                        // Update derived states if necessary
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

    // Example of more complex logic derived from settings
    fun getDynamicDescriptionForKey(key: String): String? {
        return if (key == "app_theme") {
            val currentTheme = (_uiState.value.settingsValues["app_theme"] as? SettingValue.Str)?.value
            "Current selection: $currentTheme. Changes apply on app restart."
        } else {
            null
        }
    }
}

data class SettingsUiState(
    val settingsValues: Map<String, SettingValue> = emptyMap(),
    val isLoading: Boolean = true,
    val showResetDialog: Boolean = false,
    val showThemePickerDialog: String? = null, // Holds key of picker if dialog is shown
    val isAdvancedSectionEnabled: Boolean = false // Example derived state
)

// --- 3. UI Layer (Activity, Composables, Setting Models) ---

// UI Models for settings (define structure, icons, titles, etc.)
sealed class SettingModel(
    open val key: String,
    open val title: String,
    open val icon: ImageVector,
    open val category: String, // For grouping
    open val dynamicDescriptionKey: String? = null // Key for ViewModel to provide dynamic description
) {
    data class Switch(
        override val key: String, override val title: String, override val icon: ImageVector, override val category: String,
        val summaryOff: String? = null, val summaryOn: String? = null,
        val revealsKeys: List<String>? = null, // Keys of sub-settings to reveal
        val enablesKeys: List<String>? = null,  // Keys of settings this one enables/disables
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
        override val dynamicDescriptionKey: String? = null
    ) : SettingModel(key, title, icon, category, dynamicDescriptionKey)

    data class Slider( // New setting type
        override val key: String, override val title: String, override val icon: ImageVector, override val category: String,
        val valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        val steps: Int = 0, // 0 for continuous
        val valueLabelFormat: (Float) -> String = { "%.1f".format(it) },
        override val dynamicDescriptionKey: String? = null
    ) : SettingModel(key, title, icon, category, dynamicDescriptionKey)

    // SubSwitch might not be needed if revealsKeys logic is robust
}

enum class ActionType { RESET_PREFERENCES, VIEW_PROFILE, LOG_OUT, PRIVACY_POLICY, ABOUT_APP }

// Centralized definition of all settings structure
fun createSettingDefinitions(): List<SettingModel> {
    return listOf(
        // General Category
        SettingModel.Switch("dark_mode", "Dark Mode", Icons.Filled.BrightnessMedium, "General", summaryOff = "Light theme active", summaryOn = "Dark theme active", revealsKeys = listOf("custom_dark_theme_color")),
        SettingModel.Switch("custom_dark_theme_color", "Use Custom Accent", Icons.Filled.ColorLens, "General", summaryOn = "Using custom accent in dark mode", summaryOff = "Using default accent in dark mode"),
        SettingModel.Switch("notifications", "Enable Notifications", Icons.Filled.Notifications, "General", summaryOn = "You will receive alerts", summaryOff = "Notifications are off", revealsKeys = listOf("notification_vibration")),
        SettingModel.Switch("notification_vibration", "Vibrate for Notifications", Icons.Filled.Vibration, "General", summaryOn = "Haptic feedback enabled", summaryOff = "Haptic feedback disabled"),
        SettingModel.Picker("app_theme", "App Theme", Icons.Filled.Palette, "General", options = listOf("System Default", "Blue", "Green", "Purple"), dynamicDescriptionKey = "app_theme"),
        SettingModel.Slider("text_size_scale", "Text Size", Icons.Filled.FormatSize, "General", valueRange = 0.8f..1.5f, steps = 6, valueLabelFormat = { "${(it * 100).toInt()}%" }),

        // Advanced Category
        SettingModel.Switch("advanced_enabled", "Enable Advanced Settings", Icons.Filled.Tune, "Advanced", summaryOn = "Advanced options unlocked", summaryOff = "Advanced options locked", enablesKeys = listOf("experimental_feature_x")),
        SettingModel.Switch("experimental_feature_x", "Experimental Feature X", Icons.Filled.Science, "Advanced", summaryOn = "Feature X is active (beta)", summaryOff = "Feature X is inactive"),

        // Information Category
        SettingModel.Action("reset_prefs", "Reset All Settings", Icons.Filled.RestartAlt, "Information", ActionType.RESET_PREFERENCES, summary = "Revert to factory defaults"),
        SettingModel.Action("about_app", "About App", Icons.Filled.Info, "Information", ActionType.ABOUT_APP),
        SettingModel.Action("privacy_policy", "Privacy Policy", Icons.AutoMirrored.Filled.Article, "Information", ActionType.PRIVACY_POLICY)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
class SettingsActivity : ComponentActivity() {

    // In a real app, use Hilt or manual DI for ViewModel
    private val viewModel: SettingsViewModel by lazy {
        SettingsViewModel(MockSettingsRepository()) // Replace with actual repository
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ktimaz { // Your app's theme
                val uiState by viewModel.uiState.collectAsState()

                var localShowResetDialog by remember { mutableStateOf(false) }
                var localShowPickerKey by remember { mutableStateOf<String?>(null) }


                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Settings", fontWeight = FontWeight.Medium) },
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
                    if (uiState.isLoading) {
                        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        SettingsScreenContent(
                            modifier = Modifier.padding(paddingValues),
                            settingDefinitions = viewModel.settingDefinitions,
                            settingsValues = uiState.settingsValues,
                            isAdvancedSectionEnabled = uiState.isAdvancedSectionEnabled,
                            onSwitchChanged = { key, value -> viewModel.updateBooleanSetting(key, value) },
                            onSliderChanged = { key, value -> viewModel.updateFloatSetting(key, value) },
                            onActionClicked = { actionType, _ ->
                                when (actionType) {
                                    ActionType.RESET_PREFERENCES -> localShowResetDialog = true
                                    // Handle other actions (e.g., navigation)
                                    else -> { /* TODO */ }
                                }
                            },
                            onPickerClicked = { key -> localShowPickerKey = key },
                            getDynamicDescription = viewModel::getDynamicDescriptionForKey
                        )
                    }

                    if (localShowResetDialog) {
                        ConfirmationDialog(
                            icon = Icons.Filled.RestartAlt,
                            title = "Reset Preferences?",
                            text = "This will reset all settings to their default values. This action cannot be undone.",
                            confirmButtonText = "Reset",
                            dismissButtonText = "Cancel",
                            onConfirm = {
                                createSettingDefinitions().forEach { setting -> // Reset based on definitions
                                    when(setting) {
                                        is SettingModel.Switch -> viewModel.updateBooleanSetting(setting.key, (viewModel.uiState.value.settingsValues[setting.key] as? SettingValue.Bool)?.value ?: false) // or a true default
                                        is SettingModel.Picker -> viewModel.updateStringSetting(setting.key, setting.options.firstOrNull() ?: "")
                                        is SettingModel.Slider -> viewModel.updateFloatSetting(setting.key, setting.valueRange.start)
                                        else -> {}
                                    }
                                }
                                localShowResetDialog = false
                            },
                            onDismiss = { localShowResetDialog = false }
                        )
                    }

                    localShowPickerKey?.let { pickerKey ->
                        val pickerSetting = viewModel.settingDefinitions.find { it.key == pickerKey && it is SettingModel.Picker } as? SettingModel.Picker
                        val currentValue = (uiState.settingsValues[pickerKey] as? SettingValue.Str)?.value
                        if (pickerSetting != null && currentValue != null) {
                            OptionsPickerDialog(
                                title = "Select ${pickerSetting.title}",
                                options = pickerSetting.options,
                                selectedOption = currentValue,
                                onOptionSelected = { selected ->
                                    viewModel.updateStringSetting(pickerKey, selected)
                                    localShowPickerKey = null
                                },
                                onDismiss = { localShowPickerKey = null }
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Expressive Material 3 Themed Composables ---

val Material3EaseOutExpo = CubicBezierEasing(0.0f, 0.0f, 0.0f, 1.0f) // Approximation of an expressive curve
val Material3FadeThroughEnter = fadeIn(animationSpec = tween(durationMillis = 300, delayMillis = 90, easing = LinearOutSlowInEasing)) +
        scaleIn(animationSpec = tween(durationMillis = 300, delayMillis = 90, easing = LinearOutSlowInEasing), initialScale = 0.92f)
val Material3FadeThroughExit = fadeOut(animationSpec = tween(durationMillis = 150, easing = FastOutLinearInEasing))


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    settingDefinitions: List<SettingModel>,
    settingsValues: Map<String, SettingValue>,
    isAdvancedSectionEnabled: Boolean, // Example of passing derived state
    onSwitchChanged: (key: String, isChecked: Boolean) -> Unit,
    onSliderChanged: (key: String, value: Float) -> Unit,
    onActionClicked: (actionType: ActionType, key: String) -> Unit,
    onPickerClicked: (key: String) -> Unit,
    getDynamicDescription: (key: String) -> String?
) {
    val groupedSettings = settingDefinitions.groupBy { it.category }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
    ) {
        groupedSettings.forEach { (category, settingsInCategory) ->
            item(key = "header_$category") {
                SettingsGroupHeader(category)
            }
            itemsIndexed(settingsInCategory, key = { _, item -> item.key }) { index, settingDefinition ->
                // Determine effective enabled state for the setting item
                val isEffectivelyEnabled = when (settingDefinition.key) {
                    "experimental_feature_x" -> isAdvancedSectionEnabled // This item depends on the "advanced_enabled" switch
                    else -> true // Default to true, can add more complex logic
                }
                // Determine if the setting itself is a child that should be revealed
                val isRevealedChild = settingDefinitions.any { parent ->
                    parent is SettingModel.Switch &&
                    parent.revealsKeys?.contains(settingDefinition.key) == true &&
                    (settingsValues[parent.key] as? SettingValue.Bool)?.value == false
                }

                AnimatedVisibility(
                    visible = !isRevealedChild, // Hide if it's a child of a turned-off switch
                    enter = fadeIn(tween(300, easing = LinearOutSlowInEasing)) + expandVertically(tween(400, easing = Material3EaseOutExpo)),
                    exit = fadeOut(tween(200, easing = FastOutLinearInEasing)) + shrinkVertically(tween(300, easing = FastOutLinearInEasing)),
                ) {
                     AnimatedSettingItem(indexInList = index) { // Pass unique index for stagger
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
                if (index < settingsInCategory.size - 1 && !isRevealedChild) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedSettingItem(indexInList: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((indexInList * 60L).coerceAtMost(350L)) // Staggered delay for expressive entrance
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing)) +
                slideInVertically(
                    initialOffsetY = { it / 6 }, // Slight slide from bottom
                    animationSpec = tween(durationMillis = 500, easing = Material3EaseOutExpo)
                ),
        exit = fadeOut(animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing))
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
    val currentContentAlpha = if (isEffectivelyEnabled) LocalContentColor.current.alpha else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f).alpha

    // Get current values from the state
    val currentBoolValue = (settingsValues[settingDefinition.key] as? SettingValue.Bool)?.value
    val currentStringValue = (settingsValues[settingDefinition.key] as? SettingValue.Str)?.value
    val currentFloatValue = (settingsValues[settingDefinition.key] as? SettingValue.Flt)?.value

    // Determine summary text
    var summaryText: String? = null
    if (settingDefinition is SettingModel.Switch) {
        summaryText = if (currentBoolValue == true) settingDefinition.summaryOn else settingDefinition.summaryOff
    } else if (settingDefinition is SettingModel.Action) {
        summaryText = settingDefinition.summary
    }
    settingDefinition.dynamicDescriptionKey?.let {
        summaryText = getDynamicDescription(it) ?: summaryText
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = isEffectivelyEnabled,
                onClick = {
                    when (settingDefinition) {
                        is SettingModel.Switch -> currentBoolValue?.let { onSwitchChanged(settingDefinition.key, !it) }
                        is SettingModel.Action -> onActionClicked(settingDefinition.actionType, settingDefinition.key)
                        is SettingModel.Picker -> onPickerClicked(settingDefinition.key)
                        is SettingModel.Slider -> { /* Click on slider row might do nothing, or open a detailed view */ }
                    }
                },
                interactionSource = interactionSource,
                indication = LocalIndication.current // Material 3 ripple
            )
            .padding(horizontal = 20.dp, vertical = 16.dp) // Slightly more padding for an expressive feel
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = settingDefinition.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp), // Consistent icon size
                tint = if (isEffectivelyEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = currentContentAlpha)
            )
            Spacer(Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = settingDefinition.title,
                    style = MaterialTheme.typography.titleMedium, // Expressive typography
                    color = if (isEffectivelyEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = currentContentAlpha)
                )
                AnimatedContent(
                    targetState = summaryText,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(200, delayMillis = 100, easing = LinearOutSlowInEasing)) +
                         slideInVertically(animationSpec = tween(300, easing = Material3EaseOutExpo)) { it / 2} ).togetherWith(
                            fadeOut(animationSpec = tween(150, easing = FastOutLinearInEasing))
                        )
                    }, label = "summaryAnimation"
                ) { text ->
                    if (!text.isNullOrEmpty()) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isEffectivelyEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = currentContentAlpha),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.width(16.dp)) // Space before the trailing widget

            when (settingDefinition) {
                is SettingModel.Switch -> currentBoolValue?.let {
                    Switch(checked = it, onCheckedChange = { checkVal -> onSwitchChanged(settingDefinition.key, checkVal) }, enabled = isEffectivelyEnabled,
                           thumbContent = if (it) { { Icon(Icons.Filled.Done, null, modifier = Modifier.size(SwitchDefaults.IconSize)) } } else null) // Expressive Switch
                }
                is SettingModel.Picker -> currentStringValue?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = if (isEffectivelyEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = currentContentAlpha))
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Open picker", tint = if (isEffectivelyEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = currentContentAlpha))
                    }
                }
                // Action items usually don't have a trailing widget unless it's a chevron for navigation
                is SettingModel.Action -> if (settingDefinition.actionType != ActionType.RESET_PREFERENCES) { // Reset is full width text
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Perform action", tint = if (isEffectivelyEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = currentContentAlpha))
                }
                else -> {} // Slider is handled below the main row
            }
        }

        // Slider specific UI, placed below the main info row for better layout
        if (settingDefinition is SettingModel.Slider && currentFloatValue != null) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Slider(
                    value = currentFloatValue,
                    onValueChange = { onSliderChanged(settingDefinition.key, it) },
                    valueRange = settingDefinition.valueRange,
                    steps = settingDefinition.steps,
                    enabled = isEffectivelyEnabled,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors( // Expressive M3 Slider colors
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = settingDefinition.valueLabelFormat(currentFloatValue),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isEffectivelyEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = currentContentAlpha),
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(50.dp) // Fixed width for value label
                )
            }
        }
    }
}

@Composable
fun SettingsGroupHeader(title: String) {
    Text(
        text = title.uppercase(), // Expressive styling for headers
        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.8.sp), // M3 expressive typography
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp) // Increased padding
    )
}

@Composable
fun ConfirmationDialog(
    icon: ImageVector,
    title: String,
    text: String,
    confirmButtonText: String,
    dismissButtonText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text(title, style = MaterialTheme.typography.headlineSmall) }, // M3 Dialog styling
        text = { Text(text, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            Button(onClick = onConfirm) { Text(confirmButtonText) } // M3 Button
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(dismissButtonText) } // M3 OutlinedButton
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
        Surface( // M3 Surface for Dialog
            shape = MaterialTheme.shapes.extraLarge, // Expressive shape
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp), // Dialog elevation
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 20.dp)) // M3 Dialog styling
                options.forEach { option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium) // Expressive clip
                            .clickable { onOptionSelected(option) }
                            .padding(vertical = 14.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (option == selectedOption) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (option == selectedOption) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 16.dp).size(24.dp) // Consistent icon size
                        )
                        Text(option, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Spacer(Modifier.height(24.dp))
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
