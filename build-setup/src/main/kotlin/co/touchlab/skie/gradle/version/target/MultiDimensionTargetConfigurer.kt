@file:OptIn(ExternalKotlinTargetApi::class)

package co.touchlab.skie.gradle.version.target

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.namedDomainObjectList
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExternalKotlinTargetApi
// import org.jetbrains.kotlin.gradle.dsl.HasConfigurableKotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.external.DecoratedExternalKotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.external.DecoratedExternalKotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.external.createExternalKotlinTarget

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
//         val target = kotlin.createExternalKotlinTarget<MyExternalTarget> {
//             this.configureIdeImport {
//                 registerImportAction {
//                     println("Runinng ide import")
//                 }
//             }
//         }
        allTargets.forEach { target ->
            kotlin.createTarget(target)
        }
    }
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
class MyExternalTarget(delegate: DecoratedExternalKotlinTarget.Delegate): DecoratedExternalKotlinTarget(delegate) /*, HasConfigurableKotlinCompilerOptions<KotlinJvmCompilerOptions> */
{
    val myCustomProperty: String = "hello there"

    override val compilations: NamedDomainObjectContainer<MyCustomCompilation>
        get() = super.compilations as NamedDomainObjectContainer<MyCustomCompilation>

//     @ExperimentalKotlinGradlePluginApi
//     override val compilerOptions: KotlinJvmCompilerOptions
//         get() = super.compilerOptions as KotlinJvmCompilerOptions
}

class MyCustomCompilation(delegate: DecoratedExternalKotlinCompilation.Delegate): DecoratedExternalKotlinCompilation(delegate) {
    val anotherCustomProperty: String = "hello world"
}
