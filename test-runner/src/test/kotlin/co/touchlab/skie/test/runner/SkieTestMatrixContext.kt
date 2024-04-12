package co.touchlab.skie.test.runner

import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.TestTemplateInvocationContext

class SkieTestMatrixContext(
    private val axisValues: List<SkieTestMatrix.AxisValue>,
): TestTemplateInvocationContext {
    private val runValues = axisValues.associate { it.type to it.value }

    override fun getDisplayName(invocationIndex: Int): String {
        val nameWithoutIndex = if (axisValues.isNotEmpty()) {
            axisValues.joinToString(", ") { it.value.toString() }
        } else {
            "No arguments"
        }
        return "[$invocationIndex]: $nameWithoutIndex"
    }

    override fun getAdditionalExtensions(): List<Extension> {
        return listOf(
            SkieMatrixExtension(runValues),
        )
    }
}
