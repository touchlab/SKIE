plugins {
    id("skie.common")
    id("skie.publishable")
    id("dev.buildconfig")

    alias(libs.plugins.kotlin.plugin.serialization)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
                // api(projects.analytics.analyticsConfiguration)
                implementation(projects.common.util)
            }
        }
    }
}

buildConfig {
    val pluginId: String by properties
    buildConfigField("String", "PLUGIN_ID", "\"$pluginId\"")
}
