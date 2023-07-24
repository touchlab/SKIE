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
                implementation(projects.common.configuration.configurationImpl)
                implementation(libs.kotlinx.serialization.json)

                implementation(libs.apache.compress)
            }
        }
    }
}
