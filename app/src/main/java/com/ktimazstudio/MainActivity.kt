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
import android.os.Bundle
import android.os.Debug
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ktimazstudio.ui.theme.ktimaz
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.security.MessageDigest
import java.util.Base64
import kotlin.experimental.and

// Ensure these dependencies are in build.gradle:
// implementation "androidx.compose.material3:material3:1.2.1"
// implementation "androidx.compose.animation:animation:1.6.8"
// implementation "androidx.activity:activity-compose:1.9.1"

// Theme Settings Enum
enum class ThemeSetting {
    LIGHT, DARK, SYSTEM, BATTERY_SAVER
}

// SoundEffectManager
class SoundEffectManager(private val context: Context, private val sharedPrefsManager: SharedPreferencesManager) {
    private var soundPool: SoundPool? = null
    private var clickSoundId: Int = 0

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

    fun playClickSound() {
        if (sharedPrefsManager.isSoundEnabled() && clickSoundId != 0) {
            soundPool?.play(clickSoundId, 1.0f, 1.0f, 0, 0, 1.0f)
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}

// SharedPreferencesManager
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

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

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

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    fun getThemeSetting(): ThemeSetting {
        val themeString = prefs.getString(KEY_THEME_SETTING, ThemeSetting.SYSTEM.name)
        return try {
            ThemeSetting.valueOf(themeString ?: ThemeSetting.SYSTEM.name)
        } catch (e: IllegalArgumentException) {
            ThemeSetting.SYSTEM
        }
    }

    fun setThemeSetting(themeSetting: ThemeSetting) {
        prefs.edit().putString(KEY_THEME_SETTING, themeSetting.name).apply()
    }

    fun isSoundEnabled(): Boolean = prefs.getBoolean(KEY_SOUND_ENABLED, true)

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    fun isInitialSetupComplete(): Boolean = prefs.getBoolean(KEY_INITIAL_SETUP_COMPLETE, false)

    fun setInitialSetupComplete(complete: Boolean) {
        prefs.edit().putBoolean(KEY_INITIAL_SETUP_COMPLETE, complete).apply()
    }

    fun getLanguageSetting(): String = prefs.getString(KEY_LANGUAGE_SETTING, "English") ?: "English"

    fun setLanguageSetting(language: String) {
        prefs.edit().putString(KEY_LANGUAGE_SETTING, language).apply()
    }
}

// SecurityManager with Enhanced Security
class SecurityManager(private val context: Context) {
    private val EXPECTED_APK_HASH = String(Base64.getDecoder().decode("ZjIxMzE3ZDQ0NjI3NmZmMzE3NGEzNjNjN2ZkZmY0MTcxYzczYjFiODBhODJiYjkwODI5NDNlYTkyMDBhODQyNQ==")).lowercase()

    fun isDebuggerConnected(): Boolean = Debug.isDebuggerConnected() || isTracerAttached()

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

    fun registerVpnDetectionCallback(onVpnStatusChanged: (Boolean) -> Unit): ConnectivityManager.NetworkCallback {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                onVpnStatusChanged(isVpnActive())
            }

            override fun onLost(network: Network) {
                onVpnStatusChanged(isVpnActive())
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                onVpnStatusChanged(isVpnActive())
            }
        }
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        return networkCallback
    }

    fun unregisterVpnDetectionCallback(networkCallback: ConnectivityManager.NetworkCallback) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

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
            "/su/bin/su",
            "/system/xbin/magisk"
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
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            if (reader.readLine() != null) return true
        } catch (e: Exception) {
        } finally {
            process?.destroy()
        }

        return false
    }

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
            while (reader.readLine().also { line = it } != null) {
                for (prop in props) {
                    if (line?.contains("[$prop]:") == true) return true
                }
            }
            process.destroy()
        } catch (e: Exception) {
        }

        try {
            context.packageManager.getPackageInfo("de.robv.android.xposed.installer", PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
        }

        return false
    }

    fun isApkTampered(): Boolean {
        val currentSignatureHash = getSignatureSha256Hash()
        return currentSignatureHash != null && currentSignatureHash.lowercase() != EXPECTED_APK_HASH.lowercase()
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

    fun isCertificatePinningValid(): Boolean {
        // Placeholder: Implement actual certificate pinning for network requests
        return true
    }

    fun getSecurityIssue(isInspectionMode: Boolean): SecurityIssue {
        if (isInspectionMode) return SecurityIssue.NONE
        if (isDebuggerConnected()) return SecurityIssue.DEBUGGER_ATTACHED
        if (isTracerAttached()) return SecurityIssue.DEBUGGER_ATTACHED
        if (isRunningOnEmulator()) return SecurityIssue.EMULATOR_DETECTED
        if (isDeviceRooted()) return SecurityIssue.ROOT_DETECTED
        if (isHookingFrameworkDetected()) return SecurityIssue.HOOKING_FRAMEWORK_DETECTED
        if (isApkTampered()) return SecurityIssue.APK_TAMPERED
        if (isVpnActive()) return SecurityIssue.VPN_ACTIVE
        if (!isCertificatePinningValid()) return SecurityIssue.CERTIFICATE_PINNING_FAILED
        return SecurityIssue.NONE
    }
}

enum class SecurityIssue(val message: String) {
    NONE("No security issues detected."),
    VPN_ACTIVE("A VPN connection is active. Please disable it."),
    DEBUGGER_ATTACHED("Debugger detected. For security, the app cannot run."),
    EMULATOR_DETECTED("Emulator detected. For security, the app cannot run."),
    ROOT_DETECTED("Root access detected. For security, the app cannot run."),
    APK_TAMPERED("Application integrity compromised. Please reinstall."),
    HOOKING_FRAMEWORK_DETECTED("Hooking framework detected. For security, the app cannot run."),
    CERTIFICATE_PINNING_FAILED("Network security validation failed. Please check your connection."),
    UNKNOWN("An unknown security issue occurred.")
}

// Navigation Destinations
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Dashboard)
    object AppSettings : Screen("settings", "Settings", Icons.Filled.Settings)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
}

// Utility Functions
fun isConnected(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

fun openWifiSettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
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
            val initialSecurityIssue = remember { securityManager.getSecurityIssue(isInspectionMode) }
            var showInitialSetupDialog by rememberSaveable { mutableStateOf(!sharedPrefsManager.isInitialSetupComplete()) }

            ktimaz(darkTheme = useDarkTheme) {
                if (initialSecurityIssue != SecurityIssue.NONE) {
                    SecurityAlertScreen(issue = initialSecurityIssue) { finishAffinity() }
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
                            currentSecurityIssue = if (isVpn) {
                                SecurityIssue.VPN_ACTIVE
                            } else {
                                securityManager.getSecurityIssue(isInspectionMode)
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
                                slideInHorizontally({ fullWidth -> fullWidth }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                        fadeIn(animationSpec = tween(600, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f))) togetherWith
                                        slideOutHorizontally({ fullWidth -> -fullWidth }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                        fadeOut(animationSpec = tween(400, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)))
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

    override fun onDestroy() {
        super.onDestroy()
        vpnNetworkCallback?.let { securityManager.unregisterVpnDetectionCallback(it) }
        soundEffectManager.release()
    }
}

@Composable
fun isAppInDarkTheme(themeSetting: ThemeSetting, context: Context): Boolean {
    val systemInDarkTheme = isSystemInDarkTheme()
    return when (themeSetting) {
        ThemeSetting.LIGHT -> false
        ThemeSetting.DARK -> true
        ThemeSetting.SYSTEM -> systemInDarkTheme
        ThemeSetting.BATTERY_SAVER -> {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                powerManager.isPowerSaveMode
            } else {
                systemInDarkTheme
            }
        }
    }
}

@Composable
fun SecurityAlertScreen(issue: SecurityIssue, onExitApp: () -> Unit) {
    MaterialTheme {
        AlertDialog(
            onDismissRequest = { /* Not dismissible */ },
            icon = {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = "Security Alert",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    "Security Alert",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    issue.message,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = onExitApp,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Exit Application", color = MaterialTheme.colorScheme.onError)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
        )
    }
}

@Composable
fun InitialSetupDialog(
    sharedPrefsManager: SharedPreferencesManager,
    soundEffectManager: SoundEffectManager,
    onSetupComplete: () -> Unit
) {
    val context = LocalContext.current
    var selectedTheme by remember { mutableStateOf(sharedPrefsManager.getThemeSetting()) }
    var selectedLanguage by remember { mutableStateOf(sharedPrefsManager.getLanguageSetting()) }
    val languages = listOf("English", "Spanish", "French", "German", "Bengali")

    AlertDialog(
        onDismissRequest = { /* Not dismissible */ },
        title = { Text("Initial Setup", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Text(
                    "Customize your app experience",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text("Theme", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ThemeSetting.values().forEach { theme ->
                        FilterChip(
                            selected = selectedTheme == theme,
                            onClick = {
                                soundEffectManager.playClickSound()
                                selectedTheme = theme
                            },
                            label = { Text(theme.name.replace("_", " ")) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Language", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = selectedLanguage,
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Filled.ArrowDropDown, "Expand language options")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        languages.forEach { language ->
                            DropdownMenuItem(
                                text = { Text(language) },
                                onClick = {
                                    soundEffectManager.playClickSound()
                                    selectedLanguage = language
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    soundEffectManager.playClickSound()
                    sharedPrefsManager.setThemeSetting(selectedTheme)
                    sharedPrefsManager.setLanguageSetting(selectedLanguage)
                    onSetupComplete()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        },
        modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
    )
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
            MaterialTheme.colorScheme.primary.copy(alpha = 0.92f),
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f),
            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.65f)
        )
    )

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val topAppBarShape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    val scrolledAppBarColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.96f)

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryGradient)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = size.width * 0.8f
                    )
                )
            }
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
                                slideInHorizontally({ fullWidth -> fullWidth }, animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                                        fadeIn(animationSpec = tween(400, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f))) togetherWith
                                        slideOutHorizontally({ fullWidth -> -fullWidth }, animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                                        fadeOut(animationSpec = tween(300, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)))
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
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
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
                                    contentDescription = if (isSearching) "Close Search" else "Open Search",
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
                            shadowElevation = if (isScrolled) 6.dp.toPx() else 0f
                            shape = topAppBarShape
                            clip = true
                        }
                        .background(
                            color = if (isScrolled) scrolledAppBarColor else Color.Transparent,
                            shape = topAppBarShape
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
                    slideInVertically({ fullHeight -> fullHeight / 2 }, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)) +
                            fadeIn(animationSpec = tween(500, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f))) togetherWith
                            slideOutVertically({ fullHeight -> -fullHeight / 2 }, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)) +
                            fadeOut(animationSpec = tween(300, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)))
                },
                label = "nav_rail_content_transition"
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
}

@Composable
fun CustomSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    soundEffectManager: SoundEffectManager
) {
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderAlpha by animateFloatAsState(
        targetValue = if (isFocused) 1.0f else 0.5f,
        animationSpec = tween(300, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)),
        label = "search_border_alpha"
    )

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search modules...", style = MaterialTheme.typography.bodyLarge) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
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
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = borderAlpha),
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = borderAlpha),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f),
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(vertical = 6.dp)
            .shadow(4.dp, RoundedCornerShape(32.dp))
    )
}

@Composable
fun LoginScreen(onLoginSuccess: (username: String) -> Unit, soundEffectManager: SoundEffectManager) {
    var usernameInput by rememberSaveable { mutableStateOf("") }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

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

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.8f),
            MaterialTheme.colorScheme.surfaceContainerHigh
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = size.width * 0.6f
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Card(
            shape = RoundedCornerShape(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 500.dp)
                .padding(32.dp)
                .shadow(12.dp, RoundedCornerShape(32.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 36.dp, vertical = 48.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_round),
                    contentDescription = stringResource(id = R.string.app_name) + " Logo",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .padding(10.dp)
                        .animateContentSize(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                        )
                )
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Securely sign in to your account",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = { usernameInput = it.trim(); errorMessage = null },
                    label = { Text("Username") },
                    leadingIcon = { Icon(Icons.Outlined.AccountCircle, contentDescription = "Username") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(20.dp),
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                        ),
                    isError = errorMessage != null
                )

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it; errorMessage = null },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "Password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        isLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            delay(1500)
                            if (usernameInput == "admin" && passwordInput == "admin") {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                soundEffectManager.playClickSound()
                                onLoginSuccess(usernameInput)
                            } else {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                soundEffectManager.playClickSound()
                                errorMessage = "Invalid credentials. Please try again."
                            }
                            isLoading = false
                        }
                    }),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) "Hide password" else "Show password"
                        IconButton(onClick = {
                            soundEffectManager.playClickSound()
                            passwordVisible = !passwordVisible
                        }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                        ),
                    isError = errorMessage != null
                )

                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                    ),
                    exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                    )
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.96f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
                    label = "login_button_scale"
                )
                val glow by animateFloatAsState(
                    targetValue = if (isPressed) 8f else 4f,
                    animationSpec = tween(300, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)),
                    label = "login_button_glow"
                )

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        isLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            delay(1500)
                            if (usernameInput == "admin" && passwordInput == "admin") {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                soundEffectManager.playClickSound()
                                onLoginSuccess(usernameInput)
                            } else {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                soundEffectManager.playClickSound()
                                errorMessage = "Invalid credentials. Please try again."
                            }
                            isLoading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp)
                        .height(60.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .shadow(glow.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
                    } else {
                        Text("LOGIN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }

                TextButton(onClick = {
                    soundEffectManager.playClickSound()
                    Toast.makeText(context, "Forgot Password not implemented yet", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Forgot password?", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    username: String,
    onLogout: () -> Unit,
    soundEffectManager: SoundEffectManager
) {
    val context = LocalContext.current
    val profileBackgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(profileBackgroundGradient)
            .padding(28.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(56.dp))

        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.surfaceVariant
                        ),
                        radius = 140f
                    )
                )
                .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .shadow(8.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Profile Picture",
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = username.replaceFirstChar { it.titlecase() },
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Welcome to your personalized space!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(56.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 520.dp)
                .padding(horizontal = 16.dp)
                .shadow(6.dp, RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(vertical = 20.dp)) {
                ProfileOptionItem(
                    icon = Icons.Filled.AccountBox,
                    title = "Edit Profile",
                    description = "Update your personal information.",
                    soundEffectManager = soundEffectManager
                ) {
                    Toast.makeText(context, "Edit Profile not implemented", Toast.LENGTH_SHORT).show()
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 28.dp))
                ProfileOptionItem(
                    icon = Icons.Filled.Lock,
                    title = "Change Password",
                    description = "Secure your account with a new password.",
                    soundEffectManager = soundEffectManager
                ) {
                    Toast.makeText(context, "Change Password not implemented", Toast.LENGTH_SHORT).show()
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 28.dp))
                ProfileOptionItem(
                    icon = Icons.Filled.Settings,
                    title = "Privacy Settings",
                    description = "Manage your data and privacy preferences.",
                    soundEffectManager = soundEffectManager
                ) {
                    Toast.makeText(context, "Privacy Settings not implemented", Toast.LENGTH_SHORT).show()
                }
            }
        }
        Spacer(modifier = Modifier.height(56.dp))

        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.96f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
            label = "logout_button_scale"
        )
        val glow by animateFloatAsState(
            targetValue = if (isPressed) 10f else 6f,
            animationSpec = tween(300, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)),
            label = "logout_button_glow"
        )

        Button(
            onClick = {
                soundEffectManager.playClickSound()
                onLogout()
            },
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(60.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .shadow(glow.dp, RoundedCornerShape(24.dp))
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
            Spacer(Modifier.width(16.dp))
            Text("Logout", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

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
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "profile_item_scale"
    )
    val glow by animateFloatAsState(
        targetValue = if (isPressed) 6f else 2f,
        animationSpec = tween(300, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)),
        label = "profile_item_glow"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(glow.dp, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = {
                    soundEffectManager.playClickSound()
                    onClick()
                }
            )
            .padding(horizontal = 28.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(24.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = "Navigate to $title",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
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
        targetValue = if (isExpanded) 200.dp else 88.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "nav_rail_width_anim"
    )
    val railContainerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.96f)

    NavigationRail(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxHeight()
            .width(railWidth)
            .padding(vertical = 16.dp, horizontal = 6.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        containerColor = railContainerColor,
        header = {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.92f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
                label = "menu_icon_scale"
            )

            IconButton(
                onClick = onMenuClick,
                interactionSource = interactionSource,
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
            ) {
                AnimatedContent(
                    targetState = isExpanded,
                    transitionSpec = {
                        scaleIn(initialScale = 0.7f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)) +
                                fadeIn(animationSpec = tween(300)) togetherWith
                                scaleOut(targetScale = 0.7f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)) +
                                fadeOut(animationSpec = tween(200))
                    },
                    label = "menu_icon_transition"
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
                targetValue = if (isSelected) 1.15f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
                label = "nav_item_icon_scale_anim"
            )
            val indicatorColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f) else Color.Transparent
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
                        enter = fadeIn(animationSpec = tween(300)) + expandHorizontally(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                        ),
                        exit = fadeOut(animationSpec = tween(200)) + shrinkHorizontally(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                        )
                    ) {
                        Text(screen.label, maxLines = 1, style = MaterialTheme.typography.labelLarge)
                    }
                },
                alwaysShowLabel = isExpanded,
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = contentColor,
                    selectedTextColor = contentColor,
                    indicatorColor = indicatorColor,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(vertical = 8.dp).height(60.dp)
            )
            if (destinations.last() != screen) {
                Spacer(Modifier.height(8.dp))
            }
        }
        Spacer(Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    soundEffectManager: SoundEffectManager,
    sharedPrefsManager: SharedPreferencesManager
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showChangelogDialog by remember { mutableStateOf(false) }
    val currentThemeSetting = remember { mutableStateOf(sharedPrefsManager.getThemeSetting()) }
    val isSoundEnabled = remember { mutableStateOf(sharedPrefsManager.isSoundEnabled()) }
    val selectedLanguage = remember { mutableStateOf(sharedPrefsManager.getLanguageSetting()) }
    val languages = remember { listOf("English", "Spanish", "French", "German", "Bengali") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            "Application Settings",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 20.dp, horizontal = 12.dp)
        )

        var notificationsEnabled by remember { mutableStateOf(true) }
        SettingItem(
            title = "Enable Notifications",
            description = "Receive updates and alerts.",
            leadingIcon = {
                Icon(Icons.Filled.Settings, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.secondary)
            },
            control = {
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = {
                        soundEffectManager.playClickSound()
                        notificationsEnabled = it
                    }
                )
            }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

        SettingItem(
            title = "App Theme",
            description = "Change the visual theme of the application.",
            leadingIcon = {
                Icon(Icons.Filled.ColorLens, contentDescription = "Theme", tint = MaterialTheme.colorScheme.secondary)
            },
            control = {
                var expanded by remember { mutableStateOf(false) }
                TextButton(onClick = {
                    soundEffectManager.playClickSound()
                    expanded = true
                }) {
                    Text(currentThemeSetting.value.name.replace("_", " "), style = MaterialTheme.typography.bodyLarge)
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Expand theme options")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    ThemeSetting.values().forEach { theme ->
                        DropdownMenuItem(
                            text = { Text(theme.name.replace("_", " "), style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                soundEffectManager.playClickSound()
                                sharedPrefsManager.setThemeSetting(theme)
                                currentThemeSetting.value = theme
                                expanded = false
                            }
                        )
                    }
                }
            }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

        SettingItem(
            title = "Sound Effects",
            description = "Enable or disable click sounds and other effects.",
            leadingIcon = {
                Icon(
                    if (isSoundEnabled.value) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                    contentDescription = "Sound Effects",
                    tint = MaterialTheme.colorScheme.secondary
                )
            },
            control = {
                Switch(
                    checked = isSoundEnabled.value,
                    onCheckedChange = {
                        sharedPrefsManager.setSoundEnabled(it)
                        isSoundEnabled.value = it
                        if (it) soundEffectManager.playClickSound()
                    }
                )
            }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

        SettingItem(
            title = "Language",
            description = "Change the application language.",
            leadingIcon = {
                Icon(Icons.AutoMirrored.Filled.Language, contentDescription = "Language", tint = MaterialTheme.colorScheme.secondary)
            },
            control = {
                var expanded by remember { mutableStateOf(false) }
                TextButton(onClick = {
                    soundEffectManager.playClickSound()
                    expanded = true
                }) {
                    Text(selectedLanguage.value, style = MaterialTheme.typography.bodyLarge)
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Expand language options")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    languages.forEach { language ->
                        DropdownMenuItem(
                            text = { Text(language, style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                soundEffectManager.playClickSound()
                                sharedPrefsManager.setLanguageSetting(language)
                                selectedLanguage.value = language
                                expanded = false
                            }
                        )
                    }
                }
            }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

        var showAccountDialog by remember { mutableStateOf(false) }
        SettingItem(
            title = "Account Preferences",
            description = "Manage your account details.",
            leadingIcon = {
                Icon(Icons.Filled.AccountBox, contentDescription = "Account Preferences", tint = MaterialTheme.colorScheme.secondary)
            },
            control = {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Go to account preferences", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            onClick = {
                soundEffectManager.playClickSound()
                showAccountDialog = true
            }
        )
        if (showAccountDialog) {
            AlertDialog(
                onDismissRequest = { showAccountDialog = false },
                icon = { Icon(Icons.Filled.AccountBox, contentDescription = "Account Preferences") },
                title = { Text("Account Preferences", style = MaterialTheme.typography.titleLarge) },
                text = { Text("Manage your account settings here. (Placeholder)") },
                confirmButton = {
                    TextButton(onClick = {
                        soundEffectManager.playClickSound()
                        showAccountDialog = false
                    }) { Text("OK") }
                },
                modifier = Modifier.shadow(6.dp, RoundedCornerShape(16.dp))
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

        SettingItem(
            title = "About",
            description = "Information about this application.",
            leadingIcon = {
                Icon(Icons.Filled.Info, contentDescription = "About", tint = MaterialTheme.colorScheme.secondary)
            },
            control = {
                Icon(Icons.Filled.ChevronRight, contentDescription = "View About", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            onClick = {
                soundEffectManager.playClickSound()
                showAboutDialog = true
            }
        )
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                icon = { Icon(Icons.Filled.Info, contentDescription = "About App") },
                title = { Text("About " + stringResource(id = R.string.app_name)) },
                text = {
                    Text(
                        "Version: ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})\n\nDeveloped by Ktimaz Studio.\n\nA secure and modern Android app with Jetpack Compose.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        soundEffectManager.playClickSound()
                        showAboutDialog = false
                    }) { Text("Close") }
                },
                modifier = Modifier.shadow(6.dp, RoundedCornerShape(16.dp))
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

        SettingItem(
            title = "Privacy Policy",
            description = "Read our privacy policy.",
            leadingIcon = {
                Icon(Icons.Filled.Policy, contentDescription = "Privacy Policy", tint = MaterialTheme.colorScheme.secondary)
            },
            control = {
                Icon(Icons.Filled.ChevronRight, contentDescription = "View Privacy Policy", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            onClick = {
                soundEffectManager.playClickSound()
                showPrivacyDialog = true
            }
        )
        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                icon = { Icon(Icons.Filled.Policy, contentDescription = "Privacy Policy") },
                title = { Text("Privacy Policy", style = MaterialTheme.typography.titleLarge) },
                text = {
                    Text(
                        "We are committed to protecting your privacy. This is a placeholder for the full policy.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        soundEffectManager.playClickSound()
                        showPrivacyDialog = false
                    }) { Text("Close") }
                },
                modifier = Modifier.shadow(6.dp, RoundedCornerShape(16.dp))
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

        SettingItem(
            title = "Changelog",
            description = "See what's new in this version.",
            leadingIcon = {
                Icon(Icons.Filled.HistoryEdu, contentDescription = "Changelog", tint = MaterialTheme.colorScheme.secondary)
            },
            control = {
                Icon(Icons.Filled.ChevronRight, contentDescription = "View Changelog", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            onClick = {
                soundEffectManager.playClickSound()
                showChangelogDialog = true
            }
        )
        if (showChangelogDialog) {
            AlertDialog(
                onDismissRequest = { showChangelogDialog = false },
                icon = {
                    Icon(Icons.Filled.HistoryEdu, contentDescription = "Changelog", modifier = Modifier.size(32.dp))
                },
                title = { Text("What's New - v${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.titleLarge) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            "Version ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Text(
                            "✨ New Features:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(" • Flutter-like smooth animations.", style = MaterialTheme.typography.bodyMedium)
                        Text(" • Enhanced security with certificate pinning.", style = MaterialTheme.typography.bodyMedium)
                        Text(" • Improved UI/UX with neumorphic effects.", style = MaterialTheme.typography.bodyMedium)
                        Text(" • Better accessibility with content descriptions.", style = MaterialTheme.typography.bodyMedium)
                        Text(" • Added loading overlay for async operations.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            "🐞 Bug Fixes & Improvements:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(" • Fixed compilation errors for animations and composables.", style = MaterialTheme.typography.bodyMedium)
                        Text(" • Optimized recomposition in Compose.", style = MaterialTheme.typography.bodyMedium)
                        Text(" • Enhanced root detection with Magisk checks.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            "Thank you for using our app!",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        soundEffectManager.playClickSound()
                        showChangelogDialog = false
                    }) { Text("Awesome!") }
                },
                modifier = Differ(
                    modifier = Modifier.shadow(6.dp, RoundedCornerShape(16.dp))
                )
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

        SettingItem(
            title = "App Version",
            description = "${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
            leadingIcon = {
                Icon(Icons.Filled.Info, contentDescription = "App Version", tint = MaterialTheme.colorScheme.secondary)
            },
            control = {}
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
    }
}

@Composable
fun SettingItem(
    title: String,
    description: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    control: @Composable (() -> Unit)? = null,
    soundEffectManager: SoundEffectManager? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "setting_item_scale"
    )
    val glow by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 4f else 0f,
        animationSpec = tween(300, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)),
        label = "setting_item_glow"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(glow.dp, RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = {
                    soundEffectManager?.playClickSound()
                    onClick.invoke()
                }
            ) else Modifier)
            .padding(vertical = 20.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Box(modifier = Modifier.padding(end = 20.dp).size(28.dp), contentAlignment = Alignment.Center) {
                leadingIcon.invoke()
            }
        }
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            if (description != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (control != null) {
            Box(modifier = Modifier.padding(start = 12.dp)) {
                control()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedCardGrid(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onCardClick: (String) -> Unit,
    soundEffectManager: SoundEffectManager
) {
    val cards = listOf(
        "System Config" to Icons.Filled.Computer,
        "Network Monitor" to Icons.Filled.Wifi,
        "Data Analytics" to Icons.Filled.BarChart,
        "Security Scan" to Icons.Filled.Security,
        "Performance" to Icons.Filled.Speed,
        "Logs" to Icons.Filled.Description
    ).filter {
        searchQuery.isEmpty() || it.first.contains(searchQuery, ignoreCase = true)
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(cards.size) { index ->
            val (title, icon) = cards[index]
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.95f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
                label = "card_scale_${title}"
            )
            val elevation by animateFloatAsState(
                targetValue = if (isPressed) 12f else 6f,
                animationSpec = tween(300, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)),
                label = "card_elevation_${title}"
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current
                    ) {
                        soundEffectManager.playClickSound()
                        onCardClick(title)
                    }
                    .shadow(elevation.dp, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }
    }
}