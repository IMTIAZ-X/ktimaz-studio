
package com.ktimazstudio

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Unique, modern "Under Development" screen.
 * Can be dropped anywhere: ComingNovaScreen()
 */
@Composable
fun ComingNovaScreen(
    modifier: Modifier = Modifier,
    title: String = "Under Development",
    message: String = "Hard work in progress. Please check back soon."
) {
    // Animated gradient background
    val infinite = rememberInfiniteTransition(label = "bg")
    val shift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shift"
    )

    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)
        ),
        start = androidx.compose.ui.geometry.Offset(0f, shift),
        end = androidx.compose.ui.geometry.Offset(shift, 0f)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Soft glow blobs (subtle depth)
        GlowBlob(offsetX = -120.dp, offsetY = -140.dp, size = 220.dp)
        GlowBlob(offsetX = 140.dp, offsetY = 160.dp, size = 260.dp, alpha = 0.35f)

        // Card with animated header
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Animated progress bar shimmer
                NovaHeader(title = title)

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    ),
                    textAlign = TextAlign.Center
                )

                // Subtle pulsing chip
                NovaChip()
            }
        }
    }
}

@Composable
private fun NovaHeader(title: String) {
    val infinite = rememberInfiniteTransition(label = "header")
    val scale by infinite.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val progress by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.sp
            ),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))

        // Animated progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0.15f, 1f))
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.primary
                            )
                        )
                    )
                    .blur(0.5.dp)
            )
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = "Building a better experience…",
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
        )
    }
}

@Composable
private fun NovaChip() {
    val infinite = rememberInfiniteTransition(label = "chip")
    val alpha by infinite.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    Surface(
        shape = RoundedCornerShape(999.dp),
        tonalElevation = 2.dp,
        shadowElevation = 0.dp,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f * alpha)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            text = "Hard work in progress",
            style = MaterialTheme.typography.labelLarge.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
private fun GlowBlob(
    offsetX: androidx.compose.ui.unit.Dp,
    offsetY: androidx.compose.ui.unit.Dp,
    size: androidx.compose.ui.unit.Dp,
    alpha: Float = 0.25f
) {
    Box(
        modifier = Modifier
            .offset(x = offsetX, y = offsetY)
            .size(size)
            .blur(60.dp)
            .clip(RoundedCornerShape(200.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                        Color.Transparent
                    )
                )
            )
    )
}
