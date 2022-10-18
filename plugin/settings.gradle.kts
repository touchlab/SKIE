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
    ":generator:core",
    ":generator:configuration-annotations",
    ":generator:configuration-gradle",
    ":interceptor",
    ":reflector",
    ":gradle-plugin",
    ":kotlin-plugin",
    ":kotlin-plugin:options",

    ":acceptance-tests:framework",
    ":acceptance-tests",
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
