package com.ktimazstudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.ktimazstudio.managers.ApplicationManager
import com.ktimazstudio.ui.theme.ktimaz
import kotlinx.coroutines.launch

/**
 * Minimal MainActivity - Core logic moved to ApplicationManager
 * This activity only handles basic initialization and delegates to ApplicationManager
 */
class MainActivity : ComponentActivity() {
    private lateinit var applicationManager: ApplicationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Initialize application manager with minimal setup
        applicationManager = ApplicationManager(this, lifecycleScope)
        
        setContent {
            // Get theme from application manager
            val isDarkTheme by applicationManager.isDarkTheme.collectAsState()
            
            ktimaz(darkTheme = isDarkTheme) {
                // Delegate UI composition to ApplicationManager
                applicationManager.ComposeApplication()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        applicationManager.cleanup()
    }
}