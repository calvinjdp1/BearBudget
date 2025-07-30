plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt") // Needed if you use Room or Hilt later
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.bearbudget"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.bearbudget"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // --- Compose BOM ---
    val composeBom = platform("androidx.compose:compose-bom:2024.04.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // --- Compose ---
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // --- Activity Compose ---
    implementation("androidx.activity:activity-compose:1.8.2")

    // --- Lifecycle ViewModel for Compose ---
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // --- Retrofit ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // --- Coroutines ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.material:material-icons-extended:1.5.1")
    // Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    implementation("androidx.compose.foundation:foundation:1.5.1")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.compose.ui:ui:1.5.1")
    implementation("androidx.compose.ui:ui-text:1.5.1")
    implementation("androidx.compose.ui:ui:1.5.1")
    implementation("androidx.compose.ui:ui-text:1.5.1")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.ui:ui-text:<latest-version>")
    implementation("androidx.compose.foundation:foundation:<latest-version>")
    implementation("androidx.compose.material3:material3:<latest-version>")


}
