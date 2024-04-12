package co.touchlab.skie.test.runner.condition

import co.touchlab.skie.test.annotation.filter.Smoke
import co.touchlab.skie.test.annotation.filter.SmokeOnly
import co.touchlab.skie.test.runner.SkieTestRunnerConfiguration
import co.touchlab.skie.test.runner.TestLevel
import co.touchlab.skie.test.util.isTestMethodOrClassAnnotated
import org.junit.jupiter.api.extension.ConditionEvaluationResult
import org.junit.jupiter.api.extension.ExecutionCondition
import org.junit.jupiter.api.extension.ExtensionContext

class SmokeTestCondition: ExecutionCondition {
    override fun evaluateExecutionCondition(context: ExtensionContext): ConditionEvaluationResult {
        return when (SkieTestRunnerConfiguration.testLevel) {
            TestLevel.Smoke -> if (isTestMethodOrClassAnnotated<Smoke>(context)) {
                ConditionEvaluationResult.enabled("${context.element} is marked as @Smoke test")
            } else {
                ConditionEvaluationResult.disabled("${context.element} is not marked as @Smoke test")
            }
            TestLevel.Thorough -> if (isTestMethodOrClassAnnotated<SmokeOnly>(context)) {
                ConditionEvaluationResult.disabled("${context.element} is marked as @SmokeOnly test")
            } else {
                ConditionEvaluationResult.enabled("${context.element} is not marked as @SmokeOnly test")
            }
        }
    }
}
