package com.ktimazstudio

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
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
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.lifecycle.lifecycleScope
import com.ktimazstudio.ui.theme.ktimaz
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.Locale
import kotlin.experimental.and

// Constants
private const val SECURITY_CHECK_INTERVAL_MS = 30_000L
private const val ANIMATION_DURATION_MS = 300
private const val SEARCH_DEBOUNCE_MS = 300L
private const val TAG = "MainActivity"

// NEW: Define ThemeSetting enum
enum class ThemeSetting {
    LIGHT, DARK, SYSTEM, BATTERY_SAVER
}

// NEW: Define SoundEffectManager class
class SoundEffectManager(private val context: Context, private val sharedPrefsManager: SharedPreferencesManager) {
    private var soundPool: SoundPool? = null
    private var clickSoundId: Int = 0

    fun loadSounds() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()
        clickSoundId = soundPool?.load(context, R.raw.click_sound, 1) ?: 0
    }

    fun playClickSound() {
        if (sharedPrefsManager.isSoundEnabled()) {
            soundPool?.play(clickSoundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}

// NEW: Define SharedPreferencesManager class
class SharedPreferencesManager(context: Context) {
    companion object {
        const val PREFS_NAME = "KtimazStudioPrefs"
        const val KEY_THEME_SETTING = "theme_setting"
        const val KEY_LANGUAGE_SETTING = "language_setting"
        const val KEY_SOUND_ENABLED = "sound_enabled"
        const val KEY_LOGGED_IN = "logged_in"
        const val KEY_USERNAME = "username"
        const val KEY_INITIAL_SETUP = "initial_setup"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isSoundEnabled(): Boolean = prefs.getBoolean(KEY_SOUND_ENABLED, true)
    fun setSoundEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    fun getThemeSetting(): ThemeSetting = ThemeSetting.valueOf(prefs.getString(KEY_THEME_SETTING, ThemeSetting.SYSTEM.name) ?: ThemeSetting.SYSTEM.name)
    fun setThemeSetting(theme: ThemeSetting) = prefs.edit().putString(KEY_THEME_SETTING, theme.name).apply()
    fun getLanguageSetting(): String = prefs.getString(KEY_LANGUAGE_SETTING, "English") ?: "English"
    fun setLanguageSetting(language: String) = prefs.edit().putString(KEY_LANGUAGE_SETTING, language).apply()
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)
    fun setLoggedIn(loggedIn: Boolean, username: String? = null) {
        prefs.edit().putBoolean(KEY_LOGGED_IN, loggedIn).apply()
        if (username != null) {
            prefs.edit().putString(KEY_USERNAME, username).apply()
        }
    }
    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)
    fun isInitialSetupComplete(): Boolean = prefs.getBoolean(KEY_INITIAL_SETUP, false)
    fun setInitialSetupComplete(completed: Boolean) = prefs.edit().putBoolean(KEY_INITIAL_SETUP, completed).apply()
}

fun isConnected(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

fun openWifiSettings(context: Context) {
    Toast.makeText(context, "Please enable Wi-Fi or connect to a network.", Toast.LENGTH_LONG).show()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        context.startActivity(Intent(Settings.Panel.ACTION_WIFI))
    } else {
        @Suppress("DEPRECATION")
        context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
    }
}

class SecurityManager(private val context: Context) {
    private val EXPECTED_APK_HASH = "f21317d4d6276ff3174a363c7fdff4171c73b1b80a82bb9082943ea9200a8425".lowercase()
    private var isRootedCached: Boolean? = null
    private var isHookingDetectedCached: Boolean? = null

    fun isDebuggerConnected(): Boolean {
        return Debug.isDebuggerConnected() || isTracerAttached()
    }

    fun isVpnActive(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    }

    fun registerVpnDetectionCallback(callback: (Boolean) -> Unit): ConnectivityManager.NetworkCallback {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            .build()
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                callback(true)
            }
            override fun onLost(network: Network) {
                callback(false)
            }
        }
        cm.registerNetworkCallback(request, networkCallback)
        return networkCallback
    }

    fun unregisterVpnDetectionCallback(callback: ConnectivityManager.NetworkCallback) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.unregisterNetworkCallback(callback)
    }

    fun isRunningOnEmulator(): Boolean {
        return Build.FINGERPRINT.contains("generic", ignoreCase = true) ||
                Build.MODEL.contains("Emulator", ignoreCase = true) ||
                Build.MANUFACTURER.contains("Genymotion", ignoreCase = true) ||
                Build.BRAND.contains("generic", ignoreCase = true)
    }

    fun isDeviceRooted(): Boolean {
        if (isRootedCached != null) return isRootedCached!!
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        isRootedCached = paths.any { File(it).exists() }
        return isRootedCached!!
    }

    fun isHookingFrameworkDetected(): Boolean {
        if (isHookingDetectedCached != null) return isHookingDetectedCached!!
        try {
            throw Exception()
        } catch (e: Exception) {
            val stackTrace = e.stackTrace
            isHookingDetectedCached = stackTrace.any {
                it.className.contains("de.robv.android.xposed") ||
                        it.className.contains("com.zte.heartyservice")
            }
        }
        return isHookingDetectedCached!!
    }

    fun isApkTampered(): Boolean {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            val signatures = packageInfo.signatures
            if (signatures.isEmpty()) return true
            val digest = MessageDigest.getInstance("SHA-256")
            signatures.forEach { signature ->
                val hash = digest.digest(signature.toByteArray())
                val hashString = hash.joinToString("") { byte -> "%02x".format(byte and 0xff.toByte()) }
                if (hashString.lowercase() != EXPECTED_APK_HASH) {
                    return true
                }
            }
            return false
        } catch (e: Exception) {
            Log.e(TAG, "APK tampering check failed: ${e.message}")
            return true
        }
    }

    fun isTracerAttached(): Boolean {
        try {
            val process = Runtime.getRuntime().exec("cat /proc/self/status")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var tracerPid = ""
            reader.useLines { lines ->
                lines.forEach { line ->
                    if (line.contains("TracerPid")) {
                        tracerPid = line.split(":")[1].trim()
                    }
                }
            }
            return tracerPid != "0"
        } catch (e: Exception) {
            Log.e(TAG, "Tracer check failed: ${e.message}")
            return false
        }
    }

    fun getSecurityIssue(isInspectionMode: Boolean): SecurityIssue {
        if (isInspectionMode) return SecurityIssue.NONE
        try {
            if (isDebuggerConnected()) return SecurityIssue.DEBUGGER_ATTACHED
            if (isTracerAttached()) return SecurityIssue.DEBUGGER_ATTACHED
            if (isRunningOnEmulator()) return SecurityIssue.EMULATOR_DETECTED
            if (isDeviceRooted()) return SecurityIssue.ROOT_DETECTED
            if (isHookingFrameworkDetected()) return SecurityIssue.HOOKING_FRAMEWORK_DETECTED
            if (isApkTampered()) return SecurityIssue.APK_TAMPERED
            if (isVpnActive()) return SecurityIssue.VPN_ACTIVE
            return SecurityIssue.NONE
        } catch (e: Exception) {
            Log.e(TAG, "Security check failed: ${e.message}")
            return SecurityIssue.UNKNOWN
        }
    }

    fun resetCache() {
        isRootedCached = null
        isHookingDetectedCached = null
    }

    fun isCertificatePinningValid(): Boolean {
        return true
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
    UNKNOWN("An unknown security issue occurred.")
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Dashboard)
    object AppSettings : Screen("settings", "Settings", Icons.Filled.Settings)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
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
        try {
            soundEffectManager.loadSounds()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load sounds: ${e.message}")
            Toast.makeText(this, "Failed to load resources. Please reinstall the app.", Toast.LENGTH_LONG).show()
            finishAffinity()
            return
        }
        securityManager = SecurityManager(applicationContext)

        setContent {
            val context = LocalContext.current
            val isInspectionMode = LocalInspectionMode.current
            val currentThemeSetting = remember { mutableStateOf(sharedPrefsManager.getThemeSetting()) }
            val useDarkTheme = isAppInDarkTheme(currentThemeSetting.value, context)

            val initialSecurityIssue = remember {
                securityManager.getSecurityIssue(isInspectionMode)
            }

            var showInitialSetupDialog by rememberSaveable {
                mutableStateOf(!sharedPrefsManager.isInitialSetupComplete())
            }

            ktimaz(darkTheme = useDarkTheme) {
                if (initialSecurityIssue != SecurityIssue.NONE) {
                    SecurityAlertScreen(issue = initialSecurityIssue, onExitApp = { finishAffinity() })
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

                    var isLoggedIn by rememberSaveable { mutableStateOf(sharedPrefsManager.isLoggedIn()) }
                    var currentUsername by rememberSaveable { mutableStateOf(sharedPrefsManager.getUsername() ?: "User") }
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
                            delay(SECURITY_CHECK_INTERVAL_MS)
                            val issue = securityManager.getSecurityIssue(isInspectionMode)
                            if (issue != SecurityIssue.NONE && issue != currentSecurityIssue) {
                                currentSecurityIssue = issue
                            } else if (currentSecurityIssue == SecurityIssue.VPN_ACTIVE && issue == SecurityIssue.NONE) {
                                currentSecurityIssue = SecurityIssue.NONE
                            }
                        }
                    }

                    LaunchedEffect(isLoggedIn, currentUsername) {
                        Log.d(TAG, "Login state: isLoggedIn=$isLoggedIn, username=$currentUsername")
                    }

                    if (currentSecurityIssue != SecurityIssue.NONE) {
                        SecurityAlertScreen(
                            issue = currentSecurityIssue,
                            onExitApp = { finishAffinity() },
                            onRetry = {
                                if (currentSecurityIssue == SecurityIssue.VPN_ACTIVE) {
                                    currentSecurityIssue = securityManager.getSecurityIssue(isInspectionMode)
                                }
                            }
                        )
                    } else {
                        Crossfade(
                            targetState = isLoggedIn,
                            animationSpec = tween(ANIMATION_DURATION_MS),
                            label = "LoginScreenTransition"
                        ) { targetIsLoggedIn ->
                            if (targetIsLoggedIn) {
                                Log.d(TAG, "Transitioning to MainApplicationUI with username: $currentUsername")
                                MainApplicationUI(
                                    username = currentUsername,
                                    onLogout = {
                                        Log.d(TAG, "Logout triggered")
                                        sharedPrefsManager.setLoggedIn(false)
                                        isLoggedIn = false
                                        currentUsername = "User"
                                    },
                                    soundEffectManager = soundEffectManager,
                                    sharedPrefsManager = sharedPrefsManager
                                )
                            } else {
                                Log.d(TAG, "Showing LoginScreen")
                                LoginScreen(
                                    onLoginSuccess = { loggedInUsername ->
                                        Log.d(TAG, "Login success for username: $loggedInUsername")
                                        sharedPrefsManager.setLoggedIn(true, loggedInUsername)
                                        isLoggedIn = true
                                        currentUsername = loggedInUsername
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
fun SecurityAlertScreen(issue: SecurityIssue, onExitApp: () -> Unit, onRetry: (() -> Unit)? = null) {
    MaterialTheme {
        AlertDialog(
            onDismissRequest = { },
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
            dismissButton = {
                if (issue == SecurityIssue.VPN_ACTIVE && onRetry != null) {
                    TextButton(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
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
    var selectedTheme by remember { mutableStateOf<ThemeSetting>(sharedPrefsManager.getThemeSetting()) }
    var selectedLanguage by remember { mutableStateOf(sharedPrefsManager.getLanguageSetting()) }
    var permissionsGranted by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions.values.all { it }
        if (!permissionsGranted) {
            showPermissionRationale = true
        }
    }

    LaunchedEffect(Unit) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }) {
            permissionsGranted = true
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                "Welcome to ${stringResource(id = R.string.app_name)}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Please configure your preferences to get started.")
                // FIXED: Moved SegmentedButton to Composable scope
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ThemeSetting.entries.forEachIndexed { index, theme ->
                        OutlinedButton(
                            onClick = {
                                soundEffectManager.playClickSound()
                                sharedPrefsManager.setThemeSetting(theme)
                                selectedTheme = theme
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedTheme == theme) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                        ) {
                            Text(theme.name.replace("_", " "), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Language", style = MaterialTheme.typography.titleMedium)
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = {
                                soundEffectManager.playClickSound()
                                expanded = true
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(selectedLanguage)
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select Language")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            listOf("English", "Spanish", "French", "German", "Bengali").forEach { language ->
                                DropdownMenuItem(
                                    text = { Text(language) },
                                    onClick = {
                                        soundEffectManager.playClickSound()
                                        sharedPrefsManager.setLanguageSetting(language)
                                        selectedLanguage = language
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Storage Permission", style = MaterialTheme.typography.titleMedium)
                    Icon(
                        imageVector = if (permissionsGranted) Icons.Filled.CheckCircle else Icons.Filled.Storage,
                        contentDescription = if (permissionsGranted) "Permission Granted" else "Permission Required",
                        tint = if (permissionsGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (permissionsGranted) {
                        soundEffectManager.playClickSound()
                        onSetupComplete()
                    } else {
                        showPermissionRationale = true
                    }
                },
                enabled = permissionsGranted
            ) {
                Text("Finish Setup")
            }
        },
        dismissButton = {
            if (showPermissionRationale) {
                TextButton(onClick = {
                    soundEffectManager.playClickSound()
                    Toast.makeText(context, "Storage permissions are required to proceed.", Toast.LENGTH_LONG).show()
                    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        arrayOf(
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VIDEO,
                            Manifest.permission.READ_MEDIA_AUDIO
                        )
                    } else {
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                    permissionLauncher.launch(permissions)
                }) {
                    Text("Grant Permissions")
                }
            }
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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

    val primaryGradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
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
                        Crossfade(
                            targetState = isSearching,
                            animationSpec = tween(ANIMATION_DURATION_MS),
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
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.semantics { contentDescription = "App Title" }
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
            Crossfade(
                targetState = selectedDestination,
                animationSpec = tween(ANIMATION_DURATION_MS),
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
fun AnimatedCardGrid(
    searchQuery: String,
    onCardClick: (String) -> Unit,
    soundEffectManager: SoundEffectManager
) {
    val cards = listOf(
        "System Config", "Network Monitor", "Security Scan", "Performance",
        "Storage Manager", "Battery Health", "App Updates", "Data Backup"
    )
    val filteredCards by remember(searchQuery) {
        derivedStateOf {
            if (searchQuery.isEmpty()) cards else cards.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }
    var isFiltering by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(searchQuery) {
        isFiltering = true
        delay(SEARCH_DEBOUNCE_MS)
        isFiltering = false
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(filteredCards.size, key = { filteredCards[it] }) { index ->
            val cardTitle = filteredCards[index]
            AnimatedCardItem(
                title = cardTitle,
                onClick = {
                    val intent = when (cardTitle) {
                        "System Config" -> Intent(context, SettingsActivity::class.java)
                        else -> Intent(context, ComingActivity::class.java).putExtra("CARD_TITLE", cardTitle)
                    }
                    intent.putExtra("transition_name", "card_$cardTitle")
                    try {
                        val activity = context as? ComponentActivity
                        activity?.let {
                            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                it,
                                Pair(View(context), "card_$cardTitle")
                            )
                            context.startActivity(intent, options.toBundle())
                        } ?: context.startActivity(intent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to start activity with transition: ${e.message}")
                        context.startActivity(intent)
                    }
                    onCardClick(cardTitle)
                },
                soundEffectManager = soundEffectManager,
                index = index
            )
        }
    }

    if (isFiltering) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
fun AnimatedCardItem(
    title: String,
    onClick: () -> Unit,
    soundEffectManager: SoundEffectManager,
    index: Int
) {
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION_MS,
            delayMillis = index * 50,
            easing = LinearOutSlowInEasing
        ),
        label = "card_animation"
    )
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .graphicsLayer(
                scaleX = scale * animatedProgress,
                scaleY = scale * animatedProgress,
                alpha = animatedProgress
            )
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current
            ) {
                soundEffectManager.playClickSound()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .semantics { contentDescription = "Card: $title" },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
fun SettingsScreen(
    soundEffectManager: SoundEffectManager,
    sharedPrefsManager: SharedPreferencesManager
) {
    val context = LocalContext.current
    var soundEnabled by remember { mutableStateOf(sharedPrefsManager.isSoundEnabled()) }
    var selectedTheme by remember { mutableStateOf<ThemeSetting>(sharedPrefsManager.getThemeSetting()) }
    var selectedLanguage by remember { mutableStateOf(sharedPrefsManager.getLanguageSetting()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column {
                SettingItem(
                    icon = Icons.Filled.VolumeUp,
                    title = "Sound Effects",
                    description = "Enable or disable sound effects for interactions",
                    soundEffectManager = soundEffectManager
                ) {
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = {
                            soundEffectManager.playClickSound()
                            soundEnabled = it
                            sharedPrefsManager.setSoundEnabled(it)
                        }
                    )
                }
                HorizontalDivider()
                SettingItem(
                    icon = Icons.Filled.ColorLens,
                    title = "Theme",
                    description = "Choose your preferred theme",
                    soundEffectManager = soundEffectManager
                ) {
                    // FIXED: Moved SegmentedButton to Composable scope
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ThemeSetting.entries.forEach { theme ->
                            OutlinedButton(
                                onClick = {
                                    soundEffectManager.playClickSound()
                                    sharedPrefsManager.setThemeSetting(theme)
                                    selectedTheme = theme
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selectedTheme == theme) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                )
                            ) {
                                Text(theme.name.replace("_", " "))
                            }
                        }
                    }
                }
                HorizontalDivider()
                SettingItem(
                    icon = Icons.AutoMirrored.Filled.ArrowLeft,
                    title = "Language",
                    description = "Select app language",
                    soundEffectManager = soundEffectManager
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = {
                                soundEffectManager.playClickSound()
                                expanded = true
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(selectedLanguage)
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select Language")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            listOf("English", "Spanish", "French", "German", "Bengali").forEach { language ->
                                DropdownMenuItem(
                                    text = { Text(language) },
                                    onClick = {
                                        soundEffectManager.playClickSound()
                                        sharedPrefsManager.setLanguageSetting(language)
                                        selectedLanguage = language
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                HorizontalDivider()
                SettingItem(
                    icon = Icons.Filled.Info,
                    title = "About",
                    description = "About this app",
                    soundEffectManager = soundEffectManager
                ) {
                    Toast.makeText(context, "App Version 1.0", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    description: String,
    soundEffectManager: SoundEffectManager,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "$title Icon",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
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
        }
        content()
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
            unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(vertical = 4.dp)
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

    val isLogoAvailable = try {
        context.resources.getResourceEntryName(R.mipmap.ic_launcher_round)
        true
    } catch (e: Exception) {
        Log.e(TAG, "Logo resource not found: ${e.message}")
        false
    }

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

    val backgroundGradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.surface
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient),
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
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 32.dp, vertical = 40.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (isLogoAvailable) {
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher_round),
                        contentDescription = stringResource(id = R.string.app_name) + " Logo",
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                            .padding(8.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "Fallback Logo",
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                            .padding(8.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
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

                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = { usernameInput = it.trim(); errorMessage = null },
                    label = { Text("Username") },
                    leadingIcon = { Icon(Icons.Outlined.AccountCircle, contentDescription = "Username Icon") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(18.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null
                )

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
                        isLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            delay(1000)
                            if (usernameInput == "admin" && passwordInput == "admin") {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                soundEffectManager.playClickSound()
                                onLoginSuccess(usernameInput)
                            } else {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                soundEffectManager.playClickSound()
                                errorMessage = "Invalid username or password. Please try again."
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
                    shape = RoundedCornerShape(18.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null
                )

                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(animationSpec = tween(ANIMATION_DURATION_MS)) + slideInVertically(initialOffsetY = { -it / 2 }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                    exit = fadeOut(animationSpec = tween(ANIMATION_DURATION_MS / 2)) + slideOutVertically(targetOffsetY = { -it / 2 }, animationSpec = spring(stiffness = Spring.StiffnessMedium))
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

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
                        errorMessage = null
                        coroutineScope.launch {
                            delay(1000)
                            if (usernameInput == "admin" && passwordInput == "admin") {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                soundEffectManager.playClickSound()
                                onLoginSuccess(usernameInput)
                            } else {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                soundEffectManager.playClickSound()
                                errorMessage = "Invalid username or password. Please try again."
                            }
                            isLoading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .height(56.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 10.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text("LOGIN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }

                TextButton(onClick = {
                    soundEffectManager.playClickSound()
                }) {
                    Text("Forgot password?", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(modifier: Modifier = Modifier, username: String, onLogout: () -> Unit, soundEffectManager: SoundEffectManager) {
    val context = LocalContext.current

    val profileBackgroundGradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
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

        Box(
            modifier = Modifier
                .size(160.dp)
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
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "User Profile Picture",
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = username.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Welcome to your personalized space!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(48.dp))

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
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
                ProfileOptionItem(
                    icon = Icons.Filled.Lock,
                    title = "Change Password",
                    description = "Secure your account with a new password.",
                    soundEffectManager = soundEffectManager
                ) {
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
                ProfileOptionItem(
                    icon = Icons.Filled.Settings,
                    title = "Privacy Settings",
                    description = "Manage your data and privacy preferences.",
                    soundEffectManager = soundEffectManager
                ) {
                }
            }
        }
        Spacer(modifier = Modifier.height(48.dp))

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
                soundEffectManager.playClickSound()
                onLogout()
            },
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha),
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout Icon")
            Spacer(Modifier.width(16.dp))
            Text("Logout", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(24.dp))
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
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "profile_item_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1.0f,
        animationSpec = tween(150),
        label = "profile_item_alpha"
    )

    val defaultIndication = LocalIndication.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
            .clickable(
                interactionSource = interactionSource,
                indication = defaultIndication,
                onClick = {
                    soundEffectManager.playClickSound()
                    onClick.invoke()
                }
            )
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "$title Icon",
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
            contentDescription = "Navigate to $title",
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
    soundEffectManager: SoundEffectManager,
    modifier: Modifier = Modifier
) {
    val destinations = listOf(Screen.Dashboard, Screen.AppSettings, Screen.Profile)
    val railWidth by animateDpAsState(
        targetValue = if (isExpanded) 200.dp else 80.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "rail_width_animation"
    )

    NavigationRail(
        modifier = modifier
            .width(railWidth)
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        IconButton(onClick = onMenuClick) {
            Icon(
                imageVector = if (isExpanded) Icons.AutoMirrored.Filled.MenuOpen else Icons.Filled.Menu,
                contentDescription = if (isExpanded) "Collapse Menu" else "Expand Menu",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        destinations.forEach { destination ->
            val isSelected = selectedDestination == destination
            NavigationRailItem(
                selected = isSelected,
                onClick = {
                    soundEffectManager.playClickSound()
                    onDestinationSelected(destination)
                },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = "${destination.label} Icon",
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = {
                    if (isExpanded) {
                        Text(
                            destination.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .animateContentSize()
            )
        }
    }
}
