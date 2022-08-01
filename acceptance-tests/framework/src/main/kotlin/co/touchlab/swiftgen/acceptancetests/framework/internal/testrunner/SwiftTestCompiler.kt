package co.touchlab.swiftgen.acceptancetests.framework.internal.testrunner

import co.touchlab.swiftgen.acceptancetests.framework.TempFileSystem
import co.touchlab.swiftgen.acceptancetests.framework.internal.TestResult
import java.nio.file.Path
import kotlin.io.path.absolutePathString

internal class SwiftTestCompiler(private val tempFileSystem: TempFileSystem, private val logger: Logger) {

    fun compile(swiftFile: Path, kotlinFrameworkDirectory: Path): IntermediateResult<Path> {
        val output = tempFileSystem.createFile()

        val command = createCompileSwiftCommand(swiftFile, kotlinFrameworkDirectory, output)

        val result = command.execute()

        logger.write("Swift compilation", result.stdOut)

        return interpretCompileSwiftResult(result, output)
    }

    private fun createCompileSwiftCommand(
        swiftFile: Path,
        kotlinFrameworkDirectory: Path,
        output: Path
    ): String = listOf(
        "swiftc",
        swiftFile.absolutePathString(),
        "-F",
        kotlinFrameworkDirectory.absolutePathString(),
        "-o",
        output.absolutePathString(),
    ).joinToString(" ")

    private fun interpretCompileSwiftResult(result: CommandResult, output: Path): IntermediateResult<Path> =
        if (result.exitCode == 0) {
            IntermediateResult.Value(output)
        } else {
            val testResult = TestResult.SwiftCompilationError(logger.toString(), result.stdErr)

            IntermediateResult.Error(testResult)
        }
}