package co.touchlab.skie.plugin.analytics.producer

import co.touchlab.skie.plugin.analytics.producer.collectors.produceSafely
import co.touchlab.skie.plugin.analytics.producer.compressor.AnalyticsCompressor
import co.touchlab.skie.plugin.analytics.producer.compressor.EfficientAnalyticsCompressor
import co.touchlab.skie.plugin.analytics.producer.compressor.FastAnalyticsCompressor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.io.path.writeBytes

class AnalyticsCollector(
    private val analyticsDirectory: Path,
    private val buildId: String,
) {

    private val backgroundScope = CoroutineScope(Dispatchers.Default)

    private val backgroundJobs = CopyOnWriteArrayList<Job>()

    private var isFinalized = false

    @Synchronized
    fun collect(producer: AnalyticsProducer) {
        checkIfNotFinalized()

        collectWithoutCheck(producer)
    }

    @Synchronized
    fun collectAll(producers: List<AnalyticsProducer>) {
        checkIfNotFinalized()

        producers.parallelStream().forEach {
            collectWithoutCheck(it)
        }
    }

    fun collectAll(vararg producers: AnalyticsProducer) {
        collectAll(producers.toList())
    }

    private fun collectWithoutCheck(producer: AnalyticsProducer) {
        val analyticsResult = producer.produceSafely()

        collectUsingDeflate(analyticsResult)
        collectUsingBZip2(analyticsResult)
    }

    private fun collectUsingDeflate(analyticsResult: AnalyticsProducer.Result) {
        collectUsingCompressor(analyticsResult, FastAnalyticsCompressor, 1)
    }

    private fun collectUsingBZip2(analyticsResult: AnalyticsProducer.Result) {
        val betterCompressionJob = backgroundScope.launch {
            collectUsingCompressor(analyticsResult, EfficientAnalyticsCompressor, 2)
        }

        backgroundJobs.add(betterCompressionJob)
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
    fun finalize() {
        checkIfNotFinalized()
        isFinalized = true

        runBlocking {
            backgroundJobs.joinAll()
        }
    }

    private fun checkIfNotFinalized() {
        if (isFinalized) {
            throw IllegalStateException("AnalyticsCollector was already finalized.")
        }
    }
}
