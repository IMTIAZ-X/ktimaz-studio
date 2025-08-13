package com.ktimazstudio.old.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ktimazstudio.R
import com.ktimazstudio.enums.Screen
import com.ktimazstudio.managers.SoundEffectManager
import com.ktimazstudio.managers.SharedPreferencesManager
import com.ktimazstudio.old.ui.components.OldNavigationRail
import com.ktimazstudio.old.ui.screens.OldDashboardScreen
import com.ktimazstudio.old.ui.screens.OldSettingsScreen
import com.ktimazstudio.old.ui.screens.OldProfileScreen

/**
 * Old/Classic UI - Preserved from the original design
 * This maintains the classic styling and functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OldMainApplicationUI(
    username: String,
    onLogout: () -> Unit,
    soundEffectManager: SoundEffectManager,
    sharedPrefsManager: SharedPreferencesManager
) {
    val context = LocalContext.current
    var selectedDestination by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    var isRailExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Row(modifier = Modifier.fillMaxSize()) {
        // Classic Navigation Rail
        OldNavigationRail(
            selectedDestination = selectedDestination,
            onDestinationSelected = {
                soundEffectManager.playClickSound()
                selectedDestination = it
                searchQuery = ""
            },
            isExpanded = isRailExpanded,
            onMenuClick = {
                soundEffectManager.playClickSound()
                isRailExpanded = !isRailExpanded
            },
            soundEffectManager = soundEffectManager
        )

        // Classic Scaffold
        Scaffold(
            modifier = Modifier.weight(1f),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.app_name) + " Classic",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (selectedDestination) {
                    Screen.Dashboard -> OldDashboardScreen(
                        searchQuery = searchQuery,
                        onCardClick = { title ->
                            soundEffectManager.playClickSound()
                            // Handle old-style navigation
                        },
                        soundEffectManager = soundEffectManager
                    )
                    Screen.AppSettings -> OldSettingsScreen(
                        soundEffectManager = soundEffectManager,
                        sharedPrefsManager = sharedPrefsManager
                    )
                    Screen.Profile -> OldProfileScreen(
                        username = username,
                        onLogout = onLogout,
                        soundEffectManager = soundEffectManager
                    )
                }
            }
        }
    }
}