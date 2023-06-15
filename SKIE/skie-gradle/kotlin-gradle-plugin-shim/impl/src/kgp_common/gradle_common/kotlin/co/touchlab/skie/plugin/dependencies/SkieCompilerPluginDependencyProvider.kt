package co.touchlab.skie.plugin.dependencies

import co.touchlab.skie.kotlin_gradle_plugin_shim_impl.BuildConfig
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

internal object SkieCompilerPluginDependencyProvider {

    private const val configurationName = "skieCompilerPlugin"

    fun getOrCreateDependencyConfiguration(project: Project): Configuration =
        project.configurations.findByName(configurationName)
            ?: createDependencyConfiguration(project)

    private fun createDependencyConfiguration(project: Project): Configuration {
        val skieCompilerPluginConfiguration = project.configurations.create(configurationName) {
            isCanBeConsumed = false
            isCanBeResolved = true

            exclude(
                mapOf(
                    "group" to "org.jetbrains.kotlin",
                    "module" to "kotlin-stdlib-common",
                )
            )
        }

        project.dependencies.add(configurationName, mapOf(
            "group" to BuildConfig.KOTLIN_PLUGIN_GROUP,
            "name" to BuildConfig.KOTLIN_PLUGIN_NAME,
            "version" to BuildConfig.KOTLIN_PLUGIN_VERSION,
        ))

        return skieCompilerPluginConfiguration
    }
}
