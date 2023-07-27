plugins {
    id("skie.common")
    id("skie.publishable")
}

skiePublishing {
    name = "SKIE Configuration Declaration"
    description = "Configuration declarations for SKIE, used in Gradle plugin."
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.common.configuration.configurationApi)
                implementation(projects.common.configuration.configurationAnnotations)
            }
        }
    }
}
