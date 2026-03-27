package co.touchlab.skie.acceptancetests.framework

import io.kotest.assertions.fail

sealed interface ExpectedTestResult {

    fun shouldBe(testResultWithLogs: TestResultWithLogs) {
        shouldBe(testResultWithLogs.testResult, testResultWithLogs.logs)
    }

    fun shouldBe(testResult: TestResult, logs: String) {
        if (!hasSucceeded(testResult, logs)) {
            val errorMessage = """
                Test failed:
                    Expected: $messageForExpectedError
                    Actual: ${testResult.actualErrorMessage}
                """.trimIndent()

            fail(errorMessage)
        }
    }

    fun hasSucceeded(testResultWithLogs: TestResultWithLogs): Boolean =
        hasSucceeded(testResultWithLogs.testResult, testResultWithLogs.logs)

    fun hasSucceeded(testResult: TestResult, logs: String): Boolean

    val messageForExpectedError: String

    companion object {

        const val SUCCESS: String = "Success"
        const val FAILURE: String = "Failure"
    }

    object Success : ExpectedTestResult {

        override val messageForExpectedError: String
            get() = TestResult.Success.ERROR_MESSAGE

        override fun hasSucceeded(testResult: TestResult, logs: String): Boolean =
            testResult is TestResult.Success
    }

    data class SuccessWithWarning(val warning: String) : ExpectedTestResult {

        override val messageForExpectedError: String
            get() = "Tested program ended successfully by explicitly calling exit(0) and produced warning: $warning"

        override fun hasSucceeded(testResult: TestResult, logs: String): Boolean =
            testResult is TestResult.Success && logs.contains(warning)
    }

    data class SuccessWithoutWarning(val warning: String) : ExpectedTestResult {

        override val messageForExpectedError: String
            get() = "Tested program ended successfully by explicitly calling exit(0) and did not produce warning: $warning"

        override fun hasSucceeded(testResult: TestResult, logs: String): Boolean =
            testResult is TestResult.Success && !logs.contains(warning)
    }

    object MissingExit : ExpectedTestResult {

        override val messageForExpectedError: String
            get() = TestResult.MissingExit.ERROR_MESSAGE

        override fun hasSucceeded(testResult: TestResult, logs: String): Boolean =
            testResult is TestResult.MissingExit
    }

    data class IncorrectOutput(val exitCode: Int?) : ExpectedTestResult {

        override val messageForExpectedError: String
            get() = if (exitCode != null) {
                TestResult.IncorrectOutput.errorMessage(exitCode)
            } else {
                "Tested program ended with any exit code other than 0."
            }

        override fun hasSucceeded(testResult: TestResult, logs: String): Boolean =
            testResult is TestResult.IncorrectOutput && (exitCode == null || testResult.exitCode == exitCode)
    }

    data class RuntimeError(val errors: Collection<String>) : ExpectedTestResult {

        override val messageForExpectedError: String
            get() = if (errors.isNotEmpty()) {
                "Tested program ended with an error containing: ${errors.joinToString("\n")}"
            } else {
                "Tested program ended with any error."
            }

        constructor(vararg errors: String) : this(errors.toList())

        constructor(error: String?) : this(listOfNotNull(error))

        override fun hasSucceeded(testResult: TestResult, logs: String): Boolean =
            testResult is TestResult.RuntimeError && errors.all { testResult.error.contains(it) }
    }

    data class SwiftCompilationError(val errors: Collection<String>) : ExpectedTestResult {

        override val messageForExpectedError: String
            get() = if (errors.isNotEmpty()) {
                "Swift compilation ended with an error containing: ${errors.joinToString("\n")}"
            } else {
                "Swift compilation ended with any error."
            }

        constructor(vararg errors: String) : this(errors.toList())

        constructor(error: String?) : this(listOfNotNull(error))

        override fun hasSucceeded(testResult: TestResult, logs: String): Boolean =
            testResult is TestResult.SwiftCompilationError && errors.all { testResult.error.contains(it) }
    }

    data class KotlinLinkingError(val errors: Collection<String>) : ExpectedTestResult {

        override val messageForExpectedError: String
            get() = if (errors.isNotEmpty()) {
                "Kotlin linking ended with an error containing all of: ${errors.joinToString("\n")}"
            } else {
                "Kotlin linking ended with any error."
            }

        constructor(vararg errors: String) : this(errors.toList())

        constructor(error: String?) : this(listOfNotNull(error))

        override fun hasSucceeded(testResult: TestResult, logs: String): Boolean =
            testResult is TestResult.KotlinLinkingError && errors.all { testResult.error.contains(it) }
    }

    data class KotlinCompilationError(val errors: Collection<String>) : ExpectedTestResult {

        override val messageForExpectedError: String
            get() = if (errors.isNotEmpty()) {
                "Kotlin compilation ended with an error containing: ${errors.joinToString("\n")}"
            } else {
                "Kotlin compilation ended with any error."
            }

        constructor(vararg errors: String) : this(errors.toList())

        constructor(error: String?) : this(listOfNotNull(error))

        override fun hasSucceeded(testResult: TestResult, logs: String): Boolean =
            testResult is TestResult.KotlinCompilationError && errors.all { testResult.error.contains(it) }
    }

    data class Union(val expectedTestResults: Collection<ExpectedTestResult>) : ExpectedTestResult {

        override val messageForExpectedError: String
            get() = "Test ended with one of the following options: [${expectedTestResults.joinToString { it.messageForExpectedError }}]"

        constructor(vararg expectedTestResults: ExpectedTestResult) : this(expectedTestResults.toList())

        init {
            require(expectedTestResults.isNotEmpty()) { "There must be at least one expectedTestResult." }
        }

        override fun hasSucceeded(testResult: TestResult, logs: String): Boolean =
            expectedTestResults.any { it.hasSucceeded(testResult, logs) }
    }
}
