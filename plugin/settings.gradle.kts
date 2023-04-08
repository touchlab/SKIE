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
                includeModule("co.touchlab.fork.swiftpoet", "swiftpoet")
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

includeBuild("../swiftpoet")

include(
    ":api",
    ":analytics:analytics-api",
    ":analytics:collector",
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
