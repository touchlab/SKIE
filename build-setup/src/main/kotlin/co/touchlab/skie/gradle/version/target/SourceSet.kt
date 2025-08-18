package co.touchlab.skie.gradle.version.target

import kotlin.reflect.KClass

data class SourceSet(
    val name: String,
    val components: List<ComponentSet<out Target.Component>>,
    val sourceDirs: List<RelativePath>,
    val target: Target? = null,
    val isRoot: Boolean = false,
) {

    @PublishedApi
    internal val componentsByType: Map<KClass<out Target.Component>, ComponentSet<*>> = components.associateBy { it.componentType }
        .also { check(it.size == components.size) { "Duplicate component types in $components!" } }

    inline fun <reified COMPONENT : Target.Component> componentSet(): ComponentSet<COMPONENT> {
        @Suppress("UNCHECKED_CAST")
        return componentsByType[COMPONENT::class] as ComponentSet<COMPONENT>
    }

    val isTarget: Boolean = target != null

    val isIntermediate: Boolean = !isTarget && !isRoot

    fun shouldDependOn(other: SourceSet): Boolean {
        return when {
            this == other -> false
            other.isRoot -> true
            other.isIntermediate -> components.zip(other.components) { thisComponent, otherComponent ->
                thisComponent.shouldDependOn(otherComponent)
            }.all { it }
            else -> false
        }
    }

    data class Directory(
        val name: String,
        val components: ComponentSet<out Target.Component>,
        val children: List<Directory>,
    )

    sealed interface ComponentSet<COMPONENT : Target.Component> {

        val name: String
        val dimension: Target.Dimension<COMPONENT>
        val componentType: KClass<out COMPONENT>
        val components: Set<COMPONENT>

        fun isEmpty(): Boolean = components.isEmpty()

        fun shouldDependOn(other: ComponentSet<*>): Boolean

        fun withErasedIdentity(): ComponentSet<COMPONENT> = Enumerated(
            name = components.joinToString(",") { it.value },
            dimension = dimension,
            components = components,
        )

        data class Common<COMPONENT : Target.Component>(
            override val name: String,
            override val dimension: Target.Dimension<COMPONENT>,
            override val components: Set<COMPONENT>,
        ) : ComponentSet<COMPONENT> {

            constructor(name: String, dimension: Target.Dimension<COMPONENT>) : this(name, dimension, dimension.components)

            override val componentType: KClass<out COMPONENT> = components.first()::class

            override fun shouldDependOn(other: ComponentSet<*>): Boolean = componentType == other.componentType && when (other) {
                is Common -> true
                is Enumerated -> false
                is Specific -> false
            }
        }

        data class Enumerated<COMPONENT : Target.Component>(
            override val name: String,
            override val dimension: Target.Dimension<COMPONENT>,
            override val components: Set<COMPONENT>,
        ) : ComponentSet<COMPONENT> {

            override val componentType: KClass<out COMPONENT> = components.first()::class

            override fun shouldDependOn(other: ComponentSet<*>): Boolean = componentType == other.componentType && when (other) {
                is Common -> true
                is Enumerated -> components != other.components && other.components.containsAll(components)
                is Specific -> false
            }
        }

        data class Specific<COMPONENT : Target.Component>(
            override val name: String,
            override val dimension: Target.Dimension<COMPONENT>,
            val component: COMPONENT,
        ) : ComponentSet<COMPONENT> {

            override val componentType: KClass<out COMPONENT> = component::class

            override val components: Set<COMPONENT> = setOf(component)

            override fun shouldDependOn(other: ComponentSet<*>): Boolean = componentType == other.componentType && when (other) {
                is Common -> true
                is Enumerated -> other.components.contains(component)
                is Specific -> other.component == component
            }

            companion object {

                fun <COMPONENT : Target.Component> unsafe(
                    name: String,
                    dimension: Target.Dimension<COMPONENT>,
                    component: Target.Component,
                ): Specific<COMPONENT> {
                    return Specific(name, dimension, component as COMPONENT)
                }
            }
        }
    }
}
