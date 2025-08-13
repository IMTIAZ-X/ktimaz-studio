// File: navigation/ComingNavigation.kt
package com.ktimazstudio.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ktimazstudio.ui.screens.ModernProfessionalComingSoon

/**
 * Navigation integration for ComingActivity features
 * This replaces the separate ComingActivity with navigation-based approach
 */
@Composable
fun ComingNavigation(
    cardTitle: String,
    onBackPressed: () -> Unit
) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "coming_soon",
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
            ) + fadeIn(animationSpec = tween(400))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
            ) + fadeOut(animationSpec = tween(400))
        }
    ) {
        composable("coming_soon") {
            ModernProfessionalComingSoon(
                title = cardTitle,
                onBackClick = onBackPressed,
                navController = navController
            )
        }
        
        composable("dashboard_preview") {
            DashboardPreviewScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable("features_detail") {
            FeaturesDetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable("documentation") {
            DocumentationScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable("settings_preview") {
            SettingsPreviewScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable("contact_form") {
            ContactFormScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

// Updated ModernProfessionalComingSoon to work with navigation
@Composable
fun ModernProfessionalComingSoon(
    title: String,
    onBackClick: () -> Unit,
    navController: NavController
) {
    var currentScreen by remember { mutableStateOf("main") }
    
    // Enhanced gradient colors based on theme
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    val primaryGradient = if (isDarkTheme) {
        listOf(
            androidx.compose.ui.graphics.Color(0xFF0F0F23),
            androidx.compose.ui.graphics.Color(0xFF1A1A3A),
            androidx.compose.ui.graphics.Color(0xFF2D1B4E)
        )
    } else {
        listOf(
            androidx.compose.ui.graphics.Color(0xFFE3F2FD),
            androidx.compose.ui.graphics.Color(0xFFF3E5F5),
            androidx.compose.ui.graphics.Color(0xFFE8F5E8)
        )
    }
    
    val accentGradient = if (isDarkTheme) {
        listOf(
            androidx.compose.ui.graphics.Color(0xFF667EEA),
            androidx.compose.ui.graphics.Color(0xFF764BA2),
            androidx.compose.ui.graphics.Color(0xFF96A8FB)
        )
    } else {
        listOf(
            androidx.compose.ui.graphics.Color(0xFF4A90E2),
            androidx.compose.ui.graphics.Color(0xFF7B68EE),
            androidx.compose.ui.graphics.Color(0xFF6A5ACD)
        )
    }
    
    // Navigation items with navigation actions
    val navigationItems = remember {
        listOf(
            NavigationItem(
                title = "Dashboard",
                subtitle = "Overview and analytics",
                icon = androidx.compose.material.icons.Icons.Filled.Dashboard,
                color = accentGradient[0]
            ) { navController.navigate("dashboard_preview") },
            
            NavigationItem(
                title = "Features",
                subtitle = "Explore capabilities",
                icon = androidx.compose.material.icons.Icons.Filled.Star,
                color = accentGradient[1]
            ) { navController.navigate("features_detail") },
            
            NavigationItem(
                title = "Documentation",
                subtitle = "Learn and guides",
                icon = androidx.compose.material.icons.Icons.Filled.MenuBook,
                color = accentGradient[2]
            ) { navController.navigate("documentation") },
            
            NavigationItem(
                title = "Settings",
                subtitle = "Configure preferences",
                icon = androidx.compose.material.icons.Icons.Filled.Settings,
                color = androidx.compose.ui.graphics.Color(0xFF4CAF50)
            ) { navController.navigate("settings_preview") },
            
            NavigationItem(
                title = "Contact",
                subtitle = "Get support",
                icon = androidx.compose.material.icons.Icons.Filled.ContactSupport,
                color = androidx.compose.ui.graphics.Color(0xFFFF9800)
            ) { navController.navigate("contact_form") }
        )
    }

    // Use the existing ComingSoon UI but with navigation integration
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = primaryGradient,
                    radius = 1500f,
                    center = androidx.compose.ui.geometry.Offset(0.3f, 0.2f)
                )
            )
    ) {
        MainComingSoonScreen(
            title = title,
            navigationItems = navigationItems,
            accentGradient = accentGradient,
            onBackClick = onBackClick
        )
    }
}

// Preview Screens for Navigation
@Composable
private fun DashboardPreviewScreen(onBackClick: () -> Unit) {
    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        PreviewScreenHeader(
            title = "Dashboard Preview",
            subtitle = "Analytics & Data Visualization",
            icon = androidx.compose.material.icons.Icons.Filled.Dashboard,
            onBackClick = onBackClick
        )
        
        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(24.dp))
        
        androidx.compose.foundation.lazy.LazyColumn(
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            items(5) { index ->
                androidx.compose.material3.Card(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    )
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = androidx.compose.ui.Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = androidx.compose.ui.Alignment.CenterStart
                    ) {
                        androidx.compose.foundation.layout.Column {
                            androidx.compose.material3.Text(
                                text = "Analytics Widget ${index + 1}",
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                            )
                            androidx.compose.material3.Text(
                                text = "Real-time data visualization and insights",
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        PreviewScreenHeader(
            title = "Enhanced Features",
            subtitle = "Cutting-edge Capabilities",
            icon = androidx.compose.material.icons.Icons.Filled.Star,
            onBackClick = onBackClick
        )
        
        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(24.dp))
        
        val features = listOf(
            "AI-Powered Analytics" to "Machine learning insights and predictions",
            "Real-time Collaboration" to "Multi-user synchronization and sharing",
            "Advanced Security" to "End-to-end encryption and biometric auth",
            "Cloud Integration" to "Seamless backup and sync across devices",
            "Custom Workflows" to "Automated task management and scheduling",
            "Smart Notifications" to "Intelligent filtering and prioritization"
        )
        
        androidx.compose.foundation.lazy.LazyColumn(
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            items(features) { (title, description) ->
                androidx.compose.material3.Card(
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = androidx.compose.ui.Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                            modifier = androidx.compose.ui.Modifier.size(24.dp)
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(16.dp))
                        androidx.compose.foundation.layout.Column {
                            androidx.compose.material3.Text(
                                text = title,
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                            )
                            androidx.compose.material3.Text(
                                text = description,
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        PreviewScreenHeader(
            title = "Documentation",
            subtitle = "Guides & Resources",
            icon = androidx.compose.material.icons.Icons.Filled.MenuBook,
            onBackClick = onBackClick
        )
        
        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(24.dp))
        
        val docs = listOf(
            "Quick Start Guide" to androidx.compose.material.icons.Icons.Filled.PlayArrow,
            "API Documentation" to androidx.compose.material.icons.Icons.Filled.Code,
            "Video Tutorials" to androidx.compose.material.icons.Icons.Filled.OndemandVideo,
            "Best Practices" to androidx.compose.material.icons.Icons.Filled.Star,
            "Troubleshooting" to androidx.compose.material.icons.Icons.Filled.Help,
            "Community Forum" to androidx.compose.material.icons.Icons.Filled.Forum
        )
        
        androidx.compose.foundation.lazy.LazyColumn(
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            items(docs) { (title, icon) ->
                androidx.compose.material3.Card(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .clickable { /* Handle click */ },
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = androidx.compose.ui.Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            modifier = androidx.compose.ui.Modifier.size(24.dp)
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(16.dp))
                        androidx.compose.material3.Text(
                            text = title,
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                            modifier = androidx.compose.ui.Modifier.weight(1f)
                        )
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.ChevronRight,
                            contentDescription = null,
                            modifier = androidx.compose.ui.Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsPreviewScreen(onBackClick: () -> Unit) {
    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        PreviewScreenHeader(
            title = "Settings Preview",
            subtitle = "Customization Options",
            icon = androidx.compose.material.icons.Icons.Filled.Settings,
            onBackClick = onBackClick
        )
        
        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(24.dp))
        
        val settings = listOf(
            "Theme & Appearance" to androidx.compose.material.icons.Icons.Filled.Palette,
            "Privacy & Security" to androidx.compose.material.icons.Icons.Filled.Security,
            "Notifications" to androidx.compose.material.icons.Icons.Filled.Notifications,
            "Data & Storage" to androidx.compose.material.icons.Icons.Filled.Storage,
            "Language & Region" to androidx.compose.material.icons.Icons.Filled.Language,
            "Advanced Options" to androidx.compose.material.icons.Icons.Filled.Build
        )
        
        androidx.compose.foundation.lazy.LazyColumn(
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            items(settings) { (title, icon) ->
                androidx.compose.material3.Card(
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = androidx.compose.ui.Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            modifier = androidx.compose.ui.Modifier.size(24.dp)
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(16.dp))
                        androidx.compose.material3.Text(
                            text = title,
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                            modifier = androidx.compose.ui.Modifier.weight(1f)
                        )
                        androidx.compose.material3.Switch(
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
    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        PreviewScreenHeader(
            title = "Contact Support",
            subtitle = "Get Help & Feedback",
            icon = androidx.compose.material.icons.Icons.Filled.ContactSupport,
            onBackClick = onBackClick
        )
        
        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(24.dp))
        
        androidx.compose.material3.Card(
            modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = androidx.compose.ui.Modifier.padding(24.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Filled.Support,
                    contentDescription = null,
                    modifier = androidx.compose.ui.Modifier.size(64.dp),
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
                
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                
                androidx.compose.material3.Text(
                    text = "We're Here to Help!",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
                
                androidx.compose.material3.Text(
                    text = "Our support team is ready to assist you with any questions or issues you may have.",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(24.dp))
                
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    androidx.compose.material3.Button(
                        onClick = { },
                        modifier = androidx.compose.ui.Modifier.weight(1f)
                    ) {
                        androidx.compose.material3.Text("Email Support")
                    }
                    
                    androidx.compose.material3.OutlinedButton(
                        onClick = { },
                        modifier = androidx.compose.ui.Modifier.weight(1f)
                    ) {
                        androidx.compose.material3.Text("Live Chat")
                    }
                }
                
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                
                androidx.compose.material3.Text(
                    text = "📧 support@ktimazstudio.com\n📞 +1 (555) 123-4567",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun PreviewScreenHeader(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onBackClick: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        modifier = androidx.compose.ui.Modifier.fillMaxWidth()
    ) {
        androidx.compose.material3.IconButton(
            onClick = onBackClick,
            modifier = androidx.compose.ui.Modifier
                .size(48.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(
                    androidx.compose.material3.MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
        ) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
            )
        }
        
        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(16.dp))
        
        androidx.compose.foundation.layout.Column(modifier = androidx.compose.ui.Modifier.weight(1f)) {
            androidx.compose.material3.Text(
                text = title,
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            )
            androidx.compose.material3.Text(
                text = subtitle,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier
                .size(56.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )
                ),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = androidx.compose.ui.Modifier.size(28.dp),
                tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
            )
        }
    }
}