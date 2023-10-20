package co.touchlab.skie.gradle.version

import co.touchlab.skie.gradle.KotlinToolingVersion
import co.touchlab.skie.gradle.version.target.SourceSet
import co.touchlab.skie.gradle.version.target.Target

data class KotlinToolingVersionComponent(
    val name: KotlinToolingVersion,
    // Can be different from name to allow for testing code of the given target against a different compiler version without having to rename everything.
    val primaryVersion: KotlinToolingVersion,
    val otherSupportedVersions: List<KotlinToolingVersion>,
) : Target.Component, Comparable<KotlinToolingVersionComponent> {

    val supportedVersions: List<KotlinToolingVersion> =
        listOf(primaryVersion) + otherSupportedVersions

    override val value: String = name.toString()

    override fun compareTo(other: KotlinToolingVersionComponent): Int = name.compareTo(other.name)
}

val Target.kotlinToolingVersion: KotlinToolingVersionComponent
    get() = component<KotlinToolingVersionComponent>()

val SourceSet.kotlinToolingVersion: KotlinToolingVersionComponent
    get() = when (val componentSet = componentSet<KotlinToolingVersionComponent>()) {
        is SourceSet.ComponentSet.Common -> componentSet.components.min()
        is SourceSet.ComponentSet.Enumerated -> componentSet.components.min()
        is SourceSet.ComponentSet.Specific -> componentSet.component
    }
