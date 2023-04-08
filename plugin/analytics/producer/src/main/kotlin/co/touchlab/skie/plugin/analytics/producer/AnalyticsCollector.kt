package co.touchlab.skie.plugin.analytics.producer

import co.touchlab.skie.plugin.analytics.producer.collectors.produceSafely
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.commons.compress.compressors.CompressorOutputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream
import org.apache.commons.compress.compressors.deflate.DeflateCompressorOutputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream
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
        collect(analyticsResult, { DeflateCompressorOutputStream(it) }, "1")
    }

    private fun collectUsingBZip2(analyticsResult: AnalyticsProducer.Result) {
        val betterCompressionJob = backgroundScope.launch {
            collect(analyticsResult, { BZip2CompressorOutputStream(it) }, "2")
        }

        backgroundJobs.add(betterCompressionJob)
    }

    private fun collect(
        analyticsResult: AnalyticsProducer.Result,
        compressorFactory: (OutputStream) -> CompressorOutputStream,
        fileSuffix: String,
    ) {
        try {
            val fileName = "$buildId.${analyticsResult.name}.$fileSuffix"
            val file = analyticsDirectory.resolve(fileName)

            val compressedData = compress(analyticsResult.data, compressorFactory)

            val encryptedData = encrypt(compressedData)

            file.writeBytes(encryptedData)
        } catch (_: Throwable) {
            // TODO Log to bugsnag
        }
    }

    private fun compress(data: ByteArray, compressorFactory: (OutputStream) -> CompressorOutputStream): ByteArray {
        val outputStream = ByteArrayOutputStream()

        val compressor = compressorFactory(outputStream)

        compressor.use {
            it.write(data)
        }

        return outputStream.toByteArray()
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
