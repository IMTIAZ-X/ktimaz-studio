package com.ktimazstudio

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Application class for global initialization
 */
class KtimazApplication : Application() {
    
    companion object {
        private var instance: KtimazApplication? = null
        
        fun getInstance(): KtimazApplication {
            return instance ?: throw IllegalStateException("Application not initialized")
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize global components
        initializeSecurity()
        initializeLogging()
    }
    
    private fun initializeSecurity() {
        // Initialize security components if needed
    }
    
    private fun initializeLogging() {
        // Initialize logging framework
        if (BuildConfig.DEBUG) {
            // Enable verbose logging in debug builds
        }
    }
    
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        // Anti-hooking protection could go here
    }
}

@Composable
fun LocalApplication(): KtimazApplication {
    val context = LocalContext.current
    return context.applicationContext as KtimazApplication
}