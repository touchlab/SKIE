package co.touchlab.skie.gradle.version

import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.tooling.core.KotlinToolingVersion

data class GradleApiVersion(
    val gradleVersion: GradleVersion,
    val kotlinVersion: KotlinToolingVersion,
): Comparable<GradleApiVersion> {
    override fun compareTo(other: GradleApiVersion): Int {
        return gradleVersion.compareTo(other.gradleVersion)
    }
}

data class KotlinPluginShimVersion(
    val gradleApiVersion: GradleApiVersion,
    val kotlinToolingVersion: KotlinToolingVersion,
)
