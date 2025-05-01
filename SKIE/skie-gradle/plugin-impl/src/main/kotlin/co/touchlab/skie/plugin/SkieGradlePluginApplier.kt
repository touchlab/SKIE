package co.touchlab.skie.plugin

import co.touchlab.skie.plugin.analytics.GradleAnalyticsManager
import co.touchlab.skie.plugin.configuration.CreateSkieConfigurationTask
import co.touchlab.skie.plugin.configuration.SkieExtension
import co.touchlab.skie.plugin.configuration.createExtension
import co.touchlab.skie.plugin.coroutines.addDependencyOnSkieRuntime
import co.touchlab.skie.plugin.coroutines.configureMinOsVersionIfNeeded
import co.touchlab.skie.plugin.coroutines.configureSkieConfigurationAnnotationsDependencySubstitution
import co.touchlab.skie.plugin.coroutines.configureSkieRuntimeDependencySubstitution
import co.touchlab.skie.plugin.defaultarguments.disableCachingIfNeeded
import co.touchlab.skie.plugin.dependencies.SkieCompilerPluginDependencyProvider
import co.touchlab.skie.plugin.directory.SkieDirectoriesManager
import co.touchlab.skie.plugin.fatframework.FatFrameworkConfigurator
import co.touchlab.skie.plugin.relativepaths.configureDebugPrefixMap
import co.touchlab.skie.plugin.subplugin.SkieSubPluginManager
import co.touchlab.skie.plugin.switflink.SwiftBundlingConfigurator
import co.touchlab.skie.plugin.switflink.SwiftUnpackingConfigurator
import co.touchlab.skie.plugin.util.toKotlinCompilerPluginOption
import co.touchlab.skie.util.plugin.SkiePlugin
import org.gradle.api.Project

object SkieGradlePluginApplier {

    fun apply(project: Project) {
        // We need to register the extensions here, so that Gradle knows the type of it in the build script.
        SkieExtension.createExtension(project)

        SkieInternalExtension.withExtension(project) {
            project.configureSkieGradlePlugin()

            project.afterEvaluate {
                project.configureSkieCompilerPlugin()
            }
        }
    }

    private fun Project.configureSkieGradlePlugin() {
        SkieSubPluginManager.configureDependenciesForSubPlugins(project)

        configureSkieConfigurationAnnotationsDependencySubstitution()
        configureSkieRuntimeDependencySubstitution()
    }

    private fun Project.configureSkieCompilerPlugin() {
        if (!skieInternalExtension.isSkieEnabled) {
            return
        }

        kgpShim.initializeShim()

        warnOnEmptyFrameworks()

        FatFrameworkConfigurator.configureSkieForFatFrameworks(project)

        SwiftBundlingConfigurator.configureCustomSwiftBundling(this)

        skieInternalExtension.targets.configureEach {
            configureSkie()
        }
    }

    private fun SkieTarget.configureSkie() {
        SkieDirectoriesManager.configureCreateSkieBuildDirectoryTask(this)

        GradleAnalyticsManager(project).configureAnalytics(this)

        configureMinOsVersionIfNeeded()
        configureDebugPrefixMap()

        CreateSkieConfigurationTask.registerTask(this)

        disableCachingIfNeeded()

        addDependencyOnSkieRuntime()

        SwiftUnpackingConfigurator.configureCustomSwiftUnpacking(this)

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
                logger.warn(
                    "w: No Apple frameworks configured in module ${this@warnOnEmptyFrameworks.path}. Make sure you applied SKIE plugin in the correct module.",
                )
            }
        }
    }
}
