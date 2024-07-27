plugins {
    id("skie.gradle")
    id("skie.publishable")
}

skiePublishing {
    name = "SKIE Gradle Plugin API"
    description = "Public API for SKIE Gradle plugin."
}

kotlin {
    sourceSets.commonMain {
        dependencies {
            api(projects.common.configuration.configurationDeclaration)
            implementation(projects.common.configuration.configurationInternal)
        }
    }
}
