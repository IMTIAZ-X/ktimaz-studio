// NavigationStyle.kt
package com.ktimazstudio.enums

enum class NavigationStyle {
    RAIL, BOTTOM_BAR, DRAWER, TABS
}

// LayoutDensity.kt
package com.ktimazstudio.enums

enum class LayoutDensity {
    COMPACT, MEDIUM, COMFORTABLE, SPACIOUS
}

// AnimationSpeed.kt
package com.ktimazstudio.enums

enum class AnimationSpeed(val multiplier: Float) {
    DISABLED(0f),
    SLOW(0.5f),
    NORMAL(1f),
    FAST(1.5f),
    INSTANT(3f)
}

// DashboardViewType.kt
package com.ktimazstudio.enums

enum class DashboardViewType {
    GRID, LIST, CARD_CAROUSEL, MASONRY
}

// CardSize.kt
package com.ktimazstudio.enums

enum class CardSize(val dpSize: Int) {
    SMALL(120),
    MEDIUM(160),
    LARGE(200),
    EXTRA_LARGE(240)
}

// ModuleCategory.kt
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