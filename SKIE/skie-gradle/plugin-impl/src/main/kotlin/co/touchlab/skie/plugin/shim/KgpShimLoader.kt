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
        return createShimInstance(kotlinVersion)
    }

    private fun Project.createShimInstance(kotlinVersion: String): KgpShim? {
//         val childClassLoader = createClassLoaderForShimClass(shimImplJar)

        val shimClass = loadShimClass(this@KgpShimLoader::class.java.classLoader, kotlinVersion) ?: return null

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

    private fun Project.loadShimClass(classLoader: ClassLoader, kotlinVersion: String): Class<out KgpShim>? {
        val shimClass = classLoader.loadClass("co.touchlab.skie.plugin.shim.impl_${kotlinVersion.replace('.', '_')}.ActualKgpShim")

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
