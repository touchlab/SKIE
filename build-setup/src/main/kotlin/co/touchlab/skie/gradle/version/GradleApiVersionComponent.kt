package co.touchlab.skie.gradle.version

import co.touchlab.skie.gradle.version.GradleApiVersion
import co.touchlab.skie.gradle.version.target.Target
import co.touchlab.skie.gradle.version.target.SourceSet

data class GradleApiVersionComponent(
    val version: GradleApiVersion,
): Target.Component, Comparable<GradleApiVersionComponent> {
    override val value: String = version.gradleVersion.version

    override fun compareTo(other: GradleApiVersionComponent): Int = version.compareTo(other.version)
}

val Target.gradleApiVersion: GradleApiVersionComponent
    get() = component<GradleApiVersionComponent>()
val SourceSet.gradleApiVersion: GradleApiVersionComponent
    get() = components<GradleApiVersionComponent>().min()
