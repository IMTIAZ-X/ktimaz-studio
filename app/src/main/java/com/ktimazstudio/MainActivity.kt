package com.ktimazstudio

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign // <<< ADDED IMPORT FOR TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.ktimazstudio.ui.theme.ktimaz // Assuming this theme exists
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.FileReader

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (detectVpn()) {
            Toast.makeText(this, "VPN detected. Closing app for security...", Toast.LENGTH_LONG).show()
            lifecycleScope.launch {
                delay(5000)
                finishAffinity()
            }
            return
        }

        setContent {
            ktimaz { 
                val context = LocalContext.current
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(Unit) {
                    if (!isConnected(context)) {
                        val result = snackbarHostState.showSnackbar(
                            message = "No Internet Connection!",
                            actionLabel = "Wi-Fi Settings",
                            duration = SnackbarDuration.Indefinite
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            openWifiSettings(context)
                        }
                    }
                }

                val futuristicGradient = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f)
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(futuristicGradient)
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = stringResource(id = R.string.app_name),
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                },
                                actions = {
                                    IconButton(onClick = {
                                        context.startActivity(Intent(context, SettingsActivity::class.java))
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.Settings,
                                            contentDescription = "Settings",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                                modifier = Modifier.statusBarsPadding()
                            )
                        },
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        containerColor = Color.Transparent
                    ) { paddingValues ->
                        AnimatedCardGrid(modifier = Modifier.padding(paddingValues)) { title ->
                            val intent = if (title == "System Config") {
                                Intent(context, SettingsActivity::class.java)
                            } else {
                                Intent(context, ComingActivity::class.java).putExtra("CARD_TITLE", title)
                            }
                            context.startActivity(intent)
                        }
                    }
                }
            }
        }
    }

    private fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun openWifiSettings(context: Context) {
        Toast.makeText(context, "Please enable Wi-Fi or connect to a network.", Toast.LENGTH_LONG).show()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.startActivity(Intent(Settings.Panel.ACTION_WIFI))
        } else {
            @Suppress("DEPRECATION")
            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
    }

    private fun detectVpn(): Boolean = try {
        BufferedReader(FileReader("/proc/net/tcp")).useLines { lines ->
            lines.any { it.contains("0100007F:") || it.contains("00000000:10E1") }
        }
    } catch (_: Exception) {
        false
    }
}

@Composable
fun AnimatedCardGrid(modifier: Modifier = Modifier, onCardClick: (String) -> Unit) {
    val cards = listOf("Spectrum Analyzer", "Image Synthesizer", "Holovid Player", "Neural Net Link", "Encrypted Notes", "Quantum Web", "Bio Scanner", "Interface Designer", "Sonic Emitter", "AI Core Access", "System Config")
    val icons = List(cards.size) { painterResource(id = R.mipmap.ic_launcher_round) }
    val haptic = LocalHapticFeedback.current

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
        horizontalArrangement = Arrangement.spacedBy(22.dp),
        modifier = modifier.fillMaxSize()
    ) {
        itemsIndexed(cards, key = { _, title -> title }) { index, title ->
            var itemVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(index * 120L + 150L)
                itemVisible = true
            }

            AnimatedVisibility(
                visible = itemVisible,
                enter = fadeIn(animationSpec = tween(500, easing = LinearOutSlowInEasing)) +
                        slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(700, easing = FastOutSlowInEasing)
                        ),
                exit = fadeOut(animationSpec = tween(300)) +
                       slideOutVertically(targetOffsetY = { it / 2 }, animationSpec = tween(400))
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "card_pulse_$title")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.97f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1600, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "card_scale_$title"
                )
                val cardAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.9f,
                    targetValue = 0.75f,
                     animationSpec = infiniteRepeatable(
                        animation = tween(1600, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "card_alpha_$title"
                )

                Card(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCardClick(title)
                    },
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = cardAlpha)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp, pressedElevation = 4.dp),
                    modifier = Modifier
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .then(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Modifier.blur(6.dp) else Modifier)
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = icons[index % icons.size],
                            contentDescription = title,
                            modifier = Modifier.size(68.dp)
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center // Line 251
                        )
                    }
                }
            }
        }
    }
}
