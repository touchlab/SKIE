package co.touchlab.skie.plugin

import co.touchlab.skie.plugin.analytics.GradleAnalyticsManager
import co.touchlab.skie.plugin.configuration.CreateSkieConfigurationTask
import co.touchlab.skie.plugin.configuration.skieExtension
import co.touchlab.skie.plugin.coroutines.addDependencyOnSkieRuntime
import co.touchlab.skie.plugin.coroutines.registerConfigureMinOsVersionTaskIfNeeded
import co.touchlab.skie.plugin.defaultarguments.disableCachingIfNeeded
import co.touchlab.skie.plugin.dependencies.SkieCompilerPluginDependencyProvider
import co.touchlab.skie.plugin.directory.SkieDirectoriesManager
import co.touchlab.skie.plugin.directory.skieDirectories
import co.touchlab.skie.plugin.fatframework.FatFrameworkConfigurator
import co.touchlab.skie.plugin.subplugin.SkieSubPluginManager
import co.touchlab.skie.plugin.switflink.SwiftLinkingConfigurator
import co.touchlab.skie.plugin.util.appleTargets
import co.touchlab.skie.plugin.util.frameworks
import co.touchlab.skie.plugin.util.subpluginOption
import co.touchlab.skie.util.plugin.SkiePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import org.jetbrains.kotlin.konan.target.HostManager

abstract class SkieGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.configureSkieGradlePlugin()

        project.afterEvaluate {
            project.configureSkieCompilerPlugin()
        }
    }

    private fun Project.configureSkieGradlePlugin() {
        SkieSubPluginManager.configureDependenciesForSubPlugins(project)
    }

    private fun Project.configureSkieCompilerPlugin() {
        if (!isSkieEnabled) {
            return
        }

        warnOnEmptyFrameworks()

        FatFrameworkConfigurator.configureSkieForFatFrameworks(project)

        configureEachKotlinFrameworkLinkTask {
            configureSkieForLinkTask()
        }
    }

    private fun KotlinNativeLink.configureSkieForLinkTask() {
        SkieDirectoriesManager.configureCreateSkieBuildDirectoryTask(this)

        GradleAnalyticsManager(project).configureAnalytics(this)

        disableCachingIfNeeded()
        binary.target.addDependencyOnSkieRuntime()
        binary.registerConfigureMinOsVersionTaskIfNeeded()

        CreateSkieConfigurationTask.registerTask(this)

        SwiftLinkingConfigurator.configureCustomSwiftLinking(this)

        SkieSubPluginManager.registerSubPlugins(this)

        configureKotlinCompiler()
    }

    private fun KotlinNativeLink.configureKotlinCompiler() {
        compilerPluginClasspath = listOfNotNull(
            compilerPluginClasspath,
            SkieCompilerPluginDependencyProvider.getOrCreateDependencyConfiguration(project),
        ).reduce(FileCollection::plus)

        compilerPluginOptions.addPluginArgument(
            SkiePlugin.id,
            SkiePlugin.Options.skieDirectories.subpluginOption(skieDirectories),
        )
    }
}

internal fun Project.warnOnEmptyFrameworks() {
    val hasFrameworks = extensions.findByType(KotlinMultiplatformExtension::class.java)?.appleTargets?.any { it.frameworks.isNotEmpty() } ?: false
    if (!hasFrameworks) {
        logger.warn("w: No Apple frameworks configured. Make sure you applied SKIE plugin in the correct module.")
    }
}

internal fun Project.configureEachKotlinFrameworkLinkTask(
    configure: KotlinNativeLink.() -> Unit,
) {
    configureEachKotlinAppleTarget {
        frameworks.forEach { framework ->
            // Cannot use configure on linkTaskProvider because it's not possible to register new tasks in configure block of another task
            configure(framework.linkTask)
        }
    }
}

internal fun Project.configureEachKotlinAppleTarget(
    configure: KotlinNativeTarget.() -> Unit,
) {
    val kotlinExtension = extensions.findByType(KotlinMultiplatformExtension::class.java) ?: return

    kotlinExtension.appleTargets.forEach {
        configure(it)
    }
}

private val Project.isSkieEnabled: Boolean
    get() = project.skieExtension.isEnabled.get() && HostManager.hostIsMac
