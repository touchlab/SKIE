package co.touchlab.skie.plugin.shim

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

        project.findKotlinGradlePluginVersion()?.let { kotlinVersion ->
            action(kotlinVersion)

            return
        }

        withKotlinVersionDelayed(action)
    }

    private fun Project.withKotlinVersionDelayed(action: (String) -> Unit) {
        var kotlinVersion: String? = null

        plugins.whenPluginAdded {
            if (kotlinVersion == null) {
                kotlinVersion = project.findKotlinGradlePluginVersionFromKotlinBasePlugin()

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
        if (findKotlinGradlePluginVersionFromOverrideProperty() != null) {
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

    private fun Project.findKotlinGradlePluginVersion(): String? {
        logger.debug("[SKIE] Resolving KGP version for project '${project.path}'.")

        project.findKotlinGradlePluginVersionFromOverrideProperty()?.let { override ->
            return override
        }

        project.findKotlinGradlePluginVersionFromKotlinBasePlugin()?.let { projectKotlinVersion ->
            return projectKotlinVersion
        }

        logger.debug("[SKIE] Couldn't find KGP version for project '${project.path}'.")

        return null
    }

    private fun Project.findKotlinGradlePluginVersionFromOverrideProperty(): String? =
        (findProperty("skie.kgpVersion") as? String)?.also {
            logger.debug("[SKIE] Found KGP version override: $it in project '${project.path}', skipping resolution.")
        }

    private fun Project.findKotlinGradlePluginVersionFromKotlinBasePlugin(): String? {
        try {
            logger.debug("[SKIE] Resolving KGP version from KGP plugin for project '${project.path}'.")

            val kotlinBasePluginClass = this@SkieKotlinVariantResolver.javaClass.classLoader.loadClass("org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin")

            val plugin = plugins.firstOrNull { kotlinBasePluginClass.isAssignableFrom(it.javaClass) } ?: kotlin.run {
                logger.debug(
                    "[SKIE] Could not determine the KGP version in project '{}' from KGP plugin because none of the applied plugins {} are assignable from '{}'.",
                    project.path,
                    plugins.map { it::class.qualifiedName },
                    kotlinBasePluginClass.name,
                )

                return null
            }

            val version = kotlinBasePluginClass.getDeclaredMethod("getPluginVersion").invoke(plugin) as String

            logger.debug("[SKIE] Found KGP version $version from KGP plugin in project '${project.path}'.")

            return version
        } catch (e: Throwable) {
            logger.debug(
                "[SKIE] Could not determine the KGP version in project '{}' from KGP plugin because {}",
                project.path,
                e.stackTrace,
            )

            return null
        }
    }
}
