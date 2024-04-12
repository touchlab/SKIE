package co.touchlab.skie.test.annotation

import co.touchlab.skie.test.runner.SkieTestRunner
import co.touchlab.skie.test.runner.condition.SmokeTestCondition
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith

@TestTemplate
@ExtendWith(SkieTestRunner::class)
@ExtendWith(SmokeTestCondition::class)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MatrixTest
