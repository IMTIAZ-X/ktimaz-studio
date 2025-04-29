package com.ktimazstudio

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ktimazstudio.ui.theme.ktimaz

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ktimaz {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "KTiMAZ V5 Legendary++",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.15f),
                    scrolledContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.25f)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0f)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                        )
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .blur(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    )
            )

            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val items = listOf(
                    "Call" to R.mipmap.ic_launcher,
                    "Message" to R.mipmap.ic_launcher,
                    "Nagad" to R.mipmap.ic_launcher,
                    "IP Scan" to R.mipmap.ic_launcher,
                    "Movies" to R.mipmap.ic_launcher,
                    "Player" to R.mipmap.ic_launcher,
                    "Gallery" to R.mipmap.ic_launcher,
                    "Browser" to R.mipmap.ic_launcher,
                    "Security" to R.mipmap.ic_launcher,
                    "Weather" to R.mipmap.ic_launcher
                )

                items.chunked(2).forEach { pair ->
                    if (pair.size == 2) {
                        CardGrid(
                            pair[0].first, pair[0].second, {},
                            pair[1].first, pair[1].second, {}
                        )
                    } else {
                        CardGrid(
                            pair[0].first, pair[0].second, {},
                            "", 0, {}
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CardGrid(
    title1: String,
    icon1: Int,
    onClick1: () -> Unit,
    title2: String,
    icon2: Int,
    onClick2: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CardItem(
            title = title1,
            icon = if (icon1 != 0) painterResource(id = icon1) else null,
            onClick = onClick1,
            modifier = Modifier.weight(1f)
        )
        if (title2.isNotEmpty()) {
            CardItem(
                title = title2,
                icon = if (icon2 != 0) painterResource(id = icon2) else null,
                onClick = onClick2,
                modifier = Modifier.weight(1f)
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun CardItem(title: String, icon: Painter?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val animationSpec = tween<Float>(durationMillis = 600, easing = FastOutSlowInEasing)
    val scale by animateFloatAsState(targetValue = 1f, animationSpec = animationSpec, label = "scale")

    Card(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                        )
                    )
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                icon?.let {
                    Image(
                        painter = it,
                        contentDescription = title,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
