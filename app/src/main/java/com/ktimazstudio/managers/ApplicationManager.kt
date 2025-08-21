package com.ktimazstudio.managers

import android.content.Context
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.ktimazstudio.enums.NavigationStyle
import com.ktimazstudio.ui.screens.SecurityAlertScreen
import com.ktimazstudio.ui.screens.LoginScreen
import com.ktimazstudio.ui.MainApplicationUI
import com.ktimazstudio.old.ui.OldMainApplicationUI
import com.ktimazstudio.utils.isAppInDarkTheme
import com.ktimazstudio.utils.EnhancedSecurityManager

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
                soundEffectManager.loadSounds()
                setupThemeListener()
                setupUiPreferenceListeners()
                initializeBiometricAuth()
                startSecurityMonitoring()
                checkAuthenticationState()
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
                if (sharedPrefsManager.isBiometricEnabled() && canUseBiometric()) {
                    _authenticationRequired.value = true
                    promptBiometricAuthentication()
                } else {
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
        scope.launch {
            delay(1000)
            val initialIssue = securityManager.getSecurityIssue(false)
            _currentSecurityIssue.value = initialIssue
        }
        
        scope.launch {
            while (true) {
                delay(5000)
                val issue = securityManager.getSecurityIssue(false)
                if (issue != SecurityIssue.NONE) {
                    _currentSecurityIssue.value = issue
                    logSecurityEvent(issue)
                }
            }
        }
    }
    
    private fun logSecurityEvent(issue: SecurityIssue) {
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
            username == "admin" && password == "admin" -> true
            username == "user" && password == "password" -> true
            username == "demo" && password == "demo" -> true
            else -> false
        }
    }
    
    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun ComposeApplication() {
        val context = LocalContext.current
        val isInspectionMode = LocalInspectionMode.current
        
        val currentThemeSetting = remember { mutableStateOf(sharedPrefsManager.getThemeSetting()) }
        val useDarkTheme = isAppInDarkTheme(currentThemeSetting.value, context)
        
        LaunchedEffect(useDarkTheme) {
            _isDarkTheme.value = useDarkTheme
        }
        
        val isLoggedIn by _isLoggedIn.collectAsState()
        val currentUsername by _currentUsername.collectAsState()
        val currentSecurityIssue by _currentSecurityIssue.collectAsState()
        val isOldUiEnabled by _isOldUiEnabled.collectAsState()
        val isLoading by _isLoading.collectAsState()
        val authenticationRequired by _authenticationRequired.collectAsState()
        
        if (isLoading) {
            LoadingScreen()
            return
        }
        
        if (currentSecurityIssue != SecurityIssue.NONE && !isInspectionMode) {
            SecurityAlertScreen(issue = currentSecurityIssue) { 
                activity.finishAffinity() 
            }
            return
        }
        
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
        
        AnimatedContent(
            targetState = Triple(isLoggedIn, isOldUiEnabled, currentUsername),
            transitionSpec = {
                val (wasLoggedIn, wasOldUi, _) = initialState
                val (willBeLoggedIn, willBeOldUi, _) = targetState
                
                when {
                    !wasLoggedIn && willBeLoggedIn -> {
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
                            login(user, "admin")
                        },
                        soundEffectManager = soundEffectManager,
                        biometricManager = biometricManager,
                        onBiometricEnabled = { enabled ->
                            sharedPrefsManager.setBiometricEnabled(enabled)
                        }
                    )
                }
                oldUi -> {
                    OldMainApplicationUI(
                        username = username ?: "User",
                        onLogout = { logout() },
                        soundEffectManager = soundEffectManager,
                        sharedPrefsManager = sharedPrefsManager
                    )
                }
                else -> {
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
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Initializing Security...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
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
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(24.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Fingerprint,
                        contentDescription = "Biometric Authentication",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Biometric Authentication",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Use your fingerprint or face to continue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = onFallbackToPassword,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Use Password")
                        }
                        
                        Button(
                            onClick = { promptBiometricAuthentication() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Authenticate")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextButton(onClick = onCancel) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
    
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
    
    fun exportUserSettings(): Map<String, Any?> {
        return sharedPrefsManager.exportSettings()
    }
    
    fun importUserSettings(settings: Map<String, Any?>) {
        sharedPrefsManager.importSettings(settings)
        _isOldUiEnabled.value = sharedPrefsManager.isOldUiEnabled()
    }
    
    fun setBiometricEnabled(enabled: Boolean) {
        if (enabled && !canUseBiometric()) {
            return
        }
        
        sharedPrefsManager.setBiometricEnabled(enabled)
        if (enabled) {
            initializeBiometricAuth()
        }
    }
    
    fun performSecurityCheck() {
        scope.launch {
            val issue = securityManager.getSecurityIssue(false)
            _currentSecurityIssue.value = issue
        }
    }
    
    fun clearAllData() {
        logout()
        sharedPrefsManager.clearAllData()
    }
    
    fun cleanup() {
        soundEffectManager.release()
        securityManager.cleanup()
    }
    
    data class ApplicationStats(
        val isLoggedIn: Boolean,
        val currentUser: String?,
        val securityStatus: EnhancedSecurityManager.SecurityStatus,
        val isOldUiEnabled: Boolean,
        val isBiometricEnabled: Boolean,
        val lastLoginTime: Long
    )
}