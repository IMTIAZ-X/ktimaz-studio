package com.ktimazstudio

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.LinkProperties
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
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.ktimazstudio.ui.theme.ktimaz
import com.ktimazstudio.R

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.draw.blur

// NEW IMPORTS FOR ICONS
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.PermStorage // Added missing import for PermStorage


// ---------------------------------------------------------------------------------------------
// SharedPreferences Manager
// ---------------------------------------------------------------------------------------------
class SharedPreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "MyAppPrefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in_key"
        private const val KEY_USERNAME = "username_key"
        private const val KEY_IS_FIRST_LAUNCH = "is_first_launch_key"
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun setLoggedIn(isLoggedIn: Boolean, username: String? = null) {
        with(prefs.edit()) {
            putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
            putString(KEY_USERNAME, username)
            apply()
        }
    }

    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean(KEY_IS_FIRST_LAUNCH, true)
    }

    fun setFirstLaunch(firstLaunch: Boolean) {
        prefs.edit().putBoolean(KEY_IS_FIRST_LAUNCH, firstLaunch).apply()
    }
}

// ---------------------------------------------------------------------------------------------
// Internet Connectivity Utilities
// ---------------------------------------------------------------------------------------------
fun isConnected(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
}

fun openWifiSettings(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Intent(Settings.Panel.ACTION_WIFI)
    } else {
        Intent(Settings.ACTION_WIFI_SETTINGS)
    }
    context.startActivity(intent)
}

// ---------------------------------------------------------------------------------------------
// Security Manager
// ---------------------------------------------------------------------------------------------
class SecurityManager(private val context: Context) {

    // VPN Detection
    fun isVpnActive(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networks = connectivityManager.allNetworks
        for (network in networks) {
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                // Exclude always-on VPN services which might be part of the system
                if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN) ||
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_FOREGROUND)) {
                    val linkProperties: LinkProperties? = connectivityManager.getLinkProperties(network)
                    // Check for common VPN interface names
                    if (linkProperties?.interfaceName?.startsWith("tun") == true ||
                        linkProperties?.interfaceName?.startsWith("ppp") == true) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun registerVpnDetectionCallback(callback: (Boolean) -> Unit): ConnectivityManager.NetworkCallback {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                if (capabilities != null && !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)) {
                    callback(true)
                }
            }

            override fun onLost(network: Network) {
                callback(isVpnActive())
            }
        }
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        return networkCallback
    }

    fun unregisterVpnDetectionCallback(networkCallback: ConnectivityManager.NetworkCallback) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }


    // Debugger Detection
    fun isDebuggerConnected(): Boolean {
        return Debug.isDebuggerConnected()
    }

    fun isTracerAttached(): Boolean {
        try {
            BufferedReader(InputStreamReader(java.io.FileInputStream("/proc/self/status"))).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (line?.startsWith("TracerPid:") == true) {
                        val tracerPid = line?.substring(10)?.trim()?.toInt()
                        return tracerPid != null && tracerPid != 0
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    // Emulator Detection
    fun isRunningOnEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic") ||
                Build.PRODUCT.contains("sdk") ||
                Build.PRODUCT.contains("google_sdk") ||
                Build.PRODUCT.contains("emulator") ||
                Build.PRODUCT.contains("virtual") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                Build.HOST == "android-build" ||
                Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.HARDWARE.contains("goldfish") ||
                Build.HARDWARE.contains("ranchu") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86"))
    }

    // Root Detection
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
            if (java.io.File(path).exists()) return true
        }

        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val `in` = BufferedReader(InputStreamReader(process.inputStream))
            if (`in`.readLine() != null) return true
            return false
        } catch (e: Throwable) {
            return false
        } finally {
            process?.destroy()
        }
    }

    // APK Tampering Detection
    fun isApkTampered(): Boolean {
        val currentSignatureHash = getSignatureSha256Hash()
        return currentSignatureHash != EXPECTED_APK_HASH
    }

    private fun getSignatureSha256Hash(): String {
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

            signatures?.firstOrNull()?.let { signature ->
                val md = java.security.MessageDigest.getInstance("SHA-256")
                md.update(signature.toByteArray())
                return md.digest().joinToString("") { "%02x".format(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private val EXPECTED_APK_HASH = "YOUR_APP_RELEASE_SIGNATURE_SHA256_HASH_HERE" // IMPORTANT: Replace this placeholder!

    // Hooking Framework Detection (e.g., Xposed, Frida)
    fun isHookingFrameworkDetected(): Boolean {
        val hookingFiles = arrayOf(
            "/data/local/xposed/bin/xposed",
            "/data/app/de.robv.android.xposed.installer",
            "/system/lib/libxposed_art.so",
            "/system/framework/XposedBridge.jar",
            "/data/local/frida-inject"
        )
        for (file in hookingFiles) {
            if (java.io.File(file).exists()) return true
        }

        try {
            context.packageManager.getPackageInfo("de.robv.android.xposed.installer", 0)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            // Package not found, which is good
        }

        val props = System.getProperties()
        for (key in props.keys) {
            val value = props.getProperty(key as String)
            if (key.contains("frida", true) || value.contains("frida", true)) {
                return true
            }
        }

        return false
    }

    fun getSecurityIssue(): SecurityIssue {
        if (isVpnActive()) return SecurityIssue.VPN_ACTIVE
        if (isDebuggerConnected()) return SecurityIssue.DEBUGGER_ATTACHED
        if (isTracerAttached()) return SecurityIssue.TRACER_ATTACHED
        if (isRunningOnEmulator()) return SecurityIssue.EMULATOR_DETECTED
        if (isDeviceRooted()) return SecurityIssue.DEVICE_ROOTED
        if (isApkTampered()) return SecurityIssue.APK_TAMPERED
        if (isHookingFrameworkDetected()) return SecurityIssue.HOOKING_FRAMEWORK_DETECTED
        return SecurityIssue.NONE
    }
}

enum class SecurityIssue(val message: String) {
    NONE("No security issues detected."),
    VPN_ACTIVE("A VPN is active. For security reasons, please disable it to continue."),
    DEBUGGER_ATTACHED("A debugger is detected. Please disconnect it to proceed."),
    TRACER_ATTACHED("An unauthorized tracer is attached to the application."),
    EMULATOR_DETECTED("This application cannot run on an emulator."),
    DEVICE_ROOTED("This application cannot run on a rooted device."),
    APK_TAMPERED("Application integrity compromised. Please reinstall from a trusted source."),
    HOOKING_FRAMEWORK_DETECTED("Hooking framework detected. Please remove it to use the app.")
}

@Composable
fun SecurityAlertScreen(issue: SecurityIssue, onExit: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /* Cannot be dismissed */ },
        title = {
            Text(
                "Security Alert",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Text(
                issue.message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        confirmButton = {
            Button(
                onClick = onExit,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Exit App")
            }
        },
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    )
}


// ---------------------------------------------------------------------------------------------
// MainActivity
// ---------------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
class MainActivity : ComponentActivity() {
    private lateinit var sharedPrefsManager: SharedPreferencesManager
    private lateinit var securityManager: SecurityManager
    private var vpnNetworkCallback: ConnectivityManager.NetworkCallback? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(applicationContext, "Storage permission granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(applicationContext, "Storage permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        sharedPrefsManager = SharedPreferencesManager(applicationContext)
        securityManager = SecurityManager(applicationContext)

        val initialSecurityIssue = securityManager.getSecurityIssue()
        if (initialSecurityIssue != SecurityIssue.NONE) {
            setContent {
                ktimaz {
                    SecurityAlertScreen(issue = initialSecurityIssue) { finishAffinity() }
                }
            }
            return
        }

        setContent {
            ktimaz {
                var isLoggedIn by remember { mutableStateOf(sharedPrefsManager.isLoggedIn()) }
                var currentUsername by remember(isLoggedIn) { mutableStateOf(sharedPrefsManager.getUsername()) }
                var liveVpnDetected by remember { mutableStateOf(securityManager.isVpnActive()) }
                var currentSecurityIssue by remember { mutableStateOf(SecurityIssue.NONE) }

                var showConfigureScreen by rememberSaveable { mutableStateOf(sharedPrefsManager.isFirstLaunch()) }

                DisposableEffect(Unit) {
                    vpnNetworkCallback = securityManager.registerVpnDetectionCallback { isVpn ->
                        liveVpnDetected = isVpn
                        if (isVpn) {
                            currentSecurityIssue = SecurityIssue.VPN_ACTIVE
                        } else {
                            currentSecurityIssue = securityManager.getSecurityIssue()
                        }
                    }
                    onDispose {
                        vpnNetworkCallback?.let { securityManager.unregisterVpnDetectionCallback(it) }
                    }
                }

                LaunchedEffect(Unit) {
                    while (true) {
                        delay(5000)
                        val issue = securityManager.getSecurityIssue()
                        if (issue != SecurityIssue.NONE && issue != currentSecurityIssue) {
                            currentSecurityIssue = issue
                        } else if (currentSecurityIssue == SecurityIssue.VPN_ACTIVE && issue == SecurityIssue.NONE) {
                            currentSecurityIssue = SecurityIssue.NONE
                        }
                    }
                }

                if (currentSecurityIssue != SecurityIssue.NONE) {
                    SecurityAlertScreen(issue = currentSecurityIssue) { finishAffinity() }
                } else if (showConfigureScreen) {
                    ConfigureScreen(
                        onGrantStoragePermissionClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                                    Toast.makeText(applicationContext, "Media access already granted!", Toast.LENGTH_SHORT).show()
                                } else {
                                    requestPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                                }
                            } else {
                                if (ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                    Toast.makeText(applicationContext, "Storage permission already granted!", Toast.LENGTH_SHORT).show()
                                } else {
                                    requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                }
                            }
                        },
                        onThemeModeChanged = { mode ->
                            Toast.makeText(applicationContext, "Theme mode changed to: $mode (Not fully implemented)", Toast.LENGTH_SHORT).show()
                        },
                        onLanguageClick = {
                            Toast.makeText(applicationContext, "Language settings (Not implemented)", Toast.LENGTH_SHORT).show()
                        },
                        onPerformanceModeClick = {
                            Toast.makeText(applicationContext, "Performance mode settings (Not implemented)", Toast.LENGTH_SHORT).show()
                        },
                        onLibraryTabsClick = {
                            Toast.makeText(applicationContext, "Library tabs settings (Not implemented)", Toast.LENGTH_SHORT).show()
                        },
                        onResetClick = {
                            Toast.makeText(applicationContext, "Reset settings (Not implemented)", Toast.LENGTH_SHORT).show()
                        },
                        onConfigurationComplete = {
                            sharedPrefsManager.setFirstLaunch(false)
                            showConfigureScreen = false
                        }
                    )
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
                                }
                            )
                        } else {
                            LoginScreen(
                                onLoginSuccess = { loggedInUsername ->
                                    sharedPrefsManager.setLoggedIn(true, loggedInUsername)
                                    isLoggedIn = true
                                    sharedPrefsManager.setFirstLaunch(false)
                                    showConfigureScreen = false
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
        vpnNetworkCallback?.let { securityManager.unregisterVpnDetectionCallback(it) }
    }
}

// ---------------------------------------------------------------------------------------------
// Login Screen
// ---------------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Login Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Welcome Back!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sign in to continue",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Username Icon"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(imageVector = Icons.Filled.Key, contentDescription = "Password Icon") },
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = "Toggle password visibility")
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            delay(2000) // Simulate network delay
                            if (username == "user" && password == "password") {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLoginSuccess(username)
                            } else {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                errorMessage = "Invalid username or password"
                            }
                            isLoading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text("LOGIN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Main Application UI
// ---------------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MainApplicationUI(username: String, onLogout: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (!isConnected(context)) {
            val result = snackbarHostState.showSnackbar(
                message = "No Internet Connection!",
                actionLabel = "Open Wi-Fi",
                duration = SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                openWifiSettings(context)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ktimaz Studio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { /* Open drawer */ }) {
                        Icon(Icons.AutoMirrored.Filled.MenuOpen, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                scrollBehavior = scrollBehavior
            )
        },
        content = { paddingValues ->
            var selectedDestination by rememberSaveable { mutableStateOf(AppDestination.DASHBOARD) }

            Row(modifier = Modifier.padding(paddingValues)) {
                AppNavigationRail(
                    selectedDestination = selectedDestination,
                    onDestinationSelected = { selectedDestination = it }
                )
                AnimatedContent(
                    targetState = selectedDestination,
                    transitionSpec = {
                        slideInHorizontally(animationSpec = tween(400)) { fullWidth -> if (targetState.ordinal > initialState.ordinal) fullWidth else -fullWidth } + fadeIn(animationSpec = tween(200, delayMillis = 200)) togetherWith
                                slideOutHorizontally(animationSpec = tween(400)) { fullWidth -> if (targetState.ordinal < initialState.ordinal) fullWidth else -fullWidth } + fadeOut(animationSpec = tween(200))
                    }, label = "AppContentTransition"
                ) { targetDestination ->
                    when (targetDestination) {
                        AppDestination.DASHBOARD -> DashboardScreen(modifier = Modifier.fillMaxSize())
                        AppDestination.SETTINGS -> SettingsScreen(modifier = Modifier.fillMaxSize())
                        AppDestination.PROFILE -> ProfileScreen(username = username, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    )
}


enum class AppDestination {
    DASHBOARD, SETTINGS, PROFILE
}

@Composable
fun AppNavigationRail(
    selectedDestination: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit
) {
    NavigationRail(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            .padding(vertical = 8.dp),
        containerColor = Color.Transparent,
        header = {
            // Optional header content
        }
    ) {
        Spacer(Modifier.height(16.dp))
        NavigationRailItem(
            selected = selectedDestination == AppDestination.DASHBOARD,
            onClick = { onDestinationSelected(AppDestination.DASHBOARD) },
            icon = {
                Icon(
                    imageVector = if (selectedDestination == AppDestination.DASHBOARD) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                    contentDescription = "Dashboard"
                )
            },
            label = { Text("Dashboard") },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.secondaryContainer
            )
        )
        NavigationRailItem(
            selected = selectedDestination == AppDestination.SETTINGS,
            onClick = { onDestinationSelected(AppDestination.SETTINGS) },
            icon = {
                Icon(
                    imageVector = if (selectedDestination == AppDestination.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "Settings"
                )
            },
            label = { Text("Settings") },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.secondaryContainer
            )
        )
        NavigationRailItem(
            selected = selectedDestination == AppDestination.PROFILE,
            onClick = { onDestinationSelected(AppDestination.PROFILE) },
            icon = {
                Icon(
                    imageVector = if (selectedDestination == AppDestination.PROFILE) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile") },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.secondaryContainer
            )
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Dashboard Screen
// ---------------------------------------------------------------------------------------------
@Composable
fun DashboardScreen(modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val cardTitles = remember {
        listOf(
            "Quick Access", "Recent Files", "Favorites", "Categories",
            "Storage", "Cloud Sync", "Recycle Bin", "Share"
        )
    }

    // Replaced painterResource with ImageVector from Material Icons
    val cardIcons = remember {
        listOf(
            Icons.Filled.MailOutline, // Placeholder for Quick Access
            Icons.Filled.Close, // Placeholder for Recent Files
            Icons.Filled.Info, // Placeholder for Favorites
            Icons.Filled.Dashboard, // Placeholder for Categories
            Icons.Filled.PermStorage, // Placeholder for Storage - THIS IS WHERE PERMSTORAGE IS USED
            Icons.Filled.Settings, // Placeholder for Cloud Sync
            Icons.Filled.Delete, // Placeholder for Recycle Bin (requires import)
            Icons.Filled.Share // Placeholder for Share (requires import)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        AnimatedCardGrid(
            titles = cardTitles,
            // Pass ImageVector directly instead of Painter
            icons = cardIcons,
            onCardClick = { title ->
                Toast.makeText(context, "$title clicked!", Toast.LENGTH_SHORT).show()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Animated Card Grid
// ---------------------------------------------------------------------------------------------
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedCardGrid(
    titles: List<String>,
    icons: List<ImageVector>, // Changed to ImageVector
    onCardClick: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(titles) { index, title ->
            val animatedProgress = remember { Animatable(0f) }

            LaunchedEffect(Unit) {
                delay(index * 100L)
                animatedProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 500, easing = EaseOutExpo)
                )
            }

            val scale by animateFloatAsState(
                targetValue = if (animatedProgress.value > 0) 1f else 0.8f,
                animationSpec = tween(durationMillis = 500, easing = EaseOutExpo), label = ""
            )
            val alpha by animateFloatAsState(
                targetValue = animatedProgress.value,
                animationSpec = tween(durationMillis = 500, easing = LinearEasing), label = ""
            )

            Box(
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        alpha = alpha
                    )
                    .clickable { onCardClick(title) }
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .graphicsLayer(scaleX = scale, scaleY = scale)
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
                            imageVector = icons[index % icons.size], // Changed from 'painter' to 'imageVector'
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


// ---------------------------------------------------------------------------------------------
// Settings Screen
// ---------------------------------------------------------------------------------------------
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "App Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "General",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                SettingItem(
                    icon = Icons.Filled.Notifications,
                    title = "Notifications",
                    description = "Control app notifications",
                    onClick = { Toast.makeText(context, "Notifications clicked", Toast.LENGTH_SHORT).show() }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingItem(
                    icon = Icons.Filled.AccountCircle,
                    title = "Account Preferences",
                    description = "Manage your account settings",
                    onClick = { Toast.makeText(context, "Account Preferences clicked", Toast.LENGTH_SHORT).show() }
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                SettingItem(
                    icon = Icons.Filled.Info,
                    title = "About App",
                    description = "Version, build information",
                    onClick = {
                        Toast.makeText(context, "App Version: 1.0.0 (Build 123)", Toast.LENGTH_LONG).show()
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingItem(
                    icon = Icons.Filled.PrivacyTip,
                    title = "Privacy Policy",
                    description = "Read our privacy statement",
                    onClick = { Toast.makeText(context, "Privacy Policy clicked", Toast.LENGTH_SHORT).show() }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingItem(
                    icon = Icons.Filled.Palette,
                    title = "Changelog",
                    description = "See what's new",
                    onClick = { Toast.makeText(context, "Changelog clicked", Toast.LENGTH_SHORT).show() }
                )
            }
        }
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    description: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            description?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = "Navigate",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Profile Screen
// ---------------------------------------------------------------------------------------------
@Composable
fun ProfileScreen(username: String, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = "Profile Picture",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = username,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "user@example.com",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Account Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                SettingItem(
                    icon = Icons.Filled.AccountCircle,
                    title = "Edit Profile",
                    description = "Update your personal information",
                    onClick = { Toast.makeText(context, "Edit Profile clicked", Toast.LENGTH_SHORT).show() }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingItem(
                    icon = Icons.Filled.Lock,
                    title = "Change Password",
                    description = "Update your login password",
                    onClick = { Toast.makeText(context, "Change Password clicked", Toast.LENGTH_SHORT).show() }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingItem(
                    icon = Icons.Filled.PrivacyTip,
                    title = "Privacy Settings",
                    description = "Manage data and privacy options",
                    onClick = { Toast.makeText(context, "Privacy Settings clicked", Toast.LENGTH_SHORT).show() }
                )
            }
        }
    }
}


// ---------------------------------------------------------------------------------------------
// NEW: Configure Screen
// ---------------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigureScreen(
    modifier: Modifier = Modifier,
    onGrantStoragePermissionClick: () -> Unit = {},
    onThemeModeChanged: (Int) -> Unit = {},
    onLanguageClick: () -> Unit = {},
    onPerformanceModeClick: () -> Unit = {},
    onLibraryTabsClick: () -> Unit = {},
    onResetClick: () -> Unit = {},
    onConfigurationComplete: () -> Unit = {}
) {
    var selectedThemeMode by remember { mutableIntStateOf(0) }

    var isStorageGranted by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .widthIn(max = 600.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = "Configure Icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Configure",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(onClick = onResetClick) {
                            Icon(
                                imageVector = Icons.Filled.Loop,
                                contentDescription = "Reset",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "Setup first startup",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 44.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    ConfigureSettingItem(
                        icon = Icons.Filled.Palette,
                        title = "Theme Mode",
                        description = "",
                        control = {
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier.height(48.dp)
                            ) {
                                SegmentedButton(
                                    selected = selectedThemeMode == 0,
                                    onClick = {
                                        selectedThemeMode = 0
                                        onThemeModeChanged(0)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Tune,
                                        contentDescription = "System Theme",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                SegmentedButton(
                                    selected = selectedThemeMode == 1,
                                    onClick = {
                                        selectedThemeMode = 1
                                        onThemeModeChanged(1)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Palette,
                                        contentDescription = "Light Theme",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                SegmentedButton(
                                    selected = selectedThemeMode == 2,
                                    onClick = {
                                        selectedThemeMode = 2
                                        onThemeModeChanged(2)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ModeNight,
                                        contentDescription = "Dark Theme",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp))

                    ConfigureSettingItem(
                        icon = Icons.Filled.Tune,
                        title = "Language",
                        description = "English",
                        control = {
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = "Change Language",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        onClick = onLanguageClick
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp))

                    ConfigureSettingItem(
                        icon = Icons.Filled.Speed,
                        title = "Performance mode",
                        description = "Balanced",
                        control = {
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = "Change Performance Mode",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        onClick = onPerformanceModeClick
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp))

                    ConfigureSettingItem(
                        icon = Icons.Filled.Tune,
                        title = "Library Tabs",
                        description = "6",
                        control = {
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = "Change Library Tabs",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        onClick = onLibraryTabsClick
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .padding(start = 4.dp)
                    ) {
                        Text(
                            text = "Use Media Store",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "✓ instant indexing time",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clickable(onClick = onGrantStoragePermissionClick)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.PermStorage, // THIS IS WHERE PERMSTORAGE IS USED
                                    contentDescription = "Storage Icon",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Grant Storage Permission",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isStorageGranted,
                                    onClick = onGrantStoragePermissionClick,
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary,
                                        unselectedColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                FilledIconButton(
                                    onClick = onGrantStoragePermissionClick,
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ChevronRight,
                                        contentDescription = "Proceed",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onConfigurationComplete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("DONE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun ConfigureSettingItem(
    icon: ImageVector,
    title: String,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    control: @Composable (() -> Unit)? = null
) {
    val itemModifier = Modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(vertical = 8.dp)

    Row(
        modifier = itemModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.padding(end = 16.dp).size(24.dp), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            if (description != null && description.isNotBlank()) {
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


// ---------------------------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------------------------
@Preview(showBackground = true, widthDp = 360)
@Composable
fun PreviewLoginScreen() {
    ktimaz {
        LoginScreen(onLoginSuccess = {})
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun PreviewMainApplicationUI() {
    ktimaz {
        MainApplicationUI(username = "ktimaz", onLogout = {})
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun PreviewDashboardScreen() {
    ktimaz {
        DashboardScreen()
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun PreviewSettingsScreen() {
    ktimaz {
        SettingsScreen()
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun PreviewProfileScreen() {
    ktimaz {
        ProfileScreen(username = "ktimaz")
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun PreviewConfigureScreen() {
    ktimaz {
        ConfigureScreen()
    }
}
