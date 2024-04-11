package co.touchlab.skie.test.annotation.filter

import org.intellij.lang.annotations.Language

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class Exclude(
    @Language("RegExp")
    val targets: String = ".*",
    @Language("RegExp")
    val configurations: String = ".*",
    @Language("RegExp")
    val linkModes: String = ".*",
    @Language("RegExp")
    val kotlinVersions: String = ".*"
)
