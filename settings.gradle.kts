rootProject.name = "SKIE"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }

    includeBuild("build-setup")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

includeBuild("example")
includeBuild("plugin")
includeBuild("dev-support")

include(
    ":acceptance-tests:framework",
    ":acceptance-tests",
)
