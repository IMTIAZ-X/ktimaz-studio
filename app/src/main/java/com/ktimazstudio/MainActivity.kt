
package com.ktimazstudio

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setGlobalExceptionHandler(applicationContext)
        checkSecurityThreats()

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
    val appName = stringResource(id = R.string.app_name)

    LaunchedEffect(Unit) {
        if (!isInternetAvailable(context)) {
            showNoInternetSnackbar(context)
            autoEnableWiFi(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(appName, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
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
        CardItem(title1, painterResource(icon1), onClick1, Modifier.weight(1f))
        CardItem(title2, painterResource(icon2), onClick2, Modifier.weight(1f))
    }
}

@Composable
fun CardItem(title: String, icon: Painter, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
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
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

fun isInternetAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

fun showNoInternetSnackbar(context: Context) {
    Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
}

fun autoEnableWiFi(context: Context) {
    try {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.isWifiEnabled = true
    } catch (_: Exception) {}
}

fun setGlobalExceptionHandler(context: Context) {
    Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        val crashLog = sw.toString()
        val appName = context.getString(R.string.app_name)
        val logDir = File("/storage/emulated/0/$appName/log")
        if (!logDir.exists()) logDir.mkdirs()
        val logFile = File(logDir, "crash_${System.currentTimeMillis()}.txt")
        logFile.writeText(crashLog)

        val intent = Intent(context, CrashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}

fun checkSecurityThreats() {
    val suspiciousTools = listOf("frida", "radare2", "ghidra", "jadx", "apktool", "androbugs", "androguard")
    val processes = Runtime.getRuntime().exec("ps").inputStream.bufferedReader().readLines()
    for (line in processes) {
        if (suspiciousTools.any { line.contains(it, ignoreCase = true) }) {
            showCheatToolWarning()
            break
        }
    }
}

fun showCheatToolWarning() {
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(App.instance, "Cheat tool detected!", Toast.LENGTH_LONG).show()
        Handler(Looper.getMainLooper()).postDelayed({
            android.os.Process.killProcess(android.os.Process.myPid())
        }, 5000)
    }
}
