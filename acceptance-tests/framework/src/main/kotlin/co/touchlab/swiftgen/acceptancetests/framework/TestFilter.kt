package co.touchlab.swiftgen.acceptancetests.framework

import kotlin.io.path.notExists
import kotlin.io.path.readText

interface TestFilter {

    fun shouldBeEvaluated(test: TestNode.Test, tempFileSystemFactory: TempFileSystemFactory): Boolean

    data class Regex(private val pattern: String) : TestFilter {

        private val regex = kotlin.text.Regex(pattern)

        override fun shouldBeEvaluated(test: TestNode.Test, tempFileSystemFactory: TempFileSystemFactory): Boolean =
            regex.containsMatchIn(test.fullName)
    }

    object FailedOnly : TestFilter {

        override fun shouldBeEvaluated(test: TestNode.Test, tempFileSystemFactory: TempFileSystemFactory): Boolean {
            val resultFile = test.resultPath(tempFileSystemFactory)

            return resultFile.notExists() || resultFile.readText() != ExpectedTestResult.SUCCESS
        }
    }

    object Empty : TestFilter {

        override fun shouldBeEvaluated(test: TestNode.Test, tempFileSystemFactory: TempFileSystemFactory): Boolean = true
    }

    data class Intersection(val filters: List<TestFilter>) : TestFilter {

        override fun shouldBeEvaluated(test: TestNode.Test, tempFileSystemFactory: TempFileSystemFactory): Boolean =
            filters.all { it.shouldBeEvaluated(test, tempFileSystemFactory) }
    }
}

operator fun TestFilter.plus(testFilter: TestFilter): TestFilter =
    TestFilter.Intersection(listOf(this, testFilter))
