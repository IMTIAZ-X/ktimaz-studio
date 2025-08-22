package com.ktimazstudio.utils

import android.content.Context
import java.security.MessageDigest
import java.security.cert.Certificate
import android.util.Base64

object NetworkSecurity {
    
    private val pinnedCertificates = mapOf(
        "api.ktimazstudio.com" to "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        "cdn.ktimazstudio.com" to "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB="
    )
    
    fun verifyCertificatePinning(hostname: String, certificateChain: Array<Certificate>): Boolean {
        val expectedPin = pinnedCertificates[hostname] ?: return true
        return certificateChain.any { certificate ->
            val publicKeyHash = computePublicKeyPin(certificate)
            publicKeyHash == expectedPin
        }
    }
    
    private fun computePublicKeyPin(certificate: Certificate): String {
        val publicKeyBytes = certificate.publicKey.encoded
        val hash = MessageDigest.getInstance("SHA-256").digest(publicKeyBytes)
        return "sha256/${Base64.encodeToString(hash, Base64.NO_WRAP)}"
    }
    
    fun isProxyActive(): Boolean {
        val proxyProperties = listOf("http.proxyHost", "https.proxyHost", "http.proxyPort", "https.proxyPort")
        return proxyProperties.any { property ->
            System.getProperty(property)?.isNotEmpty() == true
        }
    }
    
    fun detectMITM(): Boolean {
        return isProxyActive() || detectSuspiciousNetworkBehavior()
    }
    
    private fun detectSuspiciousNetworkBehavior(): Boolean {
        val suspiciousProcesses = listOf("burp", "charles", "mitmproxy", "fiddler", "wireshark", "tcpdump", "intercepter")
        return try {
            val process = ProcessBuilder("ps", "-A").start()
            val output = process.inputStream.bufferedReader().readText().lowercase()
            suspiciousProcesses.any { tool -> output.contains(tool) }
        } catch (e: Exception) {
            false
        }
    }
    
    fun generateSecureHeaders(context: Context): Map<String, String> {
        val deviceToken = generateDeviceToken(context)
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
    
    private fun generateDeviceToken(context: Context): String {
        val components = listOf(
            android.os.Build.BRAND, android.os.Build.MODEL, android.os.Build.DEVICE,
            android.os.Build.VERSION.RELEASE, context.packageName
        )
        val combined = components.joinToString("|")
        val hash = MessageDigest.getInstance("SHA-256").digest(combined.toByteArray())
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }
    
    private fun generateRequestHash(vararg components: String): String {
        val combined = components.joinToString("|")
        val hash = MessageDigest.getInstance("SHA-256").digest(combined.toByteArray())
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }
}