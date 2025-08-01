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
import android.os.Bundle
import android.os.Debug
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Language
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ktimazstudio.ui.theme.ktimaz
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.security.MessageDigest
import kotlin.experimental.and

// ADD THESE TO YOUR build.gradle.kts (Module: app)
//
// dependencies {
//     // ViewModel
//     implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
//
//     // Hilt (for dependency injection, optional but recommended)
//     // implementation("com.google.dagger:hilt-android:2.51.1")
//     // kapt("com.google.dagger:hilt-android-compiler:2.51.1")
//     // hilt-navigation-compose
//     // implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
// }
//
// plugins {
//     // Apply the KSP plugin
//     id("com.google.devtools.ksp") version "1.9.23-1.0.20"
//     // Apply the Hilt plugin
//     id("com.google.dagger.hilt.android")
// }


// --- ViewModel ---
/**
 * Main ViewModel to handle all application state and business logic.
 * This class is responsible for managing login state, security checks, and UI state,
 * separating them from the Activity and Composables.
 */
class MainViewModel(
    private val sharedPrefsManager: SharedPreferencesManager,
    private val securityManager: SecurityManager
) : ViewModel() {

    // --- State exposed to the UI ---
    // Using LiveData for state that can be observed across different UI components
    private val _isLoggedIn = MutableLiveData(sharedPrefsManager.isLoggedIn())
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _currentUsername = MutableLiveData(sharedPrefsManager.getUsername())
    val currentUsername: LiveData<String?> = _currentUsername

    private val _securityIssue = MutableLiveData(SecurityIssue.NONE)
    val securityIssue: LiveData<SecurityIssue> = _securityIssue

    // Live VPN detection status
    private val _liveVpnDetected = MutableLiveData(securityManager.isVpnActive())
    val liveVpnDetected: LiveData<Boolean> = _liveVpnDetected

    // --- Business Logic ---
    init {
        // Start periodic security checks when the ViewModel is created
        viewModelScope.launch {
            while (true) {
                delay(5000) // Check every 5 seconds
                performSecurityCheck()
            }
        }
    }

    /**
     * Performs a comprehensive security check and updates the state.
     * This method is called periodically and on VPN status changes.
     */
    fun performSecurityCheck() {
        // We'll pass `false` here as the ViewModel won't be active in a Preview.
        val issue = securityManager.getSecurityIssue(false)
        _securityIssue.postValue(issue)
    }

    /**
     * Handles user login logic.
     * @param username The username of the logged-in user.
     */
    fun onLoginSuccess(username: String) {
        sharedPrefsManager.setLoggedIn(true, username)
        _isLoggedIn.value = true
        _currentUsername.value = username
    }

    /**
     * Handles user logout logic.
     */
    fun onLogout() {
        sharedPrefsManager.setLoggedIn(false)
        _isLoggedIn.value = false
        _currentUsername.value = null
    }

    /**
     * Registers the VPN detection callback.
     * @param onVpnStatusChanged Callback to be invoked when VPN status changes.
     */
    fun registerVpnDetectionCallback(onVpnStatusChanged: (Boolean) -> Unit): ConnectivityManager.NetworkCallback {
        return securityManager.registerVpnDetectionCallback { isVpn ->
            _liveVpnDetected.postValue(isVpn)
            if (isVpn) {
                // If a VPN is detected, immediately flag it as a security issue.
                _securityIssue.postValue(SecurityIssue.VPN_ACTIVE)
            } else {
                // If VPN is gone, re-evaluate the security status.
                performSecurityCheck()
            }
            onVpnStatusChanged(isVpn)
        }
    }

    /**
     * Unregisters the VPN detection callback.
     * @param networkCallback The callback to unregister.
     */
    fun unregisterVpnDetectionCallback(networkCallback: ConnectivityManager.NetworkCallback) {
        securityManager.unregisterVpnDetectionCallback(networkCallback)
    }
}

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
    private val context: Context = context // Keep context for theme checks

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in_key"
        private const val KEY_USERNAME = "username_key"
        const val KEY_THEME_SETTING = "theme_setting_key" // Made public
        const val KEY_SOUND_ENABLED = "sound_enabled_key" // Made public
        private const val KEY_INITIAL_SETUP_COMPLETE = "initial_setup_complete" // NEW
        private const val KEY_LANGUAGE_SETTING = "language_setting_key" // RE-ADDED
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

    // RE-ADDED: getLanguageSetting() and setLanguageSetting() methods
    fun getLanguageSetting(): String {
        return prefs.getString(KEY_LANGUAGE_SETTING, "English") ?: "English"
    }
    fun setLanguageSetting(language: String) {
        prefs.edit().putString(KEY_LANGUAGE_SETTING, language).apply()
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
 *
 * IMPORTANT SECURITY UPGRADE: Certificate Pinning
 * To further enhance security, especially for network communication, you should implement
 * Certificate Pinning. This involves:
 * 1. Obtaining the public key hash of your server's SSL certificate.
 * 2. Storing this hash securely within your app's Network Security Configuration XML file.
 * 3. Android's network stack will then verify that the server's certificate matches your
 * pinned hash, preventing Man-in-the-Middle (MitM) attacks.
 * This is a crucial step for apps handling sensitive data.
 *
 * A sample `network_security_config.xml` would look like this:
 * ```xml
 * <?xml version="1.0" encoding="utf-8"?>
 * <network-security-config>
 * <domain-config>
 * <domain includeSubdomains="true">your-api-domain.com</domain>
 * <pin-set expiration="2025-01-01">
 * <pin digest="SHA-256">your-certificate-hash-here</pin>
 * </pin-set>
 * </domain-config>
 * </network-security-config>
 * ```
 * Then reference this file in your `AndroidManifest.xml`.
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
     * @param isInspectionMode True if the app is running in a Compose preview/inspection mode.
     * return A SecurityIssue enum indicating the first detected issue, or SecurityIssue.NONE if secure.
     */
    fun getSecurityIssue(isInspectionMode: Boolean): SecurityIssue {
        if (isInspectionMode) {
            return SecurityIssue.NONE // Skip security checks in inspection mode
        }

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
}

// --- MainActivity and UI Composables ---
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
class MainActivity : ComponentActivity() {
    private lateinit var sharedPrefsManager: SharedPreferencesManager
    private lateinit var securityManager: SecurityManager
    private lateinit var soundEffectManager: SoundEffectManager
    private lateinit var mainViewModel: MainViewModel // Instantiate ViewModel
    private var vpnNetworkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        sharedPrefsManager = SharedPreferencesManager(applicationContext)
        soundEffectManager = SoundEffectManager(applicationContext, sharedPrefsManager)
        soundEffectManager.loadSounds()
        securityManager = SecurityManager(applicationContext)
        mainViewModel = MainViewModel(sharedPrefsManager, securityManager) // Initialize ViewModel

        setContent {
            val context = LocalContext.current
            val isInspectionMode = LocalInspectionMode.current

            // Determine theme based on user setting
            val currentThemeSetting = remember { mutableStateOf(sharedPrefsManager.getThemeSetting()) }
            val useDarkTheme = isAppInDarkTheme(currentThemeSetting.value, context)

            // State to control initial setup dialog visibility
            var showInitialSetupDialog by rememberSaveable {
                mutableStateOf(!sharedPrefsManager.isInitialSetupComplete())
            }

            // Observe states from the ViewModel
            val isLoggedIn by mainViewModel.isLoggedIn.observeAsState(sharedPrefsManager.isLoggedIn())
            val currentUsername by mainViewModel.currentUsername.observeAsState(sharedPrefsManager.getUsername())
            val currentSecurityIssue by mainViewModel.securityIssue.observeAsState(
                securityManager.getSecurityIssue(isInspectionMode)
            )

            ktimaz(darkTheme = useDarkTheme) { // Theme wrapper
                if (currentSecurityIssue != SecurityIssue.NONE) {
                    SecurityAlertScreen(issue = currentSecurityIssue) { finishAffinity() }
                } else if (showInitialSetupDialog) {
                    InitialSetupDialog(
                        sharedPrefsManager = sharedPrefsManager,
                        soundEffectManager = soundEffectManager,
                        onSetupComplete = {
                            sharedPrefsManager.setInitialSetupComplete(true)
                            showInitialSetupDialog = false
                        }
                    )
                } else {
                    // Now that we are in a composable context, we can handle the theme listener
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

                    // Live VPN detection is handled by the ViewModel
                    DisposableEffect(Unit) {
                        vpnNetworkCallback = mainViewModel.registerVpnDetectionCallback { /* The ViewModel handles the state update, nothing to do here */ }
                        onDispose {
                            vpnNetworkCallback?.let { mainViewModel.unregisterVpnDetectionCallback(it) }
                        }
                    }

                    // The logic for isLoggedIn and currentUsername is now managed by the ViewModel
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
                                onLogout = { mainViewModel.onLogout() },
                                soundEffectManager = soundEffectManager,
                                sharedPrefsManager = sharedPrefsManager
                            )
                        } else {
                            LoginScreen(
                                onLoginSuccess = { loggedInUsername ->
                                    mainViewModel.onLoginSuccess(loggedInUsername)
                                },
                                soundEffectManager = soundEffectManager
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        vpnNetworkCallback?.let { mainViewModel.unregisterVpnDetectionCallback(it) }
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
                                    slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                                } else {
                                    slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                                }
                            },
                            label = "search_bar_transition"
                        ) { searching ->
                            if (searching) {
                                CustomSearchBar(
                                    query = searchQuery,
                                    onQueryChange = { searchQuery = it },
                                    onClear = { searchQuery = "" ; isSearching = false },
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
                            IconButton(onClick = { soundEffectManager.playClickSound()
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
                            slideInHorizontally(initialOffsetX = { if (initialState.route == Screen.Dashboard.route) 300 else -300 }, animationSpec = tween(300))
                        .togetherWith(
                            fadeOut(animationSpec = tween(300, easing = FastOutLinearInEasing)) +
                                    slideOutHorizontally(targetOffsetX = { if (targetState.route == Screen.Dashboard.route) -300 else 300 }, animationSpec = tween(300))
                        )
                },
                label = "nav_rail_content_transition"
            ) { targetDestination ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    when (targetDestination) {
                        Screen.Dashboard -> AnimatedCardGrid(searchQuery)
                        Screen.AppSettings -> AppSettingsScreen(sharedPrefsManager, soundEffectManager, onLogout)
                        Screen.Profile -> ProfileScreen(username, onLogout)
                    }
                }
            }
        }
    }
}

/**
 * A custom search bar Composable with a clear button and animations.
 * @param query The current search query.
 * @param onQueryChange Callback to update the query.
 * @param onClear Callback to clear the query.
 * @param soundEffectManager Manager to play click sounds.
 */
@Composable
fun CustomSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    soundEffectManager: SoundEffectManager
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        singleLine = true,
        placeholder = { Text("Search...", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search Icon",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear Search",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        ),
        shape = RoundedCornerShape(50.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { /* Handle search action */ }
        )
    )
}

@Composable
fun AnimatedCardGrid(searchQuery: String) {
    // This is a placeholder. You can replace this with your actual content.
    val items = remember {
        mutableStateListOf(
            "Item 1", "Item 2", "Item 3", "Item 4",
            "Item 5", "Item 6", "Item 7", "Item 8"
        )
    }
    val filteredItems = if (searchQuery.isNotBlank()) {
        items.filter { it.contains(searchQuery, ignoreCase = true) }
    } else {
        items
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 128.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredItems) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(item, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppSettingsScreen(sharedPrefsManager: SharedPreferencesManager, soundEffectManager: SoundEffectManager, onLogout: () -> Unit) {
    val themeOptions = remember { ThemeSetting.entries.toList() }
    val selectedTheme = remember { mutableStateOf(sharedPrefsManager.getThemeSetting()) }
    var soundEnabled by remember { mutableStateOf(sharedPrefsManager.isSoundEnabled()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Theme Selection
        Text("Theme Settings", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            themeOptions.forEachIndexed { index, theme ->
                SegmentedButton(
                    selected = index == themeOptions.indexOf(selectedTheme.value),
                    onClick = {
                        selectedTheme.value = theme
                        sharedPrefsManager.setThemeSetting(theme)
                        soundEffectManager.playClickSound()
                    },
                    shape = SegmentedButtonDefaults.baseShape,
                    icon = {
                        AnimatedVisibility(
                            visible = index == themeOptions.indexOf(selectedTheme.value),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Icon(imageVector = Icons.Filled.Check, contentDescription = null)
                        }
                    }
                ) {
                    Text(text = theme.name)
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Sound Effects Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = LocalIndication.current,
                    onClick = {
                        soundEnabled = !soundEnabled
                        sharedPrefsManager.setSoundEnabled(soundEnabled)
                        if (soundEnabled) soundEffectManager.playClickSound()
                    }
                )
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (soundEnabled) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                    contentDescription = "Sound",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text("Sound Effects", style = MaterialTheme.typography.titleMedium)
            }
            Switch(
                checked = soundEnabled,
                onCheckedChange = {
                    soundEnabled = it
                    sharedPrefsManager.setSoundEnabled(it)
                    if (it) soundEffectManager.playClickSound()
                }
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Language Setting
        var isDropdownExpanded by remember { mutableStateOf(false) }
        var language by remember { mutableStateOf(sharedPrefsManager.getLanguageSetting()) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isDropdownExpanded = true }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Language,
                    contentDescription = "Language",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text("Language", style = MaterialTheme.typography.titleMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = language, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Select language",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            DropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false }
            ) {
                DropdownMenuItem(text = { Text("English") }, onClick = {
                    language = "English"
                    sharedPrefsManager.setLanguageSetting(language)
                    isDropdownExpanded = false
                })
                DropdownMenuItem(text = { Text("Spanish") }, onClick = {
                    language = "Spanish"
                    sharedPrefsManager.setLanguageSetting(language)
                    isDropdownExpanded = false
                })
                DropdownMenuItem(text = { Text("French") }, onClick = {
                    language = "French"
                    sharedPrefsManager.setLanguageSetting(language)
                    isDropdownExpanded = false
                })
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Logout Button
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout")
        }
    }
}

@Composable
fun ProfileScreen(username: String, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome, $username!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onLogout) {
            Text("Logout")
        }
    }
}

@Composable
fun AppNavigationRail(
    selectedDestination: Screen,
    onDestinationSelected: (Screen) -> Unit,
    isExpanded: Boolean,
    onMenuClick: () -> Unit,
    soundEffectManager: SoundEffectManager
) {
    val destinations = remember { listOf(Screen.Dashboard, Screen.AppSettings, Screen.Profile) }
    NavigationRail(
        modifier = Modifier
            .animateContentSize(
                animationSpec = tween(durationMillis = 300)
            )
            .background(color = MaterialTheme.colorScheme.surface),
        containerColor = Color.Transparent,
        header = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = if (isExpanded) Icons.AutoMirrored.Filled.MenuOpen else Icons.Filled.Menu,
                    contentDescription = "Menu"
                )
            }
        }
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        destinations.forEach { destination ->
            val isSelected = destination.route == selectedDestination.route
            NavigationRailItem(
                selected = isSelected,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "navigationIconColor"
                    )
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                        tint = iconColor
                    )
                },
                label = {
                    if (isExpanded) {
                        Text(destination.label, style = MaterialTheme.typography.labelMedium)
                    }
                },
                colors = NavigationRailItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
fun InitialSetupDialog(
    sharedPrefsManager: SharedPreferencesManager,
    soundEffectManager: SoundEffectManager,
    onSetupComplete: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var currentPage by rememberSaveable { mutableStateOf(0) }
    val totalPages = 2

    AlertDialog(
        onDismissRequest = { /* Not dismissible */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Initial Setup", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Step ${currentPage + 1} of $totalPages",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                        }
                    }, label = "setup_page_transition"
                ) { page ->
                    when (page) {
                        0 -> ThemeSelectionPage(sharedPrefsManager, soundEffectManager)
                        1 -> LanguageAndSoundPage(sharedPrefsManager, soundEffectManager)
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentPage > 0) {
                    TextButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        currentPage--
                    }) {
                        Text("Previous")
                    }
                }
                Button(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (currentPage < totalPages - 1) {
                        currentPage++
                    } else {
                        onSetupComplete()
                    }
                }) {
                    Text(if (currentPage < totalPages - 1) "Next" else "Finish")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}

@Composable
fun ThemeSelectionPage(sharedPrefsManager: SharedPreferencesManager, soundEffectManager: SoundEffectManager) {
    val themeOptions = remember { ThemeSetting.entries.toList() }
    var selectedTheme by rememberSaveable { mutableStateOf(sharedPrefsManager.getThemeSetting()) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Choose your preferred theme.", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            themeOptions.forEachIndexed { index, theme ->
                SegmentedButton(
                    selected = theme == selectedTheme,
                    onClick = {
                        selectedTheme = theme
                        sharedPrefsManager.setThemeSetting(theme)
                        soundEffectManager.playClickSound()
                    },
                    shape = SegmentedButtonDefaults.baseShape,
                ) {
                    Text(theme.name, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun LanguageAndSoundPage(sharedPrefsManager: SharedPreferencesManager, soundEffectManager: SoundEffectManager) {
    var soundEnabled by rememberSaveable { mutableStateOf(sharedPrefsManager.isSoundEnabled()) }
    val languages = listOf("English", "Spanish", "French")
    var language by rememberSaveable { mutableStateOf(sharedPrefsManager.getLanguageSetting()) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Configure app settings.", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text("Sound Effects", style = MaterialTheme.typography.titleMedium)
            Switch(
                checked = soundEnabled,
                onCheckedChange = {
                    soundEnabled = it
                    sharedPrefsManager.setSoundEnabled(it)
                    if (it) soundEffectManager.playClickSound()
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text("Language", style = MaterialTheme.typography.titleMedium)
            // A simple Text for language for this dialog
            Text(language, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    soundEffectManager: SoundEffectManager
) {
    var usernameInput by rememberSaveable { mutableStateOf("") }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .blur(if (isLoading) 5.dp else 0.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome Back!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sign in to continue.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Custom TextField for Username
            CustomTextField(
                value = usernameInput,
                onValueChange = { usernameInput = it },
                label = "Username",
                leadingIcon = Icons.Outlined.AccountCircle,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Custom SecureTextField for Password
            SecureTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                label = "Password",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(visible = errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    soundEffectManager.playClickSound()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    focusManager.clearFocus()
                    isLoading = true
                    coroutineScope.launch {
                        delay(1000) // Simulate network delay
                        if (usernameInput.isNotBlank() && passwordInput.isNotBlank() && usernameInput == "user") {
                            onLoginSuccess(usernameInput)
                            errorMessage = ""
                        } else {
                            errorMessage = "Invalid username or password"
                            isLoading = false
                        }
                    }
                },
                enabled = usernameInput.isNotBlank() && passwordInput.isNotBlank() && !isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                AnimatedVisibility(visible = isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                }
                AnimatedVisibility(visible = !isLoading) {
                    Text("Sign In", fontWeight = FontWeight.Bold)
                }
            }
        }
        AnimatedVisibility(visible = isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

/**
 * A custom, animated, and themed text field.
 */
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(64.dp),
        label = { Text(label) },
        leadingIcon = { Icon(imageVector = leadingIcon, contentDescription = null) },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = keyboardOptions
    )
}

/**
 * A custom secure text field with a password visibility toggle.
 * It uses haptic feedback on toggle for a more interactive feel.
 */
@Composable
fun SecureTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(64.dp),
        label = { Text(label) },
        leadingIcon = { Icon(imageVector = Icons.Outlined.Lock, contentDescription = null) },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        trailingIcon = {
            IconButton(
                onClick = {
                    passwordVisible = !passwordVisible
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            ) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

/**
 * A custom image Composable with a placeholder, loading state, and error handling.
 * This is a foundational custom widget to improve UI and UX.
 */
@Composable
fun ThemedImage(
    drawableRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholderColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    // Use a LaunchedEffect to simulate a loading delay
    LaunchedEffect(drawableRes) {
        isLoading = true
        hasError = false
        delay(500) // Simulate image loading time
        isLoading = false
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            // Show a placeholder while loading
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(placeholderColor)
            ) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else if (hasError) {
            // Show an error icon if loading fails
            Icon(
                imageVector = Icons.Filled.BrokenImage,
                contentDescription = "Image Load Error",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
        } else {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
