package co.touchlab.skie.plugin.analytics.producer

interface AnalyticsProducer {

    val name: String

    fun produce(): ByteArray
}
