package co.touchlab.skie.plugin.generator.internal.analytics.hw

import co.touchlab.skie.configuration.features.SkieFeature
import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import co.touchlab.skie.util.Command

object HardwareAnalyticsProducer : AnalyticsProducer {

    override val name: String = "hw"

    override val feature: SkieFeature = SkieFeature.Analytics_Hardware

    override fun produce(): String =
        queryHwData()

    private fun queryHwData(): String =
        Command("system_profiler", "SPHardwareDataType", "-json")
            .execute()
            .outputLines
            .joinToString(System.lineSeparator())
}
