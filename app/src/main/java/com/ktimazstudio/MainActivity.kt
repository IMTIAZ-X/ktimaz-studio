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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ktimazstudio.ui.theme.AppTheme
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
            AppTheme {
                val context = LocalContext.current
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(Unit) {
                    if (!isConnected(context)) {
                        snackbarHostState.showSnackbar("No Internet! Enabling Wi-Fi...")
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
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                    ) {
                        AnimatedCardGrid {
                            context.startActivity(Intent(context, ComingActivity::class.java))
                        }
                    }
                }
            }
        }
    }

    private fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetwork?.let {
            cm.getNetworkCapabilities(it)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } ?: false
    }

    private fun enableWifi(context: Context) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) wifiManager.isWifiEnabled = true
    }

    private fun detectVpn(): Boolean {
        return try {
            BufferedReader(FileReader("/proc/net/tcp")).useLines { lines ->
                lines.any { it.contains("0100007F:") }
            }
        } catch (e: Exception) {
            false
        }
    }
}

@Composable
fun AnimatedCardGrid(onCardClick: (String) -> Unit) {
    val cards = listOf("Test", "Image", "Movie", "Video", "Note", "Web", "Scan", "Design", "Music", "AI")
    val icons = List(cards.size) { painterResource(id = R.mipmap.ic_launcher) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(cards) { index, title ->
            val scale = rememberInfiniteTransition()
                .animateFloat(
                    initialValue = 0.98f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = EaseInOut),
                        repeatMode = RepeatMode.Reverse
                    )
                )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .graphicsLayer(scaleX = scale.value, scaleY = scale.value)
                    .shadow(6.dp, RoundedCornerShape(20.dp))
                    .clickable { onCardClick(title) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(painter = icons[index], contentDescription = title, modifier = Modifier.size(60.dp))
                    Text(title, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
