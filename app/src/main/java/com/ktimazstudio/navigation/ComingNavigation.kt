package com.ktimazstudio.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

data class NavigationItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val action: () -> Unit
)

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

// FIXED: Added ModernProfessionalComingSoon composable
@Composable
fun ModernProfessionalComingSoon(
    title: String,
    onBackClick: () -> Unit,
    navController: NavController
) {
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    val primaryGradient = if (isDarkTheme) {
        listOf(
            Color(0xFF0F0F23),
            Color(0xFF1A1A3A),
            Color(0xFF2D1B4E)
        )
    } else {
        listOf(
            Color(0xFFE3F2FD),
            Color(0xFFF3E5F5),
            Color(0xFFE8F5E8)
        )
    }
    
    val accentGradient = if (isDarkTheme) {
        listOf(
            Color(0xFF667EEA),
            Color(0xFF764BA2),
            Color(0xFF96A8FB)
        )
    } else {
        listOf(
            Color(0xFF4A90E2),
            Color(0xFF7B68EE),
            Color(0xFF6A5ACD)
        )
    }
    
    // FIXED: Updated navigation items with proper types
    val navigationItems = remember {
        listOf(
            NavigationItem(
                title = "Dashboard",
                subtitle = "Overview and analytics",
                icon = Icons.Filled.Dashboard,
                color = accentGradient[0]
            ) { navController.navigate("dashboard_preview") },
            
            NavigationItem(
                title = "Features",
                subtitle = "Explore capabilities",
                icon = Icons.Filled.Star,
                color = accentGradient[1]
            ) { navController.navigate("features_detail") },
            
            NavigationItem(
                title = "Documentation",
                subtitle = "Learn and guides",
                icon = Icons.Filled.MenuBook,
                color = accentGradient[2]
            ) { navController.navigate("documentation") },
            
            NavigationItem(
                title = "Settings",
                subtitle = "Configure preferences",
                icon = Icons.Filled.Settings,
                color = Color(0xFF4CAF50)
            ) { navController.navigate("settings_preview") },
            
            NavigationItem(
                title = "Contact",
                subtitle = "Get support",
                icon = Icons.Filled.ContactSupport,
                color = Color(0xFFFF9800)
            ) { navController.navigate("contact_form") }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
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

@Composable
private fun MainComingSoonScreen(
    title: String,
    navigationItems: List<NavigationItem>,
    accentGradient: List<Color>,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
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
            
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Coming soon content
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Construction,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Coming Soon",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "We're working hard to bring you this amazing feature. Stay tuned for updates!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                
                // Navigation items
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(navigationItems) { item ->
                        Card(
                            onClick = item.action,
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
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = item.color,
                                    modifier = Modifier.size(24.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = item.subtitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
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
    }
}