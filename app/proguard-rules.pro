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
