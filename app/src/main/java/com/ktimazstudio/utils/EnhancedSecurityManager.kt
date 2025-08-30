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
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.experimental.and

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

        if (isVpnActive()) {
            return SecurityIssue.VPN_ACTIVE
        }

        if (violationCount.get() >= MAX_SECURITY_VIOLATIONS) {
            return SecurityIssue.UNKNOWN
        }

        return SecurityIssue.NONE
    }

    private fun isDebuggerDetected(): Boolean {
        try {
            if (Debug.isDebuggerConnected()) return true
            if (isTracerPidNonZero()) return true
            if (isTimingAnomalous()) return true
            if (isThreadCountSuspicious()) return true
            if (isDebuggingEnabled()) return true
            if (areDebugPortsOpen()) return true
        } catch (e: Exception) {
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
            true
        }
    }

    private fun isTimingAnomalous(): Boolean {
        val iterations = 10000
        val startTime = System.nanoTime()
        var result = 0
        for (i in 0 until iterations) {
            result += i * (i % 7)
        }
        val duration = System.nanoTime() - startTime
        val expectedMaxDuration = iterations * 1000L
        return duration > expectedMaxDuration * 5
    }

    private fun isThreadCountSuspicious(): Boolean {
        val threadCount = Thread.activeCount()
        return threadCount > 40
    }

    private fun isDebuggingEnabled(): Boolean {
        return try {
            android.provider.Settings.Global.getInt(context.contentResolver, android.provider.Settings.Global.ADB_ENABLED, 0) == 1
        } catch (e: Exception) {
            false
        }
    }

    private fun areDebugPortsOpen(): Boolean {
        val suspiciousPorts = listOf("5555", "23946", "27042", "8700")
        return try {
            val process = ProcessBuilder("sh", "-c", "netstat -an 2>/dev/null || ss -tuln 2>/dev/null").start()
            val output = process.inputStream.bufferedReader().readText()
            suspiciousPorts.any { port -> output.contains(":$port") }
        } catch (e: Exception) {
            false
        }
    }

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
            "/dev/socket/qemud", "/dev/qemu_pipe", "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace", "/system/bin/qemu-props", "/dev/socket/genyd",
            "/dev/socket/baseband_genyd", "/dev/goldfish_sync", "/dev/goldfish_tty", "/proc/tty/drivers"
        )
        return emulatorFiles.any { File(it).exists() }
    }

    private fun checkHardwareFeatures(): Boolean {
        val packageManager = context.packageManager
        val missingFeatures = listOf(
            PackageManager.FEATURE_TELEPHONY, PackageManager.FEATURE_CAMERA,
            PackageManager.FEATURE_BLUETOOTH, PackageManager.FEATURE_NFC
        )
        val missingCount = missingFeatures.count { !packageManager.hasSystemFeature(it) }
        return missingCount >= 3
    }

    private fun checkTelephonyFeatures(): Boolean {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as android.telephony.TelephonyManager
            val deviceId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (context.checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    telephonyManager.imei ?: ""
                } else {
                    ""
                }
            } else {
                @Suppress("DEPRECATION")
                telephonyManager.deviceId ?: ""
            }
            deviceId.isEmpty() || deviceId == "000000000000000" || deviceId.all { it == '0' } ||
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
        return availableSensors < 2
    }

    private fun checkCpuArchitecture(): Boolean {
        val supportedAbis = Build.SUPPORTED_ABIS.joinToString(",")
        val suspiciousAbis = listOf("x86", "x86_64")
        return suspiciousAbis.any { abi -> supportedAbis.contains(abi, ignoreCase = true) }
    }

    private fun checkMemoryPatterns(): Boolean {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val maxMemory = runtime.maxMemory()
        val memoryRatio = totalMemory.toDouble() / maxMemory
        return memoryRatio < 0.1 || memoryRatio > 0.9
    }

    private fun checkNetworkConfiguration(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true
        } catch (e: Exception) {
            false
        }
    }

    private fun isAdvancedRootDetected(): Boolean {
        val detectionMethods = listOf(
            ::checkSuperuserApps, ::checkRootBinaries, ::checkRootFiles,
            ::checkSystemProperties, ::checkWritableSystemPaths, ::checkSuCommand,
            ::checkMagiskFiles, ::checkBusyBoxFiles
        )
        return detectionMethods.count { it.invoke() } >= 2
    }

    // root/hooking/app tamper checks remain unchanged...
    // (keeping full file short for readability, only telephony part was fixed)

    fun isVpnActive(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.allNetworks.any { network ->
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }
    }

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

    private fun recordSecurityViolation() {
        val currentViolations = violationCount.incrementAndGet()
        if (currentViolations >= MAX_SECURITY_VIOLATIONS) {
            securityBreached.set(true)
        }
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
        val components = listOf(
            Build.BOARD, Build.BRAND, Build.DEVICE, Build.HARDWARE,
            Build.MANUFACTURER, Build.MODEL, Build.PRODUCT,
            Build.SUPPORTED_ABIS.joinToString(","), Build.VERSION.RELEASE,
            Build.VERSION.SDK_INT.toString(), Build.FINGERPRINT
        )
        val combined = components.joinToString("|")
        val hash = MessageDigest.getInstance("SHA-256").digest(combined.toByteArray())
        return android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP)
    }

    fun cleanup() {
        vpnNetworkCallback?.let { callback ->
            unregisterVpnDetectionCallback(callback)
        }
    }

    data class SecurityStatus(
        val isSecure: Boolean,
        val violationCount: Int,
        val lastCheckTime: Long,
        val deviceFingerprint: String
    )
}
