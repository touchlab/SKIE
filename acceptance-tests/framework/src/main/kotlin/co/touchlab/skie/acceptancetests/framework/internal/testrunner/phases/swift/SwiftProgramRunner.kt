package co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.swift

import co.touchlab.skie.acceptancetests.framework.TestResult
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.TestResultBuilder
import java.nio.file.Path
import kotlin.io.path.absolutePathString

internal class SwiftProgramRunner(private val testResultBuilder: TestResultBuilder) {

    fun runProgram(binary: Path): TestResult {
        val command = binary.absolutePathString()

        val result = command.execute()

        testResultBuilder.appendLog("Program output", result.stdOut)

        return interpretResult(result)
    }

    private fun interpretResult(result: CommandResult): TestResult =
        if (result.exitCode == 0) {
            testResultBuilder.buildSuccess()
        } else if (result.stdErr.isEmpty()) {
            testResultBuilder.buildIncorrectOutput(result.exitCode)
        } else if (result.stdErr.contains(TestResult.MissingExit.ERROR_MESSAGE)) {
            testResultBuilder.buildMissingExit()
        } else {
            testResultBuilder.buildRuntimeError(result.stdErr)
        }
}
