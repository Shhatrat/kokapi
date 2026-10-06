rootProject.name = "kokapi"

include(":kokapi-core")
include(":kokapi-test")
include(":example-app:shared")
include(":example-app:androidApp")
include(":example-app:desktopApp")
include(":example-app:webApp")

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
