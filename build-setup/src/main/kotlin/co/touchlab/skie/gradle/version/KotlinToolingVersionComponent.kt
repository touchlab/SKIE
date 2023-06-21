package co.touchlab.skie.gradle.version

import co.touchlab.skie.gradle.KotlinToolingVersion
import co.touchlab.skie.gradle.version.target.Target
import co.touchlab.skie.gradle.version.target.SourceSet

data class KotlinToolingVersionComponent(
    val version: KotlinToolingVersion,
): Target.Component, Comparable<KotlinToolingVersionComponent> {
    override val value: String = version.toString()
    override fun compareTo(other: KotlinToolingVersionComponent): Int = version.compareTo(other.version)
}

val Target.kotlinToolingVersion: KotlinToolingVersionComponent
    get() = component<KotlinToolingVersionComponent>()

val SourceSet.kotlinToolingVersion: KotlinToolingVersionComponent
    get() = when (val componentSet = componentSet<KotlinToolingVersionComponent>()) {
        is SourceSet.ComponentSet.Common -> componentSet.components.min()
        is SourceSet.ComponentSet.Enumerated -> componentSet.components.min()
        is SourceSet.ComponentSet.Specific -> componentSet.component
    }
