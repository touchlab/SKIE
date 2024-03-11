package co.touchlab.skie.gradle.version.target

import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.namedDomainObjectList
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

class MultiDimensionTargetConfigurer(
    private val project: Project,
) {

    val allTargets = project.objects.namedDomainObjectList(Target::class)

    fun configure(
        dimensions: List<Target.Dimension<*>>,
        filter: (Target) -> Boolean,
        createTarget: KotlinMultiplatformExtension.(Target) -> KotlinTarget,
    ) {
        dimensions
            .fold(tupleSpaceOf<Target.ComponentInDimension<*>>(tupleOf())) { acc, dimension ->
                acc * dimension.componentsWithDimension
            }
            .map { tuple ->
                Target(
                    tuple.joinToString("__") { it.componentName },
                    tuple.map { it.component },
                )
            }
            .filter { filter(it) }
            .forEach {
                allTargets.add(it)
            }

        val kotlin = project.extensions.getByType<KotlinMultiplatformExtension>()
        allTargets.forEach { target ->
            kotlin.createTarget(target)
        }
    }
}
