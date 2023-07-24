package co.touchlab.skie.plugin.analytics.producer

import co.touchlab.skie.plugin.analytics.configuration.AnalyticsConfiguration
import co.touchlab.skie.plugin.analytics.configuration.AnalyticsConfigurationTarget
import co.touchlab.skie.plugin.analytics.configuration.AnalyticsFeature
import co.touchlab.skie.plugin.analytics.producer.compressor.AnalyticsCompressor
import co.touchlab.skie.plugin.analytics.producer.compressor.EfficientAnalyticsCompressor
import co.touchlab.skie.plugin.analytics.producer.compressor.FastAnalyticsCompressor
import co.touchlab.skie.util.Environment
import co.touchlab.skie.util.directory.SkieAnalyticsDirectories
import kotlin.io.path.writeBytes
import kotlin.reflect.KClass

class AnalyticsCollector(
    analyticsDirectories: SkieAnalyticsDirectories,
    private val buildId: String,
    private val skieVersion: String,
    private val configuration: AnalyticsConfiguration,
) : AnalyticsConfigurationTarget<AnalyticsFeature.CrashReporting> {

    override val featureType: KClass<AnalyticsFeature.CrashReporting> = AnalyticsFeature.CrashReporting::class

    private val analyticsDirectories = analyticsDirectories.forEnvironment(Environment.current).map { it.toPath() }

    @Synchronized
    fun collect(producers: List<AnalyticsProducer<*>>) {
        producers.parallelStream().forEach {
            produceAndCollectData(it)
        }
    }

    @Synchronized
    fun collect(vararg producers: AnalyticsProducer<*>) {
        collect(producers.toList())
    }

    private fun produceAndCollectData(producer: AnalyticsProducer<*>) {
        val feature = configuration.getFeature(producer)

        if (feature?.isEnabled == true) {
            @Suppress("UNCHECKED_CAST")
            val analyticsResult = (producer as AnalyticsProducer<AnalyticsFeature>).produce(feature)

            collectData(producer.name, analyticsResult)
        }
    }

    private fun collectData(analyticsType: String, data: ByteArray) {
        collectUsingDeflate(analyticsType, data)
        collectUsingBZip2(analyticsType, data)
    }

    private fun collectUsingDeflate(analyticsType: String, analyticsResult: ByteArray) {
        collectUsingCompressor(analyticsType, analyticsResult, FastAnalyticsCompressor)
    }

    private fun collectUsingBZip2(analyticsType: String, analyticsResult: ByteArray) {
        val betterCompressionTask = Thread {
            collectUsingCompressor(analyticsType, analyticsResult, EfficientAnalyticsCompressor)
        }

        betterCompressionTask.start()
    }

    private fun collectUsingCompressor(
        analyticsType: String,
        analyticsResult: ByteArray,
        compressor: AnalyticsCompressor,
    ) {
        val artifact = AnalyticsArtifact(
            buildId = buildId,
            type = analyticsType,
            skieVersion = skieVersion,
            environment = Environment.current,
            compressionMethod = compressor.method,
        )

        val compressedData = compressor.compress(analyticsResult)

        val encryptedData = encrypt(compressedData)

        analyticsDirectories
            .map { artifact.path(it) }
            .forEach {
                it.writeBytes(encryptedData)
            }
    }

    private fun encrypt(data: ByteArray): ByteArray =
        AnalyticsEncryptor.encrypt(data)
}
