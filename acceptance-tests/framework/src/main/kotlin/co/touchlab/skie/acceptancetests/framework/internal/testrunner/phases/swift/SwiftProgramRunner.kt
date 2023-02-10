package co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.swift

import co.touchlab.skie.acceptancetests.framework.TestResult
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.TestLogger
import java.nio.file.Path
import kotlin.io.path.absolutePathString

internal class SwiftProgramRunner(private val testLogger: TestLogger) {

    fun runProgram(binary: Path): TestResult {
        val command = binary.absolutePathString()

        val result = command.execute()

        testLogger.appendSection("Program output", result.stdOut)

        return interpretResult(result)
    }

    private fun interpretResult(result: CommandResult): TestResult =
        if (result.exitCode == 0) {
            TestResult.Success
        } else if (result.stdOut.isEmpty()) {
            TestResult.IncorrectOutput(result.exitCode)
        } else if (result.stdOut.contains(TestResult.MissingExit.ERROR_MESSAGE)) {
            TestResult.MissingExit
        } else {
            TestResult.RuntimeError(result.stdOut)
        }
}
