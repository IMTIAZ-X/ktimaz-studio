package com.ktimazstudio.enums

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class ModuleCategory(val displayName: String, val icon: ImageVector) {
    MEDIA("Media", Icons.Filled.PlayArrow),
    TOOLS("Tools", Icons.Filled.Build),
    SECURITY("Security", Icons.Filled.Security),
    SYSTEM("System", Icons.Filled.Settings),
    NETWORK("Network", Icons.Filled.Wifi),
    ANALYTICS("Analytics", Icons.Filled.Analytics),
    CREATIVE("Creative", Icons.Filled.Brush),
    PRODUCTIVITY("Productivity", Icons.Filled.Work),
    ENTERTAINMENT("Entertainment", Icons.Filled.Games),
    UTILITIES("Utilities", Icons.Filled.Construction)
}