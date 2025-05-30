package com.ktimazstudio

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.compose.foundation.BorderStroke // Ensure this is imported
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HistoryEdu // For Changelog
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
// Direct imports for outlined icons used in LoginScreen
import androidx.compose.material.icons.outlined.AccountCircle // Correct direct import
import androidx.compose.material.icons.outlined.Lock // Correct direct import
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.platform.LocalFocusManager
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

// --- SharedPreferencesManager ---
class SharedPreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("AppPrefsKtimazStudio", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in_key"
        private const val KEY_USERNAME = "username_key"
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun setLoggedIn(loggedIn: Boolean, username: String? = null) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, loggedIn)
            if (loggedIn && username != null) {
                putString(KEY_USERNAME, username)
            } else if (!loggedIn) {
                remove(KEY_USERNAME)
            }
            apply()
        }
    }
    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }
}

// --- Top-level utility functions ---
fun isConnected(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
           capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

fun openWifiSettings(context: Context) {
    Toast.makeText(context, "Please enable Wi-Fi or connect to a network.", Toast.LENGTH_LONG).show()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        context.startActivity(Intent(Settings.Panel.ACTION_WIFI))
    } else {
        @Suppress("DEPRECATION")
        context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
    }
}

@Suppress("DEPRECATION")
fun detectVpn(context: Context): Boolean {
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

// --- Navigation Destinations ---
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Dashboard)
    object AppSettings : Screen("settings", "Settings", Icons.Filled.Settings)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
class MainActivity : ComponentActivity() {
    private lateinit var sharedPrefsManager: SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPrefsManager = SharedPreferencesManager(applicationContext)

        if (detectVpn(this)) {
            setContent {
                ktimaz {
                    VpnDetectedDialog { finishAffinity() }
                }
            }
            return
        }

        setContent {
            ktimaz {
                var isLoggedIn by remember { mutableStateOf(sharedPrefsManager.isLoggedIn()) }
                // Use a derived state for username to ensure it recomposes when sharedPrefs changes after login
                var currentUsername by remember(isLoggedIn) { mutableStateOf(sharedPrefsManager.getUsername()) }


                AnimatedContent(
                    targetState = isLoggedIn,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(400, delayMillis = 200)) +
                                scaleIn(initialScale = 0.92f, animationSpec = tween(400, delayMillis = 200)))
                            .togetherWith(
                                fadeOut(animationSpec = tween(200)) +
                                        scaleOut(targetScale = 0.92f, animationSpec = tween(200))
                            )
                            .using(SizeTransform(clip = false))
                    },
                    label = "LoginScreenTransition"
                ) { targetIsLoggedIn ->
                    if (targetIsLoggedIn) {
                        MainApplicationUI(
                            username = currentUsername ?: "User",
                            onLogout = {
                                sharedPrefsManager.setLoggedIn(false)
                                isLoggedIn = false
                                // currentUsername will update due to remember(isLoggedIn)
                            }
                        )
                    } else {
                        LoginScreen(
                            onLoginSuccess = { loggedInUsername ->
                                sharedPrefsManager.setLoggedIn(true, loggedInUsername)
                                isLoggedIn = true
                                // currentUsername will update due to remember(isLoggedIn)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VpnDetectedDialog(onExitApp: () -> Unit) {
    MaterialTheme {
        AlertDialog(
            onDismissRequest = { /* VPN Dialog is not dismissible by user action */ },
            icon = { Icon(Icons.Filled.Lock, contentDescription = "VPN Detected Icon", tint = MaterialTheme.colorScheme.error) },
            title = { Text("VPN Detected", color = MaterialTheme.colorScheme.onSurface) }, // Use onSurface for title
            text = { Text("For security reasons, this application cannot be used while a VPN connection is active. Please disable your VPN and try again.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = onExitApp,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Exit Application", color = MaterialTheme.colorScheme.onError)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh // Use a surface color for dialog
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApplicationUI(username: String, onLogout: () -> Unit) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedDestination by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    var isRailExpanded by remember { mutableStateOf(false) }

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
                            text = stringResource(id = R.string.app_name),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = scrolledAppBarColor,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .statusBarsPadding()
                        .graphicsLayer {
                            shadowElevation = if (isScrolled) 4.dp.toPx() else 0f
                            shape = topAppBarRoundedShape
                            clip = true
                        }
                        .background(
                            color = if (isScrolled) scrolledAppBarColor else Color.Transparent
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
                        Screen.Profile -> ProfileScreen(username = username, onLogout = onLogout)
                    }
                }
            }
        }
    }
}


@Composable
fun LoginScreen(onLoginSuccess: (username: String) -> Unit) {
    var usernameInput by rememberSaveable { mutableStateOf("") }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant, // Softer unfocused border
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceContainerLowest,
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .widthIn(max = 420.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 28.dp, vertical = 36.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.mipmap.ic_launcher_round),
                    contentDescription = stringResource(id = R.string.app_name) + " Logo",
                    modifier = Modifier.size(72.dp).clip(CircleShape), // Clip logo if it's not already round
                    tint = Color.Unspecified
                )
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Sign in to access your space",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = { usernameInput = it.trim(); errorMessage = null },
                    label = { Text("Username") },
                    // Using direct icon reference
                    leadingIcon = { Icon(androidx.compose.material.icons.outlined.AccountCircle, contentDescription = "Username") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(16.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it; errorMessage = null },
                    label = { Text("Password") },
                    // Using direct icon reference
                    leadingIcon = { Icon(androidx.compose.material.icons.outlined.Lock, contentDescription = "Password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        if (usernameInput == "admin" && passwordInput == "admin") {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLoginSuccess(usernameInput)
                        } else {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            errorMessage = "Invalid username or password."
                        }
                    }),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, if (passwordVisible) "Hide password" else "Show password")
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(animationSpec = tween(200)) + slideInVertically(initialOffsetY = { -it / 2 }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                    exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(targetOffsetY = { -it / 2 }, animationSpec = spring(stiffness = Spring.StiffnessMedium))
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (usernameInput == "admin" && passwordInput == "admin") {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLoginSuccess(usernameInput)
                        } else {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            errorMessage = "Invalid username or password."
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 6.dp)
                ) {
                    Text("LOGIN", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

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
        targetValue = if (isExpanded) 180.dp else 80.dp,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "nav_rail_width_anim"
    )
    val railContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp).copy(alpha = 0.95f)

    NavigationRail(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxHeight()
            .width(railWidth)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        containerColor = railContainerColor,
        header = {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                AnimatedContent(
                    targetState = isExpanded,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(200, delayMillis = 150)) + scaleIn(initialScale = 0.8f, animationSpec = tween(200, delayMillis = 150)) togetherWith
                        fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.8f, animationSpec = tween(150))
                    }, label = "menu_icon_transition"
                ) { expanded ->
                    Icon(
                        imageVector = if (expanded) Icons.AutoMirrored.Filled.MenuOpen else Icons.Filled.Menu,
                        contentDescription = if (expanded) "Collapse Menu" else "Expand Menu",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) {
        Spacer(Modifier.weight(0.05f))
        destinations.forEach { screen ->
            val isSelected = selectedDestination == screen
            val iconScale by animateFloatAsState(
                targetValue = if (isSelected) 1.1f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                label = "nav_item_icon_scale_anim"
            )
            val indicatorColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else Color.Transparent
            val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

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
                        enter = fadeIn(animationSpec = tween(200, delayMillis = 150)) + expandHorizontally(animationSpec = tween(300, delayMillis = 100), expandFrom = Alignment.Start),
                        exit = fadeOut(animationSpec = tween(150)) + shrinkHorizontally(animationSpec = tween(250), shrinkTowards = Alignment.Start)
                    ) { Text(screen.label, maxLines = 1, style = MaterialTheme.typography.labelMedium) }
                },
                alwaysShowLabel = isExpanded,
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = contentColor,
                    selectedTextColor = contentColor,
                    indicatorColor = indicatorColor,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(vertical = 6.dp).height(56.dp)
            )
            if (destinations.last() != screen) {
                Spacer(Modifier.height(6.dp))
            }
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
fun ProfileScreen(modifier: Modifier = Modifier, username: String, onLogout: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = username.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Welcome to your profile!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onLogout,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            modifier = Modifier.fillMaxWidth(0.7f).height(50.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
            Spacer(Modifier.width(12.dp))
            Text("Logout", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showChangelogDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Application Settings",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
        )

        var notificationsEnabled by remember { mutableStateOf(true) }
        SettingItem(
            title = "Enable Notifications",
            description = "Receive updates and alerts.",
            leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)},
            control = {
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

        var showAccountDialog by remember { mutableStateOf(false) }
        SettingItem(
            title = "Account Preferences",
            description = "Manage your account details.",
            leadingIcon = { Icon(Icons.Filled.AccountBox, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)},
            control = { Icon(Icons.Filled.ChevronRight, contentDescription = "Go to account preferences", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            onClick = { showAccountDialog = true }
        )
        if (showAccountDialog) {
            AlertDialog(
                onDismissRequest = { showAccountDialog = false },
                icon = { Icon(Icons.Filled.AccountBox, contentDescription = null)},
                title = { Text("Account Preferences") },
                text = { Text("Account settings details would appear here or navigate to a dedicated screen. This is a placeholder.") },
                confirmButton = { TextButton(onClick = { showAccountDialog = false }) { Text("OK") } }
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

        SettingItem(
            title = "About",
            description = "Information about this application.",
            leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)},
            control = { Icon(Icons.Filled.ChevronRight, contentDescription = "View About", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            onClick = { showAboutDialog = true }
        )
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                icon = { Icon(Icons.Filled.Info, contentDescription = "About App Icon")},
                title = { Text("About " + stringResource(id = R.string.app_name)) },
                text = { Text("Version: ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})\n\nDeveloped by Ktimaz Studio.\n\nThis application is a demonstration of various Android and Jetpack Compose features. Thank you for using our app!") },
                confirmButton = { TextButton(onClick = { showAboutDialog = false }) { Text("Close") } }
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

        SettingItem(
            title = "Privacy Policy",
            description = "Read our privacy policy.",
            leadingIcon = { Icon(Icons.Filled.Policy, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)},
            control = { Icon(Icons.Filled.ChevronRight, contentDescription = "View Privacy Policy", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            onClick = { showPrivacyDialog = true }
        )
        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                icon = { Icon(Icons.Filled.Policy, contentDescription = "Privacy Policy Icon")},
                title = { Text("Privacy Policy") },
                text = { Text("Placeholder for Privacy Policy text. In a real application, this would contain the full policy details or link to a web page.\n\nWe are committed to protecting your privacy. Our policy outlines how we collect, use, and safeguard your information.") },
                confirmButton = { TextButton(onClick = { showPrivacyDialog = false }) { Text("Close") } }
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

        // Changelog Item
        SettingItem(
            title = "Changelog",
            description = "See what's new in this version.",
            leadingIcon = { Icon(Icons.Filled.HistoryEdu, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)},
            control = { Icon(Icons.Filled.ChevronRight, contentDescription = "View Changelog", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            onClick = { showChangelogDialog = true }
        )
        if (showChangelogDialog) {
            AlertDialog(
                onDismissRequest = { showChangelogDialog = false },
                icon = { Icon(Icons.Filled.HistoryEdu, contentDescription = "Changelog Icon", modifier = Modifier.size(28.dp))},
                title = { Text("What's New - v${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.titleLarge) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text("Version ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                        Text("✨ New Features:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                        Text("  • Added persistent login with auto-login.", style = MaterialTheme.typography.bodyMedium)
                        Text("  • Implemented Logout functionality.", style = MaterialTheme.typography.bodyMedium)
                        Text("  • Enhanced VPN detection with a Material 3 dialog.", style = MaterialTheme.typography.bodyMedium)
                        Text("  • Added 'About', 'Privacy Policy', and 'Changelog' to Settings.", style = MaterialTheme.typography.bodyMedium)
                        Text("🐛 Bug Fixes & Improvements:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
                        Text("  • Addressed various icon resolution and deprecation warnings.", style = MaterialTheme.typography.bodyMedium)
                        Text("  • Polished Login screen UX and Navigation Rail visuals.", style = MaterialTheme.typography.bodyMedium)
                        Text("  • Profile screen now shows username and placeholder picture.", style = MaterialTheme.typography.bodyMedium)
                        Text("  • General UI/UX tweaks for a more expressive Material 3 feel.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Thank you for updating!", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = { TextButton(onClick = { showChangelogDialog = false }) { Text("Awesome!") } },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest // Slightly different container for dialog
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))


        SettingItem(
            title = "App Version",
            description = "${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
            leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)},
            control = {}
        )
         HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
    }
}

@Composable
fun SettingItem(
    title: String,
    description: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    control: @Composable (() -> Unit)? = null
) {
    val itemModifier = Modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(vertical = 16.dp, horizontal = 8.dp)

    Row(
        modifier = itemModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Box(modifier = Modifier.padding(end = 16.dp).size(24.dp), contentAlignment = Alignment.Center) {
                leadingIcon()
            }
        }
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            if (description != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    val icons = List(cards.size) { painterResource(id = R.mipmap.ic_launcher_round) }
    val haptic = LocalHapticFeedback.current

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier.fillMaxSize()
    ) {
        itemsIndexed(cards, key = { _, title -> title }) { index, title ->
            var itemVisible by remember { mutableStateOf(false) }
            LaunchedEffect(key1 = title) {
                delay(index * 70L + 100L)
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

                Card(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCardClick(title)
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.outlinedCardColors( // Changed to outlined for variety, can be CardDefaults.cardColors if preferred
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp).copy(alpha = animatedAlpha)
                    ),
                    // Corrected: Use BorderStroke directly for the border parameter of the Card
                    border = BorderStroke(width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Subtle elevation for outlined cards
                    modifier = Modifier
                        .graphicsLayer(scaleX = scale, scaleY = scale)
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
                            painter = icons[index % icons.size],
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
