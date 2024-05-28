package co.touchlab.skie.plugin.shim

import co.touchlab.skie.plugin.configuration.skieExtension
import org.gradle.api.Project

fun Project.reportSkieLoaderError(error: String) {
    afterEvaluate {
        // Intentionally not checking for the macOS platform
        val isSkieSupposedToBeEnabled = skieExtension.isEnabled.get()

        if (isSkieSupposedToBeEnabled) {
            val errorMessage = """
                    |Error: ${error.replace("\n", "\n|")}
                    |SKIE cannot not be used until this error is resolved.
                    |To proceed with the compilation, please remove or explicitly disable SKIE by adding 'skie { isEnabled = false }' to your Gradle configuration.
                """.trimMargin()

            error(errorMessage)
        }
    }
}
