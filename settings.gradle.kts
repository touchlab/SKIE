rootProject.name = "SwiftGen"

include(
    ":acceptance-tests:framework",
    ":acceptance-tests",
    ":playground:irinspector",
    ":playground:kotlin",
    ":playground:swift",
)

includeBuild("core")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://api.touchlab.dev/public")
    }

    includeBuild("gradle-src")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://api.touchlab.dev/public")
    }
}
