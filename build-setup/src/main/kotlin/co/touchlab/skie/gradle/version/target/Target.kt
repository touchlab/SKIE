package co.touchlab.skie.gradle.version.target

import org.gradle.api.Named
import kotlin.reflect.KClass

class Target(
    @get:JvmName("name")
    val name: String,
    val components: Tuple<Component>,
) : Named {

    override fun getName(): String = name

    @PublishedApi
    internal val componentsByType: Map<KClass<out Component>, Component> = components.associateBy { it::class }
        .also { check(it.size == components.size) { "Duplicate component types in $components!" } }

    inline fun <reified COMPONENT : Component> component(): COMPONENT {
        return componentsByType[COMPONENT::class] as COMPONENT
    }

    sealed interface Dimension<COMPONENT : Component> {

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

    data class ComponentsInDimension<COMPONENT : Component>(
        val dimension: Dimension<out COMPONENT>,
        val components: Set<COMPONENT>,
    )

    data class ComponentInDimension<COMPONENT : Component>(
        val dimension: Dimension<COMPONENT>,
        val component: COMPONENT,
    ) {

        val componentName: String
            get() = dimension.prefix + component.value
    }
}

val <COMPARABLE_COMPONENT> Target.Dimension<COMPARABLE_COMPONENT>.latest: COMPARABLE_COMPONENT where COMPARABLE_COMPONENT : Target.Component, COMPARABLE_COMPONENT : Comparable<COMPARABLE_COMPONENT>
    get() = components.maxOrNull() ?: error("No components in $this!")
