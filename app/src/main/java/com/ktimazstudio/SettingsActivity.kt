package com.ktimazstudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Correct import for LazyColumn items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ktimazstudio.ui.theme.ktimaz // Your app's theme

// You would typically place these in a ViewModel or a data source
// For demonstration, R.drawable.arrow_back_ios_24 is used as a placeholder.
// Replace with actual icons for each setting.
data class SettingItemData(
    val title: String,
    @DrawableRes val iconRes: Int?, // Nullable if no icon
    val description: String?,      // Nullable if no description
    val defaultState: Boolean,
    val key: String                // Unique key for state management & LazyColumn
)

@OptIn(ExperimentalMaterial3Api::class)
class SettingsActivity : ComponentActivity() {

    // In a real application, you would use a ViewModel to manage settings state
    // and persist them using SharedPreferences, DataStore, or a database.
    // For example:
    // private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ktimaz { // Assuming ktimaz is your MaterialTheme composable
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Settings",
                                    // Consider using a style from your MaterialTheme.typography
                                    // style = MaterialTheme.typography.headlineSmall
                                    fontSize = 20.sp
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { onBackPressedDispatcher.onBackPressed() }) {
                                    Icon( // Using Icon for better theming capabilities
                                        painter = painterResource(id = R.drawable.arrow_back_ios_24), // Make sure this icon fits Material Design or your app's style
                                        contentDescription = "Back"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                // Use a color from your theme or a semi-transparent one
                                // to blend with the gradient or provide contrast.
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                titleContentColor = MaterialTheme.colorScheme.onSurface,
                                navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                            )
                            // If your settings list becomes scrollable, consider adding scrollBehavior:
                            // scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                            // And apply Modifier.nestedScroll(scrollBehavior.nestedScrollConnection) to Scaffold
                        )
                    },
                    containerColor = Color.Transparent // Let the Box background show through
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues) // Apply padding from Scaffold
                            .background(
                                brush = Brush.verticalGradient(
                                    // Consider defining these gradient colors in your theme
                                    listOf(Color(0xFF1D2B64), Color(0xFFF8CDDA))
                                )
                            )
                    ) {
                        SettingsContent { settingKey, newState ->
                            // This is where you'd interact with your ViewModel or persistence layer
                            // e.g., settingsViewModel.updateSetting(settingKey, newState)
                            android.util.Log.d("SettingsActivity", "Setting '$settingKey' changed to: $newState")
                            // You might also trigger other actions based on setting changes here
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun SettingsContent(onSettingChanged: (key: String, newState: Boolean) -> Unit) {
    // Define your settings. In a real app, this might come from a ViewModel or constants file.
    // NOTE: Replace R.drawable.arrow_back_ios_24 with actual, distinct icons for each setting.
    val generalSettings = remember {
        listOf(
            SettingItemData("Enable Dark Mode", R.drawable.arrow_back_ios_24, "Toggle between light and dark app themes.", false, "dark_mode"),
            SettingItemData("Enable Notifications", R.drawable.arrow_back_ios_24, "Receive important updates and alerts.", true, "notifications"),
            SettingItemData("Auto Updates", R.drawable.arrow_back_ios_24, "Keep the app up-to-date automatically.", false, "auto_updates")
        )
    }

    val advancedSettings = remember {
        listOf(
            SettingItemData("Experimental Features", R.drawable.arrow_back_ios_24, "Try new features (may be unstable).", false, "experimental_features"),
            SettingItemData("Blur Effect", R.drawable.arrow_back_ios_24, "Enable blur for certain UI elements.", true, "blur_effect"),
            SettingItemData("Custom Notifications", R.drawable.arrow_back_ios_24, "Personalize notification sounds and patterns.", false, "custom_notifications_enabled")
        )
    }

    // Animation for the content appearing
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        contentVisible = true
    }

    AnimatedVisibility(
        visible = contentVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 300, delayMillis = 100)) +
                slideInVertically(initialOffsetY = { it / 10 }, animationSpec = tween(durationMillis = 400)),
        exit = fadeOut(animationSpec = tween(durationMillis = 200))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp), // Horizontal padding for the list content
            verticalArrangement = Arrangement.spacedBy(0.dp) // Let items define their own padding or use dividers
        ) {
            // General Preferences Group
            item {
                SettingsGroupHeader("Preferences")
            }
            items(generalSettings, key = { it.key }) { setting ->
                SettingSwitch(
                    title = setting.title,
                    iconRes = setting.iconRes,
                    description = setting.description,
                    initialState = setting.defaultState, // This would come from persisted storage
                    onStateChange = { newState -> onSettingChanged(setting.key, newState) }
                )
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            }

            // Advanced Settings Group
            item {
                SettingsGroupHeader("Advanced")
            }
            items(advancedSettings, key = { it.key }) { setting ->
                SettingSwitch(
                    title = setting.title,
                    iconRes = setting.iconRes,
                    description = setting.description,
                    initialState = setting.defaultState, // This would come from persisted storage
                    onStateChange = { newState -> onSettingChanged(setting.key, newState) }
                )
                if (advancedSettings.last() != setting) { // Don't add divider after the last item in the group
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) } // Bottom padding
        }
    }
}

@Composable
fun SettingsGroupHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary, // Use a prominent color from your theme
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp, start = 8.dp, end = 8.dp) // Consistent padding
    )
}

@Composable
fun SettingSwitch(
    title: String,
    @DrawableRes iconRes: Int?,
    description: String?,
    initialState: Boolean, // Renamed from defaultState for clarity
    onStateChange: (Boolean) -> Unit
) {
    // In a real app, `checked` state would be provided by a ViewModel and updated via `onStateChange`.
    // For this example, we manage it locally but rely on `initialState`.
    var checked by remember(initialState) { mutableStateOf(initialState) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val newState = !checked
                checked = newState
                onStateChange(newState)
            }
            .padding(horizontal = 8.dp, vertical = 16.dp), // Increased vertical padding for better touch targets
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f) // Allow text content to take available space
        ) {
            iconRes?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null, // Title serves as label; provide one if icon adds unique meaning
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 16.dp),
                    tint = MaterialTheme.colorScheme.secondary // Use a theme color for icons
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface // Main text color
                )
                description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, // Softer color for description
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = { newState ->
                checked = newState
                onStateChange(newState)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.54f),
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            modifier = Modifier.padding(start = 16.dp) // Add some space before the switch
        )
    }
}
