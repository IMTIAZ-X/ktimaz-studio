package com.ktimazstudio.managers

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Debug
import com.ktimazstudio.enums.SecurityIssue
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.security.MessageDigest
import kotlin.experimental.and

/**
 * Utility class for performing various security checks on the application's environment.
 * These checks are designed to detect common reverse engineering, tampering, and
 * undesirable network conditions like VPN usage.
 *
 * NOTE: Client-side security checks are never foolproof and can be bypassed by
 * determined attackers. They serve as deterrents and indicators of compromise.
 */
class SecurityManager(private val context: Context) {

    // Known good hash of the APK (replace with your actual app's release APK hash)
    // You would typically calculate this hash for your *release* APK and hardcode it here.
    // For demonstration, this is a placeholder.
    // --- IMPORTANT: UPDATE THIS HASH TO YOUR APP'S RELEASE SIGNATURE SHA-256 HASH ---
    // You provided this in your last message: f21317d4d6276ff3174a363c7fdff4171c73b1b80a82bb9082943ea9200a8425
    private val EXPECTED_APK_HASH = "f21317d4d6276ff3174a363c7fdff4171c73b1b80a82bb9082943ea9200a8425".lowercase()

    /**
     * Checks if a debugger is currently attached to the application process.
     * return true if a debugger is connected, false otherwise.
     */
    fun isDebuggerConnected(): Boolean {
        // LocalInspectionMode.current check is handled at the call site in Composable
        return Debug.isDebuggerConnected() || isTracerAttached()
    }

    /**
     * Checks if a VPN connection is active.
     * This method iterates through all active networks and checks for the VPN transport.
     * return true if a VPN is detected and it has internet capabilities, false otherwise.
     */
    @Suppress("DEPRECATION")
    fun isVpnActive(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.allNetworks.forEach { network ->
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                // Ensure the VPN is actually providing internet
                if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Registers a NetworkCallback to listen for real-time VPN status changes.
     * @param onVpnStatusChanged Callback to be invoked when VPN status changes.
     * return The registered NetworkCallback instance, which should be unregistered later.
     */
    fun registerVpnDetectionCallback(onVpnStatusChanged: (Boolean) -> Unit): ConnectivityManager.NetworkCallback {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // Build a NetworkRequest specifically for VPN transport
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_VPN) // Explicitly look for VPN
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                // When a network becomes available, re-check overall VPN status
                onVpnStatusChanged(isVpnActive())
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                // When a network is lost, re-check if any VPN is still active
                onVpnStatusChanged(isVpnActive())
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                onVpnStatusChanged(isVpnActive())
            }
        }
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        return networkCallback
    }

    /**
     * Unregisters a previously registered NetworkCallback.
     * @param networkCallback The callback to unregister.
     */
    fun unregisterVpnDetectionCallback(networkCallback: ConnectivityManager.NetworkCallback) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    /**
     * Attempts to detect if the application is running on an emulator.
     * This check is not exhaustive and can be bypassed.
     * return true if an emulator is likely detected, false otherwise.
     */
    fun isRunningOnEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || "google_sdk" == Build.PRODUCT)
    }

    /**
     * Attempts to detect if the device is rooted.
     * This check is not exhaustive and can be bypassed.
     * return true if root is likely detected, false otherwise.
     */
    fun isDeviceRooted(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }

        // Check for test-keys in build tags (common for custom ROMs/rooted devices)
        if (Build.TAGS != null && Build.TAGS.contains("test-keys")) {
            return true
        }

        // Check if `su` command can be executed
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val reader = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
            if (reader.readLine() != null) return true
        } catch (e: Exception) {
            // Command not found or other error, likely not rooted
        } finally {
            process?.destroy()
        }

        return false
    }

    /**
     * Calculates the SHA-256 hash of the application's *signing certificate*.
     * This is a more robust integrity check than file hash as it remains constant
     * for signed APKs regardless of minor build variations.
     * return The SHA-256 hash as a hexadecimal string, or null if calculation fails.
     */
     fun getSignatureSha256Hash(): String? {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            if (signatures != null && signatures.isNotEmpty()) {
                val md = MessageDigest.getInstance("SHA-256")
                // For most apps, there's only one signing certificate. If multiple, you might need to handle.
                val hashBytes = md.digest(signatures[0].toByteArray())
                return hashBytes.joinToString("") { "%02x".format(it.and(0xff.toByte())) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * REMOVED: This method is no longer used for integrity check, as signature hash is more reliable.
     * Kept for reference or if needed for other purposes.
     *
     * Calculates the SHA-256 hash of the application's APK file.
     * This can be used to detect if the APK has been tampered with.
     * return The SHA-256 hash as a hexadecimal string, or null if calculation fails.
     */
    fun getApkSha256Hash_UNUSED(): String? {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val apkPath = packageInfo.applicationInfo?.sourceDir ?: return null
            val file = File(apkPath)
            if (file.exists()) {
                val bytes = file.readBytes()
                val digest = MessageDigest.getInstance("SHA-256")
                val hashBytes = digest.digest(bytes)
                return hashBytes.joinToString("") { "%02x".format(it and 0xff.toByte()) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Attempts to detect common hooking frameworks (like Xposed or Frida) by checking
     * for known files, installed packages, or system properties.
     * This is not exhaustive and can be bypassed, but adds a layer of defense.
     * return true if a hooking framework is likely detected, false otherwise.
     */
    fun isHookingFrameworkDetected(): Boolean {
        // LocalInspectionMode.current check is handled at the call site in Composable
        // 1. Check for common Xposed/Magisk/Frida related files/directories
        val knownHookFiles = arrayOf(
            "/system/app/XposedInstaller.apk",
            "/system/bin/app_process_xposed",
            "/system/lib/libxposed_art.so",
            "/data/app/de.robv.android.xposed.installer",
            "/data/data/de.robv.android.xposed.installer",
            "/dev/frida", // Frida device file
            "/data/local/tmp/frida-agent.so", // Common Frida agent path
            "/data/local/frida/frida-server", // Frida server path
            "/sbin/magisk", // Magisk detection
            "/system/xbin/magisk"
        )
        for (path in knownHookFiles) {
            if (File(path).exists()) return true
        }

        // 2. Check for common system properties (related to Xposed)
        val props = listOf("xposed.active", "xposed.api_level", "xposed.installed")
        try {
            val process = Runtime.getRuntime().exec("getprop")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (true) {
                line = reader.readLine()
                if (line == null) break
                for (prop in props) {
                    if (line.contains("[$prop]:")) return true
                }
            }
            process.destroy()
        } catch (e: Exception) {
            // Log.e("SecurityCheck", "Error checking system properties: ${e.message}")
        }

        // 3. Check for common packages (Xposed installer)
        try {
            context.packageManager.getPackageInfo("de.robv.android.xposed.installer", PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            // Package not found, which is good
        } catch (e: Exception) {
            // Log.e("SecurityCheck", "Error checking Xposed installer package: ${e.message}")
        }

        // 4. Check for suspicious loaded libraries (less reliable, but adds another layer)
        // This would require reading /proc/self/maps and checking for known hooking library names.
        // This is more complex and might lead to false positives, so omitted for brevity.

        return false
    }

    /**
     * Checks if the APK's signature hash matches the expected hash.
     * This is now the primary integrity check.
     * return true if the signature hash matches, false otherwise.
     */
    fun isApkTampered(): Boolean {
        // LocalInspectionMode.current check is handled at the call site in Composable
        val currentSignatureHash = getSignatureSha256Hash()
        // Compare with the signature SHA-256 hash provided by you.
        return currentSignatureHash != null && currentSignatureHash.lowercase() != EXPECTED_APK_HASH.lowercase()
    }

    /**
     * Gets the size of the installed application (APK + data).
     * This can be used as a very basic indicator of tampering if the size changes unexpectedly.
     * return The app size in bytes, or -1 if unable to retrieve.
     */
    fun getAppSize(): Long {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            // Safely access applicationInfo.sourceDir as applicationInfo can be null
            val apkPath = packageInfo.applicationInfo?.sourceDir ?: return -1L
            val file = File(apkPath)
            return file.length()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return -1L
    }

    fun isTracerAttached(): Boolean {
        // LocalInspectionMode.current check is handled at the call site in Composable
        try {
            val statusFile = File("/proc/self/status")
            if (statusFile.exists()) {
                statusFile.bufferedReader().useLines { lines ->
                    val tracerPidLine = lines.firstOrNull { it.startsWith("TracerPid:") }
                    if (tracerPidLine != null) {
                        val pid = tracerPidLine.substringAfter("TracerPid:").trim().toInt()
                        return pid != 0
                    }
                }
            }
        } catch (e: Exception) {
            // Log.e("SecurityManager", "Error checking TracerPid: ${e.message}")
        }
        return false
    }

    /**
     * Aggregates all security checks to determine if the app environment is secure.
     * @param isInspectionMode True if the app is running in a Compose preview/inspection mode.
     * return A SecurityIssue enum indicating the first detected issue, or SecurityIssue.NONE if secure.
     */
    fun getSecurityIssue(isInspectionMode: Boolean): SecurityIssue {
        if (isInspectionMode) {
            return SecurityIssue.NONE // Skip security checks in inspection mode
        }

        if (isDebuggerConnected()) return SecurityIssue.DEBUGGER_ATTACHED
        if (isTracerAttached()) return SecurityIssue.DEBUGGER_ATTACHED // More robust debugger check
        if (isRunningOnEmulator()) return SecurityIssue.EMULATOR_DETECTED
        if (isDeviceRooted()) return SecurityIssue.ROOT_DETECTED
        if (isHookingFrameworkDetected()) return SecurityIssue.HOOKING_FRAMEWORK_DETECTED
        if (isApkTampered()) return SecurityIssue.APK_TAMPERED
        if (isVpnActive()) return SecurityIssue.VPN_ACTIVE // Initial VPN check as well
        // Add other checks here as needed
        return SecurityIssue.NONE
    }
}