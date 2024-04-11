package co.touchlab.skie.test.annotation.filter

import co.touchlab.skie.test.util.RawKotlinTarget

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@OnlyFor(targets = [RawKotlinTarget.macosArm64, RawKotlinTarget.macosX64])
annotation class OnlyMacos
