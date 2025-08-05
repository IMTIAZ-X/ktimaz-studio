package com.ktimazstudio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ktimazstudio.manager.SoundEffectManager

@Composable
fun ProfileScreen(username: String, onLogout: () -> Unit, soundEffectManager: SoundEffectManager) {
    // This is a placeholder screen. You will need to add your actual profile UI here.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Welcome, $username!", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            onLogout()
            soundEffectManager.playClickSound()
        }) {
            Text(text = "Logout")
        }
    }
}
