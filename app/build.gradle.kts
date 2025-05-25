plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)

   // Existing plugins
   alias(libs.plugins.compose.compiler)

}

android {
    namespace = "com.ktimazstudio"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ktimazstudio"
        minSdk = 25
        targetSdk = 35
        versionCode = 500
        versionName = "3.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
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

            val shortCommitHash = "git rev-parse --short HEAD".runCommand() ?: "dev"
            versionNameSuffix = "-alpha-$shortCommitHash"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_22
        targetCompatibility = JavaVersion.VERSION_22
    }

    kotlin {
        jvmToolchain(22)
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
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
}

androidComponents {
    onVariants { variant ->
        if (variant.buildType == "release") {
            variant.packaging.resources.excludes.addAll(
                listOf(
                    "kotlin/**",
                    "kotlin-tooling-metadata.json",
                    "assets/dexopt/**",
                    "META-INF/LICENSE",
                    "META-INF/DEPENDENCIES",
                    "META-INF/*.kotlin_module",
                    "**/DebugProbesKt.bin",
                    "okhttp3/internal/publicsuffix/NOTICE",
                    "okhttp3/**",
                    "/META-INF/{AL2.0,LGPL2.1}"
                )
            )
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
    implementation("androidx.compose.material:material-icons-core:1.7.8")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.animation:animation-core")

    // Optional
   // implementation("androidx.navigation:navigation-compose:2.7.7")
   // implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
   // implementation("androidx.datastore:datastore-preferences:1.1.1")
   // implementation("androidx.work:work-runtime-ktx:2.9.0")
   // implementation("androidx.startup:startup-runtime:1.1.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
