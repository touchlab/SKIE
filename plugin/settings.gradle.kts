pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    includeBuild("../build-setup")
}

dependencyResolutionManagement {
    repositories {
        maven("https://api.touchlab.dev/public") {
            content {
                includeModule("org.jetbrains.kotlin", "kotlin-native-compiler-embeddable")
            }
        }
        mavenCentral()
        google()
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
    ":analytics:analytics-api",
    ":analytics:analytics-configuration",
    ":analytics:producer",
    ":spi",
    ":configuration-api",
    ":linker",
    ":license",
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
    ":util",
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
