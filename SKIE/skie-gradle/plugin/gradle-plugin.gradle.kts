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
                // TODO: It might be worthwhile to make this compile-time safe, so we don't have to manually check. Or at least a test?
                // Whichever dependency is brought in by `gradle-plugin-loader` has to be `compileOnly` as we don't want duplicate classes.
                compileOnly(projects.gradle.gradlePluginApi)
                compileOnly(projects.common.configuration)

                implementation(projects.common.analytics)
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
