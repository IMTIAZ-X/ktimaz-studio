package com.ktimazstudio.managers

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Debug
import android.provider.Settings
import android.telephony.TelephonyManager
import com.ktimazstudio.BuildConfig
import com.ktimazstudio.enums.SecurityIssue
import com.ktimazstudio.utils.CryptoUtils
import com.ktimazstudio.utils.DeviceFingerprinting
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.experimental.and

/**
 * Advanced Security Manager with comprehensive threat detection
 * Implements multiple layers of protection against reverse engineering and tampering
 */
class SecurityManager(private val context: Context) {
    
    companion object {
        private const val TAG = "SecurityManager"
        private const val SECURITY_CHECK_INTERVAL = 5000L
        private const val MAX_SECURITY_VIOLATIONS = 3
    }
    
    // Security state tracking
    private val securityBreached = AtomicBoolean(false)
    private val violationCount = AtomicLong(0)
    private val lastCheckTime = AtomicLong(System.currentTimeMillis())
    
    // Crypto utilities for secure operations
    private val cryptoUtils = CryptoUtils()
    
    // Coroutine scope for background security checks
    private val securityScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var vpnNetworkCallback: ConnectivityManager.NetworkCallback? = null

    init {
        startPeriodicSecurityChecks()
    }
    
    private fun startPeriodicSecurityChecks() {
        securityScope.launch {
            while (isActive) {
                delay(SECURITY_CHECK_INTERVAL)
                performSecurityCheck(isInspectionMode = false)
            }
        }
    }
    
    fun performSecurityCheck(isInspectionMode: Boolean): SecurityIssue {
        if (isInspectionMode) return SecurityIssue.NONE
        
        lastCheckTime.set(System.currentTimeMillis())
        
        val issue = when {
            isDebuggerAttached() -> SecurityIssue.DEBUGGER_ATTACHED
            isEmulatorDetected() -> SecurityIssue.EMULATOR_DETECTED
            isDeviceRooted() -> SecurityIssue.ROOT_DETECTED
            isApkTampered() -> SecurityIssue.APK_TAMPERED
            isHookingFrameworkDetected() -> SecurityIssue.HOOKING_FRAMEWORK_DETECTED
            isVpnActive() -> SecurityIssue.VPN_ACTIVE
            else -> SecurityIssue.NONE
        }
        
        if (issue != SecurityIssue.NONE) {
            recordSecurityViolation()
        }
        
        return issue
    }

    private fun isDebuggerAttached(): Boolean {
        return Debug.isDebuggerConnected()
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
            val signature = packageInfo.signatures.firstOrNull()
            
            val md = MessageDigest.getInstance("SHA-256")
            val hash = md.digest(signature?.toByteArray() ?: ByteArray(0))
            val hexString = hash.joinToString("") { "%02x".format(it and 0xFF.toByte()) }

            hexString != BuildConfig.SECURITY_HASH
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
    
    private fun isVpnActive(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    }
    
    fun registerVpnDetectionCallback(): ConnectivityManager.NetworkCallback {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            .build()
            
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                recordSecurityViolation()
            }
        }
        
        connectivityManager.registerNetworkCallback(networkRequest, callback)
        vpnNetworkCallback = callback
        return callback
    }

    fun unregisterVpnDetectionCallback(networkCallback: ConnectivityManager.NetworkCallback) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    /**
     * Security violation tracking
     */
    private fun recordSecurityViolation() {
        val currentViolations = violationCount.incrementAndGet()
        if (currentViolations >= MAX_SECURITY_VIOLATIONS) {
            securityBreached.set(true)
        }
    }

    /**
     * Get security status for monitoring
     */
    fun getSecurityStatus(): SecurityStatus {
        return SecurityStatus(
            isSecure = !securityBreached.get(),
            violationCount = violationCount.get().toInt(),
            lastCheckTime = lastCheckTime.get(),
            deviceFingerprint = DeviceFingerprinting.generateDeviceFingerprint(context)
        )
    }

    /**
     * Cleanup resources
     */
    fun cleanup() {
        vpnNetworkCallback?.let { callback ->
            unregisterVpnDetectionCallback(callback)
        }
        securityScope.cancel()
    }

    /**
     * Security status data class
     */
    data class SecurityStatus(
        val isSecure: Boolean,
        val violationCount: Int,
        val lastCheckTime: Long,
        val deviceFingerprint: String
    )
}