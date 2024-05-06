package co.touchlab.skie.plugin

import co.touchlab.skie.gradle_plugin.BuildConfig
import co.touchlab.skie.plugin.analytics.GradleAnalyticsManager
import co.touchlab.skie.plugin.configuration.CreateSkieConfigurationTask
import co.touchlab.skie.plugin.configuration.SkieExtension
import co.touchlab.skie.plugin.configuration.skieExtension
import co.touchlab.skie.plugin.coroutines.addDependencyOnSkieRuntime
import co.touchlab.skie.plugin.coroutines.configureMinOsVersionIfNeeded
import co.touchlab.skie.plugin.defaultarguments.disableCachingIfNeeded
import co.touchlab.skie.plugin.dependencies.SkieCompilerPluginDependencyProvider
import co.touchlab.skie.plugin.directory.SkieDirectoriesManager
import co.touchlab.skie.plugin.fatframework.FatFrameworkConfigurator
import co.touchlab.skie.plugin.shim.KgpShimLoader
import co.touchlab.skie.plugin.subplugin.SkieSubPluginManager
import co.touchlab.skie.plugin.switflink.SwiftLinkingConfigurator
import co.touchlab.skie.plugin.util.SkieTarget
import co.touchlab.skie.plugin.util.appleTargets
import co.touchlab.skie.plugin.util.reportSkieLoaderError
import co.touchlab.skie.plugin.util.skieTargetsOf
import co.touchlab.skie.plugin.util.subpluginOption
import co.touchlab.skie.plugin.util.withType
import co.touchlab.skie.util.plugin.SkiePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinNativeArtifact
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.targets.native.tasks.artifact.kotlinArtifactsExtension
import org.jetbrains.kotlin.konan.target.HostManager

abstract class SkieGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // We need to register the extension here, so that Gradle knows the type of it in the build script.
        with(SkieExtension) {
            project.createExtension()
        }

        val kotlinVersion = project.getValidKotlinVersion() ?: return

        val internalExtension = project.extensions.create("skieInternal", SkieInternalExtension::class.java)

        internalExtension.kgpShim = KgpShimLoader.load(kotlinVersion, project) ?: return

        project.configureSkieGradlePlugin()

        project.afterEvaluate {
            project.configureSkieCompilerPlugin(kotlinVersion)
        }
    }

    private fun Project.getValidKotlinVersion(): String? {
        val kotlinVersion = getKotlinVersionString()
        val kgpVersion = kotlinVersion?.let { BuildConfig.KOTLIN_TO_SKIE_KGP_VERSION[it] }

        if (kotlinGradlePluginVersionOverride != null) {
            logger.error(
                """
                    Warning:
                    skie.kgpVersion is used to override automatic Kotlin version resolution for SKIE plugin.
                    Usage of this property in production is highly discouraged as it may lead to non-obvious compiler errors caused by SKIE incompatibility with the used Kotlin compiler version.
                """.trimIndent(),
            )
        }

        val error = when {
            kotlinVersion == null -> {
                """
                    SKIE could not infer Kotlin plugin version.
                    Make sure you have Kotlin Multiplatform plugin applied in the same module as SKIE and that the plugin works - for example by calling the link task that produces the Obj-C framework.
                    If that is the case, then this problem is likely caused by a bug in SKIE - please report it to the SKIE developers.
                    You can try to workaround this issue by providing the Kotlin version manually via 'skie.kgpVersion' property in your gradle.properties.
                """.trimIndent()
            }
            kgpVersion == null -> {
                val supportedKotlinVersions = BuildConfig.KOTLIN_TO_SKIE_KGP_VERSION.keys.sorted()

                """
                    SKIE ${BuildConfig.SKIE_VERSION} does not support Kotlin $kotlinVersion.
                    Supported versions are: ${supportedKotlinVersions}.
                    Check if you have the most recent version of SKIE and if so, please wait for the SKIE developers to add support for this Kotlin version.
                    New Kotlin versions are usually supported within a few days after they are released.
                    Note that there are no plans for supporting early access versions like Beta, RC, etc.
                """.trimIndent()
            }
            else -> return kgpVersion
        }

        reportSkieLoaderError(error)

        return null
    }

    private fun Project.getKotlinVersionString(): String? =
        (project.kotlinGradlePluginVersionOverride ?: project.kotlinGradlePluginVersion ?: project.rootProject.kotlinGradlePluginVersion)

    private val Project.kotlinGradlePluginVersion: String?
        get() = kotlinGradlePluginVersionFromPlugin() ?: kotlinGradlePluginVersionFromClasspathConfiguration()

    private val Project.kotlinGradlePluginVersionOverride: String?
        get() = findProperty("skie.kgpVersion") as? String

    private fun Project.kotlinGradlePluginVersionFromPlugin(): String? {
        return try {
            plugins.filterIsInstance<KotlinBasePlugin>().firstOrNull()?.pluginVersion
        } catch (e: NoClassDefFoundError) {
            // This happens when kotlin-gradle-plugin-api is not on classpath. SKIE loader doesn't add it to make sure we don't lock it to a specific version.
            null
        } catch (e: ClassNotFoundException) {
            // We'll probably never get here, but we want to be sure not to crash when we can't find the KotlinBasePlugin class.
            null
        }
    }

    private fun Project.kotlinGradlePluginVersionFromClasspathConfiguration(): String? {
        val classpathConfiguration = buildscript.configurations.getByName("classpath")
        val artifact = classpathConfiguration.resolvedConfiguration.resolvedArtifacts.singleOrNull { artifact ->
            artifact.moduleVersion.id.let { it.group == "org.jetbrains.kotlin" && it.name == "kotlin-gradle-plugin" }
        }
        return artifact?.moduleVersion?.id?.version
    }

    private fun Project.configureSkieGradlePlugin() {
        SkieSubPluginManager.configureDependenciesForSubPlugins(project)
    }

    private fun Project.configureSkieCompilerPlugin(kotlinToolingVersion: String) {
        if (!isSkieEnabled) {
            return
        }

        warnOnEmptyFrameworks()

        FatFrameworkConfigurator.configureSkieForFatFrameworks(project)

        kotlinMultiplatformExtension?.appleTargets?.configureEach {
            val target = this

            binaries.withType<Framework>().configureEach {
                val binary = this

                skieInternalExtension.targets.add(
                    SkieTarget.TargetBinary(
                        project = project,
                        target = target,
                        binary = binary,
                        outputKind = SkieTarget.OutputKind.Framework,
                    ),
                )
            }
        }

        kotlinArtifactsExtension.artifacts.withType<KotlinNativeArtifact>().configureEach {
            skieInternalExtension.targets.addAll(skieTargetsOf(this))
        }

        skieInternalExtension.targets.configureEach {
            configureSkie(kotlinToolingVersion)
        }
    }

    private fun SkieTarget.configureSkie(kotlinToolingVersion: String) {
        SkieDirectoriesManager.configureCreateSkieBuildDirectoryTask(this)

        GradleAnalyticsManager(project).configureAnalytics(this)

        configureMinOsVersionIfNeeded()

        CreateSkieConfigurationTask.registerTask(this)

        SwiftLinkingConfigurator.configureCustomSwiftLinking(this)

        disableCachingIfNeeded()

        addDependencyOnSkieRuntime(kotlinToolingVersion)

        SkieSubPluginManager.registerSubPlugins(this)

        configureKotlinCompiler(kotlinToolingVersion)
    }

    private fun SkieTarget.configureKotlinCompiler(kotlinToolingVersion: String) {
        addPluginArgument(
            SkiePlugin.id,
            SkiePlugin.Options.skieDirectories.subpluginOption(skieDirectories.get()),
        )

        addToCompilerClasspath(
            SkieCompilerPluginDependencyProvider.getOrCreateDependencyConfiguration(project, kotlinToolingVersion),
        )
    }
}

internal fun Project.warnOnEmptyFrameworks() {
    gradle.taskGraph.whenReady {
        if (skieInternalExtension.targets.isEmpty()) {
            logger.warn("w: No Apple frameworks configured in module ${this@warnOnEmptyFrameworks.path}. Make sure you applied SKIE plugin in the correct module.")
        }
    }
}

private val Project.isSkieEnabled: Boolean
    get() = project.skieExtension.isEnabled.get() && HostManager.hostIsMac

internal val Project.kotlinMultiplatformExtension: KotlinMultiplatformExtension?
    get() = project.extensions.findByType(KotlinMultiplatformExtension::class.java)
