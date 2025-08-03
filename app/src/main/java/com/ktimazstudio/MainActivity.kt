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
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.ktimazstudio.ui.theme.ktimaz
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.security.MessageDigest
import kotlin.experimental.and
import androidx.compose.foundation.border
import androidx.compose.foundation.LocalIndication
import androidx.compose.ui.platform.LocalInspectionMode
import android.app.UiModeManager
import android.os.PowerManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import com.airbnb.lottie.compose.* // Added for Lottie animations
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.accessibility.semantics
import androidx.compose.ui.accessibility.contentDescription
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.roundToInt

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
 * Manages user login status, username, theme settings, and sound settings
 * using SharedPreferences for persistent storage.
 */
class SharedPreferencesManager(context: Context) {
    val prefs: SharedPreferences = context.getSharedPreferences("AppPrefsKtimazStudio", Context.MODE_PRIVATE)
    private val context: Context = context

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in_key"
        private const val KEY_USERNAME = "username_key"
        const val KEY_THEME_SETTING = "theme_setting_key"
        const val KEY_SOUND_ENABLED = "sound_enabled_key"
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
        context.startActivity(Intent(Settings.Panel.ACTION_WIFI))
    } else {
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

    private val EXPECTED_APK_HASH = "f21317d4d6276ff3174a363c7fdff4171c73b1b80a82bb9082943ea9200a8425".lowercase()

    /**
     * Checks if a debugger is currently attached to the application process.
     * return true if a debugger is connected, false otherwise.
     */
    fun isDebuggerConnected(): Boolean {
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
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                onVpnStatusChanged(isVpnActive())
            }

            override fun onLost(network: Network) {
                super.onLost(network)
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

        if (Build.TAGS != null && Build.TAGS.contains("test-keys")) {
            return true
        }

        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val reader = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
            if (reader.readLine() != null) return true
        } catch (e: Exception) {
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
                val hashBytes = md.digest(signatures[0].toByteArray())
                return hashBytes.joinToString("") { "%02x".format(it.and(0xff.toByte())) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

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
     * Attempts to detect common hooking frameworks (like Xposed or Frida) by checking
     * for known files, installed packages, or system properties.
     * This is not exhaustive and can be bypassed, but adds a layer of defense.
     * return true if a hooking framework is likely detected, false otherwise.
     */
    fun isHookingFrameworkDetected(): Boolean {
        val knownHookFiles = arrayOf(
            "/system/app/XposedInstaller.apk",
            "/system/bin/app_process_xposed",
            "/system/lib/libxposed_art.so",
            "/data/app/de.robv.android.xposed.installer",
            "/data/data/de.robv.android.xposed.installer",
            "/dev/frida",
            "/data/local/tmp/frida-agent.so",
            "/data/local/frida/frida-server",
            "/sbin/magisk",
            "/system/xbin/magisk"
        )
        for (path in knownHookFiles) {
            if (File(path).exists()) return true
        }

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
        }

        try {
            context.packageManager.getPackageInfo("de.robv.android.xposed.installer", PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
        } catch (e: Exception) {
        }

        return false
    }

    /**
     * Checks if the APK's *signature hash* matches the expected hash.
     * This is now the primary integrity check.
     * return true if the signature hash matches, false otherwise.
     */
    fun isApkTampered(): Boolean {
        val currentSignatureHash = getSignatureSha256Hash()
        return currentSignatureHash != null && currentSignatureHash.lowercase() != EXPECTED_APK_HASH.lowercase()
    }

    /**
     * Checks if the server's certificate matches the expected pinned certificate.
     * This is a basic check and should be expanded with proper SSL pinning in production.
     * @return true if certificate pinning fails, false otherwise.
     */
    fun isCertificatePinningValid(): Boolean {
        // Placeholder: In production, use OkHttp with CertificatePinner or a similar library
        // to pin the server's public key or certificate.
        // Example: Check against a hardcoded public key hash.
        val expectedCertHash = "sha256/xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" // Replace with actual hash
        // Implement actual SSL pinning logic here.
        // For now, return false (no issue detected) as a placeholder.
        return false
    }

    /**
     * Gets the size of the installed application (APK + data).
     * This can be used as a very basic indicator of tampering if the size changes unexpectedly.
     * return The app size in bytes, or -1 if unable to retrieve.
     */
    fun getAppSize(): Long {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val apkPath = packageInfo.applicationInfo?.sourceDir ?: return -1L
            val file = File(apkPath)
            return file.length()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return -1L
    }

    fun isTracerAttached(): Boolean {
        try {
            val statusFile = File("/proc/self/status")
            if (statusFile.exists()) {
                statusFile.bufferedReader().useLines { lines ->
                    val tracerPidLine = lines.firstOrNull { it.startsWith("TracerPid:") }
                    if (tracerPidLine != null) {
                        val pid = tracerPidLine.substringAfter("TracerPid:").trim().toInt()
                        return pid != 0
                    }
                }
            }
        } catch (e: Exception) {
        }
        return false
    }

    /**
     * Aggregates all security checks to determine if the app environment is secure.
     * @param isInspectionMode True if the app is running in a Compose preview/inspection mode.
     * return A SecurityIssue enum indicating the first detected issue, or SecurityIssue.NONE if secure.
     */
    fun getSecurityIssue(isInspectionMode: Boolean): SecurityIssue {
        if (isInspectionMode) {
            return SecurityIssue.NONE
        }

        if (isDebuggerConnected()) return SecurityIssue.DEBUGGER_ATTACHED
        if (isTracerAttached()) return SecurityIssue.DEBUGGER_ATTACHED
        if (isRunningOnEmulator()) return SecurityIssue.EMULATOR_DETECTED
        if (isDeviceRooted()) return SecurityIssue.ROOT_DETECTED
        if (isHookingFrameworkDetected()) return SecurityIssue.HOOKING_FRAMEWORK_DETECTED
        if (isApkTampered()) return SecurityIssue.APK_TAMPERED
        if (isVpnActive()) return SecurityIssue.VPN_ACTIVE
        if (isCertificatePinningValid()) return SecurityIssue.CERTIFICATE_PINNING_FAILED
        return SecurityIssue.NONE
    }
}

/**
 * Enum representing different types of security issues that can be detected.
 */
enum class SecurityIssue(val message: String) {
    NONE("No security issues detected."),
    VPN_ACTIVE("A VPN connection is active. Please disable it."),
    DEBUGGER_ATTACHED("Debugger detected. For security, the app cannot run."),
    EMULATOR_DETECTED("Emulator detected. For security, the app cannot run."),
    ROOT_DETECTED("Root access detected. For security, the app cannot run."),
    APK_TAMPERED("Application integrity compromised. Please reinstall."),
    HOOKING_FRAMEWORK_DETECTED("Hooking framework detected. For security, the app cannot run."),
    CERTIFICATE_PINNING_FAILED("Invalid server certificate detected. Please check your connection."),
    UNKNOWN("An unknown security issue occurred.")
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
    private lateinit var securityManager: SecurityManager
    private lateinit var soundEffectManager: SoundEffectManager
    private var vpnNetworkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        sharedPrefsManager = SharedPreferencesManager(applicationContext)
        soundEffectManager = SoundEffectManager(applicationContext, sharedPrefsManager)
        soundEffectManager.loadSounds()
        securityManager = SecurityManager(applicationContext)

        setContent {
            val context = LocalContext.current
            val isInspectionMode = LocalInspectionMode.current
            val currentThemeSetting = remember { mutableStateOf(sharedPrefsManager.getThemeSetting()) }
            val useDarkTheme = isAppInDarkTheme(currentThemeSetting.value, context)
            val isFirstLaunch = remember { mutableStateOf(sharedPrefsManager.prefs.getBoolean("is_first_launch", true)) }

            ktimaz(darkTheme = useDarkTheme) {
                if (isFirstLaunch.value) {
                    OnboardingScreen(
                        onComplete = {
                            sharedPrefsManager.prefs.edit().putBoolean("is_first_launch", false).apply()
                            isFirstLaunch.value = false
                        },
                        soundEffectManager = soundEffectManager
                    )
                } else {
                    val initialSecurityIssue = remember {
                        securityManager.getSecurityIssue(isInspectionMode)
                    }

                    if (initialSecurityIssue != SecurityIssue.NONE) {
                        SecurityAlertScreen(issue = initialSecurityIssue) { finishAffinity() }
                    } else {
                        DisposableEffect(Unit) {
                            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                                if (key == SharedPreferencesManager.KEY_THEME_SETTING) {
                                    currentThemeSetting.value = sharedPrefsManager.getThemeSetting()
                                }
                            }
                            sharedPrefsManager.prefs.registerOnSharedPreferenceChangeListener(listener)
                            onDispose {
                                sharedPrefsManager.prefs.unregisterOnSharedPreferenceChangeListener(listener)
                            }
                        }

                        var isLoggedIn by remember { mutableStateOf(sharedPrefsManager.isLoggedIn()) }
                        var currentUsername by remember(isLoggedIn) { mutableStateOf(sharedPrefsManager.getUsername()) }
                        var liveVpnDetected by remember { mutableStateOf(securityManager.isVpnActive()) }
                        var currentSecurityIssue by remember { mutableStateOf(SecurityIssue.NONE) }

                        DisposableEffect(Unit) {
                            vpnNetworkCallback = securityManager.registerVpnDetectionCallback { isVpn ->
                                liveVpnDetected = isVpn
                                if (isVpn) {
                                    currentSecurityIssue = SecurityIssue.VPN_ACTIVE
                                } else {
                                    currentSecurityIssue = securityManager.getSecurityIssue(isInspectionMode)
                                }
                            }
                            onDispose {
                                vpnNetworkCallback?.let { securityManager.unregisterVpnDetectionCallback(it) }
                            }
                        }

                        LaunchedEffect(Unit) {
                            while (true) {
                                delay(5000)
                                val issue = securityManager.getSecurityIssue(isInspectionMode)
                                if (issue != SecurityIssue.NONE && issue != currentSecurityIssue) {
                                    currentSecurityIssue = issue
                                } else if (currentSecurityIssue == SecurityIssue.VPN_ACTIVE && issue == SecurityIssue.NONE) {
                                    currentSecurityIssue = SecurityIssue.NONE
                                }
                            }
                        }

                        if (currentSecurityIssue != SecurityIssue.NONE) {
                            SecurityAlertScreen(issue = currentSecurityIssue) { finishAffinity() }
                        } else {
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
                                        },
                                        soundEffectManager = soundEffectManager,
                                        sharedPrefsManager = sharedPrefsManager
                                    )
                                } else {
                                    LoginScreen(
                                        onLoginSuccess = { loggedInUsername ->
                                            sharedPrefsManager.setLoggedIn(true, loggedInUsername)
                                            isLoggedIn = true
                                        },
                                        soundEffectManager = soundEffectManager
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        vpnNetworkCallback?.let { securityManager.unregisterVpnDetectionCallback(it) }
        soundEffectManager.release()
    }
}
/**
 * Determines if the app should be in dark theme based on the ThemeSetting.
 * Marked as @Composable because it calls isSystemInDarkTheme().
 */
@Composable
fun isAppInDarkTheme(themeSetting: ThemeSetting, context: Context): Boolean {
    val systemInDarkTheme = isSystemInDarkTheme()
    return when (themeSetting) {
        ThemeSetting.LIGHT -> false
        ThemeSetting.DARK -> true
        ThemeSetting.SYSTEM -> systemInDarkTheme
        ThemeSetting.BATTERY_SAVER -> {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                powerManager.isPowerSaveMode
            } else {
                systemInDarkTheme
            }
        }
    }
}

/**
 * Custom animated button widget for consistent button styling and animations.
 */
@Composable
fun AnimatedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    soundEffectManager: SoundEffectManager? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors()
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "animated_button_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1.0f,
        animationSpec = tween(150),
        label = "animated_button_alpha"
    )

    Button(
        onClick = {
            soundEffectManager?.playClickSound()
            onClick()
        },
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
            .height(56.dp)
            .semantics { contentDescription = "$text button" }, // Accessibility
        shape = RoundedCornerShape(20.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 10.dp),
        enabled = enabled,
        colors = colors
    ) {
        if (isLoading) {
            LottieLoadingAnimation()
        } else {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

/**
 * Custom gradient card widget for consistent card styling with animations.
 */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "gradient_card_scale"
    )

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp,
            hoveredElevation = 6.dp // Added hover elevation
        ),
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f)
                    )
                )
            )
    ) {
        content()
    }
}

/**
 * Lottie animation for loading states.
 */
@Composable
fun LottieLoadingAnimation(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_animation))
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = modifier.size(40.dp)
    )
}

/**
 * A generic security alert dialog displayed when a critical security issue is detected.
 * This dialog is non-dismissible, forcing the user to exit the application.
 * @param issue The SecurityIssue enum indicating the reason for the alert.
 * @param onExitApp Callback to be invoked when the "Exit Application" button is clicked.
 */
@Composable
fun SecurityAlertScreen(issue: SecurityIssue, onExitApp: () -> Unit) {
    MaterialTheme {
        AlertDialog(
            onDismissRequest = { /* Not dismissible by user action */ },
            icon = { Icon(Icons.Filled.Lock, contentDescription = "Security Alert Icon", tint = MaterialTheme.colorScheme.error) },
            title = { Text("Security Alert", color = MaterialTheme.colorScheme.error) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(issue.message, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (issue == SecurityIssue.APK_TAMPERED) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Please download the official app from the Play Store.",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                AnimatedButton(
                    text = "Exit Application",
                    onClick = onExitApp,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MainApplicationUI(
    username: String,
    onLogout: () -> Unit,
    soundEffectManager: SoundEffectManager,
    sharedPrefsManager: SharedPreferencesManager
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedDestination by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    var isRailExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

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
            onDestinationSelected = {
                soundEffectManager.playClickSound()
                selectedDestination = it
                isSearching = false
                searchQuery = ""
            },
            isExpanded = isRailExpanded,
            onMenuClick = {
                soundEffectManager.playClickSound()
                isRailExpanded = !isRailExpanded
            },
            soundEffectManager = soundEffectManager
        )

        Scaffold(
            modifier = Modifier
                .weight(1f)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                val isScrolled = scrollBehavior.state.contentOffset > 0.1f
                CenterAlignedTopAppBar(
                    title = {
                        AnimatedContent(
                            targetState = isSearching,
                            transitionSpec = {
                                if (targetState) {
                                    slideInHorizontally { it } + fadeIn() togetherWith
                                            slideOutHorizontally { -it } + fadeOut()
                                } else {
                                    slideInHorizontally { -it } + fadeIn() togetherWith
                                            slideOutHorizontally { it } + fadeOut()
                                }
                            }, label = "search_bar_transition"
                        ) { searching ->
                            if (searching) {
                                CustomSearchBar(
                                    query = searchQuery,
                                    onQueryChange = { searchQuery = it },
                                    onClear = { searchQuery = ""; isSearching = false },
                                    soundEffectManager = soundEffectManager
                                )
                            } else {
                                Text(
                                    text = stringResource(id = R.string.app_name),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    },
                    actions = {
                        if (selectedDestination == Screen.Dashboard) {
                            IconButton(onClick = {
                                soundEffectManager.playClickSound()
                                isSearching = !isSearching
                                if (!isSearching) searchQuery = ""
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = if (isSearching) "Close Search" else "Search",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
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
                        Screen.Dashboard -> AnimatedCardGrid(
                            searchQuery = searchQuery,
                            onCardClick = { title ->
                                soundEffectManager.playClickSound()
                                if (title == "System Config") {
                                    context.startActivity(Intent(context, SettingsActivity::class.java))
                                } else {
                                    context.startActivity(Intent(context, ComingActivity::class.java).putExtra("CARD_TITLE", title))
                                }
                            },
                            soundEffectManager = soundEffectManager
                        )
                        Screen.AppSettings -> SettingsScreen(
                            soundEffectManager = soundEffectManager,
                            sharedPrefsManager = sharedPrefsManager
                        )
                        Screen.Profile -> ProfileScreen(username = username, onLogout = onLogout, soundEffectManager = soundEffectManager)
                    }
                }
            }
        }
    }
}

/**
 * Custom Search Bar Composable for the Top App Bar with debounced input.
 */
@Composable
fun CustomSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    soundEffectManager: SoundEffectManager
) {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    var tempQuery by remember { mutableStateOf(query) }

    OutlinedTextField(
        value = tempQuery,
        onValueChange = { newValue ->
            tempQuery = newValue
            coroutineScope.launch {
                delay(300) // Debounce for 300ms
                onQueryChange(newValue)
            }
        },
        placeholder = { Text("Search modules...", style = MaterialTheme.typography.bodyLarge) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") },
        trailingIcon = {
            if (tempQuery.isNotEmpty()) {
                IconButton(onClick = {
                    soundEffectManager.playClickSound()
                    onClear()
                    focusManager.clearFocus()
                }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Clear Search")
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f),
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(vertical = 4.dp)
            .semantics { contentDescription = "Search bar" } // Accessibility
    )
}

@Composable
fun AppNavigationRail(
    selectedDestination: Screen,
    onDestinationSelected: (Screen) -> Unit,
    isExpanded: Boolean,
    onMenuClick: () -> Unit,
    soundEffectManager: SoundEffectManager,
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
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.9f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "menu_icon_scale"
            )

            IconButton(
                onClick = onMenuClick,
                interactionSource = interactionSource,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .minimumInteractiveComponentSize() // Accessibility: 48.dp touch target
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
        destinations.forEachIndexed { index, screen ->
            var itemVisible by remember { mutableStateOf(isExpanded) }
            LaunchedEffect(isExpanded) {
                delay(index * 50L)
                itemVisible = isExpanded
            }
            AnimatedVisibility(
                visible = itemVisible,
                enter = fadeIn(animationSpec = tween(300, easing = EaseInOutQuad)) +
                        slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300, easing = EaseInOutQuad)),
                exit = fadeOut(animationSpec = tween(200)) +
                        slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(200))
            ) {
                val isSelected = selectedDestination == screen
                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    label = "nav_item_icon_scale_anim_${screen.route}"
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
                    modifier = Modifier
                        .padding(vertical = 6.dp)
                        .height(56.dp)
                        .minimumInteractiveComponentSize() // Accessibility: 48.dp touch target
                )
            }
            if (destinations.last() != screen) {
                Spacer(Modifier.height(6.dp))
            }
        }
        Spacer(Modifier.weight(1f))
    }
}
/**
 * Login Screen with animated Card and input fields for username and password.
 * The login process is simulated with a delay to mimic server authentication.
 * @param onLoginSuccess Callback invoked with the username when login is successful.
 * @param soundEffectManager The SoundEffectManager for playing UI sounds.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    soundEffectManager: SoundEffectManager
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .widthIn(max = 480.dp)
                .padding(24.dp)
                .graphicsLayer {
                    val scale by animateFloatAsState(
                        targetValue = 1.0f,
                        animationSpec = tween(600, easing = EaseInOutBack),
                        label = "login_card_scale"
                    )
                    val rotation by animateFloatAsState(
                        targetValue = 0f,
                        animationSpec = tween(600, delayMillis = 200, easing = EaseInOutBack),
                        label = "login_card_rotation"
                    )
                    scaleX = scale
                    scaleY = scale
                    rotationY = rotation
                }
                .semantics { contentDescription = "Login card" } // Accessibility
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    leadingIcon = { Icon(Icons.Outlined.AccountCircle, contentDescription = "Username Icon") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Username input" } // Accessibility
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "Password Icon") },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (showPassword) "Hide Password" else "Show Password"
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (!isLoading) {
                                soundEffectManager.playClickSound()
                                focusManager.clearFocus()
                                coroutineScope.launch {
                                    isLoading = true
                                    delay(1500)
                                    if (username.isNotBlank() && password.isNotBlank()) {
                                        onLoginSuccess(username)
                                    } else {
                                        errorMessage = if (username.isBlank()) {
                                            "Please enter a username."
                                        } else {
                                            "Please enter a password."
                                        }
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    isLoading = false
                                }
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
                        unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Password input" } // Accessibility
                )

                AnimatedVisibility(
                    visible = errorMessage.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                AnimatedButton(
                    text = "LOGIN",
                    onClick = {
                        if (!isLoading) {
                            focusManager.clearFocus()
                            coroutineScope.launch {
                                isLoading = true
                                delay(1500)
                                if (username.isNotBlank() && password.isNotBlank()) {
                                    onLoginSuccess(username)
                                } else {
                                    errorMessage = if (username.isBlank()) {
                                        "Please enter a username."
                                    } else {
                                        "Please enter a password."
                                    }
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    enabled = !isLoading,
                    isLoading = isLoading,
                    soundEffectManager = soundEffectManager
                )

                if (!isConnected(context)) {
                    AnimatedButton(
                        text = "Connect to Wi-Fi",
                        onClick = { openWifiSettings(context) },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Filled.Info,
                        soundEffectManager = soundEffectManager
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    username: String,
    onLogout: () -> Unit,
    soundEffectManager: SoundEffectManager
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Card(
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "Profile Icon",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Text(
            text = username,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Active User",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        ProfileOptionItem(
            title = "Account Details",
            icon = Icons.Filled.AccountBox,
            onClick = { /* Navigate to Account Details */ },
            soundEffectManager = soundEffectManager
        )
        ProfileOptionItem(
            title = "Privacy Policy",
            icon = Icons.Filled.Policy,
            onClick = { /* Navigate to Privacy Policy */ },
            soundEffectManager = soundEffectManager
        )
        ProfileOptionItem(
            title = "Usage History",
            icon = Icons.Filled.HistoryEdu,
            onClick = { /* Navigate to Usage History */ },
            soundEffectManager = soundEffectManager
        )

        Spacer(Modifier.weight(1f))

        AnimatedButton(
            text = "Logout",
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(bottom = 32.dp),
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            soundEffectManager = soundEffectManager,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        )
    }
}

@Composable
fun ProfileOptionItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    soundEffectManager: SoundEffectManager
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val defaultIndication = LocalIndication.current
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "profile_option_scale_anim_$title"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "profile_option_alpha_anim_$title"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
            .clickable(
                interactionSource = interactionSource,
                indication = defaultIndication,
                onClick = {
                    soundEffectManager.playClickSound()
                    onClick()
                }
            )
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .minimumInteractiveComponentSize(), // Accessibility: 48.dp touch target
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = "Navigate to $title",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    soundEffectManager: SoundEffectManager,
    sharedPrefsManager: SharedPreferencesManager
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var themeSetting by remember { mutableStateOf(sharedPrefsManager.getThemeSetting()) }
    var isSoundEnabled by remember { mutableStateOf(sharedPrefsManager.isSoundEnabled()) }
    var showThemeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 8.dp)
        )
        SettingItem(
            title = "Theme",
            icon = Icons.Filled.ColorLens,
            description = themeSetting.name.lowercase().replaceFirstChar { it.uppercase() },
            onClick = { showThemeDialog = true },
            soundEffectManager = soundEffectManager
        )
        SettingItem(
            title = "Sound Effects",
            icon = if (isSoundEnabled) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
            description = if (isSoundEnabled) "Enabled" else "Disabled",
            onClick = {
                isSoundEnabled = !isSoundEnabled
                sharedPrefsManager.setSoundEnabled(isSoundEnabled)
            },
            soundEffectManager = soundEffectManager
        )

        if (showThemeDialog) {
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                title = { Text("Select Theme") },
                text = {
                    Column {
                        ThemeSetting.entries.forEach { theme ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        themeSetting = theme
                                        sharedPrefsManager.setThemeSetting(theme)
                                        showThemeDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = themeSetting == theme,
                                    onClick = {
                                        themeSetting = theme
                                        sharedPrefsManager.setThemeSetting(theme)
                                        showThemeDialog = false
                                    }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = theme.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showThemeDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
fun SettingItem(
    title: String,
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    soundEffectManager: SoundEffectManager
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val defaultIndication = LocalIndication.current
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "setting_item_scale_anim_$title"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "setting_item_alpha_anim_$title"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
            .clickable(
                interactionSource = interactionSource,
                indication = defaultIndication,
                onClick = {
                    soundEffectManager.playClickSound()
                    onClick()
                }
            )
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .minimumInteractiveComponentSize(), // Accessibility: 48.dp touch target
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Filled.ArrowDropDown,
            contentDescription = "Expand $title",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun AnimatedCardGrid(
    searchQuery: String,
    onCardClick: (String) -> Unit,
    soundEffectManager: SoundEffectManager
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val cards = remember {
        listOf(
            "System Config",
            "Module One",
            "Module Two",
            "Module Three",
            "Module Four",
            "Module Five",
            "Module Six",
            "Module Seven",
            "Module Eight"
        )
    }
    val filteredCards by remember(cards, searchQuery) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                cards
            } else {
                cards.filter { it.contains(searchQuery, ignoreCase = true) }
            }
        }
    }
    val icons = listOf(
        painterResource(id = R.mipmap.ic_launcher_round),
        painterResource(id = R.mipmap.ic_launcher_round),
        painterResource(id = R.mipmap.ic_launcher_round)
    )

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(filteredCards, key = { _, card -> card }) { index, title ->
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            var isVisible by remember { mutableStateOf(false) }
            val animatedAlpha by animateFloatAsState(
                targetValue = if (isVisible) 1f else 0f,
                animationSpec = tween(
                    durationMillis = 400,
                    delayMillis = index * 100,
                    easing = LinearOutSlowInEasing
                ),
                label = "card_alpha_anim_$title"
            )
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.95f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                label = "card_scale_anim_$title"
            )
            val pressAlpha by animateFloatAsState(
                targetValue = if (isPressed) 0.9f else 1.0f,
                animationSpec = tween(durationMillis = 150),
                label = "card_press_alpha_anim_$title"
            )

            LaunchedEffect(Unit) {
                isVisible = true
            }

            GradientCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .then(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Modifier.blur(2.dp) else Modifier),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCardClick(title)
                }
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
                        contentDescription = "Icon for $title module",
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

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    soundEffectManager: SoundEffectManager
) {
    val pageCount = 3
    var currentPage by remember { mutableStateOf(0) }
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        HorizontalPager(state = pagerState) { page ->
            when (page) {
                0 -> OnboardingPage("Welcome!", "Explore the app's features.", R.mipmap.ic_launcher_round)
                1 -> OnboardingPage("Secure Login", "Your data is protected.", R.mipmap.ic_launcher_round)
                2 -> OnboardingPage("Get Started", "Dive into the experience!", R.mipmap.ic_launcher_round)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (currentPage > 0) {
                AnimatedButton(
                    text = "Previous",
                    onClick = { currentPage-- },
                    soundEffectManager = soundEffectManager
                )
            }
            AnimatedButton(
                text = if (currentPage == pageCount - 1) "Finish" else "Next",
                onClick = {
                    if (currentPage < pageCount - 1) {
                        currentPage++
                    } else {
                        onComplete()
                    }
                },
                soundEffectManager = soundEffectManager
            )
        }
    }
}

@Composable
fun OnboardingPage(
    title: String,
    description: String,
    iconResId: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = title,
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}
