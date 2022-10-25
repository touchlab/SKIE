plugins {
    kotlin("jvm")
    `maven-publish`

    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.buildconfig)
}

dependencies {
    implementation(project(":api"))
    implementation(libs.kotlinx.serialization.json)
}

buildConfig {
    packageName(("${project.group}.${project.name}").replace("-", "_"))

    val pluginId: String by properties
    buildConfigField("String", "PLUGIN_ID", "\"$pluginId\"")
}
