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
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://api.touchlab.dev/public")
    }
}