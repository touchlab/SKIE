package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.configuration.SkieFeature
import co.touchlab.skie.plugin.configuration.util.takeIf
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class SkieFeatureConfiguration @Inject constructor(objects: ObjectFactory) {

    val coroutinesInterop: Property<Boolean> = objects.property(Boolean::class.java).convention(true)
    val fqNames: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    val enableParallelSwiftCompilation: Property<Boolean> = objects.property(Boolean::class.java).convention(true)
    val wildcardExport: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    internal fun buildFeatureSet(): Set<SkieFeature> =
        setOfNotNull(
            SkieFeature.CoroutinesInterop takeIf coroutinesInterop,
            SkieFeature.FqNames takeIf fqNames,
            SkieFeature.ParallelSwiftCompilation takeIf enableParallelSwiftCompilation,
            SkieFeature.WildcardExport takeIf wildcardExport,
        )
}
