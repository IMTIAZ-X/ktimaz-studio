package com.ktimazstudio

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import com.ktimazstudio.ui.theme.ktimaz
import com.ktimazstudio.managers.SharedPreferencesManager
import com.ktimazstudio.managers.SoundEffectManager
import com.ktimazstudio.managers.SecurityManager
import com.ktimazstudio.enums.SecurityIssue
import com.ktimazstudio.ui.screens.SecurityAlertScreen
import com.ktimazstudio.ui.screens.LoginScreen
import com.ktimazstudio.ui.MainApplicationUI
import com.ktimazstudio.utils.isAppInDarkTheme

import com.ktimazstudio.managers.new.EnhancedSharedPreferencesManager

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
class MainActivity : ComponentActivity() {
    private lateinit var sharedPrefsManager: EnhancedSharedPreferencesManager
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

        setContent {
            val context = LocalContext.current
            val isInspectionMode = LocalInspectionMode.current
            // Determine if the app should be in dark theme based on setting
            val currentThemeSetting = remember { mutableStateOf(sharedPrefsManager.getThemeSetting()) }
            val useDarkTheme = isAppInDarkTheme(currentThemeSetting.value, context, sharedPrefsManager)

            // Perform initial security checks, passing isInspectionMode
            val initialSecurityIssue: SecurityIssue = remember {
                securityManager.getSecurityIssue(isInspectionMode)
            }

            if (initialSecurityIssue != SecurityIssue.NONE) {
                ktimaz(darkTheme = useDarkTheme) {
                    SecurityAlertScreen(issue = initialSecurityIssue) { finishAffinity() }
                }
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

                ktimaz(darkTheme = useDarkTheme,sharedPrefsManager = sharedPrefsManager) {
                    var isLoggedIn by remember { mutableStateOf(sharedPrefsManager.isLoggedIn()) }
                    var currentUsername by remember(isLoggedIn) { mutableStateOf(sharedPrefsManager.getUsername()) }
                    var liveVpnDetected by remember { mutableStateOf(securityManager.isVpnActive()) }
                    var currentSecurityIssue by remember { mutableStateOf(SecurityIssue.NONE) }

                    // Live VPN detection
                    DisposableEffect(Unit) {
                        vpnNetworkCallback = securityManager.registerVpnDetectionCallback { isVpn: Boolean ->
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
                            vpnNetworkCallback?.let { callback: ConnectivityManager.NetworkCallback -> 
                                securityManager.unregisterVpnDetectionCallback(callback) 
                            }
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
        vpnNetworkCallback?.let { callback: ConnectivityManager.NetworkCallback -> 
            securityManager.unregisterVpnDetectionCallback(callback) 
        }
        soundEffectManager.release() // Release SoundPool resources
    }
}