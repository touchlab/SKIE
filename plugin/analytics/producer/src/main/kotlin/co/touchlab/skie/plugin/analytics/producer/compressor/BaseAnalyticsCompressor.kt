package co.touchlab.skie.plugin.analytics.producer.compressor

import org.apache.commons.compress.compressors.CompressorInputStream
import org.apache.commons.compress.compressors.CompressorOutputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

abstract class BaseAnalyticsCompressor(
    private val compressorStreamFactory: (OutputStream) -> CompressorOutputStream,
    private val decompressorStreamFactory: (InputStream) -> CompressorInputStream,
) : AnalyticsCompressor {

    override fun compress(data: ByteArray): ByteArray {
        val outputStream = ByteArrayOutputStream()

        val compressor = compressorStreamFactory(outputStream)

        compressor.use {
            it.write(data)
        }

        return outputStream.toByteArray()
    }

    override fun decompress(data: ByteArray): ByteArray {
        val inputStream = data.inputStream()

        val decompressor = decompressorStreamFactory(inputStream)

        return decompressor.use {
            it.readBytes()
        }
    }
}
