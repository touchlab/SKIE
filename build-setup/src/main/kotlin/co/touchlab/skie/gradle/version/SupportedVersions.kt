package co.touchlab.skie.gradle.version

import co.touchlab.skie.gradle.util.stringListProperty
import co.touchlab.skie.gradle.version.target.DimensionWithAliases
import co.touchlab.skie.gradle.version.target.Target
import org.gradle.api.Project

fun Project.gradleApiVersionDimension(): Target.Dimension<GradleApiVersionComponent> {
    val requestedIdentifiers = project.stringListProperty("versionSupport.gradleApi")
    return ToolingVersions.Gradle.dimensionFrom(requestedIdentifiers)
}

/**
 * Supported format:
 * versionSupport.kotlinTooling=1.8.0, 1.8.20(1.8.21, 1.8.22), 1.9.0[1.9.0-RC](1.9.10), 1.9.20[1.9.20-RC]
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

fun Project.darwinPlatformDimension(): Target.Dimension<DarwinPlatformComponent> {
    return DimensionWithAliases(
        name = null,
        commonName = "darwin",
        components = DarwinPlatformComponent.values().toSet(),
        aliases = mapOf(
            "ios" to setOf(
//                 DarwinPlatformComponent.iosArm32,
                DarwinPlatformComponent.iosArm64,
                DarwinPlatformComponent.iosX64,
                DarwinPlatformComponent.iosSimulatorArm64,
            ),
            "watchos" to setOf(
                DarwinPlatformComponent.watchosArm32,
                DarwinPlatformComponent.watchosArm64,
                DarwinPlatformComponent.watchosDeviceArm64,
//                 DarwinPlatformComponent.watchosX86,
                DarwinPlatformComponent.watchosX64,
                DarwinPlatformComponent.watchosSimulatorArm64,
            ),
            "tvos" to setOf(
                DarwinPlatformComponent.tvosArm64,
                DarwinPlatformComponent.tvosX64,
                DarwinPlatformComponent.tvosSimulatorArm64,
            ),
            "macos" to setOf(
                DarwinPlatformComponent.macosX64,
                DarwinPlatformComponent.macosArm64,
            ),
        ),
    )
}

fun Project.acceptanceTestsDimension(): Target.Dimension<AcceptanceTestsComponent> {
    return DimensionWithAliases(
        name = null,
        commonName = "all-tests",
        components = AcceptanceTestsComponent.values().toSet(),
        aliases = emptyMap(),
    )
}
