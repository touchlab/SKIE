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
include(":config-plugin")
include(":gradle-plugin")
// include(":test-suite")

rootProject.children.forEach { child ->
    child.name = "swiftpack-${child.name}"
}