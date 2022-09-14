package co.touchlab.swiftgen.gradle

import co.touchlab.swiftgen.BuildConfig
import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftlink.plugin.SwiftLinkPlugin
import co.touchlab.swiftlink.plugin.SwiftLinkSubplugin
import co.touchlab.swiftpack.plugin.SwiftPackPlugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import javax.inject.Inject
import org.gradle.api.artifacts.Configuration as PluginConfiguration

abstract class SwiftGenSubplugin @Inject constructor() : SwiftLinkSubplugin {

    override val compilerPluginId: String = BuildConfig.KOTLIN_PLUGIN_ID

    override fun apply(target: Project) {
        super.apply(target)

        registerPlugins(target)
        registerSwiftGenConfigTask(target)
        registerExtension(target)
    }

    private fun registerPlugins(target: Project) {
        target.apply<SwiftLinkPlugin>()
        target.apply<SwiftPackPlugin>()
    }

    private fun registerSwiftGenConfigTask(target: Project) {
        target.tasks.register<CreateSwiftGenConfigTask>(CreateSwiftGenConfigTask.name)
    }

    private fun registerExtension(target: Project) {
        target.extensions.create("swiftGen", SwiftGenExtension::class.java)
    }

    override fun configureDependencies(project: Project, pluginConfiguration: PluginConfiguration) {
        project.dependencies {
            pluginConfiguration("co.touchlab.swiftgen:api:${BuildConfig.KOTLIN_PLUGIN_VERSION}")
            pluginConfiguration("co.touchlab.swiftgen:configuration:${BuildConfig.KOTLIN_PLUGIN_VERSION}")
            pluginConfiguration(
                group = BuildConfig.KOTLIN_PLUGIN_GROUP,
                name = BuildConfig.KOTLIN_PLUGIN_NAME,
                version = BuildConfig.KOTLIN_PLUGIN_VERSION,
            )
        }
    }

    override fun getOptions(project: Project, framework: Framework): Provider<List<SubpluginOption>> {
        val task = project.tasks.withType<CreateSwiftGenConfigTask>().getByName(CreateSwiftGenConfigTask.name)

        framework.linkTaskProvider.configure {
            it.dependsOn(task)
        }

        return project.provider {
            listOf(task.getSubpluginOption())
        }
    }

    private fun CreateSwiftGenConfigTask.getSubpluginOption(): SubpluginOption {
        val configFilePath = this.configFile.absolutePath

        return SubpluginOption(Configuration.CliOptionKey, configFilePath)
    }
}
