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
    ":analytics",
    ":ir-inspector",
    ":skie:mac",
    ":skie:mac:framework",
    ":skie:mac:dependency",
    ":skie:mac:swift",
    ":skie:ios",
    ":skie:ios:framework",
    ":skie:ios:dependency",
    ":skie:ios:swift",
    ":pure-compiler",
    ":pure-compiler:dependency",
    ":pure-compiler:framework",
    ":pure-compiler:swift",
)
