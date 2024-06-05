plugins {
    id("skie.gradle")
    id("skie.publishable")
}

skiePublishing {
    name = "SKIE Gradle Plugin API"
    description = "Public API for SKIE Gradle plugin."
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.common.configuration.configurationDeclaration)
                implementation(projects.common.configuration.configurationInternal)
            }
        }
    }
}
