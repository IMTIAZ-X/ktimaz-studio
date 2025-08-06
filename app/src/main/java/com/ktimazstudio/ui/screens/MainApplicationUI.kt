package com.ktimazstudio.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ktimazstudio.ComingActivity
import com.ktimazstudio.R
import com.ktimazstudio.SettingsActivity
import com.ktimazstudio.data.ThemeSetting
import com.ktimazstudio.manager.SharedPreferencesManager
import com.ktimazstudio.manager.SoundEffectManager
import com.ktimazstudio.ui.components.CustomSearchBar
import com.ktimazstudio.util.isConnected
import com.ktimazstudio.util.openWifiSettings
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Dashboard)
    object AppSettings : Screen("settings", "Settings", Icons.Filled.Settings)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun MainApplicationUI(
    username: String,
    onLogout: () -> Unit,
    soundEffectManager: SoundEffectManager,
    sharedPrefsManager: SharedPreferencesManager
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedDestination by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    var isRailExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") } // State for search query
    var isSearching by remember { mutableStateOf(false) } // State to control search bar visibility

    // Check for internet connectivity on app launch
    LaunchedEffect(Unit) {
        if (!isConnected(context)) {
            val result = snackbarHostState.showSnackbar(
                message = "No Internet Connection!",
                actionLabel = "Wi-Fi Settings",
                duration = SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                openWifiSettings(context)
            }
        }
    }

    val primaryGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.90f),
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
            MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp).copy(alpha = 0.6f)
        )
    )

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val topAppBarRoundedShape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
    val scrolledAppBarColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp).copy(alpha = 0.95f)

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryGradient)
    ) {
        AppNavigationRail(
            selectedDestination = selectedDestination,
            onDestinationSelected = {
                soundEffectManager.playClickSound() // Play sound on navigation item click
                selectedDestination = it
                isSearching = false // Hide search when navigating away from dashboard
                searchQuery = "" // Clear search query
            },
            isExpanded = isRailExpanded,
            onMenuClick = {
                soundEffectManager.playClickSound() // Play sound on menu click
                isRailExpanded = !isRailExpanded
            },
            soundEffectManager = soundEffectManager
        )

        Scaffold(
            modifier = Modifier
                .weight(1f)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                val isScrolled = scrollBehavior.state.contentOffset > 0.1f
                CenterAlignedTopAppBar(
                    title = {
                        AnimatedContent(
                            targetState = isSearching,
                            transitionSpec = {
                                if (targetState) {
                                    // Entering search
                                    slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                                } else {
                                    // Exiting search
                                    slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                                }
                            },
                            label = "search_bar_transition"
                        ) { searching ->
                            if (searching) {
                                CustomSearchBar(
                                    query = searchQuery,
                                    onQueryChange = { searchQuery = it },
                                    onClear = { searchQuery = "" ; isSearching = false },
                                    soundEffectManager = soundEffectManager
                                )
                            } else {
                                Text(
                                    text = stringResource(id = com.ktimazstudio.R.string.app_name),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    },
                    actions = {
                        if (selectedDestination == Screen.Dashboard) {
                            // Only show search on Dashboard
                            IconButton(onClick = {
                                soundEffectManager.playClickSound()
                                isSearching = !isSearching
                                if (!isSearching) searchQuery = "" // Clear search when closing
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = if (isSearching) "Close Search" else "Search",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = scrolledAppBarColor,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .statusBarsPadding()
                        .graphicsLayer {
                            shadowElevation = if (isScrolled) 4.dp.toPx() else 0f
                            shape = topAppBarRoundedShape
                            clip = true
                        }
                        .background(
                            color = if (isScrolled) scrolledAppBarColor else Color.Transparent
                        ),
                    scrollBehavior = scrollBehavior
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = Color.Transparent
        ) { paddingValues ->
            AnimatedContent(
                targetState = selectedDestination,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300, easing = LinearOutSlowInEasing)) + slideInHorizontally(initialOffsetX = { if (initialState.route == Screen.Dashboard.route) 300 else -300 }, animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300, easing = FastOutLinearInEasing)) + slideOutHorizontally(targetOffsetX = { if (targetState.route == Screen.Dashboard.route) -300 else 300 }, animationSpec = tween(300))
                },
                label = "nav_rail_content_transition"
            ) { targetDestination ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    when (targetDestination) {
                        Screen.Dashboard -> AnimatedCardGrid(
                            searchQuery = searchQuery, // Pass search query
                            onCardClick = { title ->
                                soundEffectManager.playClickSound() // Play sound on card click
                                if (title == "System Config") {
                                    context.startActivity(Intent(context, SettingsActivity::class.java))
                                } else {
                                    context.startActivity(Intent(context, ComingActivity::class.java).putExtra("CARD_TITLE", title))
                                }
                            },
                            soundEffectManager = soundEffectManager // Pass sound manager
                        )
                        Screen.AppSettings -> SettingsScreen(
                            soundEffectManager = soundEffectManager,
                            sharedPrefsManager = sharedPrefsManager
                        )
                        Screen.Profile -> ProfileScreen(username = username, onLogout = onLogout, soundEffectManager = soundEffectManager)
                    }
                }
            }
        }
    }
}

/**
 * Custom Navigation Rail for the main application UI.
 * This component is shown on large screens in a `Row` layout.
 *
 * @param selectedDestination The currently selected screen.
 * @param onDestinationSelected Callback to be invoked when a navigation item is clicked.
 * @param isExpanded Whether the navigation rail is in its expanded state.
 * @param onMenuClick Callback for when the menu button is clicked.
 * @param soundEffectManager Manager for playing sound effects.
 */
@Composable
fun AppNavigationRail(
    selectedDestination: Screen,
    onDestinationSelected: (Screen) -> Unit,
    isExpanded: Boolean,
    onMenuClick: () -> Unit,
    soundEffectManager: SoundEffectManager
) {
    NavigationRail(
        modifier = Modifier.animateContentSize(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight().padding(vertical = 8.dp)
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_menu),
                    contentDescription = "Menu"
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            NavigationRailItem(
                selected = selectedDestination == Screen.Dashboard,
                onClick = { onDestinationSelected(Screen.Dashboard) },
                icon = { Icon(Screen.Dashboard.icon, contentDescription = null) },
                label = { if (isExpanded) Text(Screen.Dashboard.label) }
            )
            NavigationRailItem(
                selected = selectedDestination == Screen.AppSettings,
                onClick = { onDestinationSelected(Screen.AppSettings) },
                icon = { Icon(Screen.AppSettings.icon, contentDescription = null) },
                label = { if (isExpanded) Text(Screen.AppSettings.label) }
            )
            NavigationRailItem(
                selected = selectedDestination == Screen.Profile,
                onClick = { onDestinationSelected(Screen.Profile) },
                icon = { Icon(Screen.Profile.icon, contentDescription = null) },
                label = { if (isExpanded) Text(Screen.Profile.label) }
            )
        }
    }
}

/**
 * Data class to represent the animated cards in the dashboard.
 */
data class DashboardCard(
    val title: String,
    val icon: Int,
    val description: String,
    val color: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnimatedCardGrid(
    searchQuery: String,
    onCardClick: (String) -> Unit,
    soundEffectManager: SoundEffectManager
) {
    val allCards = remember {
        listOf(
            DashboardCard(
                "System Config",
                R.drawable.ic_system_config,
                "Configure system settings and preferences.",
                Color(0xFF673AB7)
            ),
            DashboardCard(
                "Network Monitor",
                R.drawable.ic_network_monitor,
                "Monitor network activity and connections.",
                Color(0xFF2196F3)
            ),
            DashboardCard(
                "Device Info",
                R.drawable.ic_device_info,
                "View detailed information about your device.",
                Color(0xFF4CAF50)
            ),
            DashboardCard(
                "Security Check",
                R.drawable.ic_security_check,
                "Run a comprehensive security scan.",
                Color(0xFFF44336)
            ),
            DashboardCard(
                "App Usage",
                R.drawable.ic_app_usage,
                "Monitor application usage and statistics.",
                Color(0xFFFF9800)
            ),
            DashboardCard(
                "Memory Stats",
                R.drawable.ic_memory_stats,
                "Check device memory and RAM usage.",
                Color(0xFF9C27B0)
            )
        )
    }

    val filteredCards = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            allCards
        } else {
            allCards.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    if (filteredCards.isEmpty() && searchQuery.isNotBlank()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "No results",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No modules found for \"$searchQuery\"",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        val listState = rememberLazyGridState()
        LazyVerticalGrid(
            state = listState,
            columns = GridCells.Adaptive(minSize = 180.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items = filteredCards, key = { it.title }) { card ->
                // The card composable is here
                ElevatedCard(
                    onClick = {
                        soundEffectManager.playClickSound() // Play sound on card click
                        onCardClick(card.title)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItemPlacement(tween(500)),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(card.color.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = card.icon),
                                contentDescription = card.title,
                                tint = card.color,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = card.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = card.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SettingsScreen(
    soundEffectManager: SoundEffectManager,
    sharedPrefsManager: SharedPreferencesManager
) {
    // SharedPrefs state management
    var isSoundEnabled by remember { mutableStateOf(sharedPrefsManager.isSoundEnabled()) }
    var selectedTheme by remember { mutableStateOf(sharedPrefsManager.getThemeSetting()) }
    val coroutineScope = rememberCoroutineScope()

    // Listen for changes from outside this composable (e.g., from MainApplicationUI)
    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == SharedPreferencesManager.KEY_SOUND_ENABLED) {
                isSoundEnabled = sharedPrefsManager.isSoundEnabled()
            }
            if (key == SharedPreferencesManager.KEY_THEME_SETTING) {
                selectedTheme = sharedPrefsManager.getThemeSetting()
            }
        }
        sharedPrefsManager.prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            sharedPrefsManager.prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // General Settings Section
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        Text(text = "General", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        // Sound Effects Toggle
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Sound Effects", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = isSoundEnabled,
                onCheckedChange = { isChecked ->
                    soundEffectManager.playClickSound()
                    isSoundEnabled = isChecked
                    sharedPrefsManager.setSoundEnabled(isChecked)
                }
            )
        }

        // Theme Selection
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Theme", style = MaterialTheme.typography.bodyLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThemeSetting.values().forEach { theme ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                    RadioButton(
                        selected = selectedTheme == theme,
                        onClick = {
                            soundEffectManager.playClickSound()
                            selectedTheme = theme
                            sharedPrefsManager.setThemeSetting(theme)
                        }
                    )
                    Text(text = theme.name, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    username: String,
    onLogout: () -> Unit,
    soundEffectManager: SoundEffectManager
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Profile Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // Profile Picture Placeholder
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), shape = RoundedCornerShape(50.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(60.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Welcome, $username!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Logout Button
        ElevatedButton(
            onClick = {
                soundEffectManager.playClickSound()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.elevatedButtonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(
                imageVector = Icons.Outlined.ExitToApp,
                contentDescription = "Logout",
                modifier = Modifier.padding(end = 8.dp),
                tint = MaterialTheme.colorScheme.onError
            )
            Text(text = "Logout", color = MaterialTheme.colorScheme.onError)
        }
    }
}
