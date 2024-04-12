package co.touchlab.skie.test.runner.condition

import co.touchlab.skie.test.annotation.type.SkieTestType
import co.touchlab.skie.test.runner.SkieTestRunnerConfiguration
import org.junit.jupiter.api.extension.ConditionEvaluationResult
import org.junit.jupiter.api.extension.ExecutionCondition
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.commons.util.AnnotationUtils

class TestTypeCondition: ExecutionCondition {
    override fun evaluateExecutionCondition(context: ExtensionContext): ConditionEvaluationResult {
        val testTypes = AnnotationUtils.findRepeatableAnnotations(context.testClass, SkieTestType::class.java).map { it.value }.toSet()
        return if (SkieTestRunnerConfiguration.testTypes.containsAll(testTypes)) {
            ConditionEvaluationResult.enabled("${context.element} ")
        } else {
            ConditionEvaluationResult.disabled("${context.element} is marked as @Smoke test")
        }
    }

}
