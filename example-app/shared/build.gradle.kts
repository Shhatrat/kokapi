plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget {
        compilations.all { kotlinOptions { jvmTarget = "1.8" } }
    }
    jvm("desktop") {
        compilations.all { kotlinOptions { jvmTarget = "17" } }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":kokapi-core"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.ktor.client.cio)
            }
        }
    }
}

android {
    namespace = "pl.kokapi.example.shared"
    compileSdk = 35
    defaultConfig { minSdk = 24 }
}
