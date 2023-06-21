package co.touchlab.skie.gradle.version.target

import kotlin.reflect.KClass

class Target(
    val name: String,
    val components: Tuple<Component>,
) {
    @PublishedApi
    internal val componentsByType: Map<KClass<out Component>, Component> = components.associateBy { it::class }
        .also { check(it.size == components.size) { "Duplicate component types in $components!" } }

    inline fun <reified COMPONENT: Component> component(): COMPONENT {
        return componentsByType[COMPONENT::class] as COMPONENT
    }

    sealed interface Dimension<COMPONENT: Component> {
        val name: String?
        val commonName: String
        val components: Set<COMPONENT>
        val componentsWithDimension: Set<ComponentInDimension<COMPONENT>>
        val prefix: String

        fun parse(string: String): SourceSet.ComponentSet<COMPONENT>?
    }

    interface Component {
        val value: String
    }

    data class ComponentsInDimension<COMPONENT: Component>(
        val dimension: Dimension<out COMPONENT>,
        val components: Set<COMPONENT>,
    )

    data class ComponentInDimension<COMPONENT: Component>(
        val dimension: Dimension<COMPONENT>,
        val component: COMPONENT,
    ) {
        val componentName: String
            get() = dimension.prefix + component.value
    }
}
