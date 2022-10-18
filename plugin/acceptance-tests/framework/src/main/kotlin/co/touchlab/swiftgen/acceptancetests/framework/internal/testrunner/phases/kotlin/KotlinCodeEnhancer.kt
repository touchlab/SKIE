package co.touchlab.swiftgen.acceptancetests.framework.internal.testrunner.phases.kotlin

import co.touchlab.skie.BuildConfig
import java.io.File
import java.nio.file.Path

internal class KotlinCodeEnhancer {

    fun enhance(kotlinFiles: List<Path>): List<Path> {
        val apiFiles = File(BuildConfig.SWIFT_GEN_API)
            .walkTopDown()
            .filter { it.extension == "kt" }
            .map { it.toPath() }

        return kotlinFiles + apiFiles
    }
}
