pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
        maven("https://api.touchlab.dev/public")
    }
}

include(":shared")
rootProject.name = "KaMPKit"

enableFeaturePreview("VERSION_CATALOGS")
