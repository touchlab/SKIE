package co.touchlab.swiftgen.acceptancetests.framework.internal

import co.touchlab.swiftgen.acceptancetests.framework.ExpectedTestResult
import co.touchlab.swiftgen.acceptancetests.framework.ExpectedTestResult.*
import co.touchlab.swiftgen.configuration.SwiftGenConfiguration
import kotlin.reflect.KClass

internal object TestCodeParser {

    private val expectedResultRegex = "^\\s*([a-zA-Z]+)\\s*(\\((.*)\\))?\\s*\$".toRegex()
    private const val expectedResultKey = "expected"

    fun parse(lines: List<String>): ParsedTest {
        val hasConfigurationLine = lines.firstOrNull()?.startsWith("#") ?: false

        val configurationChanges = if (hasConfigurationLine) parseConfigurationLine(lines.first()) else emptyMap()

        return ParsedTest(
            expectedResult = buildExpectedResult(configurationChanges),
            configuration = buildSwiftGenConfiguration(configurationChanges),
            configurationChanges = configurationChanges,
            swiftCode = (if (hasConfigurationLine) lines.drop(1) else lines).joinToString(System.lineSeparator()),
        )
    }

    private fun parseConfigurationLine(line: String): Map<String, String> =
        line.drop(1)
            .split(";")
            .associate { singleConfiguration ->
                val components = singleConfiguration.trim().split("=")

                require(components.size >= 2) {
                    "Incorrect format of configuration item, it must be 'key=value'. Was: '$singleConfiguration'"
                }

                val key = components.first().trim()
                val value = components.drop(1).joinToString("=").trim()

                key to value
            }

    private fun buildExpectedResult(configurationChanges: Map<String, String>): ExpectedTestResult {
        val expectedResultConfiguration = configurationChanges[expectedResultKey] ?: return Success

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
    ): ExpectedTestResult = when (className) {
        Success::class.configurationName() -> Success
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

    private fun buildSwiftGenConfiguration(configurationChanges: Map<String, String>): SwiftGenConfiguration {
        val configuration = SwiftGenConfiguration()

        configurationChanges
            .filter { it.key != expectedResultKey }
            .forEach {
                configuration.set(it.key, it.value)
            }

        return configuration
    }

    data class ParsedTest(
        val expectedResult: ExpectedTestResult,
        val configuration: SwiftGenConfiguration,
        val configurationChanges: Map<String, String>,
        val swiftCode: String,
    )
}