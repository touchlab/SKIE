package co.touchlab.skie.plugin.analytics

import co.touchlab.skie.configuration.SkieFeature
import co.touchlab.skie.configuration.TypedSkieConfiguration
import co.touchlab.skie.util.directory.SkieBuildDirectory

class AnalyticsCollector(
    private val skieBuildDirectory: SkieBuildDirectory,
    private val skieConfiguration: TypedSkieConfiguration<SkieFeature>,
) {

    fun collect(producers: List<AnalyticsProducer>) {
        Thread {
            collectSynchronously(producers)
        }.start()
    }

    fun collect(vararg producers: AnalyticsProducer) {
        collect(producers.toList())
    }

    fun collectSynchronously(producers: List<AnalyticsProducer>) {
        producers
            .parallelStream()
            .forEach {
                produceAndSave(it)
            }
    }

    fun collectSynchronously(vararg producers: AnalyticsProducer) {
        collectSynchronously(producers.toList())
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
