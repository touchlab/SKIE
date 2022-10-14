pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "plugin"

include(":api")
include(":spi")
include(":gradle-plugin")
include(":kotlin-plugin")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
