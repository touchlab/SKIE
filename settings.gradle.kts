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

rootProject.name = "swikt"

include(":example")
include(":example:static")
include(":example:dynamic")

includeBuild("plugin-build")
