package com.ktimazstudio

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.itemsIndexed // For SettingsScreenContent
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // For Settings screen back
import androidx.compose.material.icons.filled.* // Keep general filled icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip // For clipping TopAppBar
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalIndication // For SettingItem clickable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel // For SettingsViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel // For instantiating SettingsViewModel
import com.ktimazstudio.ui.theme.ktimaz
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// --- Navigation Destinations ---
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Dashboard)
    object AppSettings : Screen("settings", "Settings", Icons.Filled.Settings)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
}

// --- Settings Screen Components (Integrated from previous SettingsActivity) ---

// SettingValue Data Model
sealed class MainSettingValue { // Renamed to avoid conflict if SettingsActivity.kt is still in project
    data class Bool(val value: Boolean) : MainSettingValue()
    data class Str(val value: String) : MainSettingValue()
    data class Flt(val value: Float) : MainSettingValue()
}

// SettingModel Data Model
sealed class MainSettingModel( // Renamed
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
        override val dynamicDescriptionKey: String? = null
    ) : MainSettingModel(key, title, icon, category, dynamicDescriptionKey)

    data class Picker( // Simplified for embedding, dialogs handled differently
        override val key: String, override val title: String, override val icon: ImageVector, override val category: String,
        val options: List<String>,
        val defaultValue: String,
        override val dynamicDescriptionKey: String? = null
    ) : MainSettingModel(key, title, icon, category, dynamicDescriptionKey)

    data class Slider(
        override val key: String, override val title: String, override val icon: ImageVector, override val category: String,
        val defaultValue: Float,
        val valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        val steps: Int = 0,
        val valueLabelFormat: (Float) -> String = { "%.1f".format(it) },
        override val dynamicDescriptionKey: String? = null
    ) : MainSettingModel(key, title, icon, category, dynamicDescriptionKey)
    // Action type removed for simplicity in this embedded version, can be added back
}

// Settings Definitions
fun createMainSettingsDefinitions(): List<MainSettingModel> { // Renamed
    return listOf(
        MainSettingModel.Switch("main_dark_mode", "App Dark Mode", Icons.Filled.BrightnessAuto, "Appearance", defaultValue = false, summaryOff = "Light/System", summaryOn = "Dark Active", revealsKeys = listOf("main_dynamic_color")),
        MainSettingModel.Switch("main_dynamic_color", "Use Dynamic Colors", Icons.Filled.ColorLens, "Appearance", defaultValue = true, dynamicDescriptionKey = "main_dynamic_color"),
        MainSettingModel.Picker("main_app_theme_accent", "Theme Accent", Icons.Filled.Palette, "Appearance", options = listOf("Default", "Crimson", "Forest"), defaultValue = "Default", dynamicDescriptionKey = "main_app_theme_accent"),
        MainSettingModel.Slider("main_text_size", "Global Text Scale", Icons.Filled.TextFields, "Appearance", defaultValue = 1.0f, valueRange = 0.8f..1.4f, steps = 5, valueLabelFormat = { "${(it * 100).toInt()}%" }),
        MainSettingModel.Switch("main_notifications_enabled", "Enable App Notifications", Icons.Filled.NotificationsActive, "General", defaultValue = true, summaryOn = "Active", summaryOff = "Muted")
    )
}

// Settings Repository
interface MainSettingsRepository { // Renamed
    fun getSettingsFlow(): Flow<Map<String, MainSettingValue>>
    suspend fun updateSetting(key: String, value: MainSettingValue)
}

class MainSharedPreferencesSettingsRepository(private val context: Context) : MainSettingsRepository { // Renamed
    private val prefsName = "main_app_core_settings"
    private val sharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    private val _settingsStateFlow = MutableStateFlow<Map<String, MainSettingValue>>(loadSettingsFromPrefs())

    private fun loadSettingsFromPrefs(): Map<String, MainSettingValue> {
        val map = mutableMapOf<String, MainSettingValue>()
        createMainSettingsDefinitions().forEach { def ->
            when (def) {
                is MainSettingModel.Switch -> map[def.key] = MainSettingValue.Bool(sharedPreferences.getBoolean(def.key, def.defaultValue))
                is MainSettingModel.Picker -> map[def.key] = MainSettingValue.Str(sharedPreferences.getString(def.key, def.defaultValue) ?: def.defaultValue)
                is MainSettingModel.Slider -> map[def.key] = MainSettingValue.Flt(sharedPreferences.getFloat(def.key, def.defaultValue))
            }
        }
        return map.toMap()
    }
    override fun getSettingsFlow(): Flow<Map<String, MainSettingValue>> = _settingsStateFlow.asStateFlow()
    override suspend fun updateSetting(key: String, value: MainSettingValue) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().apply {
                when (value) {
                    is MainSettingValue.Bool -> putBoolean(key, value.value)
                    is MainSettingValue.Str -> putString(key, value.value)
                    is MainSettingValue.Flt -> putFloat(key, value.value)
                }
                apply()
            }
        }
        val newMap = _settingsStateFlow.value.toMutableMap()
        newMap[key] = value
        _settingsStateFlow.value = newMap.toMap()
    }
}

// Settings ViewModel
data class MainSettingsUiState(val settingsValues: Map<String, MainSettingValue> = emptyMap(), val isLoading: Boolean = true) // Renamed
class MainSettingsViewModel(private val repository: MainSettingsRepository) : ViewModel() { // Renamed
    private val _uiState = MutableStateFlow(MainSettingsUiState())
    val uiState: StateFlow<MainSettingsUiState> = _uiState.asStateFlow()
    val settingDefinitions: List<MainSettingModel> = createMainSettingsDefinitions()

    init {
        viewModelScope.launch {
            repository.getSettingsFlow().collect { persistedValues ->
                _uiState.update { it.copy(settingsValues = persistedValues, isLoading = false) }
            }
        }
    }
    fun updateBooleanSetting(key: String, value: Boolean) = viewModelScope.launch { repository.updateSetting(key, MainSettingValue.Bool(value)) }
    fun updateStringSetting(key: String, value: String) = viewModelScope.launch { repository.updateSetting(key, MainSettingValue.Str(value)) } // For Picker
    fun updateFloatSetting(key: String, value: Float) = viewModelScope.launch { repository.updateSetting(key, MainSettingValue.Flt(value)) } // For Slider
    fun getDynamicDescription(key: String): String? { // Placeholder
        return if (key == "main_dynamic_color" && (_uiState.value.settingsValues[key] as? MainSettingValue.Bool)?.value == true) "Adapting to wallpaper" else null
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (detectVpn(this)) {
            Toast.makeText(this, "VPN connection detected. Exiting application.", Toast.LENGTH_LONG).show()
            lifecycleScope.launch { delay(4000); finishAffinity() }
            return
        }

        setContent {
            ktimaz {
                val context = LocalContext.current
                val snackbarHostState = remember { SnackbarHostState() }
                var selectedDestination by remember { mutableStateOf<Screen>(Screen.Dashboard) }

                LaunchedEffect(Unit) {
                    if (!isConnected(context)) {
                        val result = snackbarHostState.showSnackbar(
                            message = "No Internet Connection!",
                            actionLabel = "Wi-Fi Settings",
                            duration = SnackbarDuration.Indefinite
                        )
                        if (result == SnackbarResult.ActionPerformed) openWifiSettings(context)
                    }
                }

                val primaryGradient = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.90f),
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.80f), // Slightly less alpha
                        MaterialTheme.colorScheme.surfaceColorAtElevation(0.dp).copy(alpha = 0.7f) // End with surface for smoother blend
                    )
                )
                val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

                Row(modifier = Modifier.fillMaxSize().background(primaryGradient)) {
                    AppNavigationRail(
                        selectedDestination = selectedDestination,
                        onDestinationSelected = { selectedDestination = it }
                    )
                    Scaffold(
                        modifier = Modifier.weight(1f).nestedScroll(scrollBehavior.nestedScrollConnection),
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(stringResource(id = R.string.app_name), fontSize = 22.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = Color.Transparent,
                                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp).copy(alpha = 0.95f) // More distinct when scrolled
                                ),
                                modifier = Modifier.statusBarsPadding()
                                    .padding(horizontal = 8.dp) // Padding to allow rounded corners to be visible
                                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)), // Rounded bottom corners
                                scrollBehavior = scrollBehavior
                            )
                        },
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        containerColor = Color.Transparent
                    ) { paddingValues ->
                        AnimatedContent(
                            targetState = selectedDestination,
                            transitionSpec = {
                                val direction = if (targetState.ordinal > initialState.ordinal) AnimatedContentScope.SlideDirection.Left else AnimatedContentScope.SlideDirection.Right
                                slideIntoContainer(towards = direction, animationSpec = tween(450, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(400)) togetherWith
                                slideOutOfContainer(towards = direction, animationSpec = tween(450, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(400))
                                using(SizeTransform(clip = false, sizeAnimationSpec = { _, _ -> tween(450, easing = FastOutSlowInEasing) }))
                            }, label = "main_content_transition"
                        ) { targetDestination ->
                            Box(modifier = Modifier.padding(paddingValues)) { // Apply scaffold padding here
                                when (targetDestination) {
                                    Screen.Dashboard -> AnimatedCardGrid { title ->
                                        if (title == "System Config") context.startActivity(Intent(context, SettingsActivity::class.java))
                                        else context.startActivity(Intent(context, ComingActivity::class.java).putExtra("CARD_TITLE", title))
                                    }
                                    Screen.AppSettings -> SettingsScreenWrapper() // Use the wrapper
                                    Screen.Profile -> ProfilePlaceholderScreen()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private fun isConnected(context: Context): Boolean { /* ... same as before ... */
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    private fun openWifiSettings(context: Context) { /* ... same as before ... */
        Toast.makeText(context, "Please enable Wi-Fi or connect to a network.", Toast.LENGTH_LONG).show()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.startActivity(Intent(Settings.Panel.ACTION_WIFI))
        } else {
            @Suppress("DEPRECATION")
            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
    }
    private fun detectVpn(context: Context): Boolean { /* ... same as before ... */
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.allNetworks.forEach { network ->
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    return true
                }
            }
        }
        return false
    }
}

@Composable
fun AppNavigationRail(
    selectedDestination: Screen,
    onDestinationSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val destinations = listOf(Screen.Dashboard, Screen.AppSettings, Screen.Profile)
    NavigationRail(
        modifier = modifier.statusBarsPadding().fillMaxHeight().padding(top = 8.dp, bottom = 8.dp), // Added top/bottom padding
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), // More transparent rail
        header = { /* Optional header */ }
    ) {
        Spacer(Modifier.height(56.dp)) // Space for TopAppBar if it overlaps or just for aesthetic
        destinations.forEach { screen ->
            val isSelected = selectedDestination == screen
            val iconScale by animateFloatAsState(targetValue = if (isSelected) 1.15f else 1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "nav_icon_scale_${screen.route}")
            NavigationRailItem(
                selected = isSelected,
                onClick = { onDestinationSelected(screen) },
                icon = { Icon(screen.icon, contentDescription = screen.label, modifier = Modifier.scale(iconScale)) },
                label = { Text(screen.label, style = MaterialTheme.typography.labelSmall) }, // Smaller label
                alwaysShowLabel = false,
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            )
            Spacer(Modifier.height(8.dp)) // Spacing between items
        }
    }
}

// Wrapper for the embedded settings screen
@Composable
fun SettingsScreenWrapper(modifier: Modifier = Modifier) {
    val context = LocalContext.current.applicationContext
    // Create or retrieve repository instance. For simplicity, creating a new one here.
    // In a real app with DI, you'd inject or retrieve this.
    val repository: MainSettingsRepository = remember { MainSharedPreferencesSettingsRepository(context) }
    val settingsViewModel: MainSettingsViewModel = viewModel { MainSettingsViewModel(repository) }
    val uiState by settingsViewModel.uiState.collectAsState()

    // A simple Column to host the settings content, can be made scrollable if needed
    Column(modifier = modifier.fillMaxSize().padding(top = 0.dp)) { // Removed top padding if scaffold handles it
        MainSettingsScreenContent(
            settingDefinitions = settingsViewModel.settingDefinitions,
            settingsValues = uiState.settingsValues,
            isLoading = uiState.isLoading,
            onSwitchChanged = settingsViewModel::updateBooleanSetting,
            onPickerClicked = { key, selectedValue -> settingsViewModel.updateStringSetting(key, selectedValue) }, // Placeholder for picker
            onSliderChanged = settingsViewModel::updateFloatSetting,
            getDynamicDescription = settingsViewModel::getDynamicDescription
        )
    }
}

// Embedded SettingsScreenContent (Simplified from original SettingsActivity)
@Composable
fun MainSettingsScreenContent(
    modifier: Modifier = Modifier,
    settingDefinitions: List<MainSettingModel>,
    settingsValues: Map<String, MainSettingValue>,
    isLoading: Boolean,
    onSwitchChanged: (key: String, isChecked: Boolean) -> Unit,
    onPickerClicked: (key: String, selectedValue: String) -> Unit, // Simplified
    onSliderChanged: (key: String, value: Float) -> Unit,
    getDynamicDescription: (key: String) -> String?
) {
    if (isLoading && settingsValues.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val groupedSettings = settingDefinitions.groupBy { it.category }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
    ) {
        groupedSettings.forEach { (category, settingsInCategory) ->
            item(key = "header_main_$category") {
                MainSettingsGroupHeader(category)
            }
            itemsIndexed(settingsInCategory, key = { _, item -> "main_setting_${item.key}" }) { index, settingDefinition ->
                val isRevealedChild = settingDefinitions.any { parent ->
                    parent is MainSettingModel.Switch &&
                    parent.revealsKeys?.contains(settingDefinition.key) == true &&
                    (settingsValues[parent.key] as? MainSettingValue.Bool)?.value == false
                }
                AnimatedVisibility(visible = !isRevealedChild) {
                    MainSettingItem( // Using a new MainSettingItem
                        settingDefinition = settingDefinition,
                        settingsValues = settingsValues,
                        isEffectivelyEnabled = true, // Simplified for this example
                        onSwitchChanged = onSwitchChanged,
                        onSliderChanged = onSliderChanged,
                        onPickerClicked = { key ->
                            // For embedded version, Picker needs a different handling (e.g., dropdown menu or bottom sheet)
                            // For now, let's just log or invoke a simplified callback
                            val pickerModel = settingDefinition as? MainSettingModel.Picker
                            pickerModel?.let { onPickerClicked(key, it.options.firstOrNull() ?: "") }
                        },
                        getDynamicDescription = getDynamicDescription
                    )
                }
                if (index < settingsInCategory.size - 1 && !isRevealedChild) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
fun MainSettingsGroupHeader(title: String) { // Renamed
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp),
        color = MaterialTheme.colorScheme.tertiary,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp)
    )
}

@Composable
fun MainSettingItem( // Renamed and simplified
    settingDefinition: MainSettingModel,
    settingsValues: Map<String, MainSettingValue>,
    isEffectivelyEnabled: Boolean,
    onSwitchChanged: (key: String, isChecked: Boolean) -> Unit,
    onSliderChanged: (key: String, value: Float) -> Unit,
    onPickerClicked: (key: String) -> Unit, // Simplified
    getDynamicDescription: (key: String) -> String?
) {
    val currentBoolValue = (settingsValues[settingDefinition.key] as? MainSettingValue.Bool)?.value
    val currentStringValue = (settingsValues[settingDefinition.key] as? MainSettingValue.Str)?.value
    val currentFloatValue = (settingsValues[settingDefinition.key] as? MainSettingValue.Flt)?.value

    var summaryText: String? = null
    if (settingDefinition is MainSettingModel.Switch) {
        summaryText = if (currentBoolValue == true) settingDefinition.summaryOn else settingDefinition.summaryOff
    }
    settingDefinition.dynamicDescriptionKey?.let { summaryText = getDynamicDescription(it) ?: summaryText }

    val itemAlpha = if (isEffectivelyEnabled) 1f else 0.5f
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(itemAlpha)
            .clickable(
                enabled = isEffectivelyEnabled && settingDefinition !is MainSettingModel.Slider,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    when (settingDefinition) {
                        is MainSettingModel.Switch -> currentBoolValue?.let { onSwitchChanged(settingDefinition.key, !it) }
                        is MainSettingModel.Picker -> onPickerClicked(settingDefinition.key)
                        else -> {}
                    }
                },
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            )
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(settingDefinition.icon, settingDefinition.title, modifier = Modifier.size(24.dp), tint = if (isEffectivelyEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(settingDefinition.title, style = MaterialTheme.typography.titleMedium)
                if (!summaryText.isNullOrEmpty()) {
                    Text(summaryText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            when (settingDefinition) {
                is MainSettingModel.Switch -> currentBoolValue?.let {
                    Switch(checked = it, onCheckedChange = { checkVal -> onSwitchChanged(settingDefinition.key, checkVal) }, enabled = isEffectivelyEnabled)
                }
                is MainSettingModel.Picker -> currentStringValue?.let {
                     Text(it, style = MaterialTheme.typography.labelLarge, color = if (isEffectivelyEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> {}
            }
        }
         if (settingDefinition is MainSettingModel.Slider) {
            val sliderValue = currentFloatValue ?: settingDefinition.defaultValue
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically){
                Slider(value = sliderValue, onValueChange = { onSliderChanged(settingDefinition.key, it) }, valueRange = settingDefinition.valueRange, steps = settingDefinition.steps, enabled = isEffectivelyEnabled, modifier = Modifier.weight(1f))
                Text(settingDefinition.valueLabelFormat(sliderValue), style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(60.dp).padding(start=8.dp), textAlign = TextAlign.End)
            }
        }
    }
}


@Composable
fun ProfilePlaceholderScreen(modifier: Modifier = Modifier) { /* ... same as before ... */
    Box(modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Text("User Profile Content Goes Here", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
    }
}

@Composable
fun AnimatedCardGrid(modifier: Modifier = Modifier, onCardClick: (String) -> Unit) { /* ... same as before, with spring animations ... */
    val cards = listOf("Spectrum Analyzer", "Image Synthesizer", "Holovid Player", "Neural Net Link", "Encrypted Notes", "Quantum Web", "Bio Scanner", "Interface Designer", "Sonic Emitter", "AI Core Access", "System Config")
    val icons = List(cards.size) { painterResource(id = R.mipmap.ic_launcher_round) }
    val haptic = LocalHapticFeedback.current

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
        horizontalArrangement = Arrangement.spacedBy(22.dp),
        modifier = modifier.fillMaxSize()
    ) {
        itemsIndexed(cards, key = { _, title -> title }) { index, title ->
            var itemVisible by remember { mutableStateOf(false) }
            LaunchedEffect(key1 = title) {
                delay(index * 80L + 150L)
                itemVisible = true
            }

            AnimatedVisibility(
                visible = itemVisible,
                enter = fadeIn(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow)) +
                        slideInVertically(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                            initialOffsetY = { it / 2 }
                        ) +
                        scaleIn(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                            initialScale = 0.7f
                        ),
                exit = fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.8f, animationSpec = tween(200))
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "card_effects_$title")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.995f, // Very subtle scale
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2800, easing = EaseInOutSine), // Very slow
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "card_scale_$title"
                )
                 val animatedAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.75f, // Subtle alpha pulse for outlined cards
                    targetValue = 0.60f,
                     animationSpec = infiniteRepeatable(
                        animation = tween(2800, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "card_alpha_$title"
                )

                Card(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCardClick(title)
                    },
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp).copy(alpha = animatedAlpha)
                    ),
                    border = CardDefaults.outlinedCardBorder(enabled = true, brush = Brush.horizontalGradient( // Subtle gradient border
                        colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha=0.5f), MaterialTheme.colorScheme.secondary.copy(alpha=0.5f))
                    )),
                    elevation = CardDefaults.outlinedCardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .then(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Modifier.blur(3.dp) else Modifier) // Reduced blur
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = icons[index % icons.size],
                            contentDescription = title,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
