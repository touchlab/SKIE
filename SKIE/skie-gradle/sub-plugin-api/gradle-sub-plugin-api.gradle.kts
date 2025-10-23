plugins {
    id("gradle.common")
    id("skie.publishable")
}

skiePublishing {
    name = "SKIE Gradle Sub Plugin API"
    description = "API for creating Sub-plugins for SKIE."
}

dependencies {
    api(projects.gradle.gradlePluginApi)
    api(projects.gradle.gradlePluginShimApi)
}
