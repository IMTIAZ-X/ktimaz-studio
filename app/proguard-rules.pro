# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile




# Licensing
-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService

# Reflection protection
-keepattributes *Annotation*, EnclosingMethod, InnerClasses

# Obfuscation & Renaming via dictionary
#-obfuscationdictionary dict.txt
#-classobfuscationdictionary dict.txt
#-packageobfuscationdictionary dict.txt

# Full obfuscation and shrinking
-dontskipnonpubliclibraryclasses
-dontusemixedcaseclassnames
-ignorewarnings
-dontwarn
-dontnote
-printusage
-dontpreverify
-verbose
-optimizationpasses 6
-allowaccessmodification
-overloadaggressively
-useuniqueclassmembernames
-flattenpackagehierarchy
-mergeinterfacesaggressively
-repackageclasses 'a'

# Remove debug info
-renamesourcefileattribute SourceFile
-keepattributes SourceFile, LineNumberTable

# Enable control flow and advanced optimization (R8 will handle)







# ProGuard Rules - Enhanced Security and Optimization for Ktimaz Studio
# https://developer.android.com/studio/build/shrink-code

# ==========================================
# Basic Android Rules
# ==========================================
# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom Application class
-keep public class com.ktimazstudio.KtimazApplication
-keep public class com.ktimazstudio.MainActivity

# Keep Activity classes that might be referenced via intent
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Fragment
-keep public class * extends androidx.fragment.app.Fragment

# ==========================================
# Security and Anti-Tampering
# ==========================================
# Obfuscate package and class names aggressively
-repackageclasses ''
-allowaccessmodification
-overloadaggressively

# Keep security-related classes but obfuscate them
-keep,allowobfuscation class com.ktimazstudio.managers.SecurityManager
-keep,allowobfuscation class com.ktimazstudio.managers.ApplicationManager
-keep,allowobfuscation class com.ktimazstudio.utils.EnhancedSecurityManager
-keep,allowobfuscation class com.ktimazstudio.utils.CryptoUtils
-keep,allowobfuscation class com.ktimazstudio.utils.AntiTampering
-keep,allowobfuscation class com.ktimazstudio.utils.DeviceFingerprinting
-keep,allowobfuscation class com.ktimazstudio.utils.NetworkSecurity

# Keep security enum values
-keep enum com.ktimazstudio.enums.SecurityIssue {
    *;
}

# Protect string constants (but allow obfuscation)
-keepclassmembers class * {
    static final java.lang.String *;
}

# Keep BuildConfig for security checks
-keep class com.ktimazstudio.BuildConfig {
    public static final java.lang.String SECURITY_HASH;
    public static final long BUILD_TIMESTAMP;
    public static final boolean ENABLE_SECURITY_CHECKS;
}

# ==========================================
# JNI and Native Code
# ==========================================
# Keep JNI interface classes
-keep class com.ktimazstudio.security.NativeSecurityManager {
    public native <methods>;
}

-keep class com.ktimazstudio.security.NativeCrypto {
    public native <methods>;
}

# Keep classes that interface with native code
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep security callback interfaces
-keep interface com.ktimazstudio.security.SecurityCallback {
    public void onSecurityThreat(int);
}

# ==========================================
# Compose and UI Components