package co.touchlab.skie.plugin.analytics.producer.compressor

import org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream
import org.apache.commons.compress.compressors.deflate.DeflateCompressorOutputStream

object FastAnalyticsCompressor : BaseAnalyticsCompressor(
    method = CompressionMethod.Deflate,
    compressorStreamFactory = { DeflateCompressorOutputStream(it) },
    decompressorStreamFactory = { DeflateCompressorInputStream(it) },
)
