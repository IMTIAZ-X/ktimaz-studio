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
import androidx.compose.material.icons.filled.Dashboard // Icon for Home/Dashboard
import androidx.compose.material.icons.filled.Person // Icon for Profile
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.ktimazstudio.ui.theme.ktimaz // Assuming this theme exists
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.FileReader

// Define Navigation Destinations
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Dashboard)
    object AppSettings : Screen("settings", "Settings", Icons.Filled.Settings) // Renamed to avoid conflict
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
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
                var selectedDestination by remember { mutableStateOf<Screen>(Screen.Dashboard) }

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

                val primaryGradient = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.90f),
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
                        MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp).copy(alpha = 0.6f) // Softer end
                    )
                )

                val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

                Row( // Root is now a Row for NavigationRail + Content
                    modifier = Modifier
                        .fillMaxSize()
                        .background(primaryGradient)
                ) {
                    AppNavigationRail(
                        selectedDestination = selectedDestination,
                        onDestinationSelected = { selectedDestination = it }
                    )

                    Scaffold(
                        modifier = Modifier
                            .weight(1f) // Content takes remaining space
                            .nestedScroll(scrollBehavior.nestedScrollConnection),
                        topBar = {
                            CenterAlignedTopAppBar( // Changed to CenterAlignedTopAppBar
                                title = {
                                    Text(
                                        text = stringResource(id = R.string.app_name),
                                        fontSize = 22.sp, // Adjusted size
                                        fontWeight = FontWeight.Medium, // Adjusted weight
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                },
                                // Actions can be moved to NavRail or kept if specific to a screen
                                // For simplicity, removing the top app bar settings icon as it's in NavRail now
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = Color.Transparent,
                                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp).copy(alpha = 0.95f)
                                ),
                                modifier = Modifier.statusBarsPadding(),
                                scrollBehavior = scrollBehavior
                            )
                        },
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        containerColor = Color.Transparent
                    ) { paddingValues ->
                        AnimatedContent( // Animate content changes between destinations
                            targetState = selectedDestination,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300, easing = LinearOutSlowInEasing)) +
                                slideInHorizontally(initialOffsetX = { if (initialState.route == Screen.Dashboard.route) 300 else -300 }, animationSpec = tween(300)) togetherWith
                                fadeOut(animationSpec = tween(300, easing = FastOutLinearInEasing)) +
                                slideOutHorizontally(targetOffsetX = { if (targetState.route == Screen.Dashboard.route) -300 else 300 }, animationSpec = tween(300))
                            }, label = "nav_rail_content_transition"
                        ) { targetDestination ->
                            Box(modifier = Modifier.padding(paddingValues)) {
                                when (targetDestination) {
                                    Screen.Dashboard -> AnimatedCardGrid { title ->
                                        // If a card named "System Config" launches settings activity
                                        if (title == "System Config") {
                                            context.startActivity(Intent(context, SettingsActivity::class.java))
                                        } else {
                                            context.startActivity(Intent(context, ComingActivity::class.java).putExtra("CARD_TITLE", title))
                                        }
                                    }
                                    Screen.AppSettings -> SettingsPlaceholderScreen() // Placeholder
                                    Screen.Profile -> ProfilePlaceholderScreen()       // Placeholder
                                }
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
fun AppNavigationRail(
    selectedDestination: Screen,
    onDestinationSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val destinations = listOf(Screen.Dashboard, Screen.AppSettings, Screen.Profile)
    NavigationRail(
        modifier = modifier.statusBarsPadding().fillMaxHeight(), // Fill height and respect status bar
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp).copy(alpha = 0.9f), // Semi-transparent rail
        header = {
            // Optional: Add a header like an app icon or logo
            // Icon(painterResource(R.mipmap.ic_launcher_round), contentDescription = "App Logo", modifier = Modifier.padding(vertical = 20.dp))
        }
    ) {
        Spacer(Modifier.weight(0.1f)) // Push items down a bit
        destinations.forEach { screen ->
            NavigationRailItem(
                selected = selectedDestination == screen,
                onClick = { onDestinationSelected(screen) },
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                alwaysShowLabel = false, // Show label only when selected or if space permits
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        Spacer(Modifier.weight(1f)) // Push items towards center/top
    }
}

@Composable
fun SettingsPlaceholderScreen(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Text("Settings Screen Content Goes Here", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        // In a real app, you'd embed your Settings composables here or navigate.
        // For example, you could call your SettingsScreenContent directly if its ViewModel is available.
    }
}

@Composable
fun ProfilePlaceholderScreen(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Text("User Profile Content Goes Here", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
    }
}


@Composable
fun AnimatedCardGrid(modifier: Modifier = Modifier, onCardClick: (String) -> Unit) {
    val cards = listOf("Spectrum Analyzer", "Image Synthesizer", "Holovid Player", "Neural Net Link", "Encrypted Notes", "Quantum Web", "Bio Scanner", "Interface Designer", "Sonic Emitter", "AI Core Access", "System Config")
    val icons = List(cards.size) { painterResource(id = R.mipmap.ic_launcher_round) }
    val haptic = LocalHapticFeedback.current

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp), // Adjusted top padding
        verticalArrangement = Arrangement.spacedBy(22.dp),
        horizontalArrangement = Arrangement.spacedBy(22.dp),
        modifier = modifier.fillMaxSize()
    ) {
        itemsIndexed(cards, key = { _, title -> title }) { index, title ->
            var itemVisible by remember { mutableStateOf(false) }
            LaunchedEffect(key1 = title) {
                delay(index * 80L + 150L) // Slightly faster stagger
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
                            initialScale = 0.7f
                        ),
                exit = fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.8f, animationSpec = tween(200))
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "card_effects_$title")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.99f, // Even more subtle scale for breathing
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2200, easing = EaseInOutCubic), // Slower, smoother
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "card_scale_$title"
                )
                 val animatedAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.80f, // Reduced alpha for outlined cards with blur
                    targetValue = 0.65f,
                     animationSpec = infiniteRepeatable(
                        animation = tween(2200, easing = EaseInOutCubic),
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
                    colors = CardDefaults.outlinedCardColors( // OUTLINED Card
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp).copy(alpha = animatedAlpha) // Subtle pulsing alpha
                    ),
                    border = CardDefaults.outlinedCardBorder(enabled = true), // Standard outlined border
                    elevation = CardDefaults.outlinedCardElevation(defaultElevation = 0.dp), // Outlined cards usually have 0dp elevation
                    modifier = Modifier
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .then(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Modifier.blur(3.dp) else Modifier) // Reduced blur
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
                            modifier = Modifier.size(64.dp) // Kept icon size
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold, // Changed from Bold
                            color = MaterialTheme.colorScheme.onSurface, // Use onSurface for outlined card content
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
