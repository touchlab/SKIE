package co.touchlab.skie.gradle.version

import co.touchlab.skie.gradle.version.target.DimensionWithAliases
import co.touchlab.skie.gradle.version.target.Target
import org.gradle.api.Project

/**
 * Supported format:
 * ```
 * versionSupport.kotlinTooling=1.8.0, 1.8.20(1.8.21, 1.8.22), 1.9.0[1.9.0-RC](1.9.10), 1.9.20[1.9.20-RC]
 * ```
 *
 * The first version is used as the name of the component and as the primary version.
 * The [] parentheses can be used to override the primary version which can be useful for testing code
 * of the given target against a different compiler version without having to rename everything.
 *
 * The () parentheses can be used to specify other supported versions (the primary version is supported by default).
 */
fun Project.kotlinToolingVersionDimension(): Target.Dimension<KotlinToolingVersionComponent> {
    val requestedIdentifiers = project.property("versionSupport.kotlinTooling") as String

    val separatedIdentifiers = mutableListOf<String>()
    var isInParentheses = false
    var from = 0
    var to = 0

    fun cutNext() {
        val identifier = requestedIdentifiers.substring(from, to).trim()
        separatedIdentifiers.add(identifier)
        from = to + 1
    }

    while (to <= requestedIdentifiers.lastIndex) {
        when (requestedIdentifiers[to]) {
            ',' -> {
                if (!isInParentheses) {
                    cutNext()
                }
            }
            '(' -> isInParentheses = true
            ')' -> isInParentheses = false
        }

        to++
    }

    cutNext()

    return ToolingVersions.Kotlin.dimensionFrom(separatedIdentifiers)
}
