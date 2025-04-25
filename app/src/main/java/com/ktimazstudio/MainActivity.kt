package com.ktimazstudio

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.KTiMAZ.ui.theme.KTiMAZTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KTiMAZTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KTiMAZ", color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.onBackPressedDispatcher?.onBackPressed() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Cards / Sections
            CardItem("Messages", Icons.Default.Message) {
               // context.startActivity(Intent(context, MessageActivity::class.java))
            }

            CardItem("V-Messages", Icons.Default.Email) {
              //  context.startActivity(Intent(context, VMessageActivity::class.java))
            }

            CardItem("Screen Viewer", Icons.Default.ScreenShare) {
               // context.startActivity(Intent(context, ScreenActivity::class.java))
            }

            CardItem("Scan", Icons.Default.QrCodeScanner) {
               // context.startActivity(Intent(context, ScanActivity::class.java))
            }

            CardItem("Visit Website", Icons.Default.Public) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://imtiaz-x.github.io/IMTIAZDeveloper/"))
                context.startActivity(intent)
            }

            CardItem("Video Player", Icons.Default.PlayCircleFilled) {
               // context.startActivity(Intent(context, PlayerActivity::class.java))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Extra Button
            Button(
                onClick = {
                    // Add your action logic here
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Custom Button")
            }
        }
    }
}

@Composable
fun CardItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium)
        }
    }
}
