plugins {
    id("gradle.common")
    // TODO: Remove
    id("skie.publishable")
}

skiePublishing {
    name = "SKIE Gradle Plugin utils"
    description = "Internal utilities for SKIE Gradle plugin."
}
