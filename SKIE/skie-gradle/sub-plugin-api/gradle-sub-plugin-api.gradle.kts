plugins {
    id("skie.gradle")
    id("skie.publishable")
}

skiePublishing {
    name = "SKIE Gradle Sub Plugin API"
    description = "API for creating Sub-plugins for SKIE."
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.gradle.gradlePluginApi)
                api(projects.gradle.gradlePluginShimApi)
            }
        }
    }
}
