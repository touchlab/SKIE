package co.touchlab.skie.test.annotation.type

import co.touchlab.skie.test.runner.TestType
import co.touchlab.skie.test.runner.condition.TestTypeCondition
import org.junit.jupiter.api.extension.ExtendWith

@Target(AnnotationTarget.CLASS)
@ExtendWith(TestTypeCondition::class)
@Repeatable
annotation class SkieTestType(val value: TestType)
