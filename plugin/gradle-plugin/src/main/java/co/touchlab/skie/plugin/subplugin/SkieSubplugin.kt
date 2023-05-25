package co.touchlab.skie.plugin.subplugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework

interface SkieSubplugin : Plugin<Project> {

    val compilerPluginId: String

    override fun apply(target: Project) {}

    fun getOptions(project: Project, framework: Framework): Provider<List<SubpluginOption>> =
        project.provider { emptyList() }

    fun configureDependencies(project: Project, pluginConfiguration: Configuration)
}
