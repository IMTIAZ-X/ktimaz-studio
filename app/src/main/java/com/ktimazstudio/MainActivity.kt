package com.ktimazstudio

import android.Manifest // Added: For permissions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Debug
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import java.io.BufferedReader // Added: For BufferedReader
import java.io.File // Added: For File operations
import java.io.InputStreamReader // Added: For InputStreamReader
import java.security.MessageDigest // Added: For MessageDigest
import kotlin.experimental.and // Added: For bitwise 'and' operation
import androidx.activity.enableEdgeToEdge
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.automirrored.filled.Language
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.rememberTooltipState
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
import androidx.compose.ui.platform.LocalInspectionMode
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
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ktimazstudio.ui.theme.ktimaz
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.app.UiModeManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.surfaceColorAtElevation

// --- Theme Settings Enum ---
enum class ThemeSetting {
    LIGHT, DARK, SYSTEM, BATTERY_SAVER
}

// --- SoundEffectManager ---
/**
 * Manages playing short sound effects using SoundPool for low-latency audio feedback.
 * This is used for click sounds and other UI interactions.
 *
 * IMPORTANT: For this to work, you must place an audio file (e.g., click_sound.wav)
 * in your `res/raw` directory.
 */
class SoundEffectManager(private val context: Context, private val sharedPrefsManager: SharedPreferencesManager) {
    private var soundPool: SoundPool? = null
    private var clickSoundId: Int = 0

    /**
     * Loads the sound effects into the SoundPool.
     * This should be called once, typically during app startup.
     */
    fun loadSounds() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        // Load your click sound from res/raw.
        // Make sure you have a file named 'click_sound.wav' (or .mp3) in res/raw.
        // You will need to create the 'raw' directory inside 'app/src/main/res/'
        // and place your sound file there.
        // NOTE: The `R.raw.click_sound` resource ID is a placeholder. You need to add this resource.
        // The user's code snippet has R.raw.click_sound which is assumed to exist.
        clickSoundId = soundPool?.load(context, R.raw.click_sound, 1) ?: 0
    }

    /**
     * Plays the loaded click sound, only if sound effects are enabled in settings.
     */
    fun playClickSound() {
        if (sharedPrefsManager.isSoundEnabled() && clickSoundId != 0) {
            soundPool?.play(clickSoundId, 1.0f, 1.0f, 0, 0, 1.0f)
        }
    }

    /**
     * Releases the SoundPool resources.
     * This should be called when the activity/application is destroyed to prevent memory leaks.
     */
    fun release() {
        soundPool?.release()
        soundPool = null
    }
}


// --- SharedPreferencesManager ---
/**
 * Manages user login status, username, theme settings, sound settings,
 * initial setup completion, and language setting using SharedPreferences for persistent storage.
 */
class SharedPreferencesManager(context: Context) {
    val prefs: SharedPreferences = context.getSharedPreferences("AppPrefsKtimazStudio", Context.MODE_PRIVATE)
    private val context: Context = context

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in_key"
        private const val KEY_USERNAME = "username_key"
        const val KEY_THEME_SETTING = "theme_setting_key"
        const val KEY_SOUND_ENABLED = "sound_enabled_key"
        private const val KEY_INITIAL_SETUP_COMPLETE = "initial_setup_complete"
        private const val KEY_LANGUAGE_SETTING = "language_setting_key"
    }

    /**
     * Checks if a user is currently logged in.
     * return true if a user is logged in, false otherwise.
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Sets the login status of the user. If logging in, the username is also stored.
     * If logging out, the username is removed.
     * @param loggedIn The new login status.
     * @param username The username to store if logging in. Null if logging out.
     */
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

    /**
     * Retrieves the username of the currently logged-in user.
     * return The username string, or null if no user is logged in.
     */
    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    /**
     * Retrieves the current theme setting.
     * return The ThemeSetting enum value. Defaults to SYSTEM.
     */
    fun getThemeSetting(): ThemeSetting {
        val themeString = prefs.getString(KEY_THEME_SETTING, ThemeSetting.SYSTEM.name)
        return try {
            ThemeSetting.valueOf(themeString ?: ThemeSetting.SYSTEM.name)
        } catch (e: IllegalArgumentException) {
            ThemeSetting.SYSTEM
        }
    }

    /**
     * Sets the new theme setting.
     * @param themeSetting The ThemeSetting enum value to store.
     */
    fun setThemeSetting(themeSetting: ThemeSetting) {
        prefs.edit().putString(KEY_THEME_SETTING, themeSetting.name).apply()
    }

    /**
     * Checks if sound effects are enabled.
     * return true if sound is enabled, false otherwise. Defaults to true.
     */
    fun isSoundEnabled(): Boolean {
        return prefs.getBoolean(KEY_SOUND_ENABLED, true)
    }

    /**
     * Sets the sound effects enabled status.
     * @param enabled The new sound status.
     */
    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    /**
     * Checks if the initial setup dialog has been completed.
     */
    fun isInitialSetupComplete(): Boolean {
        return prefs.getBoolean(KEY_INITIAL_SETUP_COMPLETE, false)
    }

    /**
     * Sets the initial setup completion status.
     */
    fun setInitialSetupComplete(complete: Boolean) {
        prefs.edit().putBoolean(KEY_INITIAL_SETUP_COMPLETE, complete).apply()
    }

    /**
     * Retrieves the current language setting.
     * @return The language code (e.g., "en"), or a default value if not set.
     */
    fun getLanguageSetting(): String {
        return prefs.getString(KEY_LANGUAGE_SETTING, "en") ?: "en"
    }

    /**
     * Sets the new language setting.
     * @param languageCode The language code to store (e.g., "en", "es", "fr").
     */
    fun setLanguageSetting(languageCode: String) {
        prefs.edit().putString(KEY_LANGUAGE_SETTING, languageCode).apply()
    }
}

// --- ConnectivityManagerHelper ---
/**
 * A helper class to monitor network connectivity status.
 * This is useful for providing user feedback when the network state changes.
 */
class ConnectivityManagerHelper(private val context: Context, private val onNetworkStatusChanged: (Boolean) -> Unit) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            onNetworkStatusChanged(true)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            onNetworkStatusChanged(false)
        }
    }

    /**
     * Starts monitoring network connectivity changes.
     */
    fun startMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    /**
     * Stops monitoring network connectivity changes.
     */
    fun stopMonitoring() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}

// --- SecurityManager ---
/**
 * Utility class for performing various security checks on the application's environment.
 * These checks are designed to detect common reverse engineering, tampering, and
 * undesirable network conditions like VPN usage.
 *
 * NOTE: Client-side security checks are never foolproof and can be bypassed by
 * determined attackers. They serve as deterrents and indicators of compromise.
 */
class SecurityManager(private val context: Context) {
    // Known good hash of the APK (replace with your actual app's release APK hash)
    // You would typically calculate this hash for your *release* APK and hardcode it here.
    // For demonstration, this is a placeholder.
    // The hash from your code snippet: f21317d4d6276ff3174a363c7fdff4171c73b1b80a82bb9082943ea9200a8425
    private val EXPECTED_APK_HASH = "f21317d4d6276ff3174a363c7fdff4171c73b1b80a82bb9082943ea9200a8425".lowercase()

    /**
     * Checks if a debugger is currently attached to the application process.
     * @return true if a debugger is connected, false otherwise.
     */
    fun isDebuggerConnected(): Boolean {
        return Debug.isDebuggerConnected() || isTracerAttached()
    }

    /**
     * Checks if a VPN connection is active.
     * This method iterates through all active networks and checks for the VPN transport.
     * @return true if a VPN is detected and it has internet capabilities, false otherwise.
     */
    @Suppress("DEPRECATION")
    fun isVpnActive(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.allNetworks.forEach { network ->
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                // Ensure the VPN is actually providing internet
                if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Registers a NetworkCallback to listen for real-time VPN status changes.
     * @param onVpnStatusChanged Callback to be invoked when VPN status changes.
     * @return The registered NetworkCallback instance, which should be unregistered later.
     */
    fun registerVpnDetectionCallback(onVpnStatusChanged: (Boolean) -> Unit): ConnectivityManager.NetworkCallback {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // Build a NetworkRequest specifically for VPN transport
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_VPN) // Explicitly look for VPN
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                // When a network becomes available, re-check overall VPN status
                onVpnStatusChanged(isVpnActive())
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                // When a network is lost, re-check overall VPN status
                onVpnStatusChanged(isVpnActive())
            }
        }
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        return networkCallback
    }

    /**
     * Helper function to check if the app is being run by a tracer (e.g., for reverse engineering).
     * This checks for the `TracerPid` field in the `/proc/self/status` file.
     * @return true if a tracer is detected, false otherwise.
     */
    private fun isTracerAttached(): Boolean {
        return try {
            BufferedReader(InputStreamReader(File("/proc/self/status").inputStream())).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (line?.startsWith("TracerPid") == true) {
                        return line?.split(":")?.get(1)?.trim()?.toIntOrNull() != 0
                    }
                }
            }
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

// --- BaseActivity ---
/**
 * Base activity that provides common functionality like SharedPreferences,
 * sound effects, and network connectivity monitoring.
 */
abstract class BaseActivity : ComponentActivity() {
    lateinit var sharedPrefsManager: SharedPreferencesManager
    lateinit var soundEffectManager: SoundEffectManager
    lateinit var connectivityHelper: ConnectivityManagerHelper
    lateinit var securityManager: SecurityManager // Add SecurityManager

    private var vpnNetworkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize managers
        sharedPrefsManager = SharedPreferencesManager(this)
        soundEffectManager = SoundEffectManager(this, sharedPrefsManager)
        soundEffectManager.loadSounds()
        securityManager = SecurityManager(this)

        // Initialize and start network connectivity monitoring
        connectivityHelper = ConnectivityManagerHelper(this) { isConnected ->
            if (!isConnected) {
                // Show a toast message when the network is lost
                Toast.makeText(this, "Network connection lost. Please check your internet.", Toast.LENGTH_LONG).show()
            }
        }

        // Start VPN detection
        vpnNetworkCallback = securityManager.registerVpnDetectionCallback { isVpnActive ->
            if (isVpnActive) {
                Toast.makeText(this, "VPN detected. Security checks in progress.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        connectivityHelper.startMonitoring()
    }

    override fun onPause() {
        super.onPause()
        connectivityHelper.stopMonitoring()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundEffectManager.release()
        // Unregister the VPN network callback
        vpnNetworkCallback?.let {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.unregisterNetworkCallback(it)
        }
    }
}


// --- MainActivity ---
/**
 * The main entry point of the application.
 *
 * This activity handles:
 * - Initial setup and permission requests.
 * - User authentication (Login/Logout).
 * - Navigation between different screens (Dashboard, Settings, etc.).
 * - Dynamic theming based on user preferences.
 */
class MainActivity : BaseActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (!allGranted) {
            // User denied at least one permission. Handle accordingly,
            // e.g., show a message or disable functionality.
            Toast.makeText(this, "Some permissions were denied. The app may not function as expected.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request permissions on startup
        requestPermissions()

        setContent {
            // Check if initial setup is complete and show the appropriate screen
            val isSetupComplete by rememberSaveable { mutableStateOf(sharedPrefsManager.isInitialSetupComplete()) }

            ktimaz {
                if (isSetupComplete) {
                    // Show the main app content
                    MainContent(sharedPrefsManager = sharedPrefsManager, soundEffectManager = soundEffectManager)
                } else {
                    // Show the initial setup dialog
                    InitialSetupDialog {
                        sharedPrefsManager.setInitialSetupComplete(true)
                        // Relaunch the activity to show the main content
                        recreate()
                    }
                }
            }
        }
    }

    /**
     * Requests necessary permissions for the app, including storage permissions for older Android versions.
     */
    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Permissions for Android 13+
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO,
                )
            )
        } else {
            // Permissions for Android 12 and below
            @Suppress("DEPRECATION")
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }
}

// --- App Navigation Enum ---
/**
 * Defines the different navigation destinations (screens) in the app.
 */
sealed class Screen(val route: String, val icon: ImageVector, val title: String) {
    object Dashboard : Screen("dashboard", Icons.Default.Dashboard, "Dashboard")
    object Settings : Screen("settings", Icons.Default.Settings, "Settings")
    object About : Screen("about", Icons.Default.Info, "About")
    object Login : Screen("login", Icons.Default.AccountCircle, "Login")
    object Registration : Screen("registration", Icons.Default.AccountBox, "Register")
    object PrivacyPolicy : Screen("privacy_policy", Icons.Default.Policy, "Privacy Policy")
}

// --- MainContent Composable ---
/**
 * The main composable that manages the application's UI and navigation.
 * It's the root of the app's UI tree after the initial setup is complete.
 * @param sharedPrefsManager The SharedPreferences manager instance.
 * @param soundEffectManager The SoundEffectManager instance for audio feedback.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    sharedPrefsManager: SharedPreferencesManager,
    soundEffectManager: SoundEffectManager
) {
    val isLoggedIn = rememberSaveable { mutableStateOf(sharedPrefsManager.isLoggedIn()) }
    var currentScreen by rememberSaveable { mutableStateOf<Screen>(Screen.Dashboard) }

    Scaffold(
        topBar = {
            // The TopAppBar adapts to the current screen and login state.
            CenterAlignedTopAppBar(
                title = { Text(currentScreen.title) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    if (isLoggedIn.value) {
                        IconButton(onClick = {
                            soundEffectManager.playClickSound()
                            sharedPrefsManager.setLoggedIn(false)
                            isLoggedIn.value = false
                            currentScreen = Screen.Dashboard
                            Toast.makeText(
                                LocalContext.current,
                                "Logged out successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            // Placeholder for a BottomAppBar if needed for mobile layouts.
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Side navigation rail for large screens
            NavigationRail(
                modifier = Modifier.padding(innerPadding).fillMaxHeight()
            ) {
                // Determine which screens to show based on login status
                val screensToShow = if (isLoggedIn.value) {
                    listOf(
                        Screen.Dashboard,
                        Screen.Settings,
                        Screen.About
                    )
                } else {
                    listOf(
                        Screen.Dashboard,
                        Screen.Login,
                        Screen.Settings,
                        Screen.About
                    )
                }

                screensToShow.forEach { screen ->
                    NavigationRailItem(
                        selected = currentScreen == screen,
                        onClick = {
                            soundEffectManager.playClickSound()
                            currentScreen = screen
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Main content area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(targetState = currentScreen, label = "ScreenCrossfade") { screen ->
                    when (screen) {
                        is Screen.Dashboard -> DashboardScreen(soundEffectManager)
                        is Screen.Settings -> SettingsScreen(sharedPrefsManager, soundEffectManager)
                        is Screen.About -> AboutScreen(soundEffectManager)
                        is Screen.Login -> LoginScreen(onLoginSuccess = { username ->
                            soundEffectManager.playClickSound()
                            sharedPrefsManager.setLoggedIn(true, username)
                            isLoggedIn.value = true
                            currentScreen = Screen.Dashboard
                            Toast.makeText(
                                LocalContext.current,
                                "Welcome, $username!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }, onNavigateToRegistration = {
                            soundEffectManager.playClickSound()
                            currentScreen = Screen.Registration
                        })
                        is Screen.Registration -> RegistrationScreen(onRegistrationSuccess = {
                            soundEffectManager.playClickSound()
                            currentScreen = Screen.Login
                            Toast.makeText(
                                LocalContext.current,
                                "Registration successful! Please log in.",
                                Toast.LENGTH_LONG
                            ).show()
                        })
                        else -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Screen not found for route: ${screen.route}")
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- InitialSetupDialog Composable ---
/**
 * Displays a dialog to guide the user through the initial setup process.
 * This includes a brief introduction and a permission request prompt.
 *
 * @param onSetupComplete Callback to be invoked when the setup is successfully completed.
 */
@Composable
fun InitialSetupDialog(onSetupComplete: () -> Unit) {
    val context = LocalContext.current
    val soundEffectManager = SoundEffectManager(context, SharedPreferencesManager(context)) // Use a temporary manager for the dialog
    var hasStoragePermission by remember {
        mutableStateOf(checkStoragePermission(context))
    }

    // This launcher is for requesting permissions from within a Composable
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(context, "Permission granted!", Toast.LENGTH_SHORT).show()
            hasStoragePermission = true
        } else {
            Toast.makeText(context, "Permissions are required for the app to function properly.", Toast.LENGTH_LONG).show()
            hasStoragePermission = false
        }
    }

    AlertDialog(
        onDismissRequest = { /* Cannot dismiss until setup is complete */ },
        title = { Text("Welcome to Ktimaz Studio") },
        text = {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Please grant storage permissions to allow the app to function correctly.",
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    soundEffectManager.playClickSound()
                    // Launch the permission request
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_MEDIA_IMAGES,
                                Manifest.permission.READ_MEDIA_VIDEO,
                                Manifest.permission.READ_MEDIA_AUDIO,
                            )
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        permissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                    }
                }) {
                    Text("Grant Permissions")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    soundEffectManager.playClickSound()
                    // Only allow finishing if permission is granted
                    if (hasStoragePermission) {
                        onSetupComplete()
                    } else {
                        Toast.makeText(context, "Please grant storage permission to continue.", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = hasStoragePermission // Enable "Continue" only if permission is granted
            ) {
                Text("Continue")
            }
        }
    )
}


/**
 * Helper function to check if storage permission is granted.
 */
fun checkStoragePermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
    } else {
        @Suppress("DEPRECATION")
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
}


// --- DashboardScreen Composable ---
/**
 * Represents the main dashboard screen with a grid of cards for different app features.
 *
 * @param soundEffectManager The SoundEffectManager instance for audio feedback.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(soundEffectManager: SoundEffectManager) {
    val haptic = LocalHapticFeedback.current

    val titles = listOf(
        "Screen Graph", "Data Structure", "Algorithm", "Game Development",
        "Screen Graph", "Data Structure", "Algorithm", "Game Development"
    )

    val icons = listOf(
        painterResource(id = R.drawable.ic_graph_canvas), // Assuming you have a placeholder icon
        Icons.Default.Storage,
        Icons.Default.HistoryEdu,
        Icons.Default.Dashboard,
        painterResource(id = R.drawable.ic_graph_canvas),
        Icons.Default.Storage,
        Icons.Default.HistoryEdu,
        Icons.Default.Dashboard
    )

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(titles.size) { index ->
            val scale by animateFloatAsState(
                targetValue = 1f, // Always 1, not changing dynamically here.
                animationSpec = tween(durationMillis = 500, easing = EaseOutExpo),
                label = "CardScaleAnimation"
            )

            Card(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    soundEffectManager.playClickSound()
                    // Handle card click
                    Toast.makeText(
                        LocalContext.current,
                        "Clicked ${titles[index]}",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .then(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Modifier.blur(2.dp) else Modifier)
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val icon = icons[index % icons.size]
                    if (icon is ImageVector) {
                        Icon(
                            imageVector = icon,
                            contentDescription = titles[index],
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    } else if (icon is androidx.compose.ui.graphics.painter.Painter) {
                        Image(
                            painter = icon,
                            contentDescription = titles[index],
                            modifier = Modifier.size(60.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = titles[index],
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


// --- LoginScreen Composable ---
/**
 * Provides the login functionality for the user.
 *
 * @param onLoginSuccess Callback to be invoked on successful login.
 * @param onNavigateToRegistration Callback to navigate to the registration screen.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegistration: () -> Unit
) {
    val context = LocalContext.current
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var showProgress by rememberSaveable { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Login", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    leadingIcon = { Icon(Icons.Outlined.AccountCircle, "Username") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, "Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, "Toggle password visibility")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        showProgress = true
                        focusManager.clearFocus()
                        // Simulate network login
                        val correctUsername = "testuser"
                        val correctPasswordHash = hashPassword("password123") // Hash a simple password for testing

                        if (username == correctUsername && hashPassword(password) == correctPasswordHash) {
                            onLoginSuccess(username)
                        } else {
                            Toast.makeText(context, "Invalid username or password", Toast.LENGTH_SHORT).show()
                            showProgress = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !showProgress && username.isNotEmpty() && password.isNotEmpty()
                ) {
                    if (showProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Login")
                    }
                }

                TextButton(onClick = onNavigateToRegistration) {
                    Text("Don't have an account? Register here.")
                }
            }
        }
    }
}

// --- RegistrationScreen Composable ---
/**
 * Provides the user registration functionality.
 *
 * @param onRegistrationSuccess Callback to be invoked on successful registration.
 */
@Composable
fun RegistrationScreen(onRegistrationSuccess: () -> Unit) {
    val context = LocalContext.current
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var showProgress by rememberSaveable { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Register", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    leadingIcon = { Icon(Icons.Outlined.AccountCircle, "Username") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, "Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, "Toggle password visibility")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, "Confirm Password") },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    trailingIcon = {
                        val image = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(imageVector = image, "Toggle password visibility")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        showProgress = true
                        focusManager.clearFocus()
                        if (password == confirmPassword) {
                            // Simulate registration process
                            // In a real app, you would send this to a backend
                            onRegistrationSuccess()
                        } else {
                            Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                            showProgress = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !showProgress && username.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()
                ) {
                    if (showProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Register")
                    }
                }
            }
        }
    }
}

/**
 * Hashes a string using SHA-256. This is a simple helper function for demonstration purposes.
 * In a real application, a more robust and secure method like BCrypt should be used.
 */
fun hashPassword(password: String): String {
    return try {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        val hexString = StringBuilder()
        for (byte in hashBytes) {
            val hex = Integer.toHexString(0xff and byte.toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
        hexString.toString()
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

// --- SettingsScreen Composable ---
/**
 * Allows the user to configure app settings such as theme and sound.
 *
 * @param sharedPrefsManager The SharedPreferences manager for reading and writing settings.
 * @param soundEffectManager The SoundEffectManager for handling audio feedback.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(sharedPrefsManager: SharedPreferencesManager, soundEffectManager: SoundEffectManager) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val currentThemeSetting = remember { mutableStateOf(sharedPrefsManager.getThemeSetting()) }
    val isSoundEnabled = remember { mutableStateOf(sharedPrefsManager.isSoundEnabled()) }
    val currentLanguage = remember { mutableStateOf(sharedPrefsManager.getLanguageSetting()) }

    val languages = listOf("en", "es", "fr", "hi", "ja", "ko", "pt", "ru", "ar") // Example languages
    var expanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("App Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        HorizontalDivider()

        // --- Theme Settings ---
        Text("Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            ThemeSetting.entries.forEachIndexed { index, theme ->
                SegmentedButton(
                    selected = currentThemeSetting.value == theme,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        soundEffectManager.playClickSound()
                        currentThemeSetting.value = theme
                        sharedPrefsManager.setThemeSetting(theme)
                        // A reboot or recreate() is often required to apply theme changes immediately
                        (context as? ComponentActivity)?.recreate()
                    },
                    shape = SegmentedButtonDefaults.shape(index, ThemeSetting.entries.size)
                ) {
                    Text(theme.name.lowercase().capitalize())
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // --- Sound Settings ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isSoundEnabled.value) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = "Sound",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(16.dp))
                Text("Sound Effects", style = MaterialTheme.typography.titleMedium)
            }
            Switch(
                checked = isSoundEnabled.value,
                onCheckedChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isSoundEnabled.value = it
                    sharedPrefsManager.setSoundEnabled(it)
                    // If sound is enabled, play a click sound immediately to confirm
                    if (it) soundEffectManager.playClickSound()
                }
            )
        }

        // --- Language Settings ---
        HorizontalDivider()

        Text("Language", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.TopStart)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)),
                        RoundedCornerShape(8.dp)
                    )
                    .clickable(
                        onClick = { expanded = true },
                        indication = LocalIndication.current,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Language,
                    contentDescription = "Language",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Current Language: ${currentLanguage.value.uppercase()}",
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown"
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                languages.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(language.uppercase()) },
                        onClick = {
                            soundEffectManager.playClickSound()
                            currentLanguage.value = language
                            sharedPrefsManager.setLanguageSetting(language)
                            expanded = false
                            // Note: To truly change the app's language, you would need
                            // to handle resource localization and potentially recreate the activity.
                            Toast.makeText(context, "Language set to $language", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

// --- AboutScreen Composable ---
/**
 * Displays information about the application.
 *
 * @param soundEffectManager The SoundEffectManager instance for audio feedback.
 */
@Composable
fun AboutScreen(soundEffectManager: SoundEffectManager) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("About Ktimaz Studio", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        HorizontalDivider()

        Text(
            text = "Ktimaz Studio is a creative space for developers to build and explore various tools, games, and applications.",
            style = MaterialTheme.typography.bodyLarge
        )

        HorizontalDivider()

        Text("Version: 1.0.0", style = MaterialTheme.typography.bodyMedium)
        Text("Developer: Ktimaz", style = MaterialTheme.typography.bodyMedium)

        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Privacy Policy", style = MaterialTheme.typography.bodyMedium)
            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                soundEffectManager.playClickSound()
                // Handle navigation to Privacy Policy screen/dialog
                // (Currently, this is not a separate screen, so we'll just show a toast)
                Toast.makeText(LocalContext.current, "Navigating to Privacy Policy", Toast.LENGTH_SHORT).show()
            }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Privacy Policy")
            }
        }
    }
}
