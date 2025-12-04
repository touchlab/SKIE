package co.touchlab.skie.test.annotation.filter

import co.touchlab.skie.test.filter.OnlyKotlinUpToFilter

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
@FilterMatrixWith(OnlyKotlinUpToFilter::class)
annotation class OnlyKotlinUpTo(
    val major: Int,
    val minor: Int = -1,
    val patch: Int = -1,
)
