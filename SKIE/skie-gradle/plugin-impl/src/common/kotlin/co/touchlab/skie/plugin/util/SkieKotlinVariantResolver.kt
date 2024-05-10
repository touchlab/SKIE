package co.touchlab.skie.plugin.util

import co.touchlab.skie.gradle_plugin_impl.BuildConfig
import org.gradle.api.Project

object SkieKotlinVariantResolver {

    fun withSkieKotlinVersion(project: Project, action: (String) -> Unit) {
        project.withKotlinVersion { kotlinVersion ->
            project.getValidSkieKotlinVersion(kotlinVersion)?.let(action)
        }
    }

    private fun Project.withKotlinVersion(action: (String) -> Unit) {
        raiseVersionOverrideUsageWarningIfNeeded()

        project.getKotlinVersionString()?.let { kotlinVersion ->
            action(kotlinVersion)

            return
        }

        withKotlinVersionDelayed(action)
    }

    private fun Project.withKotlinVersionDelayed(action: (String) -> Unit) {
        var kotlinVersion: String? = null

        // Cannot be configureEach otherwise it could crash the project that uses SKIE
        plugins.all {
            if (kotlinVersion == null) {
                kotlinVersion = project.kotlinGradlePluginVersionFromPlugin()

                kotlinVersion?.let(action)
            }
        }

        afterEvaluate {
            if (kotlinVersion == null) {
                logger.error(
                    """
                        SKIE could not infer Kotlin plugin version.
                        Make sure you have Kotlin Multiplatform plugin applied in the same module as SKIE and that the plugin works - for example by calling the link task that produces the Obj-C framework.
                        If that is the case, then this problem is likely caused by a bug in SKIE - please report it to the SKIE developers.
                        You can try to workaround this issue by providing the Kotlin version manually via 'skie.kgpVersion' property in your gradle.properties.
                        """.trimIndent(),
                )
            }
        }
    }

    private fun Project.raiseVersionOverrideUsageWarningIfNeeded() {
        if (kotlinGradlePluginVersionOverride != null) {
            logger.error(
                """
                    Warning:
                    skie.kgpVersion is used to override automatic Kotlin version resolution for SKIE plugin.
                    Usage of this property in production is highly discouraged as it may lead to non-obvious compiler errors caused by SKIE incompatibility with the used Kotlin compiler version.
                    """.trimIndent(),
            )
        }
    }

    private fun Project.getValidSkieKotlinVersion(kotlinVersion: String): String? {
        val skieVersion = BuildConfig.KOTLIN_TO_SKIE_VERSION[kotlinVersion]

        if (skieVersion == null) {
            val supportedKotlinVersions = BuildConfig.KOTLIN_TO_SKIE_VERSION.keys.sorted()

            reportSkieLoaderError(
                """
                    SKIE ${BuildConfig.SKIE_VERSION} does not support Kotlin $kotlinVersion.
                    Supported versions are: ${supportedKotlinVersions}.
                    Check if you have the most recent version of SKIE and if so, please wait for the SKIE developers to add support for this Kotlin version.
                    New Kotlin versions are usually supported within a few days after they are released.
                    Note that there are no plans for supporting early access versions like Beta, RC, etc.
                    """.trimIndent(),
            )
        }

        return skieVersion
    }

    private fun Project.getKotlinVersionString(): String? =
        (project.kotlinGradlePluginVersionOverride ?: project.kotlinGradlePluginVersion ?: project.rootProject.kotlinGradlePluginVersion)

    private val Project.kotlinGradlePluginVersion: String?
        get() = kotlinGradlePluginVersionFromClasspathConfiguration() ?: kotlinGradlePluginVersionFromPlugin()

    private val Project.kotlinGradlePluginVersionOverride: String?
        get() = findProperty("skie.kgpVersion") as? String

    private fun Project.kotlinGradlePluginVersionFromClasspathConfiguration(): String? {
        val classpathConfiguration = buildscript.configurations.getByName("classpath")

        val artifact = classpathConfiguration.resolvedConfiguration.resolvedArtifacts.singleOrNull { artifact ->
            artifact.moduleVersion.id.let { it.group == "org.jetbrains.kotlin" && it.name == "kotlin-gradle-plugin" }
        }

        return artifact?.moduleVersion?.id?.version
    }

    private fun Project.kotlinGradlePluginVersionFromPlugin(): String? {
        try {
            val kotlinBasePluginClass = this@SkieKotlinVariantResolver.javaClass.classLoader.loadClass("org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin")

            val plugin = plugins.firstOrNull { kotlinBasePluginClass.isAssignableFrom(it.javaClass) } ?: return null

            return kotlinBasePluginClass.getDeclaredMethod("getPluginVersion").invoke(plugin) as? String
        } catch (e: Throwable) {
            logger.debug("SKIE could not determine the Kotlin Gradle plugin version directly from Kotlin Gradle plugin because: $e")

            return null
        }
    }
}
