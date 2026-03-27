package co.touchlab.skie.plugin.libraries

import co.touchlab.skie.acceptancetests.framework.ExpectedTestResult
import co.touchlab.skie.acceptancetests.framework.Test
import co.touchlab.skie.plugin.libraries.library.TestedLibrary
import java.nio.file.Path

data class ExternalLibraryTest(
    val library: TestedLibrary,
    override val expectedResult: ExpectedTestResult,
    val outputPath: Path,
) : Test {

    override val fullName: String
        get() = library.fullName

    val resultPath: Path
        get() = outputPath.resolve("result.txt")

    val durationPath: Path
        get() = outputPath.resolve("duration.txt")

    val logPath: Path
        get() = outputPath.resolve("run.log")
}
