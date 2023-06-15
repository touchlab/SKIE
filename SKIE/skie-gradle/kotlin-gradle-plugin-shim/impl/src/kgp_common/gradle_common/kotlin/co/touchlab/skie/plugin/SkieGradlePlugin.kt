package co.touchlab.skie.plugin

import co.touchlab.skie.plugin.analytics.GradleAnalyticsManager
import co.touchlab.skie.plugin.configuration.CreateSkieConfigurationTask
import co.touchlab.skie.plugin.configuration.SkieExtension
import co.touchlab.skie.plugin.configuration.skieExtension
import co.touchlab.skie.plugin.dependencies.SkieCompilerPluginDependencyProvider
import co.touchlab.skie.plugin.dependencies.addDependencyOnSkieRuntime
import co.touchlab.skie.plugin.fatframework.FatFrameworkConfigurator
import co.touchlab.skie.plugin.license.GradleSkieLicenseManager
import co.touchlab.skie.plugin.subplugin.SkieSubPluginManager
import co.touchlab.skie.plugin.switflink.SwiftLinkingConfigurator
import co.touchlab.skie.plugin.directory.SkieDirectoriesManager
import co.touchlab.skie.plugin.util.appleTargets
import co.touchlab.skie.plugin.util.frameworks
import co.touchlab.skie.plugin.directory.skieDirectories
import co.touchlab.skie.plugin.util.subpluginOption
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import org.jetbrains.kotlin.konan.target.presetName

abstract class SkieGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val licenseManager = GradleSkieLicenseManager(project)
        val analyticsManager = GradleAnalyticsManager(project, licenseManager)

        analyticsManager.withErrorLogging {
            project.configureSkieGradlePlugin(licenseManager, analyticsManager)
        }

        project.afterEvaluate {
            analyticsManager.withErrorLogging {
                project.configureSkieCompilerPlugin(licenseManager, analyticsManager)
            }
        }
    }

    private fun Project.configureSkieGradlePlugin(
        licenseManager: GradleSkieLicenseManager,
        analyticsManager: GradleAnalyticsManager,
    ) {
        licenseManager.initializeLicensing(analyticsManager)

        SkieExtension.createExtension(project)

        SkieSubPluginManager.configureDependenciesForSubPlugins(project)
    }

    private fun Project.configureSkieCompilerPlugin(
        licenseManager: GradleSkieLicenseManager,
        analyticsManager: GradleAnalyticsManager,
    ) {
        if (!isSkieEnabled) {
            return
        }

        FatFrameworkConfigurator.configureSkieForFatFrameworks(project)

        configureEachKotlinFrameworkLinkTask(analyticsManager) {
            configureSkieForLinkTask(licenseManager, analyticsManager)
        }
    }

    private fun KotlinNativeLink.configureSkieForLinkTask(
        licenseManager: GradleSkieLicenseManager,
        analyticsManager: GradleAnalyticsManager,
    ) {
        SkieDirectoriesManager.configureCreateSkieBuildDirectoryTask(this, analyticsManager)

        licenseManager.configureLicensing(this, analyticsManager)
        analyticsManager.configureAnalytics(this)

        binary.target.disableCaching()
        binary.target.addDependencyOnSkieRuntime()

        CreateSkieConfigurationTask.registerTask(this, analyticsManager)

        SwiftLinkingConfigurator.configureCustomSwiftLinking(this, analyticsManager)

        SkieSubPluginManager.registerSubPlugins(this)

        configureKotlinCompiler(analyticsManager)
    }

    private fun KotlinNativeLink.configureKotlinCompiler(
        analyticsManager: GradleAnalyticsManager,
    ) {
        compilerPluginClasspath = listOfNotNull(
            compilerPluginClasspath,
            SkieCompilerPluginDependencyProvider.getOrCreateDependencyConfiguration(project),
        ).reduce(FileCollection::plus)

        compilerPluginOptions.addPluginArgument(
            SkiePlugin.id,
            SkiePlugin.Options.buildId.subpluginOption(analyticsManager.buildId),
        )

        compilerPluginOptions.addPluginArgument(
            SkiePlugin.id,
            SkiePlugin.Options.skieDirectories.subpluginOption(skieDirectories),
        )
    }
}

internal fun Project.configureEachKotlinFrameworkLinkTask(
    analyticsManager: GradleAnalyticsManager,
    configure: KotlinNativeLink.() -> Unit,
) {
    configureEachKotlinAppleTarget {
        frameworks.forEach { framework ->
            analyticsManager.withErrorLogging {
                // Cannot use configure on linkTaskProvider because it's not possible to register new tasks in configure block of another task
                configure(framework.linkTask)
            }
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

private fun KotlinNativeTarget.disableCaching() {
    project.extensions.extraProperties.set("kotlin.native.cacheKind.${konanTarget.presetName}", "none")
}

private val Project.isSkieEnabled: Boolean
    get() = project.skieExtension.isEnabled.get()
