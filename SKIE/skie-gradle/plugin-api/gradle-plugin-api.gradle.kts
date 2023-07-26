plugins {
    id("skie.gradle")
    id("skie.publishable")
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
