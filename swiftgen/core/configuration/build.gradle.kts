plugins {
    kotlin("jvm")
    `maven-publish`

    alias(libs.plugins.serialization)
    alias(libs.plugins.buildconfig)
}

dependencies {
    implementation(project(":api"))
    implementation(libs.serialization)
}

buildConfig {
    packageName(project.group.toString())

    val pluginId: String by properties
    buildConfigField("String", "PLUGIN_ID", "\"$pluginId\"")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}