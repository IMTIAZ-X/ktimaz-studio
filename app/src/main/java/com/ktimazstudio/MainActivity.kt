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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.graphics.luminance // For determining icon color based on background
import androidx.compose.ui.graphics.toArgb
import android.app.Activity
import androidx.compose.ui.platform.LocalView
import java.io.File
import java.security.MessageDigest
import java.net.ServerSocket // Added import
import java.io.IOException // Added import
import kotlinx.coroutines.delay // Added import
import androidx.compose.ui.platform.LocalDensity // Added import
import androidx.compose.ui.graphics.vector.rememberVectorPainter // Added import


class MainActivity : ComponentActivity() {

    private lateinit var sharedPrefsManager: SharedPreferencesManager
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private lateinit var securityManager: SecurityManager

    private var vpnStatusListener: ((Boolean) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge() // Enable edge-to-edge content

        super.onCreate(savedInstanceState)

        sharedPrefsManager = SharedPreferencesManager(this)
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        securityManager = SecurityManager(this)

        // Initialize VPN detection
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                val isVpn = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                vpnStatusListener?.invoke(isVpn)
            }
        }
        securityManager.registerVpnDetectionCallback(connectivityManager, networkCallback)


        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme( // Or your darkColorScheme/dynamicColorScheme
                    primary = Color(0xFF673AB7), // Deep Purple
                    onPrimary = Color(0xFFFFFFFF),
                    primaryContainer = Color(0xFFEADDFF),
                    onPrimaryContainer = Color(0xFF21005D),
                    secondary = Color(0xFF7F4B00), // Darker Orange-Brown
                    onSecondary = Color(0xFFFFFFFF),
                    secondaryContainer = Color(0xFFFFDABF),
                    onSecondaryContainer = Color(0xFF281500),
                    tertiary = Color(0xFF6B50B0), // Lighter Purple
                    onTertiary = Color(0xFFFFFFFF),
                    tertiaryContainer = Color(0xFFEADDFF),
                    onTertiaryContainer = Color(0xFF26005B),
                    error = Color(0xFFBA1A1A),
                    onError = Color(0xFFFFFFFF),
                    errorContainer = Color(0xFFFFDAD6),
                    onErrorContainer = Color(0xFF410002),
                    background = Color(0xFFFFFBFE),
                    onBackground = Color(0xFF1C1B1F),
                    surface = Color(0xFFFFFBFE),
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

                // Check security issues on app launch
                val securityIssue = remember { securityManager.getSecurityIssue() }
                if (securityIssue != SecurityManager.SecurityIssue.NONE) {
                    SecurityWarningDialog(issue = securityIssue) {
                        finishAffinity() // Exit app if security issue is acknowledged
                    }
                } else {
                    if (sharedPrefsManager.isLoggedIn()) {
                        MainScreen(sharedPrefsManager, securityManager)
                    } else {
                        LoginScreen(sharedPrefsManager)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister VPN detection when activity is destroyed
        securityManager.unregisterVpnDetectionCallback(connectivityManager, networkCallback)
    }

    // Function to show a Toast message from Activity context
    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

// Composable to set system bar colors to a light style (dark icons)
@Composable
fun SetSystemBarsLightStyle(backgroundColor: Color) {
    val view = LocalView.current
    val window = (view.context as? Activity)?.window

    val useDarkIcons = backgroundColor.luminance() > 0.5f

    // Corrected DisposableEffect structure
    DisposableEffect(view, window, useDarkIcons, backgroundColor) {
        if (window == null) {
            // Return an empty DisposableEffectResult if window is null
            onDispose { /* Nothing to do if window is null */ }
            return@DisposableEffect
        }

        val insetsController = WindowCompat.getInsetsController(window, view)

        val originalStatusBarColor = window.statusBarColor
        val originalNavigationBarColor = window.navigationBarColor
        val originalIsAppearanceLightStatusBars = insetsController.isAppearanceLightStatusBars
        val originalIsAppearanceLightNavigationBars = insetsController.isAppearanceLightNavigationBars

        window.statusBarColor = backgroundColor.toArgb()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            insetsController.isAppearanceLightStatusBars = useDarkIcons
        } else {
            @Suppress("DEPRECATION")
            if (useDarkIcons) {
                window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
        }

        window.navigationBarColor = backgroundColor.toArgb()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            insetsController.isAppearanceLightNavigationBars = useDarkIcons
        } else {
            @Suppress("DEPRECATION")
            if (useDarkIcons) {
                window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else {
                window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
        }

        onDispose {
            window.statusBarColor = originalStatusBarColor
            window.navigationBarColor = originalNavigationBarColor
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insetsController.isAppearanceLightStatusBars = originalIsAppearanceLightStatusBars
                insetsController.isAppearanceLightNavigationBars = originalIsAppearanceLightNavigationBars
            } else {
                @Suppress("DEPRECATION")
                if (originalIsAppearanceLightStatusBars) {
                    window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
                if (originalIsAppearanceLightNavigationBars) {
                    window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                } else {
                    window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                }
            }
        }
    }
}


@Composable
fun SecurityWarningDialog(issue: SecurityManager.SecurityIssue, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /* Cannot dismiss, must confirm */ },
        icon = { Icon(Icons.Filled.Security, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
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

/**
 * Manages user login state and preferences using SharedPreferences.
 */
class SharedPreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "app_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in_key"
        private const val KEY_USERNAME = "username_key"
        private const val KEY_ANIMATE_SETTINGS_ITEMS = "animate_settings_items_key" // New key
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    fun setUsername(username: String) {
        prefs.edit().putString(KEY_USERNAME, username).apply()
    }

    fun clearLogin() {
        prefs.edit().remove(KEY_IS_LOGGED_IN).remove(KEY_USERNAME).apply()
    }

    /**
     * Checks if settings items animation is enabled.
     * @return true if enabled, false otherwise (default to true for initial experience).
     */
    fun getAnimateSettingsItems(): Boolean {
        return prefs.getBoolean(KEY_ANIMATE_SETTINGS_ITEMS, true) // Default true
    }

    /**
     * Sets whether settings items animation is enabled.
     * @param enabled The new status for the animation.
     */
    fun setAnimateSettingsItems(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANIMATE_SETTINGS_ITEMS, enabled).apply()
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

    // IMPORTANT: REPLACE THIS WITH THE ACTUAL SHA-256 HASH OF YOUR RELEASE SIGNING CERTIFICATE!
    // You can get this using `keytool -list -v -keystore your_keystore.jks`
    // Look for "Certificate SHA256"
    private val EXPECTED_APK_HASH = "f21317d4d6276ff3174a363c77fdff4171c73b1b80a82bb9082943ea9200a8425".lowercase() // Example, replace this

    private var vpnStatusListener: ((Boolean) -> Unit)? = null

    /**
     * Registers a callback to listen for VPN status changes.
     * @param connectivityManager The system ConnectivityManager.
     * @param networkCallback The callback to register.
     */
    fun registerVpnDetectionCallback(connectivityManager: ConnectivityManager, networkCallback: ConnectivityManager.NetworkCallback) {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    /**
     * Unregisters the VPN status callback.
     * @param connectivityManager The system ConnectivityManager.
     * @param networkCallback The callback to unregister.
     */
    fun unregisterVpnDetectionCallback(connectivityManager: ConnectivityManager, networkCallback: ConnectivityManager.NetworkCallback) {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    /**
     * Checks if a VPN connection is currently active.
     * @return true if a VPN is active, false otherwise.
     */
    fun isVpnActive(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
            return capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
        } else {
            // For older Android versions, less reliable check
            @Suppress("DEPRECATION")
            val activeNetworkInfo = cm.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting && activeNetworkInfo.type == ConnectivityManager.TYPE_VPN
        }
    }


    /**
     * Checks if a debugger is currently attached to the application process.
     * This combines Android's built-in check with a procfs check for TracerPid.
     * @return true if a debugger is connected, false otherwise.
     */
    fun isDebuggerConnected(): Boolean {
        // Built-in Android check
        if (Debug.isDebuggerConnected()) return true

        // Check TracerPid for more robust detection against native debuggers
        if (isTracerAttached()) return true

        // Check common debug port (optional, might require network permissions)
        // if (checkDebugPort()) return true // This can be noisy or require permissions

        return false
    }

    /**
     * Checks the TracerPid in /proc/self/status to detect if a process is being traced (debugged).
     * @return true if TracerPid is non-zero, false otherwise.
     */
    fun isTracerAttached(): Boolean {
        try {
            val statusFile = File("/proc/self/status")
            if (statusFile.exists()) {
                statusFile.bufferedReader().useLines { lines ->
                    val tracerPidLine = lines.firstOrNull { it.startsWith("TracerPid:") }
                    if (tracerPidLine != null) {
                        val pid = tracerPidLine.substringAfter("TracerPid:").trim().toInt()
                        return pid != 0 // TracerPid 0 means no debugger
                    }
                }
            }
        } catch (e: Exception) {
            // Log.e("SecurityManager", "Error checking TracerPid: ${e.message}")
            // Consider this as non-detection rather than an issue
        }
        return false
    }

    /**
     * Attempts to detect if common debug ports are open, which might indicate
     * a debugger or analysis tool.
     * NOTE: This check might require INTERNET permission and can have false positives.
     * It's often more suitable for a deeper background check, not a quick app launch.
     * @return true if a suspicious debug port is open, false otherwise.
     */
    private fun checkDebugPort(): Boolean {
        // Common debug ports used by ADB, etc.
        val debugPorts = intArrayOf(23946, 8000, 8001, 8080) // Example ports, add/remove as needed
        for (port in debugPorts) {
            try {
                ServerSocket(port).close() // If we can open and close it, port wasn't in use
            } catch (e: IOException) {
                // If opening fails, port is likely in use.
                // Could be a debugger or another app.
                return true
            }
        }
        return false
    }


    /**
     * Attempts to detect if the application is running on an emulator.
     * This check is not exhaustive and can be bypassed.
     * @return true if an emulator is likely detected, false otherwise.
     */
    fun isRunningOnEmulator(): Boolean {
        // More comprehensive emulator checks
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
     * @return true if root is likely detected, false otherwise.
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
     * Retrieves the SHA-256 hash of the app's signing certificate.
     * @return The SHA-256 hash as a hex string, or null if an error occurs.
     */
    fun getSignatureSha256Hash(): String? {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
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
                return hashBytes.joinToString("") { "%02x".format(it) }
            }
        } catch (e: Exception) {
            // Log.e("SecurityManager", "Error getting signature hash: ${e.message}")
        }
        return null
    }

    /**
     * Checks if the APK's *signature hash* matches the expected hash.
     * This is now the primary integrity check.
     * @return true if the signature hash matches, false otherwise.
     */
    fun isApkTampered(): Boolean {
        val currentSignatureHash = getSignatureSha256Hash()
        // Compare with the signature SHA-256 hash provided by you.
        return currentSignatureHash != null && currentSignatureHash.lowercase() != EXPECTED_APK_HASH.lowercase()
    }


    /**
     * Attempts to detect common hooking frameworks (like Xposed or Frida) by checking
     * for known files, installed packages, system properties, and environment variables.
     * This is not exhaustive and can be bypassed, but adds a layer of defense.
     * @return true if a hooking framework is likely detected, false otherwise.
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
     * Aggregates all security checks to determine if the app environment is secure.
     * @return A SecurityIssue enum indicating the first detected issue, or SecurityIssue.NONE if secure.
     */
    fun getSecurityIssue(): SecurityIssue {
        if (isDebuggerConnected()) return SecurityIssue.DEBUGGER_ATTACHED
        if (isRunningOnEmulator()) return SecurityIssue.EMULATOR_DETECTED
        if (isDeviceRooted()) return SecurityIssue.ROOT_DETECTED
        if (isHookingFrameworkDetected()) return SecurityIssue.HOOKING_FRAMEWORK_DETECTED
        if (isApkTampered()) return SecurityIssue.APK_TAMPERED
        if (isVpnActive()) return SecurityIssue.VPN_ACTIVE
        // Add other checks here as needed
        return SecurityIssue.NONE
    }

    enum class SecurityIssue {
        NONE, DEBUGGER_ATTACHED, EMULATOR_DETECTED, ROOT_DETECTED, HOOKING_FRAMEWORK_DETECTED, APK_TAMPERED, VPN_ACTIVE
    }
}

/**
 * Main composable for the application, handling navigation and different screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(sharedPrefsManager: SharedPreferencesManager, securityManager: SecurityManager) {
    val context = LocalContext.current
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf("Home", "Profile", "Settings", "Info") // Added "Info" for security info

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showVpnDialog by remember { mutableStateOf(false) }
    val isVpnActive = remember { mutableStateOf(securityManager.isVpnActive()) }

    // Register VPN status listener
    DisposableEffect(securityManager) {
        val listener: (Boolean) -> Unit = { isActive ->
            isVpnActive.value = isActive
        }
        // Assuming SecurityManager has a way to set this listener
        // You might need to add a method like setVpnStatusListener in SecurityManager
        // For simplicity here, we'll directly update the state based on periodic check or a better callback mechanism
        // For now, let's just rely on the onCapabilitiesChanged being handled by the Activity's networkCallback
        // and its effect on isVpnActive.
        onDispose {
            // No direct unsetting needed here if the listener is tied to Activity's lifecycle
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = { /* Open drawer or menu */ }) {
                        Icon(Icons.AutoMirrored.Filled.MenuOpen, contentDescription = "Menu")
                    }
                },
                actions = {
                    // Check for security issues dynamically if needed (e.g., VPN status)
                    val currentSecurityIssue = securityManager.getSecurityIssue()
                    if (currentSecurityIssue != SecurityManager.SecurityIssue.NONE) {
                        if (currentSecurityIssue == SecurityManager.SecurityIssue.VPN_ACTIVE) {
                            IconButton(onClick = { showVpnDialog = true }) {
                                Icon(Icons.Filled.Warning, contentDescription = "VPN Active", tint = MaterialTheme.colorScheme.error)
                            }
                        } else {
                            // Other critical issues might just force app exit or show a persistent warning
                            // For this example, we'll let the initial check in MainActivity handle it.
                        }
                    }

                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            when (item) {
                                "Home" -> Icon(Icons.Filled.Home, contentDescription = item)
                                "Profile" -> Icon(Icons.Filled.Person, contentDescription = item)
                                "Settings" -> Icon(Icons.Filled.Settings, contentDescription = item)
                                "Info" -> Icon(Icons.Filled.Info, contentDescription = item) // Icon for "Info"
                            }
                        },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedItem) {
                0 -> HomeScreen()
                1 -> ProfileScreen(sharedPrefsManager)
                2 -> SettingsScreen()
                3 -> SecurityInfoScreen(securityManager) // New screen for security info
            }
        }

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                title = { Text("Confirm Logout") },
                text = { Text("Are you sure you want to log out?") },
                confirmButton = {
                    Button(
                        onClick = {
                            sharedPrefsManager.clearLogin()
                            // Restart MainActivity to show LoginScreen
                            val intent = Intent(context, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Logout")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // VPN Active Warning Dialog
        if (showVpnDialog || isVpnActive.value) { // Show if state is true or triggered by button
            AlertDialog(
                onDismissRequest = { showVpnDialog = false }, // Allow dismiss
                icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                title = { Text("VPN Active!") },
                text = { Text("A VPN connection is detected. For security reasons, please disable VPN to continue.") },
                confirmButton = {
                    TextButton(onClick = {
                        showVpnDialog = false
                        // Optional: Navigate to VPN settings
                        context.startActivity(Intent(Settings.ACTION_VPN_SETTINGS))
                    }) {
                        Text("Go to Settings")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showVpnDialog = false
                        // Optional: Allow user to bypass for now, but log it
                    }) {
                        Text("Dismiss")
                    }
                },
                containerColor = MaterialTheme.colorScheme.errorContainer,
                titleContentColor = MaterialTheme.colorScheme.onErrorContainer,
                textContentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to KTiMAZ Studio!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        AnimatedCardGrid()
    }
}

@Composable
fun AnimatedCardGrid() {
    val icons = remember {
        listOf(
            R.drawable.ic_launcher_foreground, // Replace with actual icons
            Icons.Filled.Analytics,
            Icons.Filled.BugReport,
            Icons.Filled.Cloud,
            Icons.Filled.Code,
            Icons.Filled.DataUsage,
            Icons.Filled.Devices,
            Icons.Filled.Face,
            Icons.Filled.Favorite,
            Icons.Filled.FilterNone,
            Icons.Filled.FlashOn,
            Icons.Filled.Games,
            Icons.Filled.Healing,
            Icons.Filled.Lightbulb,
            Icons.Filled.Palette,
            Icons.Filled.Pets,
            Icons.Filled.Power,
            Icons.Filled.PrivacyTip,
            Icons.Filled.Recommend,
            Icons.Filled.Science,
            Icons.Filled.Security,
            Icons.Filled.Star,
            Icons.Filled.Storage,
            Icons.Filled.ThumbUp,
            Icons.Filled.TrendingUp,
            Icons.Filled.Verified,
            Icons.Filled.Visibility,
            Icons.Filled.Widgets,
            Icons.Filled.Wifi,
            Icons.Filled.Work,
        )
    }


    val cardTitles = listOf(
        "Dashboard", "Analytics", "Reports", "Cloud Sync", "Coding Hub",
        "Data Usage", "Device Info", "Face Unlock", "Favorites", "Filters",
        "Flashlight", "Games", "Health", "Ideas", "Design", "Pets",
        "Power Control", "Privacy", "Recommendations", "Research", "Security",
        "Stargazer", "Storage", "Likes", "Trends", "Verification",
        "Visibility", "Widgets", "Wi-Fi Tools", "Workflows"
    )

    val density = LocalDensity.current // Get LocalDensity for toPx() conversion

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(cardTitles) { index, title ->
            val animatedTrigger = remember { MutableTransitionState(false) }
            val scale by animateFloatAsState(
                targetValue = if (animatedTrigger.targetState) 1f else 0.8f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ), label = "card_scale_anim"
            )
            val offsetY by animateDpAsState(
                targetValue = if (animatedTrigger.targetState) 0.dp else 50.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ), label = "card_offset_y_anim"
            )
            val alpha by animateFloatAsState(
                targetValue = if (animatedTrigger.targetState) 1f else 0f,
                animationSpec = tween(durationMillis = 300), label = "card_alpha_anim"
            )

            LaunchedEffect(Unit) {
                delay(index * 100L) // Staggered delay for each card
                animatedTrigger.targetState = true
            }

            // Apply animations based on the animatedTrigger's state
            if (animatedTrigger.targetState || animatedTrigger.currentState) { // Keep composed until animation finishes on exit
                Card(
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationY = with(density) { offsetY.toPx() }, // Corrected toPx() usage
                            alpha = alpha
                        )
                        // Removed blur here as it was identified as a performance bottleneck
                        .fillMaxWidth()
                        .height(170.dp)
                ) {
                    Column(
                        Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Correctly handle Painter and ImageVector
                        val icon = icons[index % icons.size]
                        if (icon is Int) { // If it's a drawable resource ID
                            Image(
                                painter = painterResource(id = icon),
                                contentDescription = title,
                                modifier = Modifier.size(60.dp)
                            )
                        } else if (icon is Icons.Filled.Add.javaClass) { // If it's an ImageVector (like Icons.Filled.Analytics)
                            Icon(
                                imageVector = icon as androidx.compose.ui.graphics.vector.ImageVector,
                                contentDescription = title,
                                modifier = Modifier.size(60.dp),
                                tint = MaterialTheme.colorScheme.primary // Example tint
                            )
                        }

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


@Composable
fun LoginScreen(sharedPrefsManager: SharedPreferencesManager) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") } // Placeholder, not for real security
    var loginEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(username, password) {
        loginEnabled = username.isNotBlank() && password.isNotBlank()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Login to KTiMAZ Studio",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (loginEnabled) {
                    // Perform login logic
                    sharedPrefsManager.setLoggedIn(true)
                    sharedPrefsManager.setUsername(username)
                    // Navigate to MainScreen
                    (context as? Activity)?.recreate() // Simple way to trigger MainActivity restart
                } else {
                    Toast.makeText(context, "Please enter both username and password", Toast.LENGTH_SHORT).show()
                }
            }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Perform login logic
                if (loginEnabled) {
                    sharedPrefsManager.setLoggedIn(true)
                    sharedPrefsManager.setUsername(username)
                    // Navigate to MainScreen
                    (context as? Activity)?.recreate() // Simple way to trigger MainActivity restart
                } else {
                    Toast.makeText(context, "Please enter both username and password", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = loginEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Login", fontSize = 18.sp)
        }
    }
}

@Composable
fun ProfileScreen(sharedPrefsManager: SharedPreferencesManager) {
    val username = sharedPrefsManager.getUsername() ?: "Guest"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Placeholder for a user profile picture
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground), // Replace with a generic profile icon or user-specific image
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Hello, $username!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Welcome to your profile.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        // Add more profile details or options here
    }
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sharedPrefsManager = remember { SharedPreferencesManager(context) }

    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showChangelogDialog by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) } // Existing setting
    var animateSettingsItems by remember { mutableStateOf(sharedPrefsManager.getAnimateSettingsItems()) } // New state

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

        // Corrected type for settingsItems to be a list of Composable functions
        val settingsItems = remember {
            listOf<@Composable () -> Unit>( // Added @Composable
                {
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
                },
                {
                    SettingItem(
                        title = "Animate Settings Items", // New Setting
                        description = "Enable subtle entry animations for settings items.",
                        leadingIcon = { Icon(Icons.Filled.Dashboard, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)},
                        control = {
                            Switch(
                                checked = animateSettingsItems,
                                onCheckedChange = {
                                    animateSettingsItems = it
                                    sharedPrefsManager.setAnimateSettingsItems(it)
                                }
                            )
                        }
                    )
                },
                {
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
                },
                {
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
                },
                {
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
                },
                {
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
                                    Text(" • Added persistent login with auto-login.", style = MaterialTheme.typography.bodyMedium)
                                    Text(" • Implemented Logout functionality.", style = MaterialTheme.typography.bodyMedium)
                                    Text(" • Enhanced VPN detection with a Material 3 dialog.", style = MaterialTheme.typography.bodyMedium)
                                    Text(" • Added 'About', 'Privacy Policy', and 'Changelog' to Settings.", style = MaterialTheme.typography.bodyMedium)
                                    Text(" • Implemented basic reverse engineering detection (debugger, emulator, root, APK tampering).", style = MaterialTheme.typography.bodyMedium)
                                    Text("🐛 Bug Fixes & Improvements:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
                                    Text(" • Addressed various icon resolution and deprecation warnings.", style = MaterialTheme.typography.bodyMedium)
                                    Text(" • Polished Login screen UX and Navigation Rail visuals.", style = MaterialTheme.typography.bodyMedium)
                                    Text(" • Profile screen now shows username and placeholder picture.", style = MaterialTheme.typography.bodyMedium)
                                    Text(" • General UI/UX tweaks for a more expressive Material 3 feel.", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Thank you for updating!", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                                }
                            },
                            confirmButton = { TextButton(onClick = { showChangelogDialog = false }) { Text("Awesome!") } },
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    }
                },
                {
                    SettingItem(
                        title = "App Version",
                        description = "${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                        leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)},
                        control = {}
                    )
                }
            )
        }


        settingsItems.forEachIndexed { index, itemContent ->
            // Apply the animation if enabled
            val itemVisible = remember { mutableStateOf(false) }
            LaunchedEffect(key1 = animateSettingsItems) {
                if (animateSettingsItems) {
                    delay(index * 70L + 100L) // Staggered animation delay
                    itemVisible.value = true
                } else {
                    itemVisible.value = true // Instantly visible if animation is off
                }
            }

            if (animateSettingsItems) {
                AnimatedVisibility(
                    visible = itemVisible.value,
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
                    Column { // Use Column to ensure divider is also part of animation
                        itemContent()
                        if (index < settingsItems.size - 1) { // Add divider for all but the last item
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }
            } else {
                // Render without animation
                Column {
                    itemContent()
                    if (index < settingsItems.size - 1) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun SettingItem(
    title: String,
    description: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    control: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (leadingIcon != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                leadingIcon()
            }
            Spacer(modifier = Modifier.width(16.dp))
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (control != null) {
            Spacer(modifier = Modifier.width(16.dp))
            control()
        }
    }
}

@Composable
fun SecurityInfoScreen(securityManager: SecurityManager) {
    val context = LocalContext.current
    val securityIssue = remember { mutableStateOf(SecurityManager.SecurityIssue.NONE) }

    LaunchedEffect(Unit) {
        // Re-check security when this screen is shown
        securityIssue.value = securityManager.getSecurityIssue()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Security Information",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Current Status:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                SecurityStatusRow(
                    label = "Overall Security:",
                    isIssue = securityIssue.value != SecurityManager.SecurityIssue.NONE
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (securityIssue.value != SecurityManager.SecurityIssue.NONE) {
                    Text(
                        text = "Detected Issue: ${securityIssue.value.name.replace("_", " ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        text = "No immediate threats detected.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        SecurityCheckItem(
            title = "Debugger Connected",
            status = securityManager.isDebuggerConnected(),
            description = "Checks if a debugger is attached to the app."
        )
        SecurityCheckItem(
            title = "Emulator Detected",
            status = securityManager.isRunningOnEmulator(),
            description = "Checks if the app is running on a virtual device."
        )
        SecurityCheckItem(
            title = "Device Rooted",
            status = securityManager.isDeviceRooted(),
            description = "Checks if the device has root access."
        )
        SecurityCheckItem(
            title = "Hooking Framework Detected",
            status = securityManager.isHookingFrameworkDetected(),
            description = "Checks for tools like Xposed or Frida."
        )
        SecurityCheckItem( // Corrected call
            title = "APK Tampered",
            status = securityManager.isApkTampered(),
            description = "Verifies the app's integrity using its signature."
        )
        SecurityCheckItem(
            title = "VPN Active",
            status = securityManager.isVpnActive(),
            description = "Checks for active VPN connections."
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Note: Client-side checks are not foolproof and can be bypassed by determined attackers.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SecurityCheckItem(title: String, status: Boolean, description: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = if (status) Icons.Filled.Warning else Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = if (status) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SecurityStatusRow(label: String, isIssue: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (isIssue) "COMPROMISED" else "SECURE",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (isIssue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
        )
    }
}
