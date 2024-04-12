package co.touchlab.skie.test.annotation.filter

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class FilterMatrixWith(
    val value: KClass<out MatrixFilter>
)
