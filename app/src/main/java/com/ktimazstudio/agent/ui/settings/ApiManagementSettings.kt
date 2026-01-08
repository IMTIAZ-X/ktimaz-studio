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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.TextStyle // Added for L621, L641
import androidx.compose.ui.focus.onFocusChanged // Added for L628
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
                Text( // Fixed L46 Unresolved reference 'Text'
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
                Icon("➕", modifier = Modifier.size(16.dp))
                Text(
                    "Add New API",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // List of APIs
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(settings.apiConfigs.values.toList()) { apiConfig ->
                ApiConfigCard(
                    apiConfig = apiConfig,
                    onEdit = { editingApiId = apiConfig.id },
                    onDelete = { viewModel.deleteApiConfig(apiConfig.id) },
                    onToggleActive = { viewModel.updateApiConfig(apiConfig.copy(isActive = !apiConfig.isActive)) }
                )
            }
        }
    }

    if (showAddDialog || editingApiId != null) {
        AddEditApiDialog(
            viewModel = viewModel,
            apiConfig = settings.apiConfigs[editingApiId],
            onDismiss = { showAddDialog = false; editingApiId = null }
        )
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text( // Fixed L89 Unresolved reference 'Text'
            label,
            fontSize = 12.sp,
            color = color.copy(alpha = 0.8f)
        )
        Text( // Fixed L90 Unresolved reference 'Text'
            value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun ApiConfigCard(
    apiConfig: ApiConfig,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onToggleActive),
        color = Color(0xFF2A2A4A).copy(alpha = if (apiConfig.isActive) 1f else 0.5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text( // Fixed L112 Unresolved reference 'Text'
                    apiConfig.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text( // Fixed L113 Unresolved reference 'Text'
                    apiConfig.provider.name,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text( // Fixed L123 Unresolved reference 'Text'
                    if (apiConfig.isActive) "Active" else "Inactive",
                    fontSize = 12.sp,
                    color = if (apiConfig.isActive) Color(0xFF4ECDC4) else Color.Gray,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(end = 12.dp)
                )

                // Toggle Switch for Active/Inactive
                Switch(
                    checked = apiConfig.isActive,
                    onCheckedChange = { onToggleActive() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF4ECDC4),
                        checkedTrackColor = Color(0xFF4ECDC4).copy(alpha = 0.5f),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    )
                )

                Box {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { showMenu = true }
                    )
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") }, // Fixed L178 Unresolved reference 'Text'
                            onClick = { onEdit(); showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") }, // Fixed L180 Unresolved reference 'Text'
                            onClick = { onDelete(); showMenu = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditApiDialog(
    viewModel: AgentViewModel,
    apiConfig: ApiConfig?,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(apiConfig?.name ?: "") }
    var apiKey by remember { mutableStateOf(apiConfig?.apiKey ?: "") }
    var provider by remember { mutableStateOf(apiConfig?.provider ?: ApiProvider.GOOGLE) }

    val isEditing = apiConfig != null

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(400.dp)
                .clip(RoundedCornerShape(16.dp)),
            color = Color(0xFF1A1A2E),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text( // Fixed L196 Unresolved reference 'Text'
                    if (isEditing) "Edit API Configuration" else "Add New API Configuration",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(24.dp))

                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }, // Fixed L235 Unresolved reference 'Text'
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = createTextFieldColors( // Fixed L556 Argument type mismatch
                        TextFieldColorData(
                            focusedContainerColor = Color(0xFF2A2A4A),
                            unfocusedContainerColor = Color(0xFF2A2A4A),
                            focusedIndicatorColor = Color(0xFF4ECDC4),
                            unfocusedIndicatorColor = Color(0xFF4ECDC4).copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                )
                Spacer(Modifier.height(16.dp))

                // API Key Input
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") }, // Fixed L282 Unresolved reference 'Text'
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Password
                    ),
                    colors = createTextFieldColors( // Fixed L618 Type mismatch
                        TextFieldColorData(
                            focusedContainerColor = Color(0xFF2A2A4A),
                            unfocusedContainerColor = Color(0xFF2A2A4A),
                            focusedIndicatorColor = Color(0xFF4ECDC4),
                            unfocusedIndicatorColor = Color(0xFF4ECDC4).copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                )
                Spacer(Modifier.height(16.dp))

                // Provider Dropdown (Not fully implemented here, assuming basic Text display)
                Text( // Fixed L300 Unresolved reference 'Text'
                    "Provider: ${provider.name}",
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = createButtonColors(
                            ButtonColorData(
                                containerColor = Color.Gray.copy(alpha = 0.3f),
                                contentColor = Color.White,
                                disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                                disabledContentColor = Color.White
                            )
                        )
                    ) {
                        Text("Cancel") // Fixed L339 Unresolved reference 'Text'
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val newConfig = ApiConfig(
                                id = apiConfig?.id ?: System.currentTimeMillis().toString(),
                                name = name,
                                apiKey = apiKey,
                                provider = provider,
                                isActive = apiConfig?.isActive ?: true // Defaults to active
                            )
                            viewModel.updateApiConfig(newConfig)
                            onDismiss()
                        },
                        enabled = name.isNotBlank() && apiKey.isNotBlank(),
                        colors = createButtonColors(
                            ButtonColorData(
                                containerColor = Color(0xFF667EEA),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF667EEA).copy(alpha = 0.5f),
                                disabledContentColor = Color.White.copy(alpha = 0.5f)
                            )
                        )
                    ) {
                        Text(if (isEditing) "Save" else "Add") // Fixed L343 Unresolved reference 'Text'
                    }
                }
            }
        }
    }
}

// Custom/Utility functions (start of the original file's custom utility section)

@Composable
fun createTextFieldColors(data: TextFieldColorData): TextFieldColors {
    // Corrected to use Material 3 TextFieldDefaults.outlinedTextFieldColors for OutlinedTextField
    return TextFieldDefaults.outlinedTextFieldColors(
        focusedContainerColor = data.focusedContainerColor,
        unfocusedContainerColor = data.unfocusedContainerColor,
        focusedBorderColor = data.focusedIndicatorColor,
        unfocusedBorderColor = data.unfocusedIndicatorColor,
        focusedTextColor = data.focusedTextColor,
        unfocusedTextColor = data.unfocusedTextColor
    )
}

data class TextFieldColorData(
    val focusedContainerColor: Color,
    val unfocusedContainerColor: Color,
    val focusedIndicatorColor: Color,
    val unfocusedIndicatorColor: Color,
    val focusedTextColor: Color,
    val unfocusedTextColor: Color
)

// Added ButtonColorData and createButtonColors for inter-file compatibility
data class ButtonColorData(
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color
)

@Composable
fun createButtonColors(data: ButtonColorData): ButtonColors {
    return ButtonDefaults.buttonColors(
        containerColor = data.containerColor,
        contentColor = data.contentColor,
        disabledContainerColor = data.disabledContainerColor,
        disabledContentColor = data.disabledContentColor
    )
}

@Composable
fun ColumnScope.RowScope() {}

class PasswordVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            AnnotatedString("•".repeat(text.length)),
            OffsetMapping.Identity
        )
    }
}

interface VisualTransformation {
    fun filter(text: AnnotatedString): TransformedText
}

data class TransformedText(val text: AnnotatedString, val offsetMapping: OffsetMapping)

interface OffsetMapping {
    fun originalToTransformed(offset: Int): Int
    fun transformedToOriginal(offset: Int): Int

    companion object {
        val Identity = object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = offset
            override fun transformedToOriginal(offset: Int) = offset
        }
    }
}

class AnnotatedString(val text: String) {
    val length get() = text.length
}

@Composable
fun Spacer(modifier: Modifier) {
    Box(modifier = modifier)
}

@Composable
fun Icon(emoji: String, modifier: Modifier = Modifier) {
    Text(emoji, modifier = modifier)
}