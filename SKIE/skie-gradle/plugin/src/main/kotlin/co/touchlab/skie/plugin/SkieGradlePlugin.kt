package co.touchlab.skie.plugin

import co.touchlab.skie.plugin.analytics.GradleAnalyticsManager
import co.touchlab.skie.plugin.configuration.CreateSkieConfigurationTask
import co.touchlab.skie.plugin.configuration.SkieExtension
import co.touchlab.skie.plugin.coroutines.addDependencyOnSkieRuntime
import co.touchlab.skie.plugin.coroutines.configureMinOsVersionIfNeeded
import co.touchlab.skie.plugin.defaultarguments.disableCachingIfNeeded
import co.touchlab.skie.plugin.dependencies.SkieCompilerPluginDependencyProvider
import co.touchlab.skie.plugin.directory.SkieDirectoriesManager
import co.touchlab.skie.plugin.fatframework.FatFrameworkConfigurator
import co.touchlab.skie.plugin.subplugin.SkieSubPluginManager
import co.touchlab.skie.plugin.switflink.SwiftLinkingConfigurator
import co.touchlab.skie.plugin.util.toKotlinCompilerPluginOption
import co.touchlab.skie.util.plugin.SkiePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class SkieGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // We need to register the extensions here, so that Gradle knows the type of it in the build script.
        SkieExtension.createExtension(project)
        SkieInternalExtension.createExtension(project) ?: return

        project.configureSkieGradlePlugin()

        project.afterEvaluate {
            project.configureSkieCompilerPlugin()
        }
    }

    private fun Project.configureSkieGradlePlugin() {
        SkieSubPluginManager.configureDependenciesForSubPlugins(project)
    }

    private fun Project.configureSkieCompilerPlugin() {
        if (!skieInternalExtension.isSkieEnabled) {
            return
        }

        warnOnEmptyFrameworks()

        FatFrameworkConfigurator.configureSkieForFatFrameworks(project)

        skieInternalExtension.kgpShim.initializeSkieTargets()

        skieInternalExtension.targets.configureEach {
            configureSkie()
        }
    }

    private fun SkieTarget.configureSkie() {
        SkieDirectoriesManager.configureCreateSkieBuildDirectoryTask(this)

        GradleAnalyticsManager(project).configureAnalytics(this)

        configureMinOsVersionIfNeeded()

        CreateSkieConfigurationTask.registerTask(this)

        SwiftLinkingConfigurator.configureCustomSwiftLinking(this)

        disableCachingIfNeeded()

        addDependencyOnSkieRuntime()

        SkieSubPluginManager.registerSubPlugins(this)

        configureKotlinCompiler()
    }

    private fun SkieTarget.configureKotlinCompiler() {
        addPluginArgument(
            SkiePlugin.id,
            SkiePlugin.Options.skieDirectories.toKotlinCompilerPluginOption(skieDirectories.get()),
        )

        addToCompilerClasspath(
            SkieCompilerPluginDependencyProvider.getOrCreateDependencyConfiguration(project),
        )
    }

    private fun Project.warnOnEmptyFrameworks() {
        gradle.taskGraph.whenReady {
            if (skieInternalExtension.targets.isEmpty()) {
                logger.warn("w: No Apple frameworks configured in module ${this@warnOnEmptyFrameworks.path}. Make sure you applied SKIE plugin in the correct module.")
            }
        }
    }
}
