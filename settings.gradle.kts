pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "kotlin-gradle-plugin-template"

include(":example-static")
project(":example-static").projectDir = File("example/static")
include(":example-dynamic")
project(":example-dynamic").projectDir = File("example/dynamic")
includeBuild("plugin-build")
