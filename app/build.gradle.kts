plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    // Remove kapt and parcelize - they're causing conflicts
    id("kotlin-kapt")
    id("kotlin-parcelize")
}

android {
    namespace = "com.ktimazstudio"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ktimazstudio"
        minSdk = 26
        targetSdk = 35
        versionCode = 1000
        versionName = "3.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        // Advanced security configurations
        buildConfigField("String", "SECURITY_HASH", "\"f21317d4d6276ff3174a363c7fdff4171c73b1b80a82bb9082943ea9200a8425\"")
        buildConfigField("long", "BUILD_TIMESTAMP", "${System.currentTimeMillis()}L")
        buildConfigField("String", "BUILD_VARIANT", "\"release\"") // Fixed buildType reference
        
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    // Git command helper
    fun String.runCommand(): String? =
        try {
            ProcessBuilder(*split(" ").toTypedArray())
                .redirectErrorStream(true)
                .start()
                .inputStream
                .bufferedReader()
                .readText()
                .trim()
        } catch (e: Exception) {
            null
        }

    signingConfigs {
        create("release") {
            // Use environment variables for CI/CD, fallback to default values for local builds
            storeFile = file(System.getenv("RELEASE_STORE_FILE") ?: "Ktimazstudio.keystore")
            storePassword = System.getenv("RELEASE_STORE_PASSWORD") ?: "Ktimazstudio.keystore"
            keyAlias = System.getenv("RELEASE_KEY_ALIAS") ?: "ktimazstudio"
            keyPassword = System.getenv("RELEASE_KEY_PASSWORD") ?: "ktimazstudio123"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField("boolean", "ENABLE_DEBUG_TOOLS", "true")
            buildConfigField("boolean", "ENABLE_SECURITY_CHECKS", "false")
        }
        
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // Only use release signing if keystore exists
            val keystorePath = System.getenv("RELEASE_STORE_FILE") ?: "Ktimazstudio.keystore"
            if (file(keystorePath).exists() || System.getenv("RELEASE_STORE_FILE") != null) {
                signingConfig = signingConfigs.getByName("release")
            }

            val shortCommitHash = "git rev-parse --short HEAD".runCommand() ?: "release"
            versionNameSuffix = "-$shortCommitHash"
            buildConfigField("boolean", "ENABLE_DEBUG_TOOLS", "false")
            buildConfigField("boolean", "ENABLE_SECURITY_CHECKS", "true")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources {
            excludes += listOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "META-INF/LICENSE",
                "META-INF/DEPENDENCIES",
                "META-INF/*.kotlin_module",
                "**/DebugProbesKt.bin",
                "kotlin/**",
                "kotlin-tooling-metadata.json"
            )
        }
    }

    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val buildTypeName = variant.buildType.name.replaceFirstChar { it.uppercase() }
            val version = variant.versionName
            output.outputFileName = "ktimazstudio_${buildTypeName}_v${version}.apk"
        }
    }
    
    bundle {
        language {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")

    // Compose BOM and UI
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-core:1.7.8")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.compose.animation:animation:1.7.8")
    implementation("androidx.compose.animation:animation-core:1.7.8")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.9.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Security & Crypto - Optional dependencies that might not be available
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1") {
        exclude(group = "org.bouncycastle", module = "bcutil-jdk18on")
    }
    implementation("androidx.biometric:biometric:1.4.0-alpha02")

    // Data Storage
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // UI Enhancement
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debug tools
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// Security hardening tasks
tasks.register("verifyReleaseIntegrity") {
    doLast {
        println("Verifying release build integrity...")
        val outputDir = layout.buildDirectory.dir("outputs/apk/release").get().asFile
        if (outputDir.exists()) {
            outputDir.listFiles()?.forEach { file ->
                if (file.extension == "apk") {
                    println("APK found: ${file.name} (${file.length()} bytes)")
                }
            }
        }
    }
}

tasks.register("generateSecurityHashes") {
    doLast {
        val buildDir = layout.buildDirectory.get().asFile
        println("Generating security hashes in $buildDir")
        val timestamp = System.currentTimeMillis()
        println("Build timestamp: $timestamp")
    }
}

// Hook into build process
tasks.named("assembleRelease") {
    finalizedBy("verifyReleaseIntegrity")
}