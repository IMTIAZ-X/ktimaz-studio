import java.io.ByteArrayOutputStream

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.jetbrains.kotlin.android)
}

fun String.runCommand(): String {
  val parts = this.split(" ")
  val proc = Runtime.getRuntime().exec(parts.toTypedArray())
  return proc.inputStream.bufferedReader().readText().trim()
}

// Compute Git hashes:
val gitCommitHash: String = "git rev-parse HEAD".runCommand()
val shortCommitHash: String = "git rev-parse --short HEAD".runCommand()

android {
  namespace = "com.ktimazstudio"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.ktimazstudio"
    minSdk = 25
    targetSdk = 35
    versionCode = 1
    versionName = "1.0 - im$shortCommitHash"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }

    buildConfigField(
      "String",
      "VERSION_NAME_WITHOUT_FLAVOR",
      "\"v7.0.0-SNAPSHOT-$shortCommitHash\""
    )
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
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      // you can override debug settings here if needed
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

  packagingOptions {
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

  // Optional: Rename APK outputs
  applicationVariants.all {
    outputs.all {
      // Must cast to the internal API to change the filename
      val outputImpl = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
      val variantName = variant.name // "release" or "debug"
      outputImpl.outputFileName =
        "ktimazstudio_${variantName}_v${defaultConfig.versionName}.apk"
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

  // If you need the Material icon pack in Compose:
  implementation("androidx.compose.material:material-icons-core:1.5.15")
  implementation("androidx.compose.material:material-icons-extended:1.5.15")

  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)

  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
}
