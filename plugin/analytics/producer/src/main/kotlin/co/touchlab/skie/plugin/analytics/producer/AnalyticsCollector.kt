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
import kotlin.io.path.writeBytes

class AnalyticsCollector(
    private val analyticsDirectory: Path,
    private val buildId: String,
) {

    private val backgroundScope = CoroutineScope(Dispatchers.Default)

    private val backgroundJobs = mutableListOf<Job>()

    private var isFinalized = false

    @Synchronized
    fun collect(producer: AnalyticsProducer) {
        checkIfNotFinalized()

        val analyticsResult = producer.produceSafely()

        collectUsingDeflate(analyticsResult)
        collectUsingBZip2(analyticsResult)
    }

    private fun collectUsingDeflate(analyticsResult: AnalyticsProducer.Result) {
        collect(analyticsResult, FastAnalyticsCompressor, 1)
    }

    private fun collectUsingBZip2(analyticsResult: AnalyticsProducer.Result) {
        val betterCompressionJob = backgroundScope.launch {
            collect(analyticsResult, EfficientAnalyticsCompressor, 2)
        }

        backgroundJobs.add(betterCompressionJob)
    }

    private fun collect(
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
