package co.touchlab.swiftgen.acceptancetests.framework

import co.touchlab.swiftgen.acceptancetests.framework.internal.EvaluatedTestNode
import co.touchlab.swiftgen.acceptancetests.framework.internal.TestNodeRunner
import io.kotest.core.spec.style.scopes.FunSpecContainerScope

class AcceptanceTestsRunner(
    private val tempFileSystem: TempFileSystem,
    private val selectedAcceptanceTest: String?,
) {

    suspend fun runTests(scope: FunSpecContainerScope, testNode: TestNode) {
        val testNodeRunner = TestNodeRunner(tempFileSystem, selectedAcceptanceTest)

        val evaluatedTests = testNodeRunner.runTests(testNode)

        scope.outputEvaluatedTest(evaluatedTests)
    }

    private suspend fun FunSpecContainerScope.outputEvaluatedTest(evaluatedTest: EvaluatedTestNode) {
        when (evaluatedTest) {
            is EvaluatedTestNode.Container -> outputEvaluatedTest(evaluatedTest)
            is EvaluatedTestNode.Test -> outputEvaluatedTest(evaluatedTest)
        }
    }

    private suspend fun FunSpecContainerScope.outputEvaluatedTest(evaluatedTest: EvaluatedTestNode.Container) {
        context(evaluatedTest.name) {
            evaluatedTest.children.forEach {
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
