package co.touchlab.skie.plugin.dependencies

import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle_plugin_impl.BuildConfig
import co.touchlab.skie.plugin.skieInternalExtension
import co.touchlab.skie.plugin.util.exclude
import co.touchlab.skie.plugin.util.named
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

object SkieCompilerPluginDependencyProvider {

    private const val CONFIGURATION_NAME = "skieCompilerPlugin"

    fun getOrCreateDependencyConfiguration(project: Project): Configuration = project.configurations.findByName(CONFIGURATION_NAME)
        ?: createDependencyConfiguration(project)

    private fun createDependencyConfiguration(project: Project): Configuration {
        val skieCompilerPluginConfiguration = project.configurations.create(CONFIGURATION_NAME) {
            isCanBeConsumed = false
            isCanBeResolved = true

            attributes {
                attribute(KotlinCompilerVersion.attribute, project.objects.named(project.skieInternalExtension.kotlinVersion))
            }

            exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-common")
            exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
            exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
            exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
            exclude(group = "org.jetbrains", module = "annotations")
        }

        project.dependencies.add(CONFIGURATION_NAME, BuildConfig.SKIE_KOTLIN_PLUGIN_COORDINATE)

        return skieCompilerPluginConfiguration
    }
}
