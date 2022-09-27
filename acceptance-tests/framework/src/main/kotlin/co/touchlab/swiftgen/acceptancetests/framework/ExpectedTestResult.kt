package co.touchlab.swiftgen.acceptancetests.framework

import io.kotest.assertions.fail

sealed interface ExpectedTestResult {

    fun evaluate(testResult: TestResult)

    fun hasSucceeded(testResult: TestResult): Boolean = try {
        evaluate(testResult)

        true
    } catch (_: Throwable) {
        false
    }

    companion object {

        const val SUCCESS: String = "Success"
        const val FAILURE: String = "Failure"
    }

    object Success : ExpectedTestResult {

        override fun evaluate(testResult: TestResult) {
            if (testResult is TestResult.Success) {
                return
            }

            failTest(testResult, TestResult.Success.ERROR_MESSAGE)
        }
    }

    data class SuccessWithWarning(val warning: String) : ExpectedTestResult {

        override fun evaluate(testResult: TestResult) {
            if (testResult is TestResult.Success && testResult.logs.contains(warning)) {
                return
            }

            failTest(testResult, "Tested program ended successfully by explicitly calling exit(0) and produced warning: $warning")
        }
    }

    data class SuccessWithoutWarning(val warning: String) : ExpectedTestResult {

        override fun evaluate(testResult: TestResult) {
            if (testResult is TestResult.Success && !testResult.logs.contains(warning)) {
                return
            }

            failTest(testResult, "Tested program ended successfully by explicitly calling exit(0) and did not produce warning: $warning")
        }
    }

    object MissingExit : ExpectedTestResult {

        override fun evaluate(testResult: TestResult) {
            if (testResult is TestResult.MissingExit) {
                return
            }

            failTest(testResult, TestResult.MissingExit.ERROR_MESSAGE)
        }
    }

    data class IncorrectOutput(val exitCode: Int?) : ExpectedTestResult {

        override fun evaluate(testResult: TestResult) {
            if (exitCode != null) {
                if (testResult is TestResult.IncorrectOutput && testResult.exitCode == exitCode) {
                    return
                }

                failTest(testResult, TestResult.IncorrectOutput.errorMessage(exitCode))
            } else {
                if (testResult is TestResult.IncorrectOutput) {
                    return
                }

                failTest(testResult, "Tested program ended with any exit code other than 0.")
            }
        }
    }

    data class RuntimeError(val error: String?) : ExpectedTestResult {

        override fun evaluate(testResult: TestResult) {
            if (error != null) {
                if (testResult is TestResult.RuntimeError && testResult.error.contains(error)) {
                    return
                }

                failTest(testResult, "Tested program ended with an error containing: $error")
            } else {
                if (testResult is TestResult.RuntimeError) {
                    return
                }

                failTest(testResult, "Tested program ended with any error.")
            }
        }
    }

    data class SwiftCompilationError(val error: String?) : ExpectedTestResult {

        override fun evaluate(testResult: TestResult) {
            if (error != null) {
                if (testResult is TestResult.SwiftCompilationError && testResult.error.contains(error)) {
                    return
                }

                failTest(testResult, "Swift compilation ended with an error containing: $error")
            } else {
                if (testResult is TestResult.SwiftCompilationError) {
                    return
                }

                failTest(testResult, "Swift compilation ended with any error.")
            }
        }
    }

    data class KotlinCompilationError(val error: String?) : ExpectedTestResult {

        override fun evaluate(testResult: TestResult) {
            if (error != null) {
                if (testResult is TestResult.KotlinCompilationError && testResult.error.contains(error)) {
                    return
                }

                failTest(testResult, "Kotlin compilation ended with an error containing: $error")
            } else {
                if (testResult is TestResult.KotlinCompilationError) {
                    return
                }

                failTest(testResult, "Kotlin compilation ended with any error.")
            }
        }
    }
}

private fun failTest(result: TestResult, expected: String) {
    val errorMessage =
        """
        Test failed:
            Expected: $expected
            Actual: ${result.actualErrorMessage}
        """.trimIndent()

    fail(errorMessage)
}