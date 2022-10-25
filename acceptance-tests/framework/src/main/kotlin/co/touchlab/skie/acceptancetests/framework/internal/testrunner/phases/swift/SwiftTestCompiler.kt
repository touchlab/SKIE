package co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.swift

import co.touchlab.skie.acceptancetests.framework.TempFileSystem
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.IntermediateResult
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.TestResultBuilder
import java.nio.file.Path
import kotlin.io.path.absolutePathString

internal class SwiftTestCompiler(
    private val tempFileSystem: TempFileSystem,
    private val testResultBuilder: TestResultBuilder,
) {

    fun compile(kotlinFramework: Path, swiftFile: Path): IntermediateResult<Path> {
        val output = tempFileSystem.createFile("swift-binary")

        val command = createCompileSwiftCommand(kotlinFramework, swiftFile, output)

        val result = command.execute()

        testResultBuilder.appendLog("Swift compiler", result.stdOut)

        return interpretResult(result, output)
    }

    private fun createCompileSwiftCommand(
        kotlinFramework: Path,
        swiftFile: Path,
        output: Path,
    ): String = listOf(
        "swiftc",
        swiftFile.absolutePathString(),
        "-F",
        kotlinFramework.parent.absolutePathString(),
        "-v",
        // "-driver-time-compilation",
        // "-print-educational-notes",
        // "-Xfrontend", "-debug-constraints",
        "-o",
        output.absolutePathString(),
        // Workaround for https://github.com/apple/swift/issues/55127
        "-parse-as-library",
    ).joinToString(" ")

    private fun interpretResult(result: CommandResult, output: Path): IntermediateResult<Path> =
        if (result.exitCode == 0) {
            IntermediateResult.Value(output)
        } else {
            val testResult = testResultBuilder.buildSwiftCompilationError(result.stdErr)

            IntermediateResult.Error(testResult)
        }
}
