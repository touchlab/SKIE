package co.touchlab.skie.gradle.version.target

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import javax.inject.Inject

abstract class MultiDimensionTargetExtension @Inject constructor(private val onConfigurationComplete: () -> Unit) {
    abstract val dimensions: ListProperty<Target.Dimension<*>>

    abstract val createTarget: Property<KotlinMultiplatformExtension.(Target) -> KotlinTarget>

    abstract val configureSourceSet: Property<ConfigureSourceSetScope.(SourceSet) -> Unit>

    fun dimensions(vararg dimensions: Target.Dimension<*>) {
        this.dimensions.set(dimensions.toList())
        notifyConfigurationCompleted()
    }

    fun createTarget(block: KotlinMultiplatformExtension.(Target) -> KotlinTarget) {
        createTarget.set(block)
        notifyConfigurationCompleted()
    }

    fun configureSourceSet(block: ConfigureSourceSetScope.(SourceSet) -> Unit) {
        configureSourceSet.set(block)
        notifyConfigurationCompleted()
    }

    private fun notifyConfigurationCompleted() {
        val properties = listOf(
            dimensions,
            createTarget,
            configureSourceSet,
        )

        if (properties.all { it.isPresent }) {
            properties.forEach { it.disallowChanges() }
            onConfigurationComplete()
        }
    }
}
