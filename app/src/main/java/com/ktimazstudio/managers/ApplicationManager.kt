package com.ktimazstudio.managers

import android.content.Context
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Support
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import com.ktimazstudio.ui.screens.SecurityAlertScreen
import com.ktimazstudio.ui.screens.LoginScreen
import com.ktimazstudio.ui.MainApplicationUI
import com.ktimazstudio.old.ui.OldMainApplicationUI
import com.ktimazstudio.utils.isAppInDarkTheme
import com.ktimazstudio.utils.EnhancedSecurityManager

/**
 * Enhanced Application Manager with advanced features
 * Handles authentication, security, biometrics, and application state
 */
class ApplicationManager(
    private val activity: ComponentActivity,
    private val scope: CoroutineScope
) {
    // Core managers
    private val sharedPrefsManager = SharedPreferencesManager(activity.applicationContext)
    private val soundEffectManager = SoundEffectManager(activity.applicationContext, sharedPrefsManager)
    private val securityManager = EnhancedSecurityManager(activity.applicationContext)
    
    // Biometric authentication
    private val biometricManager = BiometricManager.from(activity)
    private var biometricPrompt: BiometricPrompt? = null
    
    // State flows for reactive UI
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()
    
    private val _isLoggedIn = MutableStateFlow(sharedPrefsManager.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    private val _currentUsername = MutableStateFlow(sharedPrefsManager.getUsername())
    val currentUsername: StateFlow<String?> = _currentUsername.asStateFlow()
    
    private val _currentSecurityIssue = MutableStateFlow(SecurityIssue.NONE)
    val currentSecurityIssue: StateFlow<SecurityIssue> = _currentSecurityIssue.asStateFlow()
    
    private val _isOldUiEnabled = MutableStateFlow(sharedPrefsManager.isOldUiEnabled())
    val isOldUiEnabled: StateFlow<Boolean> = _isOldUiEnabled.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _authenticationRequired = MutableStateFlow(false)
    val authenticationRequired: StateFlow<Boolean> = _authenticationRequired.asStateFlow()

    init {
        initializeApplication()
    }
    
    private fun initializeApplication() {
        scope.launch {
            _isLoading.value = true
            
            try {
                // Load sounds with error handling
                try {
                    soundEffectManager.loadSounds()
                } catch (e: Exception) {
                    // Continue without sound if loading fails
                }
                
                // Setup theme listener
                setupThemeListener()
                
                // Setup UI preference listeners
                setupUiPreferenceListeners()
                
                // Initialize biometric authentication
                initializeBiometricAuth()
                
                // Start security monitoring
                startSecurityMonitoring()
                
                // Check for auto-login or biometric authentication
                checkAuthenticationState()
                
            } catch (e: Exception) {
                // Handle initialization errors gracefully
                _isLoading.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun setupThemeListener() {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == SharedPreferencesManager.KEY_THEME_SETTING) {
                updateTheme()
            }
        }
        sharedPrefsManager.prefs.registerOnSharedPreferenceChangeListener(listener)
    }
    
    private fun setupUiPreferenceListeners() {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                SharedPreferencesManager.KEY_OLD_UI_ENABLED -> {
                    _isOldUiEnabled.value = sharedPrefsManager.isOldUiEnabled()
                }
            }
        }
        sharedPrefsManager.prefs.registerOnSharedPreferenceChangeListener(listener)
    }
    
    private fun updateTheme() {
        scope.launch {
            // Theme update will be handled in Compose context
        }
    }
    
    private fun initializeBiometricAuth() {
        if (!sharedPrefsManager.isBiometricEnabled()) return
        
        val executor = ContextCompat.getMainExecutor(activity)
        biometricPrompt = BiometricPrompt(activity as androidx.fragment.app.FragmentActivity,
            executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    handleBiometricSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    handleBiometricFailure()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    handleBiometricError(errorCode, errString.toString())
                }
            })
    }
    
    private fun checkAuthenticationState() {
        scope.launch {
            val isLoggedIn = sharedPrefsManager.isLoggedIn()
            val username = sharedPrefsManager.getUsername()
            
            if (isLoggedIn && username != null) {
                // Check if biometric re-authentication is required
                if (sharedPrefsManager.isBiometricEnabled() && canUseBiometric()) {
                    _authenticationRequired.value = true
                    promptBiometricAuthentication()
                } else {
                    // Auto-login successful
                    _isLoggedIn.value = true
                    _currentUsername.value = username
                }
            }
        }
    }
    
    private fun canUseBiometric(): Boolean {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }
    
    private fun promptBiometricAuthentication() {
        if (biometricPrompt == null) return
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Use your fingerprint or face to authenticate")
            .setNegativeButtonText("Use Password")
            .setConfirmationRequired(true)
            .build()
        
        biometricPrompt?.authenticate(promptInfo)
    }
    
    private fun handleBiometricSuccess() {
        scope.launch {
            _authenticationRequired.value = false
            _isLoggedIn.value = true
            soundEffectManager.playClickSound()
        }
    }
    
    private fun handleBiometricFailure() {
        scope.launch {
            _authenticationRequired.value = false
            _isLoggedIn.value = false
        }
    }
    
    private fun handleBiometricError(errorCode: Int, errorMessage: String) {
        scope.launch {
            when (errorCode) {
                BiometricPrompt.ERROR_USER_CANCELED,
                BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                    _authenticationRequired.value = false
                    _isLoggedIn.value = false
                }
                else -> {
                    _authenticationRequired.value = false
                    _isLoggedIn.value = false
                }
            }
        }
    }
    
    private fun startSecurityMonitoring() {
        // Initial security check
        scope.launch {
            delay(1000) // Allow app to fully initialize
            val initialIssue = securityManager.getSecurityIssue(false)
            _currentSecurityIssue.value = initialIssue
        }
        
        // Periodic security checks
        scope.launch {
            while (true) {
                try {
                    delay(5000) // Check every 5 seconds
                    val issue = securityManager.getSecurityIssue(false)
                    if (issue != SecurityIssue.NONE) {
                        _currentSecurityIssue.value = issue
                        logSecurityEvent(issue)
                    }
                } catch (e: Exception) {
                    // Continue monitoring even if individual checks fail
                    delay(10000) // Wait longer after error
                }
            }
        }
    }
    
    private fun logSecurityEvent(issue: SecurityIssue) {
        // In production, this would log to analytics/security monitoring
        println("Security Event Detected: ${issue.name} - ${issue.message}")
    }
    
    fun login(username: String, password: String, enableBiometric: Boolean = false): Boolean {
        return if (isValidCredentials(username, password)) {
            sharedPrefsManager.setLoggedIn(true, username)
            if (enableBiometric && canUseBiometric()) {
                sharedPrefsManager.setBiometricEnabled(true)
            }
            _isLoggedIn.value = true
            _currentUsername.value = username
            true
        } else {
            false
        }
    }
    
    fun logout() {
        sharedPrefsManager.setLoggedIn(false)
        sharedPrefsManager.setBiometricEnabled(false)
        _isLoggedIn.value = false
        _currentUsername.value = null
        _authenticationRequired.value = false
    }
    
    private fun isValidCredentials(username: String, password: String): Boolean {
        return when {
            username.isEmpty() || password.isEmpty() -> false
            username.length < 3 || password.length < 4 -> false
            // Demo credentials
            username == "admin" && password == "admin" -> true
            username == "user" && password == "password" -> true
            username == "demo" && password == "demo" -> true
            username.contains("@") && password.length >= 6 -> true // Email format
            else -> false
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
        val isLoading by _isLoading.collectAsState()
        val authenticationRequired by _authenticationRequired.collectAsState()
        
        // Show loading screen during initialization
        if (isLoading) {
            LoadingScreen()
            return
        }
        
        // Handle security issues
        if (currentSecurityIssue != SecurityIssue.NONE && !isInspectionMode) {
            SecurityAlertScreen(issue = currentSecurityIssue) { 
                activity.finishAffinity() 
            }
            return
        }
        
        // Handle biometric authentication requirement
        if (authenticationRequired) {
            BiometricAuthenticationScreen(
                onBiometricSuccess = { handleBiometricSuccess() },
                onFallbackToPassword = { 
                    _authenticationRequired.value = false
                    _isLoggedIn.value = false
                },
                onCancel = { 
                    activity.finishAffinity()
                }
            )
            return
        }
        
        // Main application content with smooth transitions
        AnimatedContent(
            targetState = Triple(isLoggedIn, isOldUiEnabled, currentUsername),
            transitionSpec = {
                val (wasLoggedIn, wasOldUi, _) = initialState
                val (willBeLoggedIn, willBeOldUi, _) = targetState
                
                when {
                    !wasLoggedIn && willBeLoggedIn -> {
                        // Login transition
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
                        ) + fadeIn(animationSpec = tween(600)) togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { -it },
                            animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
                        ) + fadeOut(animationSpec = tween(400))
                    }
                    wasLoggedIn && !willBeLoggedIn -> {
                        // Logout transition
                        slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
                        ) + fadeIn(animationSpec = tween(600)) togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
                        ) + fadeOut(animationSpec = tween(400))
                    }
                    wasOldUi != willBeOldUi -> {
                        // UI switch transition
                        scaleIn(
                            initialScale = 0.8f,
                            animationSpec = spring(dampingRatio = 0.7f, stiffness = 250f)
                        ) + fadeIn(animationSpec = tween(500)) togetherWith
                        scaleOut(
                            targetScale = 1.2f,
                            animationSpec = spring(dampingRatio = 0.7f, stiffness = 250f)
                        ) + fadeOut(animationSpec = tween(300))
                    }
                    else -> {
                        // Default transition
                        fadeIn(animationSpec = tween(400, delayMillis = 200)) +
                        scaleIn(initialScale = 0.92f, animationSpec = tween(400, delayMillis = 200)) togetherWith
                        fadeOut(animationSpec = tween(200)) +
                        scaleOut(targetScale = 0.92f, animationSpec = tween(200))
                    }
                }
            },
            label = "main_app_transition"
        ) { (loggedIn, oldUi, username) ->
            when {
                !loggedIn -> {
                    LoginScreen(
                        onLoginSuccess = { user ->
                            login(user, "admin") // Simplified for demo
                        },
                        soundEffectManager = soundEffectManager,
                        biometricManager = biometricManager,
                        onBiometricEnabled = { enabled ->
                            sharedPrefsManager.setBiometricEnabled(enabled)
                        }
                    )
                }
                oldUi -> {
                    // Old UI from the old folder
                    OldMainApplicationUI(
                        username = username ?: "User",
                        onLogout = { logout() },
                        soundEffectManager = soundEffectManager,
                        sharedPrefsManager = sharedPrefsManager
                    )
                }
                else -> {
                    // New enhanced UI
                    MainApplicationUI(
                        username = username ?: "User",
                        onLogout = { logout() },
                        soundEffectManager = soundEffectManager,
                        sharedPrefsManager = sharedPrefsManager
                    )
                }
            }
        }
    }
    
    @Composable
    private fun LoadingScreen() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Initializing Security Systems...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please wait while we ensure your data is secure",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    
    @Composable
    private fun BiometricAuthenticationScreen(
        onBiometricSuccess: () -> Unit,
        onFallbackToPassword: () -> Unit,
        onCancel: () -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(24.dp),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Animated fingerprint icon
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
                                    )
                                )
                            )
                            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Fingerprint,
                            contentDescription = "Biometric Authentication",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        text = "Biometric Authentication",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Use your fingerprint or face to continue securely",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = onFallbackToPassword,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Use Password")
                        }
                        
                        Button(
                            onClick = { promptBiometricAuthentication() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Authenticate")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    TextButton(onClick = onCancel) {
                        Text(
                            "Cancel",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Get application statistics for monitoring
     */
    fun getApplicationStats(): ApplicationStats {
        return ApplicationStats(
            isLoggedIn = _isLoggedIn.value,
            currentUser = _currentUsername.value,
            securityStatus = securityManager.getSecurityStatus(),
            isOldUiEnabled = _isOldUiEnabled.value,
            isBiometricEnabled = sharedPrefsManager.isBiometricEnabled(),
            lastLoginTime = System.currentTimeMillis()
        )
    }
    
    /**
     * Export user settings for backup
     */
    fun exportUserSettings(): Map<String, Any?> {
        return sharedPrefsManager.exportSettings()
    }
    
    /**
     * Import user settings from backup
     */
    fun importUserSettings(settings: Map<String, Any?>) {
        sharedPrefsManager.importSettings(settings)
        _isOldUiEnabled.value = sharedPrefsManager.isOldUiEnabled()
    }
    
    /**
     * Enable/disable biometric authentication
     */
    fun setBiometricEnabled(enabled: Boolean) {
        if (enabled && !canUseBiometric()) {
            return
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
            try {
                val issue = securityManager.getSecurityIssue(false)
                _currentSecurityIssue.value = issue
            } catch (e: Exception) {
                // Handle security check errors gracefully
            }
        }
    }
    
    /**
     * Clear all application data (factory reset)
     */
    fun clearAllData() {
        logout()
        sharedPrefsManager.clearAllData()
    }
    
    fun cleanup() {
        try {
            soundEffectManager.release()
            securityManager.cleanup()
        } catch (e: Exception) {
            // Handle cleanup errors gracefully
        }
    }
    
    /**
     * Application statistics data class
     */
    data class ApplicationStats(
        val isLoggedIn: Boolean,
        val currentUser: String?,
        val securityStatus: EnhancedSecurityManager.SecurityStatus,
        val isOldUiEnabled: Boolean,
        val isBiometricEnabled: Boolean,
        val lastLoginTime: Long
    )
}