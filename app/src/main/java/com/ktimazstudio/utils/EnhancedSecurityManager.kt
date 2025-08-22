package com.ktimazstudio.utils

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Debug
import android.provider.Settings
import com.ktimazstudio.enums.SecurityIssue
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.experimental.and
import kotlin.random.Random

class EnhancedSecurityManager(private val context: Context) {
    
    companion object {
        private const val TAG = "EnhancedSecurityManager"
        private const val SECURITY_CHECK_INTERVAL = 5000L
        private const val MAX_SECURITY_VIOLATIONS = 3
    }
    
    private val securityBreached = AtomicBoolean(false)
    private val violationCount = AtomicLong(0)
    private val lastCheckTime = AtomicLong(System.currentTimeMillis())
    private val expectedSignature = "f21317d4d6276ff3174a363c7fdff4171c73b1b80a82bb9082943ea9200a8425"
    private var vpnNetworkCallback: ConnectivityManager.NetworkCallback? = null

    fun getSecurityIssue(isInspectionMode: Boolean): SecurityIssue {
        if (isInspectionMode) return SecurityIssue.NONE

        lastCheckTime.set(System.currentTimeMillis())

        if (isDebuggerDetected()) {
            recordSecurityViolation()
            return SecurityIssue.DEBUGGER_ATTACHED
        }

        if (isEmulatorDetected()) {
            recordSecurityViolation()
            return SecurityIssue.EMULATOR_DETECTED
        }
        
        if (isDeviceRooted()) {
            recordSecurityViolation()
            return SecurityIssue.ROOT_DETECTED
        }

        if (isApkTampered()) {
            recordSecurityViolation()
            return SecurityIssue.APK_TAMPERED
        }
        
        if (isHookingFrameworkDetected()) {
            recordSecurityViolation()
            return SecurityIssue.HOOKING_FRAMEWORK_DETECTED
        }

        // Check for VPN asynchronously
        checkVpnStatus()

        if (violationCount.get() > 0) {
             return when (violationCount.get().toInt()) {
                1 -> SecurityIssue.UNKNOWN
                2 -> SecurityIssue.UNKNOWN
                else -> SecurityIssue.UNKNOWN
            }
        }
        
        return SecurityIssue.NONE
    }

    private fun isDebuggerDetected(): Boolean {
        return Debug.isDebuggerConnected() || Debug.waitingForDebugger()
    }

    private fun isEmulatorDetected(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || "google_sdk" == Build.PRODUCT)
    }

    private fun isDeviceRooted(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        return paths.any { File(it).exists() }
    }
    
    private fun isApkTampered(): Boolean {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            val signatures = packageInfo.signatures
            if (signatures.isNullOrEmpty()) {
                return true
            }

            val signature = signatures[0]
            val md = MessageDigest.getInstance("SHA-256")
            val hash = md.digest(signature.toByteArray())
            val hexString = hash.joinToString("") { "%02x".format(it and 0xFF.toByte()) }

            hexString != expectedSignature
        } catch (e: Exception) {
            true
        }
    }
    
    private fun isHookingFrameworkDetected(): Boolean {
        val suspiciousPackages = listOf(
            "com.saurik.substrate",
            "de.robv.android.xposed.installer",
            "com.saurik.cyadia"
        )
        
        return try {
            val process = ProcessBuilder("pm", "list", "packages").start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            
            suspiciousPackages.any { output.contains(it) }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun checkVpnStatus() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            .build()
            
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                recordSecurityViolation()
                // You can add a specific SecurityIssue for VPN, e.g., SecurityIssue.VPN_ACTIVE
            }
        }
        
        connectivityManager.registerNetworkCallback(request, callback)
        vpnNetworkCallback = callback
    }

    fun getSecurityStatus(): SecurityStatus {
        return SecurityStatus(
            isSecure = !securityBreached.get(),
            violationCount = violationCount.get().toInt(),
            lastCheckTime = lastCheckTime.get(),
            deviceFingerprint = generateDeviceFingerprint()
        )
    }

    private fun generateDeviceFingerprint(): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val components = listOf(
            Build.BOARD, Build.BRAND, Build.DEVICE, Build.HARDWARE,
            Build.MANUFACTURER, Build.MODEL, Build.PRODUCT,
            Build.SUPPORTED_ABIS.joinToString(","), Build.VERSION.RELEASE,
            Build.VERSION.SDK_INT.toString(), Build.FINGERPRINT,
            androidId
        )
        val combined = components.joinToString("|")
        val hash = MessageDigest.getInstance("SHA-256").digest(combined.toByteArray())
        return android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP)
    }

    fun cleanup() {
        vpnNetworkCallback?.let { callback ->
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
    
    data class SecurityStatus(
        val isSecure: Boolean,
        val violationCount: Int,
        val lastCheckTime: Long,
        val deviceFingerprint: String
    )
}