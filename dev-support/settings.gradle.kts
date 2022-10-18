rootProject.name = "dev-support"

include(
    ":ir-inspector",
    ":playground:kotlin",
    ":playground:swift",
)

includeBuild("../plugin")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://api.touchlab.dev/public")
    }

    includeBuild("../build-setup")
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
