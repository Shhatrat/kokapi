plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm("desktop") {
        compilations.all { kotlinOptions { jvmTarget = "17" } }
        mainRun { mainClass = "pl.kokapi.example.desktop.MainKt" }
    }
    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(project(":example-app:shared"))
                implementation(project(":kokapi-core"))
                implementation(compose.desktop.currentOs)
                implementation(libs.ktor.client.cio)
            }
        }
    }
}
