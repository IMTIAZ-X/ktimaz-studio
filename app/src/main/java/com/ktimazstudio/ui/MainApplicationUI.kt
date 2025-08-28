package com.ktimazstudio.ui

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.ktimazstudio.R
import com.ktimazstudio.SettingsActivity
import com.ktimazstudio.DevScreen
import com.ktimazstudio.enums.Screen
import com.ktimazstudio.managers.SoundEffectManager
import com.ktimazstudio.managers.SharedPreferencesManager
import com.ktimazstudio.ui.components.AppNavigationRail
import com.ktimazstudio.ui.components.CustomSearchBar
import com.ktimazstudio.ui.screens.DashboardScreen
import com.ktimazstudio.ui.screens.EnhancedDashboardScreen
import com.ktimazstudio.ui.screens.SettingsScreen
import com.ktimazstudio.ui.screens.EnhancedSettingsScreen
import com.ktimazstudio.ui.screens.ProfileScreen
import com.ktimazstudio.utils.isConnected
import com.ktimazstudio.utils.openWifiSettings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
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
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    // Enhanced UI state management
    val dashboardViewType = remember { mutableStateOf(sharedPrefsManager.getDashboardViewType()) }
    val isEnhancedMode = remember { mutableStateOf(true) } // Toggle for enhanced features
    
    // Network connectivity check on app launch
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

    // Enhanced gradient system
    val primaryGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
            MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp).copy(alpha = 0.7f),
            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        )
    )

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val topAppBarRoundedShape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    val scrolledAppBarColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp).copy(alpha = 0.98f)

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryGradient)
    ) {
        // Enhanced Navigation Rail with animations
        AppNavigationRail(
            selectedDestination = selectedDestination,
            onDestinationSelected = {
                soundEffectManager.playClickSound()
                selectedDestination = it
                isSearching = false
                searchQuery = ""
            },
            isExpanded = isRailExpanded,
            onMenuClick = {
                soundEffectManager.playClickSound()
                isRailExpanded = !isRailExpanded
            },
            soundEffectManager = soundEffectManager
        )

        // Main content area with enhanced scaffold
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
                                    slideInHorizontally { it } + fadeIn() togetherWith
                                    slideOutHorizontally { -it } + fadeOut()
                                } else {
                                    slideInHorizontally { -it } + fadeIn() togetherWith
                                    slideOutHorizontally { it } + fadeOut()
                                }
                            }, 
                            label = "search_bar_transition"
                        ) { searching ->
                            if (searching) {
                                CustomSearchBar(
                                    query = searchQuery,
                                    onQueryChange = { searchQuery = it },
                                    onClear = { 
                                        searchQuery = ""
                                        isSearching = false 
                                    },
                                    soundEffectManager = soundEffectManager
                                )
                            } else {
                                // Enhanced title with animation
                                Text(
                                    text = stringResource(id = R.string.app_name),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    },
                    actions = {
                        if (selectedDestination == Screen.Dashboard) {
                            IconButton(onClick = {
                                soundEffectManager.playClickSound()
                                isSearching = !isSearching
                                if (!isSearching) searchQuery = ""
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
                            shadowElevation = if (isScrolled) 6.dp.toPx() else 0f
                            shape = topAppBarRoundedShape
                            clip = true
                        }
                        .background(
                            color = if (isScrolled) scrolledAppBarColor else Color.Transparent,
                            shape = topAppBarRoundedShape
                        ),
                    scrollBehavior = scrollBehavior
                )
            },
            snackbarHost = { 
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(16.dp)
                ) { data ->
                    Snackbar(
                        snackbarData = data,
                        shape = RoundedCornerShape(12.dp),
                        containerColor = MaterialTheme.colorScheme.inverseSurface,
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        actionColor = MaterialTheme.colorScheme.inversePrimary
                    )
                }
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            // Enhanced content transition system
            AnimatedContent(
                targetState = selectedDestination,
                transitionSpec = {
                    val direction = if (initialState.route == Screen.Dashboard.route) 1 else -1
                    
                    fadeIn(
                        animationSpec = tween(400, easing = LinearOutSlowInEasing)
                    ) + slideInHorizontally(
                        initialOffsetX = { it * direction },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(400)
                    ) togetherWith fadeOut(
                        animationSpec = tween(300, easing = FastOutLinearInEasing)
                    ) + slideOutHorizontally(
                        targetOffsetX = { -it * direction },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + scaleOut(
                        targetScale = 1.08f,
                        animationSpec = tween(300)
                    )
                }, 
                label = "nav_content_transition"
            ) { targetDestination ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    when (targetDestination) {
                        Screen.Dashboard -> {
                            if (isEnhancedMode.value) {
                                EnhancedDashboardScreen(
                                    searchQuery = searchQuery,
                                    onCardClick = { title ->
                                        soundEffectManager.playClickSound()
                                        handleCardClick(title, context)
                                    },
                                    soundEffectManager = soundEffectManager,
                                    sharedPrefsManager = sharedPrefsManager
                                )
                            } else {
                                DashboardScreen(
                                    searchQuery = searchQuery,
                                    onCardClick = { title ->
                                        soundEffectManager.playClickSound()
                                        handleCardClick(title, context)
                                    },
                                    soundEffectManager = soundEffectManager
                                )
                            }
                        }
                        Screen.AppSettings -> {
                            if (isEnhancedMode.value) {
                                EnhancedSettingsScreen(
                                    soundEffectManager = soundEffectManager,
                                    sharedPrefsManager = sharedPrefsManager
                                )
                            } else {
                                SettingsScreen(
                                    soundEffectManager = soundEffectManager,
                                    sharedPrefsManager = sharedPrefsManager
                                )
                            }
                        }
                        Screen.Profile -> {
                            ProfileScreen(
                                username = username,
                                onLogout = onLogout,
                                soundEffectManager = soundEffectManager
                            )
                        }
                    }
                }
            }
        }
    }

    // Background particle effects for enhanced visual appeal
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            // Trigger subtle background animations
        }
    }
}

/**
 * Enhanced card click handler with improved navigation
 */
private fun handleCardClick(title: String, context: android.content.Context) {
    when (title) {
        "System Config", "System Monitor" -> {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }
        "Bio Scanner", "Quantum Encryptor", "Neural Network" -> {
            context.startActivity(
                Intent(context, DevScreen::class.java).apply { // fixed 
                    putExtra("CARD_TITLE", title)
                    putExtra("CARD_TYPE", "PREMIUM")
                }
            )
        }
        else -> {
            context.startActivity(
                Intent(context, DevScreen::class.java).apply { // fixed 
                    putExtra("CARD_TITLE", title)
                    putExtra("CARD_TYPE", "STANDARD")
                }
            )
        }
    }
}

/**
 * Performance monitoring composable
 */
@Composable
private fun PerformanceMonitor() {
    val composition = rememberCompositionContext()
    var frameCount by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(16) // ~60 FPS
            frameCount++
            if (frameCount % 60 == 0) {
                // Log performance metrics every second
                println("UI Performance: ${frameCount / (frameCount / 60)} FPS")
            }
        }
    }
}