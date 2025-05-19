plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

def gitCommitHash = "git rev-parse HEAD".execute().text.trim()
def shortCommitHash = "git rev-parse --short HEAD".execute().text.trim()

android {
    namespace = "com.ktimazstudio"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ktimazstudio"
        minSdk = 25
        targetSdk = 35
        versionCode = 1
        versionName = "1.0 - im${shortCommitHash}"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        buildConfigField "String", "VERSION_NAME_WITHOUT_FLAVOR", "\"v7.0.0-SNAPSHOT-${shortCommitHash}\""
    }

    signingConfigs {
        release {
            storeFile file(project.property("RELEASE_STORE_FILE"))
            storePassword project.property("RELEASE_STORE_PASSWORD")
            keyAlias project.property("RELEASE_KEY_ALIAS")
            keyPassword project.property("RELEASE_KEY_PASSWORD")
            enableV1Signing true
            enableV2Signing true
            enableV3Signing true
            enableV4Signing true
        }
    }

    buildTypes {
        release {
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            signingConfig signingConfigs.release
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_21
        targetCompatibility JavaVersion.VERSION_21
    }

    kotlin {
        jvmToolchain(21)
    }

    buildFeatures {
        compose true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packagingOptions {
        resources {
            excludes += [
                "kotlin/**",
                "kotlin-tooling-metadata.json",
                "assets/dexopt/**",
                "assets/dexopt/baseline.prof",
                "assets/dexopt/baseline.profm",
                "META-INF/LICENSE",
                "META-INF/DEPENDENCIES",
                "META-INF/*.kotlin_module",
                "**/DebugProbesKt.bin",
                "okhttp3/internal/publicsuffix/NOTICE",
                "okhttp3/**",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1"
            ]
        }
    }

    applicationVariants.all { variant ->
        variant.outputs.all { output ->
            def apkName = "ktimazstudio_${variant.name}_v${defaultConfig.versionName}.apk"
            output.outputFileName = apkName
        }
    }
}
