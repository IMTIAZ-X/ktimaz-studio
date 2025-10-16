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
-obfuscationdictionary dict.txt
-classobfuscationdictionary dict.txt
-packageobfuscationdictionary dict.txt

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






# ===== KTIMAZ STUDIO ENHANCED PROGUARD RULES =====

# Keep application class
-keep class com.ktimazstudio.KtimazApplication { *; }

# Keep all activities
-keep class * extends androidx.activity.ComponentActivity { *; }
-keep class com.ktimazstudio.MainActivity { *; }
-keep class com.ktimazstudio.SettingsActivity { *; }
-keep class com.ktimazstudio.ComingActivity { *; }

# Keep managers - these contain critical business logic
-keep class com.ktimazstudio.managers.** { *; }

# Keep security classes (but obfuscate some methods)
-keep class com.ktimazstudio.utils.EnhancedSecurityManager {
    public <methods>;
}
-keep class com.ktimazstudio.utils.CryptoUtils {
    public <methods>;
}

# Keep enums
-keep enum com.ktimazstudio.enums.** { *; }

# Keep data classes and their serialization
-keepclassmembers class com.ktimazstudio.** {
    @kotlinx.serialization.SerialName <fields>;
    @kotlinx.serialization.Serializable *;
}

# Keep Compose specific classes
-keep class androidx.compose.** { *; }
-keep class * extends androidx.compose.runtime.** { *; }

# Keep Kotlin metadata
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations
-keepattributes *Annotation*

# Keep Kotlin intrinsics
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# Keep coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep Android architecture components
-keep class androidx.lifecycle.** { *; }
-keep class androidx.savedstate.** { *; }
-keep interface androidx.lifecycle.** { *; }

# Keep security-related classes but obfuscate implementation details
-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# Keep biometric classes
-keep class androidx.biometric.** { *; }

# Keep reflection-used classes
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom Views and their constructors
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Remove println statements
-assumenosideeffects class java.lang.System {
    public static void out.println(...);
    public static void err.println(...);
}

# Keep BuildConfig for security checks
-keep class com.ktimazstudio.BuildConfig { *; }

# Advanced obfuscation settings
-repackageclasses ''
-allowaccessmodification
-printmapping build/outputs/mapping/release/mapping.txt

# Optimization settings
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5

# Security: Remove debug information but keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Security: Obfuscate package names except for main application package
-repackageclasses 'com.ktimazstudio.obfuscated'

# Advanced string encryption (security through obscurity)
-adaptclassstrings
-adaptresourcefilenames
-adaptresourcefilecontents

# Remove unused resources
#-shrinkresources

# Security: Keep critical security method names from being obfuscated
-keepnames class com.ktimazstudio.utils.EnhancedSecurityManager {
    public boolean getSecurityIssue(...);
    public boolean isVpnActive();
}

# Keep crash reporting
-keep public class * extends java.lang.Exception

# Network security
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# JSON parsing
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Image loading libraries (if used)
-keep class com.bumptech.glide.** { *; }
-keep class com.squareup.picasso.** { *; }

# Room database (if used)
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# WorkManager (if used)
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker { *; }

# Compose Navigation
-keep class androidx.navigation.** { *; }

# Material 3 components
-keep class com.google.android.material.** { *; }

# Custom rules for specific security methods - keep public interface but obfuscate internals
-keep class com.ktimazstudio.utils.** {
    public *;
}
-keepclassmembernames class com.ktimazstudio.utils.** {
    private *;
    protected *;
}

# Aggressive obfuscation for sensitive classes
-keepclassmembers class com.ktimazstudio.utils.StringObfuscation {
    private static *;
}

# Keep enum values used in when statements
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Disable certain warnings
-dontwarn java.lang.invoke.StringConcatFactory
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# Final security measures
-dontskipnonpubliclibraryclassmembers
-forceprocessing
