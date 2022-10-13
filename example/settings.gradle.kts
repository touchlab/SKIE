pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
        maven("https://api.touchlab.dev/public")
    }
}

include(":app", ":shared")
rootProject.name = "KaMPKit"

enableFeaturePreview("VERSION_CATALOGS")
