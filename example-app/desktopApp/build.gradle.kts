plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm("desktop") {
        compilations.all { kotlinOptions { jvmTarget = "17" } }
        mainRun { mainClass = "pl.kokapi.example.desktop.MainKt" }
    }
    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(project(":kokapi-core"))
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.ktor.client.cio)
            }
        }
    }
}
