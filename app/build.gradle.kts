plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.ktimazstudio"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ktimazstudio"
        minSdk = 25
        targetSdk = 35
        versionCode = 1000
        versionName = "3.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        // Enhanced security configurations
        buildConfigField("String", "SECURITY_HASH", "\"f21317d4d6276ff3174a363c7fdff4171c73b1b80a82bb9082943ea9200a8425\"")
        buildConfigField("long", "BUILD_TIMESTAMP", "${System.currentTimeMillis()}L")
        buildConfigField("String", "API_BASE_URL", "\"https://api.ktimazstudio.com\"")
        buildConfigField("boolean", "ENABLE_LOGGING", "false")
        
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    signingConfigs {
        create("release") {
            val storeFilePath = System.getenv("RELEASE_STORE_FILE") ?: "Ktimazstudio.keystore"
            val storePass = System.getenv("RELEASE_STORE_PASSWORD") ?: "ktimazstudio123"
            val keyAliasName = System.getenv("RELEASE_KEY_ALIAS") ?: "ktimazstudio" 
            val keyPass = System.getenv("RELEASE_KEY_PASSWORD") ?: "ktimazstudio123"
            
            storeFile = file(storeFilePath)
            storePassword = storePass
            keyAlias = keyAliasName
            keyPassword = keyPass
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
            buildConfigField("String", "BUILD_VARIANT", "\"debug\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
        }
        
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            versionNameSuffix = "-release"
            buildConfigField("boolean", "ENABLE_DEBUG_TOOLS", "false")
            buildConfigField("boolean", "ENABLE_SECURITY_CHECKS", "true")
            buildConfigField("String", "BUILD_VARIANT", "\"release\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // FIXED: Updated Kotlin options to use compilerOptions DSL
    kotlin {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
            freeCompilerArgs.addAll(
                listOf(
                    "-opt-in=kotlin.RequiresOptIn",
                    "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                    "-opt-in=androidx.compose.animation.ExperimentalAnimationApi"
                )
            )
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/DEPENDENCIES",
                "META-INF/*.kotlin_module",
                "**/DebugProbesKt.bin",
                "kotlin/**",
                "kotlin-tooling-metadata.json",
                "DebugProbesKt.bin"
            )
        }
    }
}

dependencies {
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
    implementation("org.conscrypt:conscrypt-android:2.5.2")
    implementation ("androidx.fragment:fragment-ktx:1.8.4")
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM and UI
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Enhanced dependencies for optimized functionality
    implementation("androidx.compose.material:material-icons-core:1.7.8")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.compose.animation:animation:1.7.8")
    implementation("androidx.compose.animation:animation-core:1.7.8")
    implementation("androidx.compose.animation:animation-graphics:1.7.8")
    implementation("androidx.navigation:navigation-compose:2.9.0")

    // Coroutines with enhanced support
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // Security & Biometrics
    implementation("androidx.biometric:biometric:1.4.0-alpha02")
    
    // System UI and splash screen
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Data Storage with encryption support
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Enhanced permissions
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // Performance and memory optimization
    implementation("androidx.profileinstaller:profileinstaller:1.4.1")
    implementation("androidx.startup:startup-runtime:1.2.0")

    // Testing frameworks
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("androidx.compose.ui:ui-test-junit4:1.7.8")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debug tools
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.8")
    debugImplementation("androidx.compose.ui:ui-tooling-preview:1.7.8")
}
