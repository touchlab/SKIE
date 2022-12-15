package co.touchlab.skie.acceptancetests.framework.internal

import co.touchlab.skie.acceptancetests.framework.ExpectedTestResult
import co.touchlab.skie.acceptancetests.framework.TestResultWithLogs

internal sealed interface EvaluatedTestNode {

    val name: String

    data class Test(
        override val name: String,
        val expectedResult: ExpectedTestResult,
        val actualResultWithLogs: TestResultWithLogs,
    ) : EvaluatedTestNode {

        fun outputResult() {
            expectedResult.shouldBe(actualResultWithLogs)
        }
    }

    data class SkippedTest(
        override val name: String,
    ) : EvaluatedTestNode

    data class Container(
        override val name: String,
        val children: List<EvaluatedTestNode>,
    ) : EvaluatedTestNode
}
