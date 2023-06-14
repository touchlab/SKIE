package co.touchlab.skie.gradle.version

import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.tooling.core.KotlinToolingVersion

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

        fun dependencyAxisFrom(requestedIdentifiers: List<String>): KotlinToolingVersionAxis {
            return KotlinToolingVersionAxis(resolve(requestedIdentifiers))
        }
    }

    object Gradle: VersionContainer<GradleApiVersion>(identifier = { it.gradleVersion.version }) {
        val `7․3` by version(Kotlin.`1․8․0`)
        val `7․5` by version(Kotlin.`1․6․21`)
        val `7․6` by version(Kotlin.`1․7․10`)
        val `8․0` by version(Kotlin.`1․8․10`)

        fun version(kotlin: KotlinToolingVersion) = VersionProvider { gradle ->
            GradleApiVersion(GradleVersion.version(gradle), kotlin)
        }

        fun dependencyAxisFrom(requestedIdentifiers: List<String>): GradleApiVersionAxis {
            return GradleApiVersionAxis(resolve(requestedIdentifiers))
        }
    }
}
