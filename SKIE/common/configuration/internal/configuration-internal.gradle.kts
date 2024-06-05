plugins {
    id("skie.common")
    id("skie.publishable")
}

skiePublishing {
    name = "SKIE Internal Configuration Declarations"
    description = "Internal Configuration declarations for SKIE, used in Gradle plugin."
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.common.configuration.configurationApi)
            }
        }
    }
}
