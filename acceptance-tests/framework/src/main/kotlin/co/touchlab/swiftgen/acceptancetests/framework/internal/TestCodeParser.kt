package co.touchlab.swiftgen.acceptancetests.framework.internal

import co.touchlab.swiftgen.acceptancetests.framework.ExpectedTestResult
import co.touchlab.swiftgen.acceptancetests.framework.ExpectedTestResult.IncorrectOutput
import co.touchlab.swiftgen.acceptancetests.framework.ExpectedTestResult.KotlinCompilationError
import co.touchlab.swiftgen.acceptancetests.framework.ExpectedTestResult.MissingExit
import co.touchlab.swiftgen.acceptancetests.framework.ExpectedTestResult.RuntimeError
import co.touchlab.swiftgen.acceptancetests.framework.ExpectedTestResult.Success
import co.touchlab.swiftgen.acceptancetests.framework.ExpectedTestResult.SuccessWithWarning
import co.touchlab.swiftgen.acceptancetests.framework.ExpectedTestResult.SuccessWithoutWarning
import co.touchlab.swiftgen.acceptancetests.framework.ExpectedTestResult.SwiftCompilationError
import kotlin.reflect.KClass

internal object TestCodeParser {

    private val expectedResultRegex = "^\\s*([a-zA-Z]+)\\s*(\\((.*)\\))?\\s*\$".toRegex()

    fun parse(lines: List<String>): ParsedTest = ParsedTest(
        expectedResult = getExpectedResult(lines),
        swiftCode = getSwiftCode(lines),
    )

    private fun getSwiftCode(lines: List<String>): String =
        lines.dropConfigurationLine().joinToString(System.lineSeparator())

    private fun List<String>.dropConfigurationLine(): List<String> =
        if (this.hasConfigurationLine) this.drop(1) else this

    private fun List<String>.getConfiguration(): String? =
        if (this.hasConfigurationLine) this.first().drop(1) else null

    private val List<String>.hasConfigurationLine: Boolean
        get() = this.firstOrNull()?.startsWith("#") ?: false

    private fun getExpectedResult(lines: List<String>): ExpectedTestResult {
        val expectedResultConfiguration = lines.getConfiguration() ?: return Success

        val match = expectedResultRegex.matchEntire(expectedResultConfiguration)
            ?: throwInvalidExpectedResult(expectedResultConfiguration)

        val className = match.groups[1]?.value?.lowercase() ?: throwInvalidExpectedResult(expectedResultConfiguration)
        val arguments = match.groups[3]?.value

        return ExpectedTestResult(className, arguments, expectedResultConfiguration)
    }

    private operator fun ExpectedTestResult.Companion.invoke(
        className: String,
        arguments: String?,
        expectedResultConfiguration: String,
    ): ExpectedTestResult {
        val requiredArgument by lazy {
            requireNotNull(arguments) { "$className requires an argument." }
        }

        return when (className) {
            Success::class.configurationName() -> Success
            SuccessWithWarning::class.configurationName() -> SuccessWithWarning(requiredArgument)
            SuccessWithoutWarning::class.configurationName() -> SuccessWithoutWarning(requiredArgument)
            MissingExit::class.configurationName() -> MissingExit
            IncorrectOutput::class.configurationName() -> {
                val exitCode = arguments?.let {
                    arguments.toIntOrNull() ?: throw IllegalArgumentException(
                        "Argument for ${IncorrectOutput::class.simpleName} " +
                                "must be an Int. Was: $arguments"
                    )
                }

                IncorrectOutput(exitCode)
            }
            RuntimeError::class.configurationName() -> RuntimeError(arguments)
            SwiftCompilationError::class.configurationName() -> {
                SwiftCompilationError(arguments)
            }
            KotlinCompilationError::class.configurationName() -> {
                KotlinCompilationError(arguments)
            }
            else -> throwInvalidExpectedResult(expectedResultConfiguration)
        }
    }

    private fun KClass<*>.configurationName(): String? =
        simpleName?.lowercase()

    private fun throwInvalidExpectedResult(text: String): Nothing {
        throw IllegalArgumentException(
            "\"$text\" is not a valid expected result declaration. " +
                    "Expected format: \"\$Name\" or \"\$Name(\$arguments)\". " +
                    "The name must match one of the subclasses of ExpectedResultTest. " +
                    "The arguments are always optional. " +
                    "If they are present, they are matched against the actual arguments of the TestResult. " +
                    "If not, only the class type is checked."
        )
    }

    data class ParsedTest(
        val expectedResult: ExpectedTestResult,
        val swiftCode: String,
    )
}