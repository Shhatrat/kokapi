plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

val hostOs = System.getProperty("os.name")
val isMacOs = hostOs.startsWith("Mac")

kotlin {
    androidTarget { compilations.all { kotlinOptions { jvmTarget = "1.8" } } }
    jvm("desktop")
    if (isMacOs) {
        iosX64(); iosArm64(); iosSimulatorArm64()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":kokapi-core"))
                implementation(libs.ktor.client.mock)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}

android {
    namespace = "pl.kokapi.test"
    compileSdk = 35
    defaultConfig { minSdk = 24 }
}
