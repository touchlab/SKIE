package co.touchlab.swiftgen.sealed.acceptancetests.framework.internal

import java.nio.file.Path

internal sealed interface EvaluatedTestNode {

    val name: String

    data class Test(
        override val name: String,
        val fullName: String,
        val path: Path,
        val result: TestResult,
    ) : EvaluatedTestNode

    data class Container(
        override val name: String,
        val children: List<EvaluatedTestNode>,
    ) : EvaluatedTestNode
}
