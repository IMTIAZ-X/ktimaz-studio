package com.ktimazstudio.agent.ui.settings

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.ktimazstudio.agent.viewmodel.AgentViewModel

@Composable
fun ApiManagementSettings(viewModel: AgentViewModel, settings: AppSettings) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingApiId by remember { mutableStateOf<String?>(null) }
    val currentSession = viewModel.currentSession

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp),
            color = Color(0xFF1A1A2E),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "API Management",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "APIs Added",
                        value = "${settings.apiConfigs.size}/${if (settings.isProUser) "∞" else "5"}",
                        color = Color(0xFF667EEA)
                    )
                    StatItem(
                        label = "Active Now",
                        value = "${viewModel.activeApiCount}/5",
                        color = Color(0xFF4ECDC4)
                    )
                }
            }
        }

        // Add Button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { showAddDialog = true },
            color = Color(0xFF667EEA),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("➕", fontSize = 16.sp)
                Text(
                    "Add New API",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Pro Limit Warning
        if (!settings.isProUser && settings.apiConfigs.size >= 5) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFF6B6B).copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("⚠️", fontSize = 16.sp)
                    Text(
                        "Free plan limited to 5 APIs",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Text(
            "Your Configurations",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
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
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
    }
}

@Composable
fun EmptyApiState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1A2E),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("☁️", fontSize = 40.sp)
            Text("No APIs Configured", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                "Add your first API to get started",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f)
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isActive) api.provider.color.copy(alpha = 0.2f) else Color(0xFF1A1A2E),
        shape = RoundedCornerShape(12.dp)
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
                    Text(api.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    Text(
                        "${api.provider.title} • ${api.modelName}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                MenuButton(onEdit = onEdit, onDelete = onDelete)
            }

            DividerCustom(modifier = Modifier.fillMaxWidth(), thickness = 1.dp)

            ToggleRow(
                label = "Globally Active",
                icon = "⚡",
                enabled = isActive,
                onToggle = onToggleActive
            )

            ToggleRow(
                label = "Use in Chat",
                icon = "💬",
                enabled = isInChat && isActive,
                onToggle = onToggleChat
            )
        }
    }
}

@Composable
fun ToggleRow(
    label: String,
    icon: String,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onToggle() }
            .background(Color(0xFF667EEA).copy(alpha = if (enabled) 0.3f else 0.1f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(icon, fontSize = 14.sp)
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.White, modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (enabled) Color(0xFF667EEA) else Color.Gray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            if (enabled) Text("✓", fontSize = 10.sp, color = Color.White)
        }
    }
}

@Composable
fun MenuButton(onEdit: () -> Unit, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    Box {
        Text("⋮", fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.clickable { showMenu = true })
        if (showMenu) {
            DropdownMenuCustom(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItemCustom(text = { Text("Edit", fontSize = 12.sp, color = Color.White) }, onClick = { onEdit(); showMenu = false })
                DropdownMenuItemCustom(text = { Text("Delete", fontSize = 12.sp, color = Color(0xFFFF6B6B)) }, onClick = { onDelete(); showMenu = false })
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
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .shadow(16.dp),
            color = Color(0xFF0F0F1F),
            shape = RoundedCornerShape(20.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Add New API", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                }

                item {
                    Text("Select Provider", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.7f))
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(AiProvider.values()) { provider ->
                            ProviderSelector(
                                provider = provider,
                                isSelected = selectedProvider == provider,
                                onClick = { selectedProvider = provider }
                            )
                        }
                    }
                }

                item {
                    ApiInputField("Configuration Name", name) { name = it }
                }

                item {
                    ApiInputField("API Key", apiKey, isSecret = true) { apiKey = it }
                }

                item {
                    ApiInputField("Model Name", modelName) { modelName = it }
                }

                item {
                    ApiInputField("Base URL", baseUrl) { baseUrl = it }
                }

                item {
                    ApiInputField("System Role (Optional)", systemRole) { systemRole = it }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onDismiss() },
                            color = Color(0xFF1A1A2E),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Cancel", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = apiKey.isNotBlank()) {
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
                            color = if (apiKey.isNotBlank()) Color(0xFF667EEA) else Color.Gray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Add API", fontWeight = FontWeight.Bold, color = Color.White)
                            }
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
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .shadow(16.dp),
            color = Color(0xFF0F0F1F),
            shape = RoundedCornerShape(20.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Edit API", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                }

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
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onDismiss() },
                            color = Color(0xFF1A1A2E),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Cancel", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
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
                            color = Color(0xFF667EEA),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderSelector(provider: AiProvider, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = if (isSelected) provider.color.copy(alpha = 0.2f) else Color(0xFF1A1A2E),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(provider.color)
            )
            Text(provider.title, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
            if (isSelected) Text("✓", color = provider.color, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun ApiInputField(label: String, value: String, isSecret: Boolean = false, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.7f))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp)),
            color = Color(0xFF1A1A2E),
            shape = RoundedCornerShape(10.dp)
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .height(40.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color(0xFF667EEA),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true,
                textStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontSize = 12.sp),
                visualTransformation = if (isSecret) PasswordVisualTransformation() else VisualTransformation.None
            )
        }
    }
}

@Composable
fun Surface(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    shape: RoundedCornerShape = RoundedCornerShape(0.dp),
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.background(color, shape)) {
        content()
    }
}

@Composable
fun DropdownMenuCustom(expanded: Boolean, onDismissRequest: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    if (expanded) {
        Surface(color = Color(0xFF1A1A2E), shape = RoundedCornerShape(8.dp)) {
            Column(modifier = Modifier.padding(4.dp), content = content)
        }
    }
}

@Composable
fun DropdownMenuItemCustom(text: @Composable () -> Unit, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Box(modifier = Modifier.padding(12.dp)) { text() }
    }
}

@Composable
fun DividerCustom(modifier: Modifier = Modifier, thickness: Dp = 1.dp, color: Color = Color.White.copy(alpha = 0.1f)) {
    Box(modifier = modifier.height(thickness).background(color))
}

@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    colors: TextFieldDefaults.() -> Unit = {},
    singleLine: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    textStyle: androidx.compose.material3.TextStyle = androidx.compose.material3.LocalTextStyle.current,
    placeholder: @Composable (() -> Unit)? = null
) {
    androidx.compose.material3.TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        placeholder = placeholder
    )
}

@Composable
fun PasswordVisualTransformation(): VisualTransformation {
    return object : VisualTransformation {
        override fun filter(text: androidx.compose.ui.text.AnnotatedString): androidx.compose.ui.text.input.TransformedText {
            return androidx.compose.ui.text.input.TransformedText(
                androidx.compose.ui.text.AnnotatedString("•".repeat(text.length)),
                androidx.compose.ui.text.input.OffsetMapping.Identity
            )
        }
    }
}

@Composable
fun VisualTransformation(): VisualTransformation {
    return object : VisualTransformation {
        override fun filter(text: androidx.compose.ui.text.AnnotatedString): androidx.compose.ui.text.input.TransformedText {
            return androidx.compose.ui.text.input.TransformedText(text, androidx.compose.ui.text.input.OffsetMapping.Identity)
        }
    }
}

object TextFieldDefaults {
    @Composable
    fun colors(
        focusedContainerColor: Color = Color.White,
        unfocusedContainerColor: Color = Color.White,
        focusedIndicatorColor: Color = Color.Black,
        unfocusedIndicatorColor: Color = Color.Gray,
        focusedTextColor: Color = Color.Black,
        unfocusedTextColor: Color = Color.Black
    ) = TextFieldColorData(focusedContainerColor, unfocusedContainerColor, focusedIndicatorColor, unfocusedIndicatorColor, focusedTextColor, unfocusedTextColor)
}

data class TextFieldColorData(
    val focusedContainerColor: Color,
    val unfocusedContainerColor: Color,
    val focusedIndicatorColor: Color,
    val unfocusedIndicatorColor: Color,
    val focusedTextColor: Color,
    val unfocusedTextColor: Color
)

@Composable
fun clickable(enabled: Boolean = true, onClick: () -> Unit): Modifier {
    return Modifier.then(
        androidx.compose.foundation.clickable(enabled = enabled) { onClick() }
    )
}

import androidx.compose.ui.text.input.VisualTransformation as VT
import androidx.compose.ui.text.input.PasswordVisualTransformation as PVT