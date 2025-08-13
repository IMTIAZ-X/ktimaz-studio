package com.ktimazstudio.utils

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
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.experimental.and

/**
 * Enhanced Security Manager with advanced anti-tampering measures
 * This class implements multiple layers of security checks and obfuscation
 */
class EnhancedSecurityManager(private val context: Context) {
    
    // Obfuscated strings using XOR encoding
    private val obfuscatedStrings = ObfuscatedStrings()
    
    // Security state tracking
    private val securityBreached = AtomicBoolean(false)
    private val lastSecurityCheck = System.currentTimeMillis()
    
    // Expected signature hash (should be calculated at build time)
    private val expectedSignature = "f21317d4d6276ff3174a363c7fdff4171c73b1b80a82bb9082943ea9200a8425"
    
    // Anti-hooking measures
    private val antiHookChecks = AntiHookingDetection()
    
    /**
     * Comprehensive security check with multiple validation layers
     */
    fun getSecurityIssue(isInspectionMode: Boolean): SecurityIssue {
        if (isInspectionMode) return SecurityIssue.NONE
        
        // Perform integrity self-check first
        if (!performIntegrityCheck()) {
            return SecurityIssue.APK_TAMPERED
        }
        
        // Check for debugging
        if (isDebuggerDetected()) {
            securityBreached.set(true)
            return SecurityIssue.DEBUGGER_ATTACHED
        }
        
        // Check for emulator
        if (isEmulatorDetected()) {
            return SecurityIssue.EMULATOR_DETECTED
        }
        
        // Check for root
        if (isRootDetected()) {
            return SecurityIssue.ROOT_DETECTED
        }
        
        // Check for hooking frameworks
        if (isHookingDetected()) {
            return SecurityIssue.HOOKING_FRAMEWORK_DETECTED
        }
        
        // Check VPN
        if (isVpnActive()) {
            return SecurityIssue.VPN_ACTIVE
        }
        
        // Advanced tampering detection
        if (isAdvancedTamperingDetected()) {
            return SecurityIssue.APK_TAMPERED
        }
        
        return SecurityIssue.NONE
    }
    
    /**
     * Enhanced debugger detection with multiple methods
     */
    private fun isDebuggerDetected(): Boolean {
        // Method 1: Standard debugger check
        if (Debug.isDebuggerConnected()) return true
        
        // Method 2: TracerPid check
        if (isTracerPidNonZero()) return true
        
        // Method 3: Timing-based detection
        if (isTimingAttackDetected()) return true
        
        // Method 4: Thread count anomaly
        if (isThreadCountAnomalous()) return true
        
        return false
    }
    
    private fun isTracerPidNonZero(): Boolean {
        return try {
            val statusFile = File(obfuscatedStrings.getProcStatusPath())
            if (statusFile.exists()) {
                statusFile.bufferedReader().useLines { lines ->
                    val tracerLine = lines.firstOrNull { it.startsWith(obfuscatedStrings.getTracerPidPrefix()) }
                    tracerLine?.let {
                        val pid = it.substringAfter(obfuscatedStrings.getTracerPidPrefix()).trim().toIntOrNull() ?: 0
                        return pid != 0
                    }
                }
            }
            false
        } catch (e: Exception) {
            true // Suspicious if we can't check
        }
    }
    
    private fun isTimingAttackDetected(): Boolean {
        val startTime = System.nanoTime()
        
        // Perform some calculations
        var result = 0
        for (i in 0..1000) {
            result += i * 2
        }
        
        val endTime = System.nanoTime()
        val duration = endTime - startTime
        
        // If execution is too slow, might indicate debugging
        return duration > 10_000_000 // 10ms threshold
    }
    
    private fun isThreadCountAnomalous(): Boolean {
        val threadCount = Thread.activeCount()
        // Normal apps typically have 10-20 threads, debuggers add more
        return threadCount > 30
    }
    
    /**
     * Enhanced emulator detection
     */
    private fun isEmulatorDetected(): Boolean {
        // Hardware characteristics
        if (isEmulatorByHardware()) return true
        
        // Build properties
        if (isEmulatorByBuild()) return true
        
        // File system checks
        if (isEmulatorByFiles()) return true
        
        // Sensor checks
        if (isEmulatorBySensors()) return true
        
        return false
    }
    
    private fun isEmulatorByHardware(): Boolean {
        val brand = Build.BRAND
        val device = Build.DEVICE
        val model = Build.MODEL
        val hardware = Build.HARDWARE
        val product = Build.PRODUCT
        
        val emulatorIndicators = listOf(
            "generic", "unknown", "emulator", "genymotion", "vbox86p",
            "google_sdk", "android_x86", "x86", "goldfish", "ranchu"
        )
        
        return emulatorIndicators.any { indicator ->
            brand.contains(indicator, true) ||
            device.contains(indicator, true) ||
            model.contains(indicator, true) ||
            hardware.contains(indicator, true) ||
            product.contains(indicator, true)
        }
    }
    
    private fun isEmulatorByBuild(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.contains("vbox") ||
                Build.FINGERPRINT.contains("genymotion") ||
                Build.TAGS.contains("test-keys"))
    }
    
    private fun isEmulatorByFiles(): Boolean {
        val emulatorFiles = listOf(
            "/dev/socket/qemud",
            "/dev/qemu_pipe",
            "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace",
            "/system/bin/qemu-props",
            "/dev/socket/genyd",
            "/dev/socket/baseband_genyd"
        )
        
        return emulatorFiles.any { File(it).exists() }
    }
    
    private fun isEmulatorBySensors(): Boolean {
        // This would require sensor manager access - placeholder for now
        return false
    }
    
    /**
     * Enhanced root detection
     */
    private fun isRootDetected(): Boolean {
        return isSuperuserDetected() || 
               isRootAppsDetected() || 
               isRootFilesDetected() || 
               isBusyBoxDetected()
    }
    
    private fun isSuperuserDetected(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/system/app/SuperSU.apk",
            "/system/app/Kinguser.apk",
            "/data/data/eu.chainfire.supersu",
            "/data/data/com.koushikdutta.superuser",
            "/data/data/com.kingroot.kinguser"
        )
        return paths.any { File(it).exists() }
    }
    
    private fun isRootAppsDetected(): Boolean {
        val rootApps = listOf(
            "com.koushikdutta.superuser",
            "eu.chainfire.supersu",
            "com.kingroot.kinguser",
            "com.topjohnwu.magisk",
            "me.phh.superuser"
        )
        
        return rootApps.any { packageName ->
            try {
                context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }
    
    private fun isRootFilesDetected(): Boolean {
        val rootFiles = arrayOf(
            "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su",
            "/system/sd/xbin/su", "/system/bin/failsafe/su",
            "/data/local/su", "/su/bin/su"
        )
        return rootFiles.any { File(it).exists() }
    }
    
    private fun isBusyBoxDetected(): Boolean {
        val busyBoxPaths = arrayOf(
            "/system/bin/busybox", "/system/xbin/busybox",
            "/data/local/xbin/busybox", "/sbin/busybox"
        )
        return busyBoxPaths.any { File(it).exists() }
    }
    
    /**
     * Enhanced hooking framework detection
     */
    private fun isHookingDetected(): Boolean {
        return isXposedDetected() || 
               isFridaDetected() || 
               isCydiaSubstrateDetected() ||
               isLSPosedDetected() ||
               isMagiskModulesDetected()
    }
    
    private fun isXposedDetected(): Boolean {
        // Check for Xposed files
        val xposedFiles = arrayOf(
            "/system/framework/XposedBridge.jar",
            "/system/bin/app_process_xposed",
            "/system/lib/libxposed_art.so",
            "/system/lib64/libxposed_art.so"
        )
        
        if (xposedFiles.any { File(it).exists() }) return true
        
        // Check for Xposed installer
        try {
            context.packageManager.getPackageInfo("de.robv.android.xposed.installer", 0)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            // Not found, continue checking
        }
        
        // Check system properties
        return isXposedPropertyDetected()
    }
    
    private fun isXposedPropertyDetected(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("getprop")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (true) {
                line = reader.readLine()
                if (line == null) break
                if (line.contains("xposed") || line.contains("edxp")) {
                    return true
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isFridaDetected(): Boolean {
        val fridaFiles = arrayOf(
            "/data/local/tmp/frida-server",
            "/data/local/tmp/re.frida.server",
            "/sdcard/frida-server",
            "/system/bin/frida-server"
        )
        
        if (fridaFiles.any { File(it).exists() }) return true
        
        // Check for Frida ports
        return isFridaPortOpen()
    }
    
    private fun isFridaPortOpen(): Boolean {
        // Frida typically uses port 27042
        return try {
            val process = Runtime.getRuntime().exec("netstat -an")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (true) {
                line = reader.readLine()
                if (line == null) break
                if (line.contains("27042") || line.contains("frida")) {
                    return true
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isCydiaSubstrateDetected(): Boolean {
        val substrateFiles = arrayOf(
            "/Library/MobileSubstrate",
            "/usr/libexec/cydia",
            "/System/Library/LaunchDaemons/com.saurik.Cydia.Startup.plist"
        )
        return substrateFiles.any { File(it).exists() }
    }
    
    private fun isLSPosedDetected(): Boolean {
        try {
            context.packageManager.getPackageInfo("org.lsposed.manager", 0)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            // Check for LSPosed files
            val lsposedFiles = arrayOf(
                "/data/adb/lspd",
                "/data/adb/modules/riru_lsposed",
                "/data/adb/modules/zygisk_lsposed"
            )
            return lsposedFiles.any { File(it).exists() }
        }
    }
    
    private fun isMagiskModulesDetected(): Boolean {
        val magiskPaths = arrayOf(
            "/sbin/.magisk",
            "/data/adb/magisk",
            "/data/adb/modules"
        )
        return magiskPaths.any { File(it).exists() }
    }
    
    /**
     * VPN Detection
     */
    fun isVpnActive(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.allNetworks.forEach { network ->
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    return true
                }
            }
        }
        return false
    }
    
    /**
     * VPN Monitoring
     */
    fun registerVpnDetectionCallback(onVpnStatusChanged: (Boolean) -> Unit): ConnectivityManager.NetworkCallback {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                onVpnStatusChanged(isVpnActive())
            }

            override fun onLost(network: Network) {
                super.onLost(network)
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
    
    fun unregisterVpnDetectionCallback(networkCallback: ConnectivityManager.NetworkCallback) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
    
    /**
     * Advanced tampering detection
     */
    private fun isAdvancedTamperingDetected(): Boolean {
        return isSignatureTampered() || 
               isCodeIntegrityCompromised() ||
               isResourceTampered()
    }
    
    private fun isSignatureTampered(): Boolean {
        val currentSignature = getSignatureSha256Hash()
        return currentSignature == null || currentSignature.lowercase() != expectedSignature.lowercase()
    }
    
    private fun getSignatureSha256Hash(): String? {
        return try {
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
                val hashBytes = md.digest(signatures[0].toByteArray())
                hashBytes.joinToString("") { "%02x".format(it.and(0xff.toByte())) }
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    private fun isCodeIntegrityCompromised(): Boolean {
        // Check for common code injection techniques
        return try {
            val maps = File("/proc/self/maps")
            if (maps.exists()) {
                maps.readText().contains("frida") || 
                maps.readText().contains("xposed") ||
                maps.readText().contains("substrate")
            } else false
        } catch (e: Exception) {
            true // Suspicious if we can't read maps
        }
    }
    
    private fun isResourceTampered(): Boolean {
        // Check if resources have been modified
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val apkPath = packageInfo.applicationInfo?.sourceDir
            apkPath?.let { path ->
                val file = File(path)
                // Basic size check - in real implementation you'd want more sophisticated checks
                file.length() > 0 && file.canRead()
            } ?: false
        } catch (e: Exception) {
            true
        }
    }
    
    /**
     * Integrity self-check
     */
    private fun performIntegrityCheck(): Boolean {
        // Verify critical security functions haven't been hooked
        return verifyMethodIntegrity() && verifySecurityState()
    }
    
    private fun verifyMethodIntegrity(): Boolean {
        // In a real implementation, you'd verify method bytecode or use checksums
        return !securityBreached.get()
    }
    
    private fun verifySecurityState(): Boolean {
        val currentTime = System.currentTimeMillis()
        return currentTime > lastSecurityCheck
    }
}

/**
 * Obfuscated strings to make reverse engineering harder
 */
private class ObfuscatedStrings {
    // XOR key for string obfuscation
    private val xorKey = 0x5A
    
    fun getProcStatusPath(): String = deobfuscate(byteArrayOf(0x2A, 0x70, 0x74, 0x68, 0x63, 0x2A, 0x75, 0x63, 0x6C, 0x64, 0x2A, 0x75, 0x74, 0x61, 0x74, 0x75, 0x75))
    
    fun getTracerPidPrefix(): String = deobfuscate(byteArrayOf(0x20, 0x74, 0x61, 0x63, 0x63, 0x74, 0x50, 0x69, 0x64, 0x00))
    
    private fun deobfuscate(data: ByteArray): String {
        return String(data.map { (it.toInt() xor xorKey).toByte() }.toByteArray())
    }
}

/**
 * Anti-hooking detection utilities
 */
private class AntiHookingDetection {
    fun isMethodHooked(methodName: String): Boolean {
        // Placeholder for advanced anti-hooking checks
        // In real implementation, you'd check method bytecode, call stack, etc.
        return false
    }
    
    fun detectRuntimeManipulation(): Boolean {
        // Check for runtime manipulation indicators
        return false
    }
}