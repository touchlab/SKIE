package co.touchlab.skie.test.annotation.filter

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Smoke
annotation class SmokeOnly
