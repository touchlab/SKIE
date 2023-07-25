package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.configuration.features.SkieFeature
import co.touchlab.skie.configuration.features.SkieFeatureSet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class SkieDebugConfiguration @Inject constructor(objects: ObjectFactory)  {

    val dumpSwiftApiBeforeApiNotes: Property<Boolean> = objects.property(Boolean::class.java)
    val dumpSwiftApiAfterApiNotes: Property<Boolean> = objects.property(Boolean::class.java)
    val printSkiePerformanceLogs: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    internal fun buildFeatureSet(): SkieFeatureSet =
        setOfNotNull(
            SkieFeature.Debug_DumpSwiftApiBeforeApiNotes takeIf dumpSwiftApiBeforeApiNotes,
            SkieFeature.Debug_DumpSwiftApiAfterApiNotes takeIf dumpSwiftApiAfterApiNotes,
            SkieFeature.Debug_PrintSkiePerformanceLogs takeIf printSkiePerformanceLogs,
        ).let { SkieFeatureSet(it) }
}
