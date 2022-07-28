package co.touchlab.swiftgen.sealed.acceptancetests.framework.internal

import co.touchlab.swiftgen.sealed.acceptancetests.framework.TempFileSystem
import co.touchlab.swiftgen.sealed.acceptancetests.framework.TestNode
import co.touchlab.swiftgen.sealed.acceptancetests.framework.internal.testrunner.TestRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

internal class TestNodeRunner(
    tempFileSystem: TempFileSystem,
    private val selectedAcceptanceTest: String?,
) {

    private val testRunner = TestRunner(tempFileSystem)

    private val parallelismChunkSize: Int = Runtime.getRuntime().availableProcessors() * 4

    suspend fun runTests(testNode: TestNode): EvaluatedTestNode {
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
        get() = selectedAcceptanceTest?.let { this.fullName.startsWith(it) } ?: true

    private suspend fun runTests(tests: List<TestNode.Test>): Map<TestNode.Test, TestResult> =
        tests
            .chunked(parallelismChunkSize)
            .flatMap { chunkedTests ->
                withContext(Dispatchers.Default) {
                    chunkedTests
                        .map { async { it to testRunner.runTest(it) } }
                        .awaitAll()
                }
            }
            .toMap()

    private fun mapEvaluatedTests(
        evaluatedTests: Map<TestNode.Test, TestResult>,
        testNode: TestNode
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
    ): EvaluatedTestNode.Test? =
        evaluatedTests[test]?.let { EvaluatedTestNode.Test(test.name, test.fullName, test.path, it) }
}
