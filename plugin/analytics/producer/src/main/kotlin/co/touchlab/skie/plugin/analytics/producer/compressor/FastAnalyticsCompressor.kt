package co.touchlab.skie.plugin.analytics.producer.compressor

import org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream
import org.apache.commons.compress.compressors.deflate.DeflateCompressorOutputStream

object FastAnalyticsCompressor : BaseAnalyticsCompressor(
    compressorStreamFactory = { DeflateCompressorOutputStream(it) },
    decompressorStreamFactory = { DeflateCompressorInputStream(it) },
)
