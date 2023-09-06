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
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.configurationcache.extensions.serviceOf
import org.gradle.internal.classloader.HashingClassLoaderFactory
import org.gradle.internal.classpath.DefaultClassPath
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin

@Suppress("unused")
abstract class SkieLoaderPlugin: Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        // We need to register the extension here, so that Gradle knows the type of it in the build script.
        with(SkieExtension) {
            createExtension()
        }

        val kotlinVersion = project.kotlinGradlePluginVersionOverride ?: project.kotlinGradlePluginVersion ?: rootProject.kotlinGradlePluginVersion

        if (kotlinVersion == null) {
            log.error("Couldn't find Kotlin plugin version. Make sure you have Kotlin plugin applied or set property 'skie.kgpVersion' in your gradle.properties.")
            return
        }
        val gradleVersion = GradleVersion.current().version
        log.info("Resolving SKIE gradle plugin for Kotlin plugin version $kotlinVersion and Gradle version $gradleVersion")

        KotlinCompilerVersion.registerIn(project.dependencies)
        KotlinCompilerVersion.registerIn(buildscript.dependencies)
        val skieGradleConfiguration = buildscript.configurations.detachedConfiguration(
            buildscript.dependencies.create(BuildConfig.SKIE_GRADLE_PLUGIN_DEPENDENCY)
        ).apply {
            this.isCanBeConsumed = false
            this.isCanBeResolved = true

            exclude(
                mapOf(
                    "group" to "org.jetbrains.kotlin"
                )
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
                        objects.named(GradleVersion.current().version)
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
            log.error("Loaded class ${probablySkiePluginClass.name} does not implement ${Plugin::class.java.name}! Please report this to the SKIE team.")
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

    companion object {
        val log = Logging.getLogger(SkieLoaderPlugin::class.java)
    }
}
