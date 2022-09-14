package co.touchlab.swiftgen.acceptancetests.framework.internal.testrunner.phases.swift

import co.touchlab.swiftgen.acceptancetests.framework.TempFileSystem
import co.touchlab.swiftgen.acceptancetests.framework.internal.testrunner.IntermediateResult
import co.touchlab.swiftgen.acceptancetests.framework.internal.testrunner.TestResultBuilder
import java.nio.file.Path
import kotlin.io.path.absolutePathString

internal class SwiftTestCompiler(
    private val tempFileSystem: TempFileSystem,
    private val testResultBuilder: TestResultBuilder,
) {

    fun compile(swiftFile: Path, kotlinFrameworkDirectory: Path): IntermediateResult<Path> {
        val output = tempFileSystem.createFile("swift-binary")

        val command = createCompileSwiftCommand(swiftFile, kotlinFrameworkDirectory, output)

        val result = command.execute()

        testResultBuilder.appendLog("Swift compilation", result.stdOut)

        return interpretResult(result, output)
    }

    private fun createCompileSwiftCommand(
        swiftFile: Path,
        kotlinFrameworkDirectory: Path,
        output: Path,
    ): String = listOf(
        "swiftc",
        swiftFile.absolutePathString(),
        "-F",
        kotlinFrameworkDirectory.absolutePathString(),
        "-o",
        output.absolutePathString(),
    ).joinToString(" ")

    private fun interpretResult(result: CommandResult, output: Path): IntermediateResult<Path> =
        if (result.exitCode == 0) {
            IntermediateResult.Value(output)
        } else {
            val testResult = testResultBuilder.buildSwiftCompilationError(result.stdErr)

            IntermediateResult.Error(testResult)
        }
}
