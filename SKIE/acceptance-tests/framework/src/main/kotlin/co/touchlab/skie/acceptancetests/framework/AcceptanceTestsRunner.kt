package co.touchlab.skie.acceptancetests.framework

import co.touchlab.skie.acceptance_tests_framework.BuildConfig
import co.touchlab.skie.acceptancetests.framework.internal.EvaluatedTestNode
import co.touchlab.skie.acceptancetests.framework.internal.TestNodeRunner
import co.touchlab.skie.acceptancetests.framework.util.isCI
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.engine.concurrency.TestExecutionMode
import kotlinx.coroutines.channels.Channel

class AcceptanceTestsRunner(
    private val tempFileSystemFactory: TempFileSystemFactory,
    private val testFilter: TestFilter = TestFilter.Empty,
) {

    init {
        System.setProperty("konan.home", BuildConfig.KONAN_HOME)
    }

    @OptIn(ExperimentalKotest::class)
    fun runTests(scope: FunSpec, testNode: TestNode) {
        val channel = Channel<EvaluatedTestNode>()

        scope.testExecutionMode = TestExecutionMode.LimitedConcurrency(2)

        scope.test("Evaluation") {
            try {
                val testNodeRunner = TestNodeRunner(tempFileSystemFactory, testFilter)

                val evaluatedTests = testNodeRunner.runTests(testNode)

                channel.send(evaluatedTests)
            } finally {
                channel.close()
            }
        }

        scope.context("Results") {
            val evaluatedTests = channel.receive()

            outputEvaluatedTest(evaluatedTests)

            printFailedTests(evaluatedTests)
        }
    }

    private suspend fun FunSpecContainerScope.outputEvaluatedTest(evaluatedTest: EvaluatedTestNode) {
        when (evaluatedTest) {
            is EvaluatedTestNode.Container -> outputEvaluatedTest(evaluatedTest)
            is EvaluatedTestNode.Test -> outputEvaluatedTest(evaluatedTest)
            is EvaluatedTestNode.SkippedTest -> outputEvaluatedTest(evaluatedTest)
        }
    }

    private suspend fun FunSpecContainerScope.outputEvaluatedTest(evaluatedTest: EvaluatedTestNode.Container) {
        context(evaluatedTest.name) {
            evaluatedTest.children
                .sortedBy { it.name }
                .forEach {
                    outputEvaluatedTest(it)
                }
        }
    }

    private suspend fun FunSpecContainerScope.outputEvaluatedTest(evaluatedTest: EvaluatedTestNode.Test) {
        test(evaluatedTest.name) {
            if (!isCI) {
                print(evaluatedTest.actualResultWithLogs.logs)
            }

            evaluatedTest.outputResult()
        }
    }

    private suspend fun FunSpecContainerScope.outputEvaluatedTest(evaluatedTest: EvaluatedTestNode.SkippedTest) {
        xtest(evaluatedTest.name) {
        }
    }

    private fun printFailedTests(evaluatedTests: EvaluatedTestNode) {
        val failedTests = evaluatedTests.filterFailedTests()

        if (failedTests.isNotEmpty()) {
            println("${failedTests.size} tests failed out.")
            println()

            println("Failed tests:")
            failedTests.forEach {
                println("    " + it.fullName)
            }
            println()

            println("To rerun failed tests use:")
            failedTests.forEach {
                println("acceptanceTest=${it.fullName}")
            }
        }
    }

    private fun EvaluatedTestNode.filterFailedTests(): List<EvaluatedTestNode.Test> =
        when (this) {
            is EvaluatedTestNode.Container -> children.flatMap { it.filterFailedTests() }
            is EvaluatedTestNode.Test -> if (hasSucceeded()) emptyList() else listOf(this)
            is EvaluatedTestNode.SkippedTest -> emptyList()
        }
}
