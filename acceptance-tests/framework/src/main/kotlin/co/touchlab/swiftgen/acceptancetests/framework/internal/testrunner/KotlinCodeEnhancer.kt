package co.touchlab.swiftgen.acceptancetests.framework.internal.testrunner

import co.touchlab.swiftgen.BuildConfig
import co.touchlab.swiftgen.acceptancetests.framework.TempFileSystem
import co.touchlab.swiftgen.acceptancetests.framework.TestResult
import java.io.File
import java.nio.file.Path
import kotlin.io.path.writeText

internal class KotlinCodeEnhancer {

    fun enhance(kotlinFiles: List<Path>): List<Path> {
        val apiFiles = File(BuildConfig.SWIFT_GEN_API)
            .walkTopDown()
            .filter { it.extension == "kt" }
            .map { it.toPath() }

        return kotlinFiles + apiFiles
    }
}