plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
    id("skie-buildconfig")

    alias(libs.plugins.kotlin.plugin.serialization)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    api(projects.analytics.analyticsConfiguration)
    implementation(projects.util)
}

buildConfig {
    val pluginId: String by properties
    buildConfigField("String", "PLUGIN_ID", "\"$pluginId\"")
}
