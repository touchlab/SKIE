package co.touchlab.skie.test.annotation.filter

import co.touchlab.skie.test.filter.OnlyConfigurationCacheMatrixFilter

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
@FilterMatrixWith(OnlyConfigurationCacheMatrixFilter::class)
annotation class OnlyConfigurationCache
