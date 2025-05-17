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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
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
                                    text = stringResource(R.string.app_name),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            colors = TopAppBarDefaults.smallTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                    ) {
                        AnimatedCardGrid { title ->
                            context.startActivity(
                                Intent(context, ComingActivity::class.java)
                                    .putExtra("CARD_TITLE", title)
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
        BufferedReader(FileReader("/proc/net/tcp")).useLines { lines ->
            lines.any { it.contains("0100007F:") } // Loopback VPN detection
        }
    } catch (_: Exception) {
        false
    }
}

@Composable
fun AnimatedCardGrid(onCardClick: (String) -> Unit) {
    val cards = listOf("Test", "Image", "Movie", "Video", "Note", "Web", "Scan", "Design", "Music", "AI")
    val icons = List(cards.size) { painterResource(R.mipmap.ic_launcher) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(cards) { index, title ->
            val transition = rememberInfiniteTransition()
            val scale by transition.animateFloat(
                initialValue = 0.97f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Card(
                onClick = { onCardClick(title) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                ),
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = icons[index],
                        contentDescription = title,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
