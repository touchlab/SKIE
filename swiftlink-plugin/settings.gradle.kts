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

rootProject.name = "swiftlink-plugin"

include(":plugin")
include(":plugin-spi")
include(":gradle-plugin")
include(":test-suite")
