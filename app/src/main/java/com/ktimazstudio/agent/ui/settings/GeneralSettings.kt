package com.ktimazstudio.agent.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ktimazstudio.agent.data.AppSettings
import com.ktimazstudio.agent.viewmodel.AgentViewModel

@Composable
fun GeneralSettings(viewModel: AgentViewModel, settings: AppSettings) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Appearance",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        SettingRow(
            title = "Dark Mode",
            subtitle = "Use dark color scheme",
            trailing = {
                Switch(
                    checked = settings.isDarkTheme,
                    onCheckedChange = { viewModel.toggleTheme(it) }
                )
            }
        )

        HorizontalDivider()

        Text(
            "About",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            "AI Agent is a multi-model AI assistant that allows you to use multiple AI providers simultaneously for enhanced responses.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun SettingRow(
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        trailing()
    }
}