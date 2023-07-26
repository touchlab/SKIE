package co.touchlab.skie.plugin.analytics.producer

import co.touchlab.skie.configuration.SkieFeature
import co.touchlab.skie.configuration.TypedSkieConfiguration
import co.touchlab.skie.util.directory.SkieBuildDirectory

class AnalyticsCollector(
    private val skieBuildDirectory: SkieBuildDirectory,
    private val skieConfiguration: TypedSkieConfiguration<SkieFeature>,
) {

    @Synchronized
    fun collect(producers: List<AnalyticsProducer>) {
        producers.parallelStream().forEach {
            produceAndSave(it)
        }
    }

    @Synchronized
    fun collect(vararg producers: AnalyticsProducer) {
        collect(producers.toList())
    }

    private fun produceAndSave(producer: AnalyticsProducer) {
        if (producer.isEnabled) {
            val analyticsResult = producer.produce()

            skieBuildDirectory.analytics.file(producer.name).writeText(analyticsResult)
        }
    }

    private val AnalyticsProducer.isEnabled: Boolean
        get() = this.feature in skieConfiguration.enabledFeatures
}
