package com.ktimazstudio.agent.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ktimazstudio.agent.viewmodel.AgentViewModel

@Composable
fun SettingsModal(viewModel: AgentViewModel) {
    val settings by viewModel.settings.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Dialog(
        onDismissRequest = { viewModel.closeSettings() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.closeSettings() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Tab Row
                TabRow(selectedTabIndex = selectedTab) {
                    listOf("General", "API Management", "Plan").forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                // Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        when (selectedTab) {
                            0 -> GeneralSettings(viewModel, settings)
                            1 -> ApiManagementSettings(viewModel, settings)
                            2 -> PlanSettings(viewModel, settings)
                        }
                    }
                }
            }
        }
    }
}