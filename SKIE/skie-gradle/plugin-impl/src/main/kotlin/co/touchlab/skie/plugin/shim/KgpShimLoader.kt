@file:Suppress("UnstableApiUsage")

package co.touchlab.skie.plugin.shim

import org.gradle.api.Project
import org.gradle.util.GradleVersion

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
        val shimClass = loadShimClass(this@KgpShimLoader::class.java.classLoader, kotlinVersion) ?: return null

        return shimClass.getConstructor(Project::class.java).newInstance(project) as KgpShim
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
