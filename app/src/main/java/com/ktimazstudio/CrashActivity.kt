package com.ktimazstudio

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

class CrashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val log = intent.getStringExtra("log") ?: "No crash log"
        saveCrashLog(log)
        setContent {
            CrashDialogScreen()
        }
    }

    private fun saveCrashLog(log: String) {
        val logDir = File(Environment.getExternalStorageDirectory(), "${getString(R.string.app_name)}/log")
        if (!logDir.exists()) logDir.mkdirs()
        val logFile = File(logDir, "crash_${System.currentTimeMillis()}.txt")
        logFile.writeText(log)
    }
}

@Composable
fun CrashDialogScreen() {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) {
            Card(
                elevation = CardDefaults.cardElevation(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "An error occurred!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "An error occurred while running ${stringResource(id = R.string.app_name)}. " +
                                "Do you want to report this error log so that we can fix it?\n\nNo personal information will be included.",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                (context as? Activity)?.finishAffinity()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                        ) {
                            Text("Close")
                        }
                        Button(
                            onClick = {
                                val intent = Intent(context, MainActivity::class.java)
                                context.startActivity(intent)
                                (context as? Activity)?.finishAffinity()
                            }
                        ) {
                            Text("Restart")
                        }
                    }
                }
            }
        }
    }
}
