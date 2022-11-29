package co.touchlab.skie.acceptancetests.framework

sealed interface TestResult {

    val actualErrorMessage: String

    object Success : TestResult {

        override val actualErrorMessage: String
            get() = ERROR_MESSAGE

        const val ERROR_MESSAGE: String =
            "Tested program ended successfully by explicitly calling exit(0)."
    }

    object MissingExit : TestResult {

        override val actualErrorMessage: String
            get() = ERROR_MESSAGE

        const val ERROR_MESSAGE: String =
            "Tested program ended without explicitly calling `exit(0)`." +
                " This scenario is handled as an error to prevent tests from " +
                "accidentally passing via unexpected execution path."
    }

    data class IncorrectOutput(val exitCode: Int) : TestResult {

        override val actualErrorMessage: String
            get() = errorMessage(exitCode)

        companion object {

            fun errorMessage(exitCode: Int): String =
                "Tested program ended with an incorrect exit code $exitCode."
        }
    }

    data class RuntimeError(val error: String) : TestResult {

        override val actualErrorMessage: String
            get() = "Tested program ended with the following error: $error"
    }

    data class SwiftCompilationError(val error: String) : TestResult {

        override val actualErrorMessage: String
            get() = "Swift compilation ended with the following error: $error"
    }

    data class KotlinLinkingError(val error: String) : TestResult {

        override val actualErrorMessage: String
            get() = "Kotlin linking ended with the following error: $error"
    }

    data class KotlinCompilationError(val error: String) : TestResult {

        override val actualErrorMessage: String
            get() = "Kotlin compilation ended with the following error: $error"
    }
}
