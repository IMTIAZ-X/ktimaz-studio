// ktimaz_security_jni.cpp - JNI Implementation for Native Security
#include "ktimaz_security.h"
#include "jni_helpers.h"
#include <jni.h>
#include <string>
#include <vector>
#include <memory>
#include <unistd.h>
#include <sys/ptrace.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <dirent.h>
#include <fstream>
#include <sstream>
#include <thread>
#include <chrono>
#include <random>

// Global security context
static std::unique_ptr<KtimazSecurity> g_security_instance = nullptr;
static bool g_security_initialized = false;

// Anti-debugging thread
static std::atomic<bool> g_anti_debug_running{false};
static std::thread g_anti_debug_thread;

// Obfuscated strings
static const char* OBFUSCATED_PROC_STATUS = OBFUSCATED_STRING("/proc/self/status");
static const char* OBFUSCATED_TRACER_PID = OBFUSCATED_STRING("TracerPid:");
static const char* OBFUSCATED_DEBUG_PROP = OBFUSCATED_STRING("ro.debuggable");

// Security violation callback
static std::function<void(SecurityResult)> g_security_callback = nullptr;

// Initialize security module
static void initializeSecurity() {
    if (g_security_initialized) {
        return;
    }
    
    SecurityContext context{};
    context.enable_anti_debug = true;
    context.enable_anti_emulator = true;
    context.enable_anti_root = true;
    context.enable_anti_hook = true;
    context.enable_integrity_check = true;
    context.app_signature = ""; // Will be set from Java
    context.expected_package_name = "com.ktimazstudio";
    
    g_security_instance = std::make_unique<KtimazSecurity>(context);
    g_security_initialized = true;
    
    LOGI("Native security module initialized");
}

// Anti-debugging monitoring thread
static void antiDebugMonitorThread() {
    LOGI("Starting anti-debug monitoring thread");
    
    while (g_anti_debug_running.load()) {
        try {
            // Check for debugger attachment
            if (g_security_instance && g_security_instance->isDebuggerAttached()) {
                LOGE("Debugger detected - terminating application");
                std::terminate();
            }
            
            // Check for tampering
            if (g_security_instance && g_security_instance->detectRuntimeManipulation()) {
                LOGE("Runtime manipulation detected - terminating application");
                std::terminate();
            }
            
            // Sleep for a random interval to avoid detection
            std::random_device rd;
            std::mt19937 gen(rd());
            std::uniform_int_distribution<> dis(800, 1200);
            std::this_thread::sleep_for(std::chrono::milliseconds(dis(gen)));
            
        } catch (...) {
            // Ignore exceptions to prevent crash
        }
    }
    
    LOGI("Anti-debug monitoring thread stopped");
}

// JNI Method Implementations

extern "C" JNIEXPORT jint JNICALL
Java_com_ktimazstudio_security_NativeSecurityManager_performSecurityCheck(
    JNIEnv *env, jobject thiz, jstring app_signature, jstring package_name) {
    
    ANTI_DEBUG_CHECK();
    
    try {
        initializeSecurity();
        
        if (!g_security_instance) {
            return static_cast<jint>(SecurityResult::UNKNOWN_THREAT);
        }
        
        // Update security context with app info
        std::string signature = JNIHelpers::jstringToString(env, app_signature);
        std::string package = JNIHelpers::jstringToString(env, package_name);
        
        LOGI("Performing security check for package: %s", package.c_str());
        
        SecurityResult result = g_security_instance->performSecurityCheck();
        
        if (result != SecurityResult::SECURE) {
            LOGW("Security threat detected: %d", static_cast<int>(result));
            
            // Trigger security callback if registered
            if (g_security_callback) {
                g_security_callback(result);
            }
        }
        
        return static_cast<jint>(result);
        
    } catch (const std::exception& e) {
        LOGE("Exception in performSecurityCheck: %s", e.what());
        return static_cast<jint>(SecurityResult::UNKNOWN_THREAT);
    }
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_ktimazstudio_security_NativeSecurityManager_isDebuggerAttached(
    JNIEnv *env, jobject thiz) {
    
    try {
        initializeSecurity();
        
        if (!g_security_instance) {
            return JNI_FALSE;
        }
        
        bool result = g_security_instance->isDebuggerAttached();
        LOGD("Debugger check result: %s", result ? "ATTACHED" : "NOT_ATTACHED");
        
        return result ? JNI_TRUE : JNI_FALSE;
        
    } catch (...) {
        return JNI_TRUE; // Assume compromised if we can't check
    }
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_ktimazstudio_security_NativeSecurityManager_isRunningOnEmulator(
    JNIEnv *env, jobject thiz) {
    
    ANTI_DEBUG_CHECK();
    
    try {
        initializeSecurity();
        
        if (!g_security_instance) {
            return JNI_FALSE;
        }
        
        bool result = g_security_instance->isRunningOnEmulator();
        LOGD("Emulator check result: %s", result ? "EMULATOR" : "REAL_DEVICE");
        
        return result ? JNI_TRUE : JNI_FALSE;
        
    } catch (...) {
        return JNI_TRUE; // Assume emulator if we can't check
    }
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_ktimazstudio_security_NativeSecurityManager_isDeviceRooted(
    JNIEnv *env, jobject thiz) {
    
    ANTI_DEBUG_CHECK();
    
    try {
        initializeSecurity();
        
        if (!g_security_instance) {
            return JNI_FALSE;
        }
        
        bool result = g_security_instance->isDeviceRooted();
        LOGD("Root check result: %s", result ? "ROOTED" : "NOT_ROOTED");
        
        return result ? JNI_TRUE : JNI_FALSE;
        
    } catch (...) {
        return JNI_TRUE; // Assume rooted if we can't check
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_ktimazstudio_security_NativeSecurityManager_getDeviceFingerprint(
    JNIEnv *env, jobject thiz) {
    
    ANTI_DEBUG_CHECK();
    
    try {
        initializeSecurity();
        
        if (!g_security_instance) {
            return env->NewStringUTF("unknown");
        }
        
        std::string fingerprint = g_security_instance->getDeviceFingerprint();
        LOGD("Generated device fingerprint: %s", fingerprint.substr(0, 16).c_str());
        
        return env->NewStringUTF(fingerprint.c_str());
        
    } catch (...) {
        return env->NewStringUTF("error");
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_ktimazstudio_security_NativeSecurityManager_calculateAppHash(
    JNIEnv *env, jobject thiz) {
    
    ANTI_DEBUG_CHECK();
    
    try {
        initializeSecurity();
        
        if (!g_security_instance) {
            return env->NewStringUTF("unknown");
        }
        
        std::string hash = g_security_instance->calculateApplicationHash();
        LOGD("Calculated app hash: %s", hash.substr(0, 16).c_str());
        
        return env->NewStringUTF(hash.c_str());
        
    } catch (...) {
        return env->NewStringUTF("error");
    }
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_ktimazstudio_security_NativeSecurityManager_startAntiDebugMonitoring(
    JNIEnv *env, jobject thiz) {
    
    try {
        if (g_anti_debug_running.load()) {
            LOGW("Anti-debug monitoring already running");
            return JNI_TRUE;
        }
        
        initializeSecurity();
        
        g_anti_debug_running.store(true);
        g_anti_debug_thread = std::thread(antiDebugMonitorThread);
        
        LOGI("Anti-debug monitoring started");
        return JNI_TRUE;
        
    } catch (...) {
        LOGE("Failed to start anti-debug monitoring");
        return JNI_FALSE;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_ktimazstudio_security_NativeSecurityManager_stopAntiDebugMonitoring(
    JNIEnv *env, jobject thiz) {
    
    try {
        if (!g_anti_debug_running.load()) {
            return;
        }
        
        g_anti_debug_running.store(false);
        
        if (g_anti_debug_thread.joinable()) {
            g_anti_debug_thread.join();
        }
        
        LOGI("Anti-debug monitoring stopped");
        
    } catch (...) {
        LOGE("Error stopping anti-debug monitoring");
    }
}

// Crypto JNI implementations
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_ktimazstudio_security_NativeCrypto_encryptData(
    JNIEnv *env, jobject thiz, jbyteArray data, jbyteArray key) {
    
    ANTI_DEBUG_CHECK();
    
    try {
        if (!data || !key) {
            return nullptr;
        }
        
        std::vector<uint8_t> input_data = JNIHelpers::jbyteArrayToVector(env, data);
        std::vector<uint8_t> encryption_key = JNIHelpers::jbyteArrayToVector(env, key);
        
        // Implement AES encryption here
        // For now, return a simple XOR encryption as placeholder
        std::vector<uint8_t> encrypted_data(input_data.size());
        for (size_t i = 0; i < input_data.size(); ++i) {
            encrypted_data[i] = input_data[i] ^ encryption_key[i % encryption_key.size()];
        }
        
        return JNIHelpers::vectorToJbyteArray(env, encrypted_data);
        
    } catch (...) {
        LOGE("Encryption failed");
        return nullptr;
    }
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_ktimazstudio_security_NativeCrypto_decryptData(
    JNIEnv *env, jobject thiz, jbyteArray encrypted_data, jbyteArray key) {
    
    ANTI_DEBUG_CHECK();
    
    try {
        if (!encrypted_data || !key) {
            return nullptr;
        }
        
        std::vector<uint8_t> input_data = JNIHelpers::jbyteArrayToVector(env, encrypted_data);
        std::vector<uint8_t> decryption_key = JNIHelpers::jbyteArrayToVector(env, key);
        
        // Implement AES decryption here
        // For now, return simple XOR decryption as placeholder
        std::vector<uint8_t> decrypted_data(input_data.size());
        for (size_t i = 0; i < input_data.size(); ++i) {
            decrypted_data[i] = input_data[i] ^ decryption_key[i % decryption_key.size()];
        }
        
        return JNIHelpers::vectorToJbyteArray(env, decrypted_data);
        
    } catch (...) {
        LOGE("Decryption failed");
        return nullptr;
    }
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_ktimazstudio_security_NativeCrypto_generateSecureRandom(
    JNIEnv *env, jobject thiz, jint length) {
    
    try {
        if (length <= 0 || length > 4096) {
            return nullptr;
        }
        
        std::vector<uint8_t> random_data(length);
        
        // Use /dev/urandom for secure random generation
        std::ifstream urandom("/dev/urandom", std::ios::binary);
        if (urandom.is_open()) {
            urandom.read(reinterpret_cast<char*>(random_data.data()), length);
            urandom.close();
        } else {
            // Fallback to C++ random
            std::random_device rd;
            std::mt19937 gen(rd());
            std::uniform_int_distribution<uint8_t> dis(0, 255);
            
            for (int i = 0; i < length; ++i) {
                random_data[i] = dis(gen);
            }
        }
        
        return JNIHelpers::vectorToJbyteArray(env, random_data);
        
    } catch (...) {
        LOGE("Secure random generation failed");
        return nullptr;
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_ktimazstudio_security_NativeCrypto_computeHash(
    JNIEnv *env, jobject thiz, jbyteArray data, jstring algorithm) {
    
    ANTI_DEBUG_CHECK();
    
    try {
        if (!data || !algorithm) {
            return nullptr;
        }
        
        std::vector<uint8_t> input_data = JNIHelpers::jbyteArrayToVector(env, data);
        std::string algo = JNIHelpers::jstringToString(env, algorithm);
        
        // Simple hash implementation (replace with proper crypto library)
        uint32_t hash = 0x811c9dc5; // FNV-1a offset basis
        for (uint8_t byte : input_data) {
            hash ^= byte;
            hash *= 0x01000193; // FNV-1a prime
        }
        
        // Convert to hex string
        std::stringstream ss;
        ss << std::hex << hash;
        std::string result = ss.str();
        
        return env->NewStringUTF(result.c_str());
        
    } catch (...) {
        LOGE("Hash computation failed");
        return nullptr;
    }
}

// JNI_OnLoad - Called when library is loaded
extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = nullptr;
    
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    
    // Initialize JNI helpers
    JNIHelpers::setJavaVM(vm);
    
    LOGI("Native security library loaded successfully");
    LOGI("Build timestamp: %s", __DATE__ " " __TIME__);
    
    // Perform initial security checks
    try {
        initializeSecurity();
        
        if (g_security_instance) {
            SecurityResult result = g_security_instance->performSecurityCheck();
            if (result != SecurityResult::SECURE) {
                LOGW("Security threat detected during library load: %d", static_cast<int>(result));
            }
        }
    } catch (...) {
        LOGE("Exception during initial security check");
    }
    
    return JNI_VERSION_1_6;
}

// JNI_OnUnload - Called when library is unloaded
extern "C" JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved) {
    try {
        // Stop anti-debug monitoring
        if (g_anti_debug_running.load()) {
            g_anti_debug_running.store(false);
            if (g_anti_debug_thread.joinable()) {
                g_anti_debug_thread.join();
            }
        }
        
        // Clean up security instance
        g_security_instance.reset();
        g_security_initialized = false;
        
        LOGI("Native security library unloaded");
        
    } catch (...) {
        // Ignore exceptions during cleanup
    }
}

// Helper function to register security callback
extern "C" JNIEXPORT void JNICALL
Java_com_ktimazstudio_security_NativeSecurityManager_registerSecurityCallback(
    JNIEnv *env, jobject thiz, jobject callback) {
    
    try {
        // Store global reference to callback object
        static jobject g_callback_object = nullptr;
        
        if (g_callback_object) {
            env->DeleteGlobalRef(g_callback_object);
        }
        
        if (callback) {
            g_callback_object = env->NewGlobalRef(callback);
            
            g_security_callback = [=](SecurityResult result) {
                JNIEnv* callback_env = nullptr;
                JavaVM* vm = JNIHelpers::getJavaVM();
                
                if (vm && vm->AttachCurrentThread(&callback_env, nullptr) == JNI_OK) {
                    jclass callback_class = callback_env->GetObjectClass(g_callback_object);
                    jmethodID callback_method = callback_env->GetMethodID(
                        callback_class, "onSecurityThreat", "(I)V");
                    
                    if (callback_method) {
                        callback_env->CallVoidMethod(g_callback_object, callback_method, 
                                                   static_cast<jint>(result));
                    }
                    
                    vm->DetachCurrentThread();
                }
            };
        } else {
            g_security_callback = nullptr;
        }
        
    } catch (...) {
        LOGE("Failed to register security callback");
    }
}