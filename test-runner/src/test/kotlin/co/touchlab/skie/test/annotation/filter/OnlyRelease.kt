package co.touchlab.skie.test.annotation.filter

import co.touchlab.skie.test.runner.BuildConfiguration

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@OnlyFor(configurations = [BuildConfiguration.Release])
annotation class OnlyRelease
