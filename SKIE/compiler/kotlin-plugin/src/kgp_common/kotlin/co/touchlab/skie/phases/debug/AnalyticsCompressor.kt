package co.touchlab.skie.phases.debug

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream
import java.io.ByteArrayOutputStream

object AnalyticsCompressor {

    fun compress(data: ByteArray): ByteArray {
        val outputStream = ByteArrayOutputStream()

        val compressor = BZip2CompressorOutputStream(outputStream)

        compressor.use {
            it.write(data)
        }

        return outputStream.toByteArray()
    }

    fun decompress(data: ByteArray): ByteArray {
        val inputStream = data.inputStream()

        val decompressor = BZip2CompressorInputStream(inputStream)

        return decompressor.use {
            it.readBytes()
        }
    }
}
