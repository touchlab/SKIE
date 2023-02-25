package co.touchlab.skie.plugin.libraries

import co.touchlab.skie.acceptancetests.framework.ExpectedTestResult
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.io.File

class ExternalLibrariesTestLoader(private val testTmpDir: File) {
    fun loadLibrariesToTest(): List<ExternalLibraryTest> {
        val allLibrariesFile = this::class.java.getResource("/test-input.json")
        val allLibraries = Json.decodeFromString(MapSerializer(String.serializer(), TestInput.serializer()), allLibrariesFile.readText())
        val expectedFailures = loadExpectedFailures()

        return allLibraries.toList()
            .mapIndexed { index, (library, input) ->
                ExternalLibraryTest(
                    index,
                    library,
                    input,
                    expectedTestResult(library, expectedFailures),
                    testTmpDir.resolve("${index}-${library.replace(" ", "-").replace(":", "-")}").toPath(),
                )
            }
    }

    private fun loadExpectedFailures(): List<ExpectedLinkFailure> {
        val expectedFailuresFile = this::class.java.getResource("/expected-failures")

        // Allowing us to put regexes on multiple lines with the same expected errors
        var clearOnNextExpectedError = false
        val expectedErrorsAccumulator = mutableListOf<String>()
        val expectedFailures = mutableListOf<ExpectedLinkFailure>()
        expectedFailuresFile.readText().split("\n").forEach { untrimmedLine ->
            val line = untrimmedLine.trim()
            when {
                line.startsWith(">") -> {
                    if (clearOnNextExpectedError) {
                        expectedErrorsAccumulator.clear()
                        clearOnNextExpectedError = false
                    }
                    expectedErrorsAccumulator.add(line.drop(1).trim())
                }
                line.startsWith("#") || line.isEmpty() -> return@forEach
                else -> {
                    expectedFailures.add(ExpectedLinkFailure(line.toRegex(), expectedErrorsAccumulator.toList()))
                    clearOnNextExpectedError = true
                }
            }
        }
        return expectedFailures
    }

    private fun expectedTestResult(library: String, expectedFailures: List<ExpectedLinkFailure>): ExpectedTestResult {
        return expectedFailures.find { it.regex.containsMatchIn(library) }?.let { expectedFailure ->
            ExpectedTestResult.KotlinLinkingError(expectedFailure.expectedErrors)
        } ?: ExpectedTestResult.Success
    }

    private data class ExpectedLinkFailure(
        val regex: Regex,
        val expectedErrors: List<String>,
    )
}
