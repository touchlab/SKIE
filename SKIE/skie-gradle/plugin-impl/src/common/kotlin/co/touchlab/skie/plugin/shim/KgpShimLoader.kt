@file:Suppress("UnstableApiUsage")

package co.touchlab.skie.plugin.shim

import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle_plugin_impl.BuildConfig
import co.touchlab.skie.plugin.util.exclude
import co.touchlab.skie.plugin.util.named
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.plugin.GradlePluginApiVersion
import org.gradle.configurationcache.extensions.serviceOf
import org.gradle.internal.classloader.HashingClassLoaderFactory
import org.gradle.internal.classpath.DefaultClassPath
import org.gradle.util.GradleVersion
import java.io.File

object KgpShimLoader {

    fun load(kotlinVersion: String, project: Project): KgpShim? {
        val gradleVersion = GradleVersion.current().version

        project.logger.info("Resolving SKIE gradle plugin for Kotlin plugin version $kotlinVersion and Gradle version $gradleVersion")

        return project.loadKgpShim(kotlinVersion)
    }

    private fun Project.loadKgpShim(kotlinVersion: String): KgpShim? {
        registerKotlinCompilerVersionAttribute(kotlinVersion)

        val skieGradleConfiguration = createSkieGradleShimImplConfiguration(kotlinVersion)

        val shimImplJar = skieGradleConfiguration.resolveOrRethrowFailure()

        return createShimInstance(shimImplJar)
    }

    private fun Project.registerKotlinCompilerVersionAttribute(kotlinVersion: String) {
        KotlinCompilerVersion.registerIn(project.dependencies, kotlinVersion)
        KotlinCompilerVersion.registerIn(buildscript.dependencies, kotlinVersion)
    }

    private fun Project.createSkieGradleShimImplConfiguration(kotlinVersion: String): Configuration =
        buildscript.configurations.detachedConfiguration(
            buildscript.dependencies.create(BuildConfig.SKIE_GRADLE_SHIM_IMPL_COORDINATE),
        ).apply {
            this.isCanBeConsumed = false
            this.isCanBeResolved = true

            exclude(group = "org.jetbrains.kotlin")

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

    private fun Configuration.resolveOrRethrowFailure(): Set<File> {
        this.resolvedConfiguration.rethrowFailure()

        return this.resolve()
    }

    private fun Project.createShimInstance(shimImplJar: Set<File>): KgpShim? {
        val childClassLoader = createClassLoaderForShimClass(shimImplJar)

        val shimClass = loadShimClass(childClassLoader) ?: return null

        return shimClass.getConstructor(Project::class.java).newInstance(project) as KgpShim
    }

    private fun Project.createClassLoaderForShimClass(shimImplJar: Set<File>): ClassLoader {
        val classLoaderFactory = serviceOf<HashingClassLoaderFactory>()

        return classLoaderFactory.createChildClassLoader(
            "skieKgpShimClassLoader",
            buildscript.classLoader,
            DefaultClassPath.of(shimImplJar),
            null,
        )
    }

    private fun Project.loadShimClass(classLoader: ClassLoader): Class<out KgpShim>? {
        val shimClass = classLoader.loadClass("co.touchlab.skie.plugin.shim.ActualKgpShim")

        if (!KgpShim::class.java.isAssignableFrom(shimClass)) {
            reportSkieLoaderError(
                """
                        Loaded class ${shimClass.name} does not implement ${KgpShim::class.java.name}!
                        This is a bug in SKIE - please report it to the SKIE developers.
                    """.trimIndent(),
            )

            return null
        }

        @Suppress("UNCHECKED_CAST")
        return shimClass as Class<out KgpShim>
    }
}
