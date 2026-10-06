plugins {
    alias(libs.plugins.android.application)
    kotlin("android")
    alias(libs.plugins.compose.compiler)
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
        buildConfigField("String", "CONSUMER_KEY", "\"${project.findProperty("okapiConsumerKey") ?: ""}\"")
        buildConfigField("String", "CONSUMER_SECRET", "\"${project.findProperty("okapiConsumerSecret") ?: ""}\"")
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":example-app:shared"))
    implementation(project(":kokapi-core"))
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation(libs.androidx.security.crypto)
}
