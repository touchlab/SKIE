rootProject.name = "dev-support"

pluginManagement {
    includeBuild("../build-setup")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
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
