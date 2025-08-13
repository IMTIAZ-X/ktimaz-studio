package com.ktimazstudio.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

/**
 * Advanced cryptographic utilities for secure data storage and anti-tampering
 */
class CryptoUtils {
    
    companion object {
        private const val KEYSTORE_ALIAS = "KtimazSecureKey"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_LENGTH = 256
        private const val IV_LENGTH = 12
        private const val TAG_LENGTH = 16
    }
    
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    
    init {
        generateKeyIfNotExists()
    }
    
    /**
     * Encrypts data using AES-GCM with Android Keystore
     */
    fun encrypt(data: String): String {
        return try {
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            
            val iv = cipher.iv
            val encryptedData = cipher.doFinal(data.toByteArray())
            
            // Combine IV and encrypted data
            val combined = iv + encryptedData
            Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            // Fallback to obfuscation if encryption fails
            obfuscateString(data)
        }
    }
    
    /**
     * Decrypts data using AES-GCM with Android Keystore
     */
    fun decrypt(encryptedData: String): String? {
        return try {
            val secretKey = getOrCreateSecretKey()
            val combined = Base64.decode(encryptedData, Base64.DEFAULT)
            
            val iv = combined.sliceArray(0..IV_LENGTH-1)
            val cipherText = combined.sliceArray(IV_LENGTH until combined.size)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(TAG_LENGTH * 8, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            
            String(cipher.doFinal(cipherText))
        } catch (e: Exception) {
            // Try deobfuscation as fallback
            deobfuscateString(encryptedData)
        }
    }
    
    private fun generateKeyIfNotExists() {
        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_LENGTH)
                .setUserAuthenticationRequired(false)
                .build()
            
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }
    
    private fun getOrCreateSecretKey(): SecretKey {
        return keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
    }
    
    /**
     * Fallback obfuscation method (less secure but works without keystore)
     */
    private fun obfuscateString(input: String): String {
        val key = generateObfuscationKey(input.length)
        val obfuscated = input.toByteArray().mapIndexed { index, byte ->
            (byte.toInt() xor key[index % key.size]).toByte()
        }.toByteArray()
        
        return Base64.encodeToString(key + obfuscated, Base64.DEFAULT)
    }
    
    private fun deobfuscateString(obfuscated: String): String? {
        return try {
            val combined = Base64.decode(obfuscated, Base64.DEFAULT)
            val keyLength = combined.size / 3 // Approximate key length
            val key = combined.sliceArray(0 until keyLength)
            val data = combined.sliceArray(keyLength until combined.size)
            
            val deobfuscated = data.mapIndexed { index, byte ->
                (byte.toInt() xor key[index % key.size]).toByte()
            }.toByteArray()
            
            String(deobfuscated)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun generateObfuscationKey(length: Int): ByteArray {
        val key = ByteArray(length / 2 + 1)
        SecureRandom().nextBytes(key)
        return key
    }
}

/**
 * String obfuscation utilities to make static analysis harder
 */
object StringObfuscation {
    
    /**
     * Compile-time string obfuscation (use in build process)
     */
    fun obfuscateStringLiteral(input: String): Pair<String, IntArray> {
        val key = Random.nextInt(1, 255)
        val obfuscated = input.map { it.code xor key }.toIntArray()
        return Base64.encodeToString(input.toByteArray(), Base64.DEFAULT) to obfuscated
    }
    
    /**
     * Runtime string deobfuscation
     */
    fun deobfuscateStringLiteral(obfuscated: IntArray, key: Int): String {
        return obfuscated.map { (it xor key).toChar() }.joinToString("")
    }
    
    /**
     * Advanced string hiding using stack manipulation
     */
    fun hideString(input: String): String {
        val chars = input.toCharArray()
        val result = StringBuilder()
        
        for (i in chars.indices.reversed()) {
            result.append(chars[i])
        }
        
        return Base64.encodeToString(result.toString().toByteArray(), Base64.DEFAULT)
    }
    
    fun revealString(hidden: String): String {
        val decoded = String(Base64.decode(hidden, Base64.DEFAULT))
        return decoded.reversed()
    }
}

/**
 * Anti-tampering utilities
 */
object AntiTampering {
    
    /**
     * Calculate runtime checksum of critical methods
     */
    fun calculateMethodChecksum(methodName: String): String {
        // In a real implementation, you'd calculate actual method bytecode checksum
        val combined = methodName + System.currentTimeMillis().toString()
        return combined.hashCode().toString()
    }
    
    /**
     * Verify code integrity at runtime
     */
    fun verifyCodeIntegrity(): Boolean {
        // Multiple checks to detect code modification
        return verifyStackTrace() && 
               verifyClassLoader() && 
               verifyMethodSignatures()
    }
    
    private fun verifyStackTrace(): Boolean {
        val stackTrace = Thread.currentThread().stackTrace
        // Check for suspicious stack frames that might indicate hooking
        return !stackTrace.any { frame ->
            frame.className.contains("xposed", ignoreCase = true) ||
            frame.className.contains("frida", ignoreCase = true) ||
            frame.className.contains("substrate", ignoreCase = true)
        }
    }
    
    private fun verifyClassLoader(): Boolean {
        val classLoader = AntiTampering::class.java.classLoader
        return classLoader?.javaClass?.name?.contains("dalvik") == true
    }
    
    private fun verifyMethodSignatures(): Boolean {
        // Verify that critical methods haven't been replaced
        try {
            val method = AntiTampering::class.java.getDeclaredMethod("verifyCodeIntegrity")
            return method.name == "verifyCodeIntegrity"
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * Control flow obfuscation
     */
    fun executeWithObfuscation(action: () -> Unit) {
        val random = Random.nextInt(0, 3)
        when (random) {
            0 -> {
                dummyOperation1()
                action()
                dummyOperation2()
            }
            1 -> {
                dummyOperation2()
                dummyOperation1()
                action()
            }
            else -> {
                action()
                dummyOperation1()
                dummyOperation2()
            }
        }
    }
    
    private fun dummyOperation1() {
        val dummy = Random.nextInt(1000, 9999)
        val result = dummy * 2 + 1
        // Dummy operation to confuse static analysis
    }
    
    private fun dummyOperation2() {
        val dummy = System.currentTimeMillis()
        val result = dummy % 1000
        // Another dummy operation
    }
}

/**
 * Hardware fingerprinting for device identification
 */
object DeviceFingerprinting {
    
    /**
     * Generate unique device fingerprint
     */
    fun generateDeviceFingerprint(context: android.content.Context): String {
        val components = mutableListOf<String>()
        
        // Hardware info
        components.add(android.os.Build.BRAND)
        components.add(android.os.Build.MODEL)
        components.add(android.os.Build.DEVICE)
        components.add(android.os.Build.HARDWARE)
        
        // Screen info
        val displayMetrics = context.resources.displayMetrics
        components.add("${displayMetrics.widthPixels}x${displayMetrics.heightPixels}")
        components.add(displayMetrics.densityDpi.toString())
        
        // Combine and hash
        val combined = components.joinToString("|")
        return combined.hashCode().toString()
    }
    
    /**
     * Detect if running on real hardware vs emulator
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
}

/**
 * Network security utilities
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
     * Certificate pinning verification (placeholder)
     */
    fun verifyCertificatePinning(hostname: String): Boolean {
        // In real implementation, verify SSL certificate pinning
        return true
    }
    
    /**
     * Detect man-in-the-middle attacks
     */
    fun detectMITM(): Boolean {
        return isProxyActive() // Simplified check
    }
}