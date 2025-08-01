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
import android.provider.Settings
import android.os.Bundle
import android.os.Debug
import android.widget.Toast
import java.io.BufferedReader // Added: For BufferedReader
import java.io.File // Added: For File operations
import java.io.InputStreamReader // Added: For InputStreamReader
import java.security.MessageDigest // Added: For MessageDigest
import kotlin.experimental.and // Added: For bitwise 'and' operation
import androidx.activity.enableEdgeToEdge
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts // Added: For ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult // Crucial import for rememberLauncherForActivityResult
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border // Added: For border modifier
import androidx.compose.foundation.clickable
import androidx.compose.foundation.LocalIndication // Added: For LocalIndication
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
import androidx.compose.material.icons.automirrored.filled.Language // RE-ADDED: For Language icon
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle // Added: For CheckCircle icon
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
import androidx.compose.material.icons.filled.Storage // Added: For Storage icon
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.AlertDialog // Explicit import for Material3 AlertDialog
import androidx.compose.material3.Button // Explicit import for Material3 Button
import androidx.compose.material3.ButtonDefaults // Explicit import for Material3 ButtonDefaults
import androidx.compose.material3.Card // Explicit import for Material3 Card
import androidx.compose.material3.CardDefaults // Explicit import for Material3 CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar // Explicit import for Material3 CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator // Explicit import for Material3 CircularProgressIndicator
import androidx.compose.material3.Divider // Explicit import for Material3 Divider
import androidx.compose.material3.DropdownMenu // Explicit import for Material3 DropdownMenu
import androidx.compose.material3.DropdownMenuItem // Explicit import for Material3 DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api // Explicit import for ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider // Explicit import for HorizontalDivider
import androidx.compose.material3.Icon // Explicit import for Material3 Icon
import androidx.compose.material3.IconButton // Explicit import for Material3 IconButton
import androidx.compose.material3.MaterialTheme // Explicit import for Material3 MaterialTheme
import androidx.compose.material3.NavigationRail // Explicit import for Material3 NavigationRail
import androidx.compose.material3.NavigationRailItem // Explicit import for Material3 NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults // Explicit import for Material3 NavigationRailItemDefaults
import androidx.compose.material3.OutlinedButton // Explicit import for Material3 OutlinedButton
import androidx.compose.material3.OutlinedTextField // Explicit import for Material3 OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults // Explicit import for Material3 OutlinedTextFieldDefaults
import androidx.compose.material3.PlainTooltip // Explicit import for Material3 PlainTooltip
import androidx.compose.material3.Scaffold // Explicit import for Material3 Scaffold
import androidx.compose.material3.SegmentedButton // Explicit import for SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults // Explicit import for SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow // Explicit import for SingleChoiceSegmentedButtonRow
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
import androidx.compose.material3.rememberTooltipState // Explicit import for Material3 TooltipState
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
import com.ktimazstudio.ui.theme.ktimaz
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.app.UiModeManager
import android.os.PowerManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.surfaceColorAtElevation // Added: For surfaceColorAtElevation

// --- Theme Settings Enum ---
enum class ThemeSetting {
    LIGHT, DARK, SYSTEM, BATTERY_SAVER
}

// --- Language Enum (NEW) ---
enum class AppLanguage {
    ENGLISH, SPANISH, FRENCH, GERMAN // You can add more languages here
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
    // IMPROVED: Now uses the AppLanguage enum for type-safety.
    fun getLanguageSetting(): AppLanguage {
        val languageString = prefs.getString(KEY_LANGUAGE_SETTING, AppLanguage.ENGLISH.name)
        return try {
            AppLanguage.valueOf(languageString ?: AppLanguage.ENGLISH.name)
        } catch (e: IllegalArgumentException) {
            AppLanguage.ENGLISH // Fallback to English
        }
    }
    fun setLanguageSetting(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE_SETTING, language.name).apply()
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
    var searchQuery by rememberSaveable { mutableStateOf("") } // State for search query
    var isSearching by rememberSaveable { mutableStateOf(false) } // State to control search bar visibility

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
                                if (targetState) {
                                    // Entering search
                                    slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                                } else {
                                    // Exiting search
                                    slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                                }
                            },
                            label = "search_bar_transition"
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
                            // Only show search on Dashboard
                            IconButton(onClick = {
                                soundEffectManager.playClickSound()
                                isSearching = !isSearching
                                if (!isSearching) {
                                    searchQuery = "" // Clear search if search bar is hidden
                                }
                            }) {
                                val searchIcon = if (isSearching) Icons.Default.Close else Icons.Default.Search
                                Icon(
                                    imageVector = searchIcon,
                                    contentDescription = if (isSearching) "Close Search" else "Search",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (isScrolled) scrolledAppBarColor else Color.Transparent
                    ),
                    modifier = Modifier.clip(topAppBarRoundedShape),
                    scrollBehavior = scrollBehavior
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Content for selected screen
                when (selectedDestination) {
                    Screen.Dashboard -> DashboardScreen(
                        username = username,
                        searchQuery = searchQuery,
                        soundEffectManager = soundEffectManager
                    )
                    Screen.AppSettings -> SettingsScreen(
                        sharedPrefsManager = sharedPrefsManager,
                        onLogout = onLogout,
                        soundEffectManager = soundEffectManager
                    )
                    Screen.Profile -> ProfileScreen(
                        username = username,
                        onLogout = onLogout,
                        soundEffectManager = soundEffectManager
                    )
                }
            }
        }
    }
}

// --- AppNavigationRail (NEW) ---
@Composable
fun AppNavigationRail(
    selectedDestination: Screen,
    onDestinationSelected: (Screen) -> Unit,
    isExpanded: Boolean,
    onMenuClick: () -> Unit,
    soundEffectManager: SoundEffectManager
) {
    AnimatedVisibility(
        visible = isExpanded,
        enter = slideInHorizontally { -it } + fadeIn(),
        exit = slideOutHorizontally { -it } + fadeOut(),
    ) {
        NavigationRail(
            modifier = Modifier.padding(top = 10.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f)
        ) {
            // Menu button
            IconButton(onClick = onMenuClick) {
                Icon(Icons.AutoMirrored.Filled.MenuOpen, contentDescription = "Menu")
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(Modifier.height(10.dp))

            // Navigation items
            val destinations = listOf(Screen.Dashboard, Screen.AppSettings, Screen.Profile)
            destinations.forEach { screen ->
                NavigationRailItem(
                    icon = { Icon(screen.icon, contentDescription = screen.label) },
                    label = { Text(screen.label) },
                    selected = screen == selectedDestination,
                    onClick = { onDestinationSelected(screen) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            }
            Spacer(Modifier.weight(1f))
            TooltipBox(
                tooltip = { PlainTooltip { Text("Logout") } },
                state = rememberTooltipState()
            ) {
                NavigationRailItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout") },
                    label = { Text("Logout") },
                    selected = false, // This is a static action button
                    onClick = {
                        soundEffectManager.playClickSound()
                        // onLogout() // Implement logout logic if needed
                    }
                )
            }
        }
    }
}

// --- CustomSearchBar (FIXED & IMPROVED) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    soundEffectManager: SoundEffectManager,
    modifier: Modifier = Modifier
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
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        // FIXED: The "shape" error is fixed by explicitly defining it.
        shape = RoundedCornerShape(50.dp),
        modifier = modifier.fillMaxWidth()
    )
}

// --- LoginScreen (COMPLETED) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit, soundEffectManager: SoundEffectManager) {
    val haptic = LocalHapticFeedback.current
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome Back!",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            leadingIcon = { Icon(Icons.Outlined.AccountCircle, contentDescription = "Username Icon") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "Password Icon") },
            trailingIcon = {
                IconButton(onClick = {
                    isPasswordVisible = !isPasswordVisible
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }) {
                    val icon = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    Icon(imageVector = icon, contentDescription = "Toggle password visibility")
                }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                soundEffectManager.playClickSound()
                // Simple login logic for demonstration. In a real app, this would involve API calls.
                if (username.isNotEmpty() && password.isNotEmpty()) {
                    onLoginSuccess(username)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
    }
}

// --- DashboardScreen (COMPLETED) ---
@Composable
fun DashboardScreen(
    username: String,
    searchQuery: String,
    soundEffectManager: SoundEffectManager
) {
    // This is a placeholder. You would replace this with your actual dashboard content.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome, $username!",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        // If search is active, show filtered content.
        if (searchQuery.isNotEmpty()) {
            Text(
                text = "Showing results for: '$searchQuery'",
                style = MaterialTheme.typography.bodyLarge
            )
            // Here you would implement your search results UI, e.g., a LazyColumn
            // of items that match the searchQuery.
        } else {
            // Your default dashboard content goes here
            Text(
                text = "Dashboard Content",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { soundEffectManager.playClickSound() }) {
            Text("Dashboard Button")
        }
    }
}

// --- SettingsScreen (COMPLETED & IMPROVED) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    sharedPrefsManager: SharedPreferencesManager,
    onLogout: () -> Unit,
    soundEffectManager: SoundEffectManager
) {
    val haptic = LocalHapticFeedback.current
    var isSoundEnabled by rememberSaveable { mutableStateOf(sharedPrefsManager.isSoundEnabled()) }
    var selectedTheme by rememberSaveable { mutableStateOf(sharedPrefsManager.getThemeSetting()) }
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    var selectedLanguage by rememberSaveable { mutableStateOf(sharedPrefsManager.getLanguageSetting()) }

    // Update settings when state changes
    LaunchedEffect(isSoundEnabled) { sharedPrefsManager.setSoundEnabled(isSoundEnabled) }
    LaunchedEffect(selectedTheme) { sharedPrefsManager.setThemeSetting(selectedTheme) }
    LaunchedEffect(selectedLanguage) { sharedPrefsManager.setLanguageSetting(selectedLanguage) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "App Settings",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // --- Theme Settings Section ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            ThemeSettingsSection(
                selectedTheme = selectedTheme,
                onThemeSelected = { newTheme ->
                    selectedTheme = newTheme
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    soundEffectManager.playClickSound()
                }
            )
        }

        // --- Sound Settings Section ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Sound",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Sound Effects", style = MaterialTheme.typography.bodyLarge)
                }
                Switch(
                    checked = isSoundEnabled,
                    onCheckedChange = {
                        isSoundEnabled = it
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        // Don't play sound if turning off.
                        if (it) {
                            soundEffectManager.playClickSound()
                        }
                    }
                )
            }
        }

        // --- Language Setting Section ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = LocalIndication.current
                ) {
                    soundEffectManager.playClickSound()
                    showLanguageDialog = true
                },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Language,
                        contentDescription = "Language",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = "Language", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = selectedLanguage.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Select Language",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // --- Logout button ---
        Button(
            onClick = {
                soundEffectManager.playClickSound()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            onDismissRequest = { showLanguageDialog = false },
            onLanguageSelected = { language ->
                selectedLanguage = language
                showLanguageDialog = false
            },
            currentLanguage = selectedLanguage
        )
    }
}

// --- LanguageSelectionDialog (NEW) ---
@Composable
fun LanguageSelectionDialog(
    onDismissRequest: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    currentLanguage: AppLanguage
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Select Language") },
        text = {
            Column {
                AppLanguage.values().forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(language) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = language.name.lowercase().replaceFirstChar { it.uppercase() })
                        if (language == currentLanguage) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

// --- ThemeSettingsSection (NEW) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsSection(
    selectedTheme: ThemeSetting,
    onThemeSelected: (ThemeSetting) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Theme",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            ThemeSetting.values().forEachIndexed { index, theme ->
                SegmentedButton(
                    selected = theme == selectedTheme,
                    onClick = { onThemeSelected(theme) },
                    shape = SegmentedButtonDefaults.baseShape,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(theme.name.lowercase().replaceFirstChar { it.uppercase() })
                }
            }
        }
    }
}


// --- ProfileScreen (COMPLETED) ---
@Composable
fun ProfileScreen(
    username: String,
    onLogout: () -> Unit,
    soundEffectManager: SoundEffectManager
) {
    // This is a placeholder. You would replace this with your actual profile content.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile Icon",
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = username,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "This is your profile screen content.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                soundEffectManager.playClickSound()
                onLogout()
            }
        ) {
            Text("Logout")
        }
    }
}

// --- InitialSetupDialog (COMPLETED) ---
@Composable
fun InitialSetupDialog(
    sharedPrefsManager: SharedPreferencesManager,
    soundEffectManager: SoundEffectManager,
    onSetupComplete: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var selectedTheme by rememberSaveable { mutableStateOf(sharedPrefsManager.getThemeSetting()) }
    var isSoundEnabled by rememberSaveable { mutableStateOf(sharedPrefsManager.isSoundEnabled()) }

    AlertDialog(
        onDismissRequest = { /* Not dismissible */ },
        title = { Text("Initial Setup") },
        text = {
            Column {
                Text(
                    "Welcome to ktimaz-studio! Please configure your initial settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Theme selection
                ThemeSettingsSection(
                    selectedTheme = selectedTheme,
                    onThemeSelected = {
                        selectedTheme = it
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        soundEffectManager.playClickSound()
                    }
                )

                // Sound toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Enable Sound Effects", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = isSoundEnabled,
                        onCheckedChange = {
                            isSoundEnabled = it
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (it) soundEffectManager.playClickSound()
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    sharedPrefsManager.setThemeSetting(selectedTheme)
                    sharedPrefsManager.setSoundEnabled(isSoundEnabled)
                    onSetupComplete()
                    soundEffectManager.playClickSound()
                }
            ) {
                Text("Start App")
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    )
}
