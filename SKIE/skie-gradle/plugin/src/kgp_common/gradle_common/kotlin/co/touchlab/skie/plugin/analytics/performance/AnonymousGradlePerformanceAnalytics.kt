package co.touchlab.skie.plugin.analytics.performance

import co.touchlab.skie.configuration.SkieFeature
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import co.touchlab.skie.plugin.util.toPrettyJson
import java.time.Duration

internal data class AnonymousGradlePerformanceAnalytics(
    val linkTaskDurationInSeconds: Double,
) {

    class Producer(
        private val linkTaskDuration: Duration,
    ) : AnalyticsProducer {

        override val name: String = "anonymous-gradle-performance"

        override val feature: SkieFeature = SkieFeature.Analytics_Anonymous_GradlePerformance

        override fun produce(): String =
            AnonymousGradlePerformanceAnalytics(
                linkTaskDurationInSeconds = linkTaskDuration.toMillis().toDouble() / 1000.0,
            ).toPrettyJson()
    }
}
