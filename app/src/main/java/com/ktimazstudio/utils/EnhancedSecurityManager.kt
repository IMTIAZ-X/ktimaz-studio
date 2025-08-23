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
                telephonyManager.imei ?: ""
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

    private fun checkSuperuserApps(): Boolean {
        val rootApps = listOf(
            "com.koushikdutta.superuser", "eu.chainfire.supersu", "com.kingroot.kinguser",
            "com.topjohnwu.magisk", "me.phh.superuser", "com.yellowes.su",
            "com.thirdparty.superuser", "com.koushikdutta.rommanager"
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
            "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su",
            "/data/local/bin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su",
            "/data/local/su", "/su/bin/su"
        )
        return rootBinaries.any { path ->
            File(path).exists() && File(path).canExecute()
        }
    }

    private fun checkRootFiles(): Boolean {
        val rootFiles = listOf(
            "/system/app/Superuser.apk", "/system/app/SuperSU.apk",
            "/system/etc/init.d/99SuperSUDaemon", "/system/xbin/daemonsu",
            "/system/etc/init.d/99su", "/data/data/com.android.shell/su"
        )
        return rootFiles.any { File(it).exists() }
    }

    private fun checkSystemProperties(): Boolean {
        val suspiciousProps = mapOf(
            "ro.debuggable" to "1", "ro.secure" to "0", "service.adb.root" to "1"
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
        val systemPaths = listOf("/system", "/system/bin", "/system/sbin", "/system/xbin", "/vendor/bin", "/sbin")
        return systemPaths.any { path -> File(path).canWrite() }
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
            "/sbin/.magisk", "/data/adb/magisk", "/data/adb/modules", "/cache/.disable_magisk",
            "/dev/.magisk.unblock", "/cache/magisk.log", "/data/adb/magisk.img", "/data/magisk.apk"
        )
        return magiskFiles.any { File(it).exists() }
    }

    private fun checkBusyBoxFiles(): Boolean {
        val busyBoxPaths = listOf(
            "/system/bin/busybox", "/system/xbin/busybox",
            "/data/local/xbin/busybox", "/sbin/busybox", "/data/local/busybox", "/system/sd/xbin/busybox"
        )
        return busyBoxPaths.any { path ->
            File(path).exists() && File(path).canExecute()
        }
    }

    private fun isAdvancedHookingDetected(): Boolean {
        return isXposedDetected() || isFridaDetected() || isCydiaSubstrateDetected() ||
               isLSPosedDetected() || isRiruDetected() || isEdXposedDetected()
    }

    private fun isXposedDetected(): Boolean {
        val xposedFiles = listOf(
            "/system/framework/XposedBridge.jar", "/system/bin/app_process_xposed",
            "/system/lib/libxposed_art.so", "/system/lib64/libxposed_art.so",
            "/data/data/de.robv.android.xposed.installer"
        )
        if (xposedFiles.any { File(it).exists() }) return true
        try {
            context.packageManager.getPackageInfo("de.robv.android.xposed.installer", 0)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            // Continue with other checks
        }
        return System.getenv("CLASSPATH")?.contains("XposedBridge") == true
    }

    private fun isFridaDetected(): Boolean {
        val fridaFiles = listOf(
            "/data/local/tmp/frida-server", "/data/local/tmp/re.frida.server",
            "/sdcard/frida-server", "/system/bin/frida-server",
            "/system/lib/frida-agent.so", "/system/lib64/frida-agent.so"
        )
        if (fridaFiles.any { File(it).exists() }) return true
        return try {
            val process = ProcessBuilder("sh", "-c", "netstat -an | grep :27042").start()
            process.inputStream.bufferedReader().readText().isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    private fun isCydiaSubstrateDetected(): Boolean {
        val substrateFiles = listOf(
            "/Library/MobileSubstrate", "/usr/libexec/cydia",
            "/System/Library/LaunchDaemons/com.saurik.Cydia.Startup.plist", "/data/local/tmp/substrate"
        )
        return substrateFiles.any { File(it).exists() }
    }

    private fun isLSPosedDetected(): Boolean {
        try {
            context.packageManager.getPackageInfo("org.lsposed.manager", 0)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            val lsposedFiles = listOf(
                "/data/adb/lspd", "/data/adb/modules/riru_lsposed", "/data/adb/modules/zygisk_lsposed"
            )
            return lsposedFiles.any { File(it).exists() }
        }
    }

    private fun isRiruDetected(): Boolean {
        val riruFiles = listOf(
            "/data/adb/riru", "/data/misc/riru", "/system/lib/libriru.so", "/system/lib64/libriru.so"
        )
        return riruFiles.any { File(it).exists() }
    }

    private fun isEdXposedDetected(): Boolean {
        return try {
            context.packageManager.getPackageInfo("org.meowcat.edxposed.manager", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            val edxposedFiles = listOf("/data/adb/edxposed", "/system/framework/edxposed.jar")
            edxposedFiles.any { File(it).exists() }
        }
    }

    private fun isApplicationTampered(): Boolean {
        return !verifySignature() || !verifyCodeIntegrity() || !verifyResourceIntegrity() || isDebuggingFlagsSet()
    }

    private fun verifySignature(): Boolean {
        val currentSignature = getSignatureSha256Hash()
        return currentSignature?.lowercase() == expectedSignature.lowercase()
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

    private fun verifyCodeIntegrity(): Boolean {
        return try {
            val mapsFile = File("/proc/self/maps")
            if (!mapsFile.exists()) return false
            val mapsContent = mapsFile.readText()
            val suspiciousLibraries = listOf("frida", "xposed", "substrate", "lsposed", "edxposed", "riru", "zygisk")
            !suspiciousLibraries.any { lib -> mapsContent.contains(lib, ignoreCase = true) }
        } catch (e: Exception) {
            false
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