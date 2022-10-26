rootProject.name = "dev-support"

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

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

includeBuild("../plugin")

include(
    ":ir-inspector",
    ":playground",
    ":playground:kotlin:library",
    ":playground:kotlin:framework",
    ":playground:swift",
)
