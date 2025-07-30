package com.ktimazstudio

import android.Manifest
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
import java.io.File 
import java.io.InputStreamReader 
import java.security.MessageDigest
import kotlin.experimental.and
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
import androidx.compose.material3.Divider 
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField 
import androidx.compose.material3.OutlinedTextFieldDefaults 
import androidx.compose.material3.PlainTooltip 
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton 
import androidx.compose.material3.SegmentedButtonDefaults 
import androidx.compose.material3.SingleChoiceSegmentedButtonRow 
import androidx.compose.material3.SnackbarDuration // Explicit import for Material3 SnackbarDuration
import androidx.compose.material3.SnackbarHost // Explicit import for Material3 SnackbarHost
import androidx.compose.material3.SnackbarHostState // Explicit import for Material3 SnackbarHostState
import androidx.compose.material3.SnackbarResult // Explicit import for Material3 SnackbarResult
import androidx.compose.material3.Switch // Explicit import for Material3 Switch
import androidx.compose.material3.Text // Explicit import for Material3 Text
import androidx.compose.material3.TextButton // Explicit import for Material3 TextButton
import androidx.compose.material3.TooltipBox // Explicit import for Material3 TooltipBox
import androidx.compose.material3.TooltipDefaults // Explicit import for Material3 TooltipDefaults
import androidx.compose.material3.TopAppBarDefaults // Explicit import for Material3 TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState // Explicit import for Material3 rememberTopAppBarState
import androidx.compose.material3.rememberTooltipState // Explicit import for Material3 rememberTooltipState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment // Added: For Alignment
import androidx.compose.ui.Modifier // Added: For Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush // Added: For Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector // Added: For ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType // Added: For HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext // Added: For LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode // For detecting preview mode
import androidx.compose.ui.res.painterResource // Added: For painterResource
import androidx.compose.ui.res.stringResource // Added: For stringResource
import androidx.compose.ui.text.font.FontWeight // Added: For FontWeight
import androidx.compose.ui.text.input.ImeAction // Added: For ImeAction
import androidx.compose.ui.text.input.KeyboardType // Added: For KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation // Added: For PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation // Added: For VisualTransformation
import androidx.compose.ui.text.style.TextAlign // Added: For TextAlign
import androidx.compose.ui.unit.dp // Added: For dp unit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties // Added: For DialogProperties
import androidx.core.content.ContextCompat // Added: For ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ktimazstudio.ui.theme.ktimaz // Assuming this theme exists
import kotlinx.coroutines.delay // Added: For delay
import kotlinx.coroutines.launch
import android.app.UiModeManager
import android.os.PowerManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.surfaceColorAtElevation // Added: For surfaceColorAtElevation

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
        private const val KEY_LANGUAGE_SETTING = "language_setting_key" // NEW
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

    /**
     * Retrieves the current language setting.
     * return The language string. Defaults to "English".
     */
    fun getLanguageSetting(): String {
        return prefs.getString(KEY_LANGUAGE_SETTING, "English") ?: "English"
    }

    /**
     * Sets the new language setting.
     * @param language The language string to store.
     */
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
class MainActivity : ComponentActivity() {
    private lateinit var sharedPrefsManager: SharedPreferencesManager
    private lateinit var securityManager: SecurityManager
    private lateinit var soundEffectManager: SoundEffectManager // Initialize SoundEffectManager
    private var vpnNetworkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        sharedPrefsManager = SharedPreferencesManager(applicationContext)
        // Pass sharedPrefsManager to SoundEffectManager
        soundEffectManager = SoundEffectManager(applicationContext, sharedPrefsManager)
        soundEffectManager.loadSounds() // Load sounds
        securityManager = SecurityManager(applicationContext)

        setContent { // All @Composable calls must be inside setContent
            val context = LocalContext.current
            val isInspectionMode = LocalInspectionMode.current
            // Determine if the app should be in dark theme based on setting
            val currentThemeSetting = remember { mutableStateOf(sharedPrefsManager.getThemeSetting()) }
            val useDarkTheme = isAppInDarkTheme(currentThemeSetting.value, context)

            // Perform initial security checks, passing isInspectionMode
            val initialSecurityIssue = remember {
                securityManager.getSecurityIssue(isInspectionMode)
            }

            // State to control initial setup dialog visibility
            var showInitialSetupDialog by rememberSaveable {
                mutableStateOf(!sharedPrefsManager.isInitialSetupComplete())
            }

            ktimaz(darkTheme = useDarkTheme) { // Theme wrapper
                if (initialSecurityIssue != SecurityIssue.NONE) {
                    SecurityAlertScreen(issue = initialSecurityIssue) { finishAffinity() }
                } else if (showInitialSetupDialog) {
                    // Show the initial setup dialog if it hasn't been completed
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

                    var isLoggedIn by remember { mutableStateOf(sharedPrefsManager.isLoggedIn()) }
                    var currentUsername by remember(isLoggedIn) { mutableStateOf(sharedPrefsManager.getUsername()) }
                    var liveVpnDetected by remember { mutableStateOf(securityManager.isVpnActive()) }
                    var currentSecurityIssue by remember { mutableStateOf(SecurityIssue.NONE) }

                    // Live VPN detection
                    DisposableEffect(Unit) {
                        vpnNetworkCallback = securityManager.registerVpnDetectionCallback { isVpn ->
                            liveVpnDetected = isVpn
                            // If VPN is detected, set it as the current issue.
                            // Otherwise, re-evaluate all security issues.
                            if (isVpn) {
                                currentSecurityIssue = SecurityIssue.VPN_ACTIVE
                            } else {
                                // Pass isInspectionMode to getSecurityIssue
                                currentSecurityIssue = securityManager.getSecurityIssue(isInspectionMode)
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
                            // Pass isInspectionMode to getSecurityIssue
                            val issue = securityManager.getSecurityIssue(isInspectionMode)
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
                                    soundEffectManager = soundEffectManager, // Pass sound manager
                                    sharedPrefsManager = sharedPrefsManager // FIXED: Pass sharedPrefsManager
                                )
                            } else {
                                LoginScreen(
                                    onLoginSuccess = { loggedInUsername ->
                                        sharedPrefsManager.setLoggedIn(true, loggedInUsername)
                                        isLoggedIn = true
                                    },
                                    soundEffectManager = soundEffectManager // Pass sound manager
                                )
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
        soundEffectManager.release() // Release SoundPool resources
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
                // For older versions, default to system theme or dark if power save cannot be detected
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
    var searchQuery by remember { mutableStateOf("") } // State for search query
    var isSearching by remember { mutableStateOf(false) } // State to control search bar visibility

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
            onDestinationSelected = {
                soundEffectManager.playClickSound() // Play sound on navigation item click
                selectedDestination = it
                isSearching = false // Hide search when navigating away from dashboard
                searchQuery = "" // Clear search query
            },
            isExpanded = isRailExpanded,
            onMenuClick = {
                soundEffectManager.playClickSound() // Play sound on menu click
                isRailExpanded = !isRailExpanded
            },
            soundEffectManager = soundEffectManager // Pass sound manager
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
                                if (targetState) { // Entering search
                                    slideInHorizontally { it } + fadeIn() togetherWith
                                            slideOutHorizontally { -it } + fadeOut()
                                } else { // Exiting search
                                    slideInHorizontally { -it } + fadeIn() togetherWith
                                            slideOutHorizontally { it } + fadeOut()
                                }
                            }, label = "search_bar_transition"
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
                        if (selectedDestination == Screen.Dashboard) { // Only show search on Dashboard
                            IconButton(onClick = {
                                soundEffectManager.playClickSound()
                                isSearching = !isSearching
                                if (!isSearching) searchQuery = "" // Clear search when closing
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
                            searchQuery = searchQuery, // Pass search query
                            onCardClick = { title ->
                                soundEffectManager.playClickSound() // Play sound on card click
                                if (title == "System Config") {
                                    context.startActivity(Intent(context, SettingsActivity::class.java))
                                } else {
                                    context.startActivity(Intent(context, ComingActivity::class.java).putExtra("CARD_TITLE", title))
                                }
                            },
                            soundEffectManager = soundEffectManager // Pass sound manager
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
 * Custom Search Bar Composable for the Top App Bar.
 */
@Composable
fun CustomSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    soundEffectManager: SoundEffectManager
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search modules...", style = MaterialTheme.typography.bodyLarge) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") },
        trailingIcon = {
            if (query.isNotEmpty()) {
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
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Search),
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
            unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(vertical = 4.dp)
    )
}


/**
 * Composable for the enhanced Login Screen.
 * Features a more professional UI/UX with gradients, animations, and improved error handling.
 * @param onLoginSuccess Callback invoked on successful login, providing the username.
 * @param soundEffectManager Manager for playing sound effects.
 */
@Composable
fun LoginScreen(onLoginSuccess: (username: String) -> Unit, soundEffectManager: SoundEffectManager) {
    var usernameInput by rememberSaveable { mutableStateOf("") }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) } // New state for loading indicator
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current   // Use application context for Toast
    val coroutineScope = rememberCoroutineScope() // Use rememberCoroutineScope for UI-related coroutines

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorLabelColor = MaterialTheme.colorScheme.error,
        errorLeadingIconColor = MaterialTheme.colorScheme.error,
        errorTrailingIconColor = MaterialTheme.colorScheme.error
    )

    // Gradient for the background of the login screen
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.surfaceContainerLow,
            MaterialTheme.colorScheme.surfaceContainerHigh
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        // Animated Card for the login form
        Card(
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .widthIn(max = 480.dp) // Max width for larger screens
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 32.dp, vertical = 40.dp)
                    .verticalScroll(rememberScrollState()), // Make content scrollable if it overflows
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp) // Increased spacing
            ) {
                // App Logo
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_round),
                    contentDescription = stringResource(id = R.string.app_name) + " Logo",
                    modifier = Modifier
                        .size(96.dp) // Larger logo
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                        .padding(8.dp)
                )
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium, // CHANGED from headlineLarge
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Securely sign in to your account",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Username Field
                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = { usernameInput = it.trim(); errorMessage = null },
                    label = { Text("Username") },
                    leadingIcon = { Icon(Icons.Outlined.AccountCircle, contentDescription = "Username Icon") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(18.dp), // More rounded corners
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null
                )

                // Password Field
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it; errorMessage = null },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "Password Icon") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        // Trigger login attempt
                        isLoading = true
                        errorMessage = null // Clear previous error
                        coroutineScope.launch { // Use coroutineScope
                            delay(2000) // Simulate network request
                            if (usernameInput == "admin" && passwordInput == "admin") {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                soundEffectManager.playClickSound() // Play sound on successful login
                                onLoginSuccess(usernameInput)
                                //Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                            } else {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                soundEffectManager.playClickSound() // Play sound on failed login
                                errorMessage = "Invalid username or password. Please try again."
                                //Toast.makeText(context, "Login Failed!", Toast.LENGTH_SHORT).show()
                            }
                            isLoading = false
                        }
                    }),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) "Hide password" else "Show password"
                        IconButton(onClick = {
                            soundEffectManager.playClickSound() // Play sound on visibility toggle
                            passwordVisible = !passwordVisible
                        }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null
                )

                // Error Message
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

                // Login Button
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.98f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "login_button_scale"
                )
                val alpha by animateFloatAsState(
                    targetValue = if (isPressed) 0.8f else 1.0f,
                    animationSpec = tween(150),
                    label = "login_button_alpha"
                )

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        isLoading = true
                        errorMessage = null // Clear previous error
                        coroutineScope.launch { // Use coroutineScope
                            delay(2000) // Simulate network request
                            if (usernameInput == "admin" && passwordInput == "admin") {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                soundEffectManager.playClickSound() // Play sound on successful login
                                onLoginSuccess(usernameInput)
                                //Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                            } else {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                soundEffectManager.playClickSound() // Play sound on failed login
                                errorMessage = "Invalid username or password. Please try again."
                                //Toast.makeText(context, "Login Failed!", Toast.LENGTH_SHORT).show()
                            }
                            isLoading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .height(56.dp) // Taller button
                        .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha), // Apply press animation directly
                    shape = RoundedCornerShape(20.dp), // More rounded
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 10.dp), // More prominent shadow
                    enabled = !isLoading // Disable button while loading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text("LOGIN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Placeholder for Forgot Password / Sign Up
                TextButton(onClick = {
                    soundEffectManager.playClickSound() // Play sound on text button click
                    /* TODO: Implement navigation to Forgot Password */
                }) {
                    Text("Forgot password?", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

/**
 * Composable for the enhanced Profile Screen.
 * @param username The username to display.
 * @param onLogout Callback invoked when the logout button is clicked.
 * @param soundEffectManager Manager for playing sound effects.
 */
@Composable
fun ProfileScreen(modifier: Modifier = Modifier, username: String, onLogout: () -> Unit, soundEffectManager: SoundEffectManager) {
    val context = LocalContext.current

    val profileBackgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(profileBackgroundGradient)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Profile Picture / Icon
        Box(
            modifier = Modifier
                .size(160.dp) // Larger profile picture area
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.surfaceVariant
                        ),
                        radius = 120f
                    )
                )
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape), // <-- FIXED HERE: 'border' modifier
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Profile Picture",
                modifier = Modifier.size(100.dp), // Icon size within the circle
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Username
        Text(
            text = username.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Welcome Message
        Text(
            text = "Welcome to your personalized space!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(48.dp))

        // Profile Options (Placeholder)
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .widthIn(max = 500.dp)
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                ProfileOptionItem(
                    icon = Icons.Filled.AccountBox,
                    title = "Edit Profile",
                    description = "Update your personal information.",
                    soundEffectManager = soundEffectManager
                ) {
                    //Toast.makeText(context, "Edit Profile Clicked (Placeholder)", Toast.LENGTH_SHORT).show()
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
                ProfileOptionItem(
                    icon = Icons.Filled.Lock,
                    title = "Change Password",
                    description = "Secure your account with a new password.",
                    soundEffectManager = soundEffectManager
                ) {
                    //Toast.makeText(context, "Change Password Clicked (Placeholder)", Toast.LENGTH_SHORT).show()
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
                ProfileOptionItem(
                    icon = Icons.Filled.Settings,
                    title = "Privacy Settings",
                    description = "Manage your data and privacy preferences.",
                    soundEffectManager = soundEffectManager
                ) {
                    //Toast.makeText(context, "Privacy Settings Clicked (Placeholder)", Toast.LENGTH_SHORT).show()
                }
            }
        }
        Spacer(modifier = Modifier.height(48.dp))

        // Logout Button
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.98f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "logout_button_scale"
        )
        val alpha by animateFloatAsState(
            targetValue = if (isPressed) 0.8f else 1.0f,
            animationSpec = tween(150),
            label = "logout_button_alpha"
        )

        Button(
            onClick = {
                soundEffectManager.playClickSound() // Play sound on logout click
                onLogout()
            },
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error, // Stronger error color for logout
                contentColor = MaterialTheme.colorScheme.onError
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha), // Apply press animation directly
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout Icon")
            Spacer(Modifier.width(16.dp))
            Text("Logout", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Reusable composable for an option item within the Profile Screen.
 * Includes click animation and sound.
 */
@Composable
fun ProfileOptionItem(
    icon: ImageVector,
    title: String,
    description: String,
    soundEffectManager: SoundEffectManager,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "profile_item_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1.0f,
        animationSpec = tween(150),
        label = "profile_item_alpha"
    )

    val defaultIndication = LocalIndication.current // Get the default Material indication

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha) // Apply press animation
            .clickable(
                interactionSource = interactionSource,
                indication = defaultIndication, // Explicitly pass the default indication
                onClick = {
                    soundEffectManager.playClickSound() // Play sound on item click
                    onClick()
                }
            )
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // Icon is decorative here
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Column(modifier = Modifier.weight(1f)) {
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
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = "Go to $title",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}


@Composable
fun AppNavigationRail(
    selectedDestination: Screen,
    onDestinationSelected: (Screen) -> Unit,
    isExpanded: Boolean,
    onMenuClick: () -> Unit,
    soundEffectManager: SoundEffectManager, // Pass sound manager
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

            // MODIFIED: Pass interactionSource directly to IconButton
            IconButton(
                onClick = onMenuClick,
                interactionSource = interactionSource, // Pass interactionSource here
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale) // Apply press animation
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
fun SettingsScreen(modifier: Modifier = Modifier, soundEffectManager: SoundEffectManager, sharedPrefsManager: SharedPreferencesManager) {
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showChangelogDialog by remember { mutableStateOf(false) }

    // State for theme and sound settings
    val currentThemeSetting = remember { mutableStateOf(sharedPrefsManager.getThemeSetting()) }
    val isSoundEnabled = remember { mutableStateOf(sharedPrefsManager.isSoundEnabled()) }

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
                    onCheckedChange = {
                        soundEffectManager.playClickSound() // Play sound on switch toggle
                        notificationsEnabled = it
                    }
                )
            }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

        // Theme Changer Setting
        SettingItem(
            title = "App Theme",
            description = "Change the visual theme of the application.",
            leadingIcon = { Icon(Icons.Filled.ColorLens, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)},
            control = {
                // Dropdown menu for theme selection
                var expanded by remember { mutableStateOf(false) }
                TextButton(onClick = {
                    soundEffectManager.playClickSound()
                    expanded = true
                }) {
                    Text(currentThemeSetting.value.name.replace("_", " "), style = MaterialTheme.typography.bodyMedium)
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Expand theme options")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    ThemeSetting.values().forEach { theme ->
                        DropdownMenuItem(
                            text = { Text(theme.name.replace("_", " ")) },
                            onClick = {
                                soundEffectManager.playClickSound()
                                sharedPrefsManager.setThemeSetting(theme)
                                currentThemeSetting.value = theme // Update local state
                                expanded = false
                            }
                        )
                    }
                }
            }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

        // Sound On/Off Setting
        SettingItem(
            title = "Sound Effects",
            description = "Enable or disable click sounds and other effects.",
            leadingIcon = { Icon(if (isSoundEnabled.value) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)},
            control = {
                Switch(
                    checked = isSoundEnabled.value,
                    onCheckedChange = {
                        sharedPrefsManager.setSoundEnabled(it)
                        isSoundEnabled.value = it // Update local state
                        if (it) soundEffectManager.playClickSound() // Play sound only if enabling
                    }
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
            onClick = {
                soundEffectManager.playClickSound() // Play sound on item click
                showAccountDialog = true
            }
        )
        if (showAccountDialog) {
            AlertDialog(
                onDismissRequest = { showAccountDialog = false },
                icon = { Icon(Icons.Filled.AccountBox, contentDescription = null)},
                title = { Text("Account Preferences") },
                text = { Text("Account settings details would appear here or navigate to a dedicated screen. This is a placeholder.") },
                confirmButton = { TextButton(onClick = {
                    soundEffectManager.playClickSound() // Play sound on dialog button click
                    showAccountDialog = false
                }) { Text("OK") } }
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

        SettingItem(
            title = "About",
            description = "Information about this application.",
            leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)},
            control = { Icon(Icons.Filled.ChevronRight, contentDescription = "View About", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            onClick = {
                soundEffectManager.playClickSound() // Play sound on item click
                showAboutDialog = true
            }
        )
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                icon = { Icon(Icons.Filled.Info, contentDescription = "About App Icon")},
                title = { Text("About " + stringResource(id = R.string.app_name)) },
                text = { Text("Version: ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})\n\nDeveloped by Ktimaz Studio.\n\nThis application is a demonstration of various Android and Jetpack Compose features. Thank you for using our app!") },
                confirmButton = { TextButton(onClick = {
                    soundEffectManager.playClickSound() // Play sound on dialog button click
                    showAboutDialog = false
                }) { Text("Close") } }
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

        SettingItem(
            title = "Privacy Policy",
            description = "Read our privacy policy.",
            leadingIcon = { Icon(Icons.Filled.Policy, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)},
            control = { Icon(Icons.Filled.ChevronRight, contentDescription = "View Privacy Policy", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            onClick = {
                soundEffectManager.playClickSound() // Play sound on item click
                showPrivacyDialog = true
            }
        )
        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                icon = { Icon(Icons.Filled.Policy, contentDescription = "Privacy Policy Icon")},
                title = { Text("Privacy Policy") },
                text = { Text("Placeholder for Privacy Policy text. In a real application, this would contain the full policy details or link to a web page.\n\nWe are committed to protecting your privacy. Our policy outlines how we collect, use, and safeguard your information.") },
                confirmButton = { TextButton(onClick = {
                    soundEffectManager.playClickSound() // Play sound on dialog button click
                    showPrivacyDialog = false
                }) { Text("Close") } }
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

        // Changelog Item
        SettingItem(
            title = "Changelog",
            description = "See what's new in this version.",
            leadingIcon = { Icon(Icons.Filled.HistoryEdu, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)},
            control = { Icon(Icons.Filled.ChevronRight, contentDescription = "View Changelog", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            onClick = {
                soundEffectManager.playClickSound() // Play sound on item click
                showChangelogDialog = true
            }
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
                        Text(" • Added click sound effects and beautiful press animations.", style = MaterialTheme.typography.bodyMedium)
                        Text(" • Implemented search functionality in the Dashboard.", style = MaterialTheme.typography.bodyMedium)
                        Text(" • Added tooltips for new users on Dashboard cards.", style = MaterialTheme.typography.bodyMedium)
                        Text(" • Added Theme Changer (Light, Dark, System, Battery Saver).", style = MaterialTheme.typography.bodyMedium) // New Changelog entry
                        Text(" • Added Sound Effects On/Off setting.", style = MaterialTheme.typography.bodyMedium) // New Changelog entry
                        Text(" • Improved UI sizing consistency across devices.", style = MaterialTheme.typography.bodyMedium) // New Changelog entry
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("🐛 Bug Fixes & Improvements:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
                        Text(" • Addressed various icon resolution and deprecation warnings.", style = MaterialTheme.typography.bodyMedium)
                        Text(" • Polished Login screen UX and Navigation Rail visuals.", style = MaterialTheme.typography.bodyMedium)
                        Text(" • Profile screen now shows username and placeholder picture.", style = MaterialTheme.typography.bodyMedium)
                        Text(" • General UI/UX tweaks for a more expressive Material 3 feel.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Thank you for updating!", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = { TextButton(onClick = {
                    soundEffectManager.playClickSound() // Play sound on dialog button click
                    showChangelogDialog = false
                }) { Text("Awesome!") } },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
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

/**
 * Reusable composable for a setting item.
 * Includes click animation and sound if onClick is provided.
 */
@Composable
fun SettingItem(
    title: String,
    description: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    control: @Composable (() -> Unit)? = null,
    soundEffectManager: SoundEffectManager? = null // Optional sound manager
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "setting_item_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.8f else 1.0f,
        animationSpec = tween(150),
        label = "setting_item_alpha"
    )

    val defaultIndication = LocalIndication.current // Get the default Material indication

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha) // Apply press animation
            .clickable(
                interactionSource = interactionSource,
                indication = defaultIndication, // Explicitly pass the default indication
                onClick = {
                    soundEffectManager?.playClickSound() // Play sound if manager provided
                    onClick()
                }
            )
            .padding(vertical = 16.dp, horizontal = 8.dp),
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedCardGrid(modifier: Modifier = Modifier, searchQuery: String, onCardClick: (String) -> Unit, soundEffectManager: SoundEffectManager) {
    val cards = listOf(
        "Spectrum Analyzer", "Image Synthesizer", "Holovid Player", "Neural Net Link",
        "Encrypted Notes", "Quantum Web", "Bio Scanner", "Interface Designer",
        "Sonic Emitter", "AI Core Access", "System Config"
    )
    // Consider adding specific icons for each card for better visual distinction
    val icons = List(cards.size) { painterResource(id = R.mipmap.ic_launcher_round) } // Placeholder: replace with distinct icons
    val haptic = LocalHapticFeedback.current

    val filteredCards = remember(cards, searchQuery) {
        if (searchQuery.isBlank()) {
            cards
        } else {
            cards.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier.fillMaxSize()
    ) {
        itemsIndexed(filteredCards, key = { _, title -> title }) { index, title ->
            var itemVisible by remember { mutableStateOf(false) }
            LaunchedEffect(key1 = title) {
                delay(index * 70L + 100L) // Staggered animation delay
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
                // Corrected: Use Float.value
                val animatedAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.75f,
                    targetValue = 0.60f,
                    animationSpec = infiniteRepeatable(animation = tween(2500, easing = EaseInOutCubic), repeatMode = RepeatMode.Reverse),
                    label = "card_alpha_$title"
                )

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val pressScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.95f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "card_press_scale"
                )
                val pressAlpha by animateFloatAsState(
                    targetValue = if (isPressed) 0.7f else 1.0f,
                    animationSpec = tween(150),
                    label = "card_press_alpha"
                )

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text("Click to open $title module")
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    // MODIFIED: Pass interactionSource directly to Card
                    Card(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCardClick(title)
                        },
                        interactionSource = interactionSource, // Pass interactionSource here
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp).copy(alpha = animatedAlpha.value) // Corrected alpha access
                        ),
                        border = BorderStroke(width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .graphicsLayer(
                                scaleX = scale * pressScale, // Combine infinite and press animations
                                scaleY = scale * pressScale,
                                alpha = animatedAlpha.value * pressAlpha.value // Corrected: Use .value for pressAlpha
                            )
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
                                painter = icons[index % icons.size], // Using placeholder icon
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitialSetupDialog(
    sharedPrefsManager: SharedPreferencesManager,
    soundEffectManager: SoundEffectManager,
    onSetupComplete: () -> Unit
) {
    val context = LocalContext.current

    // States for selections
    var selectedTheme by remember { mutableStateOf(sharedPrefsManager.getThemeSetting()) }
    var selectedLanguage by remember { mutableStateOf(sharedPrefsManager.getLanguageSetting()) }
    val languages = listOf("English", "Spanish", "French", "German", "Bengali") // Example languages

    // State for permission status
    var hasStoragePermission by remember {
        mutableStateOf(checkStoragePermission(context))
    }

    // Permission launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(), // Explicitly specify contract
        onResult = { permissions ->
            val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions[Manifest.permission.READ_MEDIA_IMAGES] == true &&
                permissions[Manifest.permission.READ_MEDIA_VIDEO] == true &&
                permissions[Manifest.permission.READ_MEDIA_AUDIO] == true
            } else {
                permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
            }
            hasStoragePermission = granted
            if (granted) {
                Toast.makeText(context, "Storage permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Storage permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    AlertDialog(
        onDismissRequest = { /* Dialog is not dismissible until setup is complete */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false), // Corrected import
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        icon = { Icon(Icons.Filled.Settings, contentDescription = "Setup Icon") },
        title = { Text("Configure App", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Theme Mode Section
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.ColorLens, contentDescription = "Theme Icon", modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Theme Mode", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ThemeSetting.values().forEachIndexed { index, theme ->
                            SegmentedButton(
                                selected = theme == selectedTheme,
                                onClick = {
                                    soundEffectManager.playClickSound()
                                    selectedTheme = theme
                                    sharedPrefsManager.setThemeSetting(theme)
                                },
                                shape = SegmentedButtonDefaults.shape(index, ThemeSetting.values().size), // Corrected shape usage
                                label = { Text(theme.name.replace("_", " ")) }
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Language Section
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.AutoMirrored.Filled.Language, contentDescription = "Language Icon", modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Language", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                    var languageExpanded by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = {
                            soundEffectManager.playClickSound()
                            languageExpanded = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedLanguage) // Display the selected language directly
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select Language")
                    }
                    DropdownMenu(
                        expanded = languageExpanded,
                        onDismissRequest = { languageExpanded = false }
                    ) {
                        languages.forEach { language ->
                            DropdownMenuItem(
                                text = { Text(language) },
                                onClick = {
                                    soundEffectManager.playClickSound()
                                    selectedLanguage = language
                                    sharedPrefsManager.setLanguageSetting(language)
                                    languageExpanded = false
                                }
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Storage Permission Section
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Storage, contentDescription = "Storage Icon", modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Storage Permission", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            soundEffectManager.playClickSound()
                            if (!hasStoragePermission) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    requestPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.READ_MEDIA_IMAGES,
                                            Manifest.permission.READ_MEDIA_VIDEO,
                                            Manifest.permission.READ_MEDIA_AUDIO
                                        )
                                    )
                                } else {
                                    @Suppress("DEPRECATION")
                                    requestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                                }
                            }
                        },
                        enabled = !hasStoragePermission, // Disable button if permission already granted
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (hasStoragePermission) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = "Permission Granted")
                            Spacer(Modifier.width(8.dp))
                            Text("Permission Granted")
                        } else {
                            Text("Grant Storage Permission")
                        }
                    }
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
