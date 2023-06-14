package co.touchlab.skie.plugin.analytics.producer.compressor

// TODO Refactor to enum
sealed interface AnalyticsCompressor {

    val method: CompressionMethod

    fun compress(data: ByteArray): ByteArray

    fun decompress(data: ByteArray): ByteArray
}
