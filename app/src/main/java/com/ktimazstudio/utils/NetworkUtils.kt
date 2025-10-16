package com.ktimazstudio.utils

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.widget.Toast

// --- Top-level utility functions ---

/**
 * Checks if the device has an active and validated internet connection.
 * @param context The application context.
 * return true if connected to the internet, false otherwise.
 */
fun isConnected(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

/**
 * Opens the Wi-Fi settings panel for the user to connect to a network.
 * Displays a toast message instructing the user.
 * @param context The application context.
 */
fun openWifiSettings(context: Context) {
    Toast.makeText(context, "Please enable Wi-Fi or connect to a network.", Toast.LENGTH_LONG).show()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // For Android 10 (Q) and above, use Settings.Panel.ACTION_WIFI for a panel.
        context.startActivity(Intent(Settings.Panel.ACTION_WIFI))
    } else {
        // For older Android versions, use ACTION_WIFI_SETTINGS.
        @Suppress("DEPRECATION")
        context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
    }
}