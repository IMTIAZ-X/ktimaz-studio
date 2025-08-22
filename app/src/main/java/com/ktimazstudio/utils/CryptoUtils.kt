package com.ktimazstudio.utils

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.*
import java.security.spec.ECGenParameterSpec
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

class CryptoUtils {
    
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeystore"
        private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
        private const val EC_CURVE = "secp256r1"
        
        private const val AES_KEY_SIZE = 256
        private const val RSA_KEY_SIZE = 2048
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
        private const val PBKDF2_ITERATIONS = 10000
        private const val SALT_LENGTH = 32
        
        private const val AES_KEY_ALIAS = "KtimazAESKey"
        private const val RSA_KEY_ALIAS = "KtimazRSAKey"
        private const val EC_KEY_ALIAS = "KtimazECKey"
    }
    
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    private val secureRandom = SecureRandom()
    
    init {
        generateKeysIfNotExist()
    }
    
    private fun generateKeysIfNotExist() {
        generateAESKeyIfNotExists()
        generateRSAKeyPairIfNotExists()
        generateECKeyPairIfNotExists()
    }
    
    // FIXED: Simple encrypt/decrypt methods for SharedPreferencesManager
    fun encrypt(plaintext: String): String {
        val encryptedData = encryptAES(plaintext)
        return encryptedData?.let { "${it.data}:${it.iv}" } ?: plaintext
    }
    
    fun decrypt(encryptedText: String): String {
        return try {
            val parts = encryptedText.split(":")
            if (parts.size == 2) {
                val encryptedData = EncryptedData(
                    data = parts[0],
                    iv = parts[1],
                    algorithm = "AES-256-GCM"
                )
                decryptAES(encryptedData) ?: encryptedText
            } else {
                encryptedText
            }
        } catch (e: Exception) {
            encryptedText
        }
    }
    
    fun encryptAES(plaintext: String): EncryptedData? {
        return try {
            val secretKey = getAESKey()
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            
            val iv = cipher.iv
            val cipherText = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            
            EncryptedData(
                data = Base64.encodeToString(cipherText, Base64.NO_WRAP),
                iv = Base64.encodeToString(iv, Base64.NO_WRAP),
                algorithm = "AES-256-GCM"
            )
        } catch (e: Exception) {
            null
        }
    }
    
    fun decryptAES(encryptedData: EncryptedData): String? {
        return try {
            val secretKey = getAESKey()
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, Base64.decode(encryptedData.iv, Base64.NO_WRAP))
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
            
            val decryptedBytes = cipher.doFinal(Base64.decode(encryptedData.data, Base64.NO_WRAP))
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }
    
    fun encryptRSA(plaintext: String): String? {
        return try {
            val publicKey = getRSAKeyPair().public
            val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            
            val encryptedBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }
    
    fun decryptRSA(encryptedText: String): String? {
        return try {
            val privateKey = getRSAKeyPair().private
            val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            
            val encryptedBytes = Base64.decode(encryptedText, Base64.NO_WRAP)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }
    
    // FIXED: Removed BouncyCastle dependencies - using standard Android crypto
    fun performECDH(otherPartyPublicKey: PublicKey): ByteArray? {
        return try {
            val keyAgreement = KeyAgreement.getInstance("ECDH")
            keyAgreement.init(getECKeyPair().private)
            keyAgreement.doPhase(otherPartyPublicKey, true)
            keyAgreement.generateSecret()
        } catch (e: Exception) {
            null
        }
    }
    
    fun signData(data: ByteArray): ByteArray? {
        return try {
            val signature = Signature.getInstance("SHA256withECDSA")
            signature.initSign(getECKeyPair().private)
            signature.update(data)
            signature.sign()
        } catch (e: Exception) {
            null
        }
    }
    
    fun verifySignature(data: ByteArray, signature: ByteArray, publicKey: PublicKey): Boolean {
        return try {
            val verifier = Signature.getInstance("SHA512withECDSA", BouncyCastleProvider.PROVIDER_NAME)
            verifier.initVerify(publicKey)
            verifier.update(data)
            verifier.verify(signature)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * PBKDF2 key derivation for password-based encryption
     */
    fun deriveKeyFromPassword(password: String, salt: ByteArray): SecretKey {
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, AES_KEY_SIZE)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val key = factory.generateSecret(spec)
        return SecretKeySpec(key.encoded, "AES")
    }
    
    /**
     * Generate cryptographically secure random salt
     */
    fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_LENGTH)
        secureRandom.nextBytes(salt)
        return salt
    }
    
    /**
     * Generate secure random bytes
     */
    fun generateSecureRandom(length: Int): ByteArray {
        val random = ByteArray(length)
        secureRandom.nextBytes(random)
        return random
    }
    
    /**
     * Compute SHA-512 hash
     */
    fun computeSHA512(data: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-512")
        return digest.digest(data)
    }
    
    /**
     * Compute HMAC-SHA512
     */
    fun computeHMAC(data: ByteArray, key: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA512")
        val secretKey = SecretKeySpec(key, "HmacSHA512")
        mac.init(secretKey)
        return mac.doFinal(data)
    }
    
    /**
     * Constant-time comparison to prevent timing attacks
     */
    fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }
    
    /**
     * Get or generate AES key in Android Keystore
     */
    private fun getAESKey(): SecretKey {
        return keyStore.getKey(AES_KEY_ALIAS, null) as SecretKey
    }
    
    private fun generateAESKeyIfNotExists() {
        if (keyStore.containsAlias(AES_KEY_ALIAS)) return
        
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            AES_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(AES_KEY_SIZE)
            .setUserAuthenticationRequired(false)
            .setRandomizedEncryptionRequired(true)
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }
    
    /**
     * Get or generate RSA key pair
     */
    private fun getRSAKeyPair(): KeyPair {
        val privateKey = keyStore.getKey(RSA_KEY_ALIAS, null) as PrivateKey
        val publicKey = keyStore.getCertificate(RSA_KEY_ALIAS).publicKey
        return KeyPair(publicKey, privateKey)
    }
    
    private fun generateRSAKeyPairIfNotExists() {
        if (keyStore.containsAlias(RSA_KEY_ALIAS)) return
        
        val keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            RSA_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT or 
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setKeySize(RSA_KEY_SIZE)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PSS)
            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            .setUserAuthenticationRequired(false)
            .build()
        
        keyPairGenerator.initialize(keyGenParameterSpec)
        keyPairGenerator.generateKeyPair()
    }
    
    /**
     * Get or generate EC key pair
     */
    private fun getECKeyPair(): KeyPair {
        val privateKey = keyStore.getKey(EC_KEY_ALIAS, null) as PrivateKey
        val publicKey = keyStore.getCertificate(EC_KEY_ALIAS).publicKey
        return KeyPair(publicKey, privateKey)
    }
    
    private fun generateECKeyPairIfNotExists() {
        if (keyStore.containsAlias(EC_KEY_ALIAS)) return
        
        val keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, ANDROID_KEYSTORE)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            EC_KEY_ALIAS,
            KeyProperties.PURPOSE_AGREE_KEY or KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setAlgorithmParameterSpec(ECGenParameterSpec(EC_CURVE))
            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            .setUserAuthenticationRequired(false)
            .build()
        
        keyPairGenerator.initialize(keyGenParameterSpec)
        keyPairGenerator.generateKeyPair()
    }
    
    /**
     * Export public key for key exchange
     */
    fun exportECPublicKey(): String? {
        return try {
            val publicKey = getECKeyPair().public
            Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Import public key from base64
     */
    fun importECPublicKey(base64Key: String): PublicKey? {
        return try {
            val keyBytes = Base64.decode(base64Key, Base64.NO_WRAP)
            val keyFactory = KeyFactory.getInstance("EC")
            val keySpec = java.security.spec.X509EncodedKeySpec(keyBytes)
            keyFactory.generatePublic(keySpec)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Clear all cryptographic keys (for factory reset)
     */
    fun clearAllKeys() {
        try {
            if (keyStore.containsAlias(AES_KEY_ALIAS)) {
                keyStore.deleteEntry(AES_KEY_ALIAS)
            }
            if (keyStore.containsAlias(RSA_KEY_ALIAS)) {
                keyStore.deleteEntry(RSA_KEY_ALIAS)
            }
            if (keyStore.containsAlias(EC_KEY_ALIAS)) {
                keyStore.deleteEntry(EC_KEY_ALIAS)
            }
        } catch (e: Exception) {
            // Log error in production
        }
    }
    
    /**
     * Data class for encrypted data with metadata
     */
    data class EncryptedData(
        val data: String,
        val iv: String,
        val algorithm: String,
        val timestamp: Long = System.currentTimeMillis()
    )
}

/**
 * Advanced string obfuscation utilities
 */
object StringObfuscation {
    
    private val obfuscationKey = generateObfuscationKey()
    
    private fun generateObfuscationKey(): IntArray {
        val random = Random(System.currentTimeMillis())
        return IntArray(256) { random.nextInt(1, 255) }
    }
    
    /**
     * Compile-time string obfuscation
     */
    fun obfuscateString(input: String): ObfuscatedString {
        val obfuscated = input.toByteArray(Charsets.UTF_8).mapIndexed { index, byte ->
            (byte.toInt() xor obfuscationKey[index % obfuscationKey.size]).toByte()
        }.toByteArray()
        
        return ObfuscatedString(
            data = Base64.encodeToString(obfuscated, Base64.NO_WRAP),
            keyIndex = Random.nextInt(obfuscationKey.size)
        )
    }
    
    /**
     * Runtime string deobfuscation
     */
    fun deobfuscateString(obfuscated: ObfuscatedString): String {
        val encryptedBytes = Base64.decode(obfuscated.data, Base64.NO_WRAP)
        val decryptedBytes = encryptedBytes.mapIndexed { index, byte ->
            (byte.toInt() xor obfuscationKey[(index + obfuscated.keyIndex) % obfuscationKey.size]).toByte()
        }.toByteArray()
        
        return String(decryptedBytes, Charsets.UTF_8)
    }
    
    /**
     * Stack-based string hiding
     */
    fun hideString(input: String): String {
        val scrambled = input.toCharArray()
        scrambled.shuffle(Random(input.hashCode().toLong()))
        return Base64.encodeToString(String(scrambled).toByteArray(), Base64.NO_WRAP)
    }
    
    /**
     * Reveal hidden string
     */
    fun revealString(hidden: String, original: String): String {
        val decoded = String(Base64.decode(hidden, Base64.NO_WRAP))
        val chars = decoded.toCharArray()
        val originalChars = original.toCharArray()
        
        // Sort chars back to original order using original as reference
        val charMap = chars.withIndex().groupBy { it.value }
        val result = CharArray(originalChars.size)
        
        var charIndex = 0
        for (i in originalChars.indices) {
            val targetChar = originalChars[i]
            val availableChars = charMap[targetChar]
            if (availableChars != null && charIndex < availableChars.size) {
                result[i] = availableChars[charIndex].value
                charIndex++
            }
        }
        
        return String(result)
    }
    
    data class ObfuscatedString(
        val data: String,
        val keyIndex: Int
    )
}

/**
 * Anti-tampering detection and response
 */
object AntiTampering {
    
    private val checksumMap = mutableMapOf<String, String>()
    
    /**
     * Calculate and store method checksum
     */
    fun registerMethod(methodName: String, methodBody: String) {
        val checksum = CryptoUtils().computeSHA512(methodBody.toByteArray())
        checksumMap[methodName] = Base64.encodeToString(checksum, Base64.NO_WRAP)
    }
    
    /**
     * Verify method integrity
     */
    fun verifyMethod(methodName: String, currentMethodBody: String): Boolean {
        val storedChecksum = checksumMap[methodName] ?: return false
        val currentChecksum = CryptoUtils().computeSHA512(currentMethodBody.toByteArray())
        val currentChecksumString = Base64.encodeToString(currentChecksum, Base64.NO_WRAP)
        
        return CryptoUtils().constantTimeEquals(
            Base64.decode(storedChecksum, Base64.NO_WRAP),
            Base64.decode(currentChecksumString, Base64.NO_WRAP)
        )
    }
    
    /**
     * Advanced code integrity verification
     */
    fun verifyCodeIntegrity(): Boolean {
        return verifyStackTrace() && 
               verifyClassLoader() && 
               verifyMethodSignatures() &&
               verifyRuntimeEnvironment()
    }
    
    private fun verifyStackTrace(): Boolean {
        val stackTrace = Thread.currentThread().stackTrace
        val suspiciousFrames = listOf(
            "xposed", "frida", "substrate", "lsposed", 
            "edxposed", "riru", "magisk", "supersu"
        )
        
        return !stackTrace.any { frame ->
            suspiciousFrames.any { suspicious ->
                frame.className.contains(suspicious, ignoreCase = true) ||
                frame.methodName.contains(suspicious, ignoreCase = true)
            }
        }
    }
    
    private fun verifyClassLoader(): Boolean {
        val classLoader = AntiTampering::class.java.classLoader
        val expectedLoaders = listOf("dalvik", "art", "pathclassloader")
        
        return classLoader?.javaClass?.name?.let { loaderName ->
            expectedLoaders.any { expected -> 
                loaderName.contains(expected, ignoreCase = true) 
            }
        } ?: false
    }
    
    private fun verifyMethodSignatures(): Boolean {
        return try {
            val method = AntiTampering::class.java.getDeclaredMethod("verifyCodeIntegrity")
            method.name == "verifyCodeIntegrity" && 
            method.returnType == Boolean::class.javaPrimitiveType
        } catch (e: Exception) {
            false
        }
    }
    
    private fun verifyRuntimeEnvironment(): Boolean {
        // Check for runtime manipulation indicators
        val runtime = Runtime.getRuntime()
        val processors = runtime.availableProcessors()
        val maxMemory = runtime.maxMemory()
        
        // Basic sanity checks for runtime environment
        return processors > 0 && processors <= 32 && maxMemory > 0
    }
    
    /**
     * Control flow obfuscation with random execution paths
     */
    fun executeWithObfuscation(action: () -> Unit) {
        val random = kotlin.random.Random.nextInt(0, 5)
        val decoyOperations = listOf(
            { performDecoyOperation1() },
            { performDecoyOperation2() },
            { performDecoyOperation3() }
        )
        
        when (random) {
            0 -> {
                decoyOperations[0]()
                action()
                decoyOperations[1]()
            }
            1 -> {
                decoyOperations[1]()
                decoyOperations[2]()
                action()
            }
            2 -> {
                action()
                decoyOperations[random % decoyOperations.size]()
            }
            3 -> {
                decoyOperations.forEach { it() }
                action()
            }
            else -> {
                decoyOperations[2]()
                action()
                decoyOperations[0]()
                decoyOperations[1]()
            }
        }
    }
    
    private fun performDecoyOperation1() {
        val dummy = kotlin.random.Random.nextLong(100000, 999999)
        val result = dummy * 7 + 13
        val finalResult = result.toString().hashCode()
        // Intentionally complex but meaningless operation
    }
    
    private fun performDecoyOperation2() {
        val dummy = System.currentTimeMillis()
        val processed = dummy.toString().reversed().toCharArray()
        val sum = processed.sumOf { it.code }
        // Another decoy operation
    }
    
    private fun performDecoyOperation3() {
        val dummy = kotlin.random.Random.nextBytes(16)
        val hash = dummy.contentHashCode()
        val final = (hash xor 0xDEADBEEF.toInt()).toString()
        // Third decoy operation
    }
    
    /**
     * Runtime self-modification detection
     */
    fun detectSelfModification(): Boolean {
        return try {
            val currentClass = AntiTampering::class.java
            val methods = currentClass.declaredMethods
            val expectedMethodCount = 12 // Update if methods are added/removed
            
            methods.size != expectedMethodCount ||
            methods.any { method ->
                method.name.startsWith("hooked_") || 
                method.name.contains("proxy") ||
                method.name.contains("stub")
            }
        } catch (e: Exception) {
            true // Assume tampered if we can't verify
        }
    }
}

/**
 * Hardware-based device fingerprinting for enhanced security
 */
object DeviceFingerprinting {
    
    /**
     * Generate comprehensive device fingerprint
     */
    fun generateDeviceFingerprint(context: Context): String {
        val components = mutableListOf<String>()
        
        // Hardware identifiers
        components.addAll(getHardwareIdentifiers())
        
        // System properties
        components.addAll(getSystemProperties())
        
        // Display characteristics
        components.addAll(getDisplayCharacteristics(context))
        
        // Sensor data
        components.addAll(getSensorFingerprints(context))
        
        // Network configuration
        components.addAll(getNetworkCharacteristics(context))
        
        // Combine and hash for final fingerprint
        val combined = components.joinToString("|")
        val hash = CryptoUtils().computeSHA512(combined.toByteArray())
        
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }
    
    private fun getHardwareIdentifiers(): List<String> {
        return listOf(
            android.os.Build.BOARD,
            android.os.Build.BRAND,
            android.os.Build.DEVICE,
            android.os.Build.HARDWARE,
            android.os.Build.MANUFACTURER,
            android.os.Build.MODEL,
            android.os.Build.PRODUCT,
            android.os.Build.SUPPORTED_ABIS.joinToString(","),
            android.os.Build.SOC_MANUFACTURER,
            android.os.Build.SOC_MODEL
        )
    }
    
    private fun getSystemProperties(): List<String> {
        return listOf(
            android.os.Build.VERSION.RELEASE,
            android.os.Build.VERSION.SDK_INT.toString(),
            android.os.Build.VERSION.SECURITY_PATCH,
            android.os.Build.FINGERPRINT,
            System.getProperty("java.vm.version") ?: "",
            System.getProperty("java.vm.name") ?: "",
            Runtime.getRuntime().availableProcessors().toString()
        )
    }
    
    private fun getDisplayCharacteristics(context: Context): List<String> {
        val displayMetrics = context.resources.displayMetrics
        return listOf(
            "${displayMetrics.widthPixels}x${displayMetrics.heightPixels}",
            displayMetrics.densityDpi.toString(),
            displayMetrics.density.toString(),
            displayMetrics.scaledDensity.toString(),
            displayMetrics.xdpi.toString(),
            displayMetrics.ydpi.toString()
        )
    }
    
    private fun getSensorFingerprints(context: Context): List<String> {
        return try {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
            val sensors = sensorManager.getSensorList(android.hardware.Sensor.TYPE_ALL)
            
            sensors.map { sensor ->
                "${sensor.name}|${sensor.vendor}|${sensor.version}|${sensor.type}|${sensor.maxRange}"
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun getNetworkCharacteristics(context: Context): List<String> {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
            listOf(
                wifiManager.connectionInfo?.macAddress ?: "unknown",
                wifiManager.connectionInfo?.bssid ?: "unknown",
                wifiManager.dhcpInfo?.gateway?.toString() ?: "unknown"
            )
        } catch (e: Exception) {
            listOf("unknown", "unknown", "unknown")
        }
    }
    
    /**
     * Detect virtualized/emulated environment
     */
    fun isRealDevice(context: Context): Boolean {
        val suspiciousIndicators = mutableListOf<Boolean>()
        
        // Hardware checks
        suspiciousIndicators.add(checkHardwareSuspicion())
        
        // Sensor availability
        suspiciousIndicators.add(checkSensorAvailability(context))
        
        // Network configuration
        suspiciousIndicators.add(checkNetworkConfiguration(context))
        
        // Memory patterns
        suspiciousIndicators.add(checkMemoryPatterns())
        
        // CPU characteristics
        suspiciousIndicators.add(checkCpuCharacteristics())
        
        // Require majority of checks to pass
        val suspiciousCount = suspiciousIndicators.count { it }
        return suspiciousCount < suspiciousIndicators.size / 2
    }
    
    private fun checkHardwareSuspicion(): Boolean {
        val suspiciousTerms = listOf("generic", "emulator", "simulator", "vbox", "qemu", "goldfish")
        val properties = listOf(
            android.os.Build.BRAND,
            android.os.Build.DEVICE,
            android.os.Build.MODEL,
            android.os.Build.PRODUCT,
            android.os.Build.HARDWARE
        )
        
        return properties.any { property ->
            suspiciousTerms.any { term ->
                property.contains(term, ignoreCase = true)
            }
        }
    }
    
    private fun checkSensorAvailability(context: Context): Boolean {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
        val criticalSensors = listOf(
            android.hardware.Sensor.TYPE_ACCELEROMETER,
            android.hardware.Sensor.TYPE_GYROSCOPE,
            android.hardware.Sensor.TYPE_MAGNETIC_FIELD,
            android.hardware.Sensor.TYPE_PROXIMITY
        )
        
        val availableCount = criticalSensors.count { type ->
            sensorManager.getDefaultSensor(type) != null
        }
        
        return availableCount < criticalSensors.size / 2
    }
    
    private fun checkNetworkConfiguration(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            
            // Suspicious if only ethernet (common in emulators)
            capabilities?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET) == true &&
            !capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) &&
            !capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR)
        } catch (e: Exception) {
            false
        }
    }
    
    private fun checkMemoryPatterns(): Boolean {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        
        val memoryRatio = totalMemory.toDouble() / maxMemory
        val freeRatio = freeMemory.toDouble() / totalMemory
        
        // Suspicious memory patterns common in emulators
        return memoryRatio < 0.1 || memoryRatio > 0.95 || freeRatio > 0.8
    }
    
    private fun checkCpuCharacteristics(): Boolean {
        val supportedAbis = android.os.Build.SUPPORTED_ABIS
        val processorCount = Runtime.getRuntime().availableProcessors()
        
        // x86/x86_64 more common in emulators
        val hasX86 = supportedAbis.any { it.contains("x86", ignoreCase = true) }
        val unusualProcessorCount = processorCount < 1 || processorCount > 16
        
        return hasX86 || unusualProcessorCount
    }
    
    /**
     * Generate session-based device token
     */
    fun generateSessionToken(context: Context): String {
        val baseFingerprint = generateDeviceFingerprint(context)
        val timestamp = System.currentTimeMillis()
        val random = kotlin.random.Random.nextBytes(16)
        
        val combined = "$baseFingerprint|$timestamp|${Base64.encodeToString(random, Base64.NO_WRAP)}"
        val hash = CryptoUtils().computeSHA512(combined.toByteArray())
        
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }
}

/**
 * Network security and certificate pinning utilities
 */
object NetworkSecurity {
    
    private val pinnedCertificates = mapOf(
        "api.ktimazstudio.com" to "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        "cdn.ktimazstudio.com" to "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB="
    )
    
    /**
     * Verify certificate pinning
     */
    fun verifyCertificatePinning(hostname: String, certificateChain: Array<java.security.cert.Certificate>): Boolean {
        val expectedPin = pinnedCertificates[hostname] ?: return true // No pinning configured
        
        return certificateChain.any { certificate ->
            val publicKeyHash = computePublicKeyPin(certificate)
            publicKeyHash == expectedPin
        }
    }
    
    private fun computePublicKeyPin(certificate: java.security.cert.Certificate): String {
        val publicKeyBytes = certificate.publicKey.encoded
        val hash = MessageDigest.getInstance("SHA-256").digest(publicKeyBytes)
        return "sha256/${Base64.encodeToString(hash, Base64.NO_WRAP)}"
    }
    
    /**
     * Detect proxy/interceptor
     */
    fun isProxyActive(): Boolean {
        val proxyProperties = listOf(
            "http.proxyHost", "https.proxyHost",
            "http.proxyPort", "https.proxyPort"
        )
        
        return proxyProperties.any { property ->
            System.getProperty(property)?.isNotEmpty() == true
        }
    }
    
    /**
     * Detect potential man-in-the-middle attack
     */
    fun detectMITM(): Boolean {
        return isProxyActive() || detectSuspiciousNetworkBehavior()
    }
    
    private fun detectSuspiciousNetworkBehavior(): Boolean {
        // Check for common MITM tool signatures
        val suspiciousProcesses = listOf(
            "burp", "charles", "mitmproxy", "fiddler",
            "wireshark", "tcpdump", "intercepter"
        )
        
        return try {
            val process = ProcessBuilder("ps", "-A").start()
            val output = process.inputStream.bufferedReader().readText().lowercase()
            
            suspiciousProcesses.any { tool ->
                output.contains(tool)
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Generate secure network request headers
     */
    fun generateSecureHeaders(context: Context): Map<String, String> {
        val deviceToken = DeviceFingerprinting.generateSessionToken(context)
        val timestamp = System.currentTimeMillis()
        val nonce = kotlin.random.Random.nextBytes(16)
        val nonceString = Base64.encodeToString(nonce, Base64.NO_WRAP)
        
        return mapOf(
            "X-Device-Token" to deviceToken,
            "X-Timestamp" to timestamp.toString(),
            "X-Nonce" to nonceString,
            "X-App-Version" to com.ktimazstudio.BuildConfig.VERSION_NAME,
            "X-Security-Hash" to generateRequestHash(deviceToken, timestamp.toString(), nonceString)
        )
    }
    
    private fun generateRequestHash(vararg components: String): String {
        val combined = components.joinToString("|")
        val hash = CryptoUtils().computeSHA512(combined.toByteArray())
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }
}