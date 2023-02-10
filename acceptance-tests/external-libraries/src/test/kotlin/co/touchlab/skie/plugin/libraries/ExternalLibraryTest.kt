package co.touchlab.skie.plugin.libraries

import co.touchlab.skie.acceptancetests.framework.ExpectedTestResult
import java.nio.file.Path

data class ExternalLibraryTest(
    val index: Int,
    val library: String,
    val input: TestInput,
    val expectedResult: ExpectedTestResult,
    val outputPath: Path,
) {

    val resultPath: Path
        get() = outputPath.resolve("result.txt")

    val logPath: Path
        get() = outputPath.resolve("run.log")

    val fullName: String
        get() = "[$index]: $library"
}
