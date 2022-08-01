package co.touchlab.swiftgen.acceptancetests.framework

import co.touchlab.swiftgen.acceptancetests.framework.internal.EvaluatedTestNode
import co.touchlab.swiftgen.acceptancetests.framework.internal.TestNodeRunner
import co.touchlab.swiftgen.acceptancetests.framework.internal.TestResult
import io.kotest.assertions.fail
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

            val result = evaluatedTest.result

            outputTestResult(result)
        }
    }

    private fun outputTestResult(result: TestResult) {
        println(result.logs)

        when (result) {
            is TestResult.Success -> {}
            is TestResult.MissingExit -> fail(TestResult.MissingExit.ERROR_MESSAGE)
            is TestResult.IncorrectOutput -> fail(
                """
                Program execution ended with an incorrect exit code.
                Expected: 0
                Actual: ${result.code}
            """.trimIndent()
            )
            is TestResult.RuntimeError -> fail(
                """
                Program execution ended with an error: 
                ${result.error}
            """.trimIndent()
            )
            is TestResult.SwiftCompilationError -> fail(
                """
                Error during Swift compilation:
                ${result.error}
            """.trimIndent()
            )
            is TestResult.KotlinCompilationError -> fail(
                """
                Error during Kotlin compilation:
                ${result.error}
            """.trimIndent()
            )
        }
    }
}
