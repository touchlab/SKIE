package co.touchlab.skie.plugin.analytics.hardware

import co.touchlab.skie.configuration.SkieFeature
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import co.touchlab.skie.plugin.util.toPrettyJson
import co.touchlab.skie.util.Command
import co.touchlab.skie.util.hashed

internal data class TrackingHardwareAnalytics(
    val hashedPlatformUUID: String,
) {

    object Producer : AnalyticsProducer {

        override val name: String = "tracking-hardware"

        override val feature: SkieFeature = SkieFeature.Analytics_Tracking_Hardware

        override fun produce(): String =
            TrackingHardwareAnalytics(
                hashedPlatformUUID = getHashedPlatformUUID(),
            ).toPrettyJson()

        private fun getHashedPlatformUUID(): String =
            Command("system_profiler", "SPHardwareDataType")
                .execute()
                .outputLines
                .single { it.contains("Hardware UUID") }
                .split(":")[1]
                .trim()
                .hashed()
    }
}
