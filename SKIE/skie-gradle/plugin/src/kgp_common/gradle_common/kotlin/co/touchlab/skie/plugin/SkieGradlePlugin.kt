package co.touchlab.skie.plugin

import co.touchlab.skie.plugin.analytics.GradleAnalyticsManager
import co.touchlab.skie.plugin.configuration.CreateSkieConfigurationTask
import co.touchlab.skie.plugin.configuration.skieExtension
import co.touchlab.skie.plugin.coroutines.addDependencyOnSkieRuntime
import co.touchlab.skie.plugin.coroutines.registerConfigureMinOsVersionTaskIfNeeded
import co.touchlab.skie.plugin.defaultarguments.disableCachingIfNeeded
import co.touchlab.skie.plugin.dependencies.SkieCompilerPluginDependencyProvider
import co.touchlab.skie.plugin.directory.SkieDirectoriesManager
import co.touchlab.skie.plugin.fatframework.FatFrameworkConfigurator
import co.touchlab.skie.plugin.subplugin.SkieSubPluginManager
import co.touchlab.skie.plugin.switflink.SwiftLinkingConfigurator
import co.touchlab.skie.plugin.util.*
import co.touchlab.skie.util.plugin.SkiePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinNativeArtifact
import org.jetbrains.kotlin.gradle.targets.native.tasks.artifact.kotlinArtifactsExtension
import org.jetbrains.kotlin.konan.target.HostManager

abstract class SkieGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.create("skieInternal", SkieInternalExtension::class.java)

        project.configureSkieGradlePlugin()

        project.afterEvaluate {
            project.configureRuntimeVariantFallback()
            project.configureSkieCompilerPlugin()
        }
    }

    private fun Project.configureSkieGradlePlugin() {
        SkieSubPluginManager.configureDependenciesForSubPlugins(project)
    }

    private fun Project.configureRuntimeVariantFallback() {
        if (!skieInternal.runtimeVariantFallback.isPresent) {
            val extraPropertiesKey = "skieRuntimeVariantFallback"
            skieInternal.runtimeVariantFallback.set(
                project.properties[extraPropertiesKey]?.toString().toBoolean()
            )
        }
    }

    private fun Project.configureSkieCompilerPlugin() {
        if (!isSkieEnabled) {
            return
        }

        warnOnEmptyFrameworks()

        FatFrameworkConfigurator.configureSkieForFatFrameworks(project)

        kotlinMultiplatformExtension?.appleTargets?.all {
            val target = this
            binaries.all {
                val binary = this
                skieInternal.targets.add(
                    SkieTarget.TargetBinary(
                        project = project,
                        target = target,
                        binary = binary,
                    )
                )
            }
        }

        kotlinArtifactsExtension.artifacts.withType<KotlinNativeArtifact>().all {
            skieInternal.targets.addAll(skieTargetsOf(this))
        }

        skieInternal.targets.all {
            configureSkie()
        }
    }

    private fun SkieTarget.configureSkie() {
        SkieDirectoriesManager.configureCreateSkieBuildDirectoryTask(this)

        GradleAnalyticsManager(project).configureAnalytics(this)

        registerConfigureMinOsVersionTaskIfNeeded()

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
            SkiePlugin.Options.skieDirectories.subpluginOption(skieDirectories.get()),
        )

        addToCompilerClasspath(
            SkieCompilerPluginDependencyProvider.getOrCreateDependencyConfiguration(project)
        )
    }
}

internal fun Project.warnOnEmptyFrameworks() {
    gradle.taskGraph.whenReady {
        if (skieInternal.targets.isEmpty()) {
            logger.warn("w: No Apple frameworks configured in module ${this@warnOnEmptyFrameworks.path}. Make sure you applied SKIE plugin in the correct module.")
        }
    }
}

private val Project.isSkieEnabled: Boolean
    get() = project.skieExtension.isEnabled.get() && HostManager.hostIsMac

internal val Project.kotlinMultiplatformExtension: KotlinMultiplatformExtension?
    get() = project.extensions.findByType(KotlinMultiplatformExtension::class.java)

internal val Project.skieInternal: SkieInternalExtension
    get() = project.extensions.getByType(SkieInternalExtension::class.java)
