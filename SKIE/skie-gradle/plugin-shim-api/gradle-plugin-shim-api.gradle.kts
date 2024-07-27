plugins {
    id("skie.gradle")
    id("skie.publishable")
}

skiePublishing {
    name = "SKIE Gradle Plugin Shim API"
    description = "API that's implemented by the SKIE Gradle Plugin Shim Impl, used by the main plugin module to interact with Kotlin Gradle Plugin."
}

kotlin {
    sourceSets.commonMain {
        dependencies {
            api(projects.gradle.gradlePluginApi)

            implementation(projects.common.util)
            implementation(projects.gradle.gradlePluginUtil)
        }
    }
}
