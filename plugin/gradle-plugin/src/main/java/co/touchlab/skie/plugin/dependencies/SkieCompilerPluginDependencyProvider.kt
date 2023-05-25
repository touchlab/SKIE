package co.touchlab.skie.plugin.dependencies

import co.touchlab.skie.gradle_plugin.BuildConfig
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.exclude

internal object SkieCompilerPluginDependencyProvider {

    private const val configurationName = "skieCompilerPlugin"

    fun getOrCreateDependencyConfiguration(project: Project): Configuration =
        project.configurations.findByName(configurationName)
            ?: createDependencyConfiguration(project)

    private fun createDependencyConfiguration(project: Project): Configuration {
        val skieCompilerPluginConfiguration = project.configurations.create(configurationName) {
            it.isCanBeConsumed = false
            it.isCanBeResolved = true

            it.exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")
        }

        project.dependencies {
            skieCompilerPluginConfiguration(
                group = BuildConfig.KOTLIN_PLUGIN_GROUP,
                name = BuildConfig.KOTLIN_PLUGIN_NAME,
                version = BuildConfig.KOTLIN_PLUGIN_VERSION,
            )
        }

        return skieCompilerPluginConfiguration
    }
}
