pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

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

rootProject.name = "plugin"

include(
    ":api",
    ":spi",
    ":configuration-api",
    ":linker",
    ":generator",
    ":generator:configuration-annotations",
    ":generator:configuration-gradle",
    ":interceptor",
    ":reflector",
    ":gradle-plugin",
    ":kotlin-plugin",
    ":kotlin-plugin:options",
    ":runtime:kotlin",
    ":runtime:swift",
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
