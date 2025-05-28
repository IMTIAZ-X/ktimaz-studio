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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Lock // Added for Login
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility // Added for Login
import androidx.compose.material.icons.filled.VisibilityOff // Added for Login
import androidx.compose.material.icons.outlined.AccountCircle // Added for Login
import androidx.compose.material.icons.outlined.Lock // Added for Login (alternative)
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable // Added for login state
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager // Added for Login
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.ktimazstudio.ui.theme.ktimaz // Assuming this theme exists
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Define Navigation Destinations
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Dashboard)
    object AppSettings : Screen("settings", "Settings", Icons.Filled.Settings)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // VPN Check is good to have before any UI
        if (detectVpn(this)) {
            Toast.makeText(this, "VPN connection detected. Exiting application.", Toast.LENGTH_LONG).show()
            lifecycleScope.launch {
                delay(4000)
                finishAffinity()
            }
            return
        }

        setContent {
            ktimaz {
                var isLoggedIn by rememberSaveable { mutableStateOf(false) } // Login State

                AnimatedContent(
                    targetState = isLoggedIn,
                    transitionSpec = {
                        // Transition for appearing: Fade in and scale up slightly
                        // Transition for disappearing: Fade out and scale down slightly
                        (fadeIn(animationSpec = tween(400, delayMillis = 200)) +
                                scaleIn(initialScale = 0.92f, animationSpec = tween(400, delayMillis = 200)))
                            .togetherWith(
                                fadeOut(animationSpec = tween(200)) +
                                        scaleOut(targetScale = 0.92f, animationSpec = tween(200))
                            )
                            .using(
                                SizeTransform(clip = false) // Allow content to overlap during transition
                            )
                    },
                    label = "LoginScreenTransition"
                ) { targetIsLoggedIn ->
                    if (targetIsLoggedIn) {
                        MainApplicationUI()
                    } else {
                        LoginScreen(
                            onLoginSuccess = { isLoggedIn = true }
                        )
                    }
                }
            }
        }
    }
    // detectVpn, isConnected, openWifiSettings remain the same
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

    private fun detectVpn(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.allNetworks.forEach { network ->
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    return true
                }
            }
        }
        return false
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MainApplicationUI() {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedDestination by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    var isRailExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val mainActivity = context as? MainActivity ?: return@LaunchedEffect
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
            MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp).copy(alpha = 0.6f)
        )
    )

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val topAppBarRoundedShape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
    val scrolledAppBarColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp).copy(alpha = 0.95f)

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryGradient)
    ) {
        AppNavigationRail(
            selectedDestination = selectedDestination,
            onDestinationSelected = { selectedDestination = it },
            isExpanded = isRailExpanded,
            onMenuClick = { isRailExpanded = !isRailExpanded }
        )

        Scaffold(
            modifier = Modifier
                .weight(1f)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                val isScrolled = scrollBehavior.state.contentOffset > 0.1f

                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.app_name), // Ensure R.string.app_name exists
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = scrolledAppBarColor
                    ),
                    modifier = Modifier
                        .statusBarsPadding()
                        .graphicsLayer {
                            shadowElevation = if (isScrolled) 4.dp.toPx() else 0f
                            shape = topAppBarRoundedShape
                            clip = true
                        }
                        .background(
                            color = if (isScrolled) {
                                scrolledAppBarColor
                            } else {
                                Color.Transparent
                            }
                        ),
                    scrollBehavior = scrollBehavior
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = Color.Transparent
        ) { paddingValues ->
            AnimatedContent(
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
                            if (title == "System Config") {
                                context.startActivity(Intent(context, SettingsActivity::class.java))
                            } else {
                                context.startActivity(Intent(context, ComingActivity::class.java).putExtra("CARD_TITLE", title))
                            }
                        }
                        Screen.AppSettings -> SettingsScreen()
                        Screen.Profile -> ProfilePlaceholderScreen()
                    }
                }
            }
        }
    }
}


@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current // For Toasts

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface), // Use surface color for background
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth(0.85f) // Responsive width
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(all = 24.dp)
                    .verticalScroll(rememberScrollState()), // Scrollable if content overflows on small screens
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.app_name), // Make sure R.string.app_name is defined
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Welcome Back!",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it; errorMessage = null },
                    label = { Text("Username") },
                    leadingIcon = { Icon(Icons.Outlined.AccountCircle, contentDescription = "Username Icon") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "Password Icon") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        if (username == "admin" && password == "admin") {
                            onLoginSuccess()
                        } else {
                            errorMessage = "Invalid username or password."
                        }
                    }),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) "Hide password" else "Show password"
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, description)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (username == "admin" && password == "admin") {
                            onLoginSuccess()
                        } else {
                            errorMessage = "Invalid username or password."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp) // Add some top padding
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Login", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}


// AppNavigationRail, SettingsScreen, SettingItem, ProfilePlaceholderScreen, AnimatedCardGrid, SettingsActivity, ComingActivity remain the same

@Composable
fun AppNavigationRail(
    selectedDestination: Screen,
    onDestinationSelected: (Screen) -> Unit,
    isExpanded: Boolean,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val destinations = listOf(Screen.Dashboard, Screen.AppSettings, Screen.Profile)
    val railWidth by animateDpAsState(
        targetValue = if (isExpanded) 200.dp else 80.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "nav_rail_width_anim"
    )

    NavigationRail(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxHeight()
            .width(railWidth)
            .padding(vertical = 8.dp),
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp).copy(alpha = 0.9f),
        header = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.MenuOpen else Icons.Filled.Menu,
                    contentDescription = if (isExpanded) "Collapse Menu" else "Expand Menu",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(20.dp))
        }
    ) {
        Spacer(Modifier.weight(0.1f))
        destinations.forEach { screen ->
            val isSelected = selectedDestination == screen
            val iconScale by animateFloatAsState(
                targetValue = if (isSelected) 1.15f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "nav_item_icon_scale_anim"
            )

            NavigationRailItem(
                selected = isSelected,
                onClick = { onDestinationSelected(screen) },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.label,
                        modifier = Modifier.graphicsLayer(scaleX = iconScale, scaleY = iconScale)
                    )
                },
                label = {
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn(animationSpec = tween(150, delayMillis = 100)) + expandHorizontally(animationSpec = tween(250, delayMillis = 50)),
                        exit = fadeOut(animationSpec = tween(100)) + shrinkHorizontally(animationSpec = tween(200))
                    ) {
                        Text(screen.label, maxLines = 1)
                    }
                },
                alwaysShowLabel = isExpanded,
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
fun ProfilePlaceholderScreen(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Text("User Profile Content Goes Here", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Application Settings",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        var notificationsEnabled by remember { mutableStateOf(true) }
        SettingItem(
            title = "Enable Notifications",
            description = "Receive updates and alerts.",
            control = {
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }
        )

        Divider(modifier = Modifier.padding(vertical = 12.dp))

        var showAccountDialog by remember { mutableStateOf(false) }
        SettingItem(
            title = "Account Preferences",
            description = "Manage your account details.",
            control = {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = "Go to account preferences"
                )
            },
            onClick = {
                showAccountDialog = true
            }
        )
        if (showAccountDialog) {
            AlertDialog(
                onDismissRequest = { showAccountDialog = false },
                title = { Text("Account Preferences") },
                text = { Text("Account settings would be shown here or navigate to a new screen.") },
                confirmButton = {
                    TextButton(onClick = { showAccountDialog = false }) { Text("OK") }
                }
            )
        }

        Divider(modifier = Modifier.padding(vertical = 12.dp))

        SettingItem(
            title = "App Version",
            description = "1.0.0 (Build ${BuildConfig.VERSION_CODE})", // Ensure BuildConfig is accessible
            control = {}
        )
         Divider(modifier = Modifier.padding(vertical = 12.dp))

        Text(
            "More settings options can be added below.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 24.dp).align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun SettingItem(
    title: String,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    control: @Composable (() -> Unit)? = null
) {
    val itemModifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 8.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 8.dp)
    }

    Row(
        modifier = itemModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            if (description != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (control != null) {
            Box(modifier = Modifier.padding(start = 8.dp)) {
                control()
            }
        }
    }
}


@Composable
fun AnimatedCardGrid(modifier: Modifier = Modifier, onCardClick: (String) -> Unit) {
    val cards = listOf("Spectrum Analyzer", "Image Synthesizer", "Holovid Player", "Neural Net Link", "Encrypted Notes", "Quantum Web", "Bio Scanner", "Interface Designer", "Sonic Emitter", "AI Core Access", "System Config")
    val icons = List(cards.size) { painterResource(id = R.mipmap.ic_launcher_round) } // Ensure this resource exists
    val haptic = LocalHapticFeedback.current

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
        horizontalArrangement = Arrangement.spacedBy(22.dp),
        modifier = modifier.fillMaxSize()
    ) {
        itemsIndexed(cards, key = { _, title -> title }) { index, title ->
            var itemVisible by remember { mutableStateOf(false) }
            LaunchedEffect(key1 = title) {
                delay(index * 80L + 150L)
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
                    initialValue = 0.99f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2200, easing = EaseInOutCubic),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "card_scale_$title"
                )
                 val animatedAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.80f,
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
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp).copy(alpha = animatedAlpha)
                    ),
                    border = CardDefaults.outlinedCardBorder(enabled = true),
                    elevation = CardDefaults.outlinedCardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .then(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Modifier.blur(3.dp) else Modifier)
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
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

