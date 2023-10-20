package co.touchlab.skie.gradle.version

import co.touchlab.skie.gradle.version.target.SourceSet
import co.touchlab.skie.gradle.version.target.Target

data class GradleApiVersionComponent(
    val version: GradleApiVersion,
) : Target.Component, Comparable<GradleApiVersionComponent> {

    override val value: String = version.gradleVersion.version

    override fun compareTo(other: GradleApiVersionComponent): Int = version.compareTo(other.version)
}

val Target.gradleApiVersion: GradleApiVersionComponent
    get() = component<GradleApiVersionComponent>()

val SourceSet.gradleApiVersion: GradleApiVersionComponent
    get() = when (val componentSet = componentSet<GradleApiVersionComponent>()) {
        is SourceSet.ComponentSet.Common -> componentSet.components.min()
        is SourceSet.ComponentSet.Enumerated -> componentSet.components.min()
        is SourceSet.ComponentSet.Specific -> componentSet.component
    }
