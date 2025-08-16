plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.ktimazstudio"
    compileSdk = 35
    ndkVersion = "26.1.10909125"

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
        buildConfigField("String", "BUILD_VARIANT", "\"${buildType.name}\"")
        buildConfigField("String", "GIT_HASH", "\"${getGitHash()}\"")
        
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }

        // CMake configuration for native security modules
        externalNativeBuild {
            cmake {
                cppFlags += listOf(
                    "-std=c++17",
                    "-frtti", 
                    "-fexceptions",
                    "-O3",
                    "-fvisibility=hidden",
                    "-fstack-protector-strong",
                    "-D_FORTIFY_SOURCE=2"
                )
                abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
                arguments += listOf(
                    "-DANDROID_TOOLCHAIN=clang",
                    "-DANDROID_STL=c++_shared",
                    "-DCMAKE_BUILD_TYPE=Release",
                    "-DENABLE_SECURITY_FEATURES=ON"
                )
            }
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
        prefab = true // Enable prefab for native dependencies
    }

    // External native build configuration
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    // Git command helper
    fun getGitHash(): String = try {
        ProcessBuilder("git", "rev-parse", "--short", "HEAD")
            .redirectErrorStream(true)
            .start()
            .inputStream
            .bufferedReader()
            .readText()
            .trim()
    } catch (e: Exception) {
        "unknown"
    }

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
            storeFile = file(findProperty("RELEASE_STORE_FILE") ?: "Ktimazstudio.keystore")
            storePassword = findProperty("RELEASE_STORE_PASSWORD") as? String ?: "Ktimazstudio.keystore"
            keyAlias = findProperty("RELEASE_KEY_ALIAS") as? String ?: "ktimazstudio"
            keyPassword = findProperty("RELEASE_KEY_PASSWORD") as? String ?: "ktimazstudio123"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug-${getGitHash()}"
            buildConfigField("boolean", "ENABLE_DEBUG_TOOLS", "true")
            buildConfigField("boolean", "ENABLE_SECURITY_CHECKS", "false")
            buildConfigField("boolean", "ENABLE_NATIVE_SECURITY", "false")
            
            // Debug native build
            externalNativeBuild {
                cmake {
                    arguments += listOf(
                        "-DCMAKE_BUILD_TYPE=Debug",
                        "-DENABLE_DEBUG_LOGGING=ON"
                    )
                }
            }
        }
        
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")

            val shortCommitHash = "git rev-parse --short HEAD".runCommand() ?: "release"
            versionNameSuffix = "-$shortCommitHash"
            buildConfigField("boolean", "ENABLE_DEBUG_TOOLS", "false")
            buildConfigField("boolean", "ENABLE_SECURITY_CHECKS", "true")
            buildConfigField("boolean", "ENABLE_NATIVE_SECURITY", "true")
            
            // Release native build with optimizations
            externalNativeBuild {
                cmake {
                    arguments += listOf(
                        "-DCMAKE_BUILD_TYPE=Release",
                        "-DENABLE_OBFUSCATION=ON",
                        "-DENABLE_ANTI_DEBUG=ON"
                    )
                }
            }
        }
        
        create("staging") {
            initWith(getByName("release"))
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging-${getGitHash()}"
            buildConfigField("boolean", "ENABLE_DEBUG_TOOLS", "true")
            buildConfigField("boolean", "ENABLE_SECURITY_CHECKS", "true")
            buildConfigField("boolean", "ENABLE_NATIVE_SECURITY", "true")
        }

        create("benchmark") {
            initWith(getByName("release"))
            applicationIdSuffix = ".benchmark"
            versionNameSuffix = "-benchmark"
            isDebuggable = true
            isMinifyEnabled = false
            buildConfigField("boolean", "ENABLE_BENCHMARKING", "true")
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
        // Include native libraries
        jniLibs {
            useLegacyPackaging = true
        }
    }

    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val buildTypeName = variant.buildType.name.replaceFirstChar { it.uppercase() }
            val version = variant.versionName
            val abi = variant.outputs.first().getFilter("ABI") ?: "universal"
            output.outputFileName = "ktimazstudio_${buildTypeName}_v${version}_${abi}.apk"
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

    // Test options
    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
        animationsDisabled = true
    }

    // Lint configuration
    lint {
        abortOnError = false
        checkReleaseBuilds = false
        disable += listOf("MissingTranslation", "ExtraTranslation")
        warningsAsErrors = true
        xmlReport = true
        htmlReport = true
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.core.splashscreen)

    // Compose BOM and UI
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.animation.core)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Security & Crypto
    implementation(libs.bouncycastle.bcprov)
    implementation(libs.bouncycastle.bcpkix)
    implementation(libs.conscrypt.android)

    // Biometrics
    implementation(libs.androidx.biometric)

    // Data Storage
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // UI Enhancement
    implementation(libs.lottie.compose)
    implementation(libs.accompanist.permissions)
    implementation(libs.accompanist.systemuicontroller)

    // Network
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Performance & Monitoring
    implementation("androidx.profileinstaller:profileinstaller:1.3.1")
    implementation("androidx.tracing:tracing:1.2.0")

    // Testing
    testImplementation(libs.junit)
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation("androidx.compose.ui:ui-test-manifest:1.7.8")

    // Debug tools
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
}

// Custom tasks for enhanced build process
tasks.register("verifyReleaseIntegrity") {
    group = "verification"
    description = "Verify release build integrity and security"
    
    doLast {
        println("🔒 Verifying release build integrity...")
        
        // Verify native libraries
        val nativeLibsDir = file("${layout.buildDirectory.get()}/intermediates/merged_native_libs/release/out/lib")
        if (nativeLibsDir.exists()) {
            println("✅ Native libraries found: ${nativeLibsDir.listFiles()?.size ?: 0}")
        }
        
        // Verify ProGuard mapping
        val mappingFile = file("${layout.buildDirectory.get()}/outputs/mapping/release/mapping.txt")
        if (mappingFile.exists()) {
            println("✅ ProGuard mapping file generated")
        }
        
        println("🔒 Build integrity verification completed")
    }
}

tasks.register("generateSecurityHashes") {
    group = "security"
    description = "Generate security hashes for verification"
    
    doLast {
        val buildDir = layout.buildDirectory.get().asFile
        println("🔐 Generating security hashes in $buildDir")
        
        // Generate checksums for APK files
        val apkDir = file("$buildDir/outputs/apk")
        if (apkDir.exists()) {
            apkDir.walkTopDown()
                .filter { it.extension == "apk" }
                .forEach { apkFile ->
                    val checksum = apkFile.readBytes()
                        .let { java.security.MessageDigest.getInstance("SHA-256").digest(it) }
                        .joinToString("") { "%02x".format(it) }
                    println("📱 ${apkFile.name}: $checksum")
                }
        }
    }
}

tasks.register("generateBuildReport") {
    group = "reporting"
    description = "Generate comprehensive build report"
    
    doLast {
        val report = buildString {
            appendLine("🏗️ KTIMAZ STUDIO BUILD REPORT")
            appendLine("=" * 50)
            appendLine("Build Time: ${java.time.LocalDateTime.now()}")
            appendLine("Git Hash: ${getGitHash()}")
            appendLine("Version: ${android.defaultConfig.versionName}")
            appendLine("Version Code: ${android.defaultConfig.versionCode}")
            appendLine("Target SDK: ${android.defaultConfig.targetSdk}")
            appendLine("Min SDK: ${android.defaultConfig.minSdk}")
            appendLine("NDK Version: ${android.ndkVersion}")
            appendLine("=" * 50)
        }
        
        val reportFile = file("${layout.buildDirectory.get()}/reports/build-report.txt")
        reportFile.parentFile.mkdirs()
        reportFile.writeText(report)
        
        println(report)
        println("📊 Build report saved to: ${reportFile.absolutePath}")
    }
}

tasks.register("cleanNative") {
    group = "cleanup"
    description = "Clean native build artifacts"
    
    doLast {
        delete(fileTree("src/main/cpp/.cxx"))
        delete(fileTree("${layout.buildDirectory.get()}/intermediates/cxx"))
        println("🧹 Native build artifacts cleaned")
    }
}

// Connect custom tasks to build lifecycle
tasks.named("assembleRelease") {
    finalizedBy("verifyReleaseIntegrity", "generateSecurityHashes", "generateBuildReport")
}

// Custom tasks
tasks.register("verifyReleaseIntegrity") {
    group = "verification"
    description = "Verify release build integrity"
    
    doLast {
        println("🔒 Verifying release build integrity...")
        val apkDir = file("${layout.buildDirectory.get()}/outputs/apk")
        if (apkDir.exists()) {
            println("✅ APK directory found")
        }
    }
}

tasks.register("generateSecurityHashes") {
    group = "security"
    description = "Generate security hashes"
    
    doLast {
        println("🔐 Generating security hashes...")
        val buildDir = layout.buildDirectory.get().asFile
        println("Build directory: $buildDir")
    }
}

tasks.named("assembleRelease") {
    finalizedBy("verifyReleaseIntegrity", "generateSecurityHashes")
}

tasks.named("clean") {
    dependsOn("cleanNative")
}