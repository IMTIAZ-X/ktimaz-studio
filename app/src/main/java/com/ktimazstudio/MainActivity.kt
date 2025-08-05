package com.ktimazstudio

import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.IntSize // Import IntSize
import com.ktimazstudio.data.SecurityIssue
import com.ktimazstudio.manager.SecurityManager
import com.ktimazstudio.manager.SharedPreferencesManager
import com.ktimazstudio.manager.SoundEffectManager
import com.ktimazstudio.ui.screens.LoginScreen
import com.ktimazstudio.ui.screens.MainApplicationUI
import com.ktimazstudio.ui.screens.SecurityAlertScreen
import com.ktimazstudio.ui.theme.ktimaz
import com.ktimazstudio.util.isAppInDarkTheme
import kotlinx.coroutines.delay


@OptIn(ExperimentalAnimationApi::class, ExperimentalLayoutApi::class)
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

            val initialSecurityIssue = remember {
                securityManager.getSecurityIssue(isInspectionMode)
            }

            if (initialSecurityIssue != SecurityIssue.NONE) {
                ktimaz(darkTheme = useDarkTheme) {
                    SecurityAlertScreen(issue = initialSecurityIssue) { finishAffinity() }
                }
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

                ktimaz(darkTheme = useDarkTheme) {
                    var isLoggedIn by remember { mutableStateOf(sharedPrefsManager.isLoggedIn()) }
                    var currentUsername by remember(isLoggedIn) { mutableStateOf(sharedPrefsManager.getUsername()) }
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
                                (fadeIn(animationSpec = tween(400, delayMillis = 200)) +
                                        scaleIn(initialScale = 0.92f, animationSpec = tween(400, delayMillis = 200)))
                                    .togetherWith(
                                        fadeOut(animationSpec = tween(200)) +
                                                scaleOut(targetScale = 0.92f, animationSpec = tween(200))
                                    )
                                    .using(SizeTransform(clip = false, sizeAnimationSpec = { initialSize: IntSize, targetSize: IntSize -> spring(stiffness = Spring.StiffnessLow) }))
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
