package co.touchlab.skie.plugin.analytics.performance

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import co.touchlab.skie.plugin.util.toPrettyJson
import java.time.Duration

data class GradlePerformanceAnalytics(val linkTaskDurationInSeconds: Double) {

    class Producer(private val linkTaskDuration: Duration) : AnalyticsProducer {

        override val name: String = "gradle-performance"

        override val configurationFlag: SkieConfigurationFlag = SkieConfigurationFlag.Analytics_GradlePerformance

        override fun produce(): String = GradlePerformanceAnalytics(
            linkTaskDurationInSeconds = linkTaskDuration.toMillis().toDouble() / 1000.0,
        ).toPrettyJson()
    }
}
