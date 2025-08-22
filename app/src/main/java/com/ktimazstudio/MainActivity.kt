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

class MainActivity : ComponentActivity() {
    
    private lateinit var applicationManager: ApplicationManager
    private var splashScreenDismissed = false
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        handlePermissionResults(permissions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        configureSplashScreen(splashScreen)
        initializeSecurity()
        
        applicationManager = ApplicationManager(this, lifecycleScope)
        requestRuntimePermissions()
        
        intent?.let { handleDeepLink(it) }
        
        setContent {
            val systemUiController = rememberSystemUiController()
            val isDarkTheme by applicationManager.isDarkTheme.collectAsState()
            
            LaunchedEffect(isDarkTheme) {
                systemUiController.setSystemBarsColor(
                    color = androidx.compose.ui.graphics.Color.Transparent,
                    darkIcons = !isDarkTheme
                )
            }
            
            ktimaz(darkTheme = isDarkTheme) {
                applicationManager.ComposeApplication()
            }
        }
    }

    // FIXED: Corrected onNewIntent signature
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    override fun onResume() {
        super.onResume()
        applicationManager.performSecurityCheck()
    }

    override fun onDestroy() {
        super.onDestroy()
        applicationManager.cleanup()
    }
    
    private fun configureSplashScreen(splashScreen: androidx.core.splashscreen.SplashScreen) {
        splashScreen.setKeepOnScreenCondition { !splashScreenDismissed }
        
        lifecycleScope.launch {
            kotlinx.coroutines.delay(1500)
            splashScreenDismissed = true
        }
    }
    
    private fun initializeSecurity() {
        val secureHeaders = NetworkSecurity.generateSecureHeaders(this)
        println("Security initialized with headers: ${secureHeaders.keys}")
    }
    
    private fun requestRuntimePermissions() {
        val permissions = mutableListOf<String>()
        
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
        
        permissions.add(android.Manifest.permission.USE_BIOMETRIC)
        
        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }
    
    private fun handlePermissionResults(permissions: Map<String, Boolean>) {
        val deniedPermissions = permissions.filter { !it.value }
        if (deniedPermissions.isNotEmpty()) {
            println("Denied permissions: ${deniedPermissions.keys}")
        }
    }
    
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
        
        intent.extras?.let { extras ->
            handleIntentExtras(extras)
        }
    }
    
    private fun handleWebDeepLink(path: String?) {
        when (path) {
            "/settings" -> {
                lifecycleScope.launch {
                    kotlinx.coroutines.delay(2000)
                    startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                }
            }
        }
    }
    
    private fun handleAppDeepLink(path: String?) {
        when (path) {
            "/module" -> {
                val moduleId = intent.getStringExtra("module_id")
                // Handle module deep link
            }
        }
    }
    
    private fun handleIntentExtras(extras: Bundle) {
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
        when (action) {
            "open_settings" -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
    }
    
    private fun handleWidgetAction(action: String?) {
        when (action) {
            "launch_module" -> {
                val moduleId = intent.getStringExtra("module_id")
                // Launch specific module
            }
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val appStats = applicationManager.getApplicationStats()
        outState.putBoolean("was_logged_in", appStats.isLoggedIn)
        outState.putString("current_user", appStats.currentUser)
        outState.putBoolean("old_ui_enabled", appStats.isOldUiEnabled)
    }
    
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Restore application state if needed
    }
}
