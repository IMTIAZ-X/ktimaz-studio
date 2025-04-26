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
                title = { Text("KTiMAZ") },
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
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            CardGrid(
                title1 = "Call",
                icon1 = R.mipmap.ic_launcher,
                onClick1 = {},
                title2 = "Message",
                icon2 = R.mipmap.ic_launcher,
                onClick2 = {}
            )
            CardGrid(
                title1 = "Nagad",
                icon1 = R.mipmap.ic_launcher,
                onClick1 = {},
                title2 = "Ip scan",
                icon2 = R.mipmap.ic_launcher,
                onClick2 = {}
            )
            CardGrid(
                title1 = "Movies",
                icon1 = R.mipmap.ic_launcher,
                onClick1 = {},
                title2 = "Player",
                icon2 = R.mipmap.ic_launcher,
                onClick2 = {}
            )
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CardItem(title = title1, iconResId = icon1, onClick = onClick1)
        CardItem(title = title2, iconResId = icon2, onClick = onClick2)
    }
}

@Composable
fun CardItem(title: String, iconResId: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(160.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = title,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
