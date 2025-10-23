plugins {
    id("gradle.common")
    id("utility.skie-publishable")
}

skiePublishing {
    name = "SKIE Gradle Plugin API"
    description = "Public API for SKIE Gradle plugin."
}

dependencies {
    api(projects.common.configuration.configurationDeclaration)
    implementation(projects.common.configuration.configurationInternal)
}
