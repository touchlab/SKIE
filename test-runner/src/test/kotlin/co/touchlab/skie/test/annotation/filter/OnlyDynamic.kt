package co.touchlab.skie.test.annotation.filter

import co.touchlab.skie.test.util.LinkMode

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@OnlyFor(linkModes = [LinkMode.Dynamic])
annotation class OnlyDynamic
