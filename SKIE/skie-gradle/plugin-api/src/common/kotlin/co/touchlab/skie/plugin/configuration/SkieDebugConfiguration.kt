package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.configuration.SkieFeature
import co.touchlab.skie.plugin.configuration.util.takeIf
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class SkieDebugConfiguration @Inject constructor(objects: ObjectFactory) {

    val dumpSwiftApiBeforeApiNotes: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    val dumpSwiftApiAfterApiNotes: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    val printSkiePerformanceLogs: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    internal fun buildFeatureSet(): Set<SkieFeature> =
        setOfNotNull(
            SkieFeature.Debug_DumpSwiftApiBeforeApiNotes takeIf dumpSwiftApiBeforeApiNotes,
            SkieFeature.Debug_DumpSwiftApiAfterApiNotes takeIf dumpSwiftApiAfterApiNotes,
            SkieFeature.Debug_PrintSkiePerformanceLogs takeIf printSkiePerformanceLogs,
        )
}
