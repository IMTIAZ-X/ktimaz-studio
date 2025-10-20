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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
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
import com.ktimazstudio.R
import com.ktimazstudio.managers.SoundEffectManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun LoginScreen(
    onLoginSuccess: (username: String) -> Unit,
    soundEffectManager: SoundEffectManager
) {
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
    val coroutineScope = rememberCoroutineScope()

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

    // Background Gradient with Floating Particles
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.surfaceContainerLow,
            MaterialTheme.colorScheme.surfaceContainerHigh
        )
    )

    val particlePositions = remember { List(20) { Pair(Random.nextFloat(), Random.nextFloat()) } }
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val particleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_movement"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        // Particles
        particlePositions.forEachIndexed { index, (x, y) ->
            Box(
                modifier = Modifier
                    .offset(
                        x = (x * 400).dp,
                        y = (y * 800).dp + ((particleOffset * 100f) * (index % 3 + 1)).dp
                    )
                    .size((4 + index % 3).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f + (index % 3) * 0.05f))
            )
        }

        Card(
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
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
                // Logo with Pulse/Glow Animation
                val logoScale by infiniteTransition.animateFloat(
                    initialValue = 0.98f,
                    targetValue = 1.02f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(3000, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "logo_animation"
                )

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .graphicsLayer { scaleX = logoScale; scaleY = logoScale }
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher_round),
                        contentDescription = stringResource(id = R.string.app_name) + " Logo",
                        modifier = Modifier.size(80.dp).clip(CircleShape)
                    )
                }

                // App Title
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Secure Authentication System",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // Security Status Indicator
                SecurityStatusIndicator(
                    isSecure = !isLocked && loginAttempts < 3,
                    lockTimeRemaining = lockTimeRemaining
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Username
                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = { usernameInput = it.trim(); errorMessage = null },
                    label = { Text("Username or Email") },
                    leadingIcon = { Icon(Icons.Outlined.AccountCircle, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(KeyboardType.Email, ImeAction.Next),
                    shape = RoundedCornerShape(18.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLocked && !isLoading,
                    isError = errorMessage != null
                )

                // Password
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it; errorMessage = null },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { soundEffectManager.playClickSound(); passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(KeyboardType.Password, ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        attemptLogin(
                            usernameInput,
                            passwordInput,
                            coroutineScope,
                            soundEffectManager,
                            haptic,
                            onSuccess = { username -> onLoginSuccess(username) },
                            onError = { msg -> errorMessage = msg },
                            setLoading = { isLoading = it },
                            loginAttemptsState = { loginAttempts = it },
                            isLockedState = { isLocked = it },
                            lockTimeState = { lockTimeRemaining = it }
                        )
                    }),
                    shape = RoundedCornerShape(18.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLocked && !isLoading,
                    isError = errorMessage != null,
                    supportingText = { PasswordStrengthIndicator(password = passwordInput) }
                )

                // Animated Error Card
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(animationSpec = tween(300)) + slideInVertically(initialOffsetY = { -it/2 }, animationSpec = spring()),
                    exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(targetOffsetY = { -it/2 }, animationSpec = spring())
                ) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f))) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // Login Button
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(if (isPressed) 0.95f else 1f)
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        attemptLogin(
                            usernameInput,
                            passwordInput,
                            coroutineScope,
                            soundEffectManager,
                            haptic,
                            onSuccess = { username -> onLoginSuccess(username) },
                            onError = { msg -> errorMessage = msg },
                            setLoading = { isLoading = it },
                            loginAttemptsState = { loginAttempts = it },
                            isLockedState = { isLocked = it },
                            lockTimeState = { lockTimeRemaining = it }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale),
                    shape = RoundedCornerShape(20.dp),
                    enabled = !isLoading && !isLocked
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Authenticating...", style = MaterialTheme.typography.titleMedium)
                    } else if (isLocked) {
                        Text("Locked ($lockTimeRemaining s)", style = MaterialTheme.typography.titleMedium)
                    } else {
                        Text("SIGN IN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }

                // Forgot Password
                TextButton(onClick = { soundEffectManager.playClickSound() }) {
                    Text("Forgot Password?", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun PasswordStrengthIndicator(password: String) {
    if (password.isBlank()) return
    val strength = calculatePasswordStrength(password)
    val color = when (strength) {
        in 0..2 -> MaterialTheme.colorScheme.error
        in 3..4 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    val text = when (strength) {
        0,1 -> "Weak"
        2,3 -> "Fair"
        4 -> "Good"
        else -> "Strong"
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Strength: $text", style = MaterialTheme.typography.bodySmall, color = color)
        LinearProgressIndicator(progress = strength / 5f, modifier = Modifier.width(60.dp).height(3.dp), color = color, trackColor = color.copy(alpha = 0.3f))
    }
}

@Composable
fun SecurityStatusIndicator(isSecure: Boolean, lockTimeRemaining: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            imageVector = if (isSecure) Icons.Filled.Lock else Icons.Filled.Warning,
            contentDescription = null,
            tint = if (isSecure) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = if (lockTimeRemaining>0) "Account locked - $lockTimeRemaining s"
            else if (isSecure) "Secure connection established"
            else "Security warning - multiple failed attempts",
            style = MaterialTheme.typography.bodySmall,
            color = if (isSecure) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}

fun calculatePasswordStrength(password: String): Int {
    var strength = 0
    if(password.length>=8) strength++
    if(password.any { it.isUpperCase() }) strength++
    if(password.any { it.isDigit() }) strength++
    if(password.any { !it.isLetterOrDigit() }) strength++
    if(password.length>=12) strength++
    return strength
}

fun attemptLogin(
    username: String,
    password: String,
    scope: kotlinx.coroutines.CoroutineScope,
    soundEffectManager: SoundEffectManager,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    onSuccess: (String)->Unit,
    onError: (String)->Unit,
    setLoading: (Boolean)->Unit,
    loginAttemptsState: (Int)->Unit,
    isLockedState: (Boolean)->Unit,
    lockTimeState: (Int)->Unit
){
    scope.launch {
        setLoading(true)
        delay(1500)
        var loginAttempts = 0
        var isLocked = false
        if(username=="admin" && password=="admin"){
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            soundEffectManager.playClickSound()
            onSuccess(username)
        }else{
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            soundEffectManager.playClickSound()
            loginAttempts++
            if(loginAttempts>=3){
                isLocked = true
                lockTimeState(30)
                while(lockTimeState>0){
                    delay(1000)
                    lockTimeState(lockTimeState-1)
                }
                loginAttempts = 0
                isLocked = false
            }
            loginAttemptsState(loginAttempts)
            isLockedState(isLocked)
            onError("Invalid username or password")
        }
        setLoading(false)
    }
}
