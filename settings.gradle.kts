pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "swiftkt"

include(":example")
include(":example:static")
include(":example:dynamic")

includeBuild("swiftkt-plugin")
