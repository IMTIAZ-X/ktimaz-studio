package com.ktimazstudio.ui.screens

import androidx.biometric.BiometricManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.ktimazstudio.R
import com.ktimazstudio.managers.SoundEffectManager

/**
 * Merged LoginScreen:
 * - New UI structure (from new)
 * - Added reusable attemptLogin(), password strength, security indicator, biometric option,
 *   account lock, animated error card, logo pulse/glow, ModernLoadingBar & AdvancedLoadingDots.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (username: String) -> Unit,
    soundEffectManager: SoundEffectManager,
    biometricManager: BiometricManager,
    onBiometricEnabled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var usernameInput by rememberSaveable { mutableStateOf("") }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var loginAttempts by remember { mutableIntStateOf(0) }
    var isLocked by remember { mutableStateOf(false) }
    var lockTimeRemaining by remember { mutableIntStateOf(0) }
    var showBiometricOption by remember { mutableStateOf(false) }
    var enableBiometric by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Check biometric availability (from old)
    LaunchedEffect(Unit) {
        showBiometricOption = when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    // Account lock timer (from old)
    LaunchedEffect(isLocked) {
        if (isLocked) {
            lockTimeRemaining = 30
            while (lockTimeRemaining > 0) {
                delay(1000)
                lockTimeRemaining--
            }
            isLocked = false
            loginAttempts = 0
        }
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

    // Background gradient (keep as in new)
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.surfaceContainerLow,
            MaterialTheme.colorScheme.surfaceContainerHigh
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        // Main login card (from new; preserved)
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
                // --- Logo with pulse/glow (added) ---
                val infiniteTransition = rememberInfiniteTransition(label = "logoPulse")
                val logoScale by infiniteTransition.animateFloat(
                    initialValue = 0.98f,
                    targetValue = 1.02f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "logoScale"
                )

                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .graphicsLayer { scaleX = logoScale; scaleY = logoScale }
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher_round),
                        contentDescription = stringResource(id = R.string.app_name) + " Logo",
                        modifier = Modifier.size(64.dp)
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

                Spacer(modifier = Modifier.height(8.dp))

                // --- Security status indicator (added) ---
                SecurityStatusIndicator(
                    isSecure = !isLocked && loginAttempts < 3,
                    lockTimeRemaining = lockTimeRemaining
                )

                // --- Username field ---
                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = {
                        usernameInput = it.trim()
                        errorMessage = null
                    },
                    label = { Text("Username or Email") },
                    leadingIcon = { Icon(Icons.Outlined.AccountCircle, contentDescription = "Username Icon") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(18.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null,
                    enabled = !isLocked && !isLoading
                )

                // --- Password field with PasswordStrengthIndicator ---
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        errorMessage = null
                    },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "Password Icon") },
                    trailingIcon = {
                        IconButton(onClick = {
                            soundEffectManager.playClickSound()
                            passwordVisible = !passwordVisible
                        }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        if (!isLocked && !isLoading) {
                            // Use reusable attemptLogin
                            attemptLogin(
                                usernameInput, passwordInput, coroutineScope,
                                soundEffectManager, haptic,
                                onSuccess = { username ->
                                    onLoginSuccess(username)
                                    if (enableBiometric) onBiometricEnabled(true)
                                },
                                onError = { message ->
                                    errorMessage = message
                                    loginAttempts++
                                    if (loginAttempts >= 3) {
                                        isLocked = true
                                    }
                                },
                                setLoading = { isLoading = it }
                            )
                        }
                    }),
                    shape = RoundedCornerShape(18.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLocked && !isLoading,
                    isError = errorMessage != null,
                    supportingText = {
                        PasswordStrengthIndicator(password = passwordInput)
                    }
                )

                // --- Biometric enable option (added) ---
                if (showBiometricOption) {
                    BiometricEnableOption(
                        enabled = enableBiometric,
                        onEnabledChange = {
                            soundEffectManager.playClickSound()
                            enableBiometric = it
                            onBiometricEnabled(it)
                        },
                        soundEffectManager = soundEffectManager
                    )
                }

                // --- Animated error card (added) ---
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = slideInVertically(initialOffsetY = { -it / 2 }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(animationSpec = tween(300)),
                    exit = slideOutVertically(targetOffsetY = { -it / 2 }, animationSpec = spring(stiffness = Spring.StiffnessMedium)) + fadeOut(animationSpec = tween(200))
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Login attempts indicator (only when attempts > 0 and not locked)
                if (loginAttempts > 0 && !isLocked) {
                    Text(
                        text = "Login attempts: $loginAttempts/3",
                        color = if (loginAttempts >= 2) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- Login Button with sound/haptic and loading indicators ---
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.98f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "login_button_scale"
                )
                val alpha by animateFloatAsState(
                    targetValue = if (isPressed) 0.88f else 1.0f,
                    animationSpec = tween(150),
                    label = "login_button_alpha"
                )

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        attemptLogin(
                            usernameInput, passwordInput, coroutineScope,
                            soundEffectManager, haptic,
                            onSuccess = { username ->
                                onLoginSuccess(username)
                                if (enableBiometric) onBiometricEnabled(true)
                            },
                            onError = { message ->
                                errorMessage = message
                                loginAttempts++
                                if (loginAttempts >= 3) isLocked = true
                            },
                            setLoading = { isLoading = it }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 10.dp),
                    enabled = !isLoading && !isLocked
                ) {
                    if (isLoading) {
                        // Show both dot loader and progress bar optionally
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Advanced dots (added)
                            AdvancedLoadingDotsInline()
                            Text("Authenticating...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                    } else if (isLocked) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text("Locked ($lockTimeRemaining s)")
                        }
                    } else {
                        Text("LOGIN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Modern progress bar placed under the button (added)
                if (isLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ModernLoadingBarInline()
                }

                // --- Only Forgot Password (kept) ---
                TextButton(onClick = {
                    soundEffectManager.playClickSound()
                    // TODO: navigate to forgot password flow
                }) {
                    Text("Forgot password?", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

/* -------------------------
   Reusable login coroutine
   ------------------------- */
private fun attemptLogin(
    username: String,
    password: String,
    scope: kotlinx.coroutines.CoroutineScope,
    soundEffectManager: SoundEffectManager,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit,
    setLoading: (Boolean) -> Unit
) {
    scope.launch {
        setLoading(true)
        try {
            delay(1500) // Simulate network/auth delay

            val isValid = when {
                username.isBlank() || password.isBlank() -> false
                username.length < 3 || password.length < 4 -> false
                username == "admin" && password == "admin" -> true
                username == "user" && password == "password" -> true
                username == "demo" && password == "demo" -> true
                username.contains("@") && password.length >= 6 -> true
                else -> false
            }

            if (isValid) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                soundEffectManager.playClickSound()
                onSuccess(username)
            } else {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                soundEffectManager.playClickSound()
                val errorMsg = when {
                    username.isBlank() -> "Username is required"
                    password.isBlank() -> "Password is required"
                    username.length < 3 -> "Username must be at least 3 characters"
                    password.length < 4 -> "Password must be at least 4 characters"
                    else -> "Invalid username or password"
                }
                onError(errorMsg)
            }
        } catch (e: Exception) {
            onError("Authentication failed. Please try again.")
        } finally {
            setLoading(false)
        }
    }
}

/* -------------------------
   Password strength indicator
   (from old, fixed progress param)
   ------------------------- */
@Composable
private fun PasswordStrengthIndicator(password: String) {
    if (password.isBlank()) return

    val strength = calculatePasswordStrength(password)
    val strengthColor = when (strength) {
        in 0..2 -> MaterialTheme.colorScheme.error
        in 3..4 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    val strengthText = when (strength) {
        0, 1 -> "Weak"
        2, 3 -> "Fair"
        4 -> "Good"
        else -> "Strong"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Strength: $strengthText",
            style = MaterialTheme.typography.bodySmall,
            color = strengthColor
        )

        LinearProgressIndicator(
            progress = strength / 5f,
            modifier = Modifier
                .width(60.dp)
                .height(3.dp),
            color = strengthColor,
            trackColor = strengthColor.copy(alpha = 0.3f)
        )
    }
}

private fun calculatePasswordStrength(password: String): Int {
    var strength = 0
    if (password.length >= 8) strength++
    if (password.any { it.isUpperCase() }) strength++
    if (password.any { it.isDigit() }) strength++
    if (password.any { !it.isLetterOrDigit() }) strength++
    if (password.length >= 12) strength++
    return strength
}

/* -------------------------
   Security status indicator
   ------------------------- */
@Composable
private fun SecurityStatusIndicator(isSecure: Boolean, lockTimeRemaining: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (isSecure) Icons.Filled.Shield else Icons.Filled.Warning,
            contentDescription = null,
            tint = if (isSecure) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = if (lockTimeRemaining > 0) {
                "Account locked - $lockTimeRemaining seconds remaining"
            } else if (isSecure) {
                "Secure connection established"
            } else {
                "Security warning - multiple failed attempts"
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (isSecure) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}

/* -------------------------
   Biometric enable option (from old)
   ------------------------- */
@Composable
private fun BiometricEnableOption(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    soundEffectManager: SoundEffectManager
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Filled.Fingerprint,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Enable Biometric Login",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Use fingerprint for quick access",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = enabled,
                onCheckedChange = {
                    soundEffectManager.playClickSound()
                    onEnabledChange(it)
                }
            )
        }
    }
}

/* -------------------------
   Advanced loading dots (inline small version)
   ------------------------- */
@Composable
private fun AdvancedLoadingDotsInline() {
    val infiniteTransition = rememberInfiniteTransition(label = "LoadingDotsInline")
    val dot1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
        label = "dot1"
    )
    val dot2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, delayMillis = 150, easing = LinearEasing), RepeatMode.Reverse),
        label = "dot2"
    )
    val dot3 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, delayMillis = 300, easing = LinearEasing), RepeatMode.Reverse),
        label = "dot3"
    )

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        DotInline(dot1)
        DotInline(dot2)
        DotInline(dot3)
    }
}

@Composable
private fun DotInline(scale: Float) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .graphicsLayer {
                scaleX = 0.7f + 0.3f * scale
                scaleY = 0.7f + 0.3f * scale
            }
            .clip(CircleShape)
            .background(Color(0xFF6C63FF))
    )
}

/* -------------------------
   Modern loading bar (inline small)
   ------------------------- */
@Composable
private fun ModernLoadingBarInline() {
    val infiniteTransition = rememberInfiniteTransition(label = "LoadingBarInline")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1500, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Restart),
        label = "progress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.Black.copy(alpha = 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF6C63FF), Color(0xFFFF6B9D))
                        )
                    )
            )
        }
    }
}
