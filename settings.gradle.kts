rootProject.name = "kokapi"

include(":kokapi-core")
include(":kokapi-test")
include(":example-app:androidApp")
include(":example-app:desktopApp")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
