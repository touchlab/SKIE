package co.touchlab.skie.gradle.version.target

import kotlin.reflect.KClass

data class SourceSet(
    val name: String,
    val components: List<Set<Target.Component>>,
    val sourceDirs: List<RelativePath>,
    val isRoot: Boolean = false,
) {
    @PublishedApi
    internal val componentsByType: Map<KClass<out Target.Component>, Set<Target.Component>> = components.associateBy { it.first()::class }
        .also { check(it.size == components.size) { "Duplicate component types in $components!" } }

    inline fun <reified COMPONENT: Target.Component> components(): Set<COMPONENT> {
        return componentsByType[COMPONENT::class] as Set<COMPONENT>
    }

    data class Directory(
        val name: String,
        val components: Target.ComponentsInDimension<Target.Component>,
        val children: List<Directory>,
    )
}
