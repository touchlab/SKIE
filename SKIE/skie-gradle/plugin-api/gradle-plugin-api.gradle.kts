plugins {
    id("skie.gradle")
    id("skie.publishable")
}

skiePublishing {
    name = "SKIE Gradle Plugin API"
    description = "API that's implemented by the SKIE Gradle Plugin, used by the loader module."
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.common.configuration.configurationDeclaration)
                implementation(projects.common.configuration.configurationApi)
            }
        }
    }
}
