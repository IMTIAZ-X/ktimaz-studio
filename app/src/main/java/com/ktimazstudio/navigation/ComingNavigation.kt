// File: ComingActivity.kt (Updated to use existing structure)
package com.ktimazstudio

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class ComingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val cardTitle = intent.getStringExtra("CARD_TITLE") ?: "New Feature"

        setContent {
            ktimaz {
                // Show only the coming soon screen without complex navigation
                SimpleComingSoon(cardTitle) {
                    finish()
                }
            }
        }
    }
}

@Composable
fun ktimaz(content: @Composable () -> Unit) {
    val isDarkTheme = isSystemInDarkTheme()
    
    MaterialTheme(
        colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme(),
        content = content
    )
}

@Composable
fun SimpleComingSoon(title: String, onBackClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    
    // Enhanced gradient colors based on theme
    val isDarkTheme = MaterialTheme.colorScheme.surface == darkColorScheme().surface
    val primaryGradient = if (isDarkTheme) {
        listOf(
            Color(0xFF0F0F23),  // Deep space blue
            Color(0xFF1A1A3A),  // Rich dark purple
            Color(0xFF2D1B4E)   // Mysterious purple
        )
    } else {
        listOf(
            Color(0xFFE3F2FD),  // Light blue
            Color(0xFFF3E5F5),  // Light purple
            Color(0xFFE8F5E8)   // Light green
        )
    }
    
    val accentGradient = if (isDarkTheme) {
        listOf(
            Color(0xFF667EEA),  // Electric blue
            Color(0xFF764BA2),  // Royal purple
            Color(0xFF96A8FB)   // Soft lavender
        )
    } else {
        listOf(
            Color(0xFF4A90E2),  // Professional blue
            Color(0xFF7B68EE),  // Medium slate blue
            Color(0xFF6A5ACD)   // Slate blue
        )
    }
    
    // Simple coming soon items
    val comingSoonItems = remember {
        listOf(
            ComingSoonItem(
                title = "Dashboard",
                subtitle = "Overview and analytics",
                icon = Icons.Filled.Dashboard,
                color = accentGradient[0]
            ),
            
            ComingSoonItem(
                title = "Features",
                subtitle = "Explore capabilities", 
                icon = Icons.Filled.Star,
                color = accentGradient[1]
            ),
            
            ComingSoonItem(
                title = "Documentation",
                subtitle = "Learn and guides",
                icon = Icons.Filled.MenuBook,
                color = accentGradient[2]
            ),
            
            ComingSoonItem(
                title = "Settings",
                subtitle = "Configure preferences",
                icon = Icons.Filled.Settings,
                color = Color(0xFF4CAF50)
            ),
            
            ComingSoonItem(
                title = "Contact",
                subtitle = "Get support",
                icon = Icons.Filled.ContactSupport,
                color = Color(0xFFFF9800)
            )
        )
    }

    // Background with animated gradient
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
            comingSoonItems = comingSoonItems,
            accentGradient = accentGradient,
            onBackClick = onBackClick
        )
    }
}

// Data class for coming soon items
data class ComingSoonItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun MainComingSoonScreen(
    title: String,
    comingSoonItems: List<ComingSoonItem>,
    accentGradient: List<Color>,
    onBackClick: () -> Unit
) {
    // Animation states
    val headerAnimation = remember { Animatable(0f) }
    val cardsAnimation = remember { Animatable(0f) }
    val pulseAnimation = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulseAnimation.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    LaunchedEffect(Unit) {
        headerAnimation.animateTo(1f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 100f))
        delay(300)
        cardsAnimation.animateTo(1f, animationSpec = spring(dampingRatio = 0.7f, stiffness = 80f))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(headerAnimation.value)
                .graphicsLayer {
                    translationY = -50f * (1f - headerAnimation.value)
                }
        ) {
            // Animated Logo/Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(accentGradient),
                        shape = CircleShape
                    )
                    .border(
                        width = 3.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = pulseAlpha),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Coming Soon - Experience the Future",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Coming Soon Cards
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .weight(1f)
                .alpha(cardsAnimation.value)
        ) {
            items(comingSoonItems.chunked(2)) { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowItems.forEach { item ->
                        ComingSoonCard(
                            item = item,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill remaining space if odd number of items
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Return Button
        PremiumReturnButton(
            onClick = onBackClick,
            accentGradient = accentGradient
        )
    }
}

@Composable
fun ComingSoonCard(
    item: ComingSoonItem,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "card_scale"
    )

    Card(
        modifier = modifier
            .height(120.dp)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                // Just show a visual feedback, no navigation
            }
            .shadow(
                elevation = if (isPressed) 20.dp else 12.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = item.color.copy(alpha = 0.3f),
                spotColor = item.color.copy(alpha = 0.5f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            item.color.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = item.color
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = item.title,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = item.subtitle,
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

@Composable
fun PremiumReturnButton(
    onClick: () -> Unit,
    accentGradient: List<Color>
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "button_scale"
    )
    val haptic = LocalHapticFeedback.current

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            isPressed = true
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(56.dp)
            .scale(scale)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = accentGradient.first().copy(alpha = 0.4f)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(accentGradient),
                    shape = RoundedCornerShape(28.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Return to Dashboard",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}
