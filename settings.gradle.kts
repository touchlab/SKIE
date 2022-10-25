pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        mavenLocal()
    }

    includeBuild("build-setup")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        mavenLocal()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "SKIE"

includeBuild("example")
includeBuild("plugin")
includeBuild("dev-support")

include(
    ":acceptance-tests:framework",
    ":acceptance-tests",
)
