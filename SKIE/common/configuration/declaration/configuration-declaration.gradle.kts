plugins {
    id("skie.common")
    id("skie.publishable")
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
