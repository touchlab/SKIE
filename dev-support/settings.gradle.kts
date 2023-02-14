rootProject.name = "dev-support"

pluginManagement {
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
    ":skie:mac",
    ":skie:mac:framework",
    ":skie:mac:library",
    ":skie:mac:swift",
    ":skie:ios",
    ":skie:ios:framework",
    ":skie:ios:library",
    ":pure-compiler",
    ":pure-compiler:library",
    ":pure-compiler:framework",
)
