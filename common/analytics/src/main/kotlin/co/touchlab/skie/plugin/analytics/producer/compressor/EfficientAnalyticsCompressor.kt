package co.touchlab.skie.plugin.analytics.producer.compressor

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream

object EfficientAnalyticsCompressor : BaseAnalyticsCompressor(
    method = CompressionMethod.Bzip2,
    compressorStreamFactory = { BZip2CompressorOutputStream(it) },
    decompressorStreamFactory = { BZip2CompressorInputStream(it) },
)
