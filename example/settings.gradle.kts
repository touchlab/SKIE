pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        mavenLocal()
    }
}

include(":app", ":shared")
rootProject.name = "SkieDemo"

enableFeaturePreview("VERSION_CATALOGS")

includeBuild("../plugin")
includeBuild("../swiftpoet")
