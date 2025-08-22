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
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.ktimazstudio.enums.SecurityIssue
import com.ktimazstudio.enums.NavigationStyle
import com.ktimazstudio.ui.screens.SecurityAlertScreen
import com.ktimazstudio.ui.screens.LoginScreen
import com.ktimazstudio.ui.MainApplicationUI
import com.ktimazstudio.old.ui.OldMainApplicationUI
import com.ktimazstudio.utils.isAppInDarkTheme
import com.ktimazstudio.utils.EnhancedSecurityManager
import com.ktimazstudio.utils.CryptoUtils

/**
 * Enhanced Application Manager with advanced features
 * Handles authentication, security, biometrics, and application state
 */
class ApplicationManager(
    private val activity: ComponentActivity,
    private val scope: CoroutineScope
) {
    // Core managers
    private val sharedPrefsManager = SharedPreferencesManager(activity)
    private val securityManager = SecurityManager(activity)
    private val soundEffectManager = SoundEffectManager(activity, sharedPrefsManager)
    
    // Security state
    private val _currentSecurityIssue = MutableStateFlow(SecurityIssue.NONE)
    val currentSecurityIssue: StateFlow<SecurityIssue> = _currentSecurityIssue.asStateFlow()
    
    // Authentication state
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    private val _currentUser = MutableStateFlow<String?>(null)
    val currentUser: StateFlow<String?> = _currentUser.asStateFlow()
    
    // Biometric authentication
    private var biometricPrompt: BiometricPrompt? = null
    private var biometricPromptInfo: BiometricPrompt.PromptInfo? = null

    // UI states
    private val _isOldUiEnabled = MutableStateFlow(sharedPrefsManager.isOldUiEnabled())
    val isOldUiEnabled: StateFlow<Boolean> = _isOldUiEnabled.asStateFlow()

    init {
        // Load sound effects on app start
        soundEffectManager.loadSounds()
        
        // Initialize biometric prompt
        if (canUseBiometric()) {
            initializeBiometricAuth()
        }
        
        // Restore login state
        if (sharedPrefsManager.isLoggedIn()) {
            _isLoggedIn.value = true
            _currentUser.value = sharedPrefsManager.getUsername()
        }
        
        // Start continuous security checks
        scope.launch {
            while (true) {
                val issue = securityManager.performSecurityCheck(isInspectionMode = false)
                _currentSecurityIssue.value = issue
                if (issue != SecurityIssue.NONE) {
                    // Handle critical security issue, e.g., show a dialog
                }
                delay(5000)
            }
        }
    }
    
    fun getApplicationStats(): ApplicationStats {
        return ApplicationStats(
            isLoggedIn = isLoggedIn.value,
            currentUser = currentUser.value,
            securityStatus = securityManager.getSecurityStatus(),
            isOldUiEnabled = isOldUiEnabled.value,
            isBiometricEnabled = sharedPrefsManager.isBiometricEnabled(),
            lastLoginTime = sharedPrefsManager.getLastLoginTime()
        )
    }

    fun login(username: String) {
        sharedPrefsManager.setLoggedIn(true, username)
        _isLoggedIn.value = true
        _currentUser.value = username
    }
    
    fun logout() {
        sharedPrefsManager.setLoggedIn(false, null)
        _isLoggedIn.value = false
        _currentUser.value = null
    }

    fun canUseBiometric(): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    private fun initializeBiometricAuth() {
        val executor = ContextCompat.getMainExecutor(activity)
        biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Handle error
                }
                
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Authentication successful
                    sharedPrefsManager.setLoggedIn(true, "BiometricUser")
                    _isLoggedIn.value = true
                    _currentUser.value = "BiometricUser"
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Authentication failed
                }
            })
        
        biometricPromptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password")
            .build()
    }
    
    fun promptBiometricLogin() {
        biometricPromptInfo?.let { promptInfo ->
            biometricPrompt?.authenticate(promptInfo)
        }
    }

    /**
     * Toggles between the old and enhanced UI
     */
    fun toggleOldUi(enabled: Boolean) {
        sharedPrefsManager.setOldUiEnabled(enabled)
        _isOldUiEnabled.value = enabled
    }

    /**
     * Export user settings for backup
     */
    fun exportUserSettings(): Map<String, Any?> {
        return sharedPrefsManager.exportSettings()
    }

    /**
     * Import user settings from a backup
     */
    fun importUserSettings(settings: Map<String, Any?>) {
        sharedPrefsManager.importSettings(settings)
        // Refresh UI states
        _isOldUiEnabled.value = sharedPrefsManager.isOldUiEnabled()
    }
    
    /**
     * Enable/disable biometric authentication
     */
    fun setBiometricEnabled(enabled: Boolean) {
        if (enabled && !canUseBiometric()) {
            return // Cannot enable if biometric is not available
        }
        
        sharedPrefsManager.setBiometricEnabled(enabled)
        if (enabled) {
            initializeBiometricAuth()
        }
    }
    
    /**
     * Force security check
     */
    fun performSecurityCheck() {
        scope.launch {
            val issue = securityManager.getSecurityIssue(false)
            _currentSecurityIssue.value = issue
        }
    }
    
    /**
     * Clear all application data (factory reset)
     */
    fun clearAllData() {
        logout()
        sharedPrefsManager.clearAllData()
        // In real app, also clear databases, files, etc.
    }
    
    fun cleanup() {
        soundEffectManager.release()
        securityManager.cleanup()
    }
    
    /**
     * Application statistics data class
     */
    data class ApplicationStats(
        val isLoggedIn: Boolean,
        val currentUser: String?,
        val securityStatus: SecurityManager.SecurityStatus,
        val isOldUiEnabled: Boolean,
        val isBiometricEnabled: Boolean,
        val lastLoginTime: Long
    )
}