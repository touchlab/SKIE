package co.touchlab.skie.plugin

import co.touchlab.skie.configuration.features.SkieFeature
import co.touchlab.skie.configuration.features.SkieFeatureSet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class SkieFeatureConfiguration @Inject constructor(objects: ObjectFactory) {

    val suspendInterop: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    val fqNames: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    internal fun buildFeatureSet(): SkieFeatureSet =
        listOf(
            SkieFeature.SuspendInterop to suspendInterop,
            SkieFeature.FqNames to fqNames,
        )
            .filter { it.second.get() }
            .map { it.first }
            .toSet()
            .let { SkieFeatureSet(it) }
}
