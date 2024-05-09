package co.touchlab.skie.plugin.util

import co.touchlab.skie.plugin.configuration.SkieExtension
import org.gradle.api.Project

fun Project.reportSkieLoaderError(error: String) {
    logger.error("Error:\n$error\nSKIE cannot not be used until this error is resolved.\n")

    gradle.taskGraph.whenReady {
        val hasLinkTask = allTasks.any { it.name.startsWith("link") && it.project == project }
        val isSkieEnabled = extensions.findByType(SkieExtension::class.java)?.isEnabled?.get() == true

        if (hasLinkTask && isSkieEnabled) {
            error("$error\nTo proceed with the compilation, please remove or explicitly disable SKIE by adding 'skie { isEnabled.set(false) }' to your Gradle configuration.")
        }
    }
}
