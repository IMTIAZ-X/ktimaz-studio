package com.ktimazstudio.managers

import android.content.Context
import com.ktimazstudio.enums.SecurityIssue
import com.ktimazstudio.utils.EnhancedSecurityManager

/**
 * Security Manager - Wrapper around EnhancedSecurityManager
 * This maintains compatibility while using the enhanced implementation
 */
class SecurityManager(private val context: Context) {
    
    private val enhancedSecurityManager = EnhancedSecurityManager(context)
    
    fun getSecurityIssue(isInspectionMode: Boolean): SecurityIssue {
        return enhancedSecurityManager.getSecurityIssue(isInspectionMode)
    }
    
    fun isVpnActive(): Boolean {
        return enhancedSecurityManager.isVpnActive()
    }
    
    fun getSecurityStatus(): EnhancedSecurityManager.SecurityStatus {
        return enhancedSecurityManager.getSecurityStatus()
    }
    
    fun cleanup() {
        enhancedSecurityManager.cleanup()
    }
}