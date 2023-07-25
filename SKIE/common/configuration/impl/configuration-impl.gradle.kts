plugins {
    id("skie.common")
    id("skie.publishable")
    id("dev.buildconfig")

    kotlin("plugin.serialization")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
                implementation(projects.common.util)
            }
        }
    }
}

buildConfig {
    val pluginId: String by properties
    buildConfigField("String", "PLUGIN_ID", "\"$pluginId\"")
}
