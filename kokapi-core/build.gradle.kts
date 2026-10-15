import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.dokka)
    `maven-publish`
}

val hostOs = System.getProperty("os.name")
val isMacOs = hostOs.startsWith("Mac")

kotlin {
    androidTarget {
        compilations.all { kotlinOptions { jvmTarget = "1.8" } }
    }
    jvm("desktop") {
        compilations.all { kotlinOptions { jvmTarget = "11" } }
    }
    if (isMacOs) {
        iosX64()
        iosArm64()
        iosSimulatorArm64()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.ktor.client.mock)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(libs.androidx.security.crypto)
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }
        val wasmJsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }
        if (isMacOs) {
            val iosMain by creating {
                dependsOn(commonMain)
                dependencies {
                    implementation(libs.ktor.client.darwin)
                }
            }
            listOf(
                getByName("iosX64Main"),
                getByName("iosArm64Main"),
                getByName("iosSimulatorArm64Main")
            ).forEach { it.dependsOn(iosMain) }
        }
    }
}

android {
    namespace = "pl.kokapi"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Shhatrat/kokapi")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications.withType<MavenPublication> {
        pom {
            name.set("KoKAPI")
            description.set("Kotlin Multiplatform library for the Opencaching OKAPI REST API")
            url.set("https://github.com/Shhatrat/kokapi")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
        }
    }
}
