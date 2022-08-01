rootProject.name = "SwiftGen-plugin"

include(
    ":compiler-plugin",
    ":gradle-plugin",
)

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://api.touchlab.dev/public")
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}