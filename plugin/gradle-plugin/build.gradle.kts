import co.touchlab.skie.gradle.publish.mavenArtifactId

plugins {
    id("skie-jvm")
    id("java-gradle-plugin")
    id("skie-publish-gradle")
    id("skie-buildconfig")
}

buildConfig {
    val kotlinPlugin = project(":kotlin-plugin")
    buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "\"${kotlinPlugin.group}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_NAME", "\"${kotlinPlugin.mavenArtifactId}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "\"${kotlinPlugin.version}\"")

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

configurations.configureEach {
    attributes {
        @Suppress("UnstableApiUsage")
        attribute(GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE, objects.named("7.3"))
    }
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
