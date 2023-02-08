package co.touchlab.skie.plugin.libraries

import co.touchlab.skie.acceptancetests.framework.ExpectedTestResult
import co.touchlab.skie.acceptancetests.framework.TestNode
import kotlin.io.path.notExists
import kotlin.io.path.readText

interface TestFilter {

    fun shouldBeEvaluated(test: ExternalLibraryTest): Boolean

    data class Regex(private val pattern: String) : TestFilter {

        private val regex = kotlin.text.Regex(pattern)

        override fun shouldBeEvaluated(test: ExternalLibraryTest): Boolean =
            regex.containsMatchIn(test.fullName)
    }

    object FailedOnly : TestFilter {

        override fun shouldBeEvaluated(test: ExternalLibraryTest): Boolean {
            val resultFile = test.resultPath

            return resultFile.notExists() || resultFile.readText() != ExpectedTestResult.SUCCESS
        }
    }

    object Empty : TestFilter {

        override fun shouldBeEvaluated(test: ExternalLibraryTest): Boolean = true
    }

    data class Intersection(val filters: List<TestFilter>) : TestFilter {

        override fun shouldBeEvaluated(test: ExternalLibraryTest): Boolean =
            filters.all { it.shouldBeEvaluated(test) }
    }
}

operator fun TestFilter.plus(testFilter: TestFilter): TestFilter =
    TestFilter.Intersection(listOf(this, testFilter))
