package co.touchlab.swiftgen.sealed.acceptancetests.framework.internal

internal sealed interface TestResult {

    val logs: String

    /**
     * Everything compiles and the resulting program explicitly calls `exit(0)`.
     */
    data class Success(override val logs: String) : TestResult

    data class MissingExit(override val logs: String) : TestResult {

        companion object {

            const val ERROR_MESSAGE: String =
                "The program finished without explicitly calling `exit(0)`." +
            " This is interpreted as an error to prevent tests from accidentally passing via unexpected execution path."
        }
    }

    data class IncorrectOutput(override val logs: String, val code: Int) : TestResult

    data class RuntimeError(override val logs: String, val error: String) : TestResult

    data class SwiftCompilationError(override val logs: String, val error: String) : TestResult

    data class KotlinCompilationError(override val logs: String, val error: String) : TestResult
}