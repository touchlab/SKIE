package co.touchlab.skie.gradle.version.target

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import javax.inject.Inject

data class MultiDimensionTargetConfiguration(
    val dimensions: List<Target.Dimension<*>>,
    val createTarget: KotlinMultiplatformExtension.(Target) -> KotlinTarget,
)

abstract class MultiDimensionTargetExtension @Inject constructor(
    objects: ObjectFactory,
    private val targetConfigurer: MultiDimensionTargetConfigurer,
) {
    internal val dimensions: ListProperty<Target.Dimension<*>> = objects.listProperty()

    val sourceSetConfigureActions: ListProperty<ConfigureSourceSetScope.(SourceSet) -> Unit> = objects.listProperty()

    init {
        sourceSetConfigureActions.convention(emptyList())
    }

    fun dimensions(vararg dimensions: Target.Dimension<*>, createTarget: KotlinMultiplatformExtension.(Target) -> KotlinTarget) {
        val dimensionList = dimensions.toList()
        this.dimensions.set(dimensionList)
        this.dimensions.disallowChanges()
        targetConfigurer.configure(dimensionList, createTarget)
    }

    fun configureSourceSet(block: ConfigureSourceSetScope.(SourceSet) -> Unit) {
        sourceSetConfigureActions.add(block)
    }
}
