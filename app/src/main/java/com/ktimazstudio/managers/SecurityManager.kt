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
    
    // Expected signature hash from build config
    private val expectedSignature = BuildConfig.SECURITY_HASH
    
    // Coroutine scope for background monitoring
    private val securityScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // Network callback for VPN monitoring
    private var vpnNetworkCallback: ConnectivityManager.NetworkCallback? = null
    
    init {
        if (BuildConfig.ENABLE_SECURITY_CHECKS) {
            startContinuousMonitoring()
        }
    }

    /**
     * Comprehensive security assessment with threat scoring
     */
    fun getSecurityIssue(isInspectionMode: Boolean): SecurityIssue {
        if (isInspectionMode || !BuildConfig.ENABLE_SECURITY_CHECKS) {
            return SecurityIssue.NONE
        }

        // Update last check time
        lastCheckTime.set(System.currentTimeMillis())

        // Critical security checks (immediate threats)
        if (isDebuggerDetected()) {
            recordSecurityViolation()
            return SecurityIssue.DEBUGGER_ATTACHED
        }

        if (isAdvancedEmulatorDetected()) {
            recordSecurityViolation()
            return SecurityIssue.EMULATOR_DETECTED
        }

        if (isAdvancedRootDetected()) {
            recordSecurityViolation()
            return SecurityIssue.ROOT_DETECTED
        }

        if (isAdvancedHookingDetected()) {
            recordSecurityViolation()
            return SecurityIssue.HOOKING_FRAMEWORK_DETECTED
        }

        if (isApplicationTampered()) {
            recordSecurityViolation()
            return SecurityIssue.APK_TAMPERED
        }

        // Non-critical but monitored (VPN detection)
        if (isVpnActive()) {
            return SecurityIssue.VPN_ACTIVE
        }

        // Check for excessive violations
        if (violationCount.get() >= MAX_SECURITY_VIOLATIONS) {
            return SecurityIssue.UNKNOWN
        }

        return SecurityIssue.NONE
    }

    /**
     * Enhanced debugger detection with multiple techniques
     */
    private fun isDebuggerDetected(): Boolean {
        try {
            // Method 1: Standard Android debugging check
            if (Debug.isDebuggerConnected()) return true

            // Method 2: TracerPid analysis
            if (isTracerPidNonZero()) return true

            // Method 3: Timing-based detection
            if (isTimingAnomalous()) return true

            // Method 4: Thread analysis
            if (isThreadCountSuspicious()) return true

            // Method 5: Debug flags check
            if (isDebuggingEnabled()) return true

            // Method 6: Port monitoring
            if (areDebugPortsOpen()) return true

        } catch (e: Exception) {
            // Security check failure is suspicious
            return true
        }

        return false
    }

    private fun isTracerPidNonZero(): Boolean {
        return try {
            val statusFile = File("/proc/self/status")
            if (!statusFile.exists()) return false

            statusFile.bufferedReader().use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (line!!.startsWith("TracerPid:")) {
                        val pid = line!!.substringAfter("TracerPid:").trim().toIntOrNull() ?: 0
                        return pid != 0
                    }
                }
            }
            false
        } catch (e: Exception) {
            true // Suspicious if we can't check
        }
    }

    private fun isTimingAnomalous(): Boolean {
        val iterations = 10000
        val startTime = System.nanoTime()
        
        // Perform computational work
        var result = 0
        for (i in 0 until iterations) {
            result += i * (i % 7)
        }
        
        val duration = System.nanoTime() - startTime
        val expectedMaxDuration = iterations * 1000L // 1000ns per iteration baseline
        
        // If execution is significantly slower, might indicate debugging/analysis
        return duration > expectedMaxDuration * 5
    }

    private fun isThreadCountSuspicious(): Boolean {
        val threadCount = Thread.activeCount()
        // Normal apps: 8-25 threads, debuggers/profilers add significantly more
        return threadCount > 40
    }

    private fun isDebuggingEnabled(): Boolean {
        return try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        } catch (e: Exception) {
            false
        }
    }

    private fun areDebugPortsOpen(): Boolean {
        val suspiciousPorts = listOf("5555", "23946", "27042", "8700") // ADB, JDWP, Frida, etc.
        
        return try {
            val process = ProcessBuilder("sh", "-c", "netstat -an 2>/dev/null || ss -tuln 2>/dev/null")
                .start()
            
            val output = process.inputStream.bufferedReader().readText()
            suspiciousPorts.any { port -> output.contains(":$port") }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Advanced emulator detection with comprehensive checks
     */
    private fun isAdvancedEmulatorDetected(): Boolean {
        val detectionMethods = listOf(
            ::checkBuildProperties,
            ::checkEmulatorFiles,
            ::checkHardwareFeatures,
            ::checkTelephonyFeatures,
            ::checkSensorAvailability,
            ::checkCpuArchitecture,
            ::checkMemoryPatterns,
            ::checkNetworkConfiguration
        )

        // Require multiple positive detections to reduce false positives
        val positiveDetections = detectionMethods.count { it.invoke() }
        return positiveDetections >= 3
    }

    private fun checkBuildProperties(): Boolean {
        val suspiciousValues = mapOf(
            Build.FINGERPRINT to listOf("generic", "unknown", "emulator", "simulator", "genymotion", "vbox"),
            Build.MODEL to listOf("sdk", "emulator", "android sdk", "simulator"),
            Build.MANUFACTURER to listOf("genymotion", "unknown"),
            Build.BRAND to listOf("generic"),
            Build.DEVICE to listOf("generic", "emulator"),
            Build.PRODUCT to listOf("sdk", "google_sdk", "full_x86"),
            Build.HARDWARE to listOf("goldfish", "ranchu", "vbox86")
        )

        return suspiciousValues.any { (property, suspiciousTerms) ->
            suspiciousTerms.any { term -> property.contains(term, ignoreCase = true) }
        }
    }

    private fun checkEmulatorFiles(): Boolean {
        val emulatorFiles = listOf(
            "/dev/socket/qemud",
            "/dev/qemu_pipe",
            "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace",
            "/system/bin/qemu-props",
            "/dev/socket/genyd",
            "/dev/socket/baseband_genyd",
            "/dev/goldfish_sync",
            "/dev/goldfish_tty",
            "/proc/tty/drivers"
        )

        return emulatorFiles.any { File(it).exists() }
    }

    private fun checkHardwareFeatures(): Boolean {
        val packageManager = context.packageManager
        val missingFeatures = listOf(
            PackageManager.FEATURE_TELEPHONY,
            PackageManager.FEATURE_CAMERA,
            PackageManager.FEATURE_BLUETOOTH,
            PackageManager.FEATURE_NFC
        )

        // Real devices typically have most hardware features
        val missingCount = missingFeatures.count { !packageManager.hasSystemFeature(it) }
        return missingCount >= 3
    }

    private fun checkTelephonyFeatures(): Boolean {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val deviceId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                telephonyManager.imei ?: ""
            } else {
                @Suppress("DEPRECATION")
                telephonyManager.deviceId ?: ""
            }
            
            // Emulators often have predictable or missing IMEIs
            deviceId.isEmpty() || 
            deviceId == "000000000000000" || 
            deviceId.all { it == '0' } ||
            telephonyManager.networkOperatorName.contains("android", ignoreCase = true)
        } catch (e: Exception) {
            true
        }
    }

    private fun checkSensorAvailability(): Boolean {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
        val criticalSensors = listOf(
            android.hardware.Sensor.TYPE_ACCELEROMETER,
            android.hardware.Sensor.TYPE_GYROSCOPE,
            android.hardware.Sensor.TYPE_MAGNETIC_FIELD
        )
        
        val availableSensors = criticalSensors.count { type ->
            sensorManager.getDefaultSensor(type) != null
        }
        
        // Real devices typically have these basic sensors
        return availableSensors < 2
    }

    private fun checkCpuArchitecture(): Boolean {
        val supportedAbis = Build.SUPPORTED_ABIS.joinToString(",")
        val suspiciousAbis = listOf("x86", "x86_64")
        
        // While legitimate, x86 is more common in emulators
        return suspiciousAbis.any { abi -> supportedAbis.contains(abi, ignoreCase = true) }
    }

    private fun checkMemoryPatterns(): Boolean {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val maxMemory = runtime.maxMemory()
        val freeMemory = runtime.freeMemory()
        
        // Emulators often have unusual memory configurations
        val memoryRatio = totalMemory.toDouble() / maxMemory
        return memoryRatio < 0.1 || memoryRatio > 0.9
    }

    private fun checkNetworkConfiguration(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            
            // Check for emulator-specific network configurations
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Advanced root detection with comprehensive analysis
     */
    private fun isAdvancedRootDetected(): Boolean {
        val detectionMethods = listOf(
            ::checkSuperuserApps,
            ::checkRootBinaries,
            ::checkRootFiles,
            ::checkSystemProperties,
            ::checkWritableSystemPaths,
            ::checkSuCommand,
            ::checkMagiskFiles,
            ::checkBusyBoxFiles
        )

        // Multiple detection methods for accuracy
        return detectionMethods.count { it.invoke() } >= 2
    }

    private fun checkSuperuserApps(): Boolean {
        val rootApps = listOf(
            "com.koushikdutta.superuser",
            "eu.chainfire.supersu",
            "com.kingroot.kinguser",
            "com.topjohnwu.magisk",
            "me.phh.superuser",
            "com.yellowes.su",
            "com.thirdparty.superuser",
            "com.koushikdutta.rommanager"
        )

        return rootApps.any { packageName ->
            try {
                context.packageManager.getPackageInfo(packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    private fun checkRootBinaries(): Boolean {
        val rootBinaries = listOf(
            "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su",
            "/system/sd/xbin/su", "/system/bin/failsafe/su",
            "/data/local/su", "/su/bin/su"
        )

        return rootBinaries.any { path ->
            File(path).exists() && File(path).canExecute()
        }
    }

    private fun checkRootFiles(): Boolean {
        val rootFiles = listOf(
            "/system/app/Superuser.apk",
            "/system/app/SuperSU.apk",
            "/system/etc/init.d/99SuperSUDaemon",
            "/system/xbin/daemonsu",
            "/system/etc/init.d/99su",
            "/data/data/com.android.shell/su"
        )

        return rootFiles.any { File(it).exists() }
    }

    private fun checkSystemProperties(): Boolean {
        val suspiciousProps = mapOf(
            "ro.debuggable" to "1",
            "ro.secure" to "0",
            "service.adb.root" to "1"
        )

        return try {
            val process = ProcessBuilder("getprop").start()
            val props = process.inputStream.bufferedReader().readText()
            
            suspiciousProps.any { (key, value) ->
                props.contains("[$key]: [$value]")
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun checkWritableSystemPaths(): Boolean {
        val systemPaths = listOf(
            "/system", "/system/bin", "/system/sbin",
            "/system/xbin", "/vendor/bin", "/sbin"
        )

        return systemPaths.any { path ->
            File(path).canWrite()
        }
    }

    private fun checkSuCommand(): Boolean {
        return try {
            val process = ProcessBuilder("which", "su").start()
            val output = process.inputStream.bufferedReader().readText().trim()
            output.isNotEmpty() && !output.contains("not found")
        } catch (e: Exception) {
            false
        }
    }

    private fun checkMagiskFiles(): Boolean {
        val magiskFiles = listOf(
            "/sbin/.magisk",
            "/data/adb/magisk",
            "/data/adb/modules",
            "/cache/.disable_magisk",
            "/dev/.magisk.unblock",
            "/cache/magisk.log",
            "/data/adb/magisk.img",
            "/data/magisk.apk"
        )

        return magiskFiles.any { File(it).exists() }
    }

    private fun checkBusyBoxFiles(): Boolean {
        val busyBoxPaths = listOf(
            "/system/bin/busybox", "/system/xbin/busybox",
            "/data/local/xbin/busybox", "/sbin/busybox",
            "/data/local/busybox", "/system/sd/xbin/busybox"
        )

        return busyBoxPaths.any { path ->
            File(path).exists() && File(path).canExecute()
        }
    }

    /**
     * Advanced hooking framework detection
     */
    private fun isAdvancedHookingDetected(): Boolean {
        return isXposedDetected() || 
               isFridaDetected() || 
               isSubstrateDetected() ||
               isLSPosedDetected() ||
               isRiruDetected() ||
               isEdXposedDetected()
    }

    private fun isXposedDetected(): Boolean {
        // File-based detection
        val xposedFiles = listOf(
            "/system/framework/XposedBridge.jar",
            "/system/bin/app_process_xposed",
            "/system/lib/libxposed_art.so",
            "/system/lib64/libxposed_art.so",
            "/data/data/de.robv.android.xposed.installer"
        )

        if (xposedFiles.any { File(it).exists() }) return true

        // Package detection
        try {
            context.packageManager.getPackageInfo("de.robv.android.xposed.installer", 0)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            // Continue with other checks
        }

        // Environment variable check
        return System.getenv("CLASSPATH")?.contains("XposedBridge") == true
    }

    private fun isFridaDetected(): Boolean {
        // File-based detection
        val fridaFiles = listOf(
            "/data/local/tmp/frida-server",
            "/data/local/tmp/re.frida.server",
            "/sdcard/frida-server",
            "/system/bin/frida-server",
            "/system/lib/frida-agent.so",
            "/system/lib64/frida-agent.so"
        )

        if (fridaFiles.any { File(it).exists() }) return true

        // Port detection
        return try {
            val process = ProcessBuilder("sh", "-c", "netstat -an | grep :27042").start()
            process.inputStream.bufferedReader().readText().isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    private fun isSubstrateDetected(): Boolean {
        val substrateFiles = listOf(
            "/Library/MobileSubstrate",
            "/usr/libexec/cydia",
            "/System/Library/LaunchDaemons/com.saurik.Cydia.Startup.plist",
            "/data/local/tmp/substrate"
        )

        return substrateFiles.any { File(it).exists() }
    }

    private fun isLSPosedDetected(): Boolean {
        // Package detection
        try {
            context.packageManager.getPackageInfo("org.lsposed.manager", 0)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            // File detection
            val lsposedFiles = listOf(
                "/data/adb/lspd",
                "/data/adb/modules/riru_lsposed",
                "/data/adb/modules/zygisk_lsposed"
            )
            return lsposedFiles.any { File(it).exists() }
        }
    }

    private fun isRiruDetected(): Boolean {
        val riruFiles = listOf(
            "/data/adb/riru",
            "/data/misc/riru",
            "/system/lib/libriru.so",
            "/system/lib64/libriru.so"
        )

        return riruFiles.any { File(it).exists() }
    }

    private fun isEdXposedDetected(): Boolean {
        return try {
            context.packageManager.getPackageInfo("org.meowcat.edxposed.manager", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            val edxposedFiles = listOf(
                "/data/adb/edxposed",
                "/system/framework/edxposed.jar"
            )
            edxposedFiles.any { File(it).exists() }
        }
    }

    /**
     * Application integrity verification
     */
    private fun isApplicationTampered(): Boolean {
        return !verifySignature() || 
               !verifyCodeIntegrity() || 
               !verifyResourceIntegrity() ||
               isDebuggingFlagsSet()
    }

    private fun verifySignature(): Boolean {
        val currentSignature = getSignatureSha256Hash()
        return currentSignature?.lowercase() == expectedSignature.lowercase()
    }

    private fun getSignatureSha256Hash(): String? {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName, 
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName, 
                    PackageManager.GET_SIGNATURES
                )
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

    private fun verifyCodeIntegrity(): Boolean {
        return try {
            val mapsFile = File("/proc/self/maps")
            if (!mapsFile.exists()) return false
            
            val mapsContent = mapsFile.readText()
            val suspiciousLibraries = listOf(
                "frida", "xposed", "substrate", "lsposed", 
                "edxposed", "riru", "zygisk"
            )
            
            !suspiciousLibraries.any { lib -> 
                mapsContent.contains(lib, ignoreCase = true) 
            }
        } catch (e: Exception) {
            false // Assume compromised if we can't verify
        }
    }

    private fun verifyResourceIntegrity(): Boolean {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val apkPath = packageInfo.applicationInfo?.sourceDir ?: return false
            val apkFile = File(apkPath)
            
            apkFile.exists() && apkFile.canRead() && apkFile.length() > 0
        } catch (e: Exception) {
            false
        }
    }

    private fun isDebuggingFlagsSet(): Boolean {
        val appInfo = context.applicationInfo
        return (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    /**
     * VPN Detection
     */
    fun isVpnActive(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return connectivityManager.allNetworks.any { network ->
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }
    }

    /**
     * Continuous security monitoring
     */
    private fun startContinuousMonitoring() {
        securityScope.launch {
            while (true) {
                delay(SECURITY_CHECK_INTERVAL)
                performBackgroundSecurityCheck()
            }
        }
    }

    private suspend fun performBackgroundSecurityCheck() = withContext(Dispatchers.Default) {
        try {
            // Quick security checks in background
            if (isDebuggerDetected() || isAdvancedHookingDetected()) {
                securityBreached.set(true)
                recordSecurityViolation()
            }
        } catch (e: Exception) {
            // Log security check errors
        }
    }

    /**
     * VPN monitoring with callbacks
     */
    fun registerVpnDetectionCallback(onVpnStatusChanged: (Boolean) -> Unit): ConnectivityManager.NetworkCallback {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
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