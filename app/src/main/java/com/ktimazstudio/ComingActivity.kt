package com.ktimazstudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ktimazstudio.ui.theme.ktimaz

class ComingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        val title = intent?.getStringExtra(EXTRA_DEV_TITLE) ?: "Coming Soon"
        val message = intent?.getStringExtra(EXTRA_DEV_MESSAGE) ?: "This feature is under active development. Thanks for your patience!"
        
        setContent {
            ktimaz {
                DevScreen(
                    title = title,
                    message = message
                )
            }
        }
    }
}