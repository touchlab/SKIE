package co.touchlab.skie.plugin.dependencies

import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle_plugin.BuildConfig
import co.touchlab.skie.plugin.util.exclude
import co.touchlab.skie.plugin.util.named
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

internal object SkieCompilerPluginDependencyProvider {

    private const val configurationName = "skieCompilerPlugin"

    fun getOrCreateDependencyConfiguration(project: Project, kotlinToolingVersion: String): Configuration =
        project.configurations.findByName(configurationName)
            ?: createDependencyConfiguration(project, kotlinToolingVersion)

    private fun createDependencyConfiguration(project: Project, kotlinToolingVersion: String): Configuration {
        val skieCompilerPluginConfiguration = project.configurations.create(configurationName) {
            isCanBeConsumed = false
            isCanBeResolved = true

            attributes {
                attribute(KotlinCompilerVersion.attribute, project.objects.named(kotlinToolingVersion))
            }

            exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-common")
            exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
            exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
            exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
            exclude(group = "org.jetbrains", module = "annotations")
        }

        project.dependencies.add(
            configurationName,
            mapOf(
                "group" to BuildConfig.KOTLIN_PLUGIN_GROUP,
                "name" to BuildConfig.KOTLIN_PLUGIN_NAME,
                "version" to BuildConfig.KOTLIN_PLUGIN_VERSION,
            ),
        )

        return skieCompilerPluginConfiguration
    }
}
