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
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ktimazstudio.ui.theme.ktimaz
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.FileReader

class MainActivity : ComponentActivity() {
    private val snackbarHostState = SnackbarHostState()

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
                val scope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    if (!isConnected(context)) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "No Internet! Enabling Wi-Fi...",
                                withDismissAction = true,
                                duration = SnackbarDuration.Long
                            )
                        }
                        enableWifi(context)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = stringResource(id = R.string.app_name),
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color.Transparent,
                                    titleContentColor = MaterialTheme.colorScheme.onBackground
                                ),
                                modifier = Modifier.statusBarsPadding()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            AnimatedCardGrid { cardName ->
                                context.startActivity(Intent(context, ComingActivity::class.java))
                            }
                        }

                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }
    }

    private fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetwork?.let { network ->
            cm.getNetworkCapabilities(network)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } ?: false
    }

    private fun enableWifi(context: Context) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
        }
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
fun AnimatedCardGrid(onCardClick: (String) -> Unit) {
    val cards = listOf("Test", "Image", "Movie", "Video", "Note", "Web", "Scan", "Design", "Music", "AI")
    val icons = List(cards.size) { painterResource(id = R.mipmap.ic_launcher) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(cards) { index, title ->
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(animationSpec = tween(500 + index * 100))
            ) {
                Card(
                    onClick = { onCardClick(title) },
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = icons[index],
                            contentDescription = title,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
