package co.touchlab.skie.plugin.analytics.producer.producers

import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer

class ErrorAnalyticsProducer(
    private val name: String,
    private val throwable: Throwable,
) : AnalyticsProducer {

    override fun produce(): AnalyticsProducer.Result {
        // TODO If development crash instead

        // TODO Log to bugsnag

        return AnalyticsProducer.Result(name, throwable.stackTraceToString().toByteArray())
    }
}
