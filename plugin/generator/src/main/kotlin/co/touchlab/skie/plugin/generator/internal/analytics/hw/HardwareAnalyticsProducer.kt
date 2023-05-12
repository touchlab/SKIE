package co.touchlab.skie.plugin.generator.internal.analytics.hw

import co.touchlab.skie.plugin.analytics.configuration.AnalyticsFeature
import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import co.touchlab.skie.util.Command
import kotlin.reflect.KClass

object HardwareAnalyticsProducer : AnalyticsProducer<AnalyticsFeature.Hardware> {

    override val featureType: KClass<AnalyticsFeature.Hardware> = AnalyticsFeature.Hardware::class

    override val name: String = "hw"

    override fun produce(configuration: AnalyticsFeature.Hardware): ByteArray =
        queryHwData().toByteArray()

    private fun queryHwData(): String =
        Command("system_profiler", "SPHardwareDataType", "-json")
            .execute()
            .outputLines
            .joinToString(System.lineSeparator())
}
