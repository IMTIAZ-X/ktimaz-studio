package com.ktimazstudio

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
// For Wi-Fi settings panel/page
import android.os.Build
import android.provider.Settings
// Removed WifiManager as we can't directly enable Wi-Fi

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings // Standard Material Icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope // For lifecycleScope
import com.ktimazstudio.ui.theme.ktimaz
import kotlinx.coroutines.delay // For delay in coroutine
import kotlinx.coroutines.launch // For launch in coroutine
import java.io.BufferedReader
import java.io.FileReader

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (detectVpn()) {
            Toast.makeText(this, "VPN detected. Closing app for security...", Toast.LENGTH_LONG).show()
            // Replace deprecated Handler with lifecycleScope coroutine
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
                val scope = rememberCoroutineScope() // For launching coroutines from Composables

                LaunchedEffect(Unit) {
                    if (!isConnected(context)) {
                        val result = snackbarHostState.showSnackbar(
                            message = "No Internet Connection!",
                            actionLabel = "Wi-Fi Settings",
                            duration = SnackbarDuration.Indefinite // Keep it until dismissed or action taken
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            openWifiSettings(context)
                        }
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar( // Replaced SmallTopAppBar
                            title = {
                                Text(
                                    text = stringResource(id = R.string.app_name),
                                    fontSize = 24.sp, // Slightly larger for a "futuristic" feel
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.1.sp // Added letter spacing
                                )
                            },
                            actions = {
                                IconButton(onClick = {
                                    context.startActivity(Intent(context, SettingsActivity::class.java))
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Settings, // Using Material Icon
                                        contentDescription = "Settings",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors( // Replaced smallTopAppBarColors
                                containerColor = Color.Transparent // Bar is transparent
                            ),
                            modifier = Modifier.statusBarsPadding() // Ensure content is not under status bar
                        )
                    },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    containerColor = MaterialTheme.colorScheme.background // Explicitly set background
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .padding(paddingValues) // Apply padding from Scaffold
                            .fillMaxSize()
                    ) {
                        AnimatedCardGrid { title ->
                            // For Settings card, it's handled by TopAppBar action.
                            // If you also want the card to work:
                            if (title == "Settings") {
                                context.startActivity(Intent(context, SettingsActivity::class.java))
                            } else {
                                context.startActivity(
                                    Intent(context, ComingActivity::class.java).putExtra("CARD_TITLE", title)
                                )
                            }
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
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) // Check for actual internet
    }

    // Updated to prompt user to change Wi-Fi settings
    private fun openWifiSettings(context: Context) {
        Toast.makeText(context, "Please enable Wi-Fi or connect to a network.", Toast.LENGTH_LONG).show()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android Q and above, use the Settings Panel for a better in-app experience
            val panelIntent = Intent(Settings.Panel.ACTION_WIFI)
            context.startActivity(panelIntent)
        } else {
            // For older versions, open the general Wi-Fi settings page
            @Suppress("DEPRECATION") // Needed for startActivity for older OS
            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
    }

    private fun detectVpn(): Boolean = try {
        // This method has limitations and might not work reliably on all Android versions/devices
        // due to file system access restrictions.
        BufferedReader(FileReader("/proc/net/tcp")).useLines { lines ->
            lines.any { line ->
                // Check for common VPN-related local addresses/ports, this is a heuristic
                line.contains("0100007F:") || line.contains("00000000:10E1") // 127.0.0.1 or common VPN service ports (e.g., 4321)
            }
        }
    } catch (_: Exception) {
        false // If file access fails, assume no VPN or unable to detect
    }
}

@Composable
fun AnimatedCardGrid(onCardClick: (String) -> Unit) {
    val cards = listOf("Spectrum Analyzer", "Image Synthesizer", "Holovid Player", "Neural Net Link", "Encrypted Notes", "Quantum Web", "Bio Scanner", "Interface Designer", "Sonic Emitter", "AI Core Access", "System Config") // More "futuristic" names
    // Assuming R.mipmap.ic_launcher is a generic placeholder.
    // For a real app, you'd have unique icons for each card.
    val icons = List(cards.size) { painterResource(id = R.mipmap.ic_launcher_round) } // Example: using round launcher icon

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp), // Adaptive columns for responsiveness
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp), // Increased padding
        verticalArrangement = Arrangement.spacedBy(20.dp), // Increased spacing
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(cards, key = { _, title -> title }) { index, title ->
            // Entrance Animation for each card
            var itemVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(index * 100L + 100L) // Staggered delay, plus initial delay
                itemVisible = true
            }

            AnimatedVisibility(
                visible = itemVisible,
                enter = fadeIn(animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing)) +
                        slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = tween(durationMillis = 500, easing = AnticipateOvershootInterpolator())
                        ),
                exit = fadeOut(animationSpec = tween(durationMillis = 300)) +
                       slideOutVertically(targetOffsetY = { it / 3 }, animationSpec = tween(durationMillis = 300))
            ) {
                // Breathing Animation (applied to the Card content or the Card itself)
                val infiniteTransition = rememberInfiniteTransition(label = "card_breathing_transition_$title")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.98f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing), // Slower, more subtle breath
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "card_scale_animation_$title"
                )

                Card(
                    onClick = { onCardClick(title) },
                    shape = RoundedCornerShape(20.dp), // More rounded for modern feel
                    colors = CardDefaults.cardColors(
                        // Slightly less transparent for better readability, or use surfaceVariant
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp, pressedElevation = 10.dp),
                    modifier = Modifier
                        .graphicsLayer(scaleX = scale, scaleY = scale) // Apply breathing scale here
                        .fillMaxWidth()
                        .height(170.dp) // Slightly taller cards
                ) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp), // Increased padding inside card
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = icons[index % icons.size], // Use modulo if icons list is shorter
                            contentDescription = title,
                            modifier = Modifier.size(64.dp) // Larger icon
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall, // Use M3 typography
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
