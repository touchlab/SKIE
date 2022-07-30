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

rootProject.name = "SwiftPack"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":example-consumer")
project(":example-consumer").projectDir = rootDir.resolve("example/consumer")
include(":example-producer")
project(":example-producer").projectDir = rootDir.resolve("example/producer")
include(":example-nonproducer")
project(":example-nonproducer").projectDir = rootDir.resolve("example/nonproducer")
include(":example-producer:ksp-plugin")
include(":example-producer:simple-plugin")

includeBuild("swiftpack-plugin")
