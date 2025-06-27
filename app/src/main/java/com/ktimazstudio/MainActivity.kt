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
import androidx.compose.foundation.border
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
import androidx.compose.runtime.saveable.Saver
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
        // Add this new key
        private const val KEY_ANIMATE_CARDS = "animate_cards_key"
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
     * Checks if card animations are enabled. Default is true.
     * @return true if card animations should be enabled, false otherwise.
     */
    fun getAnimateCards(): Boolean {
        return prefs.getBoolean(KEY_ANIMATE_CARDS, true) // Default to true
    }

    /**
     * Sets the state of card animations.
     * @param enabled The new state for card animations.
     */
    fun setAnimateCards(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANIMATE_CARDS, enabled).apply()
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

    // ... (isVpnActive, registerVpnDetectionCallback, unregisterVpnDetectionCallback remain the same) ...
    // ... (isDebuggerConnected, isRunningOnEmulator, isDeviceRooted remain the same) ...

     /**
     * Calculates the SHA-256 hash of the application's *signing certificate*.
     * This is a more robust integrity check than file hash as it remains constant
     * for signed APKs regardless of minor build variations.
     * return The SHA-256 hash as a hexadecimal string, or null if calculation fails.
     */

      /**
     * Checks if a debugger is currently attached to the application process.
     * This now combines Android's built-in check with a more robust procfs check.
     * return true if a debugger is connected, false otherwise.
     */
    fun isDebuggerConnected(): Boolean {
    return Debug.isDebuggerConnected() || isTracerAttached()
    }
    // ... (isRunningOnEmulator, isDeviceRooted remain the same) ...

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
     * Checks if the APK's *signature hash* matches the expected hash.
     * This is now the primary integrity check.
     * return true if the signature hash matches, false otherwise.
     */

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
     * Checks if the APK's signature hash matches the expected hash.
     * return true if the hash matches, false otherwise.
     */
    fun isApkTampered(): Boolean {
        val currentSignatureHash = getSignatureSha256Hash()
        // Compare with the signature SHA-256 hash provided by you.
        return currentSignatureHash != null && currentSignatureHash.lowercase() != EXPECTED_APK_HASH.lowercase()
    }

    // ... (getAppSize and isAppSizeModified_UNUSED remain the same) ...

    /**
     * Aggregates all security checks to determine if the app environment is secure.
     * return A SecurityIssue enum indicating the first detected issue, or SecurityIssue.NONE if secure.
     */

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

    // You could also add a check for expected app size and compare.
    // private val EXPECTED_APP_SIZE_BYTES = 12345678L // Example size
    // fun isAppSizeModified(): Boolean {
    //     return getAppSize() != -1L && getAppSize() != EXPECTED_APP_SIZE_BYTES
    // }

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
     * Aggregates all security checks to determine if the app environment is secure.
     * return A SecurityIssue enum indicating the first detected issue, or SecurityIssue.NONE if secure.
     */
    fun getSecurityIssue(): SecurityIssue {
        if (isDebuggerConnected()) return SecurityIssue.DEBUGGER_ATTACHED
        if (isTracerAttached()) return SecurityIssue.DEBUGGER_ATTACHED // More robust debugger check
        if (isRunningOnEmulator()) return SecurityIssue.EMULATOR_DETECTED
        if (isDeviceRooted()) return SecurityIssue.ROOT_DETECTED
        if (isHookingFrameworkDetected()) return SecurityIssue.HOOKING_FRAMEWORK_DETECTED
        if (isApkTampered()) return SecurityIssue.APK_TAMPERED
        if (isVpnActive()) return SecurityIssue.VPN_ACTIVE // Initial VPN check as well
        // Add other checks here as needed
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
    UNKNOWN("An unknown security issue occurred.")
}

// --- Navigation Destinations ---
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Dashboard)
    object AppSettings : Screen("settings", "Settings", Icons.Filled.Settings)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
    // Add this new destination
    object SecurityInfo : Screen("security_info", "Security Info", Icons.Filled.Lock)
}

// Removed custom WideNavigationRailValue, WideNavigationRailState, WideNavigationRailStateImpl, and rememberWideNavigationRailState
// These caused the type mismatch with Material3's WideNavigationRail

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
class MainActivity : ComponentActivity() {
    private lateinit var sharedPrefsManager: SharedPreferencesManager
    // Removed private lateinit var securityManager: SecurityManager as it will be managed by remember in Composable scope
    private var vpnNetworkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        sharedPrefsManager = SharedPreferencesManager(applicationContext)
        // Removed securityManager = SecurityManager(applicationContext)

        setContent {
            // Get context and instantiate SecurityManager within the composable scope
            val context = LocalContext.current
            val securityManagerInstance = remember { SecurityManager(context) }

            // Perform initial security checks using the composable-managed securityManagerInstance
            val initialSecurityIssue = securityManagerInstance.getSecurityIssue()
            if (initialSecurityIssue != SecurityIssue.NONE) {
                ktimaz {
                    SecurityAlertScreen(issue = initialSecurityIssue) { finishAffinity() }
                }
                return@setContent // Use return@setContent to exit the setContent lambda
            }

            ktimaz {
                var isLoggedIn by remember { mutableStateOf(sharedPrefsManager.isLoggedIn()) }
                var currentUsername by remember(isLoggedIn) { mutableStateOf(sharedPrefsManager.getUsername()) }
                var liveVpnDetected by remember { mutableStateOf(securityManagerInstance.isVpnActive()) }
                var currentSecurityIssue by remember { mutableStateOf(SecurityIssue.NONE) }

                // Live VPN detection using securityManagerInstance
                DisposableEffect(Unit) {
                    vpnNetworkCallback = securityManagerInstance.registerVpnDetectionCallback { isVpn ->
                        liveVpnDetected = isVpn
                        // If VPN is detected, set it as the current issue.
                        // Otherwise, re-evaluate all security issues.
                        if (isVpn) {
                            currentSecurityIssue = SecurityIssue.VPN_ACTIVE
                        } else {
                            currentSecurityIssue = securityManagerInstance.getSecurityIssue()
                        }
                    }
                    onDispose {
                        vpnNetworkCallback?.let { securityManagerInstance.unregisterVpnDetectionCallback(it) }
                    }
                }

                // Periodic security checks for other issues (debugger, root, emulator, tampering, hooking)
                // This runs every 5 seconds to catch issues that might appear after app launch.
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(5000) // Check every 5 seconds
                        val issue = securityManagerInstance.getSecurityIssue() // Use securityManagerInstance
                        // Only update if a new issue is found, or if the current issue was VPN and it's now gone.
                        if (issue != SecurityIssue.NONE && issue != currentSecurityIssue) {
                            currentSecurityIssue = issue
                        } else if (currentSecurityIssue == SecurityIssue.VPN_ACTIVE && issue == SecurityIssue.NONE) {
                            // If VPN was active and now no issue is found, clear the alert
                            currentSecurityIssue = SecurityIssue.NONE
                        }
                    }
                }

                // Observe currentSecurityIssue and display alert if needed
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
                                securityManager = securityManagerInstance // Pass the composable-managed instance
                            )
                        } else {
                            LoginScreen(
                                onLoginSuccess = { loggedInUsername ->
                                    sharedPrefsManager.setLoggedIn(true, loggedInUsername)
                                    isLoggedIn = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // vpnNetworkCallback?.let { securityManager.unregisterVpnDetectionCallback(it) }
        // The securityManagerInstance is managed by Composable. We should ensure unregistration is handled
        // within DisposableEffect. The direct access to securityManager here is removed.
    }
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
            text = { Text(issue.message, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = onExitApp,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Exit Application", color = MaterialTheme.colorScheme.onError)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApplicationUI(username: String, onLogout: () -> Unit, securityManager: SecurityManager) { // Added securityManager parameter
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedDestination by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    var isRailExpanded by remember { mutableStateOf(false) }
    val sharedPrefsManager = remember { SharedPreferencesManager(context) }
    // Get instance
    val animateCardsEnabled by remember { mutableStateOf(sharedPrefsManager.getAnimateCards()) } // Read the state

    // **FIXED WIDE NAVIGATION RAIL CALL**
    // Remove the custom wideRailState and its LaunchedEffect.
    // Assuming WideNavigationRail takes 'expanded' and 'onExpandedChange' like NavigationRail.
    // If this is a different WideNavigationRail (e.g., from adaptive), its parameters may vary.

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
        // **MODIFIED WIDE NAVIGATION RAIL CALL**
        // Assuming WideNavigationRail has an `expanded` parameter to control its state.
        // If this is part of the Material3 adaptive layout suite, its parameters might be different (e.g., state: NavigationSuiteScaffoldState)
        // If this component isn't meant to be used this way, it might need to be removed or replaced with an appropriate adaptive layout.
        // For now, based on the error, we're removing the custom state and assuming direct boolean control.
        WideNavigationRail(
            expanded = isRailExpanded,
            onExpandedChange = { isRailExpanded = it } // Allow collapsing/expanding
            // You might need to add other required parameters depending on the actual WideNavigationRail API.
            // header = { /* Composable for rail header */ },
            // content = { /* Composable for rail items like NavigationRailItem */ },
            // modifier = Modifier,
        ) {
            // Content of the rail, e.g., NavigationRailItems or custom composables
            // Example:
            // NavigationRailItem(
            // selected = selectedDestination == Screen.Dashboard,
            // onClick = { onDestinationSelected(Screen.Dashboard) },
            // icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
            // label = { Text("Dashboard") }
            // )
            // ... more items
        }
        // The rest of your application's content (e.g., the main screen content)
        // This would typically be a Box or Column that takes up the remaining space
        Box(modifier = Modifier.weight(1f)) { // Takes remaining space
            // Your main screen content (e.g., AnimatedCardGrid, etc.)
            Scaffold(
                modifier = Modifier
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
                        fadeIn(animationSpec = tween(300, easing = LinearOutSlowInEasing)) + slideInHorizontally(initialOffsetX = { if (initialState.route == Screen.Dashboard.route) 300 else -300 }, animationSpec = tween(300, easing = LinearOutSlowInEasing))
                    },
                    label = "MainContentTransition"
                ) { currentScreen ->
                    when (currentScreen) {
                        Screen.Dashboard -> DashboardScreen(sharedPrefsManager.getAnimateCards()) // Pass the state
                        Screen.AppSettings -> AppSettings(
                            onLogout = onLogout,
                            sharedPrefsManager = sharedPrefsManager // Pass the manager
                        )
                        Screen.Profile -> ProfileScreen(username = username, onLogout = onLogout)
                        Screen.SecurityInfo -> SecurityInformationScreen(securityManager) // Pass the securityManager instance
                    }
                }
            }
        }
    }
}

// Removed rememberWideNavigationRailState as it's for custom WideNavigationRailState


// Placeholder for AppNavigationRail if it's a custom composable and not directly part of Material3
@Composable
fun AppNavigationRail(
    selectedDestination: Screen,
    onDestinationSelected: (Screen) -> Unit,
    isExpanded: Boolean,
    onMenuClick: () -> Unit
) {
    NavigationRail(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 4.dp),
        header = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.AutoMirrored.Filled.MenuOpen, contentDescription = "Menu")
            }
        },
        containerColor = Color.Transparent
    ) {
        Spacer(Modifier.weight(1f))
        listOf(Screen.Dashboard, Screen.AppSettings, Screen.Profile, Screen.SecurityInfo).forEach { screen ->
            NavigationRailItem(
                selected = selectedDestination == screen,
                onClick = { onDestinationSelected(screen) },
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
        Spacer(Modifier.weight(1f))
    }
}


@Composable
fun DashboardScreen(animateCards: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Dashboard", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))
        // Assuming AnimatedCardGrid is defined elsewhere or directly in this file
        AnimatedCardGrid(animateCardsEnabled = animateCards)
    }
}

@Composable
fun AppSettings(onLogout: () -> Unit, sharedPrefsManager: SharedPreferencesManager) {
    val context = LocalContext.current
    val animateCardsEnabled by remember { mutableStateOf(sharedPrefsManager.getAnimateCards()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("App Settings", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))

        // Enable Notifications
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* Toggle notifications */ }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Enable Notifications", style = MaterialTheme.typography.bodyLarge)
            Switch(checked = true, onCheckedChange = { /* Handle toggle */ })
        }
        Divider()

        // Card Animations
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { sharedPrefsManager.setAnimateCards(!animateCardsEnabled) } // Toggle and save
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Card Animations", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = animateCardsEnabled,
                onCheckedChange = { sharedPrefsManager.setAnimateCards(it) } // Update preference
            )
        }
        Divider()

        // Account Preferences
        TextButton(
            onClick = { /* Show account preferences dialog */ },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Account Preferences", style = MaterialTheme.typography.bodyLarge)
                Icon(Icons.Filled.ChevronRight, contentDescription = "Account Preferences")
            }
        }
        Divider()

        // About
        TextButton(
            onClick = {
                // Show About dialog
                Toast.makeText(context, "Ktimaz Studio App v1.0\nDeveloped by Ktimaz", Toast.LENGTH_LONG).show()
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("About", style = MaterialTheme.typography.bodyLarge)
                Icon(Icons.Filled.Info, contentDescription = "About")
            }
        }
        Divider()

        // Privacy Policy
        TextButton(
            onClick = {
                // Show Privacy Policy dialog
                Toast.makeText(context, "Privacy Policy: Your data is safe with us.", Toast.LENGTH_LONG).show()
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Privacy Policy", style = MaterialTheme.typography.bodyLarge)
                Icon(Icons.Filled.Policy, contentDescription = "Privacy Policy")
            }
        }
        Divider()

        // Changelog
        TextButton(
            onClick = {
                Toast.makeText(context, "Changelog:\nv1.0: Initial Release, Enhanced Security, VPN Detection", Toast.LENGTH_LONG).show()
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Changelog", style = MaterialTheme.typography.bodyLarge)
                Icon(Icons.Filled.HistoryEdu, contentDescription = "Changelog")
            }
        }
        Divider()

        // App Version
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("App Version", style = MaterialTheme.typography.bodyLarge)
            Text("1.0", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Divider()

        // Logout Button
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Logout", color = MaterialTheme.colorScheme.onError)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
        }
    }
}

@Composable
fun ProfileScreen(username: String, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))

        // Profile Picture Placeholder
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = "Profile Picture",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Username Display
        Text(
            text = username,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Placeholder for user details or actions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            TextButton(onClick = { /* TODO: Navigate to Edit Profile */ }, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Edit Profile", style = MaterialTheme.typography.bodyLarge)
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Edit Profile")
                }
            }
            Divider()
            TextButton(onClick = { /* TODO: Navigate to Change Password */ }, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Change Password", style = MaterialTheme.typography.bodyLarge)
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Change Password")
                }
            }
            Divider()
            TextButton(onClick = { /* TODO: Navigate to Privacy Settings */ }, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Privacy Settings", style = MaterialTheme.typography.bodyLarge)
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Privacy Settings")
                }
            }
            Divider()
        }

        Spacer(modifier = Modifier.weight(1f)) // Pushes content to top

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Logout", color = MaterialTheme.colorScheme.onError)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) } // To hold error message
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer),
        startY = 0f,
        endY = Float.POSITIVE_INFINITY
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // App Icon/Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground), // Replace with your app logo
                    contentDescription = "App Logo",
                    modifier = Modifier.size(96.dp).padding(bottom = 16.dp)
                )

                Text(
                    text = "Welcome Back!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it; loginError = null }, // Clear error on input
                    label = { Text("Username") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    leadingIcon = { Icon(Icons.Outlined.AccountCircle, contentDescription = "Username Icon") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; loginError = null }, // Clear error on input
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        // Trigger login on Done key press
                        if (!isLoading) {
                            isLoading = true
                            scope.launch {
                                delay(1000) // Simulate network delay
                                if (username == "admin" && password == "admin") {
                                    onLoginSuccess(username)
                                } else {
                                    loginError = "Invalid username or password"
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                isLoading = false
                            }
                        }
                    }),
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "Password Icon") },
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) "Hide password" else "Show password"
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, description)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                )

                loginError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Button(
                    onClick = {
                        if (!isLoading) {
                            isLoading = true
                            scope.launch {
                                delay(1000) // Simulate network delay
                                if (username == "admin" && password == "admin") {
                                    onLoginSuccess(username)
                                } else {
                                    loginError = "Invalid username or password"
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading, // Disable button while loading
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(
                            text = "Login",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { /* TODO: Navigate to Forgot Password */ }) {
                    Text(
                        text = "Forgot Password?",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityInformationScreen(securityManager: SecurityManager) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Security Information",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "System Integrity Checks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                SecurityCheckItem(
                    label = "Debugger Attached",
                    isDetected = securityManager.isDebuggerConnected()
                )
                SecurityCheckItem(
                    label = "Running on Emulator",
                    isDetected = securityManager.isRunningOnEmulator()
                )
                SecurityCheckItem(
                    label = "Device Rooted",
                    isDetected = securityManager.isDeviceRooted()
                )
                SecurityCheckItem(
                    label = "Hooking Framework Detected",
                    isDetected = securityManager.isHookingFrameworkDetected()
                )
                SecurityCheckItem(
                    label = "APK Tampered",
                    isDetected = securityManager.isApkTampered()
                )
                SecurityCheckItem(
                    label = "VPN Active",
                    isDetected = securityManager.isVpnActive()
                )
            }
        }

        Text(
            text = "Disclaimer: Client-side security checks are never foolproof and can be bypassed by determined attackers. They serve as deterrents and indicators of compromise.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        )
    }
}

@Composable
fun SecurityCheckItem(label: String, isDetected: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Icon(
            imageVector = if (isDetected) Icons.Filled.Warning else Icons.Filled.CheckCircle,
            contentDescription = if (isDetected) "Detected" else "Not Detected",
            tint = if (isDetected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun AnimatedCardGrid(animateCardsEnabled: Boolean) {
    val titles = listOf(
        "Spectrum Analyzer", "System Config", "Network Monitor",
        "Process Viewer", "Battery Health", "Storage Analyzer"
    )
    val icons = listOf(
        painterResource(id = R.drawable.ic_launcher_foreground), // Replace with actual icons
        painterResource(id = R.drawable.ic_launcher_foreground),
        painterResource(id = R.drawable.ic_launcher_foreground),
        painterResource(id = R.drawable.ic_launcher_foreground),
        painterResource(id = R.drawable.ic_launcher_foreground),
        painterResource(id = R.drawable.ic_launcher_foreground)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(titles) { index, title ->
            val scale by animateFloatAsState(
                targetValue = if (animateCardsEnabled) 1.05f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "cardScaleAnimation"
            )

            AnimatedVisibility(
                visible = true, // Always visible for now, can be changed for entry/exit animations
                enter = if (animateCardsEnabled) expandVertically(expandFrom = Alignment.Top) + fadeIn() else EnterTransition.None,
                exit = if (animateCardsEnabled) shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut() else ExitTransition.None
            ) {
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .then(if (animateCardsEnabled) Modifier.graphicsLayer(scaleX = scale, scaleY = scale) else Modifier) // Apply scaling conditionally
                        .then(if (animateCardsEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Modifier.blur(2.dp) else Modifier) // Apply blur conditionally
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
