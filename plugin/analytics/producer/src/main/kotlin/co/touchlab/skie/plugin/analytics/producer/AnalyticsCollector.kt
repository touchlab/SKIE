package co.touchlab.skie.plugin.analytics.producer

import co.touchlab.skie.plugin.analytics.producer.producers.produceSafely
import co.touchlab.skie.plugin.analytics.producer.compressor.AnalyticsCompressor
import co.touchlab.skie.plugin.analytics.producer.compressor.EfficientAnalyticsCompressor
import co.touchlab.skie.plugin.analytics.producer.compressor.FastAnalyticsCompressor
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.io.path.writeBytes

class AnalyticsCollector(
    private val analyticsDirectory: Path,
    private val buildId: String,
) {

    private val backgroundTasks = CopyOnWriteArrayList<Thread>()

    @Synchronized
    fun collect(producers: List<AnalyticsProducer>) {
        producers.parallelStream().forEach {
            collectWithoutSynchronization(it)
        }
    }

    @Synchronized
    fun collect(vararg producers: AnalyticsProducer) {
        collect(producers.toList())
    }

    private fun collectWithoutSynchronization(producer: AnalyticsProducer) {
        val analyticsResult = producer.produceSafely()

        collectUsingDeflate(analyticsResult)
        collectUsingBZip2(analyticsResult)
    }

    private fun collectUsingDeflate(analyticsResult: AnalyticsProducer.Result) {
        collectUsingCompressor(analyticsResult, FastAnalyticsCompressor, 1)
    }

    private fun collectUsingBZip2(analyticsResult: AnalyticsProducer.Result) {
        val betterCompressionTask = Thread {
            collectUsingCompressor(analyticsResult, EfficientAnalyticsCompressor, 2)
        }

        betterCompressionTask.start()

        backgroundTasks.add(betterCompressionTask)
    }

    private fun collectUsingCompressor(
        analyticsResult: AnalyticsProducer.Result,
        compressor: AnalyticsCompressor,
        fileVersion: Int,
    ) {
        try {
            val fileName = FileWithMultipleVersions.addVersion("$buildId.${analyticsResult.name}", fileVersion)
            val file = analyticsDirectory.resolve(fileName)

            val compressedData = compressor.compress(analyticsResult.data)

            val encryptedData = encrypt(compressedData)

            file.writeBytes(encryptedData)
        } catch (_: Throwable) {
            // TODO Log to bugsnag
        }
    }

    private fun encrypt(data: ByteArray): ByteArray =
        AnalyticsEncryptor.encrypt(data)

    @Synchronized
    fun waitForBackgroundTasks() {
        backgroundTasks.forEach {
            it.join()
        }
    }
}
