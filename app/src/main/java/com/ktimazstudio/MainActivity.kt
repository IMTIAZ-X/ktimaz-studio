package com.ktimazstudio

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ktimazstudio.ui.theme.ktimaz
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        detectCheatToolsAndVpn()

        setContent {
            ktimaz {
                MainScreen()
            }
        }
    }

    private fun detectCheatToolsAndVpn() {
        val cheatTools = listOf("frida", "radare2", "ghidra", "jadx", "apktool", "androbugs", "androguard")
        val pm = packageManager
        cheatTools.forEach {
            try {
                pm.getPackageInfo(it, 0)
                showDetectedDialogAndExit("Cheat tool detected: $it")
                return
            } catch (_: Exception) {}
        }

        if (isVpnActive(this)) {
            showDetectedDialogAndExit("VPN is active. App will close.")
        }
    }

    private fun isVpnActive(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.allNetworks.any {
            cm.getNetworkCapabilities(it)?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
        }
    }

    private fun showDetectedDialogAndExit(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            finishAffinity()
            exitProcess(0)
        }, 5000)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        if (!isInternetAvailable(context)) {
            snackbarHostState.showSnackbar("No Internet. Turning on Wi-Fi...")
            enableWifi(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.app_name),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CardGrid("Call", R.mipmap.ic_launcher, {}, "Message", R.mipmap.ic_launcher) {
                context.startActivity(Intent(context, ComingActivity::class.java))
            }
            CardGrid("Nagad", R.mipmap.ic_launcher, {}, "IP Scan", R.mipmap.ic_launcher, {})
            CardGrid("Movies", R.mipmap.ic_launcher, {}, "Player", R.mipmap.ic_launcher, {})
        }
    }
}

@Composable
fun CardGrid(
    title1: String,
    icon1: Int,
    onClick1: () -> Unit,
    title2: String,
    icon2: Int,
    onClick2: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CardItem(title1, painterResource(id = icon1), onClick1, Modifier.weight(1f))
        CardItem(title2, painterResource(id = icon2), onClick2, Modifier.weight(1f))
    }
}

@Composable
fun CardItem(title: String, icon: Painter, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        )
                    )
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(icon, contentDescription = title, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

fun isInternetAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
    return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
}

fun enableWifi(context: Context) {
    try {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
        if (!wifiManager.isWifiEnabled) wifiManager.isWifiEnabled = true
    } catch (_: Exception) {}
}
