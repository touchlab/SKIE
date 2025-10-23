package co.touchlab.skie.gradle.version

import co.touchlab.skie.buildsetup.util.version.KotlinToolingVersion
import co.touchlab.skie.gradle.version.target.ComparableDimension
import co.touchlab.skie.gradle.version.target.Target

object ToolingVersions {

    object Kotlin {

        /**
         * ```
         * 1.8.0(1.8.10)
         * 1.8.20(1.8.21, 1.8.22)
         * 1.9.0(1.9.10)
         * 1.9.20[1.9.20-RC]
         * 1.9.20[1.9.20-RC](1.9.20)
         * ```
         */
        private val kotlinVersionRegex = "([^\\[(\\])]+)(?:\\[([^\\[(\\])]+)])?(?:\\(([^\\[(\\])]+)\\))?".toRegex()

        fun dimensionFrom(requestedIdentifiers: List<String>): Target.Dimension<KotlinToolingVersionComponent> {
            val components = requestedIdentifiers
                .map { it.trim() }
                .map { identifier ->
                    val match = kotlinVersionRegex.matchEntire(identifier) ?: error("Invalid Kotlin version identifier: $identifier")

                    val name = match.groups[1]?.value?.let(::KotlinToolingVersion)
                        ?: error("Invalid Kotlin version identifier - missing name: $identifier")

                    val primaryVersion = match.groups[2]?.value?.let(::KotlinToolingVersion) ?: name
                    val otherSupportedVersions = match.groups[3]?.value
                        ?.split(",")
                        ?.map { it.trim() }
                        ?.map(::KotlinToolingVersion)
                        ?: emptyList()

                    KotlinToolingVersionComponent(name, primaryVersion, otherSupportedVersions)
                }

            return ComparableDimension(
                name = "kgp",
                commonName = "common",
                components = components.toSet(),
                aliases = emptyMap(),
            )
        }
    }
}
