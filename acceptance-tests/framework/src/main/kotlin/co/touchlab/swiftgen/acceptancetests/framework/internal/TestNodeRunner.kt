package co.touchlab.swiftgen.acceptancetests.framework.internal

import co.touchlab.swiftgen.acceptancetests.framework.TempFileSystemFactory
import co.touchlab.swiftgen.acceptancetests.framework.TestNode
import co.touchlab.swiftgen.acceptancetests.framework.TestResult
import co.touchlab.swiftgen.acceptancetests.framework.internal.testrunner.TestRunner
import kotlin.streams.toList

internal class TestNodeRunner(
    tempFileSystemFactory: TempFileSystemFactory,
    selectedAcceptanceTestRegexPattern: String?,
) {

    private val testRunner = TestRunner(tempFileSystemFactory)

    private val selectedAcceptanceTestRegex = selectedAcceptanceTestRegexPattern?.let { Regex(it) }

    fun runTests(testNode: TestNode): EvaluatedTestNode {
        val tests = testNode.flatten()

        val testsWithResults = runTests(tests)

        return mapEvaluatedTests(testsWithResults, testNode) ?: EvaluatedTestNode.Container(
            "No tests",
            emptyList(),
        )
    }

    private fun TestNode.flatten(): List<TestNode.Test> = when (this) {
        is TestNode.Container -> this.directChildren.flatMap { it.flatten() }
        is TestNode.Test -> if (this.shouldBeEvaluated) listOf(this) else emptyList()
    }

    private val TestNode.shouldBeEvaluated: Boolean
        get() = selectedAcceptanceTestRegex?.containsMatchIn(this.fullName) ?: true

    private fun runTests(tests: List<TestNode.Test>): Map<TestNode.Test, TestResult> =
        tests
            .parallelStream()
            .map { it to testRunner.runTest(it) }
            .toList()
            .toMap()

    private fun mapEvaluatedTests(
        evaluatedTests: Map<TestNode.Test, TestResult>,
        testNode: TestNode,
    ): EvaluatedTestNode? = when (testNode) {
        is TestNode.Container -> mapEvaluatedTests(evaluatedTests, testNode)
        is TestNode.Test -> mapEvaluatedTests(evaluatedTests, testNode)
    }

    private fun mapEvaluatedTests(
        evaluatedTests: Map<TestNode.Test, TestResult>,
        container: TestNode.Container,
    ): EvaluatedTestNode.Container? {
        val children = container.directChildren.mapNotNull { mapEvaluatedTests(evaluatedTests, it) }

        if (children.isEmpty()) {
            return null
        }

        return EvaluatedTestNode.Container(container.name, children)
    }

    private fun mapEvaluatedTests(
        evaluatedTests: Map<TestNode.Test, TestResult>,
        test: TestNode.Test,
    ): EvaluatedTestNode.Test? = evaluatedTests[test]?.let {
        EvaluatedTestNode.Test(
            name = test.name,
            fullName = test.fullName,
            path = test.path,
            expectedResult = test.expectedResult,
            actualResult = it,
        )
    }
}
