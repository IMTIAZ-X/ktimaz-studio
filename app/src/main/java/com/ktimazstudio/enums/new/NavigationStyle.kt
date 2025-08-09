package com.ktimazstudio.enums

// Navigation Style Options
enum class NavigationStyle(val displayName: String) {
    BOTTOM_BAR("Bottom Navigation"),
    NAVIGATION_RAIL("Side Navigation"),
    PERSISTENT_DRAWER("Persistent Drawer"),
    AUTO("Automatic (Based on Screen Size)")
}

// Layout Density Options
enum class LayoutDensity(val displayName: String, val scaleFactor: Float) {
    COMPACT("Compact", 0.85f),
    COMFORTABLE("Comfortable", 1.0f),
    SPACIOUS("Spacious", 1.15f)
}

// Animation Speed Options
enum class AnimationSpeed(val displayName: String, val multiplier: Float) {
    SLOW("Slow", 1.5f),
    NORMAL("Normal", 1.0f),
    FAST("Fast", 0.75f),
    OFF("No Animations", 0f)
}

// Dashboard View Types
enum class DashboardViewType {
    GRID,
    LIST,
    COMPACT_GRID
}

// Card Size Options
enum class CardSize(val displayName: String) {
    SMALL("Small"),
    MEDIUM("Medium"), 
    LARGE("Large")
}

// Security Level Indicators
enum class SecurityLevel(val displayName: String, val color: Long) {
    LOW("Low Security", 0xFFFF5722),
    MEDIUM("Medium Security", 0xFFFF9800),
    HIGH("High Security", 0xFF4CAF50),
    CRITICAL("Critical Alert", 0xFFF44336)
}