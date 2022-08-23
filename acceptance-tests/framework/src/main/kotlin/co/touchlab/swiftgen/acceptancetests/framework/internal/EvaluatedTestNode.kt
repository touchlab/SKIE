package co.touchlab.swiftgen.acceptancetests.framework.internal

import co.touchlab.swiftgen.acceptancetests.framework.ExpectedTestResult
import co.touchlab.swiftgen.acceptancetests.framework.TestResult
import java.nio.file.Path

internal sealed interface EvaluatedTestNode {

    val name: String

    data class Test(
        override val name: String,
        val fullName: String,
        val path: Path,
        val configurationChanges: Map<String, String>,
        val expectedResult: ExpectedTestResult,
        val actualResult: TestResult,
    ) : EvaluatedTestNode

    data class Container(
        override val name: String,
        val children: List<EvaluatedTestNode>,
    ) : EvaluatedTestNode
}
