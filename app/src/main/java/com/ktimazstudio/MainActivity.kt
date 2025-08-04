package com.ktimazstudio

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
import android.provider.Settings
import android.os.Bundle
import android.os.Debug
import android.widget.Toast
import java.io.BufferedReader
import java.io.InputStreamReader
import androidx.activity.enableEdgeToEdge
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Lock
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.ktimazstudio.ui.theme.KtimazStudioTheme // Corrected import
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.security.MessageDigest
import kotlin.experimental.and
import androidx.compose.foundation.isSystemInDarkTheme
import android.os.PowerManager
import androidx.compose.foundation.LocalIndication
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

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
            .setUsage(AudioAttributes.USAGE_GAME) // Appropriate usage for UI sounds
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1) // Only one click sound stream at a time to prevent overlap
            .setAudioAttributes(audioAttributes)
            .build()

        // Load your click sound from res/raw.
        // Make sure you have a file named 'click_sound.wav' (or .mp3) in res/raw.
        // You will need to create the 'raw' directory inside 'app/src/main/res/'
        // and place your sound file there.
        // This line is commented out as the resource ID may not exist.
        // clickSoundId = soundPool?.load(context, R.raw.click_sound, 1) ?: 0
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
 * Manages user login status, username, theme settings, and sound settings
 * using SharedPreferences for persistent storage.
 */
class SharedPreferencesManager(context: Context) {
    val prefs: SharedPreferences = context.getSharedPreferences("AppPrefsKtimazStudio", Context.MODE_PRIVATE)
    private val context: Context = context // Keep context for theme checks

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in_key"
        private const val KEY_USERNAME = "username_key"
        const val KEY_THEME_SETTING = "theme_setting_key" // Made public
        const val KEY_SOUND_ENABLED = "sound_enabled_key" // Made public
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
            ThemeSetting.SYSTEM // Fallback to system if stored value is invalid
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
        return prefs.getBoolean(KEY_SOUND_ENABLED, true) // Default to true
    }

    /**
     * Sets the sound effects enabled status.
     * @param enabled The new sound status.
     */
    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }
}

// --- Top-level utility functions ---

/**
 * Checks if the device has an active and validated internet connection.
 * @param context The application context.
 * return true if connected to the internet, false otherwise.
 */
fun isConnected(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

/**
 * Opens the Wi-Fi settings panel for the user to connect to a network.
 * Displays a toast message instructing the user.
 * @param context The application context.
 */
fun openWifiSettings(context: Context) {
    Toast.makeText(context, "Please enable Wi-Fi or connect to a network.", Toast.LENGTH_LONG).show()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // For Android 10 (Q) and above, use Settings.Panel.ACTION_WIFI for a panel.
        context.startActivity(Intent(Settings.Panel.ACTION_WIFI))
    } else {
        // For older Android versions, use ACTION_WIFI_SETTINGS.
        @Suppress("DEPRECATION")
        context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
    }
}

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
    // --- IMPORTANT: UPDATE THIS HASH TO YOUR APP'S RELEASE SIGNATURE SHA-256 HASH ---
    // You provided this in your last message: f21317d4d6276ff3174a363c7fdff4171c73b1b80a82bb9082943ea9200a8425
    private val EXPECTED_APK_HASH = "f21317d4d6276ff3174a363c7fdff4171c73b1b80a82bb9082943ea9200a8425".lowercase()

    /**
     * Checks if a debugger is currently attached to the application process.
     * return true if a debugger is connected, false otherwise.
     */
    fun isDebuggerConnected(): Boolean {
        // LocalInspectionMode.current check is handled at the call site in Composable
        return Debug.isDebuggerConnected() || isTracerAttached()
    }

    /**
     * Checks if a VPN connection is active.
     * This method iterates through all active networks and checks for the VPN transport.
     * return true if a VPN is detected and it has internet capabilities, false otherwise.
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
     * return The registered NetworkCallback instance, which should be unregistered later.
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
                // When a network is lost, re-check if any VPN is still active
                onVpnStatusChanged(isVpnActive())
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                onVpnStatusChanged(isVpnActive())
            }
        }
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        return networkCallback
    }

    /**
     * Unregisters a previously registered NetworkCallback.
     * @param networkCallback The callback to unregister.
     */
    fun unregisterVpnDetectionCallback(networkCallback: ConnectivityManager.NetworkCallback) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }


    /**
     * Attempts to detect if the application is running on an emulator.
     * This check is not exhaustive and can be bypassed.
     * return true if an emulator is likely detected, false otherwise.
     */
    fun isRunningOnEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || "google_sdk" == Build.PRODUCT)
    }

    /**
     * Attempts to detect if the device is rooted.
     * This check is not exhaustive and can be bypassed.
     * return true if root is likely detected, false otherwise.
     */
    fun isDeviceRooted(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }

        // Check for test-keys in build tags (common for custom ROMs/rooted devices)
        if (Build.TAGS != null && Build.TAGS.contains("test-keys")) {
            return true
        }

        // Check if `su` command can be executed
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val reader = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
            if (reader.readLine() != null) return true
        } catch (e: Exception) {
            // Command not found or other error, likely not rooted
        } finally {
            process?.destroy()
        }

        return false
    }

    /**
     * Calculates the SHA-256 hash of the application's *signing certificate*.
     * This is a more robust integrity check than file hash as it remains constant
     * for signed APKs regardless of minor build variations.
     * return The SHA-256 hash as a hexadecimal string, or null if calculation fails.
     */
     fun getSignatureSha256Hash(): String? {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            if (signatures != null && signatures.isNotEmpty()) {
                val md = MessageDigest.getInstance("SHA-256")
                // For most apps, there's only one signing certificate. If multiple, you might need to handle.
                val hashBytes = md.digest(signatures[0].toByteArray())
                return hashBytes.joinToString("") { "%02x".format(it.and(0xff.toByte())) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Checks if the APK's *signature hash* matches the expected hash.
     * This is now the primary integrity check.
     * return true if the signature hash matches, false otherwise.
     */

    /**
     * REMOVED: This method is no longer used for integrity check, as signature hash is more reliable.
     * Kept for reference or if needed for other purposes.
     *
     * Calculates the SHA-256 hash of the application's APK file.
     * This can be used to detect if the APK has been tampered with.
     * return The SHA-256 hash as a hexadecimal string, or null if calculation fails.
     */
    fun getApkSha256Hash_UNUSED(): String? {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val apkPath = packageInfo.applicationInfo?.sourceDir ?: return null
            val file = File(apkPath)
            if (file.exists()) {
                val bytes = file.readBytes()
                val digest = MessageDigest.getInstance("SHA-256")
                val hashBytes = digest.digest(bytes)
                return hashBytes.joinToString("") { "%02x".format(it and 0xff.toByte()) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Checks if the APK's signature hash matches the expected hash.
     * This is now the primary integrity check.
     * return true if the signature hash matches, false otherwise.
     */
    fun isApkTampered(): Boolean {
        // LocalInspectionMode.current check is handled at the call site in Composable
        val currentSignatureHash = getSignatureSha256Hash()
        return currentSignatureHash != null && currentSignatureHash != EXPECTED_APK_HASH
    }

    /**
     * Attempts to detect common hooking frameworks (like Xposed or Frida) by checking
     * for known files, installed packages, or system properties.
     * This is not exhaustive and can be bypassed, but adds a layer of defense.
     * return true if a hooking framework is likely detected, false otherwise.
     */
    fun isHookingFrameworkDetected(): Boolean {
        // LocalInspectionMode.current check is handled at the call site in Composable
        // 1. Check for common Xposed/Magisk/Frida related files/directories
        val knownHookFiles = arrayOf(
            "/system/app/XposedInstaller.apk",
            "/system/bin/app_process_xposed",
            "/system/lib/libxposed_art.so",
            "/data/app/de.robv.android.xposed.installer",
            "/data/data/de.robv.android.xposed.installer",
            "/dev/frida", // Frida device file
            "/data/local/tmp/frida-agent.so", // Common Frida agent path
            "/data/local/frida/frida-server", // Frida server path
            "/sbin/magisk", // Magisk detection
            "/system/xbin/magisk"
        )
        for (path in knownHookFiles) {
            if (File(path).exists()) return true
        }
        // 2. Check for common system properties (related to Xposed)
        val props = listOf("xposed.active", "xposed.api_level", "xposed.installed")
        try {
            val process = Runtime.getRuntime().exec("getprop")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (true) {
                line = reader.readLine()
                if (line == null) break
                for (prop in props) {
                    if (line.contains("[$prop]:")) return true
                    }
            }
            process.destroy()
        } catch (e: Exception) {
            // Log.e("SecurityCheck", "Error checking system properties: ${e.message}")
        }
        // 3. Check for common packages (Xposed installer)
        try {
            context.packageManager.getPackageInfo("de.robv.android.xposed.installer", PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            // Package not found, which is good
        } catch (e: Exception) {
            // Log.e("SecurityCheck", "Error checking Xposed installer package: ${e.message}")
        }
        // 4. Check for suspicious loaded libraries (less reliable, but adds another layer)
        // This would require reading /proc/self/maps and checking for known hooking library names.
        // This is more complex and might lead to false positives, so omitted for brevity.
        return false
    }

    /**
     * Checks for known files related to tracer processes (e.g., used by debugger or ftrace).
     * This is a simple check and can be easily bypassed, but serves as a basic deterrent.
     * @return True if a tracer is likely attached, false otherwise.
     */
    fun isTracerAttached(): Boolean {
        return try {
            val file = File("/proc/self/status")
            val reader = BufferedReader(InputStreamReader(file.inputStream()))
            var line: String?
            while (true) {
                line = reader.readLine()
                if (line == null) break
                if (line.startsWith("TracerPid:")) {
                    val tracerPid = line.substringAfter("TracerPid:").trim().toIntOrNull() ?: 0
                    return tracerPid != 0
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }
}

// --- MainActivity class starts here ---
class MainActivity : ComponentActivity() {

    private lateinit var sharedPrefsManager: SharedPreferencesManager
    private lateinit var soundEffectManager: SoundEffectManager
    private lateinit var securityManager: SecurityManager
    private var vpnNetworkCallback: ConnectivityManager.NetworkCallback? = null


    // A function to check if the device is in a battery-saver state (dark theme may be forced)
    private fun isBatterySaverEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isPowerSaveMode
        } else {
            false
        }
    }

    // A function to determine the actual dark theme status based on user preference and system state
    private fun getSystemOrBatterySaverTheme(context: Context, themeSetting: ThemeSetting): Boolean {
        val isSystemInDark = isSystemInDarkTheme()
        val isBatterySaver = isBatterySaverEnabled(context)

        return when (themeSetting) {
            ThemeSetting.LIGHT -> false
            ThemeSetting.DARK -> true
            ThemeSetting.SYSTEM -> isSystemInDark
            ThemeSetting.BATTERY_SAVER -> if (isBatterySaver) true else isSystemInDark
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sharedPrefsManager = SharedPreferencesManager(this)
        soundEffectManager = SoundEffectManager(this, sharedPrefsManager)
        securityManager = SecurityManager(this)

        // Initialize sound effects
        soundEffectManager.loadSounds()

        // VPN Detection setup
        vpnNetworkCallback = securityManager.registerVpnDetectionCallback { isVpnActive ->
            if (isVpnActive) {
                // Handle VPN detected state (e.g., show a warning dialog)
                // This will be handled inside the Composable now.
            }
        }

        setContent {
            // Get the current theme setting from SharedPreferences
            val currentThemeSetting = remember { mutableStateOf(sharedPrefsManager.getThemeSetting()) }
            val darkTheme = getSystemOrBatterySaverTheme(LocalContext.current, currentThemeSetting.value)

            // Define the animated theme color.
            val animatedBackground = animateColorAsState(
                targetValue = if (darkTheme) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background,
                animationSpec = tween(1000), // Animate over 1 second
                label = "backgroundAnimation"
            )

            // Apply the theme with the animated background color
            KtimazStudioTheme(darkTheme = darkTheme) { // Corrected theme function call
                // A state to manage the main app content visibility
                var showMainContent by remember { mutableStateOf(sharedPrefsManager.isLoggedIn()) }

                // The Surface is the main container, animated with the theme change
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(animatedBackground.value),
                    color = Color.Transparent // Surface background is now transparent to show the animated color
                ) {
                    // Use an AnimatedContent transition for a smooth switch between screens
                    AnimatedContent(
                        targetState = showMainContent,
                        label = "screenTransition",
                        transitionSpec = {
                            if (targetState) {
                                // Fade in and slide in from right for the main content
                                slideInHorizontally { width -> width } + fadeIn() togetherWith
                                // Fade out and slide out to left for the login screen
                                slideOutHorizontally { width -> -width } + fadeOut()
                            } else {
                                // Fade in and slide in from left for the login screen
                                slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                // Fade out and slide out to right for the main content
                                slideOutHorizontally { width -> width } + fadeOut()
                            }.using(
                                SizeTransform(clip = false)
                            )
                        }
                    ) { targetShowMainContent ->
                        if (targetShowMainContent) {
                            MainAppContent(
                                sharedPrefsManager = sharedPrefsManager,
                                soundEffectManager = soundEffectManager,
                                onLogout = {
                                    showMainContent = false
                                    sharedPrefsManager.setLoggedIn(false)
                                },
                                onThemeChanged = { newTheme ->
                                    sharedPrefsManager.setThemeSetting(newTheme)
                                    currentThemeSetting.value = newTheme
                                }
                            )
                        } else {
                            LoginScreen(
                                onLoginSuccess = { username ->
                                    showMainContent = true
                                    sharedPrefsManager.setLoggedIn(true, username)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


// --- MainAppContent Composable ---
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainAppContent(
    sharedPrefsManager: SharedPreferencesManager,
    soundEffectManager: SoundEffectManager,
    onLogout: () -> Unit,
    onThemeChanged: (ThemeSetting) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val hapticEnabled = sharedPrefsManager.isSoundEnabled() // Use sound setting for haptic
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isDrawerOpen by rememberSaveable { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showVpnWarning by remember { mutableStateOf(false) }

    // State for the theme setting, which we can update from the UI
    val currentThemeSetting = remember { mutableStateOf(sharedPrefsManager.getThemeSetting()) }

    // VPN Warning logic (simplified for Composable context)
    LaunchedEffect(Unit) {
        while(true) {
            showVpnWarning = SecurityManager(context).isVpnActive()
            delay(5000L) // Check every 5 seconds
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Ktimaz Studio", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = {
                        isDrawerOpen = !isDrawerOpen
                        if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        soundEffectManager.playClickSound()
                    }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    // Theme toggling button
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = {
                            PlainTooltip {
                                Text("Change Theme")
                            }
                        },
                        state = rememberTooltipState()
                    ) {
                        IconButton(onClick = {
                            showSettingsDialog = true
                            if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            soundEffectManager.playClickSound()
                        }) {
                            Icon(Icons.Filled.ColorLens, contentDescription = "Change Theme")
                        }
                    }
                    IconButton(onClick = {
                        onLogout()
                        if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        soundEffectManager.playClickSound()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                scrollBehavior = scrollBehavior
            )
        },
        // The original bottomBar has been removed as it was also an unresolved reference.
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        ModalNavigationDrawer(
            drawerState = rememberDrawerState(
                initialValue = if (isDrawerOpen) DrawerValue.Open else DrawerValue.Closed
            ),
            drawerContent = {
                ModalDrawerSheet {
                    Text("Drawer title", modifier = Modifier.padding(16.dp))
                    Divider()
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Dashboard, contentDescription = null) },
                        label = { Text("Dashboard") },
                        selected = false,
                        onClick = { /*TODO*/ },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                        label = { Text("Settings") },
                        selected = false,
                        onClick = {
                            showSettingsDialog = true
                            isDrawerOpen = false
                            if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            soundEffectManager.playClickSound()
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                        label = { Text("Logout") },
                        selected = false,
                        onClick = {
                            isDrawerOpen = false
                            onLogout()
                            if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            soundEffectManager.playClickSound()
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            },
            gesturesEnabled = isDrawerOpen
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // VPN warning banner
                    if (showVpnWarning) {
                        VpnWarningBanner(onDismiss = { showVpnWarning = false })
                    }
                    // Main content grid, using the corrected HomeGrid
                    HomeGrid(soundEffectManager)
                }
            }
        }
    }

    // Settings Dialog with Theme & Sound options
    if (showSettingsDialog) {
        SettingsDialog(
            currentThemeSetting = currentThemeSetting.value,
            onThemeSelected = { newTheme ->
                onThemeChanged(newTheme)
                currentThemeSetting.value = newTheme
            },
            isSoundEnabled = sharedPrefsManager.isSoundEnabled(),
            onSoundToggle = { enabled ->
                sharedPrefsManager.setSoundEnabled(enabled)
            },
            onDismissRequest = {
                showSettingsDialog = false
            }
        )
    }
}

// --- Settings Dialog Composable ---
@Composable
fun SettingsDialog(
    currentThemeSetting: ThemeSetting,
    onThemeSelected: (ThemeSetting) -> Unit,
    isSoundEnabled: Boolean,
    onSoundToggle: (Boolean) -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "Settings")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Theme selection radio buttons
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ThemeSetting.entries.forEach { theme ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                onThemeSelected(theme)
                            }
                        ) {
                            RadioButton(
                                selected = (theme == currentThemeSetting),
                                onClick = { onThemeSelected(theme) }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(text = theme.name.replace("_", " "))
                        }
                    }
                }
                // Sound toggle switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Sound Effects",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Switch(
                        checked = isSoundEnabled,
                        onCheckedChange = onSoundToggle,
                        thumbContent = {
                            Icon(
                                imageVector = if (isSoundEnabled) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                                contentDescription = if (isSoundEnabled) "Sound On" else "Sound Off"
                            )
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Close")
            }
        }
    )
}

// --- LoginScreen Composable ---
@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isLoginError by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val isDebuggerConnected = LocalInspectionMode.current || Debug.isDebuggerConnected()
    val securityManager = SecurityManager(context)

    // Check for potential security issues when the screen is first composed
    LaunchedEffect(Unit) {
        if (securityManager.isDebuggerConnected()) {
            Toast.makeText(context, "Warning: Debugger detected.", Toast.LENGTH_LONG).show()
        }
        if (securityManager.isDeviceRooted()) {
            Toast.makeText(context, "Warning: Rooted device detected.", Toast.LENGTH_LONG).show()
        }
        if (securityManager.isHookingFrameworkDetected()) {
            Toast.makeText(context, "Warning: Hooking framework detected.", Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground), // Replace with your logo resource
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )

            Text(
                text = "Welcome to Ktimaz Studio",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Please enter your credentials to continue.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", color = MaterialTheme.colorScheme.onSurface) },
                leadingIcon = { Icon(Icons.Outlined.AccountCircle, "Username Icon", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = isLoginError,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = MaterialTheme.colorScheme.onSurface) },
                leadingIcon = { Icon(Icons.Outlined.Lock, "Password Icon", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = isLoginError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (username == "user" && password == "pass") {
                        isLoading = true
                        onLoginSuccess(username)
                    } else {
                        isLoginError = true
                        Toast.makeText(context, "Invalid username or password", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(text = "LOGIN", style = MaterialTheme.typography.labelLarge)
                }
            }

            if (isLoginError) {
                Text(
                    text = "Incorrect username or password. Please try again.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}


// --- HomeGrid Composable ---
@Composable
fun HomeGrid(soundEffectManager: SoundEffectManager) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val hapticEnabled = true // Assuming Haptic is always on for now
    val items = listOf("Analytics", "Settings", "Profile", "Security", "About", "Contact", "Dashboard", "Reports", "Help")

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items.size) { index ->
            GridContentCard(
                title = items[index],
                icon = Icons.Filled.Dashboard, // Fixed icon to resolve unresolved reference
                onClick = {
                    Toast.makeText(context, "${items[index]} clicked!", Toast.LENGTH_SHORT).show()
                    if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    soundEffectManager.playClickSound() // Play sound on click
                }
            )
        }
    }
}

// --- GridContentCard Composable ---
@Composable
fun GridContentCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "pressScaleAnimation"
    )

    // Using rememberInfiniteTransition for a cleaner infinite animation
    val infiniteTransition = rememberInfiniteTransition(label = "alphaAnimation")
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "animatedAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                alpha = animatedAlpha
            )
            .then(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Modifier.blur(2.dp) else Modifier)
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                // Remove the explicit rememberRipple and use the default ripple provided by Material 3
                // indication = rememberRipple(bounded = true, color = MaterialTheme.colorScheme.onPrimary)
                indication = LocalIndication.current
            ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                imageVector = icon, // Using imageVector for icons
                contentDescription = title,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                    .padding(8.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

// --- VpnWarningBanner Composable ---
@Composable
fun VpnWarningBanner(onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "Warning",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "VPN Detected. For security, please disable it.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}
