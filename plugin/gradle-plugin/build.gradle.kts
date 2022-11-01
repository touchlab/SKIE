plugins {
    id("skie-jvm")
    id("java-gradle-plugin")
    id("skie-publish-gradle")
    id("skie-buildconfig")
}

buildConfig {
    buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "\"${project.group}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "\"${project.version}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_NAME", "\"${project(":kotlin-plugin").name}\"")

    val pluginId: String by properties
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"$pluginId\"")
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())
    compileOnly(libs.plugin.kotlin.gradle)
    compileOnly(libs.plugin.kotlin.gradle.api)

    implementation(projects.configurationApi)
    implementation(projects.kotlinPlugin)
    implementation(projects.kotlinPlugin.options)

    testImplementation(gradleKotlinDsl())
    testImplementation(libs.plugin.kotlin.gradle)
}

gradlePlugin {
    plugins {
        create("co.touchlab.skie") {
            id = "co.touchlab.skie"
            displayName = "Swift and Kotlin, unified"
            implementationClass = "co.touchlab.skie.plugin.SwiftLinkPlugin"
            version = project.version
        }
    }
}

// Configuration Block for the Plugin Marker artifact on Plugin Central
pluginBundle {
    website = "https://github.com/touchlab/SKIE"
    vcsUrl = "https://github.com/touchlab/SKIE.git"
    description = "A Gradle plugin to add Swift into Kotlin/Native framework."
    tags = listOf(
        "plugin",
        "gradle",
        "swift",
        "kotlin",
        "native",
    )
}
