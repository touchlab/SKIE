package co.touchlab.swiftgen.acceptancetests.framework.internal.testrunner

import co.touchlab.swiftgen.acceptancetests.framework.TestResult

internal class TestResultBuilder {

    private val logger = StringBuilder()

    fun appendLog(header: String, text: String) {
        if (text.isNotBlank()) {
            logger.appendLine("---------------- $header ----------------")
            logger.append(text)

            if (!text.endsWith(System.lineSeparator())) {
                logger.appendLine()
            }
        }
    }

    fun buildSuccess(): TestResult.Success =
        TestResult.Success(logger.toString())

    fun buildMissingExit(): TestResult.MissingExit =
        TestResult.MissingExit(logger.toString())

    fun buildIncorrectOutput(exitCode: Int): TestResult.IncorrectOutput =
        TestResult.IncorrectOutput(logger.toString(), exitCode)

    fun buildRuntimeError(error: String): TestResult.RuntimeError =
        TestResult.RuntimeError(logger.toString(), error)

    fun buildSwiftCompilationError(error: String): TestResult.SwiftCompilationError =
        TestResult.SwiftCompilationError(logger.toString(), error)

    fun buildKotlinCompilationError(error: String): TestResult.KotlinCompilationError =
        TestResult.KotlinCompilationError(logger.toString(), error)
}