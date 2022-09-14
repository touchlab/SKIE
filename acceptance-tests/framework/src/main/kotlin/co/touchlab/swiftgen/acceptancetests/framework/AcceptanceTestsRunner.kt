package co.touchlab.swiftgen.acceptancetests.framework

import co.touchlab.swiftgen.acceptancetests.framework.internal.EvaluatedTestNode
import co.touchlab.swiftgen.acceptancetests.framework.internal.TestNodeRunner
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import kotlinx.coroutines.channels.Channel

class AcceptanceTestsRunner(
    private val tempFileSystemFactory: TempFileSystemFactory,
    private val selectedAcceptanceTestRegexPattern: String?,
) {

    fun runTests(scope: FunSpec, testNode: TestNode) {
        val channel = Channel<EvaluatedTestNode>()

        scope.concurrency = 2

        scope.test("Evaluation") {
            val testNodeRunner = TestNodeRunner(tempFileSystemFactory, selectedAcceptanceTestRegexPattern)

            val evaluatedTests = testNodeRunner.runTests(testNode)

            channel.send(evaluatedTests)
            channel.close()
        }

        scope.context("Results") {
            val evaluatedTests = channel.receive()

            outputEvaluatedTest(evaluatedTests)
        }
    }

    private suspend fun FunSpecContainerScope.outputEvaluatedTest(evaluatedTest: EvaluatedTestNode) {
        when (evaluatedTest) {
            is EvaluatedTestNode.Container -> outputEvaluatedTest(evaluatedTest)
            is EvaluatedTestNode.Test -> outputEvaluatedTest(evaluatedTest)
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
            println(evaluatedTest.path.toAbsolutePath())
            println(evaluatedTest.fullName)
            println("To run only this test add env variable: acceptanceTest=${evaluatedTest.fullName}")
            println()

            outputTestResult(evaluatedTest)
        }
    }

    private fun outputTestResult(evaluatedTest: EvaluatedTestNode.Test) {
        val result = evaluatedTest.actualResult

        print(result.logs)

        evaluatedTest.expectedResult.evaluate(evaluatedTest.actualResult)
    }
}
