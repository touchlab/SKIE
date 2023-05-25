package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.configuration.features.SkieFeature
import co.touchlab.skie.configuration.features.SkieFeatureSet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setProperty
import javax.inject.Inject

abstract class SkieDebugConfiguration @Inject constructor(objects: ObjectFactory)  {

    val dumpSwiftApiBeforeApiNotes: Property<Boolean> = objects.property(Boolean::class.java)
    val dumpSwiftApiAfterApiNotes: Property<Boolean> = objects.property(Boolean::class.java)

    internal fun buildFeatureSet(): SkieFeatureSet =
        setOfNotNull(
            SkieFeature.DumpSwiftApiBeforeApiNotes takeIf dumpSwiftApiBeforeApiNotes,
            SkieFeature.DumpSwiftApiAfterApiNotes takeIf dumpSwiftApiAfterApiNotes,
        ).let { SkieFeatureSet(it) }
}
