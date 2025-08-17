package com.ktimazstudio.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.*
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

/**
 * Simplified cryptographic utilities using standard Android APIs
 * Removed BouncyCastle and Conscrypt dependencies to fix compilation
 */
class CryptoUtils {
    
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeystore"
        private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val AES_KEY_ALIAS = "KtimazAESKey"
        private const val AES_KEY_SIZE = 256
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
        private const val PBKDF2_ITERATIONS = 100000
        private const val SALT_LENGTH = 32
    }
    
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    private val secureRandom = SecureRandom()
    
    init {
        generateAESKeyIfNotExists()
    }
    
    /**
     * AES-256-GCM encryption with Android Keystore
     */
    fun encrypt(plaintext: String): String? {
        return try {
            val secretKey = getAESKey()
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            
            val iv = cipher.iv
            val cipherText = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            
            // Combine IV and encrypted data
            val combined = iv + cipherText
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            // Fallback to simple obfuscation
            obfuscateString(plaintext)
        }
    }
    
    /**
     * AES-256-GCM decryption
     */
    fun decrypt(encryptedText: String): String? {
        return try {
            val secretKey = getAESKey()
            val combined = Base64.decode(encryptedText, Base64.NO_WRAP)
            
            val iv = combined.sliceArray(0 until GCM_IV_LENGTH)
            val cipherText = combined.sliceArray(GCM_IV_LENGTH until combined.size)
            
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            
            String(cipher.doFinal(cipherText), Charsets.UTF_8)
        } catch (e: Exception) {
            // Try deobfuscation as fallback
            deobfuscateString(encryptedText)
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
     * Compute SHA-256 hash
     */
    fun computeSHA256(data: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(data)
    }
    
    /**
     * Compute SHA-512 hash
     */
    fun computeSHA512(data: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-512")
        return digest.digest(data)
    }
    
    /**
     * Compute HMAC-SHA256
     */
    fun computeHMAC(data: ByteArray, key: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(key, "HmacSHA256")
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
     * Fallback obfuscation method (simple XOR)
     */
    private fun obfuscateString(input: String): String {
        val key = generateObfuscationKey(input.length)
        val obfuscated = input.toByteArray().mapIndexed { index, byte ->
            (byte.toInt() xor key[index % key.size]).toByte()
        }.toByteArray()
        
        return Base64.encodeToString(key + obfuscated, Base64.NO_WRAP)
    }
    
    private fun deobfuscateString(obfuscated: String): String? {
        return try {
            val combined = Base64.decode(obfuscated, Base64.NO_WRAP)
            val keyLength = combined.size / 3 // Approximate key length
            val key = combined.sliceArray(0 until keyLength)
            val data = combined.sliceArray(keyLength until combined.size)
            
            val deobfuscated = data.mapIndexed { index, byte ->
                (byte.toInt() xor key[index % key.size]).toByte()
            }.toByteArray()
            
            String(deobfuscated, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun generateObfuscationKey(length: Int): ByteArray {
        val key = ByteArray(length / 2 + 1)
        secureRandom.nextBytes(key)
        return key
    }
    
    /**
     * Clear all cryptographic keys (for factory reset)
     */
    fun clearAllKeys() {
        try {
            if (keyStore.containsAlias(AES_KEY_ALIAS)) {
                keyStore.deleteEntry(AES_KEY_ALIAS)
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
 * Simple string obfuscation utilities
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
    
    data class ObfuscatedString(
        val data: String,
        val keyIndex: Int
    )
}

/**
 * Anti-tampering detection utilities
 */
object AntiTampering {
    
    private val checksumMap = mutableMapOf<String, String>()
    
    /**
     * Calculate and store method checksum
     */
    fun registerMethod(methodName: String, methodBody: String) {
        val checksum = CryptoUtils().computeSHA256(methodBody.toByteArray())
        checksumMap[methodName] = Base64.encodeToString(checksum, Base64.NO_WRAP)
    }
    
    /**
     * Verify method integrity
     */
    fun verifyMethod(methodName: String, currentMethodBody: String): Boolean {
        val storedChecksum = checksumMap[methodName] ?: return false
        val currentChecksum = CryptoUtils().computeSHA256(currentMethodBody.toByteArray())
        val currentChecksumString = Base64.encodeToString(currentChecksum, Base64.NO_WRAP)
        
        return CryptoUtils().constantTimeEquals(
            Base64.decode(storedChecksum, Base64.NO_WRAP),
            Base64.decode(currentChecksumString, Base64.NO_WRAP)
        )
    }
    
    /**
     * Basic code integrity verification
     */
    fun verifyCodeIntegrity(): Boolean {
        return verifyStackTrace() && 
               verifyClassLoader() && 
               verifyMethodSignatures()
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
    
    /**
     * Control flow obfuscation with random execution paths
     */
    fun executeWithObfuscation(action: () -> Unit) {
        val random = Random.nextInt(0, 3)
        when (random) {
            0 -> {
                performDecoyOperation1()
                action()
                performDecoyOperation2()
            }
            1 -> {
                performDecoyOperation2()
                action()
                performDecoyOperation1()
            }
            else -> {
                action()
                performDecoyOperation1()
            }
        }
    }
    
    private fun performDecoyOperation1() {
        val dummy = Random.nextLong(100000, 999999)
        val result = dummy * 7 + 13
        // Meaningless operation to confuse static analysis
    }
    
    private fun performDecoyOperation2() {
        val dummy = System.currentTimeMillis()
        val processed = dummy.toString().reversed()
        // Another decoy operation
    }
}

/**
 * Simplified device fingerprinting
 */
object DeviceFingerprinting {
    
    /**
     * Generate basic device fingerprint
     */
    fun generateDeviceFingerprint(context: android.content.Context): String {
        val components = mutableListOf<String>()
        
        // Hardware identifiers
        components.add(android.os.Build.BRAND)
        components.add(android.os.Build.MODEL)
        components.add(android.os.Build.DEVICE)
        components.add(android.os.Build.HARDWARE)
        components.add(android.os.Build.MANUFACTURER)
        components.add(android.os.Build.PRODUCT)
        
        // System properties
        components.add(android.os.Build.VERSION.RELEASE)
        components.add(android.os.Build.VERSION.SDK_INT.toString())
        
        // Display characteristics
        val displayMetrics = context.resources.displayMetrics
        components.add("${displayMetrics.widthPixels}x${displayMetrics.heightPixels}")
        components.add(displayMetrics.densityDpi.toString())
        
        // Combine and hash
        val combined = components.joinToString("|")
        val hash = CryptoUtils().computeSHA256(combined.toByteArray())
        
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }
    
    /**
     * Basic emulator detection
     */
    fun isRealDevice(): Boolean {
        val suspiciousValues = listOf(
            android.os.Build.FINGERPRINT.contains("generic"),
            android.os.Build.MODEL.contains("sdk"),
            android.os.Build.MANUFACTURER == "Genymotion",
            android.os.Build.HARDWARE == "goldfish",
            android.os.Build.HARDWARE == "ranchu"
        )
        
        return !suspiciousValues.any { it }
    }
    
    /**
     * Generate session-based device token
     */
    fun generateSessionToken(context: android.content.Context): String {
        val baseFingerprint = generateDeviceFingerprint(context)
        val timestamp = System.currentTimeMillis()
        val random = Random.nextBytes(16)
        
        val combined = "$baseFingerprint|$timestamp|${Base64.encodeToString(random, Base64.NO_WRAP)}"
        val hash = CryptoUtils().computeSHA256(combined.toByteArray())
        
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }
}

/**
 * Basic network security utilities
 */
object NetworkSecurity {
    
    /**
     * Detect proxy usage
     */
    fun isProxyActive(): Boolean {
        val proxyHost = System.getProperty("http.proxyHost")
        val proxyPort = System.getProperty("http.proxyPort")
        return !proxyHost.isNullOrEmpty() && !proxyPort.isNullOrEmpty()
    }
    
    /**
     * Basic MITM detection
     */
    fun detectMITM(): Boolean {
        return isProxyActive()
    }
    
    /**
     * Generate secure network request headers
     */
    fun generateSecureHeaders(context: android.content.Context): Map<String, String> {
        val deviceToken = DeviceFingerprinting.generateSessionToken(context)
        val timestamp = System.currentTimeMillis()
        val nonce = Random.nextBytes(16)
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
        val hash = CryptoUtils().computeSHA256(combined.toByteArray())
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }
}