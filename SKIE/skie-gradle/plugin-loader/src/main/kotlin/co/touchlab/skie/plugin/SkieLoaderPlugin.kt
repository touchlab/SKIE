package co.touchlab.skie.plugin

import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle_plugin_loader.BuildConfig
import co.touchlab.skie.plugin.configuration.SkieExtension
import org.gradle.api.Named
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.plugin.GradlePluginApiVersion
import org.gradle.api.model.ObjectFactory
import org.gradle.configurationcache.extensions.serviceOf
import org.gradle.internal.classloader.HashingClassLoaderFactory
import org.gradle.internal.classpath.DefaultClassPath
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin

@Suppress("unused")
abstract class SkieLoaderPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // We need to register the extension here, so that Gradle knows the type of it in the build script.
        val skieExtension = with(SkieExtension) {
            project.createExtension()
        }

        project.afterEvaluate {
            val kotlinVersion = getValidKotlinVersion(skieExtension) ?: return@afterEvaluate

            loadSkieGradlePlugin(kotlinVersion)
        }
    }

    private fun Project.getValidKotlinVersion(skieExtension: SkieExtension): KotlinVersion? {
        val kotlinVersion = getKotlinVersionString()?.let(::KotlinVersion)

        val skipSupportedVersionsCheck = skieExtension.debug.skipSupportedVersionsCheck.get()
        if (skipSupportedVersionsCheck) {
            logger.error(
                """
                    Warning:
                    SKIE skips Kotlin compiler version compatibility check because 'skie.debug.skipSupportedVersionsCheck' is set to true.
                    Usage of this flag in production is highly discouraged as it may lead to non-obvious compiler errors caused by SKIE incompatibility with the given Kotlin compiler version.
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
            !kotlinVersion.isSupported && !skipSupportedVersionsCheck -> {
                """
                    SKIE ${BuildConfig.SKIE_VERSION} does not support Kotlin $kotlinVersion.
                    Supported versions are ${BuildConfig.SUPPORTED_KOTLIN_VERSIONS}.
                    Check if you have the most recent version of SKIE and if so, please wait for the SKIE developers to add support for this Kotlin version.
                    New Kotlin versions are usually supported within a few days after they are released.
                    Note that there are no plans for supporting early access versions like Beta, RC, etc.
                """.trimIndent()
            }
            else -> return kotlinVersion
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

    private fun Project.loadSkieGradlePlugin(kotlinVersion: KotlinVersion) {
        val gradleVersion = GradleVersion.current().version
        logger.info("Resolving SKIE gradle plugin for Kotlin plugin version $kotlinVersion and Gradle version $gradleVersion")

        KotlinCompilerVersion.registerIn(project.dependencies)
        KotlinCompilerVersion.registerIn(buildscript.dependencies)
        val skieGradleConfiguration = buildscript.configurations.detachedConfiguration(
            buildscript.dependencies.create(BuildConfig.SKIE_GRADLE_PLUGIN_DEPENDENCY),
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
                attribute(KotlinCompilerVersion.attribute, objects.named(kotlinVersion.value))
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

        val probablySkiePluginClass = skieGradleClassLoader.loadClass("co.touchlab.skie.plugin.SkieGradlePlugin")
        if (!Plugin::class.java.isAssignableFrom(probablySkiePluginClass)) {
            reportSkieLoaderError(
                """
                    Loaded class ${probablySkiePluginClass.name} does not implement ${Plugin::class.java.name}!
                    This is a bug in SKIE - please report it to the SKIE developers.
                """.trimIndent(),
            )
            return
        }

        @Suppress("UNCHECKED_CAST")
        val shimPlugin: Class<Plugin<Project>> = probablySkiePluginClass as Class<Plugin<Project>>
        plugins.apply(shimPlugin)
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

    private inline fun <reified T : Named> ObjectFactory.named(name: String): T =
        named(T::class.java, name)

    @JvmInline
    private value class KotlinVersion(val value: String) {

        val isSupported: Boolean
            get() = BuildConfig.SUPPORTED_KOTLIN_VERSIONS.contains(value)

        override fun toString(): String = value
    }
}
