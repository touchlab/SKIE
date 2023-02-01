package co.touchlab.skie.plugin

import co.touchlab.skie.configuration.features.SkieFeature
import co.touchlab.skie.configuration.features.SkieFeatureSet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class SkieFeatureConfiguration @Inject constructor(objects: ObjectFactory) {

    val suspendInterop: Property<Boolean> = objects.property(Boolean::class.java).convention(true)
    val fqNames: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    val swiftRuntime: Property<Boolean> = objects.property(Boolean::class.java).convention(true)

    internal fun buildFeatureSet(): SkieFeatureSet =
        setOfNotNull(
            SkieFeature.SuspendInterop takeIf suspendInterop,
            SkieFeature.FqNames takeIf fqNames,
            SkieFeature.SwiftRuntime takeIf swiftRuntime,
        ).let { SkieFeatureSet(it) }

    private infix fun SkieFeature.takeIf(property: Property<Boolean>): SkieFeature? =
        this.takeIf { property.get() }
}
