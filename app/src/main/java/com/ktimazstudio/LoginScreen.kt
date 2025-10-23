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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Lock as LockFilled
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
import kotlin.math.min

@Composable
fun LoginScreen(onLoginSuccess: (username: String) -> Unit, soundEffectManager: SoundEffectManager) {
    var usernameInput by rememberSaveable { mutableStateOf("") }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var loginAttempts by rememberSaveable { mutableIntStateOf(0) }
    var isLocked by rememberSaveable { mutableStateOf(false) }
    var lockTimeRemaining by rememberSaveable { mutableIntStateOf(0) }

    val isFormValid by remember(usernameInput, passwordInput) {
        derivedStateOf { usernameInput.isNotBlank() && passwordInput.length >= 4 }
    }

    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

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

    val passwordStrength by remember(passwordInput) {
        derivedStateOf { calculatePasswordStrength(passwordInput) }
    }

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

                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = {
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

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
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
                            attemptLogin(
                                usernameInput = usernameInput,
                                passwordInput = passwordInput,
                                scope = coroutineScope,
                                soundEffectManager = soundEffectManager,
                                haptic = haptic,
                                onSuccess = { username -> onLoginSuccess(username) },
                                onError = { message ->
                                    errorMessage = message
                                    loginAttempts++
                                    if (loginAttempts >= 3) isLocked = true
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
                    supportingText = { PasswordStrengthIndicator(password = passwordInput, strength = passwordStrength) }
                )

                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(animationSpec = tween(200)) + slideInVertically(initialOffsetY = { -it / 2 }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                    exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(targetOffsetY = { -it / 2 }, animationSpec = spring(stiffness = Spring.StiffnessMedium))
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                SecurityStatusIndicator(isSecure = !isLocked && loginAttempts < 3, lockTimeRemaining = lockTimeRemaining, attempts = loginAttempts)

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(targetValue = if (isPressed) 0.98f else 1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                val alpha by animateFloatAsState(targetValue = if (isPressed) 0.8f else 1.0f, animationSpec = tween(150))

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
                    enabled = !isLoading && !isLocked,
                    interactionSource = interactionSource
                ) {
                    if (isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    else if (isLocked) Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(LockFilled, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Locked ($lockTimeRemaining s)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    } else Text("LOGIN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    TextButton(onClick = { soundEffectManager.playClickSound() }) {
                        Text("Forgot password?", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                    }
                }

                Text(text = "🔒 Your data is protected with end-to-end encryption", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}

@Composable
private fun SecurityStatusIndicator(isSecure: Boolean, lockTimeRemaining: Int, attempts: Int = 0) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        val (icon, text, color) = remember(isSecure, lockTimeRemaining, attempts) {
            when {
                lockTimeRemaining > 0 -> Triple(Icons.Filled.Warning, "Account locked - $lockTimeRemaining seconds remaining", MaterialTheme.colorScheme.error)
                !isSecure -> Triple(Icons.Filled.Warning, "Security warning - multiple failed attempts", MaterialTheme.colorScheme.error)
                else -> Triple(LockFilled, "Secure connection established", MaterialTheme.colorScheme.primary)
            }
        }

        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = color)
    }
}

@Composable
private fun PasswordStrengthIndicator(password: String, strength: Int) {
    if (password.isBlank()) return
    val (strengthText, progressFrac, color) = remember(strength) {
        when (strength) {
            0,1 -> Triple("Weak", 0.25f, MaterialTheme.colorScheme.error)
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
            delay(1200)
            val isValid = when {
                usernameInput.isBlank() || passwordInput.isBlank() -> false
                usernameInput.length < 3 || passwordInput.length < 4 -> false
                usernameInput == "admin" && passwordInput == "admin" -> true
                usernameInput == "user" && passwordInput == "password" -> true
                usernameInput.contains("@") && passwordInput.length >= 6 -> true
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

private fun calculatePasswordStrength(password: String): Int {
    var strength = 0
    if (password.length >= 8) strength++
    if (password.any { it.isUpperCase() }) strength++
    if (password.any { it.isDigit() }) strength++
    if (password.any { !it.isLetterOrDigit() }) strength++
    return min(4, strength)
}
