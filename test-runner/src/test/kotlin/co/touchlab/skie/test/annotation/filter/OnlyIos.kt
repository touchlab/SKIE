package co.touchlab.skie.test.annotation.filter

import co.touchlab.skie.test.util.RawKotlinTarget

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@OnlyFor(targets = [RawKotlinTarget.iosSimulatorArm64, RawKotlinTarget.iosArm64, RawKotlinTarget.iosX64])
annotation class OnlyIos
