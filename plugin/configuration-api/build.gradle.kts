plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
    id("skie-buildconfig")

    alias(libs.plugins.kotlin.plugin.serialization)
}

dependencies {
    implementation(project(":api"))
    implementation(libs.kotlinx.serialization.json)
}

buildConfig {
    val pluginId: String by properties
    buildConfigField("String", "PLUGIN_ID", "\"$pluginId\"")
}
