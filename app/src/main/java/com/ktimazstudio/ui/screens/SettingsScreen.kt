package com.ktimazstudio.ui.screens

import android.media.AudioManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ktimazstudio.BuildConfig
import com.ktimazstudio.enums.ThemeSetting
import com.ktimazstudio.managers.SharedPreferencesManager
import com.ktimazstudio.managers.SoundEffectManager
import com.ktimazstudio.ui.components.SettingItem
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    soundEffectManager: SoundEffectManager,
    sharedPrefsManager: SharedPreferencesManager
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showChangelogDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager

    // 🔹 State for theme and sound settings
    val currentThemeSetting = remember { mutableStateOf(sharedPrefsManager.getThemeSetting()) }
    val isSoundEnabled = remember { mutableStateOf(sharedPrefsManager.isSoundEnabled()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Application Settings",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
        )

        var notificationsEnabled by remember { mutableStateOf(true) }
        SettingItem(
            title = "Enable Notifications",
            description = "Receive updates and alerts.",
            leadingIcon = {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
            },
            control = {
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = {
                        soundEffectManager.playClickSound()
                        notificationsEnabled = it
                    }
                )
            }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

        // 🌙 Theme Changer
        SettingItem(
            title = "App Theme",
            description = "Change the visual theme of the application.",
            leadingIcon = {
                Icon(
                    Icons.Filled.ColorLens,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
            },
            control = {
                var expanded by remember { mutableStateOf(false) }
                TextButton(onClick = {
                    soundEffectManager.playClickSound()
                    expanded = true
                }) {
                    Text(currentThemeSetting.value.name.replace("_", " "))
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    ThemeSetting.values().forEach { theme ->
                        DropdownMenuItem(
                            text = { Text(theme.name.replace("_", " ")) },
                            onClick = {
                                soundEffectManager.playClickSound()
                                sharedPrefsManager.setThemeSetting(theme)
                                currentThemeSetting.value = theme
                                expanded = false
                            }
                        )
                    }
                }
            }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

        // 🔊 Sound On/Off
        SettingItem(
            title = "Sound Effects",
            description = "Enable or disable click sounds.",
            leadingIcon = {
                Icon(
                    if (isSoundEnabled.value) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
            },
            control = {
                Switch(
                    checked = isSoundEnabled.value,
                    onCheckedChange = {
                        sharedPrefsManager.setSoundEnabled(it)
                        isSoundEnabled.value = it
                        if (it) soundEffectManager.playClickSound()
                    }
                )
            }
        )

        // 🎚️ Samsung One UI 8.5 Style Custom SeekBar
        CustomSoundSeekBar(sharedPrefsManager = sharedPrefsManager, audioManager = audioManager)

        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

        // 🧍 Account Preferences
        var showAccountDialog by remember { mutableStateOf(false) }
        SettingItem(
            title = "Account Preferences",
            description = "Manage your account details.",
            leadingIcon = {
                Icon(
                    Icons.Filled.AccountBox,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
            },
            control = {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            onClick = {
                soundEffectManager.playClickSound()
                showAccountDialog = true
            }
        )
        if (showAccountDialog) {
            AlertDialog(
                onDismissRequest = { showAccountDialog = false },
                icon = { Icon(Icons.Filled.AccountBox, contentDescription = null) },
                title = { Text("Account Preferences") },
                text = { Text("Account settings details appear here.") },
                confirmButton = {
                    TextButton(onClick = {
                        soundEffectManager.playClickSound()
                        showAccountDialog = false
                    }) { Text("OK") }
                }
            )
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

        // ℹ️ About Section
        SettingItem(
            title = "About",
            description = "Information about this application.",
            leadingIcon = {
                Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.secondary)
            },
            control = {
                Icon(
                    Icons.Filled.ChevronRight,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            onClick = {
                soundEffectManager.playClickSound()
                showAboutDialog = true
            }
        )

        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                icon = { Icon(Icons.Filled.Info, null) },
                title = { Text("About App") },
                text = { Text("Version: ${BuildConfig.VERSION_NAME}\nDeveloped by Ktimaz Studio.") },
                confirmButton = {
                    TextButton(onClick = {
                        soundEffectManager.playClickSound()
                        showAboutDialog = false
                    }) { Text("Close") }
                }
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

        // 🔒 Privacy Policy
        SettingItem(
            title = "Privacy Policy",
            description = "Read our privacy policy.",
            leadingIcon = {
                Icon(Icons.Filled.Policy, null, tint = MaterialTheme.colorScheme.secondary)
            },
            control = {
                Icon(
                    Icons.Filled.ChevronRight,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            onClick = {
                soundEffectManager.playClickSound()
                showPrivacyDialog = true
            }
        )

        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                icon = { Icon(Icons.Filled.Policy, null) },
                title = { Text("Privacy Policy") },
                text = { Text("We respect your privacy. Full policy details coming soon.") },
                confirmButton = {
                    TextButton(onClick = {
                        soundEffectManager.playClickSound()
                        showPrivacyDialog = false
                    }) { Text("Close") }
                }
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

        // 📝 Changelog
        SettingItem(
            title = "Changelog",
            description = "See what's new.",
            leadingIcon = {
                Icon(Icons.Filled.HistoryEdu, null, tint = MaterialTheme.colorScheme.secondary)
            },
            control = {
                Icon(
                    Icons.Filled.ChevronRight,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            onClick = {
                soundEffectManager.playClickSound()
                showChangelogDialog = true
            }
        )

        if (showChangelogDialog) {
            AlertDialog(
                onDismissRequest = { showChangelogDialog = false },
                icon = { Icon(Icons.Filled.HistoryEdu, null) },
                title = { Text("What's New - v${BuildConfig.VERSION_NAME}") },
                text = { Text("✨ Improved UI, performance, and added sound settings.") },
                confirmButton = {
                    TextButton(onClick = {
                        soundEffectManager.playClickSound()
                        showChangelogDialog = false
                    }) { Text("Awesome!") }
                }
            )
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

        SettingItem(
            title = "App Version",
            description = "${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
            leadingIcon = {
                Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.secondary)
            },
            control = {}
        )
    }
}

@Composable
fun CustomSoundSeekBar(
    sharedPrefsManager: SharedPreferencesManager,
    audioManager: AudioManager
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // 🎚️ Load previous sound level or default 0.5
    var soundLevel by remember { mutableStateOf(sharedPrefsManager.getSoundLevel() ?: 0.5f) }
    val animatedLevel by animateFloatAsState(targetValue = soundLevel, animationSpec = tween(200))

    val trackHeight: Dp = 6.dp
    val thumbRadius: Dp = 12.dp
    val padding: Dp = 12.dp

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .shadow(6.dp, RoundedCornerShape(24.dp))
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // 🎨 Canvas for Samsung-style SeekBar
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .pointerInput(Unit) {
                    // Tap gesture to jump sound level
                    detectTapGestures { pos ->
                        val widthPx = size.width - with(density) { padding.toPx() * 2f }
                        val x = (pos.x - with(density) { padding.toPx() }).coerceIn(0f, widthPx)
                        val newLevel = (x / widthPx).coerceIn(0f, 1f)
                        scope.launch {
                            soundLevel = newLevel
                            sharedPrefsManager.setSoundLevel(newLevel)
                            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            val newVol = (newLevel * maxVol).toInt().coerceIn(0, maxVol)
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                        }
                    }
                }
                .pointerInput(Unit) {
                    // Drag gesture to slide volume
                    detectDragGestures { change, _ ->
                        val localX = change.position.x
                        val widthPx = size.width - with(density) { padding.toPx() * 2f }
                        val x = (localX - with(density) { padding.toPx() }).coerceIn(0f, widthPx)
                        val newLevel = (x / widthPx).coerceIn(0f, 1f)
                        scope.launch {
                            soundLevel = newLevel
                            sharedPrefsManager.setSoundLevel(newLevel)
                            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            val newVol = (newLevel * maxVol).toInt().coerceIn(0, maxVol)
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                        }
                    }
                }
        ) {
            val trackHeightPx = with(density) { trackHeight.toPx() }
            val thumbPx = with(density) { thumbRadius.toPx() }
            val padPx = with(density) { padding.toPx() }
            val widthPx = size.width - padPx * 2f
            val centerY = size.height / 2f

            val activeWidth = animatedLevel * widthPx
            val thumbX = padPx + activeWidth
            val thumbY = centerY

            // 🎨 Active gradient (Blue → Green like Samsung One UI 8.5)
            val activeBrush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF2D92FF), Color(0xFF04A915)),
                startX = padPx,
                endX = padPx + widthPx
            )

            // Inactive track
            drawRoundRect(
                color = Color(0xFFDDE3E9),
                topLeft = Offset(padPx, centerY - trackHeightPx / 2f),
                size = Size(widthPx, trackHeightPx),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeightPx / 2f)
            )

            // Active track
            drawRoundRect(
                brush = activeBrush,
                topLeft = Offset(padPx, centerY - trackHeightPx / 2f),
                size = Size(activeWidth, trackHeightPx),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeightPx / 2f)
            )

            // Glow effect
            drawCircle(Color(0x332D92FF), thumbPx * 1.6f, Offset(thumbX, thumbY))
            // Thumb body
            drawCircle(Color.White, thumbPx, Offset(thumbX, thumbY))
            // Thumb border
            drawCircle(Color(0xFFB8C3D6), thumbPx, Offset(thumbX, thumbY), style = Stroke(2f))
        }

        // 🔢 Percentage display on right
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
                .background(Color(0x12000000), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${(soundLevel * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
