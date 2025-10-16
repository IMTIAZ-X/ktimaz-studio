package com.ktimazstudio.ui.screens

import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.ktimazstudio.R
import com.ktimazstudio.managers.SoundEffectManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(modifier: Modifier = Modifier, searchQuery: String, onCardClick: (String) -> Unit, soundEffectManager: SoundEffectManager) {
    val cards = listOf(
        "Spectrum Analyzer", "Image Synthesizer", "Holovid Player", "Neural Net Link",
        "Encrypted Notes", "Quantum Web", "Bio Scanner", "Interface Designer",
        "Sonic Emitter", "AI Core Access", "System Config"
    )
    // Consider adding specific icons for each card for better visual distinction
    val icons = List(cards.size) { painterResource(id = R.mipmap.ic_launcher_round) } // Placeholder: replace with distinct icons
    val haptic = LocalHapticFeedback.current

    val filteredCards = remember(cards, searchQuery) {
        if (searchQuery.isBlank()) {
            cards
        } else {
            cards.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier.fillMaxSize()
    ) {
        itemsIndexed(filteredCards, key = { _, title -> title }) { index, title ->
            var itemVisible by remember { mutableStateOf(false) }
            LaunchedEffect(key1 = title) {
                delay(index * 70L + 100L) // Staggered animation delay
                itemVisible = true
            }

            AnimatedVisibility(
                visible = itemVisible,
                enter = fadeIn(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow)) +
                        slideInVertically(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                            initialOffsetY = { it / 2 }
                        ) +
                        scaleIn(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                            initialScale = 0.75f
                        ),
                exit = fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.85f, animationSpec = tween(150))
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "card_effects_$title")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.995f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(animation = tween(2500, easing = EaseInOutCubic), repeatMode = RepeatMode.Reverse),
                    label = "card_scale_$title"
                )
                val animatedAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.75f,
                    targetValue = 0.60f,
                    animationSpec = infiniteRepeatable(animation = tween(2500, easing = EaseInOutCubic), repeatMode = RepeatMode.Reverse),
                    label = "card_alpha_$title"
                )

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val pressScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.95f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "card_press_scale"
                )
                val pressAlpha by animateFloatAsState(
                    targetValue = if (isPressed) 0.7f else 1.0f,
                    animationSpec = tween(150),
                    label = "card_press_alpha"
                )

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text("Click to open $title module")
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    // MODIFIED: Pass interactionSource directly to Card
                    Card(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCardClick(title)
                        },
                        interactionSource = interactionSource, // Pass interactionSource here
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp).copy(alpha = animatedAlpha)
                        ),
                        border = BorderStroke(width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .graphicsLayer(
                                scaleX = scale * pressScale, // Combine infinite and press animations
                                scaleY = scale * pressScale,
                                alpha = animatedAlpha * pressAlpha // Combine infinite and press animations
                            )
                            .then(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Modifier.blur(2.dp) else Modifier)
                            .fillMaxWidth()
                            .height(170.dp)
                    ) {
                        Column(
                            Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = icons[index % icons.size], // Using placeholder icon
                                contentDescription = title,
                                modifier = Modifier.size(60.dp)
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}