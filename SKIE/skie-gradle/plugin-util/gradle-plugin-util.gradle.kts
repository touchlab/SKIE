plugins {
    id("skie.gradle")
    id("skie.publishable")
}

skiePublishing {
    name = "SKIE Gradle Plugin utils"
    description = "Internal utilities for SKIE Gradle plugin."
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
            }
        }
    }
}
