package com.ktimazstudio.managers

import android.content.Context
import android.content.SharedPreferences
import com.ktimazstudio.enums.ThemeSetting
import com.ktimazstudio.enums.NavigationStyle
import com.ktimazstudio.enums.LayoutDensity
import com.ktimazstudio.enums.AnimationSpeed
import com.ktimazstudio.enums.DashboardViewType
import com.ktimazstudio.enums.CardSize

class SharedPreferencesManager(context: Context) {
    val prefs: SharedPreferences = context.getSharedPreferences("AppPrefsKtimazStudio", Context.MODE_PRIVATE)
    private val securePrefs: SharedPreferences = context.getSharedPreferences("SecurePrefsKtimaz", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in_key"
        private const val KEY_USERNAME = "username_key"
        const val KEY_THEME_SETTING = "theme_setting_key"
        const val KEY_SOUND_ENABLED = "sound_enabled_key"
        
        const val KEY_OLD_UI_ENABLED = "old_ui_enabled_key"
        const val KEY_NAVIGATION_STYLE = "navigation_style_key"
        const val KEY_LAYOUT_DENSITY = "layout_density_key"
        const val KEY_ANIMATION_SPEED = "animation_speed_key"
        const val KEY_DASHBOARD_VIEW_TYPE = "dashboard_view_type_key"
        const val KEY_CARD_SIZE = "card_size_key"
        const val KEY_HAPTIC_FEEDBACK = "haptic_feedback_key"
        const val KEY_AUTO_THEME_SWITCH = "auto_theme_switch_key"
        const val KEY_SECURE_MODE = "secure_mode_key"
        const val KEY_DEBUG_MODE = "debug_mode_key"
        
        private const val SECURE_KEY_USER_TOKEN = "user_token_secure"
        private const val SECURE_KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    }

    // Existing Methods
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun setLoggedIn(loggedIn: Boolean, username: String? = null) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, loggedIn)
            if (loggedIn && username != null) {
                putString(KEY_USERNAME, username)
            } else if (!loggedIn) {
                remove(KEY_USERNAME)
            }
            apply()
        }
    }

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    fun getThemeSetting(): ThemeSetting {
        val themeString = prefs.getString(KEY_THEME_SETTING, ThemeSetting.SYSTEM.name)
        return try {
            ThemeSetting.valueOf(themeString ?: ThemeSetting.SYSTEM.name)
        } catch (e: IllegalArgumentException) {
            ThemeSetting.SYSTEM
        }
    }

    fun setThemeSetting(themeSetting: ThemeSetting) {
        prefs.edit().putString(KEY_THEME_SETTING, themeSetting.name).apply()
    }

    fun isSoundEnabled(): Boolean = prefs.getBoolean(KEY_SOUND_ENABLED, true)

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    // New UI/UX Settings
    fun isOldUiEnabled(): Boolean = prefs.getBoolean(KEY_OLD_UI_ENABLED, false)

    fun setOldUiEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_OLD_UI_ENABLED, enabled).apply()
    }

    fun getNavigationStyle(): NavigationStyle {
        val styleString = prefs.getString(KEY_NAVIGATION_STYLE, NavigationStyle.RAIL.name)
        return try {
            NavigationStyle.valueOf(styleString ?: NavigationStyle.RAIL.name)
        } catch (e: IllegalArgumentException) {
            NavigationStyle.RAIL
        }
    }

    fun setNavigationStyle(style: NavigationStyle) {
        prefs.edit().putString(KEY_NAVIGATION_STYLE, style.name).apply()
    }

    fun getLayoutDensity(): LayoutDensity {
        val densityString = prefs.getString(KEY_LAYOUT_DENSITY, LayoutDensity.MEDIUM.name)
        return try {
            LayoutDensity.valueOf(densityString ?: LayoutDensity.MEDIUM.name)
        } catch (e: IllegalArgumentException) {
            LayoutDensity.MEDIUM
        }
    }

    fun setLayoutDensity(density: LayoutDensity) {
        prefs.edit().putString(KEY_LAYOUT_DENSITY, density.name).apply()
    }

    fun getAnimationSpeed(): AnimationSpeed {
        val speedString = prefs.getString(KEY_ANIMATION_SPEED, AnimationSpeed.NORMAL.name)
        return try {
            AnimationSpeed.valueOf(speedString ?: AnimationSpeed.NORMAL.name)
        } catch (e: IllegalArgumentException) {
            AnimationSpeed.NORMAL
        }
    }

    fun setAnimationSpeed(speed: AnimationSpeed) {
        prefs.edit().putString(KEY_ANIMATION_SPEED, speed.name).apply()
    }

    fun getDashboardViewType(): DashboardViewType {
        val typeString = prefs.getString(KEY_DASHBOARD_VIEW_TYPE, DashboardViewType.GRID.name)
        return try {
            DashboardViewType.valueOf(typeString ?: DashboardViewType.GRID.name)
        } catch (e: IllegalArgumentException) {
            DashboardViewType.GRID
        }
    }

    fun setDashboardViewType(viewType: DashboardViewType) {
        prefs.edit().putString(KEY_DASHBOARD_VIEW_TYPE, viewType.name).apply()
    }

    fun getCardSize(): CardSize {
        val sizeString = prefs.getString(KEY_CARD_SIZE, CardSize.MEDIUM.name)
        return try {
            CardSize.valueOf(sizeString ?: CardSize.MEDIUM.name)
        } catch (e: IllegalArgumentException) {
            CardSize.MEDIUM
        }
    }

    fun setCardSize(size: CardSize) {
        prefs.edit().putString(KEY_CARD_SIZE, size.name).apply()
    }

    fun isHapticFeedbackEnabled(): Boolean = prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true)

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC_FEEDBACK, enabled).apply()
    }

    fun isAutoThemeSwitchEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_THEME_SWITCH, false)

    fun setAutoThemeSwitchEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_THEME_SWITCH, enabled).apply()
    }

    fun isSecureModeEnabled(): Boolean = prefs.getBoolean(KEY_SECURE_MODE, true)

    fun setSecureModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SECURE_MODE, enabled).apply()
    }

    fun isDebugModeEnabled(): Boolean = prefs.getBoolean(KEY_DEBUG_MODE, false)

    fun setDebugModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DEBUG_MODE, enabled).apply()
    }

    // FIXED: Simple secure storage methods without encryption
    fun setUserToken(token: String) {
        securePrefs.edit().putString(SECURE_KEY_USER_TOKEN, token).apply()
    }

    fun getUserToken(): String? {
        return securePrefs.getString(SECURE_KEY_USER_TOKEN, null)
    }

    fun isBiometricEnabled(): Boolean = securePrefs.getBoolean(SECURE_KEY_BIOMETRIC_ENABLED, false)

    fun setBiometricEnabled(enabled: Boolean) {
        securePrefs.edit().putBoolean(SECURE_KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    // Utility Methods
    fun clearAllData() {
        prefs.edit().clear().apply()
        securePrefs.edit().clear().apply()
    }

    fun clearUserData() {
        prefs.edit().apply {
            remove(KEY_IS_LOGGED_IN)
            remove(KEY_USERNAME)
            apply()
        }
        securePrefs.edit().apply {
            remove(SECURE_KEY_USER_TOKEN)
            apply()
        }
    }

    fun exportSettings(): Map<String, Any?> {
        return prefs.all.filterKeys { key ->
            !key.contains("login") && !key.contains("username")
        }
    }

    fun importSettings(settings: Map<String, Any?>) {
        prefs.edit().apply {
            settings.forEach { (key, value) ->
                when (value) {
                    is Boolean -> putBoolean(key, value)
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Float -> putFloat(key, value)
                    is Long -> putLong(key, value)
                }
            }
            apply()
        }
    }
}

// Preview Screens for Navigation
@Composable
private fun DashboardPreviewScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        PreviewScreenHeader(
            title = "Dashboard Preview",
            subtitle = "Analytics & Data Visualization",
            icon = Icons.Filled.Dashboard,
            onBackClick = onBackClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(5) { index ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Column {
                            Text(
                                text = "Analytics Widget ${index + 1}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Real-time data visualization and insights",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturesDetailScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        PreviewScreenHeader(
            title = "Enhanced Features",
            subtitle = "Cutting-edge Capabilities",
            icon = Icons.Filled.Star,
            onBackClick = onBackClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        val features = listOf(
            "AI-Powered Analytics" to "Machine learning insights and predictions",
            "Real-time Collaboration" to "Multi-user synchronization and sharing",
            "Advanced Security" to "End-to-end encryption and biometric auth",
            "Cloud Integration" to "Seamless backup and sync across devices",
            "Custom Workflows" to "Automated task management and scheduling",
            "Smart Notifications" to "Intelligent filtering and prioritization"
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(features) { (title, description) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentationScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        PreviewScreenHeader(
            title = "Documentation",
            subtitle = "Guides & Resources",
            icon = Icons.Filled.MenuBook,
            onBackClick = onBackClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        val docs = listOf(
            "Quick Start Guide" to Icons.Filled.PlayArrow,
            "API Documentation" to Icons.Filled.Code,
            "Video Tutorials" to Icons.Filled.OndemandVideo,
            "Best Practices" to Icons.Filled.Star,
            "Troubleshooting" to Icons.Filled.Help,
            "Community Forum" to Icons.Filled.Forum
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(docs) { (title, icon) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsPreviewScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        PreviewScreenHeader(
            title = "Settings Preview",
            subtitle = "Customization Options",
            icon = Icons.Filled.Settings,
            onBackClick = onBackClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        val settings = listOf(
            "Theme & Appearance" to Icons.Filled.Palette,
            "Privacy & Security" to Icons.Filled.Security,
            "Notifications" to Icons.Filled.Notifications,
            "Data & Storage" to Icons.Filled.Storage,
            "Language & Region" to Icons.Filled.Language,
            "Advanced Options" to Icons.Filled.Build
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(settings) { (title, icon) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = false,
                            onCheckedChange = { }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactFormScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        PreviewScreenHeader(
            title = "Contact Support",
            subtitle = "Get Help & Feedback",
            icon = Icons.Filled.ContactSupport,
            onBackClick = onBackClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Support,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "We're Here to Help!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Our support team is ready to assist you with any questions or issues you may have.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Email Support")
                    }
                    
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Live Chat")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "📧 support@ktimazstudio.com\n📞 +1 (555) 123-4567",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun PreviewScreenHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onBackClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(48.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}