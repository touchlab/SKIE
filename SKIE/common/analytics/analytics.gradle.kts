plugins {
    id("skie.common")
    id("skie.publishable")

    kotlin("plugin.serialization")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.common.util)
                api(projects.common.configuration.configurationApi)
                api(projects.common.configuration.configurationDeclaration)
            }
        }
    }
}
