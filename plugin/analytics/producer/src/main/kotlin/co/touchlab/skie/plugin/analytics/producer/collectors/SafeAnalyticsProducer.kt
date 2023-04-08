package co.touchlab.skie.plugin.analytics.producer.collectors

import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer

class SafeAnalyticsProducer(
    private val unsafeAnalyticsProducer: AnalyticsProducer,
) : AnalyticsProducer {

    override fun produce(): AnalyticsProducer.Result =
        try {
            unsafeAnalyticsProducer.produce()
        } catch (e: Throwable) {
            ErrorAnalyticsProducer("${unsafeAnalyticsProducer::class.simpleName}-error", e).produce()
        }
}

fun AnalyticsProducer.produceSafely(): AnalyticsProducer.Result =
    SafeAnalyticsProducer(this).produce()
