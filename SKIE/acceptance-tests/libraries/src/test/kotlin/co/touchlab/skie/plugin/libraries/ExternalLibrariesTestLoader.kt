package co.touchlab.skie.plugin.libraries

import co.touchlab.skie.acceptancetests.framework.ExpectedTestResult
import co.touchlab.skie.acceptancetests.framework.util.TestProperties
import co.touchlab.skie.libraries.TestBuildConfig
import co.touchlab.skie.plugin.libraries.library.Component
import co.touchlab.skie.plugin.libraries.library.TestedLibrary
import co.touchlab.skie.plugin.libraries.library.mergeWith
import co.touchlab.skie.plugin.libraries.lockfile.LockfileProvider
import co.touchlab.skie.plugin.libraries.maven.MavenInternalSearchEngine
import co.touchlab.skie.plugin.libraries.maven.MavenPublicSearchEngine
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

class ExternalLibrariesTestLoader(
    private val testDirectoryManager: TestDirectoryManager,
    private val isSkieEnabled: Boolean,
) {

    fun loadTests(): List<ExternalLibraryTest> {
        val libraries = loadLibraries()

        val expectedFailures = loadExpectedFailures()

        validateExpectedFailures(expectedFailures)

        testDirectoryManager.createDirectories(libraries)

        return libraries.map { library ->
            ExternalLibraryTest(
                library,
                expectedTestResult(library, expectedFailures),
                testDirectoryManager.directoryForLibrary(library).toPath(),
            )
        }
    }

    private fun loadLibraries(): List<TestedLibrary> {
        val lockfileLibraries = getLockfileLibraries()

        val mavenLibraries = getMavenLibraries()

        val primaryLibraries = lockfileLibraries.mergeWith(mavenLibraries)

        val dependencyLibraries = getDependencyLibraries(primaryLibraries)

        return primaryLibraries.mergeWith(dependencyLibraries)
    }

    private fun getLockfileLibraries(): List<TestedLibrary> =
        if ("ignoreLockfile" !in TestProperties) {
            LockfileProvider.getLockfile()?.libraries ?: error("Lockfile does not exist.")
        } else {
            emptyList()
        }

    private fun getMavenLibraries(): List<TestedLibrary> {
        val mavenSearchConfiguration = TestProperties["queryMavenCentral"]

        val searchEngine = when {
            mavenSearchConfiguration == "useInternalSearch" -> MavenInternalSearchEngine
            mavenSearchConfiguration != null -> MavenPublicSearchEngine
            else -> return emptyList()
        }

        return searchEngine.findIosArm64Libraries(
            fromPage = TestProperties["queryMavenCentral-fromPage"]?.toIntOrNull() ?: 0,
            numberOfPages = TestProperties["queryMavenCentral-numberOfPages"]?.toIntOrNull() ?: 100_000,
        )
    }

    private fun getDependencyLibraries(testedLibraries: List<TestedLibrary>): List<TestedLibrary> =
        if ("convertLibraryDependenciesToTests" in TestProperties) {
            testedLibraries
                .flatMap { it.dependencies }
                .distinctBy { it.module }
                .mapIndexed { index, library ->
                    TestedLibrary(index, Component(library.module, "+"), emptyList())
                }
        } else {
            emptyList()
        }

    private fun loadExpectedFailures(): List<ExpectedTestFailure> {
        if ("ignoreExpectedFailures" in TestProperties) {
            return emptyList()
        }

        val expectedFailuresFile = Path(TestBuildConfig.EXPECTED_FAILURES_PATH).takeIf { it.exists() }

        // Allowing us to put regexes on multiple lines with the same expected errors
        var clearOnNextExpectedError = false
        val expectedErrorsAccumulator = mutableListOf<String>()
        val expectedFailures = mutableListOf<ExpectedTestFailure>()
        expectedFailuresFile?.readText().orEmpty().split("\n").forEach { untrimmedLine ->
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
                    clearOnNextExpectedError = true

                    var isXcodeDependent = false

                    val regex = when {
                        line.startsWith(skieEnabledPrefix) -> if (isSkieEnabled) {
                            line.removePrefix(skieEnabledPrefix)
                        } else {
                            null
                        }
                        line.startsWith(skieDisabledPrefix) -> if (!isSkieEnabled) {
                            line.removePrefix(skieDisabledPrefix)
                        } else {
                            null
                        }
                        line.startsWith(xcodeDependentPrefix) -> {
                            isXcodeDependent = true

                            line.removePrefix(xcodeDependentPrefix)
                        }
                        else -> line
                    }?.toRegex() ?: return@forEach

                    expectedFailures.add(ExpectedTestFailure(regex, expectedErrorsAccumulator.toList(), isXcodeDependent))
                }
            }
        }
        return expectedFailures
    }

    private fun validateExpectedFailures(expectedFailures: List<ExpectedTestFailure>) {
        val librariesWithMultipleExpectedFailures = expectedFailures
            .groupBy { it.componentRegex.pattern.trim() }
            .filter { it.value.size > 1 }
            .values
            .map { it.first() }
            .joinToString("\n") { "    " + it.componentRegex.pattern }

        if (librariesWithMultipleExpectedFailures.isNotEmpty()) {
            error("The following libraries have multiple expected failures associated with them: \n$librariesWithMultipleExpectedFailures")
        }
    }

    private fun expectedTestResult(library: TestedLibrary, expectedFailures: List<ExpectedTestFailure>): ExpectedTestResult {
        return expectedFailures.find { it.componentRegex.containsMatchIn(library.component.coordinate) }?.let { expectedFailure ->
            ExpectedTestResult.Union(
                listOf(
                    ExpectedTestResult.KotlinCompilationError(expectedFailure.expectedErrors),
                    ExpectedTestResult.KotlinLinkingError(expectedFailure.expectedErrors),
                    ExpectedTestResult.SwiftCompilationError(expectedFailure.expectedErrors),
                ) + if (expectedFailure.isXcodeDependent) {
                    listOf(ExpectedTestResult.Success)
                } else {
                    emptyList()
                },
            )
        } ?: ExpectedTestResult.Success
    }

    private data class ExpectedTestFailure(
        val componentRegex: Regex,
        val expectedErrors: List<String>,
        val isXcodeDependent: Boolean,
    )

    companion object {

        const val skieEnabledPrefix = "[skie-enabled] "
        const val skieDisabledPrefix = "[skie-disabled] "
        const val xcodeDependentPrefix = "[xcode-dependent] "
    }
}
