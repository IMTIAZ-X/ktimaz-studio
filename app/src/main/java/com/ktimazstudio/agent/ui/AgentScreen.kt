package com.ktimazstudio.agent.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ktimazstudio.agent.theme.ModernAgentTheme
import com.ktimazstudio.agent.ui.components.*
import com.ktimazstudio.agent.ui.settings.SettingsModal
import com.ktimazstudio.agent.viewmodel.AgentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentScreen(viewModel: AgentViewModel = viewModel()) {
    val settings by viewModel.settings.collectAsState()

    ModernAgentTheme(darkTheme = settings.isDarkTheme) {
        val isSidebarOpen by viewModel.isSidebarOpen.collectAsState()
        val isSettingsModalOpen by viewModel.isSettingsModalOpen.collectAsState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = if (settings.isDarkTheme) {
                            listOf(
                                Color(0xFF0A0A1E),
                                Color(0xFF1A1A2E),
                                Color(0xFF0F0F1E)
                            )
                        } else {
                            listOf(
                                Color(0xFFF8F9FE),
                                Color(0xFFEEF2FF),
                                Color(0xFFE0E7FF)
                            )
                        }
                    )
                )
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