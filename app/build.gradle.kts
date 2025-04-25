import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

fun String.runCommand(): String? {
    return try {
        val output = ByteArrayOutputStream()
        exec {
            commandLine = this@runCommand.split(" ")
            standardOutput = output
        }
        output.toString().trim()
    } catch (e: Exception) {
        null
    }
}

fun getGitTag(): String = "git describe --tags".runCommand() ?: "1.0.0"
fun getGitCommitCount(): Int = "git rev-list --count HEAD".runCommand()?.toInt() ?: 1

android {
    namespace = "com.ktimazstudio"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ktimazstudio"
        minSdk = 25
        targetSdk = 34
        versionCode = getGitCommitCount()
        versionName = getGitTag()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    signingConfigs {
        create("release") {
            storeFile = file("Antik.keystore")
            storePassword = System.getenv("Antik123")
            keyAlias = System.getenv("Antikmods")
            keyPassword = System.getenv("Antik123")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
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

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
