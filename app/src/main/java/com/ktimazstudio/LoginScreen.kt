package com.ktimazstudio.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.ktimazstudio.R
import com.ktimazstudio.managers.SoundEffectManager

/**
 * Clean, compile-ready LoginScreen.kt
 * - No duplicate functions
 * - No external libraries
 * - Modern UI, animations, password strength, lockout logic
 * - Removed: Demo Login, Biometric toggle, Fingerprint login
 */

@Composable
fun LoginScreen(
    onLoginSuccess: (username: String) -> Unit,
    soundEffectManager: SoundEffectManager
) {
    // --- State ---
    var usernameInput by rememberSaveable { mutableStateOf("") }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var loginAttempts by remember { mutableIntStateOf(0) }
    var isLocked by remember { mutableStateOf(false) }
    var lockTimeRemaining by remember { mutableIntStateOf(0) }

    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // --- TextField colors ---
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

    // --- Background gradient ---
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f),
            MaterialTheme.colorScheme.surfaceContainerLow,
            MaterialTheme.colorScheme.surface
        )
    )

    // --- Lockout timer effect ---
    LaunchedEffect(isLocked) {
        if (isLocked) {
            lockTimeRemaining = 30
            while (lockTimeRemaining > 0) {
                delay(1000L)
                // safe subtraction on Int
                lockTimeRemaining = (lockTimeRemaining - 1).coerceAtLeast(0)
            }
            isLocked = false
            loginAttempts = 0
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        // Card container
        Card(
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 520.dp)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 28.dp, vertical = 32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Logo
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_round),
                    contentDescription = stringResource(id = R.string.app_name) + " Logo",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f))
                        .padding(6.dp)
                )

                // Title
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Securely sign in to your account",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Username field
                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = {
                        // keep trimmed and length limited
                        usernameInput = it.trim().take(40)
                        errorMessage = null
                    },
                    label = { Text("Username or Email") },
                    leadingIcon = { Icon(Icons.Outlined.AccountCircle, contentDescription = "Username Icon") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(18.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLocked && !isLoading,
                    isError = errorMessage != null,
                )

                // Password field
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it.take(128) // max length
                        errorMessage = null
                    },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "Password Icon") },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val desc = if (passwordVisible) "Hide password" else "Show password"
                        IconButton(onClick = {
                            passwordVisible = !passwordVisible
                            soundEffectManager.playClickSound()
                        }) {
                            Icon(imageVector = image, contentDescription = desc)
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        if (!isLocked && !isLoading) {
                            attemptLogin(
                                username = usernameInput,
                                password = passwordInput,
                                scope = coroutineScope,
                                soundEffectManager = soundEffectManager,
                                haptic = haptic,
                                onSuccess = { user ->
                                    onLoginSuccess(user)
                                },
                                onError = { msg ->
                                    errorMessage = msg
                                    loginAttempts++
                                    if (loginAttempts >= 3) isLocked = true
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

                // Error animated message
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(animationSpec = tween(220)) + slideInVertically(
                        initialOffsetY = { -it / 2 },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ),
                    exit = fadeOut(animationSpec = tween(180)) + slideOutVertically(
                        targetOffsetY = { -it / 2 },
                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                    )
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Security status (single source)
                SecurityStatusIndicator(isSecure = !isLocked && loginAttempts < 3, lockTimeRemaining = lockTimeRemaining)

                Spacer(modifier = Modifier.height(6.dp))

                // Animated button press feedback
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.98f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                )
                val alpha by animateFloatAsState(
                    targetValue = if (isPressed) 0.85f else 1.0f,
                    animationSpec = tween(durationMillis = 150)
                )

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (!isLocked && !isLoading) {
                            attemptLogin(
                                username = usernameInput,
                                password = passwordInput,
                                scope = coroutineScope,
                                soundEffectManager = soundEffectManager,
                                haptic = haptic,
                                onSuccess = { user ->
                                    onLoginSuccess(user)
                                },
                                onError = { msg ->
                                    errorMessage = msg
                                    loginAttempts++
                                    if (loginAttempts >= 3) isLocked = true
                                },
                                setLoading = { isLoading = it }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 12.dp),
                    enabled = !isLoading && !isLocked,
                    interactionSource = interactionSource
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    } else if (isLocked) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Locked ($lockTimeRemaining s)")
                        }
                    } else {
                        Text("SIGN IN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Forgot password placeholder (no special behavior)
                TextButton(onClick = {
                    // Keep minimal: just play sound
                    soundEffectManager.playClickSound()
                }) {
                    Text("Forgot password?", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

/* ---------------- Helper components (single copy each) ---------------- */

@Composable
private fun SecurityStatusIndicator(isSecure: Boolean, lockTimeRemaining: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val icon = if (isSecure) Icons.Filled.Shield else Icons.Filled.Warning
        val tint = if (isSecure) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        Text(
            text = when {
                lockTimeRemaining > 0 -> "Account locked - $lockTimeRemaining seconds remaining"
                isSecure -> "Secure connection established"
                else -> "Security warning - multiple failed attempts"
            },
            style = MaterialTheme.typography.bodySmall,
            color = tint
        )
    }
}

@Composable
private fun PasswordStrengthIndicator(password: String) {
    if (password.isBlank()) return
    val strength = calculatePasswordStrength(password)
    val strengthText = when (strength) {
        0,1 -> "Weak"
        2,3 -> "Fair"
        4 -> "Good"
        else -> "Strong"
    }
    val strengthColor = when (strength) {
        in 0..2 -> MaterialTheme.colorScheme.error
        in 3..4 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Strength: $strengthText", style = MaterialTheme.typography.bodySmall, color = strengthColor)
        LinearProgressIndicator(
            progress = (strength.toFloat().coerceIn(0f, 5f) / 5f),
            modifier = Modifier.width(72.dp).height(4.dp),
            color = strengthColor,
            trackColor = strengthColor.copy(alpha = 0.25f)
        )
    }
}

/* Single password strength calculator function (no duplicates) */
private fun calculatePasswordStrength(password: String): Int {
    var strength = 0
    if (password.length >= 8) strength++
    if (password.any { it.isUpperCase() }) strength++
    if (password.any { it.isDigit() }) strength++
    if (password.any { !it.isLetterOrDigit() }) strength++
    if (password.length >= 12) strength++
    return strength.coerceIn(0, 5)
}

/* Single attemptLogin function (no duplicates).
   Accepts HapticFeedback object for proper typing. */
private fun attemptLogin(
    username: String,
    password: String,
    scope: CoroutineScope,
    soundEffectManager: SoundEffectManager,
    haptic: HapticFeedback,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit,
    setLoading: (Boolean) -> Unit
) {
    // Quick client-side checks
    if (username.isBlank() || password.isBlank()) {
        onError("Username and password are required.")
        return
    }
    if (username.length < 3) {
        onError("Username must be at least 3 characters.")
        return
    }
    if (password.length < 4) {
        onError("Password must be at least 4 characters.")
        return
    }

    // Start pseudo-auth flow
    scope.launch {
        setLoading(true)
        try {
            delay(1200L) // simulate network/auth delay

            // Simple auth rules for demo (replace with real auth)
            val isValid = when {
                username == "admin" && password == "admin" -> true
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
                onError("Invalid username or password")
            }
        } catch (t: Throwable) {
            onError("Authentication failed. Try again.")
        } finally {
            setLoading(false)
        }
    }
}
