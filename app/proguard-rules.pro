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

# Core Android rules
-keep class * extends android.app.Activity
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.app.Application
-keep class * extends android.content.ContentProvider
-keep class **.R$* { *; }

# Preserve required entry points and UI components
-keepclassmembers class * {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Parcelables
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Anti-debugging and anti-reverse
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** v(...);
    public static *** e(...);
}
-assumenosideeffects class android.os.Debug {
    public static boolean isDebuggerConnected();
    public static boolean waitingForDebugger();
}

# Licensing
-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService

# Reflection protection
-keepattributes *Annotation*, EnclosingMethod, InnerClasses

# Obfuscation & Renaming via dictionary
-obfuscationdictionary obf/words.txt
-classobfuscationdictionary obf/class.txt
-packageobfuscationdictionary obf/package.txt

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
