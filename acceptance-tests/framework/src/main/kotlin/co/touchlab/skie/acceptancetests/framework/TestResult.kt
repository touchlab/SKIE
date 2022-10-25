package co.touchlab.skie.acceptancetests.framework

sealed interface TestResult {

    val logs: String

    val actualErrorMessage: String

    data class Success(override val logs: String) : TestResult {

        override val actualErrorMessage: String
            get() = ERROR_MESSAGE

        companion object {

            const val ERROR_MESSAGE: String =
                "Tested program ended successfully by explicitly calling exit(0)."
        }
    }

    data class MissingExit(override val logs: String) : TestResult {

        override val actualErrorMessage: String
            get() = ERROR_MESSAGE

        companion object {

            const val ERROR_MESSAGE: String =
                "Tested program ended without explicitly calling `exit(0)`." +
                    " This scenario is handled as an error to prevent tests from " +
                    "accidentally passing via unexpected execution path."
        }
    }

    data class IncorrectOutput(override val logs: String, val exitCode: Int) : TestResult {

        override val actualErrorMessage: String
            get() = errorMessage(exitCode)

        companion object {

            fun errorMessage(exitCode: Int): String =
                "Tested program ended with an incorrect exit code $exitCode."
        }
    }

    data class RuntimeError(override val logs: String, val error: String) : TestResult {

        override val actualErrorMessage: String
            get() = "Tested program ended with the following error: $error"
    }

    data class SwiftCompilationError(override val logs: String, val error: String) : TestResult {

        override val actualErrorMessage: String
            get() = "Swift compilation ended with the following error: $error"
    }

    data class KotlinLinkingError(override val logs: String, val error: String) : TestResult {

        override val actualErrorMessage: String
            get() = "Kotlin linking ended with the following error: $error"
    }

    data class KotlinCompilationError(override val logs: String, val error: String) : TestResult {

        override val actualErrorMessage: String
            get() = "Kotlin compilation ended with the following error: $error"
    }
}
