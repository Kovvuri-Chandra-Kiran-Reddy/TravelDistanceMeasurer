import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.hilt.android)
  alias(libs.plugins.kotlin.kapt)
  alias(libs.plugins.sqldelight)
}

// Load local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
  localPropertiesFile.inputStream().use { localProperties.load(it) }
}

android {
  namespace = "com.example.traveldistancemeasurer"
  compileSdk {
    version = release(36)
  }

  defaultConfig {
    applicationId = "com.example.traveldistancemeasurer"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    // Add Google Maps API Key from local.properties
    val mapsApiKey = localProperties.getProperty("MAPS_API_KEY", "")
    manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions {
    jvmTarget = "11"
  }
  buildFeatures {
    compose = true
  }
}

sqldelight {
  databases {
    create("TravelDatabase") {
      packageName.set("com.example.traveldistancemeasurer.database")
    }
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)

  // Google Maps & Location
  implementation(libs.google.maps.compose)
  implementation(libs.play.services.maps)
  implementation(libs.play.services.location)

  // SQLDelight
  implementation(libs.sqldelight.android.driver)
  implementation(libs.sqldelight.coroutines)

  // Coroutines
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.play.services)

  // Navigation
  implementation(libs.androidx.navigation.compose)

  // ViewModel
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.lifecycle.runtime.compose)

  // Hilt
  implementation(libs.hilt.android)
  kapt(libs.hilt.compiler)
  implementation(libs.hilt.navigation.compose)

  // Testing
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.tooling)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
}
