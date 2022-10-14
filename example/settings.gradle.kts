pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        mavenLocal()
    }
}

include(":app", ":shared")
rootProject.name = "example"

enableFeaturePreview("VERSION_CATALOGS")

includeBuild("../plugin")
