import co.touchlab.skie.gradle.publish.dependencyName

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
                compileOnly(projects.common.configuration.configurationDeclaration)

                implementation(projects.common.analytics)
                implementation(projects.common.util)
            }
        }
    }
}

buildConfig {
    val kotlinPlugin = projects.compiler.kotlinPlugin.dependencyProject
    // TODO Rename to SKIE_GRADLE_PLUGIN
    buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "\"${kotlinPlugin.group}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_NAME", "\"${kotlinPlugin.name}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "\"${kotlinPlugin.version}\"")

    val runtime = project.provider { projects.runtime.runtimeKotlin.dependencyProject.dependencyName }
    buildConfigField("String", "RUNTIME_DEPENDENCY", runtime.map { """"$it"""" })

    val pluginId: String by properties
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"$pluginId\"")
}
