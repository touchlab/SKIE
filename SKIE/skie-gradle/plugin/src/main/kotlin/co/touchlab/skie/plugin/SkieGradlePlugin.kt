package co.touchlab.skie.plugin

import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle_plugin.BuildConfig
import co.touchlab.skie.plugin.analytics.GradleAnalyticsManager
import co.touchlab.skie.plugin.configuration.CreateSkieConfigurationTask
import co.touchlab.skie.plugin.configuration.SkieExtension
import co.touchlab.skie.plugin.configuration.SkieExtension.Companion.createExtension
import co.touchlab.skie.plugin.configuration.skieExtension
import co.touchlab.skie.plugin.coroutines.addDependencyOnSkieRuntime
import co.touchlab.skie.plugin.coroutines.configureMinOsVersionIfNeeded
import co.touchlab.skie.plugin.defaultarguments.disableCachingIfNeeded
import co.touchlab.skie.plugin.dependencies.SkieCompilerPluginDependencyProvider
import co.touchlab.skie.plugin.directory.SkieDirectoriesManager
import co.touchlab.skie.plugin.fatframework.FatFrameworkConfigurator
import co.touchlab.skie.plugin.shim.ShimEntrypoint
import co.touchlab.skie.plugin.subplugin.SkieSubPluginManager
import co.touchlab.skie.plugin.switflink.SwiftLinkingConfigurator
import co.touchlab.skie.plugin.util.*
import co.touchlab.skie.util.plugin.SkiePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.plugin.GradlePluginApiVersion
import org.gradle.configurationcache.extensions.serviceOf
import org.gradle.internal.classloader.HashingClassLoaderFactory
import org.gradle.internal.classpath.DefaultClassPath
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinNativeArtifact
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
        val shims = project.loadSkieGradlePluginShim(kotlinVersion) ?: return

        project.extensions.create("skieInternal", SkieInternalExtension::class.java)

        project.configureSkieGradlePlugin()

        project.afterEvaluate {
            project.configureRuntimeVariantFallback()
            project.configureSkieCompilerPlugin(shims, kotlinVersion)
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

    private fun Project.reportSkieLoaderError(error: String) {
        logger.error("Error:\n$error\nSKIE cannot not be used until this error is resolved.\n")

        gradle.taskGraph.whenReady {
            val hasLinkTask = allTasks.any { it.name.startsWith("link") && it.project == project }
            val isSkieEnabled = extensions.findByType(SkieExtension::class.java)?.isEnabled?.get() == true

            if (hasLinkTask && isSkieEnabled) {
                error("$error\nTo proceed with the compilation, please remove or explicitly disable SKIE by adding 'skie { isEnabled.set(false) }' to your Gradle configuration.")
            }
        }
    }

    private fun Project.getKotlinVersionString(): String? =
        (project.kotlinGradlePluginVersionOverride ?: project.kotlinGradlePluginVersion ?: project.rootProject.kotlinGradlePluginVersion)

    private fun Project.loadSkieGradlePluginShim(kotlinVersion: String): ShimEntrypoint? {
        val gradleVersion = GradleVersion.current().version
        logger.info("Resolving SKIE gradle plugin for Kotlin plugin version $kotlinVersion and Gradle version $gradleVersion")

        KotlinCompilerVersion.registerIn(project.dependencies, kotlinVersion)
        KotlinCompilerVersion.registerIn(buildscript.dependencies, kotlinVersion)
        val skieGradleConfiguration = buildscript.configurations.detachedConfiguration(
            buildscript.dependencies.create(BuildConfig.SKIE_GRADLE_PLUGIN),
        ).apply {
            this.isCanBeConsumed = false
            this.isCanBeResolved = true

            exclude(
                mapOf(
                    "group" to "org.jetbrains.kotlin",
                ),
            )

            attributes {
                attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
                attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
                attribute(KotlinCompilerVersion.attribute, objects.named(kotlinVersion))
                if (GradleVersion.current() >= GradleVersion.version("7.0")) {
                    attribute(
                        GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE,
                        objects.named(GradleVersion.current().version),
                    )
                }
            }
        }

        skieGradleConfiguration.resolvedConfiguration.rethrowFailure()

        val classLoaderFactory = serviceOf<HashingClassLoaderFactory>()
        val skieGradleClassLoader = classLoaderFactory.createChildClassLoader(
            "skieGradleClassLoader",
            buildscript.classLoader,
            DefaultClassPath.of(skieGradleConfiguration.resolve()),
            null,
        )

        val probablyShimEntrypointImplClass = skieGradleClassLoader.loadClass("co.touchlab.skie.plugin.shim.ShimEntrypointImpl")
        if (!ShimEntrypoint::class.java.isAssignableFrom(probablyShimEntrypointImplClass)) {
            reportSkieLoaderError(
                """
                    Loaded class ${probablyShimEntrypointImplClass.name} does not implement ${ShimEntrypoint::class.java.name}!
                    This is a bug in SKIE - please report it to the SKIE developers.
                """.trimIndent(),
            )
            return null
        }

        return probablyShimEntrypointImplClass.getConstructor().newInstance() as ShimEntrypoint
    }

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

    private fun Project.configureRuntimeVariantFallback() {
        if (!skieInternal.runtimeVariantFallback.isPresent) {
            val extraPropertiesKey = "skieRuntimeVariantFallback"
            skieInternal.runtimeVariantFallback.set(
                project.properties[extraPropertiesKey]?.toString().toBoolean()
            )
        }
    }

    private fun Project.configureSkieCompilerPlugin(shims: ShimEntrypoint, kotlinToolingVersion: String) {
        if (!isSkieEnabled) {
            return
        }

        warnOnEmptyFrameworks()

        FatFrameworkConfigurator.configureSkieForFatFrameworks(project)

        kotlinMultiplatformExtension?.appleTargets?.all {
            val target = this
            binaries.withType<Framework>().all {
                val binary = this
                skieInternal.targets.add(
                    SkieTarget.TargetBinary(
                        project = project,
                        target = target,
                        binary = binary,
                        outputKind = SkieTarget.OutputKind.Framework,
                    )
                )
            }
        }

        kotlinArtifactsExtension.artifacts.withType<KotlinNativeArtifact>().all {
            skieInternal.targets.addAll(skieTargetsOf(this))
        }

        skieInternal.targets.all {
            configureSkie(shims, kotlinToolingVersion)
        }
    }

    private fun SkieTarget.configureSkie(shims: ShimEntrypoint, kotlinToolingVersion: String) {
        SkieDirectoriesManager.configureCreateSkieBuildDirectoryTask(this)

        GradleAnalyticsManager(project).configureAnalytics(this)

        configureMinOsVersionIfNeeded(shims)

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
            SkieCompilerPluginDependencyProvider.getOrCreateDependencyConfiguration(project, kotlinToolingVersion)
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
