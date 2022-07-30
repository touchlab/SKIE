pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = ("swiftpack-plugin")

include(":api")
include(":spec")
include(":plugin-api")
include(":spi")
include(":config-plugin")
include(":gradle-plugin")
// include(":test-suite")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.children.forEach { child ->
    child.name = "swiftpack-${child.name}"
}