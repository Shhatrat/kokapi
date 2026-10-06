plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    androidTarget { compilations.all { kotlinOptions { jvmTarget = "1.8" } } }
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(project(":kokapi-core"))
                implementation("androidx.activity:activity-compose:1.9.2")
                implementation("androidx.compose.material3:material3:1.3.0")
                implementation("androidx.compose.ui:ui:1.7.3")
                implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
            }
        }
    }
}

android {
    namespace = "pl.kokapi.example.android"
    compileSdk = 35
    defaultConfig {
        applicationId = "pl.kokapi.example.android"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        // Set OKAPI_CONSUMER_KEY in local.properties or pass via -P flag
        buildConfigField("String", "CONSUMER_KEY", "\"${project.findProperty("okapiConsumerKey") ?: ""}\"")
        buildConfigField("String", "CONSUMER_SECRET", "\"${project.findProperty("okapiConsumerSecret") ?: ""}\"")
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
}
