package co.touchlab.skie.gradle.version

import co.touchlab.skie.gradle.KotlinToolingVersion
import co.touchlab.skie.gradle.version.target.*
import co.touchlab.skie.gradle.version.target.Target
import org.gradle.util.GradleVersion

// WARN: WE'RE USING A "ONE DOT LEADER" (U+2024 ․) IN THE VERSION NAMES BECAUSE WE CAN'T USE A PERIOD (U+002E) IN A PROPERTY NAME.
@Suppress("NonAsciiCharacters")
object ToolingVersions {
    object Kotlin: VersionContainer<KotlinToolingVersion>(KotlinToolingVersion::toString) {
        val `1․5․31` by version()
        val `1․6․21` by version()
        val `1․7․10` by version()
        val `1․8․0` by version()
        val `1․8․10` by version()
        val `1․8․20` by version()

        fun version() = VersionProvider(::KotlinToolingVersion)

        fun dimensionFrom(requestedIdentifiers: List<String>): Target.Dimension<KotlinToolingVersionComponent> {
            return ComparableDimension(
                name = "kgp",
                commonName = "common",
                components = resolve(requestedIdentifiers).map { KotlinToolingVersionComponent(it) }.toSet(),
                aliases = emptyMap(),
            )
        }
    }

    object Gradle: VersionContainer<GradleApiVersion>(identifier = { it.gradleVersion.version }) {
        val `7․3` by version(Kotlin.`1․8․0`, "3.0.9")
        val `7․5` by version(Kotlin.`1․6․21`, "3.0.10")
        val `7․6` by version(Kotlin.`1․7․10`, "3.0.13")
        val `8․0` by version(Kotlin.`1․8․10`, "3.0.13")
        val `8․1` by version(Kotlin.`1․8․10`, "3.0.15")

        fun version(kotlin: KotlinToolingVersion, groovy: String) = VersionProvider { gradle ->
            GradleApiVersion(GradleVersion.version(gradle), kotlin, groovy)
        }

        fun dimensionFrom(requestedIdentifiers: List<String>): Target.Dimension<GradleApiVersionComponent> {
            return ComparableDimension(
                name = "gradle",
                commonName = "common",
                components = resolve(requestedIdentifiers).map { GradleApiVersionComponent(it) }.toSet(),
                aliases = emptyMap(),
            )
        }
    }
}
