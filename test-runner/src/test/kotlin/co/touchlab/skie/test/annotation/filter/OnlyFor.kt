package co.touchlab.skie.test.annotation.filter

import co.touchlab.skie.test.filter.OnlyForMatrixFilter
import co.touchlab.skie.test.runner.BuildConfiguration
import co.touchlab.skie.test.util.LinkMode
import co.touchlab.skie.test.util.RawKotlinTarget
import org.intellij.lang.annotations.Language

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
@FilterMatrixWith(OnlyForMatrixFilter::class)
annotation class OnlyFor(
    val targets: Array<RawKotlinTarget> = [],
    val configurations: Array<BuildConfiguration> = [],
    val linkModes: Array<LinkMode> = [],
    val kotlinVersions: Array<String> = [],
)
