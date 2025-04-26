package com.ktimazstudio

import android.app.Activity
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ktimazstudio.ui.theme.ktimaz

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ktimaz {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KTiMAZ", color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? Activity)?.finish()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back"
                        )
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
            CardItem("Messages", R.mipmap.ic_launcher) {
                // context.startActivity(Intent(context, MessageActivity::class.java))
            }

            CardItem("V-Messages", R.mipmap.ic_launcher) {
                // context.startActivity(Intent(context, VMessageActivity::class.java))
            }

            CardItem("Screen Viewer", R.mipmap.ic_launcher) {
                // context.startActivity(Intent(context, ScreenActivity::class.java))
            }

            CardItem("Visit Website", R.mipmap.ic_launcher) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://imtiaz-x.github.io/IMTIAZDeveloper/"))
                context.startActivity(intent)
            }

            CardItem("Video Player", R.mipmap.ic_launcher) {
                // context.startActivity(Intent(context, PlayerActivity::class.java))
            }

            Spacer(modifier = Modifier.height(24.dp))

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
fun CardItem(title: String, iconResId: Int, onClick: () -> Unit) {
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
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium)
        }
    }
}
