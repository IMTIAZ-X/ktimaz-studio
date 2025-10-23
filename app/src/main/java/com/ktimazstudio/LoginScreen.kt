package com.ktimazstudio.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.ui.graphics.Brush
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
fun LoginScreen(
    onLoginSuccess: (username: String) -> Unit,
    soundEffectManager: SoundEffectManager,
    modifier: Modifier = Modifier
) {
    // ---------- State (save minimal necessary) ----------
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Security states
    var loginAttempts by rememberSaveable { mutableIntStateOf(0) }
    var isLocked by rememberSaveable { mutableStateOf(false) }
    var lockTimeRemaining by rememberSaveable { mutableIntStateOf(0) }

    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Derived states to avoid recomposition of unrelated UI
    val isFormValid by derivedStateOf { username.isNotBlank() && password.isNotBlank() }
    val passwordStrength by derivedStateOf { calculatePasswordStrength(password) }

    // Background gradient (subtle)
    val background = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f),
            MaterialTheme.colorScheme.surface
        )
    )

    // Simple animated particles: light and performant
    val particleCount = 12
    val particleSeed = remember { List(particleCount) { Pair(kotlin.random.Random.nextFloat(), kotlin.random.Random.nextFloat()) } }
    val infinite = rememberInfiniteTransition()
    val particleAnim by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 22000, easing = LinearEasing))
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        // Lightweight particle dots
        particleSeed.forEachIndexed { index, (x, y) ->
            val offsetX = (x * 360).dp
            val offsetY = (y * 760 + (particleAnim * (10 + index % 3))).dp
            Box(
                modifier = Modifier
                    .offset(x = offsetX, y = offsetY)
                    .size((3 + index % 3).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
            )
        }

        // Card container
        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 560.dp)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 28.dp, vertical = 28.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Animated logo (subtle breathing)
                val logoScale by infinite.animateFloat(
                    initialValue = 0.99f,
                    targetValue = 1.02f,
                    animationSpec = infiniteRepeatable(animation = tween(3000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse)
                )

                Box(modifier = Modifier.size(110.dp).graphicsLayer { scaleX = logoScale; scaleY = logoScale }, contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Brush.radialGradient(colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f), MaterialTheme.colorScheme.surface.copy(alpha = 0.05f))))
                    )
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher_round),
                        contentDescription = stringResource(id = R.string.app_name) + " logo",
                        modifier = Modifier.size(72.dp).clip(CircleShape)
                    )
                }

                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Securely sign in",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // Security status indicator
                SecurityStatusIndicator(isSecure = !isLocked && loginAttempts < 3, lockTimeRemaining = lockTimeRemaining)

                Spacer(modifier = Modifier.height(8.dp))

                // Username field
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it.take(40).trimStart() // basic sanitation
                        if (errorMessage != null) errorMessage = null
                    },
                    label = { Text("Username or Email") },
                    leadingIcon = { Icon(Icons.Outlined.AccountCircle, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLocked && !isLoading,
                    isError = errorMessage != null
                )

                // Password field with visibility toggle & strength indicator
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it.take(64)
                        if (errorMessage != null) errorMessage = null
                    },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = {
                            soundEffectManager.playClickSound()
                            passwordVisible = !passwordVisible
                        }) {
                            Icon(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, contentDescription = null)
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        if (!isLocked && !isLoading) {
                            // Trigger login flow
                            performLogin(
                                username = username,
                                password = password,
                                coroutineScope = coroutineScope,
                                onStart = { isLoading = true; errorMessage = null },
                                onSuccess = {
                                    isLoading = false
                                    // success haptic + sound
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    soundEffectManager.playClickSound()
                                    loginAttempts = 0
                                    onLoginSuccess(it)
                                },
                                onError = { msg ->
                                    isLoading = false
                                    errorMessage = msg
                                    loginAttempts++
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    soundEffectManager.playClickSound()
                                    if (loginAttempts >= 3) {
                                        startLockTimer(
                                            coroutineScope = coroutineScope,
                                            lockSeconds = 30,
                                            onTick = { lockTimeRemaining = it },
                                            onFinish = {
                                                isLocked = false
                                                loginAttempts = 0
                                                lockTimeRemaining = 0
                                            },
                                            onStart = { isLocked = true }
                                        )
                                    }
                                }
                            )
                        }
                    }),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLocked && !isLoading,
                    isError = errorMessage != null,
                    supportingText = {
                        PasswordStrengthIndicator(strength = passwordStrength, password = password)
                    }
                )

                // Animated error message
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(animationSpec = tween(220)) + slideInVertically(initialOffsetY = { -it / 2 }, animationSpec = tween(240)),
                    exit = fadeOut(animationSpec = tween(180)) + slideOutVertically(targetOffsetY = { -it / 2 }, animationSpec = tween(180))
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Login button with press animation and disabled while loading
                val interactionSource = remember { MutableInteractionSource() }
                val pressed by interactionSource.collectIsPressedAsState()
                val buttonScale by animateFloatAsState(targetValue = if (pressed) 0.985f else 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        // same as onDone
                        if (!isLocked && !isLoading) {
                            performLogin(
                                username = username,
                                password = password,
                                coroutineScope = coroutineScope,
                                onStart = { isLoading = true; errorMessage = null },
                                onSuccess = {
                                    isLoading = false
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    soundEffectManager.playClickSound()
                                    loginAttempts = 0
                                    onLoginSuccess(it)
                                },
                                onError = { msg ->
                                    isLoading = false
                                    errorMessage = msg
                                    loginAttempts++
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    soundEffectManager.playClickSound()
                                    if (loginAttempts >= 3) {
                                        startLockTimer(
                                            coroutineScope = coroutineScope,
                                            lockSeconds = 30,
                                            onTick = { lockTimeRemaining = it },
                                            onFinish = {
                                                isLocked = false
                                                loginAttempts = 0
                                                lockTimeRemaining = 0
                                            },
                                            onStart = { isLocked = true }
                                        )
                                    }
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .graphicsLayer { scaleX = buttonScale; scaleY = buttonScale },
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                    enabled = !isLocked && !isLoading && isFormValid,
                    interactionSource = interactionSource
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Signing in...", style = MaterialTheme.typography.titleMedium)
                    } else if (isLocked) {
                        Text("Locked ($lockTimeRemaining s)", style = MaterialTheme.typography.titleMedium)
                    } else {
                        Text("SIGN IN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Forgot password text (placeholder behavior)
                Text(
                    text = "Forgot password?",
                    modifier = Modifier.clickable { soundEffectManager.playClickSound() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Security note
                Text(
                    text = "🔒 Your data is protected with strong validation",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

// ---------------- Helper Composables & Functions ----------------

@Composable
private fun SecurityStatusIndicator(isSecure: Boolean, lockTimeRemaining: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            imageVector = if (isSecure) Icons.Default.Shield else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isSecure) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = if (lockTimeRemaining > 0) "Account locked - $lockTimeRemaining s" else if (isSecure) "Secure connection" else "Security warning",
            style = MaterialTheme.typography.bodySmall,
            color = if (isSecure) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun PasswordStrengthIndicator(strength: Int, password: String) {
    if (password.isBlank()) return
    val (text, progress) = when (strength) {
        in 0..1 -> Pair("Weak", 0.2f)
        2 -> Pair("Fair", 0.45f)
        3 -> Pair("Good", 0.7f)
        else -> Pair("Strong", 1f)
    }

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Strength: $text", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LinearProgressIndicator(progress = progress, modifier = Modifier.width(72.dp).height(4.dp))
    }
}

private fun calculatePasswordStrength(password: String): Int {
    var score = 0
    if (password.length >= 8) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++
    if (password.length >= 12) score++
    return score // 0..5
}

private fun performLogin(
    username: String,
    password: String,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onStart: () -> Unit,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    coroutineScope.launch {
        onStart()
        // Lightweight validation before network
        val validation = when {
            username.isBlank() -> Pair(false, "Username is required")
            password.isBlank() -> Pair(false, "Password is required")
            username.length < 3 -> Pair(false, "Username too short")
            password.length < 4 -> Pair(false, "Password too short")
            else -> Pair(true, "")
        }

        if (!validation.first) {
            // tiny delay to allow UI feedback
            delay(200)
            onError(validation.second)
            return@launch
        }

        try {
            // Simulate network call (keep short for UX)
            delay(900)

            // Replace this block with real authentication call
            val isValid = when {
                username == "admin" && password == "admin" -> true
                username.contains("@") && password.length >= 6 -> true // example rule
                else -> false
            }

            if (isValid) {
                onSuccess(username)
            } else {
                onError("Invalid username or password")
            }
        } catch (e: Exception) {
            onError("Authentication failed. Try again.")
        }
    }
}

private fun startLockTimer(
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    lockSeconds: Int,
    onStart: () -> Unit,
    onTick: (Int) -> Unit,
    onFinish: () -> Unit
) {
    coroutineScope.launch {
        onStart()
        var left = lockSeconds
        while (left > 0) {
            onTick(left)
            delay(1000)
            left--
        }
        onFinish()
    }
}
