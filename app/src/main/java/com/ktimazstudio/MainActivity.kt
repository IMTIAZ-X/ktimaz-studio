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
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ktimazstudio.ui.theme.UltraSmoothColorScheme
import com.ktimazstudio.ui.theme.ktimaz
import java.io.BufferedReader
import java.io.FileReader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (detectVpn() || detectKnownTools()) {
            Toast.makeText(this, "Security tool or VPN detected. Closing...", Toast.LENGTH_LONG).show()
            Handler().postDelayed({ finishAffinity() }, 5000)
            return
        }

        setContent {
            ktimaz(colorScheme = UltraSmoothColorScheme) {
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
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            },
                            colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent)
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    containerColor = Color.Transparent
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        CardGrid { title ->
                            if (title == "Message") startActivity(Intent(this@MainActivity, ComingActivity::class.java))
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

    private fun detectKnownTools(): Boolean {
        val tools = listOf("frida", "radare2", "ghidra", "apktool", "androg", "xposed", "substrate")
        val processList = Runtime.getRuntime().exec("ps").inputStream.bufferedReader().readText()
        return tools.any { processList.contains(it, ignoreCase = true) }
    }
}

@Composable
fun CardGrid(onCardClick: (String) -> Unit) {
    val cards = listOf("Test", "Image", "Movie", "Video", "Note", "Web", "Scan", "Design", "Music", "AI", "Message")
    val icons = List(cards.size) { R.mipmap.ic_launcher }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(cards) { index, title ->
            var scale by remember { mutableStateOf(1f) }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        shadowElevation = 12f
                    }
                    .clickable {
                        scale = 0.96f
                        Handler().postDelayed({
                            scale = 1f
                            onCardClick(title)
                        }, 120)
                    },
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.05f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = icons[index]),
                        contentDescription = title,
                        modifier = Modifier.size(64.dp).alpha(0.9f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
    }
}
