package com.ktimazstudio.managers

import android.content.Context
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.ktimazstudio.enums.SecurityIssue
import com.ktimazstudio.enums.NavigationStyle
import com.ktimazstudio.ui.screens.SecurityAlertScreen
import com.ktimazstudio.ui.screens.LoginScreen
import com.ktimazstudio.ui.MainApplicationUI
import com.ktimazstudio.old.ui.OldMainApplicationUI
import com.ktimazstudio.utils.isAppInDarkTheme
import com.ktimazstudio.utils.EnhancedSecurityManager

/**
 * Central application manager that handles all core application logic
 * Moved from MainActivity for better separation of concerns
 */
class ApplicationManager(
    private val activity: ComponentActivity,
    private val scope: CoroutineScope
) {
    // Core managers
    private val sharedPrefsManager = SharedPreferencesManager(activity.applicationContext)
    private val soundEffectManager = SoundEffectManager(activity.applicationContext, sharedPrefsManager)
    private val securityManager = EnhancedSecurityManager(activity.applicationContext)
    
    // State flows for reactive UI
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme
    
    private val _isLoggedIn = MutableStateFlow(sharedPrefsManager.isLoggedIn())
    private val _currentUsername = MutableStateFlow(sharedPrefsManager.getUsername())
    private val _currentSecurityIssue = MutableStateFlow(SecurityIssue.NONE)
    private val _isOldUiEnabled = MutableStateFlow(sharedPrefsManager.isOldUiEnabled())
    
    init {
        initializeApplication()
    }
    
    private fun initializeApplication() {
        // Load sounds
        soundEffectManager.loadSounds()
        
        // Setup theme listener
        setupThemeListener()
        
        // Start security monitoring
        startSecurityMonitoring()
        
        // Setup old UI listener
        setupOldUiListener()
    }
    
    private fun setupThemeListener() {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == SharedPreferencesManager.KEY_THEME_SETTING) {
                updateTheme()
            }
        }
        sharedPrefsManager.prefs.registerOnSharedPreferenceChangeListener(listener)
    }
    
    private fun setupOldUiListener() {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == SharedPreferencesManager.KEY_OLD_UI_ENABLED) {
                _isOldUiEnabled.value = sharedPrefsManager.isOldUiEnabled()
            }
        }
        sharedPrefsManager.prefs.registerOnSharedPreferenceChangeListener(listener)
    }
    
    private fun updateTheme() {
        // This will be called from Compose context
        scope.launch {
            // Theme update handled in Compose
        }
    }
    
    private fun startSecurityMonitoring() {
        // Initial security check
        scope.launch {
            val initialIssue = securityManager.getSecurityIssue(false)
            _currentSecurityIssue.value = initialIssue
        }
        
        // Periodic security checks
        scope.launch {
            while (true) {
                delay(5000) // Check every 5 seconds
                val issue = securityManager.getSecurityIssue(false)
                if (issue != SecurityIssue.NONE) {
                    _currentSecurityIssue.value = issue
                }
            }
        }
    }
    
    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun ComposeApplication() {
        val context = LocalContext.current
        val isInspectionMode = LocalInspectionMode.current
        
        // Update theme in composition
        val currentThemeSetting = remember { mutableStateOf(sharedPrefsManager.getThemeSetting()) }
        val useDarkTheme = isAppInDarkTheme(currentThemeSetting.value, context)
        
        LaunchedEffect(useDarkTheme) {
            _isDarkTheme.value = useDarkTheme
        }
        
        // Collect states
        val isLoggedIn by _isLoggedIn.collectAsState()
        val currentUsername by _currentUsername.collectAsState()
        val currentSecurityIssue by _currentSecurityIssue.collectAsState()
        val isOldUiEnabled by _isOldUiEnabled.collectAsState()
        
        // Handle security issues
        if (currentSecurityIssue != SecurityIssue.NONE && !isInspectionMode) {
            SecurityAlertScreen(issue = currentSecurityIssue) { 
                activity.finishAffinity() 
            }
            return
        }
        
        // Main application content with smooth transitions
        AnimatedContent(
            targetState = Pair(isLoggedIn, isOldUiEnabled),
            transitionSpec = {
                fadeIn(animationSpec = tween(400, delayMillis = 200)) +
                        scaleIn(initialScale = 0.92f, animationSpec = tween(400, delayMillis = 200)) togetherWith
                        fadeOut(animationSpec = tween(200)) +
                        scaleOut(targetScale = 0.92f, animationSpec = tween(200))
            },
            label = "main_app_transition"
        ) { (loggedIn, oldUi) ->
            when {
                !loggedIn -> {
                    LoginScreen(
                        onLoginSuccess = { username ->
                            sharedPrefsManager.setLoggedIn(true, username)
                            _isLoggedIn.value = true
                            _currentUsername.value = username
                        },
                        soundEffectManager = soundEffectManager
                    )
                }
                oldUi -> {
                    // Old UI from the old folder
                    OldMainApplicationUI(
                        username = currentUsername ?: "User",
                        onLogout = {
                            sharedPrefsManager.setLoggedIn(false)
                            _isLoggedIn.value = false
                            _currentUsername.value = null
                        },
                        soundEffectManager = soundEffectManager,
                        sharedPrefsManager = sharedPrefsManager
                    )
                }
                else -> {
                    // New enhanced UI
                    MainApplicationUI(
                        username = currentUsername ?: "User",
                        onLogout = {
                            sharedPrefsManager.setLoggedIn(false)
                            _isLoggedIn.value = false
                            _currentUsername.value = null
                        },
                        soundEffectManager = soundEffectManager,
                        sharedPrefsManager = sharedPrefsManager
                    )
                }
            }
        }
    }
    
    fun cleanup() {
        soundEffectManager.release()
    }
}