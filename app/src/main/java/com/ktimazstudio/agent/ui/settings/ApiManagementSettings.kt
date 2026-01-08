package com.ktimazstudio.agent.ui.settings

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ktimazstudio.agent.data.*
import com.ktimazstudio.agent.viewmodel.AgentViewModel

@Composable
fun ApiManagementSettings(viewModel: AgentViewModel, settings: AppSettings) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingApi by remember { mutableStateOf<ApiConfig?>(null) }
    val currentSession = viewModel.currentSession

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Stats Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("API Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(
                            "${settings.apiConfigs.size}/${if (settings.isProUser) "∞" else AppTheme.FREE_API_LIMIT}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("APIs Added", style = MaterialTheme.typography.labelMedium)
                    }
                    Column {
                        Text(
                            "${viewModel.activeApiCount}/5",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF10B981)
                        )
                        Text("Active Now", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // Add Button
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = settings.isProUser || settings.apiConfigs.size < AppTheme.FREE_API_LIMIT,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Add New API", fontWeight = FontWeight.Bold)
        }

        if (!settings.isProUser && settings.apiConfigs.size >= AppTheme.FREE_API_LIMIT) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(12.dp))
                    Text("Free plan limited to ${AppTheme.FREE_API_LIMIT} APIs. Upgrade to Pro for unlimited.")
                }
            }
        }

        Text("Your API Configurations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        if (settings.apiConfigs.isEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier
                    .fillMaxWidth()
                    .padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CloudOff, null, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No APIs configured", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            settings.apiConfigs.forEach { api ->
                ApiConfigCard(
                    api = api,
                    isGloballyActive = api.isActive,
                    isActiveInCurrentChat = currentSession?.activeApis?.contains(api.id) == true,
                    canActivateMore = viewModel.activeApiCount < AppTheme.MAX_ACTIVE_APIS_PER_CHAT,
                    onToggleGlobalActive = { viewModel.toggleApiActive(api.id) },
                    onToggleForChat = { viewModel.toggleApiForCurrentChat(api.id) },
                    onEdit = { editingApi = api },
                    onDelete = { viewModel.deleteApiConfig(api.id) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddApiDialog(
            onDismiss = { showAddDialog = false },
            onSave = { if (viewModel.addApiConfig(it)) showAddDialog = false }
        )
    }

    editingApi?.let { api ->
        EditApiDialog(
            api = api,
            onDismiss = { editingApi = null },
            onSave = { viewModel.updateApiConfig(api.id, it); editingApi = null }
        )
    }
}

@Composable
fun ApiConfigCard(
    api: ApiConfig,
    isGloballyActive: Boolean,
    isActiveInCurrentChat: Boolean,
    canActivateMore: Boolean,
    onToggleGlobalActive: () -> Unit,
    onToggleForChat: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGloballyActive) api.provider.color.copy(alpha = 0.15f) 
            else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(api.provider.color))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(api.name, fontWeight = FontWeight.Bold)
                    Text("${api.provider.title} • ${api.modelName}", style = MaterialTheme.typography.labelSmall)
                }
                Box {
                    IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, "Menu") }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { onEdit(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = { onDelete(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Power, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Globally Active", Modifier.weight(1f))
                Switch(
                    checked = isGloballyActive,
                    onCheckedChange = { if (isGloballyActive || canActivateMore) onToggleGlobalActive() },
                    enabled = isGloballyActive || canActivateMore
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Chat, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Active in Current Chat", Modifier.weight(1f))
                Switch(
                    checked = isActiveInCurrentChat,
                    onCheckedChange = { onToggleForChat() },
                    enabled = isGloballyActive
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
        if (name.isEmpty()) name = "My ${selectedProvider.title}"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f), shape = RoundedCornerShape(24.dp)) {
            LazyColumn(Modifier.padding(24.dp)) {
                item {
                    Text("Add New API", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(24.dp))
                    Text("Select Provider", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                }

                items(AiProvider.values()) { provider ->
                    Card(
                        onClick = { selectedProvider = provider },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedProvider == provider) provider.color.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Row(Modifier
                            .fillMaxWidth()
                            .padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(provider.color))
                            Spacer(Modifier.width(12.dp))
                            Text(provider.title, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.weight(1f))
                            RadioButton(selected = selectedProvider == provider, onClick = { selectedProvider = provider })
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Configuration Name") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Label, null) }
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Key, null) }
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = modelName,
                        onValueChange = { modelName = it },
                        label = { Text("Model Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        label = { Text("Base URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = systemRole,
                        onValueChange = { systemRole = it },
                        label = { Text("System Role (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                        Button(
                            onClick = {
                                if (apiKey.isNotBlank()) {
                                    onSave(ApiConfig(
                                        provider = selectedProvider,
                                        name = name.ifBlank { "My ${selectedProvider.title}" },
                                        apiKey = apiKey,
                                        modelName = modelName,
                                        baseUrl = baseUrl,
                                        systemRole = systemRole
                                    ))
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = apiKey.isNotBlank()
                        ) { Text("Add API") }
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

    Dialog(onDismissRequest = onDismiss) {
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(24.dp)) {
                Text("Edit ${api.provider.title}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, label = { Text("API Key") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = modelName, onValueChange = { modelName = it }, label = { Text("Model") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = baseUrl, onValueChange = { baseUrl = it }, label = { Text("Base URL") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = systemRole, onValueChange = { systemRole = it }, label = { Text("System Role") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = { onSave(api.copy(name = name, apiKey = apiKey, modelName = modelName, baseUrl = baseUrl, systemRole = systemRole)) },
                        modifier = Modifier.weight(1f),
                        enabled = apiKey.isNotBlank()
                    ) { Text("Save") }
                }
            }
        }
    }
}