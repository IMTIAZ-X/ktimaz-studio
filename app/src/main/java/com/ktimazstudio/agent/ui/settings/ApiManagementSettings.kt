package com.ktimazstudio.agent.ui.settings

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ktimazstudio.agent.data.*
import com.ktimazstudio.agent.viewmodel.AgentViewModel

@Composable
fun ApiManagementSettings(viewModel: AgentViewModel, settings: AppSettings) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingApiId by remember { mutableStateOf<String?>(null) }
    val currentSession = viewModel.currentSession

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "API Management",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "APIs Added",
                        value = "${settings.apiConfigs.size}/${if (settings.isProUser) "∞" else "5"}",
                        color = MaterialTheme.colorScheme.primary
                    )
                    StatItem(
                        label = "Active Now",
                        value = "${viewModel.activeApiCount}/5",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Add Button
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Add New API", fontWeight = FontWeight.Bold)
        }

        // Pro Limit Warning
        if (!settings.isProUser && settings.apiConfigs.size >= 5) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        "Free plan limited to 5 APIs",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Text(
            "Your Configurations",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        // API List
        if (settings.apiConfigs.isEmpty()) {
            EmptyApiState()
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(settings.apiConfigs, key = { it.id }) { api ->
                    ApiConfigCard(
                        api = api,
                        isActive = api.isActive,
                        isInChat = currentSession?.activeApis?.contains(api.id) == true,
                        onToggleActive = { viewModel.toggleApiActive(api.id) },
                        onToggleChat = { viewModel.toggleApiForCurrentChat(api.id) },
                        onEdit = { editingApiId = api.id },
                        onDelete = { viewModel.deleteApiConfig(api.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddApiDialog(
            onDismiss = { showAddDialog = false },
            onSave = {
                viewModel.addApiConfig(it)
                showAddDialog = false
            }
        )
    }

    editingApiId?.let { apiId ->
        val api = settings.apiConfigs.find { it.id == apiId }
        if (api != null) {
            EditApiDialog(
                api = api,
                onDismiss = { editingApiId = null },
                onSave = {
                    viewModel.updateApiConfig(apiId, it)
                    editingApiId = null
                }
            )
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = color)
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun EmptyApiState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Cloud,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                "No APIs Configured",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Add your first API to get started",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun ApiConfigCard(
    api: ApiConfig,
    isActive: Boolean,
    isInChat: Boolean,
    onToggleActive: () -> Unit,
    onToggleChat: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) 
                api.provider.color.copy(alpha = 0.2f) 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(api.provider.color)
                )
                Column(Modifier.weight(1f)) {
                    Text(api.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        "${api.provider.title} • ${api.modelName}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { onEdit(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = { onDelete(); showMenu = false },
                            leadingIcon = { 
                                Icon(
                                    Icons.Default.Delete, 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                ) 
                            }
                        )
                    }
                }
            }

            HorizontalDivider()

            // Toggle switches
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = isActive,
                    onClick = onToggleActive,
                    label = { Text("Globally Active") },
                    leadingIcon = {
                        Icon(
                            if (isActive) Icons.Default.CheckCircle else Icons.Default.Circle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                
                FilterChip(
                    selected = isInChat && isActive,
                    onClick = onToggleChat,
                    enabled = isActive,
                    label = { Text("Use in Chat") },
                    leadingIcon = {
                        Icon(
                            if (isInChat) Icons.Default.CheckCircle else Icons.Default.Circle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun AddApiDialog(onDismiss: () -> Unit, onSave: (ApiConfig) -> Unit) {
    var selectedProvider by remember { mutableStateOf(AiProvider.GEMINI) }
    var name by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var modelName by remember { mutableStateOf(selectedProvider.defaultModel) }
    var baseUrl by remember { mutableStateOf(selectedProvider.defaultUrl) }
    var systemRole by remember { mutableStateOf("") }

    LaunchedEffect(selectedProvider) {
        modelName = selectedProvider.defaultModel
        baseUrl = selectedProvider.defaultUrl
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(20.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Add New API", fontSize = 20.sp, fontWeight = FontWeight.Black)
                }

                item {
                    Text("Select Provider", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        AiProvider.values().forEach { provider ->
                            ProviderSelector(
                                provider = provider,
                                isSelected = selectedProvider == provider,
                                onClick = { selectedProvider = provider }
                            )
                        }
                    }
                }

                item { ApiInputField("Configuration Name", name) { name = it } }
                item { ApiInputField("API Key", apiKey, isSecret = true) { apiKey = it } }
                item { ApiInputField("Model Name", modelName) { modelName = it } }
                item { ApiInputField("Base URL", baseUrl) { baseUrl = it } }
                item { ApiInputField("System Role (Optional)", systemRole) { systemRole = it } }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (apiKey.isNotBlank()) {
                                    onSave(
                                        ApiConfig(
                                            provider = selectedProvider,
                                            name = name.ifBlank { "My ${selectedProvider.title}" },
                                            apiKey = apiKey,
                                            modelName = modelName,
                                            baseUrl = baseUrl,
                                            systemRole = systemRole
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = apiKey.isNotBlank()
                        ) {
                            Text("Add API")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditApiDialog(api: ApiConfig, onDismiss: () -> Unit, onSave: (ApiConfig) -> Unit) {
    var name by remember { mutableStateOf(api.name) }
    var apiKey by remember { mutableStateOf(api.apiKey) }
    var modelName by remember { mutableStateOf(api.modelName) }
    var baseUrl by remember { mutableStateOf(api.baseUrl) }
    var systemRole by remember { mutableStateOf(api.systemRole) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.8f),
            shape = RoundedCornerShape(20.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Text("Edit API", fontSize = 20.sp, fontWeight = FontWeight.Black) }
                item { ApiInputField("Name", name) { name = it } }
                item { ApiInputField("API Key", apiKey, isSecret = true) { apiKey = it } }
                item { ApiInputField("Model", modelName) { modelName = it } }
                item { ApiInputField("Base URL", baseUrl) { baseUrl = it } }
                item { ApiInputField("System Role", systemRole) { systemRole = it } }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                onSave(
                                    api.copy(
                                        name = name,
                                        apiKey = apiKey,
                                        modelName = modelName,
                                        baseUrl = baseUrl,
                                        systemRole = systemRole
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderSelector(provider: AiProvider, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                provider.color.copy(alpha = 0.2f) 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(provider.color)
            )
            Text(provider.title, fontSize = 12.sp, modifier = Modifier.weight(1f))
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = provider.color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ApiInputField(
    label: String,
    value: String,
    isSecret: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (isSecret) PasswordVisualTransformation() else VisualTransformation.None
        )
    }
}