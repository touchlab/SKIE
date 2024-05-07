package co.touchlab.skie.plugin.subplugin

import co.touchlab.skie.plugin.util.KotlinCompilerPluginOption
import co.touchlab.skie.plugin.util.SkieTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Provider

interface SkieSubplugin : Plugin<Project> {

    val compilerPluginId: String

    override fun apply(target: Project) {}

    fun getOptions(project: Project, target: SkieTarget): Provider<List<KotlinCompilerPluginOption>> =
        project.provider { emptyList() }

    fun configureDependencies(project: Project, pluginConfiguration: Configuration)
}
