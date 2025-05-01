package com.ktimazstudio

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.ktimazstudio.ui.theme.ktimaz
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ktimaz {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }

        detectVPNorTools()
    }

    private fun detectVPNorTools() {
        val suspiciousPackages = listOf("org.frida", "radare", "ghidra", "apktool", "androbugs", "androguard")
        val pm = packageManager
        for (pkg in suspiciousPackages) {
            if (try {
                    pm.getPackageInfo(pkg, 0); true
                } catch (e: Exception) {
                    false
                }
            ) {
                Toast.makeText(this, "Unauthorized tool detected! Closing...", Toast.LENGTH_LONG).show()
                window.decorView.postDelayed({ finishAffinity() }, 5000)
                break
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (!isConnected(context)) {
            scope.launch {
                snackbarHostState.showSnackbar("No internet! Trying to enable Wi-Fi...")
                enableWiFi(context)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.Black
                )
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CardGrid()
        }
    }
}

@Composable
fun CardGrid() {
    val context = LocalContext.current
    val cards = listOf(
        "Call" to R.mipmap.ic_launcher,
        "Message" to R.mipmap.ic_launcher,
        "Nagad" to R.mipmap.ic_launcher,
        "IP Scan" to R.mipmap.ic_launcher,
        "Movies" to R.mipmap.ic_launcher,
        "Player" to R.mipmap.ic_launcher,
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        cards.chunked(2).forEach { rowCards ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowCards.forEach { (label, icon) ->
                    Card(
                        modifier = Modifier
                            .size(140.dp)
                            .clickable {
                                if (label == "Message") {
                                    context.startActivity(Intent(context, ComingActivity::class.java))
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F1EC)),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = icon),
                                contentDescription = label,
                                modifier = Modifier.size(48.dp),
                                colorFilter = ColorFilter.tint(Color.DarkGray)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = label, color = Color.DarkGray)
                        }
                    }
                }
            }
        }
    }
}

fun isConnected(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

fun enableWiFi(context: Context) {
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    if (!wifiManager.isWifiEnabled) {
        wifiManager.isWifiEnabled = true
    }
}
