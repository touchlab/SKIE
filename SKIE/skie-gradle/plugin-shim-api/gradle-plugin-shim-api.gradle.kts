plugins {
    id("gradle.common")
    id("utility.skie-publishable")
}

skiePublishing {
    name = "SKIE Gradle Plugin Shim API"
    description = "API that's implemented by the SKIE Gradle Plugin Shim Impl, used by the main plugin module to interact with Kotlin Gradle Plugin."
}

dependencies {
    api(projects.gradle.gradlePluginApi)

    implementation(projects.common.util)
    implementation(projects.gradle.gradlePluginUtil)
}
