package com.ktimazstudio.agent.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.*
import com.ktimazstudio.agent.ui.components.*
import com.ktimazstudio.agent.ui.settings.SettingsModal
import com.ktimazstudio.agent.ui.theme.ModernAgentTheme
import com.ktimazstudio.agent.viewmodel.AgentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentScreen(viewModel: AgentViewModel = viewModel()) {
    val settings by viewModel.settings.collectAsState()
    val isSidebarOpen by viewModel.isSidebarOpen.collectAsState()
    val isSettingsModalOpen by viewModel.isSettingsModalOpen.collectAsState()

    ModernAgentTheme(darkTheme = settings.isDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = { TopBar(viewModel) },
                containerColor = Color.Transparent,
                content = { paddingValues ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        AnimatedVisibility(
                            visible = isSidebarOpen,
                            enter = slideInHorizontally() + fadeIn(),
                            exit = slideOutHorizontally() + fadeOut()
                        ) {
                            ModernSidebar(viewModel)
                        }

                        ChatInterface(viewModel)
                    }

                    if (isSettingsModalOpen) {
                        SettingsModal(viewModel)
                    }
                }
            )
        }
    }
}
