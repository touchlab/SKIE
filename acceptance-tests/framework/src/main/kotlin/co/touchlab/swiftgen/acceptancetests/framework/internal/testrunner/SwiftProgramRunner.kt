package co.touchlab.swiftgen.acceptancetests.framework.internal.testrunner

import co.touchlab.swiftgen.acceptancetests.framework.internal.TestResult
import java.nio.file.Path
import kotlin.io.path.absolutePathString

internal class SwiftProgramRunner(private val logger: Logger) {

    fun runProgram(binary: Path): TestResult {
        val command = binary.absolutePathString()

        val result = command.execute()

        logger.write("Program output", result.stdOut)

        return interpretRunSwiftResult(result, logger)
    }

    private fun interpretRunSwiftResult(result: CommandResult, logger: Logger): TestResult =
        if (result.exitCode == 0) {
            TestResult.Success(logger.toString())
        } else if (result.stdErr.isEmpty()) {
            TestResult.IncorrectOutput(logger.toString(), result.exitCode)
        } else if (result.stdErr.contains(TestResult.MissingExit.ERROR_MESSAGE)) {
            TestResult.MissingExit(logger.toString())
        } else {
            TestResult.RuntimeError(logger.toString(), result.stdErr)
        }
}