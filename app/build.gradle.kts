plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.ktimazstudio"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ktimazstudio"
        minSdk = 25
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

fun String.runCommand(): String? =
    try {
        ProcessBuilder(*split(" ").toTypedArray())
            .redirectErrorStream(true)
            .start()
            .inputStream
            .bufferedReader()
            .readText()
    } catch (e: Exception) {
        null
    }


    signingConfigs {
        create("release") {  
            storeFile = file(project.property("RELEASE_STORE_FILE") as String)
            storePassword = project.property("RELEASE_STORE_PASSWORD") as String
            keyAlias = project.property("RELEASE_KEY_ALIAS") as String
            keyPassword = project.property("RELEASE_KEY_PASSWORD") as String
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
val shortCommitHash = "git rev-parse --short HEAD".runCommand()?.trim() ?: "dev"
            versionName = "V3.0-Beta-$shortCommitHash"
        
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
    jvmToolchain(21)
}


    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
    resources {
        excludes += setOf(
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
            "/META-INF/{AL2.0,LGPL2.1}"
        )
    }
}

    applicationVariants.all {
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "ktimazstudio_release_v${defaultConfig.versionName}.apk"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation("androidx.compose.material:material-icons-core:1.7.8") // Or your Compose version
    implementation("androidx.compose.material:material-icons-extended:1.7.8") // Or your Compose version
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0") // Or your Compose BOM aligned version
   implementation("androidx.compose.animation:animation")
   implementation("androidx.compose.animation:animation-core") // This library contains AnticipateOvershootInterpolator

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
