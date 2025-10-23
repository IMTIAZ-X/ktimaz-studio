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

@Composable
fun LoginScreen(onLoginSuccess: (username: String) -> Unit, soundEffectManager: SoundEffectManager) {
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

    // TextField colors
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

    // Background gradient
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.surfaceContainerLow,
            MaterialTheme.colorScheme.surfaceContainerHigh
        )
    )

    // Account lock timer
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
                // Logo
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

                // Username field
                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = {
                        usernameInput = it.trim()
                        errorMessage = null
                        if (it.length > 20) usernameInput = it.take(20)
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
                        { Text("${usernameInput.length}/20 characters") }
                    } else null
                )

                // Password field
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        errorMessage = null
                        if (it.length > 50) passwordInput = it.take(50)
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
                            attemptLogin(
                                usernameInput, passwordInput, coroutineScope,
                                soundEffectManager, haptic,
                                onSuccess = { username -> onLoginSuccess(username) },
                                onError = { message ->
                                    errorMessage = message
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
                    supportingText = { PasswordStrengthIndicator(passwordInput) }
                )

                // Error message
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(animationSpec = tween(200)) + slideInVertically(initialOffsetY = { -it / 2 }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                    exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(targetOffsetY = { -it / 2 }, animationSpec = spring(stiffness = Spring.StiffnessMedium))
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Security indicator
                SecurityStatusIndicator(isSecure = !isLocked && loginAttempts < 3, lockTimeRemaining = lockTimeRemaining)

                // Login button
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
                            attemptLogin(
                                usernameInput, passwordInput, coroutineScope,
                                soundEffectManager, haptic,
                                onSuccess = { username -> onLoginSuccess(username) },
                                onError = { message ->
                                    errorMessage = message
                                    loginAttempts++
                                    if (loginAttempts >= 3) isLocked = true
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
                    enabled = !isLocked && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else if (isLocked) {
                        Text("Locked ($lockTimeRemaining s)")
                    } else {
                        Text("LOGIN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Forgot password
                TextButton(onClick = { soundEffectManager.playClickSound() }) {
                    Text("Forgot password?", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

// -------------------- Helper Functions --------------------

@Composable
private fun SecurityStatusIndicator(isSecure: Boolean, lockTimeRemaining: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            imageVector = if (isSecure) Icons.Filled.Shield else Icons.Filled.Warning,
            contentDescription = null,
            tint = if (isSecure) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = if (lockTimeRemaining > 0) "Account locked - $lockTimeRemaining seconds remaining"
            else if (isSecure) "Secure connection established"
            else "Security warning - multiple failed attempts",
            style = MaterialTheme.typography.bodySmall,
            color = if (isSecure) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}

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

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Strength: $strengthText", style = MaterialTheme.typography.bodySmall, color = strengthColor)
        LinearProgressIndicator(
            progress = strength / 5f,
            modifier = Modifier.width(60.dp).height(3.dp),
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
            delay(1500)
            val isValid = when {
                username.isBlank() || password.isBlank() -> false
                username.length < 3 || password.length < 4 -> false
                username == "admin" && password == "admin" -> true
                username == "user" && password == "password" -> true
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
