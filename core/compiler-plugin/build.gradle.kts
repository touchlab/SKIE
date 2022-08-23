plugins {
    kotlin("jvm")
    `maven-publish`

    alias(libs.plugins.buildconfig)
}

buildConfig {
    packageName(project.group.toString())

    val pluginId: String by properties
    buildConfigField("String", "PLUGIN_ID", "\"$pluginId\"")
}

dependencies {
    compileOnly(kotlin("compiler-embeddable"))
    implementation(libs.swiftpack.api)
    implementation(project(":api"))
    implementation(project(":configuration"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}