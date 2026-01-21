import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

// Generate dynamic version code and name based on build timestamp
val buildTimestamp = System.currentTimeMillis()
val versionMajor = 1
val versionMinor = 0
val versionPatch = 0

// Version code format: combine major version with timestamp hours
// Example: 1 * 10000000 + (hours since 2020) gives unique int < Int.MaxValue
val baseYear = 2020
val currentDate = Date(buildTimestamp)
val yearsSince2020 = SimpleDateFormat("yyyy").format(currentDate).toInt() - baseYear
val dayOfYear = SimpleDateFormat("DDD").format(currentDate).toInt()
val hourOfDay = SimpleDateFormat("HH").format(currentDate).toInt()
val minuteOfHour = SimpleDateFormat("mm").format(currentDate).toInt()

// Format: vMajor(1) + year(2) + dayOfYear(3) + hour(2) + minute(2) = max 10 digits
// Example: 1_06_021_14_38 = 106021438 for version 1.0.0, year 2026, day 21 at 14:38
val generatedVersionCode = (versionMajor * 100000000) + (yearsSince2020 * 1000000) + (dayOfYear * 10000) + (hourOfDay * 100) + minuteOfHour

// Format: "1.0.0 (20260121-1438)"
val generatedVersionName = "$versionMajor.$versionMinor.$versionPatch (${SimpleDateFormat("yyyyMMdd-HHmm").format(currentDate)})"

android {
    namespace = "com.example.greenalert"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.greenalert"
        minSdk = 24
        targetSdk = 36
        versionCode = generatedVersionCode
        versionName = generatedVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        buildConfig = true
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    
    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    
    // Navigation
    implementation(libs.navigation.compose)
    
    // DataStore
    implementation(libs.datastore.preferences)
    
    // Play Services Location
    implementation(libs.play.services.location)
    
    // osmdroid (OpenStreetMap)
    implementation(libs.osmdroid.android)
    
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}