rootProject.name = "SwiftGen-plugin"

include(
    ":api",
    ":configuration",
    ":compiler-plugin",
    ":gradle-plugin",
)

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    includeBuild("../gradle-src")
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
