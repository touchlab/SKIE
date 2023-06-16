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
import org.gradle.util.GradleVersion
import java.net.URLClassLoader

@Suppress("unused")
abstract class SkieLoaderPlugin: Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        // We need to register the extension here, so that Gradle knows the type of it in the build script.
        with(SkieExtension) {
            createExtension()
        }

        val rootClasspathConfiguration = rootProject.buildscript.configurations.getByName("classpath")
        val kotlinDependency = rootClasspathConfiguration.resolvedConfiguration.resolvedArtifacts.singleOrNull { artifact ->
            artifact.moduleVersion.id.let { it.group == "org.jetbrains.kotlin" && it.name == "kotlin-gradle-plugin" }
        }

        if (kotlinDependency == null) {
            log.error("Couldn't find Kotlin plugin version. Make sure you have Kotlin plugin applied.")
            return
        }
        val gradleVersion = GradleVersion.current().version
        val kotlinVersion = kotlinDependency.moduleVersion.id.version
        log.info("Resolving SKIE gradle plugin for Kotlin plugin version $kotlinVersion and Gradle version $gradleVersion")

        KotlinCompilerVersion.registerIn(project.dependencies)
        val skieGradleConfiguration = project.configurations.detachedConfiguration(
            project.dependencies.module(BuildConfig.SKIE_GRADLE_PLUGIN_DEPENDENCY)
        ).apply {
            this.isCanBeConsumed = false
            this.isCanBeResolved = true

            attributes {
                attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category.LIBRARY))
                attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage.JAVA_RUNTIME))
                attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements.JAR))
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

        val skieGradleClassLoader = URLClassLoader(
            "skieGradleClassLoader",
            skieGradleConfiguration.resolve().map { it.toURI().toURL() }.toTypedArray(),
            buildscript.classLoader,
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

    private inline fun <reified T : Named> ObjectFactory.named(name: String): T =
        named(T::class.java, name)

    companion object {
        val log = Logging.getLogger(SkieLoaderPlugin::class.java)
    }
}
