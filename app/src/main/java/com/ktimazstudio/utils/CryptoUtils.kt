package com.ktimazstudio.utils

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.conscrypt.Conscrypt
import java.security.*
import java.security.spec.ECGenParameterSpec
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

/**
 * Advanced cryptographic utilities with military-grade security
 */
class CryptoUtils {
    
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeystore"
        private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val AES_KEY_SIZE = 256
        private const val GCM_TAG_LENGTH = 16
        private const val AES_KEY_ALIAS = "KtimazAESKey"
        
        init {
            // Install enhanced security providers
            if (Security.getProvider("BC") == null) {
                Security.addProvider(BouncyCastleProvider())
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Security.getProvider("Conscrypt") == null) {
                    Security.insertProviderAt(Conscrypt.newProvider(), 1)
                }
            }
        }
    }
    
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    private val secureRandom = SecureRandom()
    
    init {
        generateAESKeyIfNotExists()
    }
    
    private fun generateAESKeyIfNotExists() {
        if (keyStore.containsAlias(AES_KEY_ALIAS)) return
        
        try {
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
        } catch (e: Exception) {
            // Continue without keystore if generation fails
        }
    }
    
    private fun getAESKey(): SecretKey? {
        return try {
            keyStore.getKey(AES_KEY_ALIAS, null) as? SecretKey
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Compute SHA-512 hash
     */
    fun computeSHA512(data: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-512")
        return digest.digest(data)
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
            android.os.Build.SUPPORTED_ABIS.joinToString(",")
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
 * Network security utilities
 */
object NetworkSecurity {
    
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