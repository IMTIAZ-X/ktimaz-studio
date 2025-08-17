package com.ktimazstudio

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.ktimazstudio.managers.ApplicationManager
import com.ktimazstudio.ui.theme.ktimaz
import com.ktimazstudio.utils.NetworkSecurity
import kotlinx.coroutines.launch

/**
 * Enhanced MainActivity with advanced security, splash screen, and system UI control
 * Implements modern Android practices and security hardening
 */
class MainActivity : ComponentActivity() {
    
    private lateinit var applicationManager: ApplicationManager
    private var splashScreenDismissed = false
    
    // Permission launcher for runtime permissions
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        handlePermissionResults(permissions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before calling super.onCreate()
        val splashScreen = installSplashScreen()
        
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Configure splash screen
        configureSplashScreen(splashScreen)
        
        // Security initialization
        initializeSecurity()
        
        // Initialize application manager
        applicationManager = ApplicationManager(this, lifecycleScope)
        
        // Request necessary permissions
        requestRuntimePermissions()
        
        // Handle deep links - fixed null check
        intent?.let { handleDeepLink(it) }
        
        setContent {
            // System UI controller for immersive experience
            val systemUiController = rememberSystemUiController()
            val isDarkTheme by applicationManager.isDarkTheme.collectAsState()
            
            // Configure system bars
            LaunchedEffect(isDarkTheme) {
                systemUiController.setSystemBarsColor(
                    color = androidx.compose.ui.graphics.Color.Transparent,
                    darkIcons = !isDarkTheme
                )
            }
            
            ktimaz(darkTheme = isDarkTheme) {
                // Enhanced application UI with security checks
                applicationManager.ComposeApplication()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleDeepLink(it) }
    }

    override fun onResume() {
        super.onResume()
        // Perform security check on app resume
        applicationManager.performSecurityCheck()
    }

    override fun onDestroy() {
        super.onDestroy()
        applicationManager.cleanup()
    }
    
    /**
     * Configure splash screen behavior
     */
    private fun configureSplashScreen(splashScreen: androidx.core.splashscreen.SplashScreen) {
        splashScreen.setKeepOnScreenCondition { !splashScreenDismissed }
        
        // Dismiss splash screen after initialization
        lifecycleScope.launch {
            kotlinx.coroutines.delay(1500) // Minimum display time
            splashScreenDismissed = true
        }
    }
    
    /**
     * Initialize security measures
     */
    private fun initializeSecurity() {
        // Generate secure network headers for future requests
        val secureHeaders = NetworkSecurity.generateSecureHeaders(this)
        
        // Log security initialization (in production, send to analytics)
        println("Security initialized with headers: ${secureHeaders.keys}")
    }
    
    /**
     * Request runtime permissions
     */
    private fun requestRuntimePermissions() {
        val permissions = mutableListOf<String>()
        
        // Add permissions based on Android version
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.addAll(listOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO,
                android.Manifest.permission.READ_MEDIA_AUDIO,
                android.Manifest.permission.POST_NOTIFICATIONS
            ))
        } else {
            permissions.addAll(listOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
        }
        
        // Add biometric permission
        permissions.add(android.Manifest.permission.USE_BIOMETRIC)
        
        // Launch permission request
        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }
    
    /**
     * Handle permission request results
     */
    private fun handlePermissionResults(permissions: Map<String, Boolean>) {
        val deniedPermissions = permissions.filter { !it.value }
        
        if (deniedPermissions.isNotEmpty()) {
            // Handle denied permissions
            showPermissionRationale(deniedPermissions.keys.toList())
        }
    }
    
    /**
     * Show rationale for denied permissions
     */
    private fun showPermissionRationale(deniedPermissions: List<String>) {
        // In a real app, show a dialog explaining why permissions are needed
        println("Denied permissions: $deniedPermissions")
    }
    
    /**
     * Handle deep links and intent data
     */
    private fun handleDeepLink(intent: Intent) {
        intent.data?.let { uri ->
            when (uri.host) {
                "ktimazstudio.com" -> {
                    handleWebDeepLink(uri.path)
                }
                "app" -> {
                    handleAppDeepLink(uri.path)
                }
            }
        }
        
        // Handle other intent extras
        intent.extras?.let { extras ->
            handleIntentExtras(extras)
        }
    }
    
    private fun handleWebDeepLink(path: String?) {
        // Handle web deep links
        when (path) {
            "/settings" -> {
                // Navigate to settings when app is ready
                lifecycleScope.launch {
                    // Wait for app initialization
                    kotlinx.coroutines.delay(2000)
                    startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                }
            }
            "/profile" -> {
                // Navigate to profile
            }
        }
    }
    
    private fun handleAppDeepLink(path: String?) {
        // Handle app-specific deep links
        when (path) {
            "/module" -> {
                val moduleId = intent.getStringExtra("module_id")
                // Handle module deep link
            }
        }
    }
    
    private fun handleIntentExtras(extras: Bundle) {
        // Handle notification taps, widget interactions, etc.
        when {
            extras.containsKey("notification_action") -> {
                val action = extras.getString("notification_action")
                handleNotificationAction(action)
            }
            extras.containsKey("widget_action") -> {
                val action = extras.getString("widget_action")
                handleWidgetAction(action)
            }
        }
    }
    
    private fun handleNotificationAction(action: String?) {
        // Handle notification actions
        when (action) {
            "open_settings" -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            "quick_action" -> {
                // Perform quick action
            }
        }
    }
    
    private fun handleWidgetAction(action: String?) {
        // Handle home screen widget actions
        when (action) {
            "launch_module" -> {
                val moduleId = intent.getStringExtra("module_id")
                // Launch specific module
            }
        }
    }
    
    /**
     * Save application state
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        
        // Save important application state
        val appStats = applicationManager.getApplicationStats()
        outState.putBoolean("was_logged_in", appStats.isLoggedIn)
        outState.putString("current_user", appStats.currentUser)
        outState.putBoolean("old_ui_enabled", appStats.isOldUiEnabled)
    }
    
    /**
     * Restore application state
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        
        // Restore application state if needed
        val wasLoggedIn = savedInstanceState.getBoolean("was_logged_in", false)
        val currentUser = savedInstanceState.getString("current_user")
        val oldUiEnabled = savedInstanceState.getBoolean("old_ui_enabled", false)
        
        // Apply restored state through application manager if necessary
    }
}
