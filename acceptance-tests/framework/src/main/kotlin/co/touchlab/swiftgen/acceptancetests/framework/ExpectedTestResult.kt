package co.touchlab.swiftgen.acceptancetests.framework

import io.kotest.assertions.fail

sealed interface ExpectedTestResult {

    fun evaluate(testResult: TestResult)

    companion object {

        private val expectedResultRegex = "^\\s*([a-zA-Z]+)\\s*(\\((.*)\\))?\\s*\$".toRegex()

        operator fun invoke(text: String): ExpectedTestResult {
            val match = expectedResultRegex.matchEntire(text) ?: throwInvalidExpectedResultDeclaration(text)

            val className = match.groups[1]?.value?.lowercase() ?: throwInvalidExpectedResultDeclaration(text)
            val arguments = match.groups[3]?.value

            return when (className) {
                Success::class.simpleName?.lowercase() -> Success
                MissingExit::class.simpleName?.lowercase() -> MissingExit
                IncorrectOutput::class.simpleName?.lowercase() -> {
                    val exitCode = arguments?.let {
                        arguments.toIntOrNull() ?: throw IllegalArgumentException(
                            "Argument for ${IncorrectOutput::class.simpleName} must be an Int. Was: $arguments"
                        )
                    }

                    IncorrectOutput(exitCode)
                }
                RuntimeError::class.simpleName?.lowercase() -> RuntimeError(arguments)
                SwiftCompilationError::class.simpleName?.lowercase() -> SwiftCompilationError(arguments)
                KotlinCompilationError::class.simpleName?.lowercase() -> KotlinCompilationError(arguments)
                else -> throwInvalidExpectedResultDeclaration(text)
            }
        }

        private fun throwInvalidExpectedResultDeclaration(text: String): Nothing {
            throw IllegalArgumentException(
                "\"$text\" is not a valid expected result declaration. " +
                        "Expected format: \"\$Name\" or \"\$Name(\$arguments)\". " +
                        "The name must match one of the subclasses of ExpectedResultTest. " +
                        "The arguments are always optional. " +
                        "If they are present, they are matched against the actual arguments of the TestResult. " +
                        "If not, only the class type is checked."
            )
        }
    }

    object Success : ExpectedTestResult {

        override fun evaluate(testResult: TestResult) {
            if (testResult is TestResult.Success) {
                return
            }

            failTest(testResult, TestResult.Success.ERROR_MESSAGE)
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
    val errorMessage = """
            Expected: $expected
            Actual: ${result.actualErrorMessage}
        """.trimIndent()

    fail(errorMessage)
}