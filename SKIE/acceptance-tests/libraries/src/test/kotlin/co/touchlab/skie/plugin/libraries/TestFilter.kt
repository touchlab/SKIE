package co.touchlab.skie.plugin.libraries

import co.touchlab.skie.acceptancetests.framework.ExpectedTestResult
import co.touchlab.skie.acceptancetests.framework.util.TestProperties
import co.touchlab.skie.libraries.TestBuildConfig
import co.touchlab.skie.plugin.libraries.library.Module
import co.touchlab.skie.plugin.libraries.lockfile.LockfileProvider
import kotlin.io.path.Path
import kotlin.io.path.notExists
import kotlin.io.path.readText

sealed interface TestFilter {

    fun shouldBeEvaluated(test: ExternalLibraryTest): Boolean

    data class Regex(private val pattern: String) : TestFilter {

        private val regex = kotlin.text.Regex(pattern)

        override fun shouldBeEvaluated(test: ExternalLibraryTest): Boolean =
            regex.containsMatchIn(test.library.fullName)
    }

    object FailedOnly : TestFilter {

        override fun shouldBeEvaluated(test: ExternalLibraryTest): Boolean {
            val resultFile = test.resultPath

            return resultFile.notExists() || resultFile.readText() != ExpectedTestResult.SUCCESS
        }
    }

    data class Indices(private val indices: Set<Int>) : TestFilter {

        override fun shouldBeEvaluated(test: ExternalLibraryTest): Boolean = test.library.index in indices
    }

    data class NotModules(private val modules: Set<Module>) : TestFilter {

        override fun shouldBeEvaluated(test: ExternalLibraryTest): Boolean = test.library.component.module !in modules
    }

    object VersionUnresolved : TestFilter {

        override fun shouldBeEvaluated(test: ExternalLibraryTest): Boolean =
            test.library.component.version == "+"
    }

    object Empty : TestFilter {

        override fun shouldBeEvaluated(test: ExternalLibraryTest): Boolean = true
    }

    data class Intersection(val filters: List<TestFilter>) : TestFilter {

        override fun shouldBeEvaluated(test: ExternalLibraryTest): Boolean =
            filters.all { it.shouldBeEvaluated(test) }
    }

    companion object {

        val active: TestFilter = buildTestFilter()

        private fun buildTestFilter(): TestFilter {
            var testFilter: TestFilter = Empty

            testFilter = testFilter.withRegexFilter()
            testFilter = testFilter.withIndicesFilter()
            testFilter = testFilter.withSkipTestsInLockfileFilter()
            testFilter = testFilter.withFailedOnlyFilter()
            testFilter = testFilter.withVersionUnresolvedFilter()

            return testFilter
        }

        private fun TestFilter.withRegexFilter(): TestFilter {
            val regex = TestProperties["libraryTest"]

            if (!regex.isNullOrBlank()) {
                return this + Regex(regex)
            }

            return this
        }

        private fun TestFilter.withIndicesFilter(): TestFilter {
            val rawIndices = TestProperties["onlyIndices"]

            if (!rawIndices.isNullOrBlank()) {
                val indexRanges = rawIndices.split(',').map { it.trim() }.filter { it.isNotBlank() }
                val indices = indexRanges.flatMap { range ->
                    val parsedRange = range.split('-').map { it.trim() }

                    when (parsedRange.size) {
                        1 -> listOf(parsedRange[0].toInt())
                        2 -> parsedRange[0].toInt().rangeTo(parsedRange[1].toInt()).toList()
                        else -> throw IllegalArgumentException("Invalid range: $parsedRange")
                    }
                }.toSet()

                return this + Indices(indices)
            }

            return this
        }

        private fun TestFilter.withSkipTestsInLockfileFilter(): TestFilter {
            if ("skipTestsInLockfile" !in TestProperties) {
                return this
            }

            val lockfilePath = TestProperties["skipTestsInLockfile"]?.takeIf { it.isNotBlank() }?.let { Path(it) } ?: Path(TestBuildConfig.LOCKFILE_PATH)

            val skippedModules = LockfileProvider.getLockfile(lockfilePath)?.libraries?.map { it.component.module } ?: emptyList()

            return this + NotModules(skippedModules.toSet())
        }

        private fun TestFilter.withFailedOnlyFilter(): TestFilter {
            if ("failedOnly" in TestProperties) {
                return this + FailedOnly
            }

            return this
        }

        private fun TestFilter.withVersionUnresolvedFilter(): TestFilter {
            if ("onlyUnresolvedVersions" in TestProperties) {
                return this + VersionUnresolved
            }

            return this
        }
    }
}

operator fun TestFilter.plus(testFilter: TestFilter): TestFilter =
    TestFilter.Intersection(listOf(this, testFilter))
