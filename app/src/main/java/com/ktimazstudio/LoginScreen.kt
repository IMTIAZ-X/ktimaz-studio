package com.ktimazstudio.ui.screens

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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Lock as LockFilled
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import kotlin.math.min

/**
 * Merged LoginScreen:
 * - Keeps the **new** UI/design intact
 * - Adds **old** security features (password strength, login attempts lock, demo login, animated error card etc.)
 * - Removes biometric-related code (per request)
 * - Optimized with derivedStateOf and remember where applicable
 *
 * Usage:
 * Provide onLoginSuccess(username), and a SoundEffectManager instance.
 */
@Composable
fun LoginScreen(onLoginSuccess: (username: String) -> Unit, soundEffectManager: SoundEffectManager) {
    // --- Input & UI state ---
    var usernameInput by rememberSaveable { mutableStateOf("") }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) } // Loading indicator

    // --- Security & auxiliary state (added from old screen) ---
    var loginAttempts by rememberSaveable { mutableIntStateOf(0) } // failed attempts count
    var isLocked by rememberSaveable { mutableStateOf(false) }
    var lockTimeRemaining by rememberSaveable { mutableIntStateOf(0) } // seconds
    // derived state: is form valid enough to attempt
    val isFormValid by remember(usernameInput, passwordInput) {
        derivedStateOf {
            usernameInput.isNotBlank() && passwordInput.length >= 4
        }
    }

    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // --- Reusable colors and brushes (remembered for performance) ---
    val textFieldColors = remember {
        OutlinedTextFieldDefaults.colors(
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
    }

    val backgroundGradient = remember {
        Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                MaterialTheme.colorScheme.surfaceContainerLow,
                MaterialTheme.colorScheme.surfaceContainerHigh
            )
        )
    }

    // Password strength derived (0..5)
    val passwordStrength by remember(passwordInput) {
        derivedStateOf { calculatePasswordStrength(passwordInput) }
    }

    // Lockout countdown effect
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        // Animated Card for the login form (UI kept as in new code)
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
                // App Logo (unchanged)
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_round),
                    contentDescription = stringResource(id = R.string.app_name) + " Logo",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                        .padding(8.dp)
                )
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

                Spacer(modifier = Modifier.height(16.dp))

                // Username Field (validation: trim once on change)
                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = {
                        // keep it light: trim leading/trailing spaces but don't force mid-edit trimming
                        usernameInput = it.trimStart()
                        errorMessage = null
                    },
                    label = { Text("Username or Email") },
                    leadingIcon = { Icon(Icons.Outlined.AccountCircle, contentDescription = "Username Icon") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(18.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLocked && !isLoading,
                    isError = errorMessage != null,
                    supportingText = if (usernameInput.length > 15) {
                        { Text("${usernameInput.length}/50 characters") }
                    } else null
                )

                // Password Field with trailing visibility toggle
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        // limit and update
                        passwordInput = if (it.length > 150) it.take(150) else it
                        errorMessage = null
                    },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "Password Icon") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        if (!isLocked && !isLoading && isFormValid) {
                            // trigger login
                            attemptLogin(
                                usernameInput = usernameInput,
                                passwordInput = passwordInput,
                                scope = coroutineScope,
                                soundEffectManager = soundEffectManager,
                                haptic = haptic,
                                onSuccess = { username ->
                                    onLoginSuccess(username)
                                },
                                onError = { message ->
                                    errorMessage = message
                                    // increment attempts for incorrect credentials
                                    loginAttempts++
                                    if (loginAttempts >= 3) {
                                        isLocked = true
                                    }
                                },
                                setLoading = { isLoading = it }
                            )
                        } else if (!isFormValid) {
                            errorMessage = if (usernameInput.isBlank()) "Username is required" else "Password must be at least 4 characters"
                        }
                    }),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) "Hide password" else "Show password"
                        IconButton(onClick = {
                            soundEffectManager.playClickSound()
                            passwordVisible = !passwordVisible
                        }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLocked && !isLoading,
                    isError = errorMessage != null,
                    supportingText = {
                        // Password strength indicator (small, unobtrusive)
                        PasswordStrengthIndicator(password = passwordInput, strength = passwordStrength)
                    }
                )

                // Animated error card (from old code style) — appears when errorMessage != null
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(animationSpec = tween(200)) + slideInVertically(initialOffsetY = { -it / 2 }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                    exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(targetOffsetY = { -it / 2 }, animationSpec = spring(stiffness = Spring.StiffnessMedium))
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
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

                // Security status indicator (shows lock time or secure message)
                SecurityStatusIndicator(
                    isSecure = !isLocked && loginAttempts < 3,
                    lockTimeRemaining = lockTimeRemaining,
                    attempts = loginAttempts
                )

                // Login Button with press animations
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.98f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "login_button_scale"
                )
                val alpha by animateFloatAsState(
                    targetValue = if (isPressed) 0.8f else 1.0f,
                    animationSpec = tween(150),
                    label = "login_button_alpha"
                )

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (!isLocked && !isLoading) {
                            if (!isFormValid) {
                                errorMessage = if (usernameInput.isBlank()) "Username is required" else "Password must be at least 4 characters"
                                return@Button
                            }
                            attemptLogin(
                                usernameInput = usernameInput,
                                passwordInput = passwordInput,
                                scope = coroutineScope,
                                soundEffectManager = soundEffectManager,
                                haptic = haptic,
                                onSuccess = { username ->
                                    onLoginSuccess(username)
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
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .height(56.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 10.dp),
                    enabled = !isLoading && !isLocked,
                    interactionSource = interactionSource
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else if (isLocked) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(LockFilled, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Locked ($lockTimeRemaining s)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Text("LOGIN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Row with Forgot Password and Demo Login (demo added from old)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = {
                        soundEffectManager.playClickSound()
                        // TODO: navigate to forgot password
                    }) {
                        Text("Forgot password?", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                    }

                    TextButton(onClick = {
                        soundEffectManager.playClickSound()
                        // Demo login autofill (helpful for testing)
                        usernameInput = "demo"
                        passwordInput = "demo"
                    }) {
                        Text("Demo Login", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                    }
                }

                // Security notice (kept)
                Text(
                    text = "🔒 Your data is protected with end-to-end encryption",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

/** Helper composable: Security status indicator (compact & efficient) */
@Composable
private fun SecurityStatusIndicator(
    isSecure: Boolean,
    lockTimeRemaining: Int,
    attempts: Int = 0
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        val (icon, text, color) = remember(isSecure, lockTimeRemaining, attempts) {
            when {
                lockTimeRemaining > 0 -> Triple(Icons.Filled.Warning, "Account locked - $lockTimeRemaining seconds remaining", MaterialTheme.colorScheme.error)
                !isSecure -> Triple(Icons.Filled.Warning, "Security warning - multiple failed attempts", MaterialTheme.colorScheme.error)
                else -> Triple(Icons.Filled.Lock, "Secure connection established", MaterialTheme.colorScheme.primary)
            }
        }

        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

/** Password strength UI (small, non-invasive) */
@Composable
private fun PasswordStrengthIndicator(password: String, strength: Int) {
    if (password.isBlank()) return

    val (strengthText, progressFrac, color) = remember(strength) {
        when (strength) {
            0, 1 -> Triple("Weak", 0.25f, MaterialTheme.colorScheme.error)
            2 -> Triple("Fair", 0.45f, MaterialTheme.colorScheme.tertiary)
            3 -> Triple("Good", 0.7f, MaterialTheme.colorScheme.primary)
            else -> Triple("Strong", 1.0f, MaterialTheme.colorScheme.primary)
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Strength: $strengthText", style = MaterialTheme.typography.bodySmall, color = color)
        LinearProgressIndicator(progress = progressFrac, modifier = Modifier.width(60.dp).height(3.dp))
    }
}

/**
 * Reusable login attempt logic (from old code) — optimized and non-blocking.
 * - setLoading controls the loading indicator
 * - onSuccess / onError callbacks handle results
 */
private fun attemptLogin(
    usernameInput: String,
    passwordInput: String,
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
            // Simulate network auth delay (kept short for UX)
            delay(1200)

            val isValid = when {
                usernameInput.isBlank() || passwordInput.isBlank() -> false
                usernameInput.length < 3 || passwordInput.length < 4 -> false
                usernameInput == "admin" && passwordInput == "admin" -> true
                usernameInput == "user" && passwordInput == "password" -> true
                usernameInput == "demo" && passwordInput == "demo" -> true
                usernameInput.contains("@") && passwordInput.length >= 6 -> true // basic email heuristic
                else -> false
            }

            if (isValid) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                soundEffectManager.playClickSound()
                onSuccess(usernameInput)
            } else {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                soundEffectManager.playClickSound()
                val errorMsg = when {
                    usernameInput.isBlank() -> "Username is required"
                    passwordInput.isBlank() -> "Password is required"
                    usernameInput.length < 3 -> "Username must be at least 3 characters"
                    passwordInput.length < 4 -> "Password must be at least 4 characters"
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

/** Utility: calculate password strength (0..4) */
private fun calculatePasswordStrength(password: String): Int {
    var strength = 0
    if (password.length >= 8) strength++
    if (password.any { it.isUpperCase() }) strength++
    if (password.any { it.isDigit() }) strength++
    if (password.any { !it.isLetterOrDigit() }) strength++
    // cap strength between 0..4 (we will use mapping)
    return min(4, strength)
}
