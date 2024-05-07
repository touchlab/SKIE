package co.touchlab.skie.plugin.util

import co.touchlab.skie.gradle_plugin.BuildConfig
import org.gradle.api.Project

object KotlinVersionResolver {

    fun resolve(project: Project): String? =
        project.getValidKotlinVersion()

    private fun Project.getValidKotlinVersion(): String? {
        val kotlinVersion = getKotlinVersionString()
        val skieVersion = kotlinVersion?.let { BuildConfig.KOTLIN_TO_SKIE_VERSION[it] }

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
            skieVersion == null -> {
                val supportedKotlinVersions = BuildConfig.KOTLIN_TO_SKIE_VERSION.keys.sorted()

                """
                    SKIE ${BuildConfig.SKIE_VERSION} does not support Kotlin $kotlinVersion.
                    Supported versions are: ${supportedKotlinVersions}.
                    Check if you have the most recent version of SKIE and if so, please wait for the SKIE developers to add support for this Kotlin version.
                    New Kotlin versions are usually supported within a few days after they are released.
                    Note that there are no plans for supporting early access versions like Beta, RC, etc.
                """.trimIndent()
            }
            else -> return skieVersion
        }

        reportSkieLoaderError(error)

        return null
    }

    private fun Project.getKotlinVersionString(): String? =
        (project.kotlinGradlePluginVersionOverride ?: project.kotlinGradlePluginVersion ?: project.rootProject.kotlinGradlePluginVersion)

    private val Project.kotlinGradlePluginVersion: String?
        get() = kotlinGradlePluginVersionFromClasspathConfiguration()

    private val Project.kotlinGradlePluginVersionOverride: String?
        get() = findProperty("skie.kgpVersion") as? String

    private fun Project.kotlinGradlePluginVersionFromClasspathConfiguration(): String? {
        val classpathConfiguration = buildscript.configurations.getByName("classpath")

        val artifact = classpathConfiguration.resolvedConfiguration.resolvedArtifacts.singleOrNull { artifact ->
            artifact.moduleVersion.id.let { it.group == "org.jetbrains.kotlin" && it.name == "kotlin-gradle-plugin" }
        }

        return artifact?.moduleVersion?.id?.version
    }
}
