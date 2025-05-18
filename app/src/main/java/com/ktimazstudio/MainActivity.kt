package com.ktimazstudio

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ktimazstudio.ui.theme.ktimaz
import java.io.BufferedReader
import java.io.FileReader

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (detectVpn()) {
            Toast.makeText(this, "VPN detected. Closing...", Toast.LENGTH_LONG).show()
            Handler().postDelayed({ finishAffinity() }, 5000)
            return
        }

        setContent {
            ktimaz {
                val context = LocalContext.current
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(Unit) {
                    if (!isConnected(context)) {
                        snackbarHostState.showSnackbar("No Internet! Enabling Wi-Fi…")
                        enableWifi(context)
                    }
                }

                Scaffold(
                    topBar = {
                        SmallTopAppBar(
                            title = {
                                Text(
                                    text = stringResource(id = R.string.app_name),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            actions = {
                                IconButton(onClick = {
                                    context.startActivity(Intent(context, SettingsActivity::class.java))
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.arrow_back_ios_24),
                                        contentDescription = "Settings",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.smallTopAppBarColors(
                                containerColor = Color.Transparent
                            )
                        )
                    },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                    ) {
                        AnimatedCardGrid { title ->
                            context.startActivity(
                                Intent(context, ComingActivity::class.java).putExtra("CARD_TITLE", title)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetwork
            ?.let { cm.getNetworkCapabilities(it)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) }
            ?: false
    }

    private fun enableWifi(context: Context) {
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wm.isWifiEnabled) wm.isWifiEnabled = true
    }

    private fun detectVpn(): Boolean = try {
        BufferedReader(FileReader("/proc/net/tcp")).useLines { seq ->
            seq.any { it.contains("0100007F:") }
        }
    } catch (_: Exception) {
        false
    }
}

@Composable
fun AnimatedCardGrid(onCardClick: (String) -> Unit) {
    val cards = listOf("Test", "Image", "Movie", "Video", "Note", "Web", "Scan", "Design", "Music", "AI", "Settings")
    val icons = List(cards.size) { painterResource(id = R.mipmap.ic_launcher) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(cards) { index, title ->
            val scale by rememberInfiniteTransition().animateFloat(
                initialValue = 0.95f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Card(
                onClick = {
                    if (title == "Settings") {
                        onCardClick("Settings") // opens SettingsActivity
                    } else {
                        onCardClick(title)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = icons[index],
                        contentDescription = title,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
