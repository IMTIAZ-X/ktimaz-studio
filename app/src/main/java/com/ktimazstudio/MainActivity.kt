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
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ktimazstudio.ui.theme.ktimaz
import java.io.BufferedReader
import java.io.FileReader
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (detectVpn() || detectKnownTools()) {
            Toast.makeText(this, "Security tool or VPN detected. Closing...", Toast.LENGTH_LONG).show()
            Handler().postDelayed({ finishAffinity() }, 5000)
            return
        }

        setContent {
            ktimaz {
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
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { padding ->
                    AnimatedCardGrid(Modifier.padding(padding)) {
                        context.startActivity(Intent(context, ComingActivity::class.java))
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedCardGrid(modifier: Modifier = Modifier, onCardClick: () -> Unit) {
    val cards = listOf("Test", "Image", "Movie", "Video", "Note", "Web", "Scan", "Design", "Music", "AI")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        cards.chunked(2).forEachIndexed { rowIndex, rowItems ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(500)) + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowItems.forEach { title ->
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                            tonalElevation = 4.dp,
                            modifier = Modifier
                                .weight(1f)
                                .height(140.dp)
                                .clickable { onCardClick() }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                                            )
                                        )
                                    )
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f)) // Fill 2nd cell if odd
                }
            }
        }
    }
}
