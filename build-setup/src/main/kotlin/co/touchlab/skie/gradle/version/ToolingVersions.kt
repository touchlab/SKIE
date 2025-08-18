package co.touchlab.skie.gradle.version

import co.touchlab.skie.gradle.KotlinToolingVersion
import co.touchlab.skie.gradle.version.target.ComparableDimension
import co.touchlab.skie.gradle.version.target.Target

// WARN: WE'RE USING A "ONE DOT LEADER" (U+2024 ․) IN THE VERSION NAMES BECAUSE WE CAN'T USE A PERIOD (U+002E) IN A PROPERTY NAME.
@Suppress("NonAsciiCharacters")
object ToolingVersions {

    object Kotlin : VersionContainer<KotlinToolingVersion>(KotlinToolingVersion::toString) {

        val `1․5․31` by version()
        val `1․6․21` by version()
        val `1․7․10` by version()
        val `1․8․0` by version()
        val `1․8․10` by version()
        val `1․8․20` by version()
        val `1․9․0` by version()
        val `1․9․20` by version()
        val `2․0․0` by version()

        fun version() = VersionProvider(::KotlinToolingVersion)

        /**
         * ```
         * 1.8.0(1.8.10)
         * 1.8.20(1.8.21, 1.8.22)
         * 1.9.0(1.9.10)
         * 1.9.20[1.9.20-RC]
         * 1.9.20[1.9.20-RC](1.9.20)
         * ```
         */
        private val kotlinVersionRegex = "([^\\[(\\])]+)(?:\\[([^\\[(\\])]+)\\])?(?:\\(([^\\[(\\])]+)\\))?".toRegex()

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
