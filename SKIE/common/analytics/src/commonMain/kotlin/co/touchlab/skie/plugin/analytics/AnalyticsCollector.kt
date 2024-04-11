package co.touchlab.skie.plugin.analytics

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.configuration.UntypedSkieConfigurationData
import co.touchlab.skie.util.directory.SkieBuildDirectory

class AnalyticsCollector(
    private val skieBuildDirectory: SkieBuildDirectory,
    private val skieConfiguration: UntypedSkieConfigurationData<SkieConfigurationFlag>,
) {

    fun collectAsync(producers: List<AnalyticsProducer>) {
        Thread {
            collectSynchronously(producers)
        }.start()
    }

    fun collectAsync(vararg producers: AnalyticsProducer) {
        collectAsync(producers.toList())
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
            val analyticsResult = try {
                producer.produce()
            } catch (e: Throwable) {
                handleProducerError(e)
            }

            skieBuildDirectory.analytics.file(producer.name).writeText(analyticsResult)
        }
    }

    private fun handleProducerError(e: Throwable): String {
        if (SkieConfigurationFlag.Debug_CrashOnSoftErrors in skieConfiguration.enabledConfigurationFlags) {
            throw e
        }

        return """
            {
                "error": "${e.message}"
            }
            """.trimIndent()
    }

    private val AnalyticsProducer.isEnabled: Boolean
        get() = this.configurationFlag in skieConfiguration.enabledConfigurationFlags
}
