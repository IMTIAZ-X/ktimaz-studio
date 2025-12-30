package com.ktimazstudio.agent

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ktimazstudio.ui.theme.KtimazStudioTheme

class AgentActivity : ComponentActivity() {

    companion object {
        fun createIntent(context: Context): Intent = Intent(context, AgentActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KtimazStudioTheme {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    // Replace with your real Agent screen composable
                    Text(text = "Agent screen", style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
    }
}
