plugins {
    id("skie.shim")
    // TODO: Remove
    id("skie.publishable")
}

skiePublishing {
    name = "SKIE Gradle Plugin Shim Implementation"
    description = "Implementation of the SKIE Gradle Plugin Shim API, used by the main plugin module to interact with Kotlin Gradle Plugin."
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                // All dependencies should be `compileOnly` and instead brought in by `gradle-plugin-impl` to minimize the amount of runtime-loaded artifacts.
                compileOnly(projects.gradle.gradlePluginShimApi)
                compileOnly(projects.common.configuration.configurationDeclaration)
                compileOnly(projects.gradle.gradlePluginUtil)
                compileOnly(projects.common.util)
            }
        }
    }
}
