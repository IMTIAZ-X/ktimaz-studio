package com.ktimazstudio.agent.ui.settings

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ktimazstudio.agent.data.*
import com.ktimazstudio.agent.viewmodel.AgentViewModel

@Composable
fun ApiManagementSettings(viewModel: AgentViewModel, settings: AppSettings) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingApiId by remember { mutableStateOf<String?>(null) }
    val currentSession = viewModel.currentSession

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp)
                .background(Color(0xFF1A1A2E), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column {
                Text("API Management", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                Spacer(Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${settings.apiConfigs.size}/${if (settings.isProUser) "∞" else "5"}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF667EEA))
                        Spacer(Modifier.height(4.dp))
                        Text("APIs Added", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${viewModel.activeApiCount}/5", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF4ECDC4))
                        Spacer(Modifier.height(4.dp))
                        Text("Active Now", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                }
            }
        }

        // Add Button
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF667EEA))
        ) {
            Text("➕ Add New API", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
        }

        if (!settings.isProUser && settings.apiConfigs.size >= 5) {
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFFF6B6B).copy(alpha = 0.15f)).padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("⚠", fontSize = 16.sp)
                    Text("Free plan limited to 5 APIs", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
                }
            }
        }

        Text("Your Configurations", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)

        if (settings.apiConfigs.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF1A1A2E)).padding(32.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("☁", fontSize = 40.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("No APIs Configured", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
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
        AddApiDialog(onDismiss = { showAddDialog = false }, onSave = { viewModel.addApiConfig(it); showAddDialog = false })
    }

    editingApiId?.let { apiId ->
        val api = settings.apiConfigs.find { it.id == apiId }
        if (api != null) {
            EditApiDialog(api = api, onDismiss = { editingApiId = null }, onSave = { viewModel.updateApiConfig(apiId, it); editingApiId = null })
        }
    }
}

@Composable
fun ApiConfigCard(api: ApiConfig, isActive: Boolean, isInChat: Boolean, onToggleActive: () -> Unit, onToggleChat: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (isActive) api.provider.color.copy(alpha = 0.2f) else Color(0xFF1A1A2E)).padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(api.provider.color))
                Column(Modifier.weight(1f)) {
                    Text(api.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    Text("${api.provider.title} • ${api.modelName}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                }
                var showMenu by remember { mutableStateOf(false) }
                Box {
                    Text("⋮", fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.clickable { showMenu = true })
                    if (showMenu) {
                        DropdownMenu(expanded = true, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(text = { Text("Edit", fontSize = 12.sp) }, onClick = { onEdit(); showMenu = false })
                            DropdownMenuItem(text = { Text("Delete", fontSize = 12.sp, color = Color(0xFFFF6B6B)) }, onClick = { onDelete(); showMenu = false })
                        }
                    }
                }
            }

            Divider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = Color.White.copy(alpha = 0.1f))

            Row(
                modifier = Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF667EEA).copy(alpha = if (isActive) 0.3f else 0.1f)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("⚡", fontSize = 14.sp)
                Text("Globally Active", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.White, modifier = Modifier.weight(1f))
                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(if (isActive) Color(0xFF667EEA) else Color.Gray.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                    if (isActive) Text("✓", fontSize = 10.sp, color = Color.White)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF667EEA).copy(alpha = if (isInChat && isActive) 0.3f else 0.1f)).padding(12.dp).clickable { onToggleChat() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("💬", fontSize = 14.sp)
                Text("Use in Chat", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.White, modifier = Modifier.weight(1f))
                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(if (isInChat && isActive) Color(0xFF667EEA) else Color.Gray.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                    if (isInChat && isActive) Text("✓", fontSize = 10.sp, color = Color.White)
                }
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

    Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.fillMaxWidth(0.9f).shadow(16.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFF0F0F1F))) {
            LazyColumn(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item { Text("Add New API", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White) }

                item {
                    Text("Select Provider", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.7f))
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(AiProvider.values()) { provider ->
                            Box(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(if (selectedProvider == provider) provider.color.copy(alpha = 0.2f) else Color(0xFF1A1A2E)).clickable { selectedProvider = provider }.padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(provider.color))
                                    Text(provider.title, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                    if (selectedProvider == provider) Text("✓", color = provider.color, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }

                item { ApiInputField("Configuration Name", name) { name = it } }
                item { ApiInputField("API Key", apiKey, isSecret = true) { apiKey = it } }
                item { ApiInputField("Model Name", modelName) { modelName = it } }
                item { ApiInputField("Base URL", baseUrl) { baseUrl = it } }
                item { ApiInputField("System Role (Optional)", systemRole) { systemRole = it } }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = onDismiss, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A2E))) {
                            Text("Cancel", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Button(onClick = { if (apiKey.isNotBlank()) onSave(ApiConfig(provider = selectedProvider, name = name.ifBlank { "My ${selectedProvider.title}" }, apiKey = apiKey, modelName = modelName, baseUrl = baseUrl, systemRole = systemRole)) }, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = if (apiKey.isNotBlank()) Color(0xFF667EEA) else Color.Gray.copy(alpha = 0.3f))) {
                            Text("Add API", fontWeight = FontWeight.Bold, color = Color.White)
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

    Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.fillMaxWidth(0.9f).shadow(16.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFF0F0F1F))) {
            LazyColumn(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item { Text("Edit API", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White) }
                item { ApiInputField("Name", name) { name = it } }
                item { ApiInputField("API Key", apiKey, isSecret = true) { apiKey = it } }
                item { ApiInputField("Model", modelName) { modelName = it } }
                item { ApiInputField("Base URL", baseUrl) { baseUrl = it } }
                item { ApiInputField("System Role", systemRole) { systemRole = it } }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = onDismiss, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A2E))) {
                            Text("Cancel", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Button(onClick = { onSave(api.copy(name = name, apiKey = apiKey, modelName = modelName, baseUrl = baseUrl, systemRole = systemRole)) }, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF667EEA))) {
                            Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ApiInputField(label: String, value: String, isSecret: Boolean = false, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.7f))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF1A1A2E)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color(0xFF667EEA),
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true,
            visualTransformation = if (isSecret) PasswordVisualTransformation() else VisualTransformation.None
        )
    }
}