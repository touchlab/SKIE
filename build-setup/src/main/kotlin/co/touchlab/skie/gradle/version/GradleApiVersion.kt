package co.touchlab.skie.gradle.version

import co.touchlab.skie.gradle.KotlinToolingVersion
import org.gradle.util.GradleVersion

data class GradleApiVersion(
    val gradleVersion: GradleVersion,
    val kotlinVersion: KotlinToolingVersion,
): Comparable<GradleApiVersion> {
    override fun compareTo(other: GradleApiVersion): Int {
        return gradleVersion.compareTo(other.gradleVersion)
    }
}
