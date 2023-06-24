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

abstract class MultiDimensionTargetExtension @Inject constructor(objects: ObjectFactory) {
    internal val configuration: Property<MultiDimensionTargetConfiguration> = objects.property()

    val sourceSetConfigureActions: ListProperty<ConfigureSourceSetScope.(SourceSet) -> Unit> = objects.listProperty()

    init {
        sourceSetConfigureActions.convention(emptyList())
    }

    fun dimensions(vararg dimensions: Target.Dimension<*>, createTarget: KotlinMultiplatformExtension.(Target) -> KotlinTarget) {
        configuration.set(
            MultiDimensionTargetConfiguration(
                dimensions.toList(),
                createTarget
            )
        )
    }

    fun configureSourceSet(block: ConfigureSourceSetScope.(SourceSet) -> Unit) {
        sourceSetConfigureActions.add(block)
    }

}
