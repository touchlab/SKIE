import co.touchlab.skie.gradle.publish.dependencyName
import co.touchlab.skie.gradle.publish.mavenArtifactId

plugins {
    id("skie.shim")
    id("skie.publishable")

    id("dev.buildconfig")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                compileOnly(projects.gradle.kotlinGradlePluginShim)

                implementation(projects.common.analytics)
                implementation(projects.common.configuration)
                implementation(projects.common.license)
                implementation(projects.common.util)

                implementation(libs.kotlinx.serialization.json)
                implementation(libs.jgit)
            }
        }
    }
}



buildConfig {
    val kotlinPlugin = projects.compiler.kotlinPlugin.dependencyProject
    // TODO Rename to SKIE_GRADLE_PLUGIN
    buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "\"${kotlinPlugin.group}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_NAME", "\"${kotlinPlugin.mavenArtifactId}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "\"${kotlinPlugin.version}\"")

    val runtime = projects.runtime.runtimeKotlin.dependencyProject
    buildConfigField("String", "RUNTIME_DEPENDENCY", "\"${runtime.dependencyName}\"")

    val pluginId: String by properties
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"$pluginId\"")
}
