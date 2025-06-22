package com.ktimazstudio

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
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
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
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
import com.ktimazstudio.ui.theme.ktimaz // Assuming this theme exists
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.security.MessageDigest
import kotlin.experimental.and
import androidx.compose.foundation.border

// --- SharedPreferencesManager ---
/**
 * Manages user login status and username using SharedPreferences for persistent storage.
 * This class provides a simple way to store and retrieve whether a user is logged in
 * and their username.
 */
class SharedPreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("AppPrefsKtimazStudio", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in_key"
        private const val KEY_USERNAME = "username_key"
        private const val KEY_STRICT_MODE = "strict_mode_key"
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
     * Retrieves the strict mode setting.
     * return true if strict mode is enabled, false otherwise.
     */
    fun isStrictModeEnabled(): Boolean {
        return prefs.getBoolean(KEY_STRICT_MODE, false)
    }

    /**
     * Sets the strict mode setting.
     * @param enabled The new strict mode status.
     */
    fun setStrictMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_STRICT_MODE, enabled).apply()
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

    // Known good hash of the APK's signing certificate (SHA-256)
    // --- IMPORTANT: UPDATE THIS HASH TO YOUR APP'S RELEASE SIGNATURE SHA-256 HASH ---
    // The user provided this in a previous turn.
    private val EXPECTED_APK_HASH = "f21317d4d6276ff3174a363c7fdff4171c73b1b80a82bb9082943ea9200a8425".lowercase()

    /**
     * Checks if a debugger is currently attached to the application process.
     * This now combines Android's built-in check with a more robust procfs check for TracerPid.
     * return true if a debugger is connected, false otherwise.
     */
    fun isDebuggerConnected(): Boolean {
        return Debug.isDebuggerConnected() || isTracerAttached()
    }

    /**
     * Checks if a TracerPid is attached, which indicates a debugger or ptrace tool.
     * return true if TracerPid is non-zero, false otherwise.
     */
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
            // Log.e("SecurityManager", "Error checking TracerPid: ${e.message}")
        }
        return false
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
                || "google_sdk" == Build.PRODUCT
                || Build.HARDWARE.contains("goldfish") // Older emulators
                || Build.HARDWARE.contains("vbox") // VirtualBox (Genymotion)
                || Build.HOST == "BuildHost") // Common emulator build host
    }

    /**
     * Attempts to detect if the device is rooted.
     * This check is not exhaustive and can be bypassed.
     * return true if root is likely detected, false otherwise.
     */
    fun isDeviceRooted(): Boolean {
        // Paths to common su binaries
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
            "/su/bin/su",
            "/system/bin/daemonsu", // Magisk su
            "/system/etc/init.d/99SuperSUDaemon", // SuperSU init script
            "/data/local/.daemon",
            "/data/local/.xbin",
            "/system/bin/.ext",
            "/system/usr/we-need-root",
            "/system/app/Kinguser.apk",
            "/data/data/com.kingroot.kinguser",
            "/system/app/Kingroot.apk"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }

        // Check for test-keys in build tags (common for custom ROMs/rooted devices)
        if (Build.TAGS != null && Build.TAGS.contains("test-keys")) {
            return true
        }

        // Check for common root package names
        val rootPackages = listOf(
            "com.noshufou.android.su",
            "com.koushikdutta.superuser",
            "eu.chainfire.supersu",
            "com.thirdparty.superuser",
            "com.topjohnwu.magisk"
        )
        for (pkg in rootPackages) {
            try {
                context.packageManager.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES)
                return true
            } catch (e: PackageManager.NameNotFoundException) {
                // Package not found, which is good
            }
        }

        // Check if `su` command can be executed
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            if (reader.readLine() != null) return true
        } catch (e: Exception) {
            // Command not found or other error, likely not rooted
        } finally {
            process?.destroy()
        }

        return false
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
                // When network capabilities change, re-check VPN status
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
     * Attempts to detect common hooking frameworks (like Xposed or Frida) by checking
     * for known files, installed packages, or system properties.
     * This is not exhaustive and can be bypassed, but adds a layer of defense.
     * return true if a hooking framework is likely detected, false otherwise.
     */
    fun isHookingFrameworkDetected(): Boolean {
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
            "/system/xbin/magisk",
            "/system/lib/libfrida-gadget.so", // Frida gadget
            "/data/local/re.frida.server", // Another common Frida server path
            // "/proc/self/maps" // Can check for specific libraries loaded, but more complex and potentially slow
        )
        for (path in knownHookFiles) {
            if (File(path).exists()) return true
        }

        // 2. Check for common system properties (related to Xposed, Magisk)
        val props = listOf(
            "xposed.active", "xposed.api_level", "xposed.installed", // Xposed
            "dalvik.vm.xposedenabled",
            "ro.build.selinux", // Can sometimes indicate permissive SELinux on rooted devices
            "magisk.su", "magisk.version" // Magisk
        )
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

        // 3. Check for common packages (Xposed installer, Magisk Manager)
        val hookPackages = listOf(
            "de.robv.android.xposed.installer",
            "com.topjohnwu.magisk"
        )
        for (pkg in hookPackages) {
            try {
                context.packageManager.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES)
                return true
            } catch (e: PackageManager.NameNotFoundException) {
                // Package not found, which is good
            } catch (e: Exception) {
                // Log.e("SecurityCheck", "Error checking hook installer package: ${e.message}")
            }
        }

        // 4. Check for suspicious environment variables (can indicate Frida)
        val envVars = System.getenv()
        if (envVars.containsKey("LD_PRELOAD") || envVars.containsKey("DA_DEBUG_MONITOR")) {
            // LD_PRELOAD is commonly used for injecting shared libraries
            // DA_DEBUG_MONITOR is sometimes set by dynamic analysis tools
            return true
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
    fun isApkTampered(): Boolean {
        val currentSignatureHash = getSignatureSha256Hash()
        // Compare with the signature SHA-256 hash provided by you.
        return currentSignatureHash != null && currentSignatureHash.lowercase() != EXPECTED_APK_HASH.lowercase()
    }

    /**
     * Gets the size of the installed application (APK + data).
     * This can be used as a very basic indicator of tampering if the size changes unexpectedly.
     * return The app size in bytes, or -1 if unable to retrieve.
     */
    fun getAppSize(): Long {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            // Safely access applicationInfo.sourceDir as applicationInfo can be null
            val apkPath = packageInfo.applicationInfo?.sourceDir ?: return -1L
            val file = File(apkPath)
            return file.length()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return -1L
    }

    /**
     * Aggregates all security checks to determine if the app environment is secure.
     * return A SecurityIssue enum indicating the first detected issue, or SecurityIssue.NONE if secure.
     */
    fun getSecurityIssue(): SecurityIssue {
        if (isDebuggerConnected()) return SecurityIssue.DEBUGGER_ATTACHED
        if (isRunningOnEmulator()) return SecurityIssue.EMULATOR_DETECTED
        if (isDeviceRooted()) return SecurityIssue.ROOT_DETECTED
       // if (isHookingFrameworkDetected()) return SecurityIssue.HOOKING_FRAMEWORK_DETECTED
       // if (isApkTampered()) return SecurityIssue.APK_TAMPERED
        if (isVpnActive()) return SecurityIssue.VPN_ACTIVE
        // Add other checks here as needed
        return SecurityIssue.NONE
    }

    enum class SecurityIssue {
        NONE, DEBUGGER_ATTACHED, EMULATOR_DETECTED, ROOT_DETECTED, HOOKING_FRAMEWORK_DETECTED, APK_TAMPERED, VPN_ACTIVE
    }
}

// --- Navigation Destinations ---
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Dashboard)
    object AppSettings : Screen("settings", "Settings", Icons.Filled.Settings)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
    object SecurityInfo : Screen("security_info", "Security Info", Icons.Filled.Lock) // New
    object About : Screen("about", "About", Icons.Filled.Info) // New
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
class MainActivity : ComponentActivity() {
    private lateinit var sharedPrefsManager: SharedPreferencesManager
    private lateinit var securityManager: SecurityManager
    private var vpnNetworkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        sharedPrefsManager = SharedPreferencesManager(applicationContext)
        securityManager = SecurityManager(applicationContext)

        // Perform initial security checks
        val initialSecurityIssue = securityManager.getSecurityIssue()
        if (initialSecurityIssue != SecurityManager.SecurityIssue.NONE) {
            setContent {
                ktimaz {
                    SecurityWarningDialog(issue = initialSecurityIssue) { finishAffinity() }
                }
            }
            return // Stop further app initialization if a critical issue is found
        }

        setContent {
            // Professional UI Design: Using a more modern and cohesive color scheme
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF6200EE), // Deep Indigo
                    onPrimary = Color(0xFFFFFFFF),
                    primaryContainer = Color(0xFFEDE7F6), // Light Indigo
                    onPrimaryContainer = Color(0xFF21005D),
                    secondary = Color(0xFF03DAC6), // Teal
                    onSecondary = Color(0xFF000000),
                    secondaryContainer = Color(0xFFCCF8F5), // Light Teal
                    onSecondaryContainer = Color(0xFF00201D),
                    tertiary = Color(0xFF3F51B5), // Medium Indigo
                    onTertiary = Color(0xFFFFFFFF),
                    tertiaryContainer = Color(0xFFE0E0F8), // Lighter Medium Indigo
                    onTertiaryContainer = Color(0xFF1A237E),
                    error = Color(0xFFB00020), // Red
                    onError = Color(0xFFFFFFFF),
                    errorContainer = Color(0xFFFFDAD6),
                    onErrorContainer = Color(0xFF410002),
                    background = Color(0xFFFFFFFF),
                    onBackground = Color(0xFF1C1B1F),
                    surface = Color(0xFFFFFFFF),
                    onSurface = Color(0xFF1C1B1F),
                    surfaceVariant = Color(0xFFE7E0EC),
                    onSurfaceVariant = Color(0xFF49454F),
                    outline = Color(0xFF7A757F),
                    inverseOnSurface = Color(0xFFF4EFF4),
                    inverseSurface = Color(0xFF313034),
                    inversePrimary = Color(0xFFD0BCFF),
                    surfaceDim = Color(0xFFDDD8DE),
                    surfaceBright = Color(0xFFFCF8FF),
                    surfaceContainerLowest = Color(0xFFFFFFFF),
                    surfaceContainerLow = Color(0xFFF7F2F7),
                    surfaceContainer = Color(0xFFF1EDF1),
                    surfaceContainerHigh = Color(0xFFECE7EC),
                    surfaceContainerHighest = Color(0xFFE6E2E6)
                )
            ) {
                // Set system bar colors for Material 3 look
                SetSystemBarsLightStyle(backgroundColor = MaterialTheme.colorScheme.background)

                var isLoggedIn by remember { mutableStateOf(sharedPrefsManager.isLoggedIn()) }
                var currentUsername by remember(isLoggedIn) { mutableStateOf(sharedPrefsManager.getUsername()) }
                var liveVpnDetected by remember { mutableStateOf(securityManager.isVpnActive()) }
                var currentSecurityIssue by remember { mutableStateOf(SecurityManager.SecurityIssue.NONE) }

                // Live VPN detection
                DisposableEffect(Unit) {
                    vpnNetworkCallback = securityManager.registerVpnDetectionCallback { isVpn ->
                        liveVpnDetected = isVpn
                        // If VPN is detected, set it as the current issue.
                        // Otherwise, re-evaluate all security issues.
                        if (isVpn) {
                            currentSecurityIssue = SecurityManager.SecurityIssue.VPN_ACTIVE
                        } else {
                            currentSecurityIssue = securityManager.getSecurityIssue()
                        }
                    }
                    onDispose {
                        vpnNetworkCallback?.let { securityManager.unregisterVpnDetectionCallback(it) }
                    }
                }

                // Periodic security checks for other issues (debugger, root, emulator, tampering, hooking)
                // This runs every 5 seconds to catch issues that might appear after app launch.
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(5000) // Check every 5 seconds
                        val issue = securityManager.getSecurityIssue()
                        // Only update if a new issue is found, or if the current issue was VPN and it's now gone.
                        if (issue != SecurityManager.SecurityIssue.NONE && issue != currentSecurityIssue) {
                            currentSecurityIssue = issue
                        } else if (currentSecurityIssue == SecurityManager.SecurityIssue.VPN_ACTIVE && issue == SecurityManager.SecurityIssue.NONE) {
                            // If VPN was active and now no issue is found, clear the alert
                            currentSecurityIssue = SecurityManager.SecurityIssue.NONE
                        }
                    }
                }

                // Observe currentSecurityIssue and display alert if needed
                if (currentSecurityIssue != SecurityManager.SecurityIssue.NONE) {
                    SecurityWarningDialog(issue = currentSecurityIssue) { finishAffinity() }
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
                                sharedPrefsManager = sharedPrefsManager,
                                securityManager = securityManager,
                                username = currentUsername ?: "User",
                                onLogout = { sharedPrefsManager.setLoggedIn(false); isLoggedIn = false }
                            )
                        } else {
                            LoginScreen(
                                onLoginSuccess = { loggedInUsername -> sharedPrefsManager.setLoggedIn(true, loggedInUsername); isLoggedIn = true }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        vpnNetworkCallback?.let { securityManager.unregisterVpnDetectionCallback(it) }
    }
}

/**
 * A generic security alert dialog displayed when a critical security issue is detected.
 * This dialog is non-dismissible, forcing the user to exit the application.
 * @param issue The SecurityIssue enum indicating the reason for the alert.
 * @param onConfirm Callback to be invoked when the "Understand and Exit" button is clicked.
 */
@Composable
fun SecurityWarningDialog(issue: SecurityManager.SecurityIssue, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /* Cannot dismiss, must confirm */ },
        icon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Security Alert!") },
        text = {
            Column {
                Text("A potential security risk has been detected:")
                Spacer(modifier = Modifier.height(8.dp))
                when (issue) {
                    SecurityManager.SecurityIssue.DEBUGGER_ATTACHED -> Text("• Debugger Attached: The app is being debugged.")
                    SecurityManager.SecurityIssue.EMULATOR_DETECTED -> Text("• Emulator Detected: The app is running on an emulator.")
                    SecurityManager.SecurityIssue.ROOT_DETECTED -> Text("• Root Detected: The device appears to be rooted.")
                    SecurityManager.SecurityIssue.HOOKING_FRAMEWORK_DETECTED -> Text("• Hooking Framework Detected: Suspicious tools like Xposed or Frida might be present.")
                    SecurityManager.SecurityIssue.APK_TAMPERED -> Text("• APK Tampered: The app's integrity has been compromised.")
                    SecurityManager.SecurityIssue.VPN_ACTIVE -> Text("• VPN Active: A VPN connection is active.")
                    SecurityManager.SecurityIssue.NONE -> Text("• Unknown Issue.") // Should not happen
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("For your security, the application will now close.", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Understand and Exit")
            }
        },
        containerColor = MaterialTheme.colorScheme.errorContainer,
        titleContentColor = MaterialTheme.colorScheme.onErrorContainer,
        textContentColor = MaterialTheme.colorScheme.onErrorContainer
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApplicationUI(
    sharedPrefsManager: SharedPreferencesManager,
    securityManager: SecurityManager,
    username: String,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedDestination by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    var isRailExpanded by remember { mutableStateOf(false) }

    // Check for internet connectivity on app launch
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
                            text = "Ktimaz Studio", // Replaced stringResource with hardcoded string for simplicity
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
                    fadeIn(animationSpec = tween(300, easing = LinearOutSlowInEasing)) + slideInHorizontally(initialOffsetX = { if (initialState.route == Screen.Dashboard.route) 300 else -300 }, animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300, easing = FastOutLinearInEasing)) + slideOutHorizontally(targetOffsetX = { if (targetState.route == Screen.Dashboard.route) -300 else 300 }, animationSpec = tween(300))
                },
                label = "nav_rail_content_transition"
            ) { targetDestination ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    when (targetDestination) {
                        Screen.Dashboard -> AnimatedCardGrid { title ->
                            if (title == "System Config") {
                                // context.startActivity(Intent(context, SettingsActivity::class.java)) // Assuming SettingsActivity exists
                                Toast.makeText(context, "Navigating to System Config (Placeholder)", Toast.LENGTH_SHORT).show()
                            } else {
                                // context.startActivity(Intent(context, ComingActivity::class.java).putExtra("CARD_TITLE", title)) // Assuming ComingActivity exists
                                Toast.makeText(context, "Navigating to ${title} (Placeholder)", Toast.LENGTH_SHORT).show()
                            }
                        }
                        Screen.AppSettings -> SettingsScreen(sharedPrefsManager)
                        Screen.Profile -> ProfileScreen(username = username, onLogout = onLogout)
                        Screen.SecurityInfo -> SecurityInformationScreen(securityManager) // New
                        Screen.About -> AboutScreen() // New
                    }
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
    onMenuClick: () -> Unit
) {
    NavigationRail(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
    ) {
        NavigationRailItem(
            selected = false,
            onClick = onMenuClick,
            icon = {
                Icon(
                    imageVector = if (isExpanded) Icons.AutoMirrored.Filled.MenuOpen else Icons.Filled.Menu,
                    contentDescription = "Menu"
                )
            },
            label = { Text("Menu") }
        )
        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val destinations = listOf(Screen.Dashboard, Screen.SecurityInfo, Screen.AppSettings, Screen.Profile, Screen.About) // Added SecurityInfo and About
            destinations.forEach { screen ->
                NavigationRailItem(
                    selected = selectedDestination == screen,
                    onClick = { onDestinationSelected(screen) },
                    icon = { Icon(screen.icon, contentDescription = screen.label) },
                    label = {
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = fadeIn() + slideInHorizontally(initialOffsetX = { it / 2 }),
                            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it / 2 })
                        ) {
                            Text(screen.label)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(sharedPrefsManager: SharedPreferencesManager) {
    val context = LocalContext.current
    val isStrictMode by remember { mutableStateOf(sharedPrefsManager.isStrictModeEnabled()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "Application Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Strict Mode Setting
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    sharedPrefsManager.setStrictMode(!isStrictMode)
                    Toast.makeText(context, "Strict Mode Toggled", Toast.LENGTH_SHORT).show()
                }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Enable Strict Security Mode", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = isStrictMode,
                onCheckedChange = {
                    sharedPrefsManager.setStrictMode(it)
                    Toast.makeText(context, "Strict Mode ${if (it) "Enabled" else "Disabled"}", Toast.LENGTH_SHORT).show()
                }
            )
        }
        Divider()

        // Upgrade Setting
        SettingItem(
            title = "Check for Updates",
            description = "Check if a new version of the application is available.",
            icon = Icons.Filled.HistoryEdu
        ) {
            Toast.makeText(context, "Checking for updates...", Toast.LENGTH_SHORT).show()
            // In a real app, this would trigger an update check (e.g., to a backend or app store)
        }
        Divider()

        SettingItem(
            title = "About App",
            description = "Learn more about this application.",
            icon = Icons.Filled.Info
        ) {
            Toast.makeText(context, "Navigating to About screen (placeholder)", Toast.LENGTH_SHORT).show()
            // This could navigate to a detailed About screen
        }
        Divider()
    }
}

@Composable
fun SettingItem(title: String, description: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = "Go",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProfileScreen(username: String, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = "Profile Picture",
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Welcome, $username!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(0.8f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Logout", color = MaterialTheme.colorScheme.onError)
        }
    }
}

@Composable
fun SecurityInformationScreen(securityManager: SecurityManager) {
    val isStrictMode = remember { mutableStateOf(false) } // This should ideally come from SharedPreferencesManager
    val context = LocalContext.current
    val sharedPrefsManager = remember { SharedPreferencesManager(context) }
    isStrictMode.value = sharedPrefsManager.isStrictModeEnabled()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "Security Information",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Strict mode toggle for demonstration, though it's in SettingsScreen
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Strict Mode Enabled", style = MaterialTheme.typography.bodyLarge)
            Switch(checked = isStrictMode.value, onCheckedChange = {
                isStrictMode.value = it
                sharedPrefsManager.setStrictMode(it)
                Toast.makeText(context, "Strict Mode ${if (it) "Enabled" else "Disabled"}", Toast.LENGTH_SHORT).show()
            })
        }
        Divider()

        Text(
            "Runtime Checks",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        SecurityCheckItem("Debugger Attached", securityManager.isDebuggerConnected())
        SecurityCheckItem("Running on Emulator", securityManager.isRunningOnEmulator())
        SecurityCheckItem("Device is Rooted", securityManager.isDeviceRooted())
        SecurityCheckItem("VPN is Active", securityManager.isVpnActive())
        
        // Strict mode checks
        Text(
            "Strict Mode Checks (${if (isStrictMode.value) "ON" else "OFF"})",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        SecurityCheckItem("Hooking Framework Detected", if(isStrictMode.value) securityManager.isHookingFrameworkDetected() else false)
        SecurityCheckItem("APK Integrity Compromised", if(isStrictMode.value) securityManager.isApkTampered() else false)
    }
}

@Composable
fun SecurityCheckItem(title: String, isDetected: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Icon(
            imageVector = if (isDetected) Icons.Filled.Warning else Icons.Filled.CheckCircle,
            contentDescription = if (isDetected) "Detected" else "OK",
            tint = if (isDetected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = "About App",
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Ktimaz Studio App",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            "Version 1.0.0",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "This application provides various features including a secure login, " +
                    "system configuration options, and real-time security checks. " +
                    "It is designed with user privacy and security in mind.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "© 2025 Ktimaz Studio. All rights reserved.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


/**
 * Composable for the enhanced Login Screen.
 * Features a more professional UI/UX with gradients, animations, and improved error handling.
 * @param onLoginSuccess Callback invoked on successful login, providing the username.
 */
@Composable
fun LoginScreen(onLoginSuccess: (username: String) -> Unit) {
    var usernameInput by rememberSaveable { mutableStateOf("") }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    val sharedPrefsManager = remember { SharedPreferencesManager(context) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                )
            )
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "App Logo",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Welcome Back!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Sign in to continue to Ktimaz Studio",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = { usernameInput = it },
                    label = { Text("Username") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Username Icon") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Password Icon") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) "Hide password" else "Show password"
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                errorMessage?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = {
                        // Simple validation for demonstration
                        if (usernameInput == "user" && passwordInput == "pass") {
                            onLoginSuccess(usernameInput)
                            errorMessage = null
                        } else {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            errorMessage = "Invalid username or password."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Login", fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimary)
                }

                TextButton(onClick = { Toast.makeText(context, "Forgot Password Clicked", Toast.LENGTH_SHORT).show() }) {
                    Text("Forgot Password?", color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

/**
 * Animated grid of cards for the dashboard.
 */
@Composable
fun AnimatedCardGrid(onCardClick: (String) -> Unit) {
    val items = remember {
        listOf(
            "System Config", "User Management", "Data Analytics", "Notifications",
            "Report Generator", "Backup & Restore", "Security Logs", "Help & Support"
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items) { item ->
            var scale by remember { mutableStateOf(1f) }
            val animatedScale by animateFloatAsState(
                targetValue = scale,
                animationSpec = tween(durationMillis = 150)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Makes cards square
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    }
                    .clickable { onCardClick(item) }
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.surface
                            ),
                            radius = 200f
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        RoundedCornerShape(16.dp)
                    )
                    .clip(RoundedCornerShape(16.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent) // Transparent to show background brush
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBox, // Placeholder icon
                        contentDescription = item,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}


/**
 * Sets the system bar colors (status bar and navigation bar) to match the app's theme.
 * This is a common practice for a modern Android UI.
 */
@Composable
fun SetSystemBarsLightStyle(backgroundColor: Color) {
    val systemUiController = rememberSystemUiController()
    SideEffect {
        // Set status bar color
        systemUiController.setStatusBarColor(
            color = backgroundColor,
            darkIcons = true // For light background, use dark icons
        )
        // Set navigation bar color
        systemUiController.setNavigationBarColor(
            color = backgroundColor,
            darkIcons = true // For light background, use dark icons
        )
    }
}

// Dummy for rememberSystemUiController if not available directly
@Composable
fun rememberSystemUiController(): SystemUiController {
    return remember { SystemUiControllerImpl() }
}

interface SystemUiController {
    fun setStatusBarColor(color: Color, darkIcons: Boolean)
    fun setNavigationBarColor(color: Color, darkIcons: Boolean)
}

class SystemUiControllerImpl : SystemUiController {
    override fun setStatusBarColor(color: Color, darkIcons: Boolean) {
        // Dummy implementation
    }

    override fun setNavigationBarColor(color: Color, darkIcons: Boolean) {
        // Dummy implementation
    }
}
