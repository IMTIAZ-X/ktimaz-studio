// ktimaz_security.h - Main Security Module Header
#ifndef KTIMAZ_SECURITY_H
#define KTIMAZ_SECURITY_H

#include <jni.h>
#include <android/log.h>
#include <string>
#include <vector>
#include <memory>

// Security configuration
#ifdef ENABLE_SECURITY_FEATURES
    #define SECURITY_ENABLED 1
#else
    #define SECURITY_ENABLED 0
#endif

#ifdef ENABLE_DEBUG_LOGGING
    #define LOG_TAG "KtimazSecurity"
    #define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
    #define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
    #define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
    #define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#else
    #define LOGD(...)
    #define LOGI(...)
    #define LOGW(...)
    #define LOGE(...)
#endif

// Security check results
enum class SecurityResult {
    SECURE = 0,
    DEBUGGER_DETECTED = 1,
    EMULATOR_DETECTED = 2,
    ROOT_DETECTED = 3,
    HOOKING_DETECTED = 4,
    TAMPERING_DETECTED = 5,
    UNKNOWN_THREAT = 99
};

// Security check context
struct SecurityContext {
    bool enable_anti_debug;
    bool enable_anti_emulator;
    bool enable_anti_root;
    bool enable_anti_hook;
    bool enable_integrity_check;
    std::string app_signature;
    std::string expected_package_name;
};

// Main security class
class KtimazSecurity {
public:
    explicit KtimazSecurity(const SecurityContext& context);
    ~KtimazSecurity();

    // Core security checks
    SecurityResult performSecurityCheck();
    bool isDebuggerAttached();
    bool isRunningOnEmulator();
    bool isDeviceRooted();
    bool isHookingFrameworkDetected();
    bool isApplicationTampered();

    // Advanced security features
    bool performIntegrityCheck(const std::string& expected_hash);
    std::string calculateApplicationHash();
    bool detectRuntimeManipulation();
    bool verifyApplicationSignature();

    // Utility functions
    std::string getDeviceFingerprint();
    bool isSecureEnvironment();
    void obfuscateMemory(void* ptr, size_t size);

private:
    SecurityContext context_;
    bool security_breach_detected_;
    
    // Internal security methods
    bool checkTracerPid();
    bool checkDebugFlags();
    bool checkEmulatorFiles();
    bool checkEmulatorProperties();
    bool checkRootFiles();
    bool checkRootApps();
    bool checkSuperuserBinaries();
    bool detectXposed();
    bool detectFrida();
    bool detectSubstrate();
    
    // Anti-tampering
    bool verifyCodeIntegrity();
    bool checkMemoryPatterns();
    
    // Obfuscation helpers
    void scrambleString(std::string& str);
    uint32_t calculateCRC32(const void* data, size_t length);
};

// JNI interface functions
extern "C" {
    JNIEXPORT jint JNICALL
    Java_com_ktimazstudio_security_NativeSecurityManager_performSecurityCheck(
        JNIEnv *env, jobject thiz, jstring app_signature, jstring package_name);

    JNIEXPORT jboolean JNICALL
    Java_com_ktimazstudio_security_NativeSecurityManager_isDebuggerAttached(
        JNIEnv *env, jobject thiz);

    JNIEXPORT jboolean JNICALL
    Java_com_ktimazstudio_security_NativeSecurityManager_isRunningOnEmulator(
        JNIEnv *env, jobject thiz);

    JNIEXPORT jboolean JNICALL
    Java_com_ktimazstudio_security_NativeSecurityManager_isDeviceRooted(
        JNIEnv *env, jobject thiz);

    JNIEXPORT jstring JNICALL
    Java_com_ktimazstudio_security_NativeSecurityManager_getDeviceFingerprint(
        JNIEnv *env, jobject thiz);

    JNIEXPORT jstring JNICALL
    Java_com_ktimazstudio_security_NativeSecurityManager_calculateAppHash(
        JNIEnv *env, jobject thiz);

    JNIEXPORT jbyteArray JNICALL
    Java_com_ktimazstudio_security_NativeCrypto_encryptData(
        JNIEnv *env, jobject thiz, jbyteArray data, jbyteArray key);

    JNIEXPORT jbyteArray JNICALL
    Java_com_ktimazstudio_security_NativeCrypto_decryptData(
        JNIEnv *env, jobject thiz, jbyteArray encrypted_data, jbyteArray key);

    JNIEXPORT jbyteArray JNICALL
    Java_com_ktimazstudio_security_NativeCrypto_generateSecureRandom(
        JNIEnv *env, jobject thiz, jint length);

    JNIEXPORT jstring JNICALL
    Java_com_ktimazstudio_security_NativeCrypto_computeHash(
        JNIEnv *env, jobject thiz, jbyteArray data, jstring algorithm);
}

// Utility macros for security
#define SECURITY_CHECK(condition, result) \
    do { \
        if (!(condition)) { \
            LOGE("Security check failed: %s", #condition); \
            return result; \
        } \
    } while(0)

#define OBFUSCATE_CALL(func) \
    do { \
        volatile auto ptr = reinterpret_cast<void*>(&func); \
        reinterpret_cast<decltype(&func)>(ptr)(); \
    } while(0)

// String obfuscation at compile time
constexpr char obfuscate_char(char c, int key) {
    return c ^ key;
}

template<int N, int K>
class ObfuscatedString {
private:
    char data_[N];
    
public:
    constexpr ObfuscatedString(const char (&str)[N]) : data_{} {
        for (int i = 0; i < N; ++i) {
            data_[i] = obfuscate_char(str[i], K);
        }
    }
    
    std::string decrypt() const {
        std::string result;
        result.reserve(N - 1);
        for (int i = 0; i < N - 1; ++i) {
            result += obfuscate_char(data_[i], K);
        }
        return result;
    }
};

#define OBFUSCATED_STRING(str) \
    (ObfuscatedString<sizeof(str), __COUNTER__>(str).decrypt())

// Anti-debugging measures
#ifdef ENABLE_ANTI_DEBUG
    #define ANTI_DEBUG_CHECK() \
        do { \
            if (KtimazSecurity::isDebuggerAttached()) { \
                std::terminate(); \
            } \
        } while(0)
#else
    #define ANTI_DEBUG_CHECK()
#endif

// Memory protection
class SecureMemory {
public:
    static void* secure_malloc(size_t size);
    static void secure_free(void* ptr, size_t size);
    static void secure_memset(void* ptr, int value, size_t size);
    static bool protect_memory(void* addr, size_t len, int prot);
    static bool lock_memory(void* addr, size_t len);
    static bool unlock_memory(void* addr, size_t len);
};

// RAII secure memory wrapper
template<typename T>
class SecureBuffer {
private:
    T* data_;
    size_t size_;
    
public:
    explicit SecureBuffer(size_t count) 
        : size_(count * sizeof(T)) {
        data_ = static_cast<T*>(SecureMemory::secure_malloc(size_));
        if (data_) {
            SecureMemory::lock_memory(data_, size_);
        }
    }
    
    ~SecureBuffer() {
        if (data_) {
            SecureMemory::secure_memset(data_, 0, size_);
            SecureMemory::unlock_memory(data_, size_);
            SecureMemory::secure_free(data_, size_);
        }
    }
    
    T* get() { return data_; }
    const T* get() const { return data_; }
    size_t size() const { return size_ / sizeof(T); }
    
    // Non-copyable
    SecureBuffer(const SecureBuffer&) = delete;
    SecureBuffer& operator=(const SecureBuffer&) = delete;
    
    // Movable
    SecureBuffer(SecureBuffer&& other) noexcept 
        : data_(other.data_), size_(other.size_) {
        other.data_ = nullptr;
        other.size_ = 0;
    }
};

#endif // KTIMAZ_SECURITY_H